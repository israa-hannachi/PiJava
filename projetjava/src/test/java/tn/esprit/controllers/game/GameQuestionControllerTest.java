package tn.esprit.controllers.game;

import org.junit.jupiter.api.Test;
import tn.esprit.entities.game.Game;
import tn.esprit.entities.game.Game_Question;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GameQuestionControllerTest {

    @Test
    void validerQuestionShouldRejectEmptyText() throws Exception {
        GameQuestionController controller = new GameQuestionController();
        Game_Question q = new Game_Question();
        q.setQuestionText(""); // question vide
        q.setOption1("Paris");
        q.setOption2("Rome");
        q.setOption3("Madrid");
        q.setOption4("Tunis");
        q.setCorrectAnswer("Tunis");

        Method method = GameQuestionController.class.getDeclaredMethod("validerQuestion", Game_Question.class);
        method.setAccessible(true);
        Object result = method.invoke(controller, q);

        assertNotNull(result);
        assertTrue(((String) result).contains("Question manquante"));
    }

    @Test
    void validerQuestionShouldRejectInvalidCorrectAnswer() throws Exception {
        GameQuestionController controller = new GameQuestionController();
        Game_Question q = new Game_Question();
        q.setQuestionText("Quelle est la capitale de la Tunisie ?");
        q.setOption1("Paris");
        q.setOption2("Rome");
        q.setOption3("Madrid");
        q.setOption4("Berlin");
        q.setCorrectAnswer("Tunis"); // ne correspond à aucune option

        Method method = GameQuestionController.class.getDeclaredMethod("validerQuestion", Game_Question.class);
        method.setAccessible(true);
        Object result = method.invoke(controller, q);

        assertNotNull(result);
        assertTrue(((String) result).contains("La réponse correcte doit correspondre"));
    }
}
