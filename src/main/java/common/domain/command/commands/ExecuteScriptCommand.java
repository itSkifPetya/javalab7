package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;
import common.domain.command.Invoker;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

public class ExecuteScriptCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        String path = args[0];
        Invoker invoker = Invoker.getInstance();
        Map<String, Command> commandMap = invoker.getCommandMap();
        StringBuilder message = new StringBuilder();
        Response response = null;
//        String[] tempArgs = new String[]{};

        try (Scanner sc = new Scanner(new FileReader(path))) {
            String inp;
            while (sc.hasNextLine()) {
                inp = sc.nextLine();
                ArrayList<String> inpArray = new ArrayList<>(List.of(inp.split(" ")));
                Command command = commandMap.get(inpArray.getFirst());

                if (command instanceof ExecuteScriptCommand) {
                    message.append("\nВы не можете запустить скрипт внутри скрипта. Выполнение остановлено.\n");
                    break;
                }

                if (command == null) {
                    message.append("Неизвестная команда: %s\n".formatted(inpArray.getFirst()));
                    continue;
                }
                if (command.getArgsCount() != inpArray.size()-1) {
                    message.append("Команда %s не имеет аргументов или их количество некорректно\n".formatted(inpArray.getFirst()));
                    continue;
                }
                System.out.println(inpArray);

                response = command.execute(collection, new String[]{inpArray.getLast()});
                message.append("\n\t" + inp + ":\n\n");
                if (command instanceof ShowCommand) {
                    for (HumanBeing hb : collection.values()) {
                        message.append(hb.toPrettyString());
                    }
                }
                message.append(response.getMessage());

//                Thread.sleep(100);
            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
        }
        if (response == null) response = new Response(false, "Команды не были выполнены", collection);

        return new Response(true, message.toString(), response.getData());
    }

    @Override
    public int getArgsCount() {
        return 1;
    }
}
