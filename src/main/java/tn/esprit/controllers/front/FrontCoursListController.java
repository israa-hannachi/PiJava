package tn.esprit.controllers.front;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.entities.cours.cours;
import tn.esprit.entities.cours.cours_categorie;
import tn.esprit.entities.cours.cours_module;
import tn.esprit.entities.users.Users;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class FrontCoursListController implements Initializable {

    @FXML private FlowPane coursContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterVisible;
    @FXML private Label moduleNomLabel;
    @FXML private Label moduleNiveauLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private Label totalCoursLabel;
    @FXML private Label withPdfLabel;
    @FXML private Label totalDureeLabel;
    @FXML private Label countLabel;
    @FXML private Label profBadge;
    @FXML private Button addCoursBtn;
    @FXML private Button profileButton;
    @FXML private Button backToModulesBtn;
    @FXML private VBox emptyState;

    private final CoursController coursController = new CoursController();

    private Users currentUser;
    private cours_categorie currentCategorie;
    private cours_module currentModule;
    private List<cours> allCours = new ArrayList<>();
    private boolean isProf = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterVisible.setItems(FXCollections.observableArrayList("Tous", "Visible", "Caché"));
        filterVisible.getSelectionModel().selectFirst();
    }

    public void initData(Users user, cours_categorie cat, cours_module mod) {
        this.currentUser = user;
        this.currentCategorie = cat;
        this.currentModule = mod;

        if (user != null) {
            profileButton.setText(user.getFirstName());
            String role = user.getRole();
            // Prof = "Enseignant" role
            isProf = "Enseignant".equalsIgnoreCase(role);
        }

        // Setup UI based on role
        if (isProf) {
            addCoursBtn.setVisible(true);
            addCoursBtn.setManaged(true);
            profBadge.setVisible(true);
            profBadge.setManaged(true);
        }

        moduleNomLabel.setText("📖 " + mod.getTitre());
        moduleNiveauLabel.setText("Niveau : " + mod.getNiveau() + "  |  Durée : " + mod.getDuree() + "h");
        breadcrumbLabel.setText(mod.getTitre());

        loadData();
    }

    private void loadData() {
        allCours = coursController.findByModuleId(currentModule.getId()).stream()
                .filter(c -> c.getActif() == 1)
                .sorted(Comparator.comparingInt(cours::getOrdre))
                .collect(Collectors.toList());
        updateStats(allCours);
        renderCards(allCours);
    }

    private void updateStats(List<cours> list) {
        totalCoursLabel.setText(String.valueOf(list.size()));
        withPdfLabel.setText(String.valueOf(
                list.stream().filter(c -> c.getFichierContenu() != null && !c.getFichierContenu().isEmpty()).count()));
        long totalMin = list.stream().mapToLong(cours::getDuree).sum();
        totalDureeLabel.setText(String.valueOf(totalMin));
    }

    private void renderCards(List<cours> list) {
        coursContainer.getChildren().clear();
        boolean empty = list.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        countLabel.setText(list.size() + " cours");

        for (cours c : list) {
            boolean hasPdf = c.getFichierContenu() != null && !c.getFichierContenu().isEmpty();

            VBox card = new VBox(10);
            card.setPrefWidth(320);
            card.setStyle("-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
                    "-fx-border-color:#e5e7eb; -fx-border-radius:16; " +
                    "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),8,0,0,2);");

            // Top row: order + duration
            HBox topRow = new HBox(8);
            topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label ordreLabel = new Label("#" + c.getOrdre());
            ordreLabel.setStyle("-fx-background-color:#f0f9ff; -fx-text-fill:#0369a1; " +
                    "-fx-background-radius:6; -fx-padding:2 8; -fx-font-weight:700; -fx-font-size:11;");
            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);
            Label dureeLabel = new Label("⏱ " + c.getDuree() + " min");
            dureeLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
            topRow.getChildren().addAll(ordreLabel, sp, dureeLabel);

            Label titreLabel = new Label(c.getTitre());
            titreLabel.setStyle("-fx-font-size:17px; -fx-font-weight:800; -fx-text-fill:#1f2937; -fx-wrap-text:true;");
            titreLabel.setWrapText(true);

            Label descLabel = new Label(c.getDescription() != null && !c.getDescription().isEmpty()
                    ? c.getDescription() : "");
            descLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-wrap-text:true;");
            descLabel.setWrapText(true);

            // PDF + Visible badges
            HBox badges = new HBox(6);
            if (hasPdf) {
                Label pdfBadge = new Label("📄 PDF");
                pdfBadge.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:#2563eb; " +
                        "-fx-background-radius:6; -fx-padding:2 8; -fx-font-size:11;");
                badges.getChildren().add(pdfBadge);
            }
            if (c.getVisible() == 1) {
                Label visBadge = new Label("👁 Visible");
                visBadge.setStyle("-fx-background-color:#f0fdf4; -fx-text-fill:#166534; " +
                        "-fx-background-radius:6; -fx-padding:2 8; -fx-font-size:11;");
                badges.getChildren().add(visBadge);
            }

            // Footer actions
            HBox footer = new HBox(6);
            footer.setStyle("-fx-border-color:#f1f5f9; -fx-border-width:1 0 0 0; -fx-padding:8 0 0 0;");
            footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            if (hasPdf) {
                Button viewPdfBtn = new Button("📄 Voir PDF");
                viewPdfBtn.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:#2563eb; " +
                        "-fx-background-radius:8; -fx-padding:5 12; -fx-font-size:12; -fx-cursor:hand;");
                viewPdfBtn.setOnAction(e -> openPdf(c));
                footer.getChildren().add(viewPdfBtn);
            }

            if (c.getContenu() != null && !c.getContenu().isEmpty()) {
                Button viewContentBtn = new Button("📖 Lire");
                viewContentBtn.setStyle("-fx-background-color:#f0fdf4; -fx-text-fill:#166534; " +
                        "-fx-background-radius:8; -fx-padding:5 12; -fx-font-size:12; -fx-cursor:hand;");
                viewContentBtn.setOnAction(e -> showContent(c));
                footer.getChildren().add(viewContentBtn);
            }

            // PROF ONLY: edit + delete buttons
            if (isProf) {
                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                footer.getChildren().add(spacer);

                Button editBtn = new Button("✏️");
                editBtn.setStyle("-fx-background-color:#fef9c3; -fx-text-fill:#92400e; " +
                        "-fx-background-radius:8; -fx-padding:5 10; -fx-font-size:12; -fx-cursor:hand;");
                editBtn.setTooltip(new Tooltip("Modifier ce cours"));
                editBtn.setOnAction(e -> handleEditCours(c));

                Button delBtn = new Button("🗑️");
                delBtn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; " +
                        "-fx-background-radius:8; -fx-padding:5 10; -fx-font-size:12; -fx-cursor:hand;");
                delBtn.setTooltip(new Tooltip("Supprimer ce cours"));
                delBtn.setOnAction(e -> handleDeleteCours(c));

                footer.getChildren().addAll(editBtn, delBtn);
            }

            card.getChildren().addAll(topRow, titreLabel);
            if (!descLabel.getText().isEmpty()) card.getChildren().add(descLabel);
            if (!badges.getChildren().isEmpty()) card.getChildren().add(badges);
            card.getChildren().add(footer);

            coursContainer.getChildren().add(card);
        }
    }

    private void openPdf(cours c) {
        String path = c.getFichierContenu();
        if (path == null || path.isEmpty()) return;
        try {
            if (path.startsWith("http")) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(path));
            } else {
                File f = new File(path);
                if (f.exists()) java.awt.Desktop.getDesktop().open(f);
                else {
                    Alert a = new Alert(Alert.AlertType.WARNING, "Fichier introuvable : " + path);
                    a.showAndWait();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showContent(cours c) {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle(c.getTitre());
        dlg.setHeaderText(c.getTitre());
        // Strip basic HTML tags for display
        String text = c.getContenu().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim();
        TextArea ta = new TextArea(text);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setPrefSize(500, 300);
        dlg.getDialogPane().setContent(ta);
        dlg.showAndWait();
    }

    // ─── PROF ONLY CRUD ──────────────────────────────────────────────────────────

    @FXML
    public void handleAddCours() {
        showCoursFormDialog(null);
    }

    private void handleEditCours(cours c) {
        showCoursFormDialog(c);
    }

    private void handleDeleteCours(cours c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer le cours");
        confirm.setContentText("Supprimer \"" + c.getTitre() + "\" définitivement ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            coursController.supprimerCours(c.getId());
            loadData();
        }
    }

    /**
     * Inline dialog for add/edit a cours (prof only).
     * Uses a simple Dialog<ButtonType> with a form inside.
     */
    private void showCoursFormDialog(cours coursToEdit) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(coursToEdit == null ? "Ajouter un cours" : "Modifier le cours");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);
        form.setPrefWidth(500);

        TextField titreField = new TextField(coursToEdit != null ? coursToEdit.getTitre() : "");
        titreField.setPromptText("Titre du cours");

        TextArea descField = new TextArea(coursToEdit != null && coursToEdit.getDescription() != null
                ? coursToEdit.getDescription() : "");
        descField.setPromptText("Description");
        descField.setPrefRowCount(2);

        TextArea contenuField = new TextArea(coursToEdit != null && coursToEdit.getContenu() != null
                ? coursToEdit.getContenu() : "");
        contenuField.setPromptText("Contenu texte");
        contenuField.setPrefRowCount(3);

        TextField dureeField = new TextField(coursToEdit != null ? String.valueOf(coursToEdit.getDuree()) : "");
        dureeField.setPromptText("Durée (minutes)");

        TextField ordreField = new TextField(coursToEdit != null ? String.valueOf(coursToEdit.getOrdre()) : "1");
        ordreField.setPromptText("Ordre");

        ComboBox<String> visibleCombo = new ComboBox<>(FXCollections.observableArrayList("Visible", "Caché"));
        visibleCombo.getSelectionModel().select(
                coursToEdit == null || coursToEdit.getVisible() == 1 ? "Visible" : "Caché");

        TextField pdfField = new TextField(
                coursToEdit != null && coursToEdit.getFichierContenu() != null ? coursToEdit.getFichierContenu() : "");
        pdfField.setPromptText("Chemin ou URL du PDF");
        pdfField.setEditable(false);
        Button browsePdf = new Button("📂 Parcourir");
        browsePdf.setStyle("-fx-background-color:#e2e8f0; -fx-background-radius:6; -fx-padding:5 10;");
        browsePdf.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
            File f = fc.showOpenDialog(coursContainer.getScene().getWindow());
            if (f != null) {
                if (f.length() > 10 * 1024 * 1024) {
                    new Alert(Alert.AlertType.WARNING, "Fichier trop grand (max 10MB)").showAndWait();
                } else {
                    pdfField.setText(f.getAbsolutePath());
                }
            }
        });
        HBox pdfRow = new HBox(8, pdfField, browsePdf);
        HBox.setHgrow(pdfField, Priority.ALWAYS);

        Label titreErr = new Label();
        titreErr.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11;");
        Label dureeErr = new Label();
        dureeErr.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11;");

        form.add(new Label("Titre *"), 0, 0); form.add(titreField, 1, 0);
        form.add(titreErr, 1, 1);
        form.add(new Label("Description"), 0, 2); form.add(descField, 1, 2);
        form.add(new Label("Contenu"), 0, 3); form.add(contenuField, 1, 3);
        form.add(new Label("Durée (min) *"), 0, 4); form.add(dureeField, 1, 4);
        form.add(dureeErr, 1, 5);
        form.add(new Label("Ordre"), 0, 6); form.add(ordreField, 1, 6);
        form.add(new Label("Visibilité"), 0, 7); form.add(visibleCombo, 1, 7);
        form.add(new Label("Fichier PDF"), 0, 8); form.add(pdfRow, 1, 8);

        dlg.getDialogPane().setContent(form);

        // Disable OK until validation
        Button okButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Enregistrer");
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            boolean valid = true;
            titreErr.setText("");
            dureeErr.setText("");

            String titre = titreField.getText() == null ? "" : titreField.getText().trim();
            if (titre.isEmpty()) {
                titreErr.setText("⚠️ Le titre est obligatoire.");
                valid = false;
            } else if (titre.length() < 3) {
                titreErr.setText("⚠️ Minimum 3 caractères.");
                valid = false;
            }

            String dureeStr = dureeField.getText() == null ? "" : dureeField.getText().trim();
            if (dureeStr.isEmpty()) {
                dureeErr.setText("⚠️ La durée est obligatoire.");
                valid = false;
            } else {
                try {
                    int d = Integer.parseInt(dureeStr);
                    if (d <= 0) { dureeErr.setText("⚠️ Durée > 0."); valid = false; }
                } catch (NumberFormatException ex) {
                    dureeErr.setText("⚠️ Entier requis.");
                    valid = false;
                }
            }

            if (!valid) e.consume();
        });

        Optional<ButtonType> result = dlg.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String titre = titreField.getText().trim();
            String desc = descField.getText().trim();
            String contenu = contenuField.getText().trim();
            int duree = Integer.parseInt(dureeField.getText().trim());
            int ordre;
            try { ordre = Integer.parseInt(ordreField.getText().trim()); } catch (NumberFormatException ex) { ordre = 1; }
            int visible = "Visible".equals(visibleCombo.getValue()) ? 1 : 0;
            String pdfPath = pdfField.getText().trim();

            if (coursToEdit == null) {
                cours newCours = new cours(titre,
                        desc.isEmpty() ? null : desc,
                        contenu.isEmpty() ? null : contenu,
                        duree, ordre,
                        new Timestamp(System.currentTimeMillis()),
                        1, currentModule.getId(),
                        pdfPath.isEmpty() ? null : pdfPath,
                        0, visible);
                coursController.ajouterCours(newCours);
            } else {
                coursToEdit.setTitre(titre);
                coursToEdit.setDescription(desc.isEmpty() ? null : desc);
                coursToEdit.setContenu(contenu.isEmpty() ? null : contenu);
                coursToEdit.setDuree(duree);
                coursToEdit.setOrdre(ordre);
                coursToEdit.setVisible(visible);
                coursToEdit.setFichierContenu(pdfPath.isEmpty() ? null : pdfPath);
                coursController.modifierCours(coursToEdit);
            }
            loadData();
        }
    }

    // ─── FILTERS / SORT ──────────────────────────────────────────────────────────

    @FXML
    public void handleSearch() { applyFilters(); }

    @FXML
    public void handleFilterVisible() { applyFilters(); }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String vis = filterVisible.getValue();
        List<cours> filtered = allCours.stream().filter(c -> {
            boolean matchQ = q.isEmpty()
                    || c.getTitre().toLowerCase().contains(q)
                    || (c.getDescription() != null && c.getDescription().toLowerCase().contains(q));
            boolean matchVis = "Tous".equals(vis) || vis == null
                    || ("Visible".equals(vis) && c.getVisible() == 1)
                    || ("Caché".equals(vis) && c.getVisible() == 0);
            return matchQ && matchVis;
        }).collect(Collectors.toList());
        renderCards(filtered);
        countLabel.setText(filtered.size() + " cours");
    }

    @FXML public void sortAZ() {
        allCours.sort(Comparator.comparing(cours::getTitre, String.CASE_INSENSITIVE_ORDER));
        applyFilters();
    }
    @FXML public void sortByOrdre() {
        allCours.sort(Comparator.comparingInt(cours::getOrdre));
        applyFilters();
    }
    @FXML public void sortByDuree() {
        allCours.sort(Comparator.comparingInt(cours::getDuree));
        applyFilters();
    }

    // ─── NAVIGATION ──────────────────────────────────────────────────────────────

    @FXML
    public void handleBackToModules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_CoursModules.fxml"));
            Parent root = loader.load();
            FrontCoursModulesController ctrl = loader.getController();
            ctrl.initData(currentUser, currentCategorie);
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleCoursCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_CoursCategories.fxml"));
            Parent root = loader.load();
            FrontCoursCategorieController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void handleHome() { navigateTo("/tn/esprit/view/front_user_dashboard.fxml"); }
    @FXML public void handleJeux() { navigateTo("/tn/esprit/view/front_GameList.fxml"); }
    @FXML public void handleLogout() { navigateTo("/tn/esprit/view/front_login.fxml"); }
    @FXML public void handleProfile() { navigateTo("/tn/esprit/view/front_profile.fxml"); }

    private void navigateTo(String fxml) {
        try {
            if ("/tn/esprit/view/front_user_dashboard.fxml".equals(fxml) && currentUser != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();
                FrontUserDashboardController ctrl = loader.getController();
                ctrl.initUser(currentUser);
                Stage stage = (Stage) coursContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
                return;
            }
            if ("/tn/esprit/view/front_profile.fxml".equals(fxml) && currentUser != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();
                FrontProfileController ctrl = loader.getController();
                ctrl.initUser(currentUser);
                Stage stage = (Stage) coursContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
                return;
            }
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
