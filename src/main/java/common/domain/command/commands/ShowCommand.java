package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Hashtable;

public class ShowCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        StringBuilder sb = new StringBuilder();
        // userId теперь передаётся последним аргументом
        Integer userId = null;
        if (args.length > 0) {
            try {
                userId = Integer.parseInt(args[args.length - 1]);
            } catch (Exception ignored) {}
        }
        for (HumanBeing hb : collection.values()) {
            boolean isMine = hb.getUserId().equals(userId);
            if (isMine) {
                sb.append("\u001B[32m"); // зелёный
            } else {
                sb.append("\u001B[31m"); // красный
            }
            sb.append(hb.toPrettyString()).append("\n");
            sb.append("\u001B[0m"); // сброс цвета

        }
        return new Response(true, sb.toString(), collection);
    }
}
