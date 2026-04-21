package tn.esprit.controllers.meet;

import tn.esprit.entities.meet.Meet;
import tn.esprit.services.meet.MeetService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MeetController {
    private MeetService meetService;

    public MeetController() {
        meetService = new MeetService();
    }

    public void ajouterMeet(Meet m) {
        try {
            if (m.getTitre() == null || m.getTitre().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le titre est obligatoire !");
                return;
            }
            if (m.getDateDebut() == null) {
                System.out.println("❌ Erreur : La date de début est obligatoire !");
                return;
            }
            if (m.getDateFin() == null) {
                System.out.println("❌ Erreur : La date de fin est obligatoire !");
                return;
            }
            if (m.getDateFin().before(m.getDateDebut())) {
                System.out.println("❌ Erreur : La date de fin doit être après la date de début !");
                return;
            }
            if (m.getParticipantId() <= 0) {
                System.out.println("❌ Erreur : L'ID du participant (organisateur) est obligatoire !");
                return;
            }
            meetService.ajouter(m);
            System.out.println("✅ Meet ajouté avec succès ! (ID: " + m.getId() + ")");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de l'ajout du meet : " + ex.getMessage());
        }
    }

    public List<Meet> recupererMeets() {
        try {
            return meetService.recuperer();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des meets : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void modifierMeet(Meet m) {
        try {
            if (m.getId() <= 0) {
                System.out.println("❌ Erreur : ID invalide !");
                return;
            }
            meetService.modifier(m);
            System.out.println("✅ Meet modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la modification du meet : " + ex.getMessage());
        }
    }

    public void supprimerMeet(int id) {
        try {
            meetService.supprimer(id);
            System.out.println("✅ Meet supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression du meet : " + e.getMessage());
        }
    }

    public Meet findById(int id) {
        try {
            return meetService.findById(id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return null;
        }
    }

    public List<Meet> findByParticipantId(int participantId) {
        try {
            return meetService.findByParticipantId(participantId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
