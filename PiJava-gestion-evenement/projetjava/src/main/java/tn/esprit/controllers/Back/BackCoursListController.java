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
import tn.esprit.controllers.Back.BackCoursFormController;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.entities.cours.Cours;
import tn.esprit.entities.cours.Cours_Module;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BackCoursListController implements Initializable {

    @FXML private TableView<Cours> coursTable;
    @FXML private TableColumn<Cours, Integer> colId;
    @FXML private TableColumn<Cours, String> colTitre;
    @FXML private TableColumn<Cours, String> colModule;
    @FXML private TableColumn<Cours, Integer> colDuree;
    @FXML private TableColumn<Cours, Integer> colOrdre;
    @FXML private TableColumn<Cours, String> colFichier;
    @FXML private TableColumn<Cours, String> colVisible;
    @FXML private TableColumn<Cours, String> colActif;
    @FXML private TableColumn<Cours, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterModule;
    @FXML private ComboBox<String> filterVisible;
    @FXML private Label totalCoursLabel;
    @FXML private Label activeCoursLabel;
    @FXML private Label withPdfLabel;
    @FXML private Label totalDureeLabel;
    @FXML private Label resultCountLabel;

    private final CoursController controller = new CoursController();
    private final CoursModuleController moduleController = new CoursModuleController();
    private ObservableList<Cours> masterData;
    private FilteredList<Cours> filteredData;
    private final Map<Integer, String> moduleMap = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadModules();
        setupColumns();
        setupFilters();
        loadData();
    }

    private void loadModules() {
        for (Cours_Module m : moduleController.recupererModules()) {
            moduleMap.put(m.getId(), m.getTitre());
        }
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colModule.setCellValueFactory(c ->
                new SimpleStringProperty(moduleMap.getOrDefault(c.getValue().getModuleId(), "—")));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        colFichier.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getFichierContenu() != null ? "📄 PDF" : "—"));
        colVisible.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getVisible() == 1 ? "✅" : "❌"));
        colActif.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getActif() == 1 ? "✅" : "❌"));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn   = new Button("✏️");
            private final Button delBtn    = new Button("🗑️");
            private final Button viewBtn   = new Button("👁️ PDF");
            private final HBox box = new HBox(4, editBtn, delBtn, viewBtn);

            {
                editBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:5; -fx-padding:4 8; -fx-font-size:11;");
                delBtn.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-background-radius:5; -fx-padding:4 8; -fx-font-size:11;");
                viewBtn.setStyle("-fx-background-color:#7c3aed; -fx-text-fill:white; -fx-background-radius:5; -fx-padding:4 8; -fx-font-size:11;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
                viewBtn.setOnAction(e -> handleViewPdf(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Cours c = getTableView().getItems().get(getIndex());
                viewBtn.setVisible(c.getFichierContenu() != null && !c.getFichierContenu().isEmpty());
                viewBtn.setManaged(c.getFichierContenu() != null && !c.getFichierContenu().isEmpty());
                setGraphic(box);
            }
        });
    }

    private void setupFilters() {
        filterVisible.setItems(FXCollections.observableArrayList("Tous", "Visible", "Caché"));
        filterVisible.getSelectionModel().selectFirst();

        List<String> moduleTitles = new ArrayList<>();
        moduleTitles.add("Tous");
        moduleTitles.addAll(moduleMap.values());
        filterModule.setItems(FXCollections.observableArrayList(moduleTitles));
        filterModule.getSelectionModel().selectFirst();
    }

    private void loadData() {
        List<Cours> list = controller.recupererCours();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Cours> sorted = new SortedList<>(filteredData);
        sorted.comparatorProperty().bind(coursTable.comparatorProperty());
        coursTable.setItems(sorted);
        updateStats(list);
        updateResultCount();
    }

    private void updateStats(List<Cours> list) {
        totalCoursLabel.setText(String.valueOf(list.size()));
        activeCoursLabel.setText(String.valueOf(list.stream().filter(c -> c.getActif() == 1).count()));
        withPdfLabel.setText(String.valueOf(list.stream().filter(c -> c.getFichierContenu() != null && !c.getFichierContenu().isEmpty()).count()));
        long totalDuree = list.stream().mapToLong(Cours::getDuree).sum();
        totalDureeLabel.setText(String.valueOf(totalDuree));
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " résultat(s)");
    }

    @FXML public void handleSearch() { applyFilters(); }
    @FXML public void handleFilterModule() { applyFilters(); }
    @FXML public void handleFilterVisible() { applyFilters(); }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String modFilter = filterModule.getValue();
        String visFilter = filterVisible.getValue();

        filteredData.setPredicate(c -> {
            boolean matchSearch = search.isEmpty()
                    || c.getTitre().toLowerCase().contains(search)
                    || (c.getDescription() != null && c.getDescription().toLowerCase().contains(search));
            boolean matchMod = "Tous".equals(modFilter) || modFilter == null
                    || modFilter.equals(moduleMap.get(c.getModuleId()));
            boolean matchVis = "Tous".equals(visFilter) || visFilter == null
                    || ("Visible".equals(visFilter) && c.getVisible() == 1)
                    || ("Caché".equals(visFilter) && c.getVisible() == 0);
            return matchSearch && matchMod && matchVis;
        });
        updateResultCount();
    }

    @FXML public void sortByTitre() {
        masterData.sort(Comparator.comparing(Cours::getTitre, String.CASE_INSENSITIVE_ORDER));
        coursTable.refresh();
    }
    @FXML public void sortByDuree() {
        masterData.sort(Comparator.comparingInt(Cours::getDuree));
        coursTable.refresh();
    }
    @FXML public void sortByOrdre() {
        masterData.sort(Comparator.comparingInt(Cours::getOrdre));
        coursTable.refresh();
    }
    @FXML public void sortByDate() {
        masterData.sort(Comparator.comparing(c -> c.getDateCreation() != null ? c.getDateCreation() : new java.sql.Timestamp(0)));
        coursTable.refresh();
    }

    @FXML
    public void handleAddCours(ActionEvent event) {
        navigateToForm(event, null);
    }

    private void handleEdit(Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_Cours_form.fxml"));
            Parent root = loader.load();
            BackCoursFormController formCtrl = loader.getController();
            formCtrl.initForm(c);
            Stage stage = (Stage) coursTable.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void handleDelete(Cours c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation suppression");
        confirm.setHeaderText("Supprimer le cours");
        confirm.setContentText("Supprimer \"" + c.getTitre() + "\" ? Les réclamations associées seront aussi supprimées.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            controller.supprimerCours(c.getId());
            loadData();
        }
    }

    private void handleViewPdf(Cours c) {
        String fichier = c.getFichierContenu();
        if (fichier == null || fichier.isEmpty()) return;
        try {
            if (fichier.startsWith("http")) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(fichier));
            } else {
                java.io.File file = new java.io.File(fichier);
                if (file.exists()) java.awt.Desktop.getDesktop().open(file);
                else {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("Fichier introuvable: " + fichier);
                    alert.showAndWait();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigateToForm(ActionEvent event, Cours c) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_Cours_form.fxml"));
            Parent root = loader.load();
            BackCoursFormController formCtrl = loader.getController();
            formCtrl.initForm(c);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── SIDEBAR ──────────────────────────────────────────────────────────────────
    @FXML public void handleDashboard(ActionEvent event) { navigate("/tn/esprit/view/back_admin.fxml", event); }
    @FXML public void handleCategories(ActionEvent event) { navigate("/tn/esprit/view/back_CoursCategorieList.fxml", event); }
    @FXML public void handleModules(ActionEvent event) { navigate("/tn/esprit/view/back_CoursModuleList.fxml", event); }
    @FXML public void handleCours(ActionEvent event) { loadData(); }
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
