package server.domain;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Request;
import common.data.models.Response;
import common.domain.command.*;
import common.domain.command.commands.LogInCommand;
import common.domain.command.commands.LogOutCommand;
import common.domain.command.commands.RegisterCommand;
import server.data.UserRepository;
import server.data.RemoteRepository;

import java.io.Console;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static Server instance;
    private static Hashtable<Integer, HumanBeing> globalCollection = new Hashtable<>();
    public static RemoteRepository remoteRepository;
    public static UserRepository userRepository = new UserRepository();
    private final ExecutorService readPool = Executors.newCachedThreadPool();
    private final ExecutorService processPool = Executors.newFixedThreadPool(8);
    private final ForkJoinPool sendPool = new ForkJoinPool();
    private static final ReentrantLock collectionLock = new ReentrantLock();
    public static Scanner SCANNER = new Scanner(System.in);
    private static Invoker invoker = Invoker.getInstance();
    private static Console console = System.console();
    private Set<SelectionKey> selectedKeys;
    private Map<SocketChannel, ClientSession> clientSessionMap = new ConcurrentHashMap<>();
    private int PORT = 0;


    private Server() {
        invoker.invokerInit();
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public void start() {
        prepareConnection();
        management();
    }

    private void prepareConnection() {
        System.out.println("Запуск сервера...");
        all:
        while (true) {
            int opt;
            try {
                System.out.println("Выберите режим работы:\n1) Сервер запущен локально\n2) Сервер запущен на гелиосе");
                System.out.print("Номер: ");
                opt = Integer.parseInt(SCANNER.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Попробуйте ещё раз");
                continue;
            }
            switch (opt) {
                case 1 -> {
                    System.out.println("Сервер запущен локально + SSH Tunnel для psql");
                    SSHTunnel tunnel = new SSHTunnel(console);
                    try {
                        tunnel.psqlTunnel();
                        remoteRepository = new RemoteRepository();
                    } catch (SQLException e) {
                        e.printStackTrace();
                        continue;
                    } catch (Exception e) {
                        e.printStackTrace();
                        continue;
                    }
                    break all;
                }
                case 2 -> {
                    System.out.println("Сервер запущен на гелиосе");
                    try {
                        remoteRepository = new RemoteRepository("jdbc:postgresql://pg:5432/studs", "s465877", "D7cCg1cMguDJeuwv");
                    } catch (SQLException e) {
                        e.printStackTrace();
                        continue;
                    }
                    break all;
                }
                default -> System.out.println("Некорректный ввод. Попробуйте ещё раз");
            }
        }
        globalCollection = remoteRepository.readData();

        int PORT = 0;
        while (true) {
            try {
                System.out.print("Введите порт: ");
                PORT = Integer.parseInt(SCANNER.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Попробуйте ещё раз");
            }
        }

    }

    private void management() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            serverSocketChannel.bind(new InetSocketAddress(PORT));
            serverSocketChannel.configureBlocking(false);
            // создаём selector - объект, отслеживающий каналы и их состояние
            Selector selector = Selector.open();
            // привязка селектора к сокету сервера
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

            System.out.println("Сервер запущен на порту " + PORT);
            while (true) {
                selector.select();
                selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while (iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if (key.isAcceptable()) {
                        try {
                            SocketChannel client = serverSocketChannel.accept();
                            client.configureBlocking(false);
                            // регистрация канала как готового к чтению
                            SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
                            ClientSession cs = new ClientSession(false);
                            clientKey.attach(cs);
                            clientSessionMap.put(client, cs);
                            System.out.println("Подключен клиент: " + client.getRemoteAddress());

                        } catch (CancelledKeyException e) {
                            System.out.println("Клиент разорвал соединение");
                        }
                    } else if (key.isReadable()) {
                        SocketChannel client = (SocketChannel) key.channel();
                        readPool.submit(() -> readData(client));
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка запуска сервера: " + e.getMessage());
        }
    }
    private void readData(SocketChannel client) {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int bytesRead = client.read(buffer);
            if (bytesRead == -1) {
                client.close();
                return;
            }
            if (bytesRead == 0) {
                return;
            }
            buffer.flip();
            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);
            processPool.submit(() -> handleRequest(client, data));
        } catch (IOException e) {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }

    private void handleRequest(SocketChannel client, byte[] data) {
        try {
            Request request = (Request) Serializer.getInstance().deserialize(data);
            Response response = processRequest(client, request);
            sendPool.submit(() -> sendResponse(client, response));
        } catch (Exception e) {
            e.printStackTrace(); // Логируем ошибку в консоль
            sendPool.submit(() -> {
                try {
                    Response error = new Response(false, "Ошибка обработки запроса: " + e, new Hashtable<>());
                    sendResponse(client, error);
                } catch (Exception ignored) {
                }
            });
        }
    }

    private Response processRequest(SocketChannel client, Request request) {
        Command command = request.getCommand();
        String commandName = invoker.getCommandName(command);
        boolean authenticated = clientSessionMap.get(client).getAuth();

        if (commandName == null) {
            return new Response(false, "Неизвестная команда", new Hashtable<>());
        }
        switch (command) {
            case RegisterCommand ignored -> {
                if (request.getArgs().length < 2) {
                    return new Response(false, "Необходимо указать логин и пароль", new Hashtable<>());
                }
                boolean success = userRepository.register(request.getArgs()[0], request.getArgs()[1]);
                if (success) {
                    return new Response(true, "Регистрация успешна", new Hashtable<>());
                } else {
                    return new Response(false, "Пользователь с таким именем уже существует", new Hashtable<>());
                }
            }
            case LogInCommand ignored -> {
                authenticated = userRepository.authenticate(request.getUsername(), request.getPassword());
                clientSessionMap.get(client).setAuth(authenticated);
                return authenticated
                        ? new Response(true, "Вы авторизовались", globalCollection)
                        : new Response(false, "Ошибка входа", new Hashtable<>());
            } case LogOutCommand ignored -> {
                if (!authenticated) return new Response(false, "Вы не авторизованы", new Hashtable<>());
                else {
                    authenticated = false;
                    clientSessionMap.get(client).setAuth(authenticated);
                }
            }
            default -> {
                if (!authenticated) {
                    clientSessionMap.get(client).setAuth(authenticated);
                    return new Response(false, "Пользователь не авторизован", new Hashtable<>());
                }

            }
        }

        // Выполнение команды

        if (command == null) {
            return new Response(false, "Команда не найдена", new Hashtable<>());
        }

        Integer userId = userRepository.getUserId(request.getUsername());
        String[] argsWithUserId;
        if (userId != null) {
            // Добавляем userId в конец args
            String[] origArgs = request.getArgs();
            argsWithUserId = new String[origArgs.length + 1];
            System.arraycopy(origArgs, 0, argsWithUserId, 0, origArgs.length);
            argsWithUserId[origArgs.length] = userId.toString();
        } else {
            argsWithUserId = request.getArgs();
        }

        HistoryKeeper.getInstance().add(commandName, userId);

        boolean isModifying = invoker.modifyingCommands.contains(commandName);
        if (isModifying) {
            collectionLock.lock();
            try {
                Hashtable<Integer, HumanBeing> tempCollection = new Hashtable<>(globalCollection);
                Response resp = command.execute(tempCollection, argsWithUserId);
                if (resp.isSuccess()) {
                    try {
                        remoteRepository.writeData(tempCollection);
                        globalCollection = tempCollection;
                        return new Response(true, resp.getMessage(), globalCollection);
                    } catch (Exception e) {
                        return new Response(false, "Ошибка при сохранении в БД: " + e.getMessage(), globalCollection);
                    }
                } else {
                    return resp;
                }
            } finally {
                collectionLock.unlock();
            }
        } else {
            return command.execute(globalCollection, argsWithUserId);
        }
    }

    private void sendResponse(SocketChannel client, Response response) {
        try {
            byte[] data = Serializer.getInstance().serialize(response);
            ByteBuffer buffer = ByteBuffer.wrap(data);
            while (buffer.hasRemaining()) {
                client.write(buffer);
            }
        } catch (IOException e) {
            try {
                client.close();
            } catch (IOException ignored) {
            }
        }
    }

    private class ClientSession {
        private boolean authenticated;

        ClientSession(boolean authenticated) {
            this.authenticated = authenticated;
        }

        public void setAuth(boolean authenticated) {
            this.authenticated = authenticated;
        }

        public boolean getAuth() {
            return authenticated;
        }
    }
}
