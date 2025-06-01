package server.data;

import common.data.models.HumanBeingModel.*;
import org.junit.jupiter.api.*;
import java.time.LocalDate;
import java.util.Hashtable;
import static org.junit.jupiter.api.Assertions.*;

public class RemoteRepositoryTest {
    private RemoteRepository repo;

    @BeforeEach
    public void setUp() {
        repo = new RemoteRepository();
    }

    @Test
    public void testWriteAndReadData() {
        UserRepository userRepository = new UserRepository();
        String username1 = "testuser1_" + System.currentTimeMillis();
        String username2 = "testuser2_" + System.currentTimeMillis();
        String password = "testpass";
        userRepository.register(username1, password);
        userRepository.register(username2, password);
        Integer userId1 = userRepository.getUserId(username1);
        Integer userId2 = userRepository.getUserId(username2);

        Hashtable<Integer, HumanBeing> collection = new Hashtable<>();
        HumanBeing hb1 = HumanBeing.insertHumanBeing(
                1001, "Test1", new Coordinates(1, 2.0), LocalDate.now(), true, false, 10.5, "track1", 5L, WeaponType.AXE, new Car(true), userId1);
        HumanBeing hb2 = HumanBeing.insertHumanBeing(
                1002, "Test2", new Coordinates(3, 4.0), LocalDate.now(), false, true, 20.5, "track2", 10L, WeaponType.BAT, new Car(false), userId2);
        collection.put(hb1.getId(), hb1);
        collection.put(hb2.getId(), hb2);
        // Запись
        repo.writeData(collection);
        // Чтение
        Hashtable<Integer, HumanBeing> loaded = repo.readData();
        assertEquals(collection.size(), loaded.size(), "Размер коллекции должен совпадать");
        assertTrue(loaded.containsKey(1001));
        assertTrue(loaded.containsKey(1002));
        assertEquals("Test1", loaded.get(1001).getName());
        assertEquals("Test2", loaded.get(1002).getName());
    }
}

