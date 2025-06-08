package common.domain.command;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * Класс, отвечающий за историю используемых команд.
 */
public class HistoryKeeper {
    private static HistoryKeeper instance;
    // private Stack<String> history = new Stack<>();
    private Map<Integer, Stack<String>> userHistory = new HashMap<>();

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
    public void add(String command, Integer userId) {
        if (userId == null) return;
        userHistory.putIfAbsent(userId, new Stack<>());
        userHistory.get(userId).push(command);
    }

    /**
     * Возвращает историю команд в виде коллекции Stack
     * @return
     */
    public Stack<String> getHistory(Integer userId) {
        return userHistory.getOrDefault(userId, new Stack<>());
    }
}
