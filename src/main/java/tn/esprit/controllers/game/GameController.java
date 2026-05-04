package tn.esprit.controllers.game;

import tn.esprit.entities.game.Game;   // adapte à ton chemin exact
import tn.esprit.services.game.GameService;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class GameController {
    private GameService gameService;

    public GameController() {
        gameService = new GameService();
    }

    // Ajouter un jeu
    public void ajouterGame(Game g) {
        try {
            // Vérifier que le titre n'est pas vide
            if (g.getTitre() == null || g.getTitre().trim().isEmpty()) {
                System.out.println("Erreur : Le titre est obligatoire !");
                return;
            }

            // Vérifier que le score maximum est positif
            if (g.getScoreMax() <= 0) {
                System.out.println("Erreur : Le score maximum doit être positif !");
                return;
            }

            // Vérifier que la durée est correcte
            if (g.getDuration() < 0) {
                System.out.println("Erreur : La durée doit être un nombre positif !");
                return;
            }
            if (g.getDuration() > 3600) {
                System.out.println("Erreur : La durée ne peut pas dépasser 3600 secondes (1 heure) !");
                return;
            }

            // Vérifier que le nombre de tentatives est entre 0 et 3
            if (g.getAttemptNumber() < 0 || g.getAttemptNumber() > 3) {
                System.out.println("Erreur : Le nombre de tentatives doit être entre 0 et 3 !");
                return;
            }

            // ✅ Si tout est correct → appel du service
            gameService.ajouter(g);
            System.out.println("Game ajouté avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de l'ajout du jeu : " + e.getMessage());
        }
    }


    // Récupérer tous les jeux
    public List<Game> recupererGames() {
        try {
            return gameService.recuperer();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des jeux : " + e.getMessage());
            return null;
        }
    }

    // Supprimer un jeu
    public void supprimerGame(int id) {
        try {
            gameService.supprimer(id);
            System.out.println("Game supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression du jeu : " + e.getMessage());
        }
    }
    // Trouver un jeu par ID
    public Game findById(int id) {
        try {
            return gameService.findById(id);
        } catch (SQLException e) {
            System.out.println("Erreur lors de la recherche du jeu : " + e.getMessage());
            return null;
        }
    }

    // Modifier un jeu
    public void modifierGame(Game g) {
        try {
            // Contrôle de saisie avant modification
            if (g.getTitre() == null || g.getTitre().trim().isEmpty()) {
                System.out.println("Erreur : Le titre est obligatoire !");
                return;
            }
            if (g.getScoreMax() <= 0) {
                System.out.println("Erreur : Le score maximum doit être positif !");
                return;
            }
            if (g.getDuration() < 0 || g.getDuration() > 3600) {
                System.out.println("Erreur : La durée doit être entre 0 et 3600 secondes !");
                return;
            }
            if (g.getAttemptNumber() < 0 || g.getAttemptNumber() > 3) {
                System.out.println("Erreur : Le nombre de tentatives doit être entre 0 et 3 !");
                return;
            }

            gameService.modifier(g);
            System.out.println("Game modifié avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la modification du jeu : " + e.getMessage());
        }
    }
    public List<Game> rechercherParTitre(String titre) {
        try {
            return gameService.rechercherParTitre(titre);
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }

}
