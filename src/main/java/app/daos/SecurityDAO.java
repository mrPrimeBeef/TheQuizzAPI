package app.daos;

import app.entities.Role;
import app.entities.User;
import app.exceptions.DaoException;
import app.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SecurityDAO extends AbstractDao<User, Integer> implements ISecurityDAO {
    private static SecurityDAO instance;
    private static RoleDao roleDao = RoleDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(SecurityDAO.class);

    private SecurityDAO(EntityManagerFactory emf) {
        super(User.class, emf);
    }

    public static SecurityDAO getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new SecurityDAO(emf);
        }
        return instance;
    }

    public void createRolesInDataBase() {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();
            Role userRole = new Role("USER");
            Role adminRole = new Role("ADMIN");

            em.persist(userRole);
            em.persist(adminRole);

            em.getTransaction().commit();
        } catch (Exception e) {
            throw new DaoException("Error in createWithRole()");
        }
    }

    public User createWithRole(User user) throws ValidationException {
        List<Role> rolesInDatabase = roleDao.findAll();

        boolean userGotRole = user.getRoles().stream()
                .anyMatch(userRole ->
                        rolesInDatabase.stream()
                                .anyMatch(dbRole -> dbRole.getName().equals(userRole.getName()))
                );

        if (userGotRole) {
            try (EntityManager em = emf.createEntityManager()) {
                em.getTransaction().begin();
                User managedUser = em.merge(user);
                em.getTransaction().commit();
                return managedUser;
            } catch (Exception e) {
                throw new DaoException("Error in createWithRole(): " + e.getMessage(), e);
            }
        } else {
            throw new ValidationException("Cannot create user without a valid role");
        }
    }

    @Override
    public UserDTO getVerifiedUser(String username, String password) throws ValidationException, DaoException {
        User user = findById(username);
        if (user == null) {
            throw new DaoException("Error reading object from db");
        }
        if (!user.verifyPassword(password)) {
            logger.error("{} {}", user.getUsername(), user.getPassword());
            throw new ValidationException("Password does not match");
        }
        return new UserDTO(user.getUsername(), user.getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toSet()));
    }

    @Override
    public User createUser(User user) {
        try (EntityManager em = emf.createEntityManager()) {
            User existingUser = em.find(User.class, user.getUsername());
            if (existingUser != null) {
                throw new EntityExistsException("User with username " + user.getUsername() + " already exists");
            }

            Set<Role> userRoles = user.getRoles();
            if (userRoles == null || userRoles.isEmpty()) {
                Role role = new Role("USER");
                user.addRole(role);
            }
            try {
                User createdUser = instance.createWithRole(user);
                logger.info("User created (username {})", user.getUsername());
                return createdUser;
            } catch (Exception e) {
                logger.error("Error creating user", e);
                throw new EntityExistsException("Error creating user", e);
            }
        }
    }

    @Override
    public User addRoleToUser(String username, Role role) throws DaoException {
        User foundUser = findById(username);
        if (foundUser == null) {
            throw new DaoException("Error reading object from db");
        }
        foundUser.addRole(role);
        try {
            foundUser = update(foundUser);
            logger.info("Role added to user (username {}, role {})", username, role);
            return foundUser;
        } catch (Exception e) {
            logger.error("Error adding role to user", e);
            throw new DaoException("Error adding role to user", e);
        }
    }

    @Override
    public User removeRoleFromUser(String username, Role role) {
        User foundUser = findById(username);
        if (foundUser == null) {
            throw new DaoException("Error reading object from db");
        }
        foundUser.removeRole(role.getName());
        try {
            foundUser = update(foundUser);
            logger.info("Role removed from user (username {}, role {})", username, role);
            return foundUser;
        } catch (Exception e) {
            logger.error("Error removing role from user", e);
            throw new DaoException("Error removing role from user", e);
        }
    }

    private Role findRoleByName(String roleName) {
        // Implement method to find Role by name
        // This is a placeholder implementation
        return new Role(roleName);
    }
}