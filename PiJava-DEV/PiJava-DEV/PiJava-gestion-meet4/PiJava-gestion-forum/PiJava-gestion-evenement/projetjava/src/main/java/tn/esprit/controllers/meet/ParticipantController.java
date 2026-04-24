package tn.esprit.controllers.meet;

import tn.esprit.entities.meet.participant;
import tn.esprit.services.meet.ParticipantService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ParticipantController {
    private ParticipantService participantService;

    public ParticipantController() {
        participantService = new ParticipantService();
    }

    public void ajouterParticipant(participant p) {
        try {
            if (p.getNom() == null || p.getNom().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le nom est obligatoire !");
                return;
            }
            if (p.getPrenom() == null || p.getPrenom().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le prénom est obligatoire !");
                return;
            }
            if (p.getEmail() == null || p.getEmail().trim().isEmpty()) {
                System.out.println("❌ Erreur : L'email est obligatoire !");
                return;
            }
            if (p.getRole() == null || p.getRole().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le rôle est obligatoire !");
                return;
            }
            participantService.ajouter(p);
            System.out.println("✅ Participant ajouté avec succès ! (ID: " + p.getId() + ")");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de l'ajout du participant : " + ex.getMessage());
        }
    }

    public List<participant> recupererParticipants() {
        try {
            return participantService.recuperer();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération des participants : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void modifierParticipant(participant p) {
        try {
            if (p.getId() <= 0) {
                System.out.println("❌ Erreur : ID invalide !");
                return;
            }
            participantService.modifier(p);
            System.out.println("✅ Participant modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la modification du participant : " + ex.getMessage());
        }
    }

    public void supprimerParticipant(int id) {
        try {
            participantService.supprimer(id);
            System.out.println("✅ Participant supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression du participant : " + e.getMessage());
        }
    }

    public participant findById(int id) {
        try {
            return participantService.findById(id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return null;
        }
    }
}
