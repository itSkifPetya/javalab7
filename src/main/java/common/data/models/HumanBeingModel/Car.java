package common.data.models.HumanBeingModel;

import java.io.Serializable;

/**
 * Модель класса Car
 */
public class Car implements Serializable {
    private Boolean cool; //Поле не может быть null

    public Car(Boolean cool) {
        this.cool = cool;
    }

    public Boolean getCool() {
        return cool;
    }

    @Override
    public String toString() {
        return "Car{" +
                "cool=" + cool +
                '}';
    }
}