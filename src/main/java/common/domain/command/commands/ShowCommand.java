package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Hashtable;

public class ShowCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        String message = "";
//        for (HumanBeing hb : collection.values()) {
//            message += hb.toPrettyString() + "\n";
//        }
        return new Response(true, message, collection);
    }
}
