package app.daos;

import app.config.HibernateConfig;
import app.entities.Role;
import jakarta.persistence.EntityManagerFactory;

public class RoleDao extends AbstractDao<Role, Integer> {
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static RoleDao instance;

    private RoleDao(EntityManagerFactory emf) {
        super(Role.class, emf);
    }

    public static RoleDao getInstance() {
        if (instance == null) {
            instance = new RoleDao(emf);
        }
        return instance;
    }
}
