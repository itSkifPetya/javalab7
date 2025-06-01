package server.data;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserRepositoryTest {
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        userRepository = new UserRepository();
    }

    @Test
    public void testRegisterAndAuthenticate() {
        String username = "testuser" + System.currentTimeMillis();
        String password = "testpass";
        // Регистрация
        boolean registered = userRepository.register(username, password);
        assertTrue(registered, "Пользователь должен быть зарегистрирован");
        // Аутентификация
        boolean authenticated = userRepository.authenticate(username, password);
        assertTrue(authenticated, "Пользователь должен пройти аутентификацию");
        // Получение userId
        Integer userId = userRepository.getUserId(username);
        assertNotNull(userId, "userId должен быть получен");
    }

    @Test
    public void testRegisterDuplicate() {
        String username = "duplicateuser" + System.currentTimeMillis();
        String password = "pass";
        assertTrue(userRepository.register(username, password));
        // Повторная регистрация должна вернуть false
        assertFalse(userRepository.register(username, password));
    }

    @Test
    public void testAuthenticateWrongPassword() {
        String username = "wrongpassuser" + System.currentTimeMillis();
        String password = "rightpass";
        userRepository.register(username, password);
        assertFalse(userRepository.authenticate(username, "wrongpass"));
    }
}

