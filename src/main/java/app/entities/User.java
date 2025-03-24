package app.entities;

import app.entities.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Entity
@Table(name = "users")
@NamedQueries(@NamedQuery(name = "User.deleteAll", query = "DELETE FROM User"))
public class User implements ISecurityUser {
    @Id
    private String username;
    private String password;

    @ManyToMany
    private List<Game> games;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    Set<Role> roles = new HashSet<>();

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    @Override
    public Set<String> getRolesAsStrings() {
        return Set.of();
    }

    @Override
    public boolean verifyPassword(String pw) {
        return BCrypt.checkpw(pw, this.password);
    }

    @Override
    public void addRole(Role role) {
        if (role != null) {
            roles.add(role);
        }
    }

    @Override
    public void removeRole(String role) {
        roles.remove(role);
    }
}