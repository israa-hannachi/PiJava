package tn.esprit.services.meet;

import org.junit.jupiter.api.*;
import tn.esprit.entities.meet.participant;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ParticipantServiceTest {

    private static ParticipantService participantService;
    private static int testParticipantId;

    @BeforeAll
    static void setUp() {
        participantService = new ParticipantService();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        if (testParticipantId > 0) {
            participantService.supprimer(testParticipantId);
        }
    }

    @Test
    @Order(1)
    void testAjouterParticipant() {
        participant p = new participant("AdminNom", "AdminPrenom", "admin@meet.com", "Admin");

        assertDoesNotThrow(() -> {
            participantService.ajouter(p);
            testParticipantId = p.getId();
        });

        assertTrue(testParticipantId > 0, "L'ID du participant généré doit être positif.");
    }

    @Test
    @Order(2)
    void testRecupererParticipants() throws SQLException {
        List<participant> participants = participantService.recuperer();
        assertNotNull(participants);
        assertTrue(participants.size() > 0, "La liste de participants ne doit pas être vide.");
    }

    @Test
    @Order(3)
    void testFindById() throws SQLException {
        participant p = participantService.findById(testParticipantId);
        assertNotNull(p);
        assertEquals("admin@meet.com", p.getEmail());
    }

    @Test
    @Order(4)
    void testModifierParticipant() throws SQLException {
        participant p = participantService.findById(testParticipantId);
        p.setRole("Moderator");

        assertDoesNotThrow(() -> participantService.modifier(p));

        participant pUpdated = participantService.findById(testParticipantId);
        assertEquals("Moderator", pUpdated.getRole());
    }

    @Test
    @Order(5)
    void testRechercherParNom() throws SQLException {
        List<participant> list = participantService.rechercherParNom("AdminNom");
        assertFalse(list.isEmpty());
        assertEquals(testParticipantId, list.get(0).getId());
    }

    @Test
    @Order(6)
    void testFiltrerParRole() throws SQLException {
        List<participant> list = participantService.filtrerParRole("Moderator");
        assertFalse(list.isEmpty());
        assertTrue(list.stream().anyMatch(p -> p.getId() == testParticipantId));
    }

    @Test
    @Order(7)
    void testSupprimerParticipant() throws SQLException {
        assertDoesNotThrow(() -> participantService.supprimer(testParticipantId));
        participant deleted = participantService.findById(testParticipantId);
        assertNull(deleted);
        testParticipantId = 0; // Prevent teardown fail
    }
}
