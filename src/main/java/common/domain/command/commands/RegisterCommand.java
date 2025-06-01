package common.domain.command.commands;

import common.data.models.Response;
import common.domain.command.Command;
import server.data.UserRepository;

import java.util.Hashtable;

public class RegisterCommand extends Command {
    @Override
    public Response execute(Hashtable collection, String[] args) {
        // args не используются, логин и пароль берутся из Request
        // Получить username и password из Request невозможно напрямую, поэтому используем хак:
        // username и password должны быть переданы в args (или через Request, если расширить интерфейс)
        // Здесь args[0] = username, args[1] = password
        if (args.length < 2) {
            return new Response(false, "Недостаточно данных для регистрации", null);
        }
        String username = args[0];
        String password = args[1];
        UserRepository userRepo = new UserRepository();
        boolean registered = userRepo.register(username, password);
        if (registered) {
            return new Response(true, "Регистрация успешна!", null);
        } else {
            return new Response(false, "Пользователь с таким именем уже существует", null);
        }
    }

    @Override
    public int getArgsCount() {
        return 2;
    }
}

