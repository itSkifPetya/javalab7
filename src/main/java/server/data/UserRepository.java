package server.data;

import server.domain.DAO.UserDAO;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.*;

public class UserRepository implements UserDAO {
    private static final String URL = "jdbc:postgresql://localhost:5432/studs";
    private static final String USER = "s465877";
    private static final String PASSWORD = "D7cCg1cMguDJeuwv";

    private String sha1(String input) {
        try {
            // создаём дайджест с алгоритмом SHA-1
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] bytes = md.digest(input.getBytes());
            // перевод дайджеста в строковое представление в шестнадцатеричной сс
            String sb = new BigInteger(1, bytes).toString(16);
            return sb;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean register(String username, String password) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, sha1(password));
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean authenticate(String username, String password) {
        String sql = "SELECT password_hash FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                // сверка хэшей пароля из бд и с клиента
                String hash = rs.getString("password_hash");
                return hash.equals(sha1(password));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Integer getUserId(String username) {
        String sql = "SELECT id FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
