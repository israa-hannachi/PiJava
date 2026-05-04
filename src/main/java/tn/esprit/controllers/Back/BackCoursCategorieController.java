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
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursCategorieController;
import tn.esprit.entities.cours.Cours_Categorie;

import java.io.IOException;
import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackCoursCategorieController implements Initializable {

    @FXML private TableView<Cours_Categorie> categorieTable;
    @FXML private TableColumn<Cours_Categorie, String> colNom;
    @FXML private TableColumn<Cours_Categorie, String> colDescription;
    @FXML private TableColumn<Cours_Categorie, String> colDateCreation;
    @FXML private TableColumn<Cours_Categorie, String> colActif;
    @FXML private TableColumn<Cours_Categorie, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterActif;
    @FXML private Label totalCatLabel;
    @FXML private Label activeCatLabel;
    @FXML private Label inactiveCatLabel;
    @FXML private Label resultCountLabel;
    @FXML private Label errorLabel;

    private final CoursCategorieController controller = new CoursCategorieController();
    private ObservableList<Cours_Categorie> masterData;
    private FilteredList<Cours_Categorie> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        filterActif.setItems(FXCollections.observableArrayList("Tous", "Actif", "Inactif"));
        filterActif.getSelectionModel().selectFirst();
        loadData();
    }

    private void setupColumns() {
        colNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colDescription.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDescription() != null ? c.getValue().getDescription() : "—"));
        colDateCreation.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getDateCreation() != null
                        ? c.getValue().getDateCreation().toString().substring(0, 10) : "—"));
        colActif.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getActif() == 1 ? "✅ Actif" : "❌ Inactif"));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("✏️ Modifier");
            private final Button delBtn = new Button("🗑️ Supprimer");
            private final HBox box = new HBox(6, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11;");
                delBtn.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:4 10; -fx-font-size:11;");
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
        List<Cours_Categorie> list = controller.recupererCategories();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Cours_Categorie> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(categorieTable.comparatorProperty());
        categorieTable.setItems(sortedData);
        updateStats(list);
        updateResultCount();
    }

    private void updateStats(List<Cours_Categorie> list) {
        long total = list.size();
        long active = list.stream().filter(c -> c.getActif() == 1).count();
        long inactive = total - active;
        totalCatLabel.setText(String.valueOf(total));
        activeCatLabel.setText(String.valueOf(active));
        inactiveCatLabel.setText(String.valueOf(inactive));
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " résultat(s)");
    }

    @FXML
    public void handleSearch() {
        applyFilters();
    }

    @FXML
    public void handleFilterActif() {
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String statut = filterActif.getValue();

        filteredData.setPredicate(cat -> {
            boolean matchSearch = search.isEmpty()
                    || cat.getNom().toLowerCase().contains(search)
                    || (cat.getDescription() != null && cat.getDescription().toLowerCase().contains(search));

            boolean matchStatut = "Tous".equals(statut) || statut == null
                    || ("Actif".equals(statut) && cat.getActif() == 1)
                    || ("Inactif".equals(statut) && cat.getActif() == 0);

            return matchSearch && matchStatut;
        });
        updateResultCount();
    }

    @FXML public void sortByNom() {
        masterData.sort(Comparator.comparing(Cours_Categorie::getNom, String.CASE_INSENSITIVE_ORDER));
        categorieTable.refresh();
    }

    @FXML public void sortByDate() {
        masterData.sort(Comparator.comparing(c -> c.getDateCreation() != null ? c.getDateCreation() : new java.sql.Timestamp(0)));
        categorieTable.refresh();
    }

    @FXML public void sortByStatut() {
        masterData.sort(Comparator.comparingInt(Cours_Categorie::getActif).reversed());
        categorieTable.refresh();
    }

    @FXML
    public void handleAddCategorie(ActionEvent event) {
        navigateToForm(event, null);
    }

    private void handleEdit(Cours_Categorie cat) {
        navigateToFormDirect(cat);
    }

    private void handleDelete(Cours_Categorie cat) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la catégorie");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + cat.getNom() + "\" ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.supprimerCategorie(cat.getId());
            loadData();
        }
    }

    private void navigateToForm(ActionEvent event, Cours_Categorie cat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_CoursCategorie_form.fxml"));
            Parent root = loader.load();
            BackCoursCategorieFormController formCtrl = loader.getController();
            formCtrl.initForm(cat);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToFormDirect(Cours_Categorie cat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_CoursCategorie_form.fxml"));
            Parent root = loader.load();
            BackCoursCategorieFormController formCtrl = loader.getController();
            formCtrl.initForm(cat);
            Stage stage = (Stage) categorieTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─── SIDEBAR NAVIGATION ──────────────────────────────────────────────────────

    @FXML public void handleDashboard(ActionEvent event) { navigate("/tn/esprit/view/back_admin.fxml", event); }
    @FXML public void handleCategories(ActionEvent event) { /* already here */ loadData(); }
    @FXML public void handleModules(ActionEvent event) { navigate("/tn/esprit/view/back_CoursModuleList.fxml", event); }
    @FXML public void handleCours(ActionEvent event) { navigate("/tn/esprit/view/back_CoursList.fxml", event); }
    @FXML public void handleLogout(ActionEvent event) { navigate("/tn/esprit/view/front_login.fxml", event); }

    private void navigate(String fxml, ActionEvent event) {
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
