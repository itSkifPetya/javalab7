package server;

import server.domain.Server;

public class Main {
    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.start();
    }
}
/*while (true) {
            try {
                System.out.println("Выберите режим работы:\n1) Сервер запущен локально\n2) Сервер запущен на гелиосе");
                System.out.print("Номер: ");
                opt = Integer.parseInt(SCANNER.nextLine());
                switch (opt) {
                    case 1 -> {
                        System.out.println("Сервер запущен локально + SSH Tunnel");
                        SSHTunnel tunnel = new SSHTunnel();
                        try {
                            tunnel.start();
                        } catch (JSchException ex) {
                            throw new RuntimeException(ex);
                        }
                        remoteRepository = new RemoteRepository();
                        break;
                    }
                    case 2 -> {
                        System.out.println("Сервер запущен на гелиосе");
                        remoteRepository = new RemoteRepository("jdb:postgresql://pg:5432/studs", "s465877", "D7cCg1cMguDJeuwv");
                        break;
                    }
                    default -> {
                        System.out.println("Некорректный ввод. Попробуйте ещё раз");
                        continue;
                    }
                }
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод. Попробуйте ещё раз");
            }
        }*/