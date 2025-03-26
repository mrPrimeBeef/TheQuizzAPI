package app.daos;

import app.entities.Role;
import app.entities.User;
import app.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;


public interface ISecurityDAO
{
    UserDTO getVerifiedUser(String username, String password) throws ValidationException;
    User createUser(User user);
    User addRoleToUser(String username, Role role);
    User removeRoleFromUser(String username, Role role);
}
