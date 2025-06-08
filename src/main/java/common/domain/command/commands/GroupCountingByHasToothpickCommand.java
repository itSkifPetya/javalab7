package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Hashtable;
import java.util.List;

public class GroupCountingByHasToothpickCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        StringBuilder message = new StringBuilder();
        List<HumanBeing> col1, col2;
        col1 = collection.values()
                .stream()
                .filter(s -> s.getHasToothpick())
                .toList();
        col2 = collection.values()
                .stream()
                .filter(s -> !s.getHasToothpick())
                .toList();
        message.append("\u001B[32mИмеют зубочистку:\u001B[0m\n");
        for (HumanBeing hb : col1) {
            message.append(hb.toPrettyString()).append("\n");
        }
        message.append("\u001B[31mНе имеют зубочистку:\u001B[0m\n");
        for (HumanBeing hb : col2) {
            message.append(hb.toPrettyString()).append("\n");
        }
        return new Response(true, message.toString(), collection);
    }

}
