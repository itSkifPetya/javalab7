package common.data.models.HumanBeingModel;

import java.io.Serializable;

/**
 * Модель класса Coordinates
 */
public class Coordinates implements Serializable {
    private int x;
    private double y;

    public Coordinates(int x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getY() {
        return y;
    }

    public int getX() {
        return x;
    }

    public double getRadiusVector() { return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2)); }

    @Override
    public String toString() {
        return "Coordinates{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}