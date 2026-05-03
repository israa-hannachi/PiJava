package tn.esprit.services.meet;

import org.junit.jupiter.api.*;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.participant;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeetServiceTest {

    private static MeetService meetService;
    private static ParticipantService participantService;
    private static int testParticipantId;
    private static int testMeetId;

    @BeforeAll
    static void setUp() throws SQLException {
        meetService = new MeetService();
        participantService = new ParticipantService();

        // Need a participant to act as an organizer for the meet
        participant p = new participant("TestNom", "TestPrenom", "test@test.com", "Enseignant");
        participantService.ajouter(p);

        // Find the inserted participant to get its ID
        List<participant> parts = participantService.recuperer();
        testParticipantId = parts.get(parts.size() - 1).getId();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        // Cleanup the test data from the DB
        if (testMeetId > 0) {
            meetService.supprimer(testMeetId);
        }
        if (testParticipantId > 0) {
            participantService.supprimer(testParticipantId);
        }
    }

    @Test
    @Order(1)
    void testAjouterMeet() {
        Meet m = new Meet("Réunion de Projet", "Discussion sur le jalon 1",
            new Timestamp(System.currentTimeMillis() + 86400000), // tomorrow
            new Timestamp(System.currentTimeMillis() + 90000000),
            "meet.google.com/test", testParticipantId);

        assertDoesNotThrow(() -> {
            meetService.ajouter(m);
            testMeetId = m.getId(); // Capturing generated key
        });

        assertTrue(testMeetId > 0, "L'ID du meet généré doit être positif.");
    }

    @Test
    @Order(2)
    void testRecupererMeets() throws SQLException {
        List<Meet> meets = meetService.recuperer();
        assertNotNull(meets);
        assertTrue(meets.size() > 0, "La liste des meets ne doit pas être vide après un ajout.");
    }

    @Test
    @Order(3)
    void testFindById() throws SQLException {
        Meet m = meetService.findById(testMeetId);
        assertNotNull(m);
        assertEquals("Réunion de Projet", m.getTitre());
        assertEquals(testParticipantId, m.getParticipantId());
    }

    @Test
    @Order(4)
    void testModifierMeet() throws SQLException {
        Meet m = meetService.findById(testMeetId);
        assertNotNull(m);

        m.setTitre("Réunion Modifiée");
        assertDoesNotThrow(() -> meetService.modifier(m));

       Meet mUpdated = meetService.findById(testMeetId);
        assertEquals("Réunion Modifiée", mUpdated.getTitre());
    }

    @Test
    @Order(5)
    void testRechercherParTitre() throws SQLException {
        List<Meet> results = meetService.rechercherParTitre("Modifiée");
        assertFalse(results.isEmpty());
        assertEquals(testMeetId, results.get(0).getId());
    }

    @Test
    @Order(6)
    void testTrierParDateDebut() throws SQLException {
        List<Meet> meets = meetService.trierParDateDebut(true);
        assertNotNull(meets);
        if (meets.size() > 1) {
            assertTrue(meets.get(0).getDateDebut().compareTo(meets.get(1).getDateDebut()) <= 0);
        }
    }

    @Test
    @Order(7)
    void testSupprimerMeet() throws SQLException {
        assertDoesNotThrow(() -> meetService.supprimer(testMeetId));
        Meet deleted = meetService.findById(testMeetId);
        assertNull(deleted, "Le meet devrait être nul après suppression.");
        testMeetId = 0; // Prevent tearDown from failing
    }
}
