package app.dtos;


import app.entities.enums.GameMode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRequestDTO {
    private int limit;
    private String category;
    private String difficulty;
    private GameMode gameMode;
}