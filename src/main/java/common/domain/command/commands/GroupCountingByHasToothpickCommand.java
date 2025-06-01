package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Hashtable;
import java.util.List;

public class GroupCountingByHasToothpickCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        String message = "";
        List<HumanBeing> col1, col2;
        col1 = collection.values()
                .stream()
                .filter(s -> s.getHasToothpick())
                .toList();
        col2 = collection.values()
                .stream()
                .filter(s -> !s.getHasToothpick())
                .toList();
        message += "Имеют зубочистку:\n";
        for (HumanBeing hb : col1) {
            message += hb.toPrettyString() + "\n";
        }
        message += "Не имеют зубочистку:\n";
        for (HumanBeing hb : col2) {
            message += hb.toPrettyString() + "\n";
        }
        return new Response(true, message, collection);
    }

}
