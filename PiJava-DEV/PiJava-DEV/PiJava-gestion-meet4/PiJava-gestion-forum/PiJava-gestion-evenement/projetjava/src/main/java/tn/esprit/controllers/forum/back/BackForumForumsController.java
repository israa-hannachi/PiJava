package tn.esprit.controllers.forum.back;

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
import javafx.stage.Stage;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;
import tn.esprit.services.forum.ServiceCategorie;
import tn.esprit.services.forum.ServiceForum;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackForumForumsController implements Initializable {

    @FXML private TableView<Forum> forumTable;
    @FXML private TableColumn<Forum, Integer> colId;
    @FXML private TableColumn<Forum, String> colTitre;
    @FXML private TableColumn<Forum, String> colDescription;
    @FXML private TableColumn<Forum, String> colCategorie;
    @FXML private TableColumn<Forum, String> colEtat;
    @FXML private TableColumn<Forum, String> colCreatedBy;
    @FXML private TableColumn<Forum, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterEtat;
    @FXML private ComboBox<Categorie> categorieCombo;
    
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField createdByField;
    @FXML private ComboBox<String> etatCombo;
    @FXML private ComboBox<Categorie> categorieFormCombo;
    
    @FXML private Label errorLabel;
    @FXML private Label resultCountLabel;
    @FXML private Label totalForumsLabel;
    @FXML private Label activeForumsLabel;
    @FXML private Label inactiveForumsLabel;

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
        errorLabel.setVisible(false);
    }

    private void setupComboBoxes() {
        filterEtat.setItems(FXCollections.observableArrayList("Tous", "Actif", "Inactif", "Fermé"));
        filterEtat.getSelectionModel().selectFirst();
        
        etatCombo.setItems(FXCollections.observableArrayList("actif", "inactif", "fermé"));
        etatCombo.getSelectionModel().selectFirst();
        
        List<Categorie> categories = serviceCategorie.afficher();
        categorieCombo.setItems(FXCollections.observableArrayList(categories));
        categorieFormCombo.setItems(FXCollections.observableArrayList(categories));
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDescription() != null ? c.getValue().getDescription() : "—"));
        colCategorie.setCellValueFactory(c -> {
            Categorie cat = c.getValue().getCategorie();
            return new SimpleStringProperty(cat != null ? cat.getTitre() : "—");
        });
        colEtat.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEtat() != null ? c.getValue().getEtat() : "—"));
        colCreatedBy.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCreatedBy() != null ? c.getValue().getCreatedBy() : "—"));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button delBtn = new Button("🗑️");
            private final HBox box = new HBox(6, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:4 10;");
                delBtn.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:4 10;");
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

    private void loadData() {
        List<Forum> list = serviceForum.afficher();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Forum> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(forumTable.comparatorProperty());
        forumTable.setItems(sortedData);
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
    public void handleFilterEtat() {
        applyFilters();
    }

    @FXML
    public void handleFilterCategorie() {
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String etat = filterEtat.getValue();
        Categorie cat = categorieCombo.getValue();

        filteredData.setPredicate(forum -> {
            boolean matchSearch = search.isEmpty()
                    || forum.getTitre().toLowerCase().contains(search)
                    || (forum.getDescription() != null && forum.getDescription().toLowerCase().contains(search));

            boolean matchEtat = "Tous".equals(etat) || etat == null
                    || ("Actif".equals(etat) && "actif".equalsIgnoreCase(forum.getEtat()))
                    || ("Inactif".equals(etat) && "inactif".equalsIgnoreCase(forum.getEtat()))
                    || ("Fermé".equals(etat) && "fermé".equalsIgnoreCase(forum.getEtat()));

            boolean matchCategorie = cat == null || 
                    (forum.getCategorie() != null && forum.getCategorie().getId() == cat.getId());

            return matchSearch && matchEtat && matchCategorie;
        });
        updateResultCount();
    }

    @FXML
    public void sortByTitre() {
        masterData.sort(Comparator.comparing(Forum::getTitre, String.CASE_INSENSITIVE_ORDER));
        forumTable.refresh();
    }

    @FXML
    public void sortById() {
        masterData.sort(Comparator.comparingInt(Forum::getId));
        forumTable.refresh();
    }

    @FXML
    public void sortByEtat() {
        masterData.sort(Comparator.comparing(Forum::getEtat, String.CASE_INSENSITIVE_ORDER));
        forumTable.refresh();
    }

    @FXML
    public void handleAdd() {
        errorLabel.setVisible(false);
        
        String titre = titreField.getText();
        String description = descriptionField.getText();
        String createdBy = createdByField.getText();
        String etat = etatCombo.getValue();
        Categorie categorie = categorieFormCombo.getValue();

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
        Categorie categorie = categorieFormCombo.getValue();

        // Contrôle de saisie
        if (titre == null || titre.trim().isEmpty()) {
            showError("Le titre est obligatoire");
            return;
        }
        if (titre.length() > 200) {
            showError("Le titre ne doit pas dépasser 200 caractères");
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
        categorieFormCombo.setValue(forum.getCategorie());
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
        categorieFormCombo.setValue(null);
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

    @FXML
    public void handleBack(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forum_dashboard.fxml", event);
    }

    private void navigateTo(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
