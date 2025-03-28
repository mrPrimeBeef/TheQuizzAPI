package app.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "roles")
public class Role {
    @Id
    private String name;

    @ManyToMany(mappedBy = "roles", fetch = FetchType.EAGER)
    Set<User> users = new HashSet<>();

    public Role() {
    }

    public Role(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Role{" +
                "name='" + name + '\'' +
                '}';
    }
}