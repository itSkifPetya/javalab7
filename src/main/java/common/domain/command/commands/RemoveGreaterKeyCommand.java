package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

public class RemoveGreaterKeyCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        Integer key = Integer.parseInt(args[0]);
        System.out.println(key);

        // userId теперь передаётся последним аргументом с сервера
        Integer userId = null;
        if (args.length > 1) {
            try {
                userId = Integer.parseInt(args[1]);
            } catch (Exception ignored) {}
        }
        Integer finalUserId = userId; // если так не делать, то лямбда не сможет использовать переменную userId
        Hashtable<Integer, HumanBeing> newCol = collection
                .entrySet()
                .stream()
                // Оставляем все, которые не принадлежат пользователю, или id <= key
                .filter(entry -> !(entry.getValue().getId() > key && entry.getValue().getUserId() != null && entry.getValue().getUserId().equals(finalUserId)))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (existing, replacement) -> existing,
                        Hashtable::new
                ));
        int removed = collection.size() - newCol.size();
        collection.clear();
        collection.putAll(newCol);
        return new Response(true, "Удалено элементов: " + removed, collection);
    }

    @Override
    public int getArgsCount() {
        return 1;
    }
}
