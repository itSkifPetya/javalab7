package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;

import java.util.Hashtable;

public class HelpCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        String help = """
                help: вывести справку по доступным командам
                info: информация о коллекци
                show: вывести коллекцию
                insert {key}: добавить элемент по ключу
                update {id}: обновить элемент по его id
                remove_key {key}: удалить элемент из коллекции по его ключу
                clear: очистить коллекцию
                execute_script {file_path}: выполнить скрипт
                exit: завершить работу клиента
                history: вывести последние 5 команд
                replace_if_greater {key}: заменить элемент по ключу, если новый больше старого
                remove_greater_key {key}: удалить все элементы, ключ которых превышает данный
                sum_of_impact_speed: вывести сумму значений поля impactSpeed для всех элементов коллекции
                min_by_soundtrack_name: вывести любой элемент коллекции с минимальным полем soundtrackName
                group_counting_by_has_toothpick: сгруппировать элементы коллекции по значению поля hasToothpick, вывести количество элементов в каждой группе
                """;
        return new Response(true, help, collection);
    }
}
