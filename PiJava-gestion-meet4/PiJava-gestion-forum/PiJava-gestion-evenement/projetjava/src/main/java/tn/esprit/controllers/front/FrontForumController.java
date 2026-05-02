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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.esprit.controllers.Back.BackCoursModuleController;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;
import tn.esprit.entities.forum.Message;
import tn.esprit.entities.users.Users;
import tn.esprit.services.forum.ServiceCategorie;
import tn.esprit.services.forum.ServiceForum;
import tn.esprit.services.forum.ServiceMessage;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class FrontForumController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCategorie;
    @FXML private VBox categoriesContainer;
    @FXML private VBox forumsContainer;
    @FXML private Button adminButton;
    @FXML private Label userForumsCount;
    @FXML private Label userMessagesCount;

    private Users currentUser;
    private final ServiceCategorie categorieService = new ServiceCategorie();
    private final ServiceForum forumService = new ServiceForum();
    private final ServiceMessage messageService = new ServiceMessage();
    private Map<String, Integer> categorieNameToId = new HashMap<>();

    // Cache pour les messages
    private static List<Message> cachedMessages = null;

    // Cache statique pour éviter les rechargements
    private static List<Categorie> cachedCategories = null;
    private static List<Forum> cachedForums = null;
    private static long lastCacheTime = 0;
    private static final long CACHE_DURATION_MS = 30000; // 30 secondes

    // Couleurs pour les cartes de catégories
    private final String[] categoryColors = {"#8b5cf6", "#22c55e", "#3b82f6", "#f59e0b", "#ef4444", "#06b6d4"};
    private final String[] categoryBgColors = {"#f3e8ff", "#dcfce7", "#dbeafe", "#fef3c7", "#fee2e2", "#cffafe"};

    // Icônes pour les catégories
    private final String[] categoryIcons = {"💻", "🌐", "📊", "🎨", "📚", "🔬", "🎮", "⚙️", "📱", "🔒", "🌐", "📈", "🔧", "💡", "📝", "🗂️", "🎯", "🏗️", "🔍", "🎓", "📡", "🖥️", "⚡", "🛡️", "🧪", "📂", "🌟", "🔑", "💼", "📞", "🤖", "🚀", "🌈", "📸", "💰"};

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            // Réinitialiser le cache pour forcer le rechargement depuis la BD
            cachedCategories = null;
            cachedForums = null;
            cachedMessages = null;
            lastCacheTime = 0;

            setupSearchListener();
            loadCategories();
            loadForums();
            setupCategorieFilter();
            loadUserStats();
        } catch (Exception e) {
            System.err.println("Erreur initialisation Forum: " + e.getMessage());
            e.printStackTrace();
            // Afficher un message d'erreur dans l'UI si possible
            if (forumsContainer != null) {
                forumsContainer.getChildren().clear();
                Label errorLabel = new Label("Erreur lors du chargement des forums. Veuillez réessayer.");
                errorLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #dc2626; -fx-padding: 40;");
                forumsContainer.getChildren().add(errorLabel);
            }
        }
    }

    public void initUser(Users user) {
        this.currentUser = user;
        if (user != null && adminButton != null) {
            boolean isAdmin = "ADMIN".equals(user.getRole());
            adminButton.setVisible(isAdmin);
            adminButton.setManaged(isAdmin);
        }
        loadUserStats();
    }

    private void setupSearchListener() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCategories(newVal);
        });
    }

    private void setupCategorieFilter() {
        filterCategorie.getItems().add("Toutes les catégories");
        for (Categorie c : getCachedCategories()) {
            filterCategorie.getItems().add(c.getTitre());
            categorieNameToId.put(c.getTitre(), c.getId());
        }
        filterCategorie.getSelectionModel().selectFirst();
        filterCategorie.setOnAction(e -> applyCategorieFilter());
    }

    private void applyCategorieFilter() {
        String selected = filterCategorie.getValue();
        if ("Toutes les catégories".equals(selected)) {
            loadForums();
        } else {
            filterForumsByCategorie(categorieNameToId.getOrDefault(selected, 0));
        }
    }

    private List<Categorie> getCachedCategories() {
        long currentTime = System.currentTimeMillis();
        if (cachedCategories == null || (currentTime - lastCacheTime) > CACHE_DURATION_MS) {
            cachedCategories = categorieService.afficher();
            lastCacheTime = currentTime;
        }
        return cachedCategories;
    }

    private List<Forum> getCachedForums() {
        long currentTime = System.currentTimeMillis();
        if (cachedForums == null || (currentTime - lastCacheTime) > CACHE_DURATION_MS) {
            cachedForums = forumService.afficher();
            lastCacheTime = currentTime;
        }
        return cachedForums;
    }

    private List<Message> getCachedMessages() {
        long currentTime = System.currentTimeMillis();
        if (cachedMessages == null || (currentTime - lastCacheTime) > CACHE_DURATION_MS) {
            cachedMessages = messageService.afficher();
            lastCacheTime = currentTime;
        }
        return cachedMessages;
    }

    private void loadCategories() {
        categoriesContainer.getChildren().clear();
        List<Categorie> categories = getCachedCategories();

        System.out.println("Nombre de catégories récupérées: " + categories.size());

        if (categories.isEmpty()) {
            Label noCategories = new Label("Aucune catégorie trouvée dans la base de données.");
            noCategories.setStyle("-fx-font-size: 14; -fx-text-fill: #dc2626; -fx-padding: 20;");
            categoriesContainer.getChildren().add(noCategories);
            return;
        }

        int colorIndex = 0;
        for (Categorie cat : categories) {
            System.out.println("Affichage catégorie: " + cat.getTitre() + " (ID: " + cat.getId() + ")");
            HBox card = createCategoryCard(cat, colorIndex % categoryColors.length);
            categoriesContainer.getChildren().add(card);
            colorIndex++;
        }

        // Forcer la mise à jour de l'affichage
        categoriesContainer.requestLayout();
    }

    private HBox createCategoryCard(Categorie cat, int colorIndex) {
        String mainColor = categoryColors[colorIndex];
        String bgColor = categoryBgColors[colorIndex];

        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-cursor: hand;");
        card.setSpacing(15);

        // Icône ronde - utiliser une icône différente selon l'ID de la catégorie
        String icon = getIconForCategory(cat);
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 28; -fx-min-width: 50; -fx-min-height: 50; -fx-alignment: center; -fx-background-color: " + mainColor + "; -fx-background-radius: 25; -fx-text-fill: white;");
        iconLabel.setAlignment(Pos.CENTER);

        // Contenu
        VBox content = new VBox(5);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Tag et titre sur la même ligne
        HBox headerRow = new HBox(10);
        headerRow.setAlignment(Pos.CENTER_LEFT);

        Label tagLabel = new Label(cat.getTitre().toUpperCase());
        tagLabel.setStyle("-fx-font-size: 10; -fx-text-fill: " + mainColor + "; -fx-font-weight: bold;");

        headerRow.getChildren().addAll(tagLabel);

        Label titleLabel = new Label(cat.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#1e293b"));

        Label descLabel = new Label(cat.getDescription() != null ? cat.getDescription() : "");
        descLabel.setFont(Font.font("System", 13));
        descLabel.setTextFill(Color.web("#475569"));
        descLabel.setWrapText(true);

        // Compter les forums dans cette catégorie
        List<Forum> allForums = getCachedForums();
        long forumCount = allForums.stream().filter(f -> f.getCategorie() != null && f.getCategorie().getId() == cat.getId()).count();
        Label countLabel = new Label(forumCount + " forums");
        countLabel.setStyle("-fx-font-size: 12; -fx-text-fill: " + mainColor + ";");

        content.getChildren().addAll(headerRow, titleLabel, descLabel, countLabel);

        // Flèche à droite
        Label arrowLabel = new Label("→");
        arrowLabel.setStyle("-fx-font-size: 24; -fx-text-fill: " + mainColor + ";");

        card.getChildren().addAll(iconLabel, content, arrowLabel);

        // Click handler - naviguer vers la page de la catégorie
        card.setOnMouseClicked(e -> openCategoryPage(cat));

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 4);"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 12; -fx-cursor: hand;"));
        return card;
    }

    private String getIconForCategory(Categorie cat) {
        // Utiliser l'icône de la BD si elle existe, sinon utiliser une icône basée sur l'ID
        if (cat.getIcone() != null && !cat.getIcone().isEmpty()) {
            return cat.getIcone();
        }
        // Sélectionner une icône basée sur l'ID (modulo pour éviter dépassement)
        int iconIndex = (cat.getId() - 1) % categoryIcons.length;
        return categoryIcons[iconIndex];
    }

    private void loadUserStats() {
        // Compter tous les forums (pas seulement ceux de l'utilisateur) pour afficher le total
        List<Forum> allForums = getCachedForums();
        long totalForumCount = allForums.size();

        if (userForumsCount != null) {
            userForumsCount.setText(String.valueOf(totalForumCount));
        }

        // Compter tous les messages
        List<Message> allMessages = getCachedMessages();
        long totalMessageCount = allMessages.size();

        if (userMessagesCount != null) {
            userMessagesCount.setText(String.valueOf(totalMessageCount));
        }
    }

    private void loadForums() {
        forumsContainer.getChildren().clear();
        List<Forum> forums = getCachedForums();

        if (forums.isEmpty()) {
            Label emptyLabel = new Label("Aucun forum disponible pour le moment.");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b; -fx-padding: 40;");
            forumsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Forum forum : forums) {
            HBox card = createForumCard(forum);
            forumsContainer.getChildren().add(card);
        }
    }

    private HBox createForumCard(Forum forum) {
        HBox card = new HBox(15);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);");
        card.setSpacing(20);

        // Category icon/indicator
        Label iconLabel = new Label(forum.getCategorie() != null && forum.getCategorie().getIcone() != null 
            ? forum.getCategorie().getIcone() : "💬");
        iconLabel.setStyle("-fx-font-size: 24; -fx-min-width: 40; -fx-alignment: center;");

        // Content
        VBox content = new VBox(8);
        content.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(content, Priority.ALWAYS);

        // Title and status
        HBox titleRow = new HBox(10);
        titleRow.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label(forum.getTitre());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setTextFill(Color.web("#0f172a"));

        // Status badge
        Label statusLabel = new Label("Actif");
        statusLabel.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-font-size: 11; -fx-padding: 3 10; -fx-background-radius: 20;");

        titleRow.getChildren().addAll(titleLabel, statusLabel);

        // Description
        Label descLabel = new Label(forum.getDescription() != null ? forum.getDescription() : "Pas de description");
        descLabel.setFont(Font.font("System", 13));
        descLabel.setTextFill(Color.web("#64748b"));
        descLabel.setWrapText(true);

        // Meta info
        HBox metaRow = new HBox(15);
        metaRow.setAlignment(Pos.CENTER_LEFT);

        Label catLabel = new Label("📁 " + (forum.getCategorie() != null ? forum.getCategorie().getTitre() : "Général"));
        catLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #14b8a6;");

        Label dateLabel = new Label("📅 " + (forum.getDateCreation() != null ? forum.getDateCreation().toString() : ""));
        dateLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");

        Label authorLabel = new Label("👤 " + (forum.getCreatedBy() != null ? forum.getCreatedBy() : "Anonyme"));
        authorLabel.setStyle("-fx-font-size: 12; -fx-text-fill: #94a3b8;");

        metaRow.getChildren().addAll(catLabel, dateLabel, authorLabel);

        content.getChildren().addAll(titleRow, descLabel, metaRow);

        // View button
        Button viewBtn = new Button("Voir →");
        viewBtn.setStyle("-fx-background-color: #14b8a6; -fx-text-fill: white; -fx-font-size: 12; -fx-padding: 8 20; -fx-background-radius: 6; -fx-cursor: hand;");
        viewBtn.setOnAction(e -> openForum(forum));

        card.getChildren().addAll(iconLabel, content, viewBtn);

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2); -fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 6, 0, 0, 1);"));

        return card;
    }

    private void filterCategories(String searchText) {
        categoriesContainer.getChildren().clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            loadCategories();
            return;
        }

        String search = searchText.toLowerCase();
        List<Categorie> categories = getCachedCategories();
        int colorIndex = 0;
        boolean found = false;

        for (Categorie cat : categories) {
            // Filter categories that START with the search letter
            if (cat.getTitre().toLowerCase().startsWith(search)) {
                HBox card = createCategoryCard(cat, colorIndex % categoryColors.length);
                categoriesContainer.getChildren().add(card);
                colorIndex++;
                found = true;
            }
        }

        if (!found) {
            Label noResultLabel = new Label("Aucune catégorie ne correspond à votre recherche.");
            noResultLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b; -fx-padding: 40;");
            categoriesContainer.getChildren().add(noResultLabel);
        }
    }

    private void filterForums(String searchText) {
        forumsContainer.getChildren().clear();
        if (searchText == null || searchText.trim().isEmpty()) {
            loadForums();
            return;
        }

        String search = searchText.toLowerCase();
        List<Forum> forums = getCachedForums();

        for (Forum forum : forums) {
            if (forum.getTitre().toLowerCase().contains(search) ||
                (forum.getDescription() != null && forum.getDescription().toLowerCase().contains(search))) {
                forumsContainer.getChildren().add(createForumCard(forum));
            }
        }

        if (forumsContainer.getChildren().isEmpty()) {
            Label noResultLabel = new Label("Aucun forum ne correspond à votre recherche.");
            noResultLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b; -fx-padding: 40;");
            forumsContainer.getChildren().add(noResultLabel);
        }
    }

    private void filterForumsByCategorie(int categorieId) {
        forumsContainer.getChildren().clear();
        List<Forum> forums = getCachedForums();

        for (Forum forum : forums) {
            if (forum.getCategorie() != null && forum.getCategorie().getId() == categorieId) {
                forumsContainer.getChildren().add(createForumCard(forum));
            }
        }

        if (forumsContainer.getChildren().isEmpty()) {
            Label emptyLabel = new Label("Aucun forum dans cette catégorie.");
            emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b; -fx-padding: 40;");
            forumsContainer.getChildren().add(emptyLabel);
        }
    }

    private void openForum(Forum forum) {
        // Navigate to forum details or messages view
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Forum");
        alert.setHeaderText(forum.getTitre());
        alert.setContentText("Ouverture du forum: " + forum.getDescription());
        alert.showAndWait();
    }

    private void openCategoryPage(Categorie categorie) {
        // Naviguer vers la page dédiée de la catégorie
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_forum_category.fxml"));
            Parent root = loader.load();

            FrontForumCategoryController controller = loader.getController();
            controller.initData(currentUser, categorie);

            Stage stage = (Stage) categoriesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation vers catégorie: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── NAVIGATION HANDLERS ─────────────────────────────────────────────────────

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
    public void handleBackOffice(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_admin.fxml"));
            Parent root = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.initAdmin(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/front_login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxml, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof FrontUserDashboardController && currentUser != null) {
                ((FrontUserDashboardController) controller).initUser(currentUser);
            } else if (controller instanceof FrontCoursCategorieController && currentUser != null) {
                ((FrontCoursCategorieController) controller).initUser(currentUser);
            } else if (controller instanceof FrontGameListController && currentUser != null) {
                ((FrontGameListController) controller).initUser(currentUser);
            } else if (controller instanceof FrontProfileController && currentUser != null) {
                ((FrontProfileController) controller).initUser(currentUser);
            } else if (controller instanceof EventFrontController && currentUser != null) {
                ((EventFrontController) controller).initUser(currentUser);
            } else if (controller instanceof FrontForumController && currentUser != null) {
                ((FrontForumController) controller).initUser(currentUser);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
