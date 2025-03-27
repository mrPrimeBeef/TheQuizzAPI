package app.daos;

import app.entities.Player;
import app.entities.Question;
import app.entities.enums.Difficulty;
import app.exceptions.DaoException;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;

class QuestionDaoTest extends TestSetup {
    @Test
    void createQuestion() {
        List<String> wrongAnswers = new ArrayList<>();
        wrongAnswers.add("Test1");
        wrongAnswers.add("Test2");
        Question testQuestion = new Question("is this a NewTestQuestion?", "Yes", wrongAnswers, "Computer:Sience", Difficulty.EASY);

        questionDao.create(testQuestion);

        Question questionFound = questionDao.findById(2);

        assertEquals(questionFound.getId(), testQuestion.getId());
        assertThrows(DaoException.class, () -> questionDao.findById(3));
    }

    @Test
    void findQuestionById() {
        Question question = questionDao.findById(1);
        assertEquals(1, question.getId());
        assertThrows(DaoException.class, () -> playerDao.findById(2));
    }

    @Test
    void findAllQuesstions() {
        List<Question> questions = questionDao.findAll();
        assertEquals(1, questions.size());
    }

    @Test
    void updateQuestion() {
        Question question1 = questionDao.findById(1);
        question1.setDescription("This is a new desription");
        questionDao.update(question1);

        Question question2 = questionDao.findById(1);

        assertEquals("This is a new desription", question2.getDescription());
    }

    @Test
    void deleteRoom() {
        playerDao.delete(1);
        assertThrows(DaoException.class, () -> playerDao.findById(1));
    }
}