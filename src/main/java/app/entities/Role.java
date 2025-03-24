package app.entities;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
@Entity
@Table(name = "roles")
@NamedQueries(@NamedQuery(name = "Role.deleteAll", query = "DELETE FROM Role"))
public class Role {
    @Id
    private String name;

    @ManyToMany(mappedBy = "roles")
    Set<User> users = new HashSet<>();

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }
}