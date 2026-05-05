package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import tn.esprit.entities.users.Users;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import tn.esprit.entities.game.Game;
import tn.esprit.services.game.GameService;

import javafx.scene.control.Alert;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class FrontGameListController {
    @FXML private FlowPane gameListContainer;
    @FXML private javafx.scene.control.TextField searchField;
    @FXML private ImageView profileImageView;

    private Users currentUser;
    private GameService gameService = new GameService();

    @FXML
    public void initialize() {
        // UI setup only, data loading moved to initUser
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            loadGames(newVal);
        });
    }

    private void loadGames() {
        loadGames("");
    }

    private void loadGames(String filter) {
        gameListContainer.getChildren().clear();
        try {
            List<Game> games = filter.isEmpty() ? gameService.recuperer() : gameService.rechercherParTitre(filter);
            for (Game g : games) {
                VBox card = new VBox(8);
                card.setPrefWidth(250); // largeur fixe pour la grille
                card.setStyle("-fx-background-color:#ffffff; -fx-background-radius:12; -fx-padding:16; -fx-border-color:#e5e7eb;");

                // Emoji selon type
                String emoji = "🎮";
                if ("qcm".equalsIgnoreCase(g.getType())) emoji = "🧠";
                else if ("math".equalsIgnoreCase(g.getType())) emoji = "🔢";
                else if ("vraie ou faux".equalsIgnoreCase(g.getType())) emoji = "✅";

                Label icon = new Label(emoji);
                icon.setStyle("-fx-font-size:32px; -fx-font-family:'Segoe UI Emoji';");

                Label title = new Label(g.getTitre());
                title.setStyle("-fx-font-size:18px; -fx-font-weight:700;");

                Label info = new Label("Type: " + g.getType() + " | Niveau: " + g.getNiveau());
                info.setStyle("-fx-text-fill:#64748b;");

                Label attempts = new Label("Tentatives: " + g.getAttemptNumber());
                attempts.setStyle("-fx-text-fill:#64748b;");

                // Bouton Jouer
                Button playButton = new Button("▶ Jouer");
                playButton.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-font-weight:700; -fx-background-radius:8; -fx-padding:6 12;");
                playButton.setOnAction(e -> openGamePlay(g)); // 👉 lien vers gameplay

                HBox footer = new HBox(10, attempts, playButton);
                footer.setStyle("-fx-padding:8 0 0 0; -fx-border-color:#e5e7eb; -fx-border-width:1 0 0 0;");

                card.getChildren().addAll(icon, title, info, footer);
                gameListContainer.getChildren().add(card);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 👉 Méthode pour ouvrir front_GamePlay.fxml
    private void openGamePlay(Game game) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_GamePlay.fxml"));
            Parent root = loader.load();

            // Récupérer le contrôleur du GamePlay
            FrontGamePlayController controller = loader.getController();
            controller.initGame(game); // passer le jeu sélectionné

            // Changer la scène
            Stage stage = (Stage) gameListContainer.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void initUser(Users user) {
        this.currentUser = user;
        try {
            if (user != null && profileImageView != null) {
                Image profile = tn.esprit.tools.UserAvatarUtils.resolveUserImage(user.getProfilePicture(), getClass());
                if (profile != null) {
                    profileImageView.setImage(profile);
                    applyCircularProfileClip();
                }
            }
            loadGames();
        } catch (Exception e) {
            System.err.println("Error in Games.initUser: " + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur d'initialisation Jeux: " + e.toString()).show();
        }
    }

    private void applyCircularProfileClip() {
        if (profileImageView == null) return;
        double radius = 20.0;
        javafx.scene.shape.Circle clip = new javafx.scene.shape.Circle(20.0, 20.0, radius);
        profileImageView.setClip(clip);
    }

    // ─── NAVIGATION HANDLERS ───────────────────────────────────────────────────

    @FXML
    public void handleHome(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_user_dashboard.fxml", event);
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_profile.fxml", event);
    }

    @FXML
    public void handleCours(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_CoursCategories.fxml", event);
    }

    @FXML
    public void handleGameList(ActionEvent event) {
        loadGames(); // Already on list, refresh
    }

    @FXML
    public void handleEvents(ActionEvent event) {
        navigateTo("/tn/esprit/view/frontEvent.fxml", event);
    }

    @FXML
    public void handleForums(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_forum.fxml", event);
    }

    @FXML
    public void handleMeets(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_MeetList.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof FrontUserDashboardController) {
                ((FrontUserDashboardController) controller).initUser(currentUser);
            } else if (controller instanceof FrontCoursCategorieController) {
                ((FrontCoursCategorieController) controller).initUser(currentUser);
            } else if (controller instanceof EventFrontController) {
                ((EventFrontController) controller).initUser(currentUser);
            } else if (controller instanceof FrontProfileController) {
                ((FrontProfileController) controller).initUser(currentUser);
            } else if (controller instanceof FrontForumController) {
                ((FrontForumController) controller).initUser(currentUser);
            } else if (controller instanceof FrontMeetListController) {
                ((FrontMeetListController) controller).initUser(currentUser);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (Exception e) {
            System.err.println("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
