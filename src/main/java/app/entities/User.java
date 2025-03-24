package app.entities;

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

    @Setter
    @ManyToMany
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "name")
    )
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
        roles.add(role);
        role.users.add(this);
    }

    @Override
    public void removeRole(String role) {
            for (Role roleEntity : roles){
                if (roleEntity.getName().equals(role)){
                    roles.remove(roleEntity);
                    roleEntity.users.remove(this);
                }
            }
    }
}