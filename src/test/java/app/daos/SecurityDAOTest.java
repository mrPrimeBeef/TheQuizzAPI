package app.daos;

import app.config.HibernateConfig;
import app.entities.User;
import app.entities.Role;
import app.exceptions.DaoException;
import app.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityDAOTest {
    private static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    private static final RoleDao roleDao = RoleDao.getInstance(emf);
    private static final SecurityDAO securityDAO = SecurityDAO.getInstance(emf, roleDao);
    private User testUserAccount;

    Role adminRole = new Role("ADMIN");
    Role userRole = new Role("USER");

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            em.createQuery("DELETE FROM User").executeUpdate();
            em.createQuery("DELETE FROM Role").executeUpdate();

            userRole = new Role("USER");
            adminRole = new Role("ADMIN");
            em.persist(userRole);
            em.persist(adminRole);

            // Create test user with user role
            testUserAccount = new User("testuser", "password123");
            testUserAccount.addRole(userRole);
            em.persist(testUserAccount);

            em.getTransaction().commit();
        }
    }

    @Test
    void testGetVerifiedUser_Success() throws ValidationException {
        // Arrange
        String username = "testuser";
        String password = "password123";

        // Act
        UserDTO result = securityDAO.getVerifiedUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertTrue(result.getRoles().contains("USER"));
        assertEquals(1, result.getRoles().size());
    }

    @Test
    void testGetVerifiedUser_WrongPassword() {
        // Arrange
        String username = "testuser";
        String wrongPassword = "wrongpassword";

        // Act & Assert
        ValidationException exception = assertThrows(ValidationException.class,
                () -> securityDAO.getVerifiedUser(username, wrongPassword));

        assertEquals("Password does not match", exception.getMessage());
    }

    @Test
    void testGetVerifiedUser_UserNotFound() {
        // Arrange
        String nonExistentUsername = "nonexistentuser";
        String password = "password123";

        // Act & Assert
        DaoException exception = assertThrows(DaoException.class,
                () -> securityDAO.getVerifiedUser(nonExistentUsername, password));

//        assertTrue(exception.getMessage().contains("Error in finding class app.entities.User with id: nonexistentuser"));
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        String username = "newuser";
        String password = "newpassword";
        User user = new User(username, password);
        user.addRole(userRole);

        // Act
        User result = securityDAO.createUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());

        // Verify user was persisted with the user role
        try (EntityManager em = emf.createEntityManager()) {
            User persistedUserAccount = em.find(User.class, username);
            assertNotNull(persistedUserAccount);
            assertEquals(1, persistedUserAccount.getRoles().size());
            assertTrue(persistedUserAccount.getRoles().stream().anyMatch(role -> role.getName().equals("USER")));
        }
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        // Arrange
        String existingUsername = "testuser";
        String password = "newpassword";
        User existingUser = new User(existingUsername, password);

        // Act & Assert
        EntityExistsException exception = assertThrows(EntityExistsException.class,
                () -> securityDAO.createUser(existingUser));
    }

    @Test
    void testAddRoleToUser_Success() {
        // Arrange
        String username = testUserAccount.getUsername();

        // Act
        User result = securityDAO.addRoleToUser(username, adminRole);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().stream().anyMatch(role -> role.getName().equals("USER")));
        assertTrue(result.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")));

        // Verify role was added in the database
        try (EntityManager em = emf.createEntityManager()) {
            User persistedUserAccount = em.find(User.class, username);
            assertNotNull(persistedUserAccount);
            assertEquals(2, persistedUserAccount.getRoles().size());
            assertTrue(persistedUserAccount.getRoles().toString().contains("ADMIN"));
        }
    }

    @Test
    void testAddRoleToUser_UserNotFound() {
        // Arrange
        String nonExistentUsername = "nonexistentuser";

        // Act & Assert
        DaoException exception = assertThrows(DaoException.class,
                () -> securityDAO.addRoleToUser(nonExistentUsername, userRole));

        // assertTrue(exception.getMessage().contains("Error in finding class app.entities.User with id: nonexistentuser"));
    }


    @Test
    void testRemoveRoleFromUser_Success() {
        // First add admin role to test user
        securityDAO.addRoleToUser("testuser", adminRole);

        // Arrange
        String username = "testuser";

        // Act
        User result = securityDAO.removeRoleFromUser(username, adminRole);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().stream().anyMatch(role -> role.getName().equals("USER")));
        assertFalse(result.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")));

        // Verify role was removed in the database
        try (EntityManager em = emf.createEntityManager()) {
            User persistedUserAccount = em.find(User.class, username);
            assertNotNull(persistedUserAccount);
            assertEquals(1, persistedUserAccount.getRoles().size());
            assertFalse(persistedUserAccount.getRoles().stream().anyMatch(role -> role.getName().equals("ADMIN")));
        }
    }

    @Test
    void testRemoveRoleFromUser_UserNotFound() {
        // Arrange
        String nonExistentUsername = "nonexistentuser";

        // Act & Assert
        DaoException exception = assertThrows(DaoException.class,
                () -> securityDAO.removeRoleFromUser(nonExistentUsername, userRole));

//        assertTrue(exception.getMessage().contains("Error in finding class app.entities.User with id: nonexistentuser"));
    }
}