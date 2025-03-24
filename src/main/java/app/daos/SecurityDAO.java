package app.daos;

import app.entities.User;
import app.exceptions.DaoException;
import app.exceptions.ValidationException;
import app.entities.enums.Role;
import dk.bugelhartmann.UserDTO;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class SecurityDAO extends AbstractDao<User, Integer> implements ISecurityDAO {
    private static SecurityDAO instance;
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

    @Override
    public UserDTO getVerifiedUser(String username, String password) throws DaoException, ValidationException {

        User user = instance.findById(username); //Throws DaoException if user not found
        if (!user.verifyPassword(password)) {
            logger.error("{} {}", user.getUsername(), user.getPassword());
            throw new ValidationException("Password does not match");
        }
        return new UserDTO(user.getUsername(), user.getRoles()
                .stream()
                .map(Role::toString)
                .collect(Collectors.toSet()));

    }

    @Override
    public User createUser(String username, String password) {
        User user = new User(username, password);
        user.addRole(Role.USER);
        try {
            user = instance.create(user);
            logger.info("User created (username {})", username);
            return user;
        } catch (Exception e) {
            logger.error("Error creating user", e);
            throw new EntityExistsException("Error creating user", e);
        }
    }

    @Override
    public User addRoleToUser(String username, Role role) {
        User foundUser = super.findById(username);
        foundUser.addRole(role);
        try {
            foundUser = super.update(foundUser);
            logger.info("Role added to user (username {}, role {})", username, role);
            return foundUser;
        } catch (Exception e) {
            logger.error("Error adding role to user", e);
            throw new DaoException("Error adding role to user", e);
        }
    }

    @Override
    public User removeRoleFromUser(String username, Role role) {
        User foundUserAccount = super.findById(username);
        foundUserAccount.removeRole(role.toString());
        try {
            foundUserAccount = super.update(foundUserAccount);
            logger.info("Role removed from user (username {}, role {})", username, role);
            return foundUserAccount;
        } catch (Exception e) {
            logger.error("Error removing role from user", e);
            throw new DaoException("Error removing role from user", e);
        }
    }
}