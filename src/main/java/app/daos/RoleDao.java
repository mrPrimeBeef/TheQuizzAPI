package app.daos;

import jakarta.persistence.EntityManagerFactory;

import app.entities.Role;

public class RoleDao extends AbstractDao<Role, Integer> {
    private static RoleDao instance;

    private RoleDao(EntityManagerFactory emf) {
        super(Role.class, emf);
    }

    public static RoleDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new RoleDao(emf);
        }
        return instance;
    }
}
