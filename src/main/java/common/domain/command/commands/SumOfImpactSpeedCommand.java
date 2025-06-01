package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Hashtable;

public class SumOfImpactSpeedCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        Double sum = collection
                .values()
                .stream()
                .mapToDouble(s -> s.getImpactSpeed())
                .sum();
        String message = "Сумма скоростей в момент аварии: " + sum;
        return new Response(true, message, collection);
    }
}
