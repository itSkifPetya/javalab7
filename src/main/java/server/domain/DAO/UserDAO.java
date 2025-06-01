package server.domain.DAO;

public interface UserDAO {

    boolean register(String username, String password);
    boolean authenticate(String username, String password);
    Integer getUserId(String username);
    
}
