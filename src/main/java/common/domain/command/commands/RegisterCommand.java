package common.domain.command.commands;

import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.Response;
import common.domain.command.Command;
import server.data.UserRemoteRepository;

import java.util.Hashtable;

public class RegisterCommand extends Command {
    @Override
    public Response execute(Hashtable<Integer, HumanBeing> collection, String[] args) {
        if (args.length < 2) {
            return new Response(false, "Недостаточно данных для регистрации", new Hashtable<>());
        }
        String username = args[0];
        String password = args[1];
        UserRemoteRepository userRepo = new UserRemoteRepository();
        boolean registered = userRepo.register(username, password);
        if (registered) {
            return new Response(true, "Регистрация успешна! Теперь войдите (команда login)", new Hashtable<>());
        } else {
            return new Response(false, "Пользователь с таким именем уже существует", new Hashtable<>());
        }
    }

//    @Override
//    public int getArgsCount() {
//        return ;
//    }
}

