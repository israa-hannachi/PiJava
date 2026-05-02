package tn.esprit.controllers.Back;

import com.itextpdf.text.DocumentException;
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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.controllers.cours.CoursCategorieController;
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.entities.cours.Cours;
import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.entities.cours.Cours_Module;
import tn.esprit.entities.users.Users;
import tn.esprit.services.cours.PdfExportService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BackCoursListController implements Initializable {

    // ── Table ─────────────────────────────────────────────────────────────────────
    @FXML private TableView<Cours>                coursTable;
    @FXML private TableColumn<Cours, String>      colTitre;
    @FXML private TableColumn<Cours, String>      colModule;
    @FXML private TableColumn<Cours, Integer>     colDuree;
    @FXML private TableColumn<Cours, Integer>     colOrdre;
    @FXML private TableColumn<Cours, String>      colFichier;
    @FXML private TableColumn<Cours, String>      colVisible;
    @FXML private TableColumn<Cours, String>      colActif;
    @FXML private TableColumn<Cours, Void>        colActions;

    // ── Filtres & stats ──────────────────────────────────────────────────────────
    @FXML private TextField        searchField;
    @FXML private ComboBox<String> filterModule;
    @FXML private ComboBox<String> filterVisible;
    @FXML private Label            totalCoursLabel;
    @FXML private Label            activeCoursLabel;
    @FXML private Label            withPdfLabel;
    @FXML private Label            totalDureeLabel;
    @FXML private Label            resultCountLabel;

    // ── Services ─────────────────────────────────────────────────────────────────
    private final CoursController         controller       = new CoursController();
    private final CoursModuleController   moduleController = new CoursModuleController();
    private final CoursCategorieController catController   = new CoursCategorieController();

    private ObservableList<Cours> masterData;
    private FilteredList<Cours>   filteredData;
    private final Map<Integer, String> moduleMap = new HashMap<>();
    private final Map<Integer, String> catMap    = new HashMap<>();
    private Users currentUser;

    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadModulesAndCategories();
        setupColumns();
        setupFilters();
        loadData();
    }

    public void initAdmin(Users user) {
        this.currentUser = user;
    }

    private void loadModulesAndCategories() {
        for (Cours_Module m : moduleController.recupererModules())
            moduleMap.put(m.getId(), m.getTitre());
        for (Cours_Categorie c : catController.recupererCategories())
            catMap.put(c.getId(), c.getNom());
    }

    private void setupColumns() {
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colModule.setCellValueFactory(c ->
                new SimpleStringProperty(moduleMap.getOrDefault(c.getValue().getModuleId(), "—")));
        colDuree.setCellValueFactory(new PropertyValueFactory<>("duree"));
        colOrdre.setCellValueFactory(new PropertyValueFactory<>("ordre"));
        colFichier.setCellValueFactory(c -> {
            String f = c.getValue().getFichierContenu();
            if (f == null || f.isEmpty()) return new SimpleStringProperty("—");
            if (f.startsWith("https://res.cloudinary.com")) return new SimpleStringProperty("☁️ Cloud");
            return new SimpleStringProperty("📄 Local");
        });
        colVisible.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getVisible() == 1 ? "✅" : "❌"));
        colActif.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getActif() == 1 ? "✅" : "❌"));

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn  = new Button("✏️");
            private final Button delBtn   = new Button("🗑️");
            private final Button viewBtn  = new Button("👁 PDF");
            private final HBox   box      = new HBox(4, editBtn, delBtn, viewBtn);

            {
                editBtn.setStyle("-fx-background-color:linear-gradient(to bottom right,#2563eb,#0891b2); -fx-text-fill:white; -fx-background-radius:10; -fx-padding:6 10; -fx-font-size:11; -fx-font-weight:bold; -fx-cursor:hand;");
                delBtn.setStyle("-fx-background-color:linear-gradient(to bottom right,#ef4444,#dc2626); -fx-text-fill:white; -fx-background-radius:10; -fx-padding:6 10; -fx-font-size:11; -fx-font-weight:bold; -fx-cursor:hand;");
                viewBtn.setStyle("-fx-background-color:linear-gradient(to bottom right,#8b5cf6,#2563eb); -fx-text-fill:white; -fx-background-radius:10; -fx-padding:6 10; -fx-font-size:11; -fx-font-weight:bold; -fx-cursor:hand;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e  -> handleDelete(getTableView().getItems().get(getIndex())));
                viewBtn.setOnAction(e -> handleViewPdf(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
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

        List<String> titles = new ArrayList<>();
        titles.add("Tous");
        titles.addAll(moduleMap.values());
        filterModule.setItems(FXCollections.observableArrayList(titles));
        filterModule.getSelectionModel().selectFirst();
    }

    private void loadData() {
        List<Cours> list = controller.recupererCours();
        masterData   = FXCollections.observableArrayList(list);
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
        withPdfLabel.setText(String.valueOf(list.stream()
                .filter(c -> c.getFichierContenu() != null && !c.getFichierContenu().isEmpty()).count()));
        long totalDuree = list.stream().mapToLong(Cours::getDuree).sum();
        totalDureeLabel.setText(String.valueOf(totalDuree));
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " résultat(s)");
    }

    // ── Filtres ───────────────────────────────────────────────────────────────────
    @FXML public void handleSearch()       { applyFilters(); }
    @FXML public void handleFilterModule() { applyFilters(); }
    @FXML public void handleFilterVisible(){ applyFilters(); }

    private void applyFilters() {
        String search    = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
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
                    || ("Caché".equals(visFilter)   && c.getVisible() == 0);
            return matchSearch && matchMod && matchVis;
        });
        updateResultCount();
    }

    // ── Tri ───────────────────────────────────────────────────────────────────────
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
        masterData.sort(Comparator.comparing(c -> c.getDateCreation() != null
                ? c.getDateCreation() : new java.sql.Timestamp(0)));
        coursTable.refresh();
    }

    // ── CRUD ─────────────────────────────────────────────────────────────────────
    @FXML
    public void handleAddCours(ActionEvent event) {
        openFormDialog(null, (Node) event.getSource());
    }

    private void handleEdit(Cours c) {
        openFormDialog(c, coursTable);
    }

    private void handleDelete(Cours c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation suppression");
        confirm.setHeaderText("Supprimer le cours");
        confirm.setContentText("Supprimer \"" + c.getTitre() + "\" ?\nLes réclamations associées seront aussi supprimées.");
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
                    Alert a = new Alert(Alert.AlertType.WARNING);
                    a.setContentText("Fichier introuvable : " + fichier);
                    a.showAndWait();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Export PDF ────────────────────────────────────────────────────────────────
    /**
     * Exporte le catalogue complet (toutes catégories → modules → cours) en PDF.
     * L'utilisateur choisit l'emplacement de sauvegarde.
     */
    @FXML
    public void handleExportPdf(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Enregistrer le catalogue PDF");
        chooser.setInitialFileName("catalogue_cours_" +
                java.time.LocalDate.now().toString().replace("-", "") + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        File dest = chooser.showSaveDialog(stage);
        if (dest == null) return;

        try {
            List<Cours_Categorie> categories = catController.recupererCategories();
            List<Cours_Module>    modules    = moduleController.recupererModules();
            List<Cours>           cours      = controller.recupererCours();

            // Construire catMap (id → nom catégorie)
            Map<Integer, String> catNameMap = new HashMap<>();
            for (Cours_Categorie c : categories) catNameMap.put(c.getId(), c.getNom());

            PdfExportService.exportCatalogue(dest, categories, modules, cours, moduleMap, catNameMap);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Export réussi");
            ok.setHeaderText("Catalogue PDF généré !");
            ok.setContentText("Fichier enregistré :\n" + dest.getAbsolutePath());
            ok.showAndWait();

            // Ouvrir automatiquement le PDF
            try { java.awt.Desktop.getDesktop().open(dest); } catch (Exception ignored) {}

        } catch (DocumentException | IOException ex) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Erreur export PDF");
            err.setContentText("Impossible de générer le PDF :\n" + ex.getMessage());
            err.showAndWait();
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────────
    private void openFormDialog(Cours c, Node ownerNode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_Cours_form.fxml"));
            Parent root = loader.load();
            BackCoursFormController formCtrl = loader.getController();
            formCtrl.initForm(c);
            formCtrl.setDialogMode(true);

            Stage owner = (Stage) ownerNode.getScene().getWindow();
            Stage dialog = new Stage();
            dialog.initOwner(owner);
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle(c == null ? "Ajouter un cours" : "Modifier le cours");
            dialog.setScene(new Scene(root, 920, 760));
            dialog.setMinWidth(760);
            dialog.setMinHeight(620);
            dialog.setResizable(true);
            dialog.showAndWait();
            loadData();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void handleDashboard(ActionEvent event) { navigate("/tn/esprit/view/back_admin.fxml",              event); }
    @FXML public void handleCategories(ActionEvent event){ navigate("/tn/esprit/view/back_CoursCategorieList.fxml", event); }
    @FXML public void handleModules(ActionEvent event)   { navigate("/tn/esprit/view/back_CoursModuleList.fxml",    event); }
    @FXML public void handleCours(ActionEvent event)     { loadData(); }
    @FXML public void handleLogout(ActionEvent event)    { navigate("/tn/esprit/view/front_login.fxml",             event); }

    private void navigate(String fxml, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof tn.esprit.controllers.front.AdminDashboardController) {
                ((tn.esprit.controllers.front.AdminDashboardController) ctrl).initAdmin(currentUser);
            } else if (ctrl instanceof BackCoursCategorieController) {
                ((BackCoursCategorieController) ctrl).initAdmin(currentUser);
            } else if (ctrl instanceof BackCoursModuleController) {
                ((BackCoursModuleController) ctrl).initAdmin(currentUser);
            } else if (ctrl instanceof BackCoursListController) {
                ((BackCoursListController) ctrl).initAdmin(currentUser);
            }
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setMaximized(true);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
