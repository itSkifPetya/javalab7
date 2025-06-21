package server.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Hashtable;

import common.data.models.HumanBeingModel.Car;
import common.data.models.HumanBeingModel.Coordinates;
import common.data.models.HumanBeingModel.HumanBeing;
import common.data.models.HumanBeingModel.WeaponType;
import server.domain.DAO.HumanBeingDAO;

public class HumanBeingRemoteRepository implements HumanBeingDAO {
    private static String URL = "jdbc:postgresql://localhost:5432/studs";
    private static String USER = "s465877";
    private static String PASSWORD = "D7cCg1cMguDJeuwv";

    public HumanBeingRemoteRepository() {}

    public HumanBeingRemoteRepository(String url, String user, String password) throws SQLException {
        URL = url;
        USER = user;
        PASSWORD = password;
    }

    @Override
    public void writeData(Hashtable<Integer, HumanBeing> collection) {
        String insertSql = "INSERT INTO human_beings (id, name, coord_x, coord_y, creation_date, real_hero, has_toothpick, impact_speed, soundtrack_name, minutes_of_waiting, weapon_type, car_cool, user_id)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)" + " ON CONFLICT (id) DO NOTHING";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);) {
            // ручное управление транзакциями
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (HumanBeing hb : collection.values()) {
                    ps.setInt(1, hb.getId());
                    ps.setString(2, hb.getName());
                    ps.setInt(3, hb.getCoordinates().getX());
                    ps.setDouble(4, hb.getCoordinates().getY());
                    ps.setDate(5, java.sql.Date.valueOf(hb.getCreationDate()));
                    ps.setBoolean(6, hb.getRealHero());
                    ps.setBoolean(7, hb.getHasToothpick());
                    ps.setDouble(8, hb.getImpactSpeed());
                    ps.setString(9, hb.getSoundtrackName());
                    ps.setLong(10, hb.getMinutesOfWaiting());
                    ps.setString(11, hb.getWeaponType().name());
                    ps.setBoolean(12, hb.getCar().getCool());
                    ps.setInt(13, hb.getUserId());

                    // добавление данных выше в пакет запросов
                    ps.addBatch();
                }
                // отправка пакета запросов
                ps.executeBatch();

                // сохранение результата
                conn.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    @Override
    public Hashtable<Integer, HumanBeing> readData() {
        Hashtable<Integer, HumanBeing> collection = new Hashtable<>();
        String sql = "SELECT * FROM human_beings";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int coordX = rs.getInt("coord_x");
                double coordY = rs.getDouble("coord_y");
                LocalDate creationDate = rs.getDate("creation_date").toLocalDate();
                boolean realHero = rs.getBoolean("real_hero");
                boolean hasToothpick = rs.getBoolean("has_toothpick");
                double impactSpeed = rs.getDouble("impact_speed");
                String soundtrackName = rs.getString("soundtrack_name");
                long minutesOfWaiting = rs.getLong("minutes_of_waiting");
                String weaponType = rs.getString("weapon_type");
                boolean carCool = rs.getBoolean("car_cool");
                int userId = rs.getInt("user_id");

                HumanBeing hb = HumanBeing.insertHumanBeing(id, name, new Coordinates(coordX, coordY), creationDate, realHero, hasToothpick, impactSpeed, soundtrackName, minutesOfWaiting, WeaponType.valueOf(weaponType), new Car(carCool), userId);
                collection.put(id, hb);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return collection;
    }

}
