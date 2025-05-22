package app.dtos;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GameRequestDTO {
    private int limit;
    private String category;
    private String difficulty;
}