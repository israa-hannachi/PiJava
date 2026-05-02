package tn.esprit.controllers.meet;

import org.junit.jupiter.api.*;
import tn.esprit.entities.meet.participant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParticipantControllerTest {

    private static ParticipantController controller;
    private static int createdId;

    @BeforeAll static void setup() { controller = new ParticipantController(); }

    @Test @Order(1)
    void testAjouterParticipant() {
        participant p = new participant("TestNom", "TestPrenom", "test_junit@test.com", "etudiant");
        controller.ajouterParticipant(p);
        assertTrue(p.getId() > 0);
        createdId = p.getId();
    }

    @Test @Order(2)
    void testRecupererParticipants() {
        List<participant> list = controller.recupererParticipants();
        assertNotNull(list);
        assertFalse(list.isEmpty());
    }

    @Test @Order(3)
    void testFindById() {
        participant found = controller.findById(createdId);
        assertNotNull(found);
        assertEquals("TestNom", found.getNom());
    }

    @Test @Order(4)
    void testModifierParticipant() {
        participant p = controller.findById(createdId);
        p.setNom("NomModifie");
        controller.modifierParticipant(p);
        assertEquals("NomModifie", controller.findById(createdId).getNom());
    }

    @Test @Order(5)
    void testAjouterNomVide() {
        participant p = new participant("", "Prenom", "email@t.com", "etudiant");
        controller.ajouterParticipant(p);
        assertEquals(0, p.getId());
    }

    @Test @Order(6)
    void testAjouterEmailVide() {
        participant p = new participant("Nom", "Prenom", "", "etudiant");
        controller.ajouterParticipant(p);
        assertEquals(0, p.getId());
    }

    @Test @Order(7)
    void testSupprimerParticipant() {
        controller.supprimerParticipant(createdId);
        assertNull(controller.findById(createdId));
    }
}
