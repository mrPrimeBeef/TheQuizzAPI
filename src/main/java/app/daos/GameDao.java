package app.daos;

import app.config.HibernateConfig;
import app.entities.Game;
import app.entities.Question;
import jakarta.persistence.EntityManagerFactory;

public class GameDao extends AbstractDao<Game, Integer>{
    private static EntityManagerFactory emf = HibernateConfig.getEntityManagerFactory();
    private static GameDao instance;

    private GameDao(EntityManagerFactory emf) {
        super(Game.class, emf);
    }

    public static GameDao getInstance() {
        if (instance == null) {
            instance = new GameDao(emf);
        }
        return instance;
    }
}
