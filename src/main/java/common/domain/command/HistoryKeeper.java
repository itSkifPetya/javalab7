package common.domain.command;

import java.util.Stack;

/**
 * Класс, отвечающий за историю используемых команд.
 */
public class HistoryKeeper {
    private static HistoryKeeper instance;
    private Stack<String> history = new Stack<>();

    private HistoryKeeper() {}

    /**
     * Реализация Singleton
     * @return
     */
    public static HistoryKeeper getInstance() {
        if (instance == null) {
            instance = new HistoryKeeper();
        }
        return instance;
    }

    /**
     * Добавляет во внутреннюю коллекцию передаваемую команду
     * @param command строка, содержащая название введённой команды
     */
    public void add(String command) {
        history.push(command);
    }

    /**
     * Возвращает историю команд в виде коллекции Stack
     * @return
     */
    public Stack<String> getHistory() {
        return history;
    }
}
