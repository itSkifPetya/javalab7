package client.domain;

import com.jcraft.jsch.JSchException;
import common.data.models.Request;
import common.data.models.Response;
import common.domain.command.SSHTunnel;
import common.domain.command.Serializer;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.InputMismatchException;

public class Client {
    private static Client instance;
    private static final Console CONSOLE = System.console();
    private SSHTunnel tunnel;
    private String username;
    private String password;
    private String checkpassword;

    private Client() {
    }

    public static Client getInstance() {
        if (instance == null) instance = new Client();
        return instance;
    }

    public void start() {
        Serializer serializer = Serializer.getInstance();
        CommandHandler handler = CommandHandler.getInstance();
        int PORT = 0;
        int opt;

        while (true) {
            try {
                System.out.println("Выберите режим работы:\n1) Сервер и клиент на одном компьютере\n2) Сервер запущен на гелиосе, проброс портов для клиента");
                System.out.print("Номер: ");
                opt = Integer.parseInt(CONSOLE.readLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Попробуйте ещё раз");
            }
        }
        switch (opt) {
            case 1 -> {
                while (true) {
                    try {
                        System.out.print("Введите порт: ");
                        PORT = Integer.parseInt(CONSOLE.readLine());
                        break;
                    } catch (NumberFormatException e) {
                        System.err.println("Ошибка формата");
                    }
                }
            }
            case 2 -> {
                tunnel = new SSHTunnel(CONSOLE);
                try {
                    tunnel.baseTunnel();
                } catch (JSchException e) {
                    throw new RuntimeException(e);
                }
                // получаем порт, который будем использовать для подключения к серверу через туннель
                PORT = tunnel.getLocalPort();
            }
        }
        System.out.println(PORT);
        // порт зависит от opt
        try (Socket socket = new Socket("localhost", PORT)) {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            boolean authenticated = false;
            while (!authenticated) {
                System.out.println("1) Войти\n2) Зарегистрироваться");
                System.out.print("Выберите действие: ");
                int choice = Integer.parseInt(CONSOLE.readLine());
                switch (choice) {
                    case 1 -> {
                        Request loginRequest = handler.collectRequest("login", new String[]{}, "", "");
                        serializer.serialize(loginRequest, os);
                        Response loginResponse = (Response) serializer.deserialize(is);
                        if (loginResponse.isSuccess()) {
                            System.out.println(loginResponse.getMessage());
                            authenticated = true;
                            username = handler.getUsername();
                            password = handler.getPassword();
                            handler.setUsername("");
                            handler.setPassword("");
                        } else {
                            System.out.println("Ошибка авторизации: " + loginResponse.getMessage());
                        }
                    }
                    case 2 -> {
                        Request regRequest = handler.collectRequest("register", new String[]{}, "", "");
                        serializer.serialize(regRequest, os);
                        Response regResponse = (Response) serializer.deserialize(is);
                        if (regResponse.isSuccess()) {
                            System.out.println(regResponse.getMessage());
                        } else {
                            System.out.println("Ошибка регистрации: " + regResponse.getMessage());
                        }
                    }
                    default -> System.out.println("Некорректный выбор. Попробуйте ещё раз.");
                }
            }
            // После авторизации — основной цикл команд
            while (true) {
                System.out.print("> ");
                String input = CONSOLE.readLine();
                if ("exit".equalsIgnoreCase(input)) {
                    socket.close();
                    break;
                }
                String[] parts = input.split(" ", 2);
                String commandName = parts[0];
                String[] args = parts.length > 1 ? Arrays.stream(parts[1].split(" "))
                        .map(s -> s.trim())
                        .toList()
                        .toArray(new String[0])
                        : new String[0];
                try {
                    Request request = handler.collectRequest(commandName, args, username, password);
                    serializer.serialize(request, os);
                } catch (NullPointerException e) {
                    System.out.println("Команда не найдена. Повторите попытку");
                    continue;
                }
                Response response = (Response) serializer.deserialize(is);
                System.out.println(response.getMessage());
            }

        } catch (EOFException e) {
            System.err.println("Сбой подключения к серверу.");
            System.exit(404);
        } catch
         (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        catch (Exception e) {
//            System.out.println("Ошибка соединения: " + e.getMessage());
//        }
    }

    public static Console getConsole() {
        return CONSOLE;
    }
}
