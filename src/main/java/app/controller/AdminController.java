package app.controller;

import io.javalin.http.Context;
import jakarta.persistence.EntityManagerFactory;

import app.daos.QuestionDao;
import app.daos.RoleDao;
import app.daos.SecurityDAO;
import app.entities.Question;
import app.utils.Populate;

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
        Populate.addComputerSienceQuestions(emf);
    }
    public void populateDatabaseWithGenerelKnowledgdeQuestions() {
        Populate.addGeneralKnowledgeQuestions(emf);
    }
    public void populateDatabaseWithSienceAndNatureQuestions() {
        Populate.addSienceAndNatureQuestions(emf);
    }
    public void populateDatabaseWithEntertainmentFilmQuestions() {
        Populate.addEntertainmentFilmQuestions(emf);
    }

    public void populateDatabaseRoles() {
        Populate.usersAndRoles(securityDAO);
    }

    public Integer createQuestion(Context ctx) {
        Question question = ctx.bodyAsClass(Question.class);

        Question q = questionDao.create(question);

        return q.getId();
    }

    public void deleteQuestion(Context ctx) {
        int removeId = Integer.parseInt(ctx.pathParam("questionid"));
        questionDao.delete(removeId);
    }


}
