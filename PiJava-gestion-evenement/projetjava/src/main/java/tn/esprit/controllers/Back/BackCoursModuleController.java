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
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.entities.cours.Cours_Module;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BackCoursModuleController implements Initializable {

    @FXML private TableView<Cours_Module> moduleTable;
    @FXML private TableColumn<Cours_Module, Integer> colId;
    @FXML private TableColumn<Cours_Module, String> colTitre;
    @FXML private TableColumn<Cours_Module, String> colNiveau;
    @FXML private TableColumn<Cours_Module, Integer> colDuree;
    @FXML private TableColumn<Cours_Module, String> colCategorie;
    @FXML private TableColumn<Cours_Module, String> colActif;
    @FXML private TableColumn<Cours_Module, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterNiveau;
    @FXML private ComboBox<String> filterCategorie;
    @FXML private Label totalModLabel;
    @FXML private Label debutantLabel;
    @FXML private Label interLabel;
    @FXML private Label avanceLabel;
    @FXML private Label resultCountLabel;

    private final CoursModuleController controller = new CoursModuleController();
    private final CoursCategorieController catController = new CoursCategorieController();
    private ObservableList<Cours_Module> masterData;
    private FilteredList<Cours_Module> filteredData;
    private Map<Integer, String> categorieMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadCategories();
        setupColumns();
        setupFilters();
        loadData();
    }

    private void loadCategories() {
        for (Cours_Categorie c : catController.recupererCategories()) {
            categorieMap.put(c.getId(), c.getNom());
        }
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colNiveau.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colCategorie.setCellValueFactory(c ->
                new SimpleStringProperty(categorieMap.getOrDefault(c.getValue().getCategorieId(), "—")));
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

    private void setupFilters() {
        filterNiveau.setItems(FXCollections.observableArrayList(
                "Tous", "Débutant", "Intermédiaire", "Avancé"));
        filterNiveau.getSelectionModel().selectFirst();

        List<String> catNames = new ArrayList<>();
        catNames.add("Toutes");
        catNames.addAll(categorieMap.values());
        filterCategorie.setItems(FXCollections.observableArrayList(catNames));
        filterCategorie.getSelectionModel().selectFirst();
    }

    private void loadData() {
        List<Cours_Module> list = controller.recupererModules();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Cours_Module> sorted = new SortedList<>(filteredData);
        sorted.comparatorProperty().bind(moduleTable.comparatorProperty());
        moduleTable.setItems(sorted);
        updateStats(list);
        updateResultCount();
    }

    private void updateStats(List<Cours_Module> list) {
        totalModLabel.setText(String.valueOf(list.size()));
        debutantLabel.setText(String.valueOf(list.stream().filter(m -> "Débutant".equalsIgnoreCase(m.getNiveau())).count()));
        interLabel.setText(String.valueOf(list.stream().filter(m -> "Intermédiaire".equalsIgnoreCase(m.getNiveau())).count()));
        avanceLabel.setText(String.valueOf(list.stream().filter(m -> "Avancé".equalsIgnoreCase(m.getNiveau())).count()));
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " résultat(s)");
    }

    @FXML public void handleSearch() { applyFilters(); }
    @FXML public void handleFilterNiveau() { applyFilters(); }
    @FXML public void handleFilterCategorie() { applyFilters(); }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String niveau = filterNiveau.getValue();
        String categorie = filterCategorie.getValue();

        filteredData.setPredicate(mod -> {
            boolean matchSearch = search.isEmpty()
                    || mod.getTitre().toLowerCase().contains(search)
                    || (mod.getDescription() != null && mod.getDescription().toLowerCase().contains(search));
            boolean matchNiveau = "Tous".equals(niveau) || niveau == null
                    || mod.getNiveau().equalsIgnoreCase(niveau);
            boolean matchCat = "Toutes".equals(categorie) || categorie == null
                    || categorie.equals(categorieMap.get(mod.getCategorieId()));
            return matchSearch && matchNiveau && matchCat;
        });
        updateResultCount();
    }

    @FXML public void sortByTitre() {
        masterData.sort(Comparator.comparing(Cours_Module::getTitre, String.CASE_INSENSITIVE_ORDER));
        moduleTable.refresh();
    }
    @FXML public void sortByDuree() {
        masterData.sort(Comparator.comparingInt(Cours_Module::getDuree));
        moduleTable.refresh();
    }
    @FXML public void sortByNiveau() {
        List<String> order = List.of("Débutant", "Intermédiaire", "Avancé");
        masterData.sort(Comparator.comparingInt(m -> {
            int idx = order.indexOf(m.getNiveau());
            return idx == -1 ? 99 : idx;
        }));
        moduleTable.refresh();
    }

    @FXML
    public void handleAddModule(ActionEvent event) {
        navigateToForm(event, null);
    }

    private void handleEdit(Cours_Module mod) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_CoursModule_form.fxml"));
            Parent root = loader.load();
            BackCoursModuleFormController formCtrl = loader.getController();
            formCtrl.initForm(mod);
            Stage stage = (Stage) moduleTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleDelete(Cours_Module mod) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le module");
        confirm.setContentText("Supprimer \"" + mod.getTitre() + "\" et tous ses cours associés ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.supprimerModule(mod.getId());
            loadData();
        }
    }

    private void navigateToForm(ActionEvent event, Cours_Module mod) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_CoursModule_form.fxml"));
            Parent root = loader.load();
            BackCoursModuleFormController formCtrl = loader.getController();
            formCtrl.initForm(mod);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── SIDEBAR ──────────────────────────────────────────────────────────────────
    @FXML public void handleDashboard(ActionEvent event) { navigate("/tn/esprit/view/back_admin.fxml", event); }
    @FXML public void handleCategories(ActionEvent event) { navigate("/tn/esprit/view/back_CoursCategorieList.fxml", event); }
    @FXML public void handleModules(ActionEvent event) { loadData(); }
    @FXML public void handleCours(ActionEvent event) { navigate("/tn/esprit/view/back_CoursList.fxml", event); }
    @FXML public void handleLogout(ActionEvent event) { navigate("/tn/esprit/view/front_login.fxml", event); }

    private void navigate(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
