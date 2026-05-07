package tn.esprit.controllers.Back;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;
import tn.esprit.entities.users.Users;
import tn.esprit.services.forum.ServiceCategorie;
import tn.esprit.services.forum.ServiceForum;
import tn.esprit.controllers.front.AdminDashboardController;
import tn.esprit.controllers.front.UserIndexController;
import tn.esprit.controllers.event.BackEventController;
import tn.esprit.controllers.Back.BackGameListController;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackForumsGestionController implements Initializable {

    // Sidebar Submenus
    @FXML private VBox comptesSubmenu;
    @FXML private VBox coursSubmenu;
    @FXML private VBox jeuxSubmenu;
    @FXML private VBox forumSubmenu;
    @FXML private VBox eventsSubmenu;
    @FXML private VBox meetSubmenu;
    @FXML private VBox mailingSubmenu;
    @FXML private Label adminNameLabel;

    // Table des Forums
    @FXML private TableView<Forum> forumsTable;
    @FXML private TableColumn<Forum, Integer> colId;
    @FXML private TableColumn<Forum, String> colTitre;
    @FXML private TableColumn<Forum, String> colCreatedBy;
    @FXML private TableColumn<Forum, Date> colDate;
    @FXML private TableColumn<Forum, String> colEtat;
    @FXML private TableColumn<Forum, Void> colActions;

    // Filtres
    @FXML private TextField searchEtatField;
    @FXML private TextField searchCreatorField;
    @FXML private ComboBox<String> sortByDateCombo;
    @FXML private Label resultCountLabel;

    // Formulaire
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField createdByField;
    @FXML private ComboBox<String> etatCombo;
    @FXML private ComboBox<Categorie> categorieCombo;
    @FXML private Label errorLabel;

    private Users currentUser;
    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceCategorie serviceCategorie = new ServiceCategorie();
    private ObservableList<Forum> masterData;
    private FilteredList<Forum> filteredData;
    private Forum selectedForum = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupComboBoxes();
        loadData();
        setupSearchListeners();
        errorLabel.setVisible(false);

        // Expand forum submenu by default
        if (forumSubmenu != null) {
            forumSubmenu.setVisible(true);
            forumSubmenu.setManaged(true);
        }
    }

    public void initAdmin(Users user) {
        this.currentUser = user;
        if (user != null && adminNameLabel != null) {
            adminNameLabel.setText("👑 " + user.getFirstName() + " " + user.getLastName());
        }
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colCreatedBy.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCreatedBy() != null ? c.getValue().getCreatedBy() : "—"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Format date column
        colDate.setCellFactory(tc -> new TableCell<Forum, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText("—");
                } else {
                    setText(date.toString());
                }
            }
        });

        // État column with colored badges (actif=vert, inactif=rouge)
        colEtat.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEtat() != null ? c.getValue().getEtat() : "—"));
        colEtat.setCellFactory(tc -> new TableCell<Forum, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(etat.toLowerCase());
                    badge.setStyle(getEtatBadgeStyle(etat));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Actions column with edit/delete buttons (like photo - small bordered buttons)
        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button delBtn = new Button("🗑");
            private final HBox box = new HBox(5, editBtn, delBtn);

            {
                // Blue bordered edit button like photo
                editBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #3b82f6; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 3 6; -fx-cursor: hand; -fx-font-size: 12;");
                // Red bordered delete button like photo
                delBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 3 6; -fx-cursor: hand; -fx-font-size: 12;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private String getEtatBadgeStyle(String etat) {
        switch (etat.toLowerCase()) {
            case "actif":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;";
            case "inactif":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;";
            case "fermé":
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11;";
        }
    }

    private void setupComboBoxes() {
        etatCombo.setItems(FXCollections.observableArrayList("actif", "inactif", "fermé"));
        etatCombo.getSelectionModel().selectFirst();

        sortByDateCombo.setItems(FXCollections.observableArrayList("Trier par date", "Plus récent", "Plus ancien"));
        sortByDateCombo.getSelectionModel().selectFirst();

        List<Categorie> categories = serviceCategorie.afficher();
        categorieCombo.setItems(FXCollections.observableArrayList(categories));
    }

    private void setupSearchListeners() {
        searchEtatField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchCreatorField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Listener pour le tri par date
        sortByDateCombo.setOnAction(e -> handleSortByDate());
    }

    private void loadData() {
        List<Forum> list = serviceForum.afficher();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Forum> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(forumsTable.comparatorProperty());
        forumsTable.setItems(sortedData);
        updateResultCount();
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " forum(s)");
    }

    @FXML
    public void handleAppliquer() {
        applyFilters();
    }

    @FXML
    public void handleSortByDate() {
        String sort = sortByDateCombo.getValue();
        if (sort == null) return;

        if ("Plus récent".equals(sort)) {
            masterData.sort(Comparator.comparing(Forum::getDateCreation, Comparator.nullsLast(Comparator.reverseOrder())));
        } else if ("Plus ancien".equals(sort)) {
            masterData.sort(Comparator.comparing(Forum::getDateCreation, Comparator.nullsLast(Comparator.naturalOrder())));
        }
        forumsTable.refresh();
    }

    private void applyFilters() {
        String etatSearch = searchEtatField.getText() == null ? "" : searchEtatField.getText().toLowerCase().trim();
        String creatorSearch = searchCreatorField.getText() == null ? "" : searchCreatorField.getText().toLowerCase().trim();

        filteredData.setPredicate(forum -> {
            boolean matchEtat = etatSearch.isEmpty()
                    || (forum.getEtat() != null && forum.getEtat().toLowerCase().contains(etatSearch));

            boolean matchCreator = creatorSearch.isEmpty()
                    || (forum.getCreatedBy() != null && forum.getCreatedBy().toLowerCase().contains(creatorSearch));

            return matchEtat && matchCreator;
        });
        updateResultCount();
    }

    @FXML
    public void handleNouveauForum() {
        errorLabel.setVisible(false);

        String titre = titreField.getText();
        String description = descriptionField.getText();
        String createdBy = createdByField.getText();
        String etat = etatCombo.getValue();
        Categorie categorie = categorieCombo.getValue();

        if (titre == null || titre.trim().isEmpty()) {
            showError("Le titre est obligatoire");
            return;
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            showError("Le créateur est obligatoire");
            return;
        }
        if (categorie == null) {
            showError("Veuillez sélectionner une catégorie");
            return;
        }

        Forum forum = new Forum(
                titre.trim(),
                description,
                new Date(System.currentTimeMillis()),
                etat,
                createdBy.trim(),
                categorie
        );
        serviceForum.ajouter(forum);

        clearFields();
        loadData();
        showSuccess("Forum créé avec succès");
    }

    @FXML
    public void handleAjouterCategorie() {
        // Navigation vers la gestion des catégories
        showInfo("Redirection vers Ajouter une catégorie...");
    }

    @FXML
    public void handleStatistiques() {
        showInfo("Statistiques des forums");
    }

    @FXML
    public void handleClustering() {
        showInfo("Clustering des discussions");
    }

    @FXML
    public void handleGoToFront() {
        showInfo("Navigation vers Front Office...");
    }

    private void handleEdit(Forum forum) {
        selectedForum = forum;
        titreField.setText(forum.getTitre());
        descriptionField.setText(forum.getDescription());
        createdByField.setText(forum.getCreatedBy());
        etatCombo.setValue(forum.getEtat());
        categorieCombo.setValue(forum.getCategorie());
    }

    private void handleDelete(Forum forum) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le forum");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + forum.getTitre() + "\" ?\n\n⚠️ Tous les messages associés seront aussi supprimés.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceForum.supprimer(forum.getId());
            loadData();
            showSuccess("Forum supprimé avec succès");
        }
    }

    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        createdByField.clear();
        etatCombo.getSelectionModel().selectFirst();
        categorieCombo.setValue(null);
        selectedForum = null;
    }

    private void showError(String msg) {
        errorLabel.setText("❌ " + msg);
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
    }

    private void showSuccess(String msg) {
        errorLabel.setText("✅ " + msg);
        errorLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    // ─── SIDEBAR TOGGLE HANDLERS ────────────────────────────────────────────────

    @FXML public void toggleComptesMenu(ActionEvent event) { toggleMenu(comptesSubmenu); }
    @FXML public void toggleCoursMenu(ActionEvent event)   { toggleMenu(coursSubmenu); }
    @FXML public void toggleJeuxMenu(ActionEvent event)    { toggleMenu(jeuxSubmenu); }
    @FXML public void toggleForumMenu(ActionEvent event)   { toggleMenu(forumSubmenu); }
    @FXML public void toggleEventsMenu(ActionEvent event)  { toggleMenu(eventsSubmenu); }
    @FXML public void toggleMeetMenu(ActionEvent event)    { toggleMenu(meetSubmenu); }
    @FXML public void toggleMailingMenu(ActionEvent event) { toggleMenu(mailingSubmenu); }

    private void toggleMenu(VBox submenu) {
        if (submenu == null) return;
        boolean showing = submenu.isVisible();
        submenu.setVisible(!showing);
        submenu.setManaged(!showing);
    }

    // ─── NAVIGATION HANDLERS ─────────────────────────────────────────────────────

    @FXML public void handleDashboard(ActionEvent event) { navigateTo("/tn/esprit/view/back_admin.fxml", event, null, null); }
    @FXML public void handleListeComptes(ActionEvent event) { navigateTo("/tn/esprit/view/user_index.fxml", event, UserIndexController.class, (ctrl) -> ctrl.initAdmin(currentUser)); }
    @FXML public void handleCategories(ActionEvent event) { navigateSimple("/tn/esprit/view/back_CoursCategorieList.fxml", event); }
    @FXML public void handleModules(ActionEvent event)    { navigateSimple("/tn/esprit/view/back_CoursModuleList.fxml", event); }
    @FXML public void handleCours(ActionEvent event)      { navigateSimple("/tn/esprit/view/back_CoursList.fxml", event); }
    @FXML public void handleBackList(ActionEvent event)   { navigateTo("/tn/esprit/view/back_GameList.fxml", event, BackGameListController.class, (ctrl) -> ctrl.initAdmin(currentUser)); }
    @FXML public void handleForums(ActionEvent event)     { /* Already here */ }
    @FXML public void handleEventsList(ActionEvent event) { navigateTo("/tn/esprit/view/back_Event.fxml", event, BackEventController.class, (ctrl) -> { ctrl.initAdmin(currentUser); ctrl.selectTab(0); }); }
    @FXML public void handleEventsCalendrier(ActionEvent event) { handleEventsList(event); }
    @FXML public void handleEventsSponsors(ActionEvent event)   { navigateTo("/tn/esprit/view/back_Event.fxml", event, BackEventController.class, (ctrl) -> { ctrl.initAdmin(currentUser); ctrl.selectTab(2); }); }
    @FXML public void handleEventsInscriptions(ActionEvent event) { navigateTo("/tn/esprit/view/back_Event.fxml", event, BackEventController.class, (ctrl) -> { ctrl.initAdmin(currentUser); ctrl.selectTab(1); }); }
    @FXML public void handleProfile(ActionEvent event)   { showInfo("Profile"); }
    @FXML public void handleLogout(ActionEvent event)    { navigateSimple("/tn/esprit/view/front_login.fxml", event); }

    private <T> void navigateTo(String fxmlPath, ActionEvent event, Class<T> controllerClass, ControllerInit<T> init) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            if (controllerClass != null && init != null) {
                T controller = loader.getController();
                init.init(controller);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
        }
    }

    private void navigateSimple(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation simple: " + e.getMessage());
        }
    }

    // ─── PAGINATION HANDLERS ─────────────────────────────────────────────────────

    private int currentPage = 1;
    private final int itemsPerPage = 10;

    @FXML
    public void handlePrevPage(ActionEvent event) {
        if (currentPage > 1) {
            currentPage--;
            applyPagination();
        }
    }

    @FXML
    public void handleNextPage(ActionEvent event) {
        int totalPages = (int) Math.ceil((double) filteredData.size() / itemsPerPage);
        if (currentPage < totalPages) {
            currentPage++;
            applyPagination();
        }
    }

    @FXML
    public void handlePage1(ActionEvent event) {
        currentPage = 1;
        applyPagination();
    }

    @FXML
    public void handlePage2(ActionEvent event) {
        currentPage = 2;
        applyPagination();
    }

    private void applyPagination() {
        int totalItems = filteredData.size();
        int totalPages = (int) Math.ceil((double) totalItems / itemsPerPage);

        int fromIndex = (currentPage - 1) * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, totalItems);

        if (fromIndex < totalItems) {
            ObservableList<Forum> pageItems = FXCollections.observableArrayList(
                filteredData.subList(fromIndex, toIndex)
            );
            forumsTable.setItems(pageItems);
        }

        updatePaginationButtons(totalPages);
    }

    private void updatePaginationButtons(int totalPages) {
        // Update button styles based on current page
        // This is a simplified version - you can enhance it
    }

    @FunctionalInterface
    public interface ControllerInit<T> {
        void init(T controller);
    }
}
