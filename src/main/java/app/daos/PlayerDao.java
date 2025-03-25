package app.daos;

import app.config.HibernateConfig;
import app.entities.Player;
import app.entities.Question;
import jakarta.persistence.EntityManagerFactory;

public class PlayerDao extends AbstractDao<Player, Integer>{
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static PlayerDao instance;

    private PlayerDao(EntityManagerFactory emf) {
        super(Player.class, emf);
    }

    public static PlayerDao getInstance() {
        if (instance == null) {
            instance = new PlayerDao(emf);
        }
        return instance;
    }
}
