package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "usercategoryoffset")
public class UserCategoryOffset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private Integer question_offset;

    public UserCategoryOffset() {}

    public UserCategoryOffset(String username, String category, Integer question_offset) {
        this.username = username;
        this.category = category;
        this.question_offset = question_offset;
    }
}