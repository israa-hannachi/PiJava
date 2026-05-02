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

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackGestionForumsController implements Initializable {

    // Table des Forums
    @FXML private TableView<Forum> forumsTable;
    @FXML private TableColumn<Forum, Integer> idColumn;
    @FXML private TableColumn<Forum, String> titreColumn;
    @FXML private TableColumn<Forum, String> createdByColumn;
    @FXML private TableColumn<Forum, Date> dateColumn;
    @FXML private TableColumn<Forum, String> etatColumn;
    @FXML private TableColumn<Forum, Void> actionsColumn;

    // Filtres
    @FXML private TextField searchField;
    @FXML private TextField searchCreatorField;
    @FXML private ComboBox<String> sortByDateCombo;
    @FXML private Label resultCountLabel;

    // Stats
    @FXML private Label totalForumsLabel;
    @FXML private Label activeForumsLabel;
    @FXML private Label inactiveForumsLabel;

    // Sidebar Submenus
    @FXML private VBox comptesSubmenu;
    @FXML private VBox coursSubmenu;
    @FXML private VBox jeuxSubmenu;
    @FXML private VBox forumSubmenu;
    @FXML private VBox eventsSubmenu;
    @FXML private VBox meetSubmenu;
    @FXML private VBox mailingSubmenu;
    @FXML private Label adminNameLabel;

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

        // Ensure forum submenu is expanded
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
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        createdByColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCreatedBy() != null ? c.getValue().getCreatedBy() : "—"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        // Format date column
        dateColumn.setCellFactory(tc -> new TableCell<Forum, Date>() {
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

        // État column with colored badges
        etatColumn.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEtat() != null ? c.getValue().getEtat() : "—"));
        etatColumn.setCellFactory(tc -> new TableCell<Forum, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(etat.toLowerCase());
                    badge.setStyle(getEtatStyle(etat));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Actions column with edit/delete buttons
        actionsColumn.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button delBtn = new Button("🗑️");
            private final HBox box = new HBox(6, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color:#3b82f6; -fx-text-fill:white; -fx-background-radius:4; -fx-padding:4 8; -fx-cursor: hand;");
                delBtn.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-background-radius:4; -fx-padding:4 8; -fx-cursor: hand;");
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

    private String getEtatStyle(String etat) {
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
        sortByDateCombo.setOnAction(e -> handleSortByDate());

        List<Categorie> categories = serviceCategorie.afficher();
        categorieCombo.setItems(FXCollections.observableArrayList(categories));
    }

    private void setupSearchListeners() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchCreatorField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadData() {
        List<Forum> list = serviceForum.afficher();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Forum> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(forumsTable.comparatorProperty());
        forumsTable.setItems(sortedData);
        updateStats(list);
        updateResultCount();
    }

    private void updateStats(List<Forum> list) {
        long total = list.size();
        long active = list.stream().filter(f -> "actif".equalsIgnoreCase(f.getEtat())).count();
        long inactive = total - active;
        totalForumsLabel.setText(String.valueOf(total));
        activeForumsLabel.setText(String.valueOf(active));
        inactiveForumsLabel.setText(String.valueOf(inactive));
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " forum(s)");
    }

    @FXML
    public void handleSearch() {
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
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String creatorSearch = searchCreatorField.getText() == null ? "" : searchCreatorField.getText().toLowerCase().trim();

        filteredData.setPredicate(forum -> {
            boolean matchSearch = search.isEmpty()
                    || forum.getTitre().toLowerCase().contains(search)
                    || (forum.getDescription() != null && forum.getDescription().toLowerCase().contains(search));

            boolean matchCreator = creatorSearch.isEmpty()
                    || (forum.getCreatedBy() != null && forum.getCreatedBy().toLowerCase().contains(creatorSearch));

            return matchSearch && matchCreator;
        });
        updateResultCount();
    }

    @FXML
    public void handleAdd() {
        errorLabel.setVisible(false);

        String titre = titreField.getText();
        String description = descriptionField.getText();
        String createdBy = createdByField.getText();
        String etat = etatCombo.getValue();
        Categorie categorie = categorieCombo.getValue();

        // Contrôle de saisie
        if (titre == null || titre.trim().isEmpty()) {
            showError("Le titre est obligatoire");
            return;
        }
        if (titre.length() > 200) {
            showError("Le titre ne doit pas dépasser 200 caractères");
            return;
        }
        if (description != null && description.length() > 1000) {
            showError("La description ne doit pas dépasser 1000 caractères");
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
        showSuccess("Forum ajouté avec succès");
    }

    @FXML
    public void handleUpdate() {
        if (selectedForum == null) {
            showError("Veuillez sélectionner un forum à modifier");
            return;
        }

        String titre = titreField.getText();
        String description = descriptionField.getText();
        String createdBy = createdByField.getText();
        String etat = etatCombo.getValue();
        Categorie categorie = categorieCombo.getValue();

        if (titre == null || titre.trim().isEmpty()) {
            showError("Le titre est obligatoire");
            return;
        }

        selectedForum.setTitre(titre.trim());
        selectedForum.setDescription(description);
        selectedForum.setCreatedBy(createdBy);
        selectedForum.setEtat(etat);
        selectedForum.setCategorie(categorie);

        serviceForum.modifier(selectedForum);

        clearFields();
        selectedForum = null;
        loadData();
        showSuccess("Forum modifié avec succès");
    }

    @FXML
    public void handleClear() {
        clearFields();
        selectedForum = null;
        errorLabel.setVisible(false);
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

    // Navigation handlers
    @FXML
    public void handleDashboard(ActionEvent event) { navigateTo("/tn/esprit/view/back_admin.fxml", event); }

    @FXML
    public void toggleComptesMenu() { toggleMenu(comptesSubmenu); }

    @FXML
    public void toggleCoursMenu() { toggleMenu(coursSubmenu); }

    @FXML
    public void toggleJeuxMenu() { toggleMenu(jeuxSubmenu); }

    @FXML
    public void toggleForumMenu() { toggleMenu(forumSubmenu); }

    @FXML
    public void toggleEventsMenu() { toggleMenu(eventsSubmenu); }

    @FXML
    public void toggleMeetMenu() { toggleMenu(meetSubmenu); }

    @FXML
    public void toggleMailingMenu() { toggleMenu(mailingSubmenu); }

    private void toggleMenu(VBox menu) {
        if (menu != null) {
            menu.setVisible(!menu.isVisible());
            menu.setManaged(!menu.isManaged());
        }
    }

    @FXML
    public void handleListeComptes(ActionEvent event) { navigateTo("/tn/esprit/view/back_admin.fxml", event); }

    @FXML
    public void handleCategories(ActionEvent event) { navigateTo("/tn/esprit/view/back_CoursCategorieList.fxml", event); }

    @FXML
    public void handleModules(ActionEvent event) { navigateTo("/tn/esprit/view/back_CoursModuleList.fxml", event); }

    @FXML
    public void handleCours(ActionEvent event) { navigateTo("/tn/esprit/view/back_CoursList.fxml", event); }

    @FXML
    public void handleBackList(ActionEvent event) { navigateTo("/tn/esprit/view/back_GameList.fxml", event); }

    @FXML
    public void handleForumsList(ActionEvent event) { /* Already on this page */ }

    @FXML
    public void handleEventsList(ActionEvent event) { navigateTo("/tn/esprit/view/back_Event.fxml", event); }

    @FXML
    public void handleEventsCalendrier(ActionEvent event) { navigateTo("/tn/esprit/view/back_Event.fxml", event); }

    @FXML
    public void handleEventsSponsors(ActionEvent event) { navigateTo("/tn/esprit/view/back_Event.fxml", event); }

    @FXML
    public void handleEventsInscriptions(ActionEvent event) { navigateTo("/tn/esprit/view/back_Event.fxml", event); }

    @FXML
    public void handleMeet(ActionEvent event) { navigateTo("/tn/esprit/view/back_admin.fxml", event); }

    @FXML
    public void handleMailing(ActionEvent event) { navigateTo("/tn/esprit/view/back_admin.fxml", event); }

    @FXML
    public void handleProfile(ActionEvent event) { navigateTo("/tn/esprit/view/back_admin.fxml", event); }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/login.fxml"));
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

            // Pass admin data if the controller supports it
            Object controller = loader.getController();
            if (controller instanceof BackGestionForumsController && currentUser != null) {
                ((BackGestionForumsController) controller).initAdmin(currentUser);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
