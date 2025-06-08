package common.data.models;

import common.domain.command.Command;

import java.io.Serializable;

public class Request implements Serializable {
    private final Command command;
    private final String[] args;
    private final String username;
    private final String password;

    public Request(Command command, String[] args, String username, String password) {
        this.command = command;
        this.args = args;
        this.username = username;
        this.password = password;
    }

    public Command getCommand() {
        return command;
    }

    public String[] getArgs() {
        return args;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}

