package tn.esprit.controllers.cours;

import tn.esprit.entities.cours.cours_module;
import tn.esprit.services.cours.CoursModuleService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CoursModuleController {
    private CoursModuleService service;

    public CoursModuleController() {
        service = new CoursModuleService();
    }

    public void ajouterModule(cours_module m) {
        try {
            if (m.getTitre() == null || m.getTitre().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le titre du module est obligatoire !");
                return;
            }
            if (m.getCategorieId() <= 0) {
                System.out.println("❌ Erreur : L'ID de la catégorie est invalide !");
                return;
            }
            service.ajouter(m);
            System.out.println("✅ Module ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de l'ajout : " + ex.getMessage());
        }
    }

    public List<cours_module> recupererModules() {
        try {
            return service.recuperer();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void modifierModule(cours_module m) {
        try {
            if (m.getId() <= 0) {
                System.out.println("❌ Erreur : ID invalide !");
                return;
            }
            service.modifier(m);
            System.out.println("✅ Module modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la modification : " + ex.getMessage());
        }
    }

    public void supprimerModule(int id) {
        try {
            service.supprimer(id);
            System.out.println("✅ Module supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public cours_module findById(int id) {
        try {
            return service.findById(id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return null;
        }
    }
}