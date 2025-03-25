package app.daos;

import app.config.HibernateConfig;
import app.entities.User;
import app.entities.enums.Role;
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
    private static final SecurityDAO securityDAO = SecurityDAO.getInstance(emf);
    private User testUserAccount;

    @BeforeEach
    void setUp() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            // Clean up existing data
            em.createQuery("DELETE FROM User").executeUpdate();

            // Create test user with user role
            testUserAccount = new User("testuser", "password123");
            testUserAccount.addRole(Role.USER);
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
        assertTrue(result.getRoles().contains(Role.USER.toString()));
        assertFalse(result.getRoles().contains(Role.USER));
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

        assertTrue(exception.getMessage().contains("Error reading object from db"));
    }

    @Test
    void testCreateUser_Success() {
        // Arrange
        String username = "newuser";
        String password = "newpassword";

        // Act
        User result = securityDAO.createUser(username, password);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());

        // Verify user was persisted with the user role
        try (EntityManager em = emf.createEntityManager()) {
            User persistedUserAccount = em.find(User.class, username);
            assertNotNull(persistedUserAccount);
            assertEquals(1, persistedUserAccount.getRoles().size());
            assertTrue(persistedUserAccount.getRoles().toString().contains("USER"));
        }
    }

    @Test
    void testCreateUser_UserAlreadyExists() {
        // Arrange
        String existingUsername = "testuser";
        String password = "newpassword";

        // Act & Assert
        EntityExistsException exception = assertThrows(EntityExistsException.class,
                () -> securityDAO.createUser(existingUsername, password));

        assertTrue(exception.getMessage().contains("Error creating user"));
    }

    @Test
    void testAddRoleToUser_Success() {
        // Arrange
        String username = testUserAccount.getUsername();

        // Act
        User result = securityDAO.addRoleToUser(username, Role.ADMIN);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.USER));
        assertTrue(result.getRoles().contains(Role.ADMIN));

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
                () -> securityDAO.addRoleToUser(nonExistentUsername, Role.USER));

        assertTrue(exception.getMessage().contains("Error reading object from db"));
    }


    @Test
    void testRemoveRoleFromUser_Success() {
        // First add admin role to test user
        securityDAO.addRoleToUser("testuser", Role.ADMIN);

        // Arrange
        String username = "testuser";

        // Act
        User result = securityDAO.removeRoleFromUser(username, Role.ADMIN);

        // Assert
        assertNotNull(result);
        assertEquals(username, result.getUsername());
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.USER));
        assertFalse(result.getRoles().contains(Role.ADMIN));

        // Verify role was removed in the database
        try (EntityManager em = emf.createEntityManager()) {
            User persistedUserAccount = em.find(User.class, username);
            assertNotNull(persistedUserAccount);
            assertEquals(1, persistedUserAccount.getRoles().size());
            assertFalse(persistedUserAccount.getRoles().contains(Role.ADMIN));
        }
    }

    @Test
    void testRemoveRoleFromUser_UserNotFound() {
        // Arrange
        String nonExistentUsername = "nonexistentuser";

        // Act & Assert
        DaoException exception = assertThrows(DaoException.class,
                () -> securityDAO.removeRoleFromUser(nonExistentUsername, Role.USER));

        assertTrue(exception.getMessage().contains("Error reading object from db"));
    }
}