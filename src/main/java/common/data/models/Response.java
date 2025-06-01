package common.data.models;

import common.data.models.HumanBeingModel.HumanBeing;

import java.io.Serializable;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;

public class Response implements Serializable {
    private final boolean success;
    private final String message;
    private final Hashtable<Integer, HumanBeing> data;

    public Response(boolean success, String message, Hashtable<Integer, HumanBeing> data) {
        this.success = success;
        this.message = message;
        this.data = data.entrySet().stream().sorted(Map.Entry.comparingByValue()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, Hashtable::new));
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Hashtable<Integer, HumanBeing> getData() {
        return data;
    }

    @Override
    public String toString() {
        return (success ? "[OK]" : "[ERROR]") + message;
    }
}
