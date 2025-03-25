package app.daos;

import app.entities.Player;
import app.exceptions.ApiException;
import app.exceptions.DaoException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.samePropertyValuesAs;
import static org.junit.jupiter.api.Assertions.*;

class PlayerDaoTest extends TestSetup {
    @Test
    void createPlayer() {

        Player playerTest = new Player(0, "TestName2");

        playerDao.create(playerTest);

        Player playerFound = playerDao.findById(2);

        assertEquals(playerFound.getId(), playerTest.getId());
        assertThrows(DaoException.class, () -> playerDao.findById(3));
        // if there are a @ManyToMany use ignoredProperties
        assertThat(playerTest, samePropertyValuesAs(playerFound, "id"));
    }

    @Test
    void findPlayerById() {
        Player player = playerDao.findById(1);
        assertEquals(1, player.getId());
        assertThrows(DaoException.class, () -> playerDao.findById(2));
    }

    @Test
    void findAllPlayers() {
        List<Player> players = playerDao.findAll();
        assertEquals(1, players.size());
    }

    @Test
    void updatePlayer() {
        Player player = playerDao.findById(1);
        player.setName("OtherTestName");
        playerDao.update(player);
        Player player2 = playerDao.findById(1);

        assertThat(player, samePropertyValuesAs(player2));
        assertEquals(player2.getId(), player.getId());
    }

    @Test
    void deletePlayer() {
        playerDao.delete(1);

        assertThrows(DaoException.class, () -> playerDao.findById(1));
    }
}