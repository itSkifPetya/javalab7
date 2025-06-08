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
import java.util.Scanner;

public class Client {
    private static Client instance;
    private static final Scanner SCANNER = new Scanner(System.in);
    private SSHTunnel tunnel;
    private String username;
    private String password;
    private Client() {}

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
                opt = Integer.parseInt(SCANNER.nextLine());
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
                        PORT = SCANNER.nextInt();
                        break;
                    } catch (InputMismatchException e) {
                        System.out.println(e);
                        SCANNER.nextLine();
                    }
                }
                SCANNER.nextLine();
            }
            case 2 -> {
                tunnel = new SSHTunnel(SCANNER);
                try {
                    tunnel.baseTunnel();
                } catch (JSchException e) {
                    throw new RuntimeException(e);
                }
                // получаем порт, который будем использовать для подключения к серверу через туннель
                PORT = tunnel.getLocalPort();
            }
        }
        // порт зависит от opt
        try (Socket socket = new Socket("localhost", PORT)) {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            boolean authenticated = false;
            while (!authenticated) {
                System.out.println("1) Войти\n2) Зарегистрироваться");
                System.out.print("Выберите действие: ");

                int choice = SCANNER.nextInt();
                switch (choice) {
                    case 1 -> {
                        System.out.print("Логин: ");
                        username = SCANNER.nextLine();
                        System.out.print("Пароль: ");
                        password = SCANNER.nextLine();
                        Request loginRequest = handler.collectRequest("login", new String[]{}, username, password);
                        Request testRequest = handler.collectRequest("info", new String[]{}, username, password);
                        serializer.serialize(testRequest, os);
                        Response testResponse = (Response) serializer.deserialize(is);
                        if (testResponse.isSuccess()) {
                            System.out.println("Авторизация успешна!");
                            authenticated = true;
                        } else {
                            System.out.println("Ошибка авторизации: " + testResponse.getMessage());
                        }
                    }
                    case 2 -> {
                        System.out.print("Придумайте логин: ");
                        username = SCANNER.nextLine();
                        System.out.print("Придумайте пароль: ");
                        password = SCANNER.nextLine();
                        String[] args = new String[]{ username, password };
                        Request regRequest = handler.collectRequest("register", args, username, password);
                        serializer.serialize(regRequest, os);
                        Response regResponse = (Response) serializer.deserialize(is);
                        if (regResponse.isSuccess()) {
                            System.out.println("Регистрация успешна! Теперь войдите.");
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
                String input = SCANNER.nextLine();
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
                Request request = handler.collectRequest(commandName, args, username, password);
                serializer.serialize(request, os);
                Response response = (Response) serializer.deserialize(is);
                System.out.println(response.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Ошибка соединения: " + e.getMessage());
        }
    }

    public static Scanner getSCANNER() {
        return SCANNER;
    }
}
