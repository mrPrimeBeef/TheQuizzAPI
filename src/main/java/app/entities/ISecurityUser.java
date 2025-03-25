package app.entities;

import app.entities.enums.Role;

import java.util.Set;

public interface ISecurityUser {
    Set<String> getRolesAsStrings();
    boolean verifyPassword(String pw);
    void addRole(Role role);
    void removeRole(String role);
}