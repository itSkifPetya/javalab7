package client.domain;
import com.jcraft.jsch.JSchException;
import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Request;
import common.data.models.Response;
import common.domain.command.Serializer;

import java.io.*;
import java.net.Socket;
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
        // --- Регистрация/логин пользователя ---
        // Сначала выбираем порт, чтобы использовать его для авторизации/регистрации
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
                tunnel = new SSHTunnel();
                try {
                    tunnel.start();
                } catch (JSchException e) {
                    throw new RuntimeException(e);
                }
                PORT = tunnel.getLocalPort();
            }
        }
        boolean authenticated = false;
        while (!authenticated) {
            System.out.println("1) Войти\n2) Зарегистрироваться");
            System.out.print("Выберите действие: ");
            String choice = SCANNER.nextLine();
            if ("1".equals(choice)) {
                System.out.print("Логин: ");
                username = SCANNER.nextLine();
                System.out.print("Пароль: ");
                password = SCANNER.nextLine();
                // Попытка авторизации (отправить info)
                try (Socket testSocket = new Socket("localhost", PORT)) {
                    Serializer testSerializer = Serializer.getInstance();
                    OutputStream testOs = testSocket.getOutputStream();
                    InputStream testIs = testSocket.getInputStream();
                    Request testRequest = handler.collectRequest("info", new String[]{}, username, password);
                    testSerializer.serialize(testRequest, testOs);
                    Response testResponse = (Response) testSerializer.deserialize(testIs);
                    if (testResponse.isSuccess()) {
                        System.out.println("Авторизация успешна!");
                        authenticated = true;
                    } else {
                        System.out.println("Ошибка авторизации: " + testResponse.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка соединения: " + e.getMessage());
                }
            } else if ("2".equals(choice)) {
                System.out.print("Придумайте логин: ");
                username = SCANNER.nextLine();
                System.out.print("Придумайте пароль: ");
                password = SCANNER.nextLine();
                String[] args = new String[]{ username, password };
                // Отправить команду регистрации
                try (Socket regSocket = new Socket("localhost", PORT)) {
                    Serializer regSerializer = Serializer.getInstance();
                    OutputStream regOs = regSocket.getOutputStream();
                    InputStream regIs = regSocket.getInputStream();
                    Request regRequest = handler.collectRequest("register", args, username, password);
                    regSerializer.serialize(regRequest, regOs);
                    Response regResponse = (Response) regSerializer.deserialize(regIs);
                    if (regResponse.isSuccess()) {
                        System.out.println("Регистрация успешна! Теперь войдите.");
                    } else {
                        System.out.println("Ошибка регистрации: " + regResponse.getMessage());
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка соединения: " + e.getMessage());
                }
            } else {
                System.out.println("Некорректный выбор. Попробуйте ещё раз.");
            }
        }

        try (Socket socket = new Socket("localhost", PORT)) {
            InputStream is = socket.getInputStream();
            OutputStream os = socket.getOutputStream();
            while (true) {
                // Этап 1: Отправляем имя файла
                System.out.print("Введите имя файла: ");
                String fileName = SCANNER.nextLine();
                String response = "";
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
                writer.write(fileName);
                writer.flush();

                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                reader.read(response.toCharArray());
                break;

            }
            // Этап 2: Начинаем обмен командами
            while (true) {
                System.out.print("> ");
                String input = SCANNER.nextLine();

                if ("exit".equalsIgnoreCase(input)) {
                    socket.close();
                    return;
                }

                String[] parts = input.split(" ", 2);
                String commandName = parts[0];
                String[] args = parts.length > 1 ? new String[]{parts[1]} : new String[0];
                Request request = handler.collectRequest(commandName, args, username, password);

                if (request.getCommand() == null) {
                    System.out.printf("Команда %s не распознана\n", commandName);
                    continue;
                }

                serializer.serialize(request, os);

                // Получаем ответ
                Response response = (Response) serializer.deserialize(is);
                System.out.println("Ответ сервера: \n" + response.getMessage());

                if (commandName.equals("show")) {
                    for (HumanBeing hb : response.getData().values()) {
                        System.out.println(hb.toPrettyString());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Ошибка клиента: " + e.getMessage());
        }
    }

    public static Scanner getSCANNER() {
        return SCANNER;
    }
}

