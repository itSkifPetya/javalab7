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

        // userId теперь передаётся последним аргументом
        String userId = args.length > 1 ? args[args.length - 1] : null;

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
                // args для команды: все аргументы из строки + userId
                String[] commandArgs = new String[inpArray.size()];
                for (int i = 1; i < inpArray.size(); i++) {
                    commandArgs[i - 1] = inpArray.get(i);
                }
                commandArgs[inpArray.size() - 1] = userId;
                if (command.getArgsCount() != inpArray.size() - 1) {
                    message.append("Команда %s не имеет аргументов или их количество некорректно\n".formatted(inpArray.getFirst()));
                    continue;
                }
                System.out.println(inpArray);

                response = command.execute(collection, commandArgs);
                message.append("\n\t").append(inp).append(":\n\n");
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
