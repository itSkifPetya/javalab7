package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Hashtable;

public class ClearCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        Integer userId = null;
        if (args.length > 0) {
            try {
                userId = Integer.parseInt(args[args.length - 1]);
            } catch (Exception ignored) {}
        }
        Integer finalUserId = userId; // если так не делать, то лямбда не сможет использовать переменную userId
        // Оставляем только чужие объекты
        collection.entrySet().removeIf(entry -> entry.getValue().getUserId() != null && entry.getValue().getUserId().equals(finalUserId));
        return new Response(true, "Удалены все ваши объекты!", collection);
    }
}
