package common.domain.command.commands;

import common.data.models.HumanBeingModel.Car;
import common.data.models.HumanBeingModel.Coordinates;
import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.HumanBeingModel.WeaponType;
import common.data.models.Response;
import common.domain.command.Command;
import common.domain.command.DataCollector;

import java.time.LocalDate;
import java.util.Hashtable;

public class UpdateCommand extends Command implements DataCollector {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        Integer id = Integer.parseInt(args[0]);
        boolean keyExists = collection.keySet().stream().anyMatch(i -> i.equals(id));
        if (!keyExists) return new Response(false, "Такого id нет. Используйте insert (подробнее - help).", collection);

        String name = args[1];
        int coordX = Integer.parseInt(args[2]);
        double coordY = Double.parseDouble(args[3]);
        Coordinates coordinates = new Coordinates(coordX, coordY);
        Boolean realHero = Boolean.parseBoolean(args[4]);
        Boolean hasToothpick = Boolean.parseBoolean(args[5]);
        double impactSpeed = Double.parseDouble(args[6]);
        String soundtrackName = args[7];
        long minutesOfWaiting = Long.parseLong(args[8]);
        WeaponType weaponType = WeaponType.valueOf(args[9]);
        Car car = new Car(Boolean.parseBoolean(args[10]));

        Integer userId = null;
        if (args.length > 11) {
            try {
                userId = Integer.parseInt(args[11]);
            } catch (Exception ignored) {}
        }

        // Проверка прав: только владелец может обновлять объект
        HumanBeing old = collection.get(id);
        if (old != null && old.getUserId() != null && !old.getUserId().equals(userId)) {
            return new Response(false, "Вы не являетесь владельцем этого объекта. Обновление запрещено.", collection);
        }

        HumanBeing humanBeing = HumanBeing.insertHumanBeing(id, name, coordinates, LocalDate.now(), realHero, hasToothpick, impactSpeed, soundtrackName, minutesOfWaiting, weaponType, car, userId);
        collection.replace(id, humanBeing);
        return new Response(true, "Объект успешно обновлён", collection);
    }

    @Override
    public int getArgsCount() {
        return 1;
    }
}
