package app.daos;

import app.config.HibernateConfig;
import app.entities.Game;
import app.entities.Question;
import jakarta.persistence.EntityManagerFactory;

public class GameDao extends AbstractDao<Game, Integer>{
    private static GameDao instance;

    private GameDao(EntityManagerFactory emf) {
        super(Game.class, emf);
    }

    public static GameDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new GameDao(emf);
        }
        return instance;
    }
}
