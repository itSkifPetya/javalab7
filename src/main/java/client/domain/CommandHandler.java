package client.domain;

import common.data.models.HumanBeingModel.WeaponType;
import common.data.models.Request;
import common.domain.command.Command;
import common.domain.command.DataCollector;
import common.domain.command.Invoker;
import common.domain.command.commands.LogInCommand;
import common.domain.command.commands.RegisterCommand;

import java.io.Console;
import java.util.ArrayList;
import java.util.List;

public class CommandHandler {
    private static CommandHandler instance;
    private String username = "";
    private Console CONSOLE = Client.getConsole();

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    private String password = "";

    private CommandHandler() {
    }

    public static CommandHandler getInstance() {
        if (instance == null) {
            instance = new CommandHandler();
        }
        return instance;
    }

    public Request collectRequest(String commandName, String[] args, String username, String password) {
        Command command = handle(commandName, args);
        String[] newArgs = args;
        switch (command) {
            case DataCollector ignored -> newArgs = dataCollectorArgsBuilder(args);
            case RegisterCommand ignored -> {
                newArgs = registerDataCollector();
                username = newArgs[0];
                password = newArgs[1];
            }
            case LogInCommand ignored -> {
                newArgs = loginDataCollector();
                username = newArgs[0];
                password = newArgs[1];
            }
            default -> {
                return new Request(command, args, username, password);
            }
        }
        return new Request(command, newArgs, username, password);
    }

    private Command handle(String commandName, String[] args) {
        Invoker invoker = Invoker.getInstance();
        invoker.invokerInit();
//        Map<String, Command> map = invoker.getCommandMap();
//        System.out.println(map);
        Command command = null;
        String[] newArgs = null;
        try {
            command = invoker.getCommandMap().get(commandName);
//            System.out.println(command);

        } catch (Exception e) {
            CONSOLE.printf("Такой команды нет. Для подробной информации используйте help");
//            System.out.println("Такой команды нет. Для подробной информации используйте help");
        }
//        System.out.println(commandName);
        if (command != null && args.length != command.getArgsCount()) {
            System.out.println("Некорректное количество аргументов");
            return null;
        }

        return command;
    }

    private String[] dataCollectorArgsBuilder(String[] args) {
        String key = args[0];
        ArrayList<String> argsList = new ArrayList<>(List.of(key));
        Console console = Client.getConsole();
        String inp;
        String name;
        Boolean realHero;
        Boolean hasToothpick;
        double impactSpeed;
        String soundtrackName;
        long minutesOfWaiting;
        WeaponType weaponType;
        boolean cool;
        int coordX = 0;
        double coordY = 0.0;


        while (true) {
            try {
                System.out.print("Введите имя пострадавшего: ");
                name = console.readLine();
                if (name == null || name.trim().isEmpty()) {
                    throw new IllegalArgumentException("Имя не может быть пустым.");
                }
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка:" + e);
            }

        }
        argsList.add(name);

        while (true) {
            try {
                System.out.print("Введите X координату (int): ");
                inp = console.readLine();
                if (inp == null || inp.trim().isEmpty()) {
                    throw new IllegalArgumentException("Это поле не может быть пустым.");
                }
                coordX = Integer.parseInt(inp);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите целое число (int) " + e);
            }
        }
        argsList.add(String.valueOf(coordX));


        while (true) {
            try {
                System.out.print("Введите Y координату (double): ");
                inp = console.readLine();
                if (inp == null || inp.trim().isEmpty()) {
                    throw new IllegalArgumentException("Это поле не может быть пустым.");
                }
                coordY = Double.parseDouble(inp);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите вещественное число (double) " + e);
            }
        }
        argsList.add(String.valueOf(coordY));

        // Ввод realHero
        realHero = readBoolean(console, "Реальный герой (boolean)? (true/false): ");
        argsList.add(String.valueOf(realHero));

        // Ввод hasToothpick
        hasToothpick = readBoolean(console, "Есть зубочистка (boolean)? (true/false): ");
        argsList.add(String.valueOf(hasToothpick));

        while (true) {
            try {
                System.out.print("Введите скорость столкновения (double): ");
                inp = console.readLine();
                if (inp == null || inp.trim().isEmpty()) {
                    impactSpeed = 0;
                } else {
                    impactSpeed = Double.parseDouble(inp);
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите вещественное число (double) " + e);
            }
        }
        argsList.add(String.valueOf(impactSpeed));

        while (true) {
            try {
                System.out.print("Введите название трека: ");
                soundtrackName = console.readLine();
                if (soundtrackName == null || soundtrackName.trim().isEmpty()) {
                    throw new IllegalArgumentException("Поле не может быть пустым.");
                }
                break;
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка:" + e);
            }
        }
        argsList.add(soundtrackName);

        while (true) {
            try {
                System.out.print("Введите время ожидания (long): ");
                inp = console.readLine();
                if (inp == null || inp.trim().isEmpty()) {
                    minutesOfWaiting = 0;
                } else {
                    minutesOfWaiting = Long.parseLong(inp);
                }
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Введите целое число (long) " + e);
            }
        }
        argsList.add(String.valueOf(minutesOfWaiting));

        while (true) {
            try {
                System.out.println("Введите тип оружия: \nAXE\nPISTOL\nSHOTGUN\nMACHINE_GUN\nBAT");
                weaponType = WeaponType.valueOf(console.readLine());
                break; // Выход из цикла, если ввод корректен
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: Введите один из доступных типов оружия.");
            }
        }
        argsList.add(String.valueOf(weaponType));

        cool = readBoolean(console, "Крутая машина (boolean)? (true/false): ");
        argsList.add(String.valueOf(cool));
        return argsList.toArray(String[]::new);
    }

    public String[] registerDataCollector() {
        ArrayList<String> newArgs =  new ArrayList<>(2);
        Console CONSOLE = Client.getConsole();
        while (true) {
            try {
                System.out.print("Логин: ");
                newArgs.add(CONSOLE.readLine());
                System.out.print("Пароль: ");
                newArgs.add(new String(CONSOLE.readPassword()));
                System.out.print("Повторите пароль: ");
                newArgs.add(new String(CONSOLE.readPassword()));
                if (newArgs.get(1).equals(newArgs.get(2))) {
//                    username = newArgs.get(0);
//                    password = newArgs.get(1);
                    break;
                }
                else {
                    System.err.println("Пароли не совпадают.");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return newArgs.toArray(String[]::new);
    }

    private String[] loginDataCollector() {
        ArrayList<String> newArgs =  new ArrayList<>(2);
        Console CONSOLE = Client.getConsole();
        while (true) {
            try {
                System.out.print("Логин: ");
                newArgs.add(CONSOLE.readLine());
                System.out.print("Пароль: ");
                newArgs.add(new String(CONSOLE.readPassword()));
                username = newArgs.get(0);
                password = newArgs.get(1);
                break;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return newArgs.toArray(String[]::new);
    }

    private boolean readBoolean(Console console, String promt) {
        while (true) {
            System.out.print(promt);
            String input = console.readLine();
            if (input.equals("true") || input.equals("false")) {
                return Boolean.parseBoolean(input);
            }
            System.out.println("Ошибка: введите 'true' или 'false'");
        }
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}

