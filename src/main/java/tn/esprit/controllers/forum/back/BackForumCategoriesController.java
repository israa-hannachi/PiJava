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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.services.forum.ServiceCategorie;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackForumCategoriesController implements Initializable {

    @FXML private TableView<Categorie> categorieTable;
    @FXML private TableColumn<Categorie, String> colTitre;
    @FXML private TableColumn<Categorie, String> colDescription;
    @FXML private TableColumn<Categorie, String> colIcone;
    @FXML private TableColumn<Categorie, Void> colActions;
    @FXML private TextField searchField;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField iconeField;
    @FXML private Label errorLabel;
    @FXML private Label resultCountLabel;
    @FXML private BorderPane rootPane;
    @FXML private HBox topBar;
    @FXML private VBox listContainer;
    @FXML private AnchorPane formContainer;
    @FXML private Button addBtn;
    @FXML private Button updateBtn;

    private final ServiceCategorie serviceCategorie = new ServiceCategorie();
    private ObservableList<Categorie> masterData;
    private FilteredList<Categorie> filteredData;
    private Categorie selectedCategorie = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        loadData();
        errorLabel.setVisible(false);
    }

    private void setupColumns() {

        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colDescription.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDescription() != null ? c.getValue().getDescription() : "—"));
        colIcone.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getIcone() != null ? c.getValue().getIcone() : "—"));

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
        List<Categorie> list = serviceCategorie.afficher();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Categorie> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(categorieTable.comparatorProperty());
        categorieTable.setItems(sortedData);
        updateResultCount();
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " catégorie(s)");
    }

    @FXML
    public void handleSearch() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        filteredData.setPredicate(cat -> {
            if (search.isEmpty()) return true;
            return cat.getTitre().toLowerCase().contains(search) ||
                   (cat.getDescription() != null && cat.getDescription().toLowerCase().contains(search));
        });
        updateResultCount();
    }

    @FXML
    public void sortByTitre() {
        masterData.sort(Comparator.comparing(Categorie::getTitre, String.CASE_INSENSITIVE_ORDER));
        categorieTable.refresh();
    }



    @FXML
    public void handleAdd() {
        errorLabel.setVisible(false);
        
        String titre = titreField.getText();
        String description = descriptionField.getText();
        String icone = iconeField.getText();

        // Contrôle de saisie
        if (titre == null || titre.trim().isEmpty()) {
            showError("Le titre est obligatoire");
            return;
        }
        if (titre.length() > 255) {
            showError("Le titre ne doit pas dépasser 255 caractères");
            return;
        }
        if (icone != null && icone.length() > 500) {
            showError("L'icône ne doit pas dépasser 500 caractères");
            return;
        }

        Categorie cat = new Categorie(titre.trim(), description, icone);
        serviceCategorie.ajouter(cat);
        
        clearFields();
        loadData();
        showList();
        // showSuccess is handled in showList if needed, but here we just return
    }

    @FXML
    public void handleUpdate() {
        if (selectedCategorie == null) {
            showError("Veuillez sélectionner une catégorie à modifier");
            return;
        }

        String titre = titreField.getText();
        String description = descriptionField.getText();
        String icone = iconeField.getText();

        // Contrôle de saisie
        if (titre == null || titre.trim().isEmpty()) {
            showError("Le titre est obligatoire");
            return;
        }
        if (titre.length() > 255) {
            showError("Le titre ne doit pas dépasser 255 caractères");
            return;
        }

        selectedCategorie.setTitre(titre.trim());
        selectedCategorie.setDescription(description);
        selectedCategorie.setIcone(icone);
        
        serviceCategorie.modifier(selectedCategorie);
        
        clearFields();
        selectedCategorie = null;
        loadData();
        showList();
    }

    @FXML
    public void handleClear() {
        clearFields();
        selectedCategorie = null;
        errorLabel.setVisible(false);
    }

    private void handleEdit(Categorie cat) {
        selectedCategorie = cat;
        titreField.setText(cat.getTitre());
        descriptionField.setText(cat.getDescription());
        iconeField.setText(cat.getIcone());
        showCreateForm();
        addBtn.setVisible(false);
        addBtn.setManaged(false);
        updateBtn.setVisible(true);
        updateBtn.setManaged(true);
    }

    private void handleDelete(Categorie cat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la catégorie");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + cat.getTitre() + "\" ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceCategorie.supprimer(cat.getId());
            loadData();
            showSuccess("Catégorie supprimée avec succès");
        }
    }

    private void clearFields() {
        titreField.clear();
        descriptionField.clear();
        iconeField.clear();
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
        navigateTo("/tn/esprit/view/back_forums_dashboard.fxml", event);
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

    @FXML
    public void showCreateForm() {
        if (listContainer != null && formContainer != null) {
            listContainer.setVisible(false);
            listContainer.setManaged(false);
            formContainer.setVisible(true);
            formContainer.setManaged(true);
            
            // Hide navigation to match requested look
            if (topBar != null) {
                topBar.setVisible(false);
                topBar.setManaged(false);
            }
            
            if (selectedCategorie == null) {
                clearFields();
                addBtn.setVisible(true);
                addBtn.setManaged(true);
                updateBtn.setVisible(false);
                updateBtn.setManaged(false);
            }
        }
    }

    @FXML
    public void showList() {
        if (listContainer != null && formContainer != null) {
            listContainer.setVisible(true);
            listContainer.setManaged(true);
            formContainer.setVisible(false);
            formContainer.setManaged(false);
            
            // Restore navigation
            if (topBar != null) {
                topBar.setVisible(true);
                topBar.setManaged(true);
            }
            
            loadData();
        }
    }
}
