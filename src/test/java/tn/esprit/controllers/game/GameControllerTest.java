package tn.esprit.controllers.game;

import org.junit.jupiter.api.Test;
import tn.esprit.entities.game.Game;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class GameControllerTest {

    @Test
    void validerGameShouldRejectEmptyTitle() throws Exception {
        GameController controller = new GameController();
        Game g = new Game();
        g.setTitre(""); // titre vide
        g.setType("QCM");
        g.setNiveau("Débutant");
        g.setAttemptNumber(3);

        Method method = GameController.class.getDeclaredMethod("validerGame", Game.class);
        method.setAccessible(true);
        Object result = method.invoke(controller, g);

        assertNotNull(result);
        assertTrue(((String) result).contains("Titre manquant"));
    }
}

