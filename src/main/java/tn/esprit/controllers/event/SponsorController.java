package tn.esprit.controllers.event;

import tn.esprit.entities.event.Sponsor;
import tn.esprit.services.event.SponsorService;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class SponsorController {
    private SponsorService sponsorService;

    public SponsorController() {
        sponsorService = new SponsorService();
    }

    public void ajouterSponsor(Sponsor s) {
        try {
            if (s.getNom() == null || s.getNom().trim().isEmpty()) {
                System.out.println("Erreur : Le nom du sponsor est obligatoire !");
                return;
            }
            sponsorService.ajouter(s);
            System.out.println("Sponsor ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du sponsor : " + e.getMessage());
        }
    }

    public List<Sponsor> recupererSponsors() {
        try {
            return sponsorService.recuperer();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des sponsors : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void modifierSponsor(Sponsor s) {
        try {
            sponsorService.modifier(s);
            System.out.println("Sponsor modifié avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du sponsor : " + e.getMessage());
        }
    }

    public void supprimerSponsor(int id) {
        try {
            sponsorService.supprimer(id);
            System.out.println("Sponsor supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du sponsor : " + e.getMessage());
        }
    }

    public Sponsor findById(int id) {
        try {
            return sponsorService.findById(id);
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
            return null;
        }
    }
}
