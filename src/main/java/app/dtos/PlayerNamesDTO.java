package app.dtos;

import app.entities.Player;

import java.util.List;

public record PlayerNamesDTO(List<PlayerNameAndPoints> players) {

    public static PlayerNamesDTO convertFromEntityToDTO(List<Player> players) {
        List<PlayerNameAndPoints> list = players.stream()
                .map(player -> new PlayerNameAndPoints(player.getName(), player.getPoints()))
                .toList();

        return new PlayerNamesDTO(list);
    }
}

