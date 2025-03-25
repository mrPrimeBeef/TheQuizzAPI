package app.daos;

import app.config.HibernateConfig;
import app.entities.Question;
import jakarta.persistence.EntityManagerFactory;

public class QuestionDao extends AbstractDao<Question, Integer>{
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
}
