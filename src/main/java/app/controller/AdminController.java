package app.controller;

import java.util.ArrayList;
import java.util.List;

import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import app.daos.QuestionDao;
import app.daos.RoleDao;
import app.daos.SecurityDAO;
import app.entities.Question;
import app.utils.Populate;
import app.dtos.QuestionBody;
import app.dtos.QuestionDTO;

public class AdminController {
    private EntityManagerFactory emf;
    private SecurityDAO securityDAO;
    private RoleDao roleDao;
    private QuestionDao questionDao;

    public AdminController(EntityManagerFactory emf) {
        this.emf = emf;
        this.roleDao = RoleDao.getInstance(emf);
        this.questionDao = QuestionDao.getInstance(emf);
        this.securityDAO = SecurityDAO.getInstance(emf, roleDao);
    }

    public void populateDatabaseWithScienceComputersQuestions() {
        Populate.addQuestions(emf);
    }

    public void populateDatabaseRoles() {
        Populate.usersAndRoles(securityDAO);
    }

    public QuestionDTO createQuestion(Context ctx) {
        Question question = ctx.bodyAsClass(Question.class);

        Question q = questionDao.create(question);

        List<QuestionBody> newQuestion = new ArrayList<>();
        newQuestion.add(new QuestionBody(q.getDifficulty().toString(), q.getCategory(), q.getDescription(), q.getRightAnswer(), q.getWrongAnswers()));

        QuestionDTO questionDTO = new QuestionDTO(newQuestion);

        return questionDTO;
    }

    public void deleteQuestion(Context ctx) {
        int removeId = Integer.parseInt(ctx.pathParam("questionid"));
        questionDao.delete(removeId);
    }
}
