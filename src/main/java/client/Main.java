package client;

import client.domain.Client;

public class Main {
    public static void main(String[] args) {
        Client client = Client.getInstance();
        client.start();
    }
}
