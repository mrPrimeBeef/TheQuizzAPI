package app.daos;

import app.exceptions.DaoException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import app.entities.Question;
import jakarta.persistence.TypedQuery;

import java.util.List;

public class QuestionDao extends AbstractDao<Question, Integer> {
    private static QuestionDao instance;

    private QuestionDao(EntityManagerFactory emf) {
        super(Question.class, emf);
    }

    public static QuestionDao getInstance(EntityManagerFactory emf) {
        if (instance == null) {
            instance = new QuestionDao(emf);
        }
        return instance;
    }

    public List<Question> findQuestionWithCategory(String category, Integer limit) {
        category = "Science & Nature".equals(category) ? "Science &amp; Nature" : category;

        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT q FROM Question q WHERE q.category = :category"; // Brug ":" foran parameteren
            TypedQuery<Question> query = em.createQuery(jpql, Question.class);

            query.setParameter("category", category);
            query.setMaxResults(limit);

            return query.getResultList();
        } catch (Exception e) {
            throw new DaoException("Error in questions with category: " + category, e);
        }
    }
}
