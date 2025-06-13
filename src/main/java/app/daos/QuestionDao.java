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

    public List<Question> findQuestionWithCategory(String username, String category, Integer limit) {
        category = "Science & Nature".equals(category) ? "Science &amp; Nature" : category;

        int offset = getAndUpdateUserOffset(username, category, limit);

        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT q FROM Question q WHERE q.category = :category ORDER BY q.id";
            TypedQuery<Question> query = em.createQuery(jpql, Question.class);

            query.setParameter("category", category);
            query.setFirstResult(offset); // Start from the user's offset
            query.setMaxResults(limit);

            return query.getResultList();
        } catch (Exception e) {
            throw new DaoException("Error in questions with category: " + category, e);
        }
    }

    public int getAndUpdateUserOffset(String username, String category, int questionCount) {
        try (EntityManager em = emf.createEntityManager()) {
            em.getTransaction().begin();

            // Fetch current offset for the user and category
            Integer offset = em.createQuery(
                            "SELECT uco.question_offset FROM UserCategoryOffset uco WHERE uco.username = :username AND uco.category = :category",
                            Integer.class)
                    .setParameter("username", username)
                    .setParameter("category", category)
                    .getResultStream()
                    .findFirst()
                    .orElse(0); // Default to 0 if no entry exists

            // Fetch total number of questions in the category
            Long totalQuestions = em.createQuery(
                            "SELECT COUNT(q) FROM Question q WHERE q.category = :category", Long.class)
                    .setParameter("category", category)
                    .getSingleResult();

            // Calculate new offset
            int newOffset = (offset + questionCount) % totalQuestions.intValue();

            // Update or insert the offset in the database
            int updated = em.createQuery(
                            "UPDATE UserCategoryOffset uco SET uco.question_offset = :newOffset WHERE uco.username = :username AND uco.category = :category")
                    .setParameter("newOffset", newOffset)
                    .setParameter("username", username)
                    .setParameter("category", category)
                    .executeUpdate();

            if (updated == 0) {
                em.createNativeQuery("INSERT INTO UserCategoryOffset (username, category, question_offset) VALUES (:username, :category, :question_offset)")
                        .setParameter("username", username)
                        .setParameter("category", category)
                        .setParameter("question_offset", newOffset)
                        .executeUpdate();
            }

            em.getTransaction().commit();
            return offset;
        } catch (Exception e) {
            throw new DaoException("Error updating offset for user: " + username + " and category: " + category, e);
        }
    }
}
