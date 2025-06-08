package client.domain;

import common.data.models.HumanBeingModel.WeaponType;
import common.data.models.Request;
import common.domain.command.Command;
import common.domain.command.DataCollector;
import common.domain.command.Invoker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class CommandHandler {
    private static CommandHandler instance;

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
        if (command instanceof DataCollector) {
            newArgs = dataCollectorArgsBuilder((DataCollector) command, args);
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
            System.out.println("Такой команды нет. Для подробной информации используйте help");
        }
//        System.out.println(commandName);
        if (command != null && args.length != command.getArgsCount()) {
            System.out.println("Некорректное количество аргументов");
            return null;
        }

        return command;
    }

    private String[] dataCollectorArgsBuilder(DataCollector command, String[] args) {
        String key = args[0];
        ArrayList<String> argsList = new ArrayList<>(List.of(key));
        Scanner sc = Client.getSCANNER();
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
                name = sc.nextLine();
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
                inp = sc.nextLine();
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
                inp = sc.nextLine();
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
        realHero = readBoolean(sc, "Реальный герой (boolean)? (true/false): ");
        argsList.add(String.valueOf(realHero));

        // Ввод hasToothpick
        hasToothpick = readBoolean(sc, "Есть зубочистка (boolean)? (true/false): ");
        argsList.add(String.valueOf(hasToothpick));

        while (true) {
            try {
                System.out.print("Введите скорость столкновения (double): ");
                inp = sc.nextLine();
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
                soundtrackName = sc.nextLine();
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
                inp = sc.nextLine();
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
                weaponType = WeaponType.valueOf(sc.nextLine());
                break; // Выход из цикла, если ввод корректен
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: Введите один из доступных типов оружия.");
            }
        }
        argsList.add(String.valueOf(weaponType));

        cool = readBoolean(sc, "Крутая машина (boolean)? (true/false): ");
        argsList.add(String.valueOf(cool));
        return argsList.toArray(String[]::new);
    }

    private boolean readBoolean(Scanner sc, String promt) {
        while (true) {
            System.out.print(promt);
            String input = sc.nextLine();
            if (input.equals("true") || input.equals("false")) {
                return Boolean.parseBoolean(input);
            }
            System.out.println("Ошибка: введите 'true' или 'false'");
        }
    }


}

