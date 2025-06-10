package common.domain.command;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.Console;
import java.util.Scanner;

public class SSHTunnel {
    private static final String SSH_HOST = "helios.cs.ifmo.ru";
    private static final int PORT = 2222;
    private static final String LOGIN = "s465877";
    private static final String PASSWORD = "zAwm#7410";
    private Console console;
    private Session session;
    private int localPort;

    public SSHTunnel(Console console) {
        this.console = console;
    }

    public void baseTunnel() throws JSchException {
        int REMOTE_PORT;
        while (true) {
            try {
                System.out.print("Введите порт для ssh-туннеля: ");
                String temp = console.readLine();
                REMOTE_PORT = Integer.parseInt(temp);
                break;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка ввода. Попробуйте ещё раз");
            }
        }

        JSch jSch = new JSch();
        session = jSch.getSession(LOGIN, SSH_HOST, PORT);
        session.setPassword(PASSWORD);
        // отключение проверки подлинности ключа хоста
        session.setConfig("StrictHostKeyChecking", "no");

        session.connect();
        // получаем локальный порт для подключения через туннель
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

    /**
     * Перенаправление базы данных на гелиосе на localhost при помощи проброса портов
     * @throws Exception
     */
    public void psqlTunnel() throws Exception {
        int SSH_PORT = 2222;
        String REMOTE_HOST = "pg";
        int REMOTE_PORT = 5432;
        int LOCAL_PORT = 5432;

        JSch jSch = new JSch();
        session = jSch.getSession(LOGIN, SSH_HOST, SSH_PORT);
        session.setPassword(PASSWORD);

        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        session.setPortForwardingL(LOCAL_PORT, REMOTE_HOST, REMOTE_PORT);
        System.out.println("SSH-туннель создан: localhost:" + LOCAL_PORT + " -> " + REMOTE_HOST + ":" + REMOTE_PORT + " через " + SSH_HOST);
    }
}
