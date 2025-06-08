package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;
import common.domain.command.HistoryKeeper;

import java.util.Hashtable;
import java.util.Stack;

public class HistoryCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        StringBuilder message = new StringBuilder();
        HistoryKeeper historyKeeper = HistoryKeeper.getInstance();
        // userId теперь передаётся последним аргументом
        Integer userId = null;
        if (args.length > 0) {
            try {
                userId = Integer.parseInt(args[args.length - 1]);
            } catch (Exception ignored) {}
        }
        Stack<String> history = historyKeeper.getHistory(userId);
        if (history.isEmpty()) {
            return new Response(true, "История команд пуста.", collection);
        }
        int hisLen = Math.min(history.size(), 5);
        for (int i = 0; i < hisLen; i++) {
            message.append("%s\n".formatted(history.get(history.size() - 1 - i)));
        }
        return new Response(true, message.toString(), collection);
    }
}
