package server.domain;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Request;
import common.data.models.Response;
import common.domain.command.HistoryKeeper;
import common.domain.command.Invoker;
import common.domain.command.Serializer;
import server.data.UserRepository;
import server.data.RemoteRepository;

import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

public class Server {
    private static Server instance;
    private final Map<SocketChannel, ClientContext> clients = new HashMap<>();
    private static Hashtable<Integer, HumanBeing> globalCollection = new Hashtable<>();
    private static String fileName = "";
    private static boolean hasFileName = false;
    private final ExecutorService readPool = Executors.newCachedThreadPool();
    private final ExecutorService processPool = Executors.newFixedThreadPool(8); // число потоков можно настроить
    private final ForkJoinPool sendPool = new ForkJoinPool();
    private static final ReentrantLock collectionLock = new ReentrantLock();

    private Server() {
    }

    public static Server getInstance() {
        if (instance == null) {
            instance = new Server();
        }
        return instance;
    }

    public void start() {
        Serializer serializer = Serializer.getInstance();
        Scanner sc = new Scanner(System.in);
        HistoryKeeper historyKeeper = HistoryKeeper.getInstance();
        Invoker invoker = Invoker.getInstance();
        invoker.invokerInit();


        int PORT = 0;
        while (true) {
            try {
                System.out.print("Введите порт: ");
                PORT = sc.nextInt();
            } catch (InputMismatchException e) {
                System.out.println(e);
                sc.nextLine();
            }

            try (ServerSocketChannel ssc = ServerSocketChannel.open();) {
                ssc.configureBlocking(false);
                ssc.socket().bind(new InetSocketAddress(PORT));
                System.out.println("Сервер запущен на порту " + PORT);

                // Загружаем коллекцию из БД при старте
                RemoteRepository repo = new RemoteRepository();
                globalCollection = repo.readData();
                System.out.println("Коллекция загружена из БД. Готов к работе с командами...");

                Selector selector = Selector.open();
                ssc.register(selector, SelectionKey.OP_ACCEPT);

                while (true) {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) continue;

                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();

                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();

                        if (key.isAcceptable()) {
                            acceptClient(key, selector);
                        } else if (key.isReadable()) {
                            readPool.submit(() -> {
                                try {
                                    readClientData(key, serializer, invoker, historyKeeper);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                }
            } catch (BindException e) {
                System.out.println("Ошибка: " + e);
            } catch (IOException e) {
                System.err.println("Ошибка сервера: " + e.getMessage());
            }
        }
    }

    private void acceptClient(SelectionKey key, Selector selector) throws IOException {
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel clientSocketChannel = ssc.accept();
        if (clientSocketChannel == null) return;

        clientSocketChannel.configureBlocking(false);
        clientSocketChannel.register(selector, SelectionKey.OP_READ);

        // Инициализируем контекст клиента
        ClientContext context = new ClientContext();
        context.collection = globalCollection; // Теперь у каждого клиента есть ссылка на коллекцию
        clients.put(clientSocketChannel, context);
        System.out.println("Клиент " + clientSocketChannel.getRemoteAddress() + " подключен");
    }

    private void readClientData(SelectionKey key, Serializer serializer, Invoker invoker, HistoryKeeper historyKeeper) throws IOException {
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        ClientContext context = clients.get(clientSocketChannel);

        if (context == null) {
            clientSocketChannel.close();
            return;
        }

        processPool.submit(() -> {
            try {
                processCommands(clientSocketChannel, context, serializer, invoker, historyKeeper);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void processCommands(SocketChannel clientSocketChannel, ClientContext context, Serializer serializer, Invoker invoker, HistoryKeeper historyKeeper) throws IOException, ClassNotFoundException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(8192);
        int bytesRead = clientSocketChannel.read(buffer);
        if (bytesRead > 0) {
            if (!buffer.hasRemaining()) {
                System.out.println("Получены нулевые данные.");
                return;
            }
            buffer.flip();

            byte[] data = new byte[buffer.remaining()];
            buffer.get(data);

            try {
                Request request = (Request) serializer.deserialize(data);
                System.out.println("Получена команда: " + request.getCommand().getClass().getName());

                // Аутентификация пользователя
                String username = request.getUsername();
                String password = request.getPassword();
                System.out.printf("Пользователь: %s, Пароль: %s%n", username, password);
                UserRepository userRepo = new UserRepository();
                boolean authenticated = userRepo.authenticate(username, password);
                if (!authenticated) {
                    Response response = new Response(false, "Ошибка авторизации. Проверьте логин и пароль.", context.collection);
                    byte[] responseBytes = serializer.serialize(response);
                    clientSocketChannel.write(ByteBuffer.wrap(responseBytes));
                    return;
                }
                Integer userId = userRepo.getUserId(username);

                // Прокидываем userId в команды, если они поддерживают это
                collectionLock.lock();
                try {
                    if (request.getCommand() instanceof common.domain.command.DataCollector) {
                        String[] args = request.getArgs();
                        // Последний аргумент всегда userId (null на клиенте)
                        String[] newArgs = java.util.Arrays.copyOf(args, args.length + 1);
                        newArgs[newArgs.length - 1] = userId != null ? userId.toString() : null;
                        Response response = request.getCommand().execute(context.collection, newArgs);
                        String cmdName = invoker.getCommandName(request.getCommand());
                        historyKeeper.add(cmdName);
                        // --- Синхронизация с БД для команд, изменяющих коллекцию ---
                        String[] modifyingCommands = {"insert", "update", "remove_key", "remove_greater_key", "clear", "replace_if_greater"};
                        for (String modCmd : modifyingCommands) {
                            if (cmdName != null && cmdName.equalsIgnoreCase(modCmd) && response.isSuccess()) {
                                RemoteRepository repo = new RemoteRepository();
                                repo.writeData(context.collection);
                                break;
                            }
                        }
                        // --- конец блока синхронизации ---
                        sendPool.submit(() -> {
                            try {
                                byte[] responseBytes = serializer.serialize(response);
                                clientSocketChannel.write(ByteBuffer.wrap(responseBytes));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        Response response = request.getCommand().execute(context.collection, request.getArgs());
                        String cmdName = invoker.getCommandName(request.getCommand());
                        historyKeeper.add(cmdName);
                        sendPool.submit(() -> {
                            try {
                                byte[] responseBytes = serializer.serialize(response);
                                clientSocketChannel.write(ByteBuffer.wrap(responseBytes));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
                    }
                } finally {
                    collectionLock.unlock();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    // Контекст клиента для хранения состояния
    private static class ClientContext {
        ClientState state = ClientState.WAITING_FILE_NAME;
        String fileName;
        Hashtable<Integer, HumanBeing> collection;

        enum ClientState {
            WAITING_FILE_NAME,
            LOADING_COLLECTION,
            PROCESSING_COMMANDS
        }
    }
}
