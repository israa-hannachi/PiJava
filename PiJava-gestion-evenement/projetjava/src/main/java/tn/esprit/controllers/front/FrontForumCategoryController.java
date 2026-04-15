package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;
import tn.esprit.entities.forum.Message;
import tn.esprit.entities.users.Users;
import tn.esprit.services.forum.ServiceForum;
import tn.esprit.services.forum.ServiceMessage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class FrontForumCategoryController implements Initializable {

    @FXML private Label breadcrumbCategory;
    @FXML private VBox categoryHeader;
    @FXML private Label categoryIcon;
    @FXML private Label categoryTitle;
    @FXML private Label categoryDescription;
    @FXML private Label forumCountBadge;
    @FXML private Label messageCountBadge;
    @FXML private VBox forumsListContainer;

    private Users currentUser;
    private Categorie currentCategory;
    private final ServiceForum forumService = new ServiceForum();
    private final ServiceMessage messageService = new ServiceMessage();

    // Couleurs pour les différentes catégories (correspondent à FrontForumController)
    private final String[] categoryColors = {"#8b5cf6", "#22c55e", "#3b82f6", "#f59e0b", "#ef4444", "#06b6d4"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // L'initialisation se fait dans initData
    }

    public void initData(Users user, Categorie categorie) {
        this.currentUser = user;
        this.currentCategory = categorie;
        loadCategoryData();
    }

    private void loadCategoryData() {
        if (currentCategory == null) return;

        // Mettre à jour le breadcrumb
        breadcrumbCategory.setText(currentCategory.getTitre());

        // Mettre à jour le header
        categoryIcon.setText(currentCategory.getIcone() != null ? currentCategory.getIcone() : "💬");
        categoryTitle.setText(currentCategory.getTitre());
        categoryDescription.setText(currentCategory.getDescription() != null ? currentCategory.getDescription() : "");

        // Changer la couleur du header en fonction de l'ID de la catégorie
        int colorIndex = (currentCategory.getId() - 1) % categoryColors.length;
        String mainColor = categoryColors[colorIndex];
        String gradientEnd = colorIndex == 0 ? "#ec4899" : // violet -> pink
                            colorIndex == 1 ? "#10b981" : // vert
                            colorIndex == 2 ? "#60a5fa" : // bleu
                            colorIndex == 3 ? "#fbbf24" : // orange
                            colorIndex == 4 ? "#f87171" : // rouge
                            "#22d3ee"; // cyan
        categoryHeader.setStyle("-fx-background-color: linear-gradient(to right, " + mainColor + ", " + gradientEnd + "); -fx-background-radius: 12; -fx-padding: 25;");

        // Récupérer les forums de cette catégorie
        List<Forum> allForums = forumService.afficher();
        List<Forum> categoryForums = allForums.stream()
            .filter(f -> f.getCategorie() != null && f.getCategorie().getId() == currentCategory.getId())
            .collect(Collectors.toList());

        // Compter les forums
        forumCountBadge.setText(categoryForums.size() + " forum" + (categoryForums.size() > 1 ? "s" : ""));

        // Compter les messages dans ces forums
        List<Message> allMessages = messageService.afficher();
        long messageCount = allMessages.stream()
            .filter(m -> m.getForum() != null && categoryForums.stream().anyMatch(f -> f.getId() == m.getForum().getId()))
            .count();
        messageCountBadge.setText(messageCount + " message" + (messageCount > 1 ? "s" : ""));

        // Afficher les forums
        forumsListContainer.getChildren().clear();
        for (Forum forum : categoryForums) {
            VBox forumCard = createForumCard(forum, mainColor);
            forumsListContainer.getChildren().add(forumCard);
        }

        if (categoryForums.isEmpty()) {
            Label emptyLabel = new Label("Aucun forum dans cette catégorie.");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b; -fx-padding: 20;");
            forumsListContainer.getChildren().add(emptyLabel);
        }
    }

    private VBox createForumCard(Forum forum, String categoryColor) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10;");

        // Titre et bouton Voir sur la même ligne
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label iconLabel = new Label("💬");
        iconLabel.setStyle("-fx-font-size: 18;");

        VBox titleBox = new VBox(3);
        HBox.setHgrow(titleBox, Priority.ALWAYS);

        Label titleLabel = new Label(forum.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#1e293b"));

        Label descLabel = new Label(forum.getDescription() != null ? forum.getDescription() : "");
        descLabel.setFont(Font.font("System", 13));
        descLabel.setTextFill(Color.web("#64748b"));
        descLabel.setWrapText(true);

        titleBox.getChildren().addAll(titleLabel, descLabel);

        Button viewBtn = new Button("Voir →");
        viewBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + categoryColor + "; -fx-font-weight: bold; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> openForum(forum));

        header.getChildren().addAll(iconLabel, titleBox, viewBtn);

        // Métadonnées (date, auteur, statut)
        HBox metaBox = new HBox(15);
        metaBox.setAlignment(Pos.CENTER_LEFT);

        Label dateLabel = new Label("📅 " + (forum.getDateCreation() != null ? forum.getDateCreation().toString() : ""));
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");

        Label authorLabel = new Label("👤 " + (forum.getCreatedBy() != null ? forum.getCreatedBy() : "Anonyme"));
        authorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");

        // Statut actif/inactif
        Label statusLabel = new Label(forum.getEtat() != null && forum.getEtat().equalsIgnoreCase("actif") ? "actif" : "inactif");
        statusLabel.setStyle("-fx-background-color: " + (forum.getEtat() != null && forum.getEtat().equalsIgnoreCase("actif") ? "#dcfce7" : "#fee2e2") + "; -fx-text-fill: " + (forum.getEtat() != null && forum.getEtat().equalsIgnoreCase("actif") ? "#166534" : "#991b1b") + "; -fx-font-size: 11; -fx-padding: 2 10; -fx-background-radius: 10;");

        // Nombre de messages
        List<Message> forumMessages = messageService.getMessagesByForum(forum.getId());
        Label msgCountLabel = new Label(forumMessages.size() + " messages");
        msgCountLabel.setStyle("-fx-font-size: 11; -fx-text-fill: " + categoryColor + "; -fx-background-color: " + categoryColor + "20; -fx-padding: 2 10; -fx-background-radius: 10;");

        metaBox.getChildren().addAll(dateLabel, authorLabel, statusLabel, msgCountLabel);

        card.getChildren().addAll(header, metaBox);

        // Click handler - rendre toute la carte cliquable
        card.setOnMouseClicked(e -> openForum(forum));

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: " + categoryColor + "; -fx-border-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 8, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10;"));

        return card;
    }

    private void openForum(Forum forum) {
        // Naviguer vers la page des messages du forum
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_forum_messages.fxml"));
            Parent root = loader.load();

            FrontForumMessagesController controller = loader.getController();
            controller.initData(currentUser, currentCategory, forum);

            Stage stage = (Stage) forumsListContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation vers messages: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── NAVIGATION HANDLERS ───────────────────────────────────────────────────

    @FXML
    public void handleDashboard(ActionEvent event) {
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
        navigateTo("/tn/esprit/view/front_GameList.fxml", event);
    }

    @FXML
    public void handleEvents(ActionEvent event) {
        navigateTo("/tn/esprit/view/frontEvent.fxml", event);
    }

    @FXML
    public void handleBackToForums(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_forum.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof FrontForumController && currentUser != null) {
                ((FrontForumController) controller).initUser(currentUser);
            } else if (controller instanceof FrontUserDashboardController && currentUser != null) {
                ((FrontUserDashboardController) controller).initUser(currentUser);
            } else if (controller instanceof FrontProfileController && currentUser != null) {
                ((FrontProfileController) controller).initUser(currentUser);
            } else if (controller instanceof FrontCoursCategorieController && currentUser != null) {
                ((FrontCoursCategorieController) controller).initUser(currentUser);
            } else if (controller instanceof FrontGameListController && currentUser != null) {
                ((FrontGameListController) controller).initUser(currentUser);
            } else if (controller instanceof EventFrontController && currentUser != null) {
                ((EventFrontController) controller).initUser(currentUser);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
