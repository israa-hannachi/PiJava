package tn.esprit.controllers.cours;

import tn.esprit.entities.cours.cours_categorie;
import tn.esprit.services.cours.CoursCategorieService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CoursCategorieController {
    private CoursCategorieService service;

    public CoursCategorieController() {
        service = new CoursCategorieService();
    }

    public void ajouterCategorie(cours_categorie c) {
        try {
            if (c.getNom() == null || c.getNom().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le nom de la catégorie est obligatoire !");
                return;
            }
            service.ajouter(c);
            System.out.println("✅ Catégorie ajoutée avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de l'ajout : " + ex.getMessage());
        }
    }

    public List<cours_categorie> recupererCategories() {
        try {
            return service.recuperer();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void modifierCategorie(cours_categorie c) {
        try {
            if (c.getId() <= 0) {
                System.out.println("❌ Erreur : ID invalide !");
                return;
            }
            service.modifier(c);
            System.out.println("✅ Catégorie modifiée avec succès !");
        } catch (SQLException ex) {
            System.out.println("❌ Erreur lors de la modification : " + ex.getMessage());
        }
    }

    public void supprimerCategorie(int id) {
        try {
            service.supprimer(id);
            System.out.println("✅ Catégorie supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public cours_categorie findById(int id) {
        try {
            return service.findById(id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return null;
        }
    }
}