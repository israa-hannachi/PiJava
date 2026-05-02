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
import tn.esprit.services.forum.ServiceMessage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class FrontForumMessagesController implements Initializable {

    @FXML private Label breadcrumbCategory;
    @FXML private Label breadcrumbForum;
    @FXML private VBox forumHeader;
    @FXML private Label forumIcon;
    @FXML private Label forumTitle;
    @FXML private Label forumDescription;
    @FXML private TextField messageTitleField;
    @FXML private TextArea messageContentField;
    @FXML private Label messagesCount;
    @FXML private VBox messagesContainer;
    @FXML private HBox publishButtonContainer;

    private Users currentUser;
    private Forum currentForum;
    private Categorie currentCategory;
    private final ServiceMessage messageService = new ServiceMessage();

    // Couleurs pour les différentes catégories
    private final String[] categoryColors = {"#8b5cf6", "#22c55e", "#3b82f6", "#f59e0b", "#ef4444", "#06b6d4"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // L'initialisation se fait dans initData
    }

    public void initData(Users user, Categorie categorie, Forum forum) {
        this.currentUser = user;
        this.currentCategory = categorie;
        this.currentForum = forum;
        // Créer le bouton Publier ici pour s'assurer que le conteneur est chargé
        createPublishButton();
        loadForumData();
    }

    private void createPublishButton() {
        if (publishButtonContainer != null) {
            publishButtonContainer.getChildren().clear();
            publishButtonContainer.setStyle("-fx-padding: 10 0 0 0;");

            Button publishBtn = new Button("Publier");
            publishBtn.setPrefHeight(40);
            publishBtn.setPrefWidth(120);
            publishBtn.setStyle("-fx-background-color: #0FB5A9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 25;");
            publishBtn.setOnAction(e -> handlePublishMessage());

            publishButtonContainer.getChildren().add(publishBtn);
            System.out.println("Bouton Publier créé et ajouté au conteneur");
        } else {
            System.err.println("ERREUR: publishButtonContainer est null!");
        }
    }

    private void loadForumData() {
        if (currentForum == null) return;

        // Mettre à jour le breadcrumb
        breadcrumbCategory.setText(currentCategory != null ? currentCategory.getTitre() : "Catégorie");
        breadcrumbForum.setText(currentForum.getTitre());

        // Mettre à jour le header
        forumIcon.setText(currentCategory != null && currentCategory.getIcone() != null ? currentCategory.getIcone() : "📁");
        forumTitle.setText(currentForum.getTitre());
        forumDescription.setText(currentForum.getDescription() != null ? currentForum.getDescription() : "");

        // Changer la couleur du header en fonction de la catégorie
        if (currentCategory != null) {
            int colorIndex = (currentCategory.getId() - 1) % categoryColors.length;
            String mainColor = categoryColors[colorIndex];
            String gradientEnd = colorIndex == 0 ? "#ec4899" :
                                colorIndex == 1 ? "#10b981" :
                                colorIndex == 2 ? "#60a5fa" :
                                colorIndex == 3 ? "#fbbf24" :
                                colorIndex == 4 ? "#f87171" :
                                "#22d3ee";
            forumHeader.setStyle("-fx-background-color: linear-gradient(to right, " + mainColor + ", " + gradientEnd + "); -fx-background-radius: 12; -fx-padding: 25;");
        }

        // Charger les messages
        loadMessages();
    }

    private void loadMessages() {
        messagesContainer.getChildren().clear();
        
        if (currentForum == null) return;

        List<Message> messages = messageService.getMessagesByForum(currentForum.getId());
        messagesCount.setText("(" + messages.size() + ")");

        for (Message message : messages) {
            VBox messageCard = createMessageCard(message);
            messagesContainer.getChildren().add(messageCard);
        }

        if (messages.isEmpty()) {
            Label emptyLabel = new Label("Aucun message dans ce forum. Soyez le premier à poster !");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b; -fx-padding: 20;");
            messagesContainer.getChildren().add(emptyLabel);
        }
    }

    private VBox createMessageCard(Message message) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10;");

        // Header avec auteur et date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label("👤 " + (message.getCreatedBy() != null ? message.getCreatedBy() : "Anonyme"));
        authorLabel.setStyle("-fx-font-size: 13; -fx-font-weight: bold; -fx-text-fill: #1e293b;");

        Label dateLabel = new Label("📅 " + (message.getDatePublication() != null ? message.getDatePublication().toString() : ""));
        dateLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #94a3b8;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge statut
        Label statusLabel = new Label(message.getEtat() != null ? message.getEtat() : "actif");
        statusLabel.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 10; -fx-padding: 2 10; -fx-background-radius: 10;");

        header.getChildren().addAll(authorLabel, dateLabel, spacer, statusLabel);

        // Contenu du message
        Label contentLabel = new Label(message.getContenu() != null ? message.getContenu() : "");
        contentLabel.setWrapText(true);
        contentLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #374151;");

        // Actions (J'aime, Je n'aime pas, Éditer, Supprimer)
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setStyle("-fx-padding: 10 0 0 0; -fx-border-color: #f3f4f6; -fx-border-width: 1 0 0 0;");

        // Bouton J'aime - vert
        Button likeBtn = new Button("♥ J'aime (0)");
        likeBtn.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 12;");
        likeBtn.setOnAction(e -> handleLikeMessage(message));

        // Bouton Je n'aime pas - rouge
        Button dislikeBtn = new Button("♥ Je n'aime pas (0)");
        dislikeBtn.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 12;");
        dislikeBtn.setOnAction(e -> handleDislikeMessage(message));

        // Bouton Éditer - bleu
        Button editBtn = new Button("✏ Éditer");
        editBtn.setStyle("-fx-background-color: #dbeafe; -fx-text-fill: #1e40af; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 12;");
        editBtn.setOnAction(e -> handleEditMessage(message));

        // Bouton Supprimer - jaune/orange
        Button deleteBtn = new Button("🗑 Supprimer");
        deleteBtn.setStyle("-fx-background-color: #fef3c7; -fx-text-fill: #92400e; -fx-font-size: 12; -fx-cursor: hand; -fx-background-radius: 20; -fx-padding: 5 12;");
        deleteBtn.setOnAction(e -> handleDeleteMessage(message));

        actions.getChildren().addAll(likeBtn, dislikeBtn, editBtn, deleteBtn);

        card.getChildren().addAll(header, contentLabel, actions);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #0FB5A9; -fx-border-radius: 10; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 6, 0, 0, 2);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #e5e7eb; -fx-border-radius: 10;"));

        return card;
    }

    @FXML
    public void handlePublishMessage() {
        String title = messageTitleField.getText();
        String content = messageContentField.getText();

        if (content == null || content.trim().isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Champ requis");
            alert.setHeaderText("Veuillez saisir un message");
            alert.showAndWait();
            return;
        }

        // Créer le nouveau message
        Message newMessage = new Message();
        newMessage.setContenu(content);
        newMessage.setCreatedBy(currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Anonyme");
        newMessage.setForum(currentForum);
        newMessage.setEtat("actif");

        messageService.ajouter(newMessage);

        // Vider les champs
        messageTitleField.clear();
        messageContentField.clear();

        // Recharger les messages
        loadMessages();
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

    @FXML
    public void handleBackToCategory(ActionEvent event) {
        // Retourner à la page de la catégorie
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_forum_category.fxml"));
            Parent root = loader.load();

            FrontForumCategoryController controller = loader.getController();
            controller.initData(currentUser, currentCategory);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
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

    // ─── MESSAGE ACTION HANDLERS ─────────────────────────────────────────────────

    private void handleLikeMessage(Message message) {
        // TODO: Implémenter la logique de like avec persistance en BD
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Like");
        alert.setHeaderText("Message aimé !");
        alert.setContentText("Vous avez aimé le message de " + (message.getCreatedBy() != null ? message.getCreatedBy() : "Anonyme"));
        alert.showAndWait();
    }

    private void handleDislikeMessage(Message message) {
        // TODO: Implémenter la logique de dislike avec persistance en BD
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Dislike");
        alert.setHeaderText("Message non aimé");
        alert.setContentText("Vous n'avez pas aimé le message de " + (message.getCreatedBy() != null ? message.getCreatedBy() : "Anonyme"));
        alert.showAndWait();
    }

    private void handleEditMessage(Message message) {
        // Vérifier si l'utilisateur est l'auteur du message ou un admin
        String currentUserName = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "";
        boolean isAuthor = message.getCreatedBy() != null && message.getCreatedBy().equals(currentUserName);
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole());

        if (!isAuthor && !isAdmin) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Permission refusée");
            alert.setHeaderText("Vous ne pouvez pas modifier ce message");
            alert.setContentText("Seul l'auteur du message ou un administrateur peut l'éditer.");
            alert.showAndWait();
            return;
        }

        // Créer une boîte de dialogue pour l'édition
        TextInputDialog dialog = new TextInputDialog(message.getContenu());
        dialog.setTitle("Éditer le message");
        dialog.setHeaderText("Modifier le message");
        dialog.setContentText("Nouveau contenu :");

        dialog.showAndWait().ifPresent(newContent -> {
            if (newContent.trim().isEmpty()) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Le message ne peut pas être vide");
                error.showAndWait();
                return;
            }
            message.setContenu(newContent);
            messageService.modifier(message);
            loadMessages(); // Recharger pour afficher les changements
        });
    }

    private void handleDeleteMessage(Message message) {
        // Vérifier si l'utilisateur est l'auteur du message ou un admin
        String currentUserName = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "";
        boolean isAuthor = message.getCreatedBy() != null && message.getCreatedBy().equals(currentUserName);
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole());

        if (!isAuthor && !isAdmin) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Permission refusée");
            alert.setHeaderText("Vous ne pouvez pas supprimer ce message");
            alert.setContentText("Seul l'auteur du message ou un administrateur peut le supprimer.");
            alert.showAndWait();
            return;
        }

        // Confirmation de suppression
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le message ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce message ? Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                messageService.supprimer(message.getId());
                loadMessages(); // Recharger pour mettre à jour l'affichage
            }
        });
    }
}
