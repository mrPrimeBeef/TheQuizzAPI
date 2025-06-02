package app.daos;

import app.exceptions.DaoException;
import jakarta.persistence.EntityManager;
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

    public Integer getLastedGameFromUsername(String username) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT ug.gameId FROM users_game ug WHERE ug.username = :username ORDER BY ug.gameId DESC";
            return em.createQuery(jpql, Integer.class)
                    .setParameter("username", username)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (Exception e) {
            throw new DaoException("Error in getting the highest gameId for user with name: " + username, e);
        }
    }
}