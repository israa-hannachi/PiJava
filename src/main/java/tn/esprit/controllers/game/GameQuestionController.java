package tn.esprit.controllers.game;

import tn.esprit.entities.game.Game_Question;
import tn.esprit.services.game.GameQuestionService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GameQuestionController {

    private GameQuestionService questionService = new GameQuestionService();

    // ✅ Ajouter une question avec contrôle de saisie
    public void ajouterQuestion(Game_Question q) {
        try {
            // Question obligatoire
            if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le texte de la question est obligatoire !");
                return;
            }
            // Réponse correcte obligatoire
            if (q.getCorrectAnswer() == null || q.getCorrectAnswer().trim().isEmpty()) {
                System.out.println("❌ Erreur : La réponse correcte est obligatoire !");
                return;
            }
            // Au moins option1 et option2 obligatoires
            if (q.getOption1() == null || q.getOption1().trim().isEmpty()) {
                System.out.println("❌ Erreur : L'option 1 est obligatoire !");
                return;
            }
            if (q.getOption2() == null || q.getOption2().trim().isEmpty()) {
                System.out.println("❌ Erreur : L'option 2 est obligatoire !");
                return;
            }
            // Vérifier que la réponse correcte correspond à une des options
            if (!q.getCorrectAnswer().equals(q.getOption1()) &&
                    !q.getCorrectAnswer().equals(q.getOption2()) &&
                    !q.getCorrectAnswer().equals(q.getOption3()) &&
                    !q.getCorrectAnswer().equals(q.getOption4())) {
                System.out.println("❌ Erreur : La réponse correcte doit correspondre à une des options !");
                return;
            }

            questionService.ajouter(q);
            System.out.println("✅ Question ajoutée avec succès ! ID : " + q.getId());

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de l'ajout : " + e.getMessage());
        }
    }

    // ✅ Récupérer toutes les questions
    public List<Game_Question> recupererQuestions() {
        try {
            return questionService.recuperer();
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ✅ Récupérer les questions d'un jeu
    public List<Game_Question> recupererParGame(int gameId) {
        try {
            return questionService.recupererParGame(gameId);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    // ✅ Modifier une question avec contrôle de saisie
    public void modifierQuestion(Game_Question q) {
        try {
            if (q.getQuestionText() == null || q.getQuestionText().trim().isEmpty()) {
                System.out.println("❌ Erreur : Le texte de la question est obligatoire !");
                return;
            }
            if (q.getCorrectAnswer() == null || q.getCorrectAnswer().trim().isEmpty()) {
                System.out.println("❌ Erreur : La réponse correcte est obligatoire !");
                return;
            }
            if (!q.getCorrectAnswer().equals(q.getOption1()) &&
                    !q.getCorrectAnswer().equals(q.getOption2()) &&
                    !q.getCorrectAnswer().equals(q.getOption3()) &&
                    !q.getCorrectAnswer().equals(q.getOption4())) {
                System.out.println("❌ Erreur : La réponse correcte doit correspondre à une des options !");
                return;
            }

            questionService.modifier(q);
            System.out.println("✅ Question modifiée avec succès !");

        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la modification : " + e.getMessage());
        }
    }

    // ✅ Supprimer une question
    public void supprimerQuestion(int id) {
        try {
            questionService.supprimer(id);
            System.out.println("✅ Question supprimée avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    // ✅ Trouver par ID
    public Game_Question findById(int id) {
        try {
            return questionService.findById(id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return null;
        }
    }

    // ✅ Rechercher par texte
    public List<Game_Question> rechercherParTexte(String texte) {
        try {
            return questionService.rechercherParTexte(texte);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }
}