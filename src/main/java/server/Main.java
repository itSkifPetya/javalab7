package server;

import server.domain.Server;

public class Main {
    public static void main(String[] args) {
        Server server = Server.getInstance();
        server.start();
    }
}
