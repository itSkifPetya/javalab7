package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Comparator;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

public class MinBySoundtrackNameCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        Optional<Map.Entry<Integer, HumanBeing>> result = collection.entrySet().stream()
                .min(Comparator.comparingInt(entry -> entry.getValue().getSoundtrackName().length()));
        String message = result.get().getValue().toPrettyString();
        return new Response(true, message, collection);
    }
}
