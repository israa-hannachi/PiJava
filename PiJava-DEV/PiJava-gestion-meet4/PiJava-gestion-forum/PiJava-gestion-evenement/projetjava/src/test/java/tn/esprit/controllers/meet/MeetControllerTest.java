package tn.esprit.controllers.meet;

import org.junit.jupiter.api.*;
import tn.esprit.entities.meet.Meet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeetControllerTest {

    private static MeetController controller;
    private static int createdId;

    @BeforeAll
    static void setup() { controller = new MeetController(); }

    @Test @Order(1)
    void testAjouterMeet() {
        Meet m = new Meet("Test JUnit Meet", "Description test",
            Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
            Timestamp.valueOf(LocalDateTime.now().plusDays(1).plusHours(2)),
            null, 3);
        controller.ajouterMeet(m);
        assertTrue(m.getId() > 0, "L'ID doit être positif après insertion");
        createdId = m.getId();
    }

    @Test @Order(2)
    void testRecupererMeets() {
        List<Meet> list = controller.recupererMeets();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test @Order(3)
    void testFindById() {
        Meet found = controller.findById(createdId);
        assertNotNull(found);
        assertEquals("Test JUnit Meet", found.getTitre());
    }

    @Test @Order(4)
    void testModifierMeet() {
        Meet m = controller.findById(createdId);
        assertNotNull(m);
        m.setTitre("Meet Modifié JUnit");
        controller.modifierMeet(m);
        Meet updated = controller.findById(createdId);
        assertEquals("Meet Modifié JUnit", updated.getTitre());
    }

    @Test @Order(5)
    void testAjouterMeetTitreVide() {
        Meet m = new Meet("", "desc",
            Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
            Timestamp.valueOf(LocalDateTime.now().plusDays(2)),
            null, 3);
        // Le controller affiche erreur sans exception — on vérifie que l'id reste 0
        controller.ajouterMeet(m);
        assertEquals(0, m.getId(), "Un meet avec titre vide ne doit pas être inséré");
    }

    @Test @Order(6)
    void testAjouterMeetDateFinAvantDebut() {
        Meet m = new Meet("Bad Dates",
            "desc",
            Timestamp.valueOf(LocalDateTime.now().plusDays(3)),
            Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
            null, 3);
        controller.ajouterMeet(m);
        assertEquals(0, m.getId(), "Un meet avec date fin avant début ne doit pas être inséré");
    }

    @Test @Order(7)
    void testSupprimerMeet() {
        controller.supprimerMeet(createdId);
        Meet deleted = controller.findById(createdId);
        assertNull(deleted, "Le meet supprimé ne doit plus exister");
    }
}
