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
//        assertThrows(DaoException.class, () -> questionDao.findById(3));
        // if there are a @ManyToMany use ignoredProperties
        //assertThat(testQuestion, samePropertyValuesAs(questionFound, "id", "games"));
    }

//    @Test
//    void findRoomById() {
//        Player player = playerDao.findById(1);
//        assertEquals(1, player.getId());
//        assertThrows(DaoException.class, () -> playerDao.findById(2));
//    }
//
//    @Test
//    void findAllRooms() {
//        List<Player> players = playerDao.findAll();
//        assertEquals(1, players.size());
//    }
//
//    @Test
//    void updateRoom() {
//        Player player = playerDao.findById(1);
//        player.setName("OtherTestName");
//        playerDao.update(player);
//        Player player2 = playerDao.findById(1);
//
//        assertThat(player, samePropertyValuesAs(player2));
//        assertEquals(player2.getId(), player.getId());
//    }
//
//    @Test
//    void deleteRoom() {
//        playerDao.delete(1);
//
//        assertThrows(DaoException.class, () -> playerDao.findById(1));
//    }

}