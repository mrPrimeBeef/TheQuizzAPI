package app.daos;

import app.entities.enums.Role;
import app.entities.User;
import app.exceptions.ValidationException;
import dk.bugelhartmann.UserDTO;


public interface ISecurityDAO
{
    UserDTO getVerifiedUser(String username, String password) throws ValidationException;
    User createUser(String username, String password);
    User addRoleToUser(String username, Role role);
    User removeRoleFromUser(String username, Role role);
}
