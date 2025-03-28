package app.daos;

import app.config.HibernateConfig;
import app.entities.Player;
import app.entities.Question;
import app.exceptions.DaoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class PlayerDao extends AbstractDao<Player, Integer>{
    private static PlayerDao instance;

    private PlayerDao(EntityManagerFactory emf) {
        super(Player.class, emf);
    }

    public static PlayerDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new PlayerDao(emf);
        }
        return instance;
    }
    public List<Player> findAllPlayersByGameId(Integer gameId) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT p FROM Player p WHERE p.game.id = :gameId"; // Brug ":" foran parameteren
            TypedQuery<Player> query = em.createQuery(jpql, Player.class);

            query.setParameter("gameId", gameId);
            return query.getResultList();

        } catch (Exception e) {
            throw new DaoException("Error in finding all players in game: " + gameId, e);
        }
    }
}
//try(EntityManager em = emf.createEntityManager()){
//String jpql = "SELECT SUM (e.salary) FROM Employee e";
//TypedQuery<Double> query = em.createQuery(jpql, Double.class);
//            return query.getSingleResult();
//        }