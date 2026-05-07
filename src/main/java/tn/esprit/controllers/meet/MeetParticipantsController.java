package tn.esprit.controllers.meet;

import tn.esprit.entities.meet.meet_participants;
import tn.esprit.services.meet.MeetParticipantsService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MeetParticipantsController {
    private MeetParticipantsService meetParticipantsService;

    public MeetParticipantsController() {
        meetParticipantsService = new MeetParticipantsService();
    }

    public void ajouterParticipantAuMeet(int meetId, int participantId) {
        try {
            if (meetId <= 0 || participantId <= 0) {
                System.out.println("❌ Erreur : IDs invalides !");
                return;
            }
            meetParticipantsService.ajouter(new meet_participants(meetId, participantId));
            System.out.println("✅ Participant " + participantId + " ajouté au meet " + meetId + " avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de l'ajout : " + ex.getMessage());
        }
    }

    public void retirerParticipantDuMeet(int meetId, int participantId) {
        try {
            meetParticipantsService.supprimer(meetId, participantId);
            System.out.println("✅ Participant " + participantId + " retiré du meet " + meetId + " avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public List<meet_participants> getParticipantsDuMeet(int meetId) {
        try {
            return meetParticipantsService.findByMeetId(meetId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<meet_participants> getMeetsDuParticipant(int participantId) {
        try {
            return meetParticipantsService.findByParticipantId(participantId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean isParticipantInscrit(int meetId, int participantId) {
        try {
            return meetParticipantsService.exists(meetId, participantId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return false;
        }
    }
}
