package common.data.models.HumanBeingModel;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Модель класса HumanBeing. С этим типом данных осуществляется работа в коллекции
 */
public class HumanBeing implements Comparable<HumanBeing>, Serializable {
    private Integer id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private LocalDate creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Boolean realHero; //Поле не может быть null
    private Boolean hasToothpick; //Поле не может быть null
    private double impactSpeed;
    private String soundtrackName; //Поле не может быть null
    private long minutesOfWaiting;
    private WeaponType weaponType; //Поле не может быть null
    private Car car; //Поле может быть null
    private Integer userId; // ID пользователя-владельца объекта
    public static final HumanBeing zeroHumanBeing = new HumanBeing("Zero", new Coordinates(1, 1.1), false, false, 1.0, "Zero", 1, WeaponType.PISTOL, new Car(false), null);

    private HumanBeing(Integer id, String name, Coordinates coordinates, LocalDate creationDate, Boolean realHero, Boolean hasToothpick, double impactSpeed, String soundtrackName, long minutesOfWaiting, WeaponType weaponType, Car car, Integer userId) throws NullPointerException {
        if (name == null) throw new NullPointerException("Поле name не может быть пустым!");
        if (coordinates == null) throw new NullPointerException("Поле coordinates не может быть пустым!");
        if (realHero == null) throw new NullPointerException("Поле realHero не может быть пустым!");
        if (hasToothpick == null) throw new NullPointerException("Поле hasToothpick не может быть пустым!");
        if (soundtrackName == null) throw new NullPointerException("Поле soundtrackName не может быть пустым!");
        if (weaponType == null) throw new NullPointerException("Поле weaponType не может быть пустым!");
        if (car == null) throw new NullPointerException("Поле car не может быть пустым!");
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.realHero = realHero;
        this.hasToothpick = hasToothpick;
        this.impactSpeed = impactSpeed;
        this.soundtrackName = soundtrackName;
        this.minutesOfWaiting = minutesOfWaiting;
        this.weaponType = weaponType;
        this.car = car;
        this.userId = userId;
    }


    public HumanBeing(String name, Coordinates coordinates, Boolean realHero, Boolean hasToothpick, double impactSpeed, String soundtrackName, long minutesOfWaiting, WeaponType weaponType, Car car, Integer userId) throws NullPointerException {
//        UUID uuid = UUID.randomUUID();
//        this.id = ByteBuffer.wrap(uuid.toString().getBytes()).getInt();
        UUID uuid = UUID.randomUUID();
        this.id = Integer.parseInt(uuid.toString().substring(0, 3).replace("-", ""), 16);
        if (name == null) throw new NullPointerException("Поле name не может быть пустым!");
        if (coordinates == null) throw new NullPointerException("Поле coordinates не может быть пустым!");
        if (realHero == null) throw new NullPointerException("Поле realHero не может быть пустым!");
        if (hasToothpick == null) throw new NullPointerException("Поле hasToothpick не может быть пустым!");
        if (soundtrackName == null) throw new NullPointerException("Поле soundtrackName не может быть пустым!");
        if (weaponType == null) throw new NullPointerException("Поле weaponType не может быть пустым!");
        if (car == null) throw new NullPointerException("Поле car не может быть пустым!");
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = LocalDate.now();
        this.realHero = realHero;
        this.hasToothpick = hasToothpick;
        this.impactSpeed = impactSpeed;
        this.soundtrackName = soundtrackName;
        this.minutesOfWaiting = minutesOfWaiting;
        this.weaponType = weaponType;
        this.car = car;
        this.userId = userId;
    }

//    public static HumanBeing parseHumanBeing(Integer id, String name, Coordinates coordinates, LocalDate creationDate, Boolean realHero, Boolean hasToothpick, double impactSpeed, String soundtrackName, long minutesOfWaiting, WeaponType weaponType, Car car) {
//        return new HumanBeing(id, name, coordinates, creationDate, realHero, hasToothpick, impactSpeed, soundtrackName, minutesOfWaiting, weaponType, car);
//    }

    /**
     * Возвращает новый HumanBeing. Используется при замещении в коллекции по ID
     * @param id
     * @param name
     * @param coordinates
     * @param realHero
     * @param hasToothpick
     * @param impactSpeed
     * @param soundtrackName
     * @param minutesOfWaiting
     * @param weaponType
     * @param car
     * @param userId
     * @return
     */
    public static HumanBeing insertHumanBeing(Integer id, String name, Coordinates coordinates, LocalDate creationDate, Boolean realHero, Boolean hasToothpick, double impactSpeed, String soundtrackName, long minutesOfWaiting, WeaponType weaponType, Car car, Integer userId) {
        return new HumanBeing(id, name, coordinates, creationDate, realHero, hasToothpick, impactSpeed, soundtrackName, minutesOfWaiting, weaponType, car, userId);
    }

    @Override
    public int compareTo(HumanBeing other) {
        if (this.coordinates.getRadiusVector() > other.coordinates.getRadiusVector()) {
            return 1;
        } else {
            return -1;
        }
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public String toPrettyString() {
        return String.format(
                "─── ID: %d ───%n" +
                        "├─ Пострадавший: %s%n" +
                        "├─ Дата регистрации: %s | Координаты: (%d; %.2f)%n" +
                        "├─ Удар: %.1f км/ч | Время ожидания: %d мин%n" +
                        "├─ Герой: %s | Оружие: %s%n" +
                        "├─ Зубочистка: %s%n" +
                        "├─ Авто: %s%n" +
                        "└─ Трек: %s%n",
                id,
                name,
                creationDate, coordinates.getX(), coordinates.getY(),
                impactSpeed, minutesOfWaiting,
                realHero ? "✓" : "✗", weaponType,
                hasToothpick ? "✓" : "✗",
                car != null ? (car.getCool() ? "Крутое" : "Обычное") : "Нет",
                soundtrackName
        );
    }

    @Override
    public String toString() {
        return "HumanBeing{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", coordinates=" + coordinates +
                ", creationDate=" + creationDate +
                ", realHero=" + realHero +
                ", hasToothpick=" + hasToothpick +
                ", impactSpeed=" + impactSpeed +
                ", soundtrackName='" + soundtrackName + '\'' +
                ", minutesOfWaiting=" + minutesOfWaiting +
                ", weaponType=" + weaponType +
                ", car=" + car +
                ", userId=" + userId +
                '}';
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public Boolean getRealHero() {
        return realHero;
    }

    public Boolean getHasToothpick() {
        return hasToothpick;
    }

    public double getImpactSpeed() {
        return impactSpeed;
    }

    public String getSoundtrackName() {
        return soundtrackName;
    }

    public long getMinutesOfWaiting() {
        return minutesOfWaiting;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public Car getCar() {
        return car;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }
}

