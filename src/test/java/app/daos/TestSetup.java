package app.daos;

import app.config.HibernateConfig;
import app.entities.Player;
import app.entities.Question;
import app.entities.enums.Difficulty;
import app.entities.enums.Role;
import app.utils.Populator;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;

import java.util.ArrayList;
import java.util.List;

public class TestSetup {
    protected static final EntityManagerFactory emf = HibernateConfig.getEntityManagerFactoryForTest();
    protected static final QuestionDao questionDao = QuestionDao.getInstance(emf);
    protected static final PlayerDao playerDao = PlayerDao.getInstance(emf);
    @BeforeEach
    void setup(){
        try(EntityManager em = emf.createEntityManager()){
            em.getTransaction().begin();
            em.createQuery("DELETE FROM Question").executeUpdate();
            em.createQuery("DELETE FROM Player").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE question_id_seq RESTART WITH 1").executeUpdate();
            em.createNativeQuery("ALTER SEQUENCE player_id_seq RESTART WITH 1").executeUpdate();
            em.getTransaction().commit();

            List<String> wrongAnswers = new ArrayList<>();
            wrongAnswers.add("no");
            wrongAnswers.add("no2");
            questionDao.create(new Question("is this a TestQuestion?", "Yes", wrongAnswers,"Computer:Sience", Difficulty.EASY));

            playerDao.create(new Player(0,"testName"));

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    @AfterAll
    static void tearDown() {
        if (emf != null && emf.isOpen()) {
            emf.close();
            System.out.println("EntityManagerFactory closed.");
        }
    }
}