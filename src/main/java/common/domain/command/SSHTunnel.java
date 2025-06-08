package common.domain.command;

import client.domain.Client;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.util.Scanner;

public class SSHTunnel {
    private static final String SSH_HOST = "helios.cs.ifmo.ru";
    private static final int PORT = 2222;
    private static final String LOGIN = "s465877";
    private static final String PASSWORD = "zAwm#7410";
//    private static final int REMOTE_PORT = 1234;

    private Session session;
    private int localPort;

    public void start() throws JSchException {
        Scanner sc = Client.getSCANNER();
        int REMOTE_PORT;
        while (true) {
            try {
                System.out.print("Введите порт: ");
                REMOTE_PORT = Integer.parseInt(sc.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка ввода. Попробуйте ещё раз");
            }
        }

        JSch jSch = new JSch();
        session = jSch.getSession(LOGIN, SSH_HOST, PORT);
        session.setPassword(PASSWORD);

        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();

        localPort = session.setPortForwardingL(0, "localhost", REMOTE_PORT);

        System.out.println("SSH-туннель создан: localhost:" + localPort + " -> " + SSH_HOST + ":" + REMOTE_PORT);
    }

    public int getLocalPort() {
        return localPort;
    }

    public void stop() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    public void psqlSSHTunnel() throws Exception {
        String SSH_USER = "s465877";
        String SSH_HOST = "helios.cs.ifmo.ru";
        String sshPassword = "zAwm#7410";
        int SSH_PORT = 2222;
        String REMOTE_HOST = "pg";
        int REMOTE_PORT = 5432;
        int LOCAL_PORT = 5432;

        JSch jSch = new JSch();
        session = jSch.getSession(SSH_USER, SSH_HOST, SSH_PORT);
        session.setPassword(sshPassword);

        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        session.setPortForwardingL(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);
        System.out.println("SSH-туннель создан: localhost:" + LOCAL_PORT + " -> " + REMOTE_HOST + ":" + REMOTE_PORT + " через " + SSH_HOST);
    }
}
