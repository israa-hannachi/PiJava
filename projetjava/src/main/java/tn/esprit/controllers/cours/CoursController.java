package tn.esprit.controllers.cours;

import tn.esprit.entities.cours.cours;
import tn.esprit.services.cours.CoursService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CoursController {
    private CoursService service;

    public CoursController() {
        service = new CoursService();
    }

    public void ajouterCours(cours c) {
        try {
            if (c.getTitre() == null || c.getTitre().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le titre du cours est obligatoire !");
                return;
            }
            if (c.getDuree() <= 0) {
                System.out.println("❌ Erreur : La durée doit être positive !");
                return;
            }
            if (c.getModuleId() <= 0) {
                System.out.println("❌ Erreur : L'ID du module est invalide !");
                return;
            }
            service.ajouter(c);
            System.out.println("✅ Cours ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de l'ajout : " + ex.getMessage());
        }
    }

    public List<cours> recupererCours() {
        try {
            return service.recuperer();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void modifierCours(cours c) {
        try {
            if (c.getId() <= 0) {
                System.out.println("❌ Erreur : ID invalide !");
                return;
            }
            service.modifier(c);
            System.out.println("✅ Cours modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la modification : " + ex.getMessage());
        }
    }

    public void supprimerCours(int id) {
        try {
            service.supprimer(id);
            System.out.println("✅ Cours supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public cours findById(int id) {
        try {
            return service.findById(id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return null;
        }
    }

    public List<cours> findByModuleId(int moduleId) {
        try {
            return service.findByModuleId(moduleId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }
}