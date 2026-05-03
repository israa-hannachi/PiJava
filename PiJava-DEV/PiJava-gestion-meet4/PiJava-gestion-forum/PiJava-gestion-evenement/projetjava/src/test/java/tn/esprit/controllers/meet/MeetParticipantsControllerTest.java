package tn.esprit.controllers.meet;

import org.junit.jupiter.api.*;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.Meet_Participants;
import tn.esprit.entities.meet.participant;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MeetParticipantsControllerTest {

    private static MeetParticipantsController controller;
    private static MeetController meetCtrl;
    private static ParticipantController partCtrl;
    private static int meetId, partId;

    @BeforeAll static void setup() {
        controller = new MeetParticipantsController();
        meetCtrl   = new MeetController();
        partCtrl   = new ParticipantController();

        // Créer meet et participant de test
        Meet m = new Meet("MP_Test_Meet", "test",
            Timestamp.valueOf(LocalDateTime.now().plusDays(1)),
            Timestamp.valueOf(LocalDateTime.now().plusDays(2)),
            null, 3);
        meetCtrl.ajouterMeet(m);
        meetId = m.getId();

        participant p = new participant("MP_Nom", "MP_Prenom", "mp_test@t.com", "etudiant");
        partCtrl.ajouterParticipant(p);
        partId = p.getId();
    }

    @Test @Order(1)
    void testAjouterParticipantAuMeet() {
        controller.ajouterParticipantAuMeet(meetId, partId);
        assertTrue(controller.isParticipantInscrit(meetId, partId));
    }

    @Test @Order(2)
    void testIsParticipantInscrit() {
        assertTrue(controller.isParticipantInscrit(meetId, partId));
    }

    @Test @Order(3)
    void testGetParticipantsDuMeet() {
        List<Meet_Participants> list = controller.getParticipantsDuMeet(meetId);
        assertFalse(list.isEmpty());
        assertEquals(partId, list.get(0).getParticipantId());
    }

    @Test @Order(4)
    void testGetMeetsDuParticipant() {
        List<Meet_Participants> list = controller.getMeetsDuParticipant(partId);
        assertFalse(list.isEmpty());
        assertEquals(meetId, list.get(0).getMeetId());
    }

    @Test @Order(5)
    void testAjouterDoublon() {
        // Ajouter deux fois le même — ne doit pas créer un doublon
        controller.ajouterParticipantAuMeet(meetId, partId);
        List<Meet_Participants> list = controller.getParticipantsDuMeet(meetId);
        assertEquals(1, list.size(), "Pas de doublon autorisé");
    }

    @Test @Order(6)
    void testRetirerParticipantDuMeet() {
        controller.retirerParticipantDuMeet(meetId, partId);
        assertFalse(controller.isParticipantInscrit(meetId, partId));
    }

    @AfterAll static void cleanup() {
        meetCtrl.supprimerMeet(meetId);
        partCtrl.supprimerParticipant(partId);
    }
}
