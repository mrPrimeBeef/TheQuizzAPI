package app.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.mindrot.jbcrypt.BCrypt;

@Getter
@Entity
@Table(name = "users")
@NamedQueries(@NamedQuery(name = "User.deleteAll", query = "DELETE FROM User"))
public class User implements ISecurityUser {
    @Id
    @Column(unique = true)
    private String username;
    private String password;

    @Setter
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_role",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "name")
    )
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_game",
            joinColumns = @JoinColumn(name = "username"),
            inverseJoinColumns = @JoinColumn(name = "game_id")
    )
    private List<Game> games = new ArrayList<>();

    public User() {
    }

    public User(String username, String password) {
        this.username = username;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
    }

    public void addGame(Game game) {
        if (!games.contains(game)) {
            games.add(game);
            game.getUsers().add(this);
        }
    }

    @Override
    public Set<String> getRolesAsStrings() {
        Set<String> roleNames = new HashSet<>();
        for (Role role : roles) {
            roleNames.add(role.getName());
        }
        return roleNames;
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

    public void removeRole(String roleName) {
        roles.removeIf(role -> role.getName().equals(roleName));
    }
}