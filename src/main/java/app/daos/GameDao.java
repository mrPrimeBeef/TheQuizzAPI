package app.daos;

import jakarta.persistence.EntityManagerFactory;

import app.entities.Game;

public class GameDao extends AbstractDao<Game, Integer> {
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