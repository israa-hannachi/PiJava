package tn.esprit.controllers.event;

import tn.esprit.entities.event.Registration;
import tn.esprit.services.event.RegistrationService;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class RegistrationController {
    private RegistrationService registrationService;

    public RegistrationController() {
        registrationService = new RegistrationService();
    }

    public void ajouterRegistration(Registration r) {
        try {
            if (r.getVisitorEmail() == null || r.getVisitorEmail().trim().isEmpty()) {
                System.out.println("Erreur : L'email du visiteur est obligatoire !");
                return;
            }
            registrationService.ajouter(r);
            System.out.println("Registration ajoutée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout de la registration : " + e.getMessage());
        }
    }

    public List<Registration> recupererRegistrations() {
        try {
            return registrationService.recuperer();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des registrations : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void modifierRegistration(Registration r) {
        try {
            registrationService.modifier(r);
            System.out.println("Registration modifiée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification de la registration : " + e.getMessage());
        }
    }

    public void supprimerRegistration(int id) {
        try {
            registrationService.supprimer(id);
            System.out.println("Registration supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de la registration : " + e.getMessage());
        }
    }

    public Registration findById(int id) {
        try {
            return registrationService.findById(id);
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
            return null;
        }
    }
}
