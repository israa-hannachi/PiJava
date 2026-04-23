package tn.esprit.controllers.front;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.entities.cours.Cours;
import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.entities.cours.Cours_Module;
import tn.esprit.entities.users.Users;
import tn.esprit.services.cours.FavorisCours;
import tn.esprit.services.cours.GeminiService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Contrôleur front-office — liste des cours d'un module.
 *
 * Fonctionnalités :
 *   - Affichage en cartes (FlowPane)
 *   - Recherche / filtre visibilité
 *   - Tri (titre, ordre, durée)
 *   - ⭐ Favoris (toggle + onglet "Mes Favoris")
 *   - ▶ Parcours séquentiel (lancer le parcours depuis n'importe quel cours)
 *   - CRUD inline pour l'Enseignant
 */
public class FrontCoursListController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────────
    @FXML private FlowPane         coursContainer;
    @FXML private FlowPane         favorisContainer;
    @FXML private TextField        searchField;
    @FXML private TextField        favSearchField;
    @FXML private ComboBox<String> filterVisible;
    @FXML private Label            moduleNomLabel;
    @FXML private Label            moduleNiveauLabel;
    @FXML private Label            breadcrumbLabel;
    @FXML private Label            totalCoursLabel;
    @FXML private Label            withPdfLabel;
    @FXML private Label            totalDureeLabel;
    @FXML private Label            countLabel;
    @FXML private Label            profBadge;
    @FXML private Label            favCountBadge;   // badge "X favori(s)"
    @FXML private Button           addCoursBtn;
    @FXML private Button           profileButton;
    @FXML private Button           backToModulesBtn;
    @FXML private Button           parcoursBtn;     // bouton "Démarrer le parcours"
    @FXML private Button           parcoursInteractiveBtn; // bouton parcours interactif
    @FXML private Button           exportPdfBtn;    // bouton export PDF
    @FXML private TabPane          tabPane;         // onglets "Cours" / "Mes Favoris"
    @FXML private VBox             emptyState;
    @FXML private VBox             emptyFavState;

    // ── Services ─────────────────────────────────────────────────────────────────
    private final CoursController coursController = new CoursController();
    private final FavorisCours    favoris         = FavorisCours.getInstance();

    // ── État ─────────────────────────────────────────────────────────────────────
    private Users          currentUser;
    private Cours_Categorie currentCategorie;
    private Cours_Module    currentModule;
    private List<Cours>     allCours   = new ArrayList<>();
    private boolean         isProf     = false;
    private int             userId     = -1;

    // TinyMCE configuration
    private static final String TINYMCE_CDN_URL =
        "https://cdnjs.cloudflare.com/ajax/libs/tinymce/6.8.3/tinymce.min.js";
    private WebEngine dialogWebEngine = null;

    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterVisible.setItems(FXCollections.observableArrayList("Tous", "Visible", "Caché"));
        filterVisible.getSelectionModel().selectFirst();
    }

    /** Point d'entrée — appelé depuis FrontCoursModulesController */
    public void initData(Users user, Cours_Categorie cat, Cours_Module mod) {
        this.currentUser     = user;
        this.currentCategorie = cat;
        this.currentModule   = mod;

        if (user != null) {
            profileButton.setText(user.getFirstName());
            userId = user.getId();
            String role = user.getRole();
            isProf = "Enseignant".equalsIgnoreCase(role);
        }

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
        List<Cours> rawCours = coursController.findByModuleId(currentModule.getId());

        if (isProf) {
            allCours = rawCours.stream()
                .filter(c -> c.getActif() == 1)
                .sorted(Comparator.comparingInt(Cours::getOrdre))
                .collect(Collectors.toList());
        } else {
            allCours = rawCours.stream()
                .filter(c -> c.getVisible() == 1)
                .sorted(Comparator.comparingInt(Cours::getOrdre))
                .collect(Collectors.toList());
        }

        updateStats(allCours);
        renderCards(allCours);

        // Cacher les fonctionnalités réservées aux étudiants si l'utilisateur n'est pas étudiant
        boolean isStudent = isEtudiant();

        // Onglet favoris
        if (tabPane != null) {
            Tab favorisTab = null;
            for (Tab tab : tabPane.getTabs()) {
                if ("Mes Favoris".equals(tab.getText())) {
                    favorisTab = tab;
                    break;
                }
            }
            if (favorisTab != null) {
                // désactiver l'onglet si ce n'est pas un étudiant
                favorisTab.setDisable(!isStudent);
            }
        }

        // Afficher les favoris seulement si étudiant
        if (isStudent) {
            renderFavoris();
        }

        // Afficher boutons parcours si au moins 1 cours (visibles pour tous)
        boolean hasCourses = !allCours.isEmpty();
        parcoursBtn.setVisible(hasCourses);
        parcoursBtn.setManaged(hasCourses);
        parcoursInteractiveBtn.setVisible(hasCourses);
        parcoursInteractiveBtn.setManaged(hasCourses);

        // Activer uniquement pour les étudiants
        parcoursBtn.setDisable(!isStudent);
        parcoursInteractiveBtn.setDisable(!isStudent);
    }

    // ── Stats ─────────────────────────────────────────────────────────────────────
    private void updateStats(List<Cours> list) {
        totalCoursLabel.setText(String.valueOf(list.size()));
        withPdfLabel.setText(String.valueOf(
            list.stream().filter(c -> c.getFichierContenu() != null
                && !c.getFichierContenu().isEmpty()).count()));
        long totalMin = list.stream().mapToLong(Cours::getDuree).sum();
        totalDureeLabel.setText(String.valueOf(totalMin));
    }

    // ── Rendu cartes ─────────────────────────────────────────────────────────────
    private void renderCards(List<Cours> list) {
        coursContainer.getChildren().clear();
        boolean empty = list.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        countLabel.setText(list.size() + " cours");

        for (Cours c : list) {
            coursContainer.getChildren().add(buildCoursCard(c, false));
        }
    }

    private void renderFavoris() {
        if (favorisContainer == null) return;
        favorisContainer.getChildren().clear();

        List<Cours> favList = favoris.getFavoris(userId, allCours);
        boolean empty = favList.isEmpty();
        if (emptyFavState != null) {
            emptyFavState.setVisible(empty);
            emptyFavState.setManaged(empty);
        }
        favCountBadge.setText(favList.size() + " favori(s)");

        for (Cours c : favList) {
            favorisContainer.getChildren().add(buildCoursCard(c, true));
        }
    }

    /**
     * Construit une carte pour un cours.
     * @param isFavTab  si true = on est dans l'onglet favoris (pas de bouton favori redondant)
     */
    private VBox buildCoursCard(Cours c, boolean isFavTab) {
        boolean hasPdf   = c.getFichierContenu() != null && !c.getFichierContenu().isEmpty();
        boolean isFavori = favoris.estFavori(userId, c.getId());

        VBox card = new VBox(10);
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
            "-fx-border-color:#e5e7eb; -fx-border-radius:16; " +
            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),8,0,0,2);");

        // ── Ligne haut : ordre + durée + ⭐ ──────────────────────────────────────
        HBox topRow = new HBox(8);
        topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label ordreLabel = new Label("#" + c.getOrdre());
        ordreLabel.setStyle("-fx-background-color:#f0f9ff; -fx-text-fill:#0369a1; " +
            "-fx-background-radius:6; -fx-padding:2 8; -fx-font-weight:700; -fx-font-size:11;");

        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        Label dureeLabel = new Label("⏱ " + c.getDuree() + " min");
        dureeLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        // Bouton favori ⭐
        Button favBtn = new Button(isFavori ? "⭐" : "☆");
        favBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:" +
            (isFavori ? "#f59e0b" : "#94a3b8") + "; -fx-font-size:16; " +
            "-fx-cursor:hand; -fx-padding:0;");
        favBtn.setTooltip(new Tooltip(isFavori ? "Retirer des favoris" : "Ajouter aux favoris"));
        favBtn.setOnAction(e -> {
            // Vérifier si l'utilisateur est un étudiant
            if (!isEtudiant()) {
                showAlert("Accès refusé", "Les favoris sont réservés aux étudiants.");
                return;
            }
            boolean nowFav = favoris.toggle(userId, c);
            favBtn.setText(nowFav ? "⭐" : "☆");
            favBtn.setStyle("-fx-background-color:transparent; -fx-text-fill:" +
                (nowFav ? "#f59e0b" : "#94a3b8") + "; -fx-font-size:16; -fx-cursor:hand; -fx-padding:0;");
            renderFavoris();      // rafraîchir l'onglet favoris
        });

        topRow.getChildren().addAll(ordreLabel, sp, dureeLabel, favBtn);

        // ── Titre ────────────────────────────────────────────────────────────────
        Label titreLabel = new Label(c.getTitre());
        titreLabel.setStyle("-fx-font-size:17px; -fx-font-weight:800; -fx-text-fill:#1f2937; -fx-wrap-text:true;");
        titreLabel.setWrapText(true);

        // ── Description ──────────────────────────────────────────────────────────
        Label descLabel = new Label(c.getDescription() != null && !c.getDescription().isEmpty()
            ? c.getDescription() : "");
        descLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-wrap-text:true;");
        descLabel.setWrapText(true);

        // ── Badges PDF / Visible ──────────────────────────────────────────────────
        HBox badges = new HBox(6);
        if (hasPdf) {
            Label pdfBadge = new Label("📄 PDF");
            pdfBadge.setStyle("-fx-background-color:#eff6ff; -fx-text-fill:#2563eb; " +
                "-fx-background-radius:6; -fx-padding:2 8; -fx-font-size:11;");
            badges.getChildren().add(pdfBadge);
        }
        if (c.getVisible() == 1 && isProf) {
            Label visBadge = new Label("👁 Visible");
            visBadge.setStyle("-fx-background-color:#f0fdf4; -fx-text-fill:#166534; " +
                "-fx-background-radius:6; -fx-padding:2 8; -fx-font-size:11;");
            badges.getChildren().add(visBadge);
        }

        // ── Footer actions ───────────────────────────────────────────────────────
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

            // Bouton d'export PDF individuel
            Button exportContentBtn = new Button("Export PDF");
            exportContentBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; " +
                "-fx-background-radius:8; -fx-padding:5 12; -fx-font-size:12; -fx-cursor:hand;");
            exportContentBtn.setOnAction(e -> exportCoursContentPdf(c));
            footer.getChildren().add(exportContentBtn);
        }

        // Bouton ▶ Parcours à partir de ce cours
        Button startBtn = new Button("▶ Parcourir");
        startBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-font-weight:700; " +
            "-fx-background-radius:8; -fx-padding:5 12; -fx-font-size:12; -fx-cursor:hand;");
        startBtn.setOnAction(e -> lancerParcours(c));
        footer.getChildren().add(startBtn);

        // Enseignant : modifier / supprimer
        if (isProf) {
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            footer.getChildren().add(spacer);

            Button editBtn = new Button("✏️");
            editBtn.setStyle("-fx-background-color:#fef9c3; -fx-text-fill:#92400e; " +
                "-fx-background-radius:8; -fx-padding:5 10; -fx-font-size:12; -fx-cursor:hand;");
            editBtn.setTooltip(new Tooltip("Modifier"));
            editBtn.setOnAction(e -> showCoursFormDialog(c));

            Button delBtn = new Button("🗑️");
            delBtn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#dc2626; " +
                "-fx-background-radius:8; -fx-padding:5 10; -fx-font-size:12; -fx-cursor:hand;");
            delBtn.setTooltip(new Tooltip("Supprimer"));
            delBtn.setOnAction(e -> handleDeleteCours(c));

            footer.getChildren().addAll(editBtn, delBtn);
        }

        card.getChildren().addAll(topRow, titreLabel);
        if (!descLabel.getText().isEmpty()) card.getChildren().add(descLabel);
        if (!badges.getChildren().isEmpty())  card.getChildren().add(badges);
        card.getChildren().add(footer);

        // Hover
        card.setOnMouseEntered(e -> card.setStyle(
            "-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
                "-fx-border-color:#0FB5A9; -fx-border-radius:16; " +
                "-fx-effect:dropshadow(three-pass-box,rgba(15,181,169,0.25),14,0,0,4);"));
        card.setOnMouseExited(e -> card.setStyle(
            "-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
                "-fx-border-color:#e5e7eb; -fx-border-radius:16; " +
                "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),8,0,0,2);"));

        return card;
    }

    // ── Parcours ─────────────────────────────────────────────────────────────────

    /** Lance le parcours à partir du premier cours du module. */
    @FXML
    public void handleDemarrerParcours() {
        // Vérifier si l'utilisateur est un étudiant
        if (!isEtudiant()) {
            showAlert("Accès refusé", "Les parcours d'apprentissage sont réservés aux étudiants.");
            return;
        }
        if (!allCours.isEmpty()) lancerParcours(allCours.get(0));
    }

    /** Lance le parcours interactif avec drag & drop. */
    @FXML
    public void handleParcoursInteractive() {
        // Vérifier si l'utilisateur est un étudiant
        if (!isEtudiant()) {
            showAlert("Accès refusé", "Les parcours d'apprentissage sont réservés aux étudiants.");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/tn/esprit/view/front_CoursParcoursInteractive.fxml"));
            Parent root = loader.load();
            FrontCoursParcoursInteractiveController ctrl = loader.getController();
            ctrl.initData(currentUser, currentCategorie, currentModule, allCours);
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /** Lance le parcours séquentiel à partir d'un cours spécifique. */
    private void lancerParcours(Cours departureCours) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/tn/esprit/view/front_CoursParcours.fxml"));
            Parent root = loader.load();
            FrontCoursParcoursController ctrl = loader.getController();
            ctrl.initParcours(currentUser, currentCategorie, currentModule,
                allCours, departureCours);
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ── Actions ──────────────────────────────────────────────────────────────────
    private void openPdf(Cours c) {
        String path = c.getFichierContenu();
        if (path == null || path.isEmpty()) return;
        try {
            if (path.startsWith("http")) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(path));
            } else {
                File f = new File(path);
                if (f.exists()) java.awt.Desktop.getDesktop().open(f);
                else new Alert(Alert.AlertType.WARNING, "Fichier introuvable : " + path).showAndWait();
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void showContent(Cours c) {
        Dialog<Void> dlg = new Dialog<>();
        dlg.setTitle("Lecture : " + c.getTitre());
        dlg.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dlg.getDialogPane().setPrefWidth(600);
        dlg.getDialogPane().setPrefHeight(500);

        VBox root = new VBox(15);
        root.setPadding(new javafx.geometry.Insets(20));

        // Contenu
        Label contentLabel = new Label("Contenu du cours");
        contentLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        
        String text = c.getContenu() != null ? c.getContenu().replaceAll("<[^>]+>", " ").replaceAll("\\s+", " ").trim() : "Aucun contenu.";
        TextArea ta = new TextArea(text);
        ta.setWrapText(true);
        ta.setEditable(false);
        ta.setPrefHeight(250);
        
        // Section Résumé IA
        VBox summaryBox = new VBox(10);
        summaryBox.setStyle("-fx-background-color: #f5f3ff; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #ddd6fe; -fx-border-radius: 10;");
        summaryBox.setVisible(c.getResumeAi() != null && !c.getResumeAi().isEmpty());
        summaryBox.setManaged(c.getResumeAi() != null && !c.getResumeAi().isEmpty());

        Label summaryTitle = new Label("✨ Résumé IA");
        summaryTitle.setStyle("-fx-font-weight: bold; -fx-text-fill: #7c3aed;");
        
        Label summaryText = new Label(c.getResumeAi());
        summaryText.setWrapText(true);
        summaryText.setStyle("-fx-text-fill: #4c1d95;");
        
        summaryBox.getChildren().addAll(summaryTitle, summaryText);

        Button genSummaryBtn = new Button("✨ Générer Résumé IA");
        genSummaryBtn.setStyle("-fx-background-color: #8b5cf6; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        
        // Si le résumé existe déjà, on change le texte du bouton pour "Recréer" ou on le cache
        if (c.getResumeAi() != null && !c.getResumeAi().isEmpty()) {
            genSummaryBtn.setText("✨ Actualiser le Résumé IA");
        }

        genSummaryBtn.setOnAction(e -> {
            if ((c.getContenu() == null || c.getContenu().trim().isEmpty()) && 
                (c.getFichierContenu() == null || c.getFichierContenu().trim().isEmpty())) {
                new Alert(Alert.AlertType.WARNING, "Aucun contenu (texte ou PDF) à résumer.").showAndWait();
                return;
            }

            genSummaryBtn.setDisable(true);
            genSummaryBtn.setText("⌛ Analyse en cours...");
            
            javafx.concurrent.Task<String> task = new javafx.concurrent.Task<>() {
                @Override
                protected String call() {
                    return GeminiService.getInstance().generateSummary(c.getContenu(), c.getFichierContenu());
                }
            };

            task.setOnSucceeded(ev -> {
                String result = task.getValue();
                c.setResumeAi(result);
                summaryText.setText(result);
                summaryBox.setVisible(true);
                summaryBox.setManaged(true);
                genSummaryBtn.setDisable(false);
                genSummaryBtn.setText("✨ Actualiser le Résumé IA");
                
                // Sauvegarde asynchrone
                new Thread(() -> coursController.modifierCours(c)).start();
            });

            task.setOnFailed(ev -> {
                genSummaryBtn.setDisable(false);
                genSummaryBtn.setText("✨ Générer Résumé IA");
                new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du résumé.").showAndWait();
            });

            new Thread(task).start();
        });

        root.getChildren().addAll(contentLabel, ta, genSummaryBtn, summaryBox);
        dlg.getDialogPane().setContent(root);
        dlg.showAndWait();
    }

    // ── CRUD enseignant ──────────────────────────────────────────────────────────
    @FXML
    public void handleAddCours() { showCoursFormDialog(null); }

    private void handleDeleteCours(Cours c) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation"); confirm.setHeaderText("Supprimer le cours");
        confirm.setContentText("Supprimer \"" + c.getTitre() + "\" définitivement ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            coursController.supprimerCours(c.getId());
            loadData();
        }
    }

    private void showCoursFormDialog(Cours coursToEdit) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(coursToEdit == null ? "Ajouter un cours" : "Modifier le cours");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setPrefWidth(520);

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(10);

        TextField titreField  = new TextField(coursToEdit != null ? coursToEdit.getTitre() : "");
        titreField.setPromptText("Titre du cours");

        TextArea descField = new TextArea(coursToEdit != null && coursToEdit.getDescription() != null
            ? coursToEdit.getDescription() : "");
        descField.setPromptText("Description"); descField.setPrefRowCount(2);

        WebView contenuWebView = new WebView();
        contenuWebView.setPrefHeight(300);
        String initialContent = coursToEdit != null && coursToEdit.getContenu() != null
            ? coursToEdit.getContenu() : "";

        // Initialize TinyMCE after the dialog is shown
        javafx.application.Platform.runLater(() -> {
            initDialogTinyMCE(contenuWebView, initialContent);
            if (coursToEdit != null && coursToEdit.getContenu() != null) {
                setDialogTinyMCEContent(coursToEdit.getContenu());
            }
        });

        TextField dureeField  = new TextField(coursToEdit != null ? String.valueOf(coursToEdit.getDuree()) : "");
        dureeField.setPromptText("Durée (minutes)");

        TextField ordreField  = new TextField(coursToEdit != null ? String.valueOf(coursToEdit.getOrdre()) : "1");
        ordreField.setPromptText("Ordre");

        ComboBox<String> visCombo = new ComboBox<>(FXCollections.observableArrayList("Visible", "Caché"));
        visCombo.getSelectionModel().select(
            coursToEdit == null || coursToEdit.getVisible() == 1 ? "Visible" : "Caché");

        TextField pdfField = new TextField(
            coursToEdit != null && coursToEdit.getFichierContenu() != null
                ? coursToEdit.getFichierContenu() : "");
        pdfField.setPromptText("Chemin ou URL du PDF"); pdfField.setEditable(false);

        Button browsePdf = new Button("📂 Parcourir");
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

        Label titreErr = new Label(); titreErr.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11;");
        Label dureeErr = new Label(); dureeErr.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11;");

        form.add(new Label("Titre *"),       0, 0); form.add(titreField,  1, 0);
        form.add(titreErr,                   1, 1);
        form.add(new Label("Description"),   0, 2); form.add(descField,   1, 2);
        form.add(new Label("Contenu *"),     0, 3); form.add(contenuWebView, 1, 3);
        form.add(new Label("Durée (min) *"), 0, 4); form.add(dureeField,  1, 4);
        form.add(dureeErr,                   1, 5);
        form.add(new Label("Ordre"),          0, 6); form.add(ordreField,  1, 6);
        form.add(new Label("Visibilité"),     0, 7); form.add(visCombo,    1, 7);
        form.add(new Label("Fichier PDF"),    0, 8); form.add(pdfRow,      1, 8);

        dlg.getDialogPane().setContent(form);

        Button okBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Enregistrer");
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            boolean valid = true;
            titreErr.setText(""); dureeErr.setText("");
            String titre = titreField.getText() == null ? "" : titreField.getText().trim();
            if (titre.isEmpty()) { titreErr.setText("⚠️ Obligatoire."); valid = false; }
            else if (titre.length() < 3) { titreErr.setText("⚠️ Minimum 3 caractères."); valid = false; }
            try {
                int d = Integer.parseInt(dureeField.getText().trim());
                if (d <= 0) { dureeErr.setText("⚠️ Durée > 0."); valid = false; }
            } catch (NumberFormatException ex) { dureeErr.setText("⚠️ Entier requis."); valid = false; }
            if (!valid) e.consume();
        });

        Optional<ButtonType> result = dlg.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String titre  = titreField.getText().trim();
            String desc   = descField.getText().trim();
            String contenu = getDialogTinyMCEContent().trim();
            int duree     = Integer.parseInt(dureeField.getText().trim());
            int ordre;
            try { ordre = Integer.parseInt(ordreField.getText().trim()); } catch (NumberFormatException ex) { ordre = 1; }
            int visible   = "Visible".equals(visCombo.getValue()) ? 1 : 0;
            String pdfPath = pdfField.getText().trim();

            if (coursToEdit == null) {
                Cours nc = new Cours(titre,
                    desc.isEmpty()   ? null : desc,
                    contenu.isEmpty() ? null : contenu,
                    duree, ordre,
                    new Timestamp(System.currentTimeMillis()),
                    1, currentModule.getId(),
                    pdfPath.isEmpty() ? null : pdfPath, 0, visible);
                coursController.ajouterCours(nc);
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

    // ── Filtres / tri ─────────────────────────────────────────────────────────────
    @FXML public void handleSearch()        { applyFilters(); }
    @FXML public void handleFilterVisible() { applyFilters(); }

    private void applyFilters() {
        String q   = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String vis = filterVisible.getValue();
        List<Cours> filtered = allCours.stream().filter(c -> {
            boolean matchQ   = q.isEmpty()
                || c.getTitre().toLowerCase().contains(q)
                || (c.getDescription() != null && c.getDescription().toLowerCase().contains(q));
            boolean matchVis = "Tous".equals(vis) || vis == null
                || ("Visible".equals(vis) && c.getVisible() == 1)
                || ("Caché".equals(vis)   && c.getVisible() == 0);
            return matchQ && matchVis;
        }).collect(Collectors.toList());
        renderCards(filtered);
        countLabel.setText(filtered.size() + " cours");
    }

    @FXML public void sortAZ()       { allCours.sort(Comparator.comparing(Cours::getTitre, String.CASE_INSENSITIVE_ORDER)); applyFilters(); }
    @FXML public void sortZA()       { allCours.sort(Comparator.comparing(Cours::getTitre, String.CASE_INSENSITIVE_ORDER).reversed()); applyFilters(); }
    @FXML public void sortByOrdreAsc()  { allCours.sort(Comparator.comparingInt(Cours::getOrdre));  applyFilters(); }
    @FXML public void sortByOrdreDesc()  { allCours.sort(Comparator.comparingInt(Cours::getOrdre).reversed());  applyFilters(); }
    @FXML public void sortByDureeAsc()  { allCours.sort(Comparator.comparingInt(Cours::getDuree));  applyFilters(); }
    @FXML public void sortByDureeDesc()  { allCours.sort(Comparator.comparingInt(Cours::getDuree).reversed());  applyFilters(); }

    // Garder les anciennes méthodes pour compatibilité
    @FXML public void sortByOrdre()  { sortByOrdreAsc(); }
    @FXML public void sortByDuree()  { sortByDureeAsc(); }

    // Export PDF individuel d'un cours
    private void exportCoursContentPdf(Cours cours) {
        try {
            // Créer un fichier PDF pour ce cours spécifique
            String fileName = "cours_" + cours.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + "_" +
                System.currentTimeMillis() + ".pdf";
            java.io.File destFile = new java.io.File(System.getProperty("user.home"), fileName);

            // Utiliser un service d'export simple pour un seul cours
            exportSingleCoursePdf(destFile, cours);

            // Succès
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export réussi");
            success.setHeaderText("PDF du cours généré");
            success.setContentText("Le cours \"" + cours.getTitre() + "\" a été exporté vers : " + destFile.getAbsolutePath());
            success.showAndWait();

            // Ouvrir le fichier PDF
            try {
                if (destFile.exists()) {
                    java.awt.Desktop.getDesktop().open(destFile);
                }
            } catch (Exception e) {
                System.err.println("Impossible d'ouvrir le PDF : " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'export PDF du cours : " + e.getMessage()).showAndWait();
        }
    }

    private void exportSingleCoursePdf(java.io.File destFile, Cours cours) throws Exception {
        com.itextpdf.text.Document doc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(destFile));

        // En-tête
        writer.setPageEvent(new SimpleHeaderFooterEvent());
        doc.open();

        // Titre du cours
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD,
            new com.itextpdf.text.BaseColor(15, 181, 169));
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(cours.getTitre(), titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        doc.add(title);

        // Informations du cours
        com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);

        com.itextpdf.text.Paragraph info = new com.itextpdf.text.Paragraph();
        info.add(new com.itextpdf.text.Chunk("Module : ", new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        info.add(new com.itextpdf.text.Chunk(currentModule.getTitre() + "\n", infoFont));
        info.add(new com.itextpdf.text.Chunk("Durée : ", new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        info.add(new com.itextpdf.text.Chunk(cours.getDuree() + " minutes\n", infoFont));
        info.add(new com.itextpdf.text.Chunk("Ordre : ", new com.itextpdf.text.Font(
            com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        info.add(new com.itextpdf.text.Chunk("#" + cours.getOrdre(), infoFont));
        info.setSpacingAfter(20);
        doc.add(info);

        // Description
        if (cours.getDescription() != null && !cours.getDescription().isEmpty()) {
            com.itextpdf.text.Font descTitleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph descTitle = new com.itextpdf.text.Paragraph("Description", descTitleFont);
            descTitle.setSpacingAfter(10);
            doc.add(descTitle);

            com.itextpdf.text.Font descFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Paragraph desc = new com.itextpdf.text.Paragraph(cours.getDescription(), descFont);
            desc.setSpacingAfter(20);
            doc.add(desc);
        }

        // Contenu du cours
        if (cours.getContenu() != null && !cours.getContenu().isEmpty()) {
            com.itextpdf.text.Font contentTitleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph contentTitle = new com.itextpdf.text.Paragraph("Contenu du cours", contentTitleFont);
            contentTitle.setSpacingAfter(10);
            doc.add(contentTitle);

            // Nettoyer le HTML et ajouter le contenu
            String cleanContent = cours.getContenu().replaceAll("<[^>]+>", " ");
            com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Paragraph content = new com.itextpdf.text.Paragraph(cleanContent, contentFont);
            content.setSpacingAfter(20);
            doc.add(content);
        }

        // Information PDF si disponible
        if (cours.getFichierContenu() != null && !cours.getFichierContenu().isEmpty()) {
            com.itextpdf.text.Font pdfTitleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph pdfTitle = new com.itextpdf.text.Paragraph("Ressource PDF", pdfTitleFont);
            pdfTitle.setSpacingAfter(10);
            doc.add(pdfTitle);

            com.itextpdf.text.Font pdfFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL,
                new com.itextpdf.text.BaseColor(37, 99, 235));
            String pdfInfo = cours.getFichierContenu().startsWith("http") ?
                "Disponible sur Cloudinary" : "Fichier local disponible";
            com.itextpdf.text.Paragraph pdf = new com.itextpdf.text.Paragraph(pdfInfo, pdfFont);
            pdf.setSpacingAfter(20);
            doc.add(pdf);
        }

        doc.close();
    }

    // Header/Footer simple pour les exports individuels
    private static class SimpleHeaderFooterEvent extends com.itextpdf.text.pdf.PdfPageEventHelper {
        @Override
        public void onEndPage(com.itextpdf.text.pdf.PdfWriter writer, com.itextpdf.text.Document document) {
            com.itextpdf.text.pdf.PdfContentByte cb = writer.getDirectContent();

            // Ligne de pied de page
            cb.setColorStroke(new com.itextpdf.text.BaseColor(226, 232, 240));
            cb.setLineWidth(0.5f);
            cb.moveTo(document.leftMargin(), document.bottomMargin() - 5);
            cb.lineTo(document.right(), document.bottomMargin() - 5);
            cb.stroke();

            // Texte pied de page
            try {
                com.itextpdf.text.pdf.BaseFont baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
                    com.itextpdf.text.pdf.BaseFont.HELVETICA, com.itextpdf.text.pdf.BaseFont.WINANSI,
                    com.itextpdf.text.pdf.BaseFont.EMBEDDED);

                cb.beginText();
                cb.setFontAndSize(baseFont, 8);
                cb.showTextAligned(com.itextpdf.text.Element.ALIGN_LEFT, "Naja7ni - Cours",
                    document.leftMargin(), document.bottomMargin() - 18, 0);
                cb.showTextAligned(com.itextpdf.text.Element.ALIGN_RIGHT, "Page " + writer.getPageNumber(),
                    document.right(), document.bottomMargin() - 18, 0);
                cb.endText();
            } catch (Exception e) {
                // Si le BaseFont échoue, on saute le footer
                System.err.println("Erreur BaseFont: " + e.getMessage());
            }
        }
    }

    // Export PDF
    @FXML public void handleExportPdf() {
        try {
            // Créer une liste des cours à exporter (cours filtrés actuels)
            List<Cours> coursToExport = new ArrayList<>();
            String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
            String vis = filterVisible.getValue();

            for (Cours c : allCours) {
                boolean matchQ = q.isEmpty()
                    || c.getTitre().toLowerCase().contains(q)
                    || (c.getDescription() != null && c.getDescription().toLowerCase().contains(q));
                boolean matchVis = "Tous".equals(vis) || vis == null
                    || ("Visible".equals(vis) && c.getVisible() == 1)
                    || ("Caché".equals(vis) && c.getVisible() == 0);
                if (matchQ && matchVis) {
                    coursToExport.add(c);
                }
            }

            if (coursToExport.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Aucun cours à exporter.").showAndWait();
                return;
            }

            // Créer un fichier PDF temporaire
            String fileName = "catalogue_" + currentModule.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + "_" +
                System.currentTimeMillis() + ".pdf";
            java.io.File destFile = new java.io.File(System.getProperty("user.home"), fileName);

            // Préparer les données pour l'export
            List<Cours_Categorie> categories = List.of(currentCategorie);
            List<Cours_Module> modules = List.of(currentModule);

            // Créer les maps nécessaires
            java.util.Map<Integer, String> moduleMap = new java.util.HashMap<>();
            moduleMap.put(currentModule.getId(), currentModule.getTitre());

            java.util.Map<Integer, String> catMap = new java.util.HashMap<>();
            catMap.put(currentCategorie.getId(), currentCategorie.getNom());

            // Utiliser PdfExportService
            tn.esprit.services.cours.PdfExportService.exportCatalogue(
                destFile, categories, modules, coursToExport, moduleMap, catMap);

            // Succès
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export réussi");
            success.setHeaderText("PDF généré avec succès");
            success.setContentText("Le catalogue a été exporté vers : " + destFile.getAbsolutePath());
            success.showAndWait();

            // Ouvrir le fichier PDF
            try {
                if (destFile.exists()) {
                    java.awt.Desktop.getDesktop().open(destFile);
                }
            } catch (Exception e) {
                System.err.println("Impossible d'ouvrir le PDF : " + e.getMessage());
            }

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'export PDF : " + e.getMessage()).showAndWait();
        }
    }

    // Recherche dans les favoris
    @FXML public void handleFavSearch() {
        String q = favSearchField.getText() == null ? "" : favSearchField.getText().toLowerCase().trim();
        List<Cours> allFavs = favoris.getFavoris(userId, allCours);
        List<Cours> filtered = allFavs.stream().filter(c -> {
            return q.isEmpty() || c.getTitre().toLowerCase().contains(q)
                || (c.getDescription() != null && c.getDescription().toLowerCase().contains(q));
        }).collect(Collectors.toList());
        renderFavorisList(filtered);
    }

    // Ajouter des favoris
    @FXML public void handleAddFavoris() {
        // Vérifier si l'utilisateur est un étudiant
        if (!isEtudiant()) {
            showAlert("Accès refusé", "Les favoris sont réservés aux étudiants.");
            return;
        }
        showAddFavorisDialog();
    }

    private void showAddFavorisDialog() {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle("Ajouter des cours aux favoris");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPrefWidth(500);

        Label title = new Label("Sélectionnez les cours à ajouter aux favoris :");
        title.setStyle("-fx-font-weight:700; -fx-font-size:14;");

        ListView<Cours> listView = new ListView<>();
        listView.setPrefHeight(300);
        listView.getItems().addAll(allCours.stream()
            .filter(c -> !favoris.estFavori(userId, c.getId()))
            .collect(Collectors.toList()));

        listView.setCellFactory(param -> new ListCell<Cours>() {
            @Override
            protected void updateItem(Cours c, boolean empty) {
                super.updateItem(c, empty);
                if (empty || c == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    CheckBox checkBox = new CheckBox();
                    Label label = new Label(c.getTitre() + " (" + c.getDuree() + " min)");
                    hbox.getChildren().addAll(checkBox, label);
                    setGraphic(hbox);

                    // Stocker la référence du checkbox
                    checkBox.setUserData(c);
                }
            }
        });

        content.getChildren().addAll(title, listView);
        dlg.getDialogPane().setContent(content);

        Optional<ButtonType> result = dlg.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // Ajouter les cours sélectionnés aux favoris
            for (Node node : listView.lookupAll(".check-box")) {
                CheckBox cb = (CheckBox) node;
                if (cb.isSelected()) {
                    Cours c = (Cours) cb.getUserData();
                    favoris.toggle(userId, c);
                }
            }
            renderFavoris();
            renderCards(allCours); // Rafraîchir les étoiles
        }
    }

    private void renderFavorisList(List<Cours> favList) {
        favorisContainer.getChildren().clear();
        boolean empty = favList.isEmpty();
        if (emptyFavState != null) {
            emptyFavState.setVisible(empty);
            emptyFavState.setManaged(empty);
        }

        for (Cours c : favList) {
            favorisContainer.getChildren().add(buildCoursCard(c, true));
        }
    }

    // ── Navigation ───────────────────────────────────────────────────────────────
    @FXML
    public void handleBackToModules() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_CoursModules.fxml"));
            Parent root = loader.load();
            FrontCoursModulesController ctrl = loader.getController();
            ctrl.initData(currentUser, currentCategorie);
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleCoursCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_CoursCategories.fxml"));
            Parent root = loader.load();
            ((FrontCoursCategorieController) loader.getController()).initUser(currentUser);
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML public void handleHome()    { navigateTo("/tn/esprit/view/front_user_dashboard.fxml"); }
    @FXML public void handleJeux()    { navigateTo("/tn/esprit/view/front_GameList.fxml"); }
    @FXML public void handleLogout()  { navigateTo("/tn/esprit/view/front_login.fxml"); }
    @FXML public void handleProfile() { navigateTo("/tn/esprit/view/front_profile.fxml"); }

    private void navigateTo(String fxml) {
        try {
            if ("/tn/esprit/view/front_user_dashboard.fxml".equals(fxml) && currentUser != null) {
                FXMLLoader l = new FXMLLoader(getClass().getResource(fxml));
                Parent r = l.load();
                ((FrontUserDashboardController) l.getController()).initUser(currentUser);
                Stage s = (Stage) coursContainer.getScene().getWindow();
                s.setScene(new Scene(r)); s.show(); return;
            }
            if ("/tn/esprit/view/front_profile.fxml".equals(fxml) && currentUser != null) {
                FXMLLoader l = new FXMLLoader(getClass().getResource(fxml));
                Parent r = l.load();
                ((FrontProfileController) l.getController()).initUser(currentUser);
                Stage s = (Stage) coursContainer.getScene().getWindow();
                s.setScene(new Scene(r)); s.show(); return;
            }
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) coursContainer.getScene().getWindow();
            stage.setScene(new Scene(root)); stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // Méthodes utilitaires pour le contrôle de rôle
    private boolean isEtudiant() {
        if (currentUser == null) return false;
        String role = currentUser.getRole();
        if (role == null) return false;
        String r = role.trim().toLowerCase();
        return r.equals("etudiant") || r.equals("student") || r.contains("etudi");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // ── TinyMCE Helper Methods ─────────────────────────────────────────────────────
    private String buildTinyMCEHtml(String initialContent) {
        return "<!DOCTYPE html>" +
            "<html lang='fr'>" +
            "<head>" +
            "<meta charset='UTF-8'>" +
            "<script src='" + TINYMCE_CDN_URL + "' referrerpolicy='origin'></script>" +
            "<style>" +
            "  body { font-family: Arial, sans-serif; font-size: 14px; margin: 10px; }" +
            "  table { border-collapse: collapse; width: 100%; }" +
            "  table, th, td { border: 1px solid #ddd; padding: 8px; }" +
            "</style>" +
            "</head>" +
            "<body>" +
            "<textarea id='tinyMCEEditor'>" + (initialContent != null ? initialContent : "") + "</textarea>" +
            "<script>" +
            "tinymce.init({" +
            "  selector: '#tinyMCEEditor'," +
            "  height: 280," +
            "  menubar: false," +
            "  statusbar: true," +
            "  plugins: [" +
            "    'advlist autolink lists link image charmap print preview anchor'," +
            "    'searchreplace visualblocks code fullscreen'," +
            "    'insertdatetime media table paste code help wordcount'" +
            "  ]," +
            "  toolbar1: 'undo redo | formatselect | bold italic underline strikethrough | " +
            "            alignleft aligncenter alignright alignjustify | " +
            "            bullist numlist outdent indent | " +
            "            forecolor backcolor removeformat | " +
            "            fontsizeselect fontselect | " +
            "            link image media table | code fullscreen help'," +
            "  font_formats: " +
            "    'Andale Mono=andale mono,times; Arial=arial,helvetica,sans-serif; " +
            "    Arial Black=arial black,avant garde; Book Antiqua=book antiqua,palatino; " +
            "    Comic Sans MS=comic sans ms,sans-serif; Courier New=courier new,courier; " +
            "    Georgia=georgia,palatino; Helvetica=helvetica; Impact=impact,chicago; " +
            "    Symbol=symbol; Tahoma=tahoma,arial,helvetica,sans-serif; " +
            "    Terminal=terminal,monaco; Times New Roman=times new roman,times; " +
            "    Trebuchet MS=trebuchet ms,geneva; Verdana=verdana,geneva; " +
            "    Webdings=webdings; Wingdings=wingdings,zapf dingbats'," +
            "  fontsize_formats: '8pt 10pt 12pt 14pt 18pt 24pt 36pt'," +
            "  color_cols: 8," +
            "  color_map: [" +
            "    '#000000', 'Black'," +
            "    '#FF0000', 'Red'," +
            "    '#00FF00', 'Green'," +
            "    '#0000FF', 'Blue'," +
            "    '#FFFF00', 'Yellow'," +
            "    '#FF00FF', 'Magenta'," +
            "    '#00FFFF', 'Cyan'," +
            "    '#FFFFFF', 'White'," +
            "    '#808080', 'Gray'," +
            "    '#FFA500', 'Orange'," +
            "    '#800080', 'Purple'," +
            "    '#FFC0CB', 'Pink'," +
            "    '#A52A2A', 'Brown'," +
            "    '#808000', 'Olive'," +
            "    '#008080', 'Teal'," +
            "    '#000080', 'Navy'" +
            "  ]," +
            "  image_advtab: true," +
            "  image_uploadtab: true," +
            "  images_upload_url: 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg=='," +
            "  images_upload_handler: function (blobInfo, success, failure) {" +
            "    setTimeout(function() {" +
            "      var reader = new FileReader();" +
            "      reader.onload = function() {" +
            "        success(this.result);" +
            "      };" +
            "      reader.readAsDataURL(blobInfo.blob());" +
            "    }, 1000);" +
            "  }," +
            "  content_style: 'body { font-family: Arial, sans-serif; font-size: 14px; margin: 10px; }'," +
            "  branding: false," +
            "  relative_urls: false," +
            "  remove_script_host: false," +
            "  convert_urls: false" +
            "});" +
            "</script>" +
            "</body>" +
            "</html>";
    }

    private void initDialogTinyMCE(WebView webView, String initialContent) {
        if (webView == null) {
            System.err.println("WebView is null - TinyMCE cannot be initialized");
            return;
        }
        dialogWebEngine = webView.getEngine();
        dialogWebEngine.setJavaScriptEnabled(true);

        String html = buildTinyMCEHtml(initialContent);
        dialogWebEngine.loadContent(html);

        dialogWebEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                System.out.println("Dialog TinyMCE loaded successfully");
            } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("Dialog TinyMCE load failed: " + dialogWebEngine.getLoadWorker().getException());
            }
        });
    }

    private String getDialogTinyMCEContent() {
        if (dialogWebEngine == null) {
            System.err.println("Dialog WebEngine is null - cannot get TinyMCE content");
            return "";
        }
        try {
            Object value = dialogWebEngine.executeScript(
                "(function(){" +
                    "try{" +
                    "if(typeof tinymce!=='undefined'){" +
                    "var ed = tinymce.get('tinyMCEEditor') || tinymce.activeEditor || (tinymce.editors && tinymce.editors[0] ? tinymce.editors[0] : null);" +
                    "if(ed){ return ed.getContent(); }" +
                    "}" +
                    "}catch(e){}" +
                    "var ta=document.getElementById('tinyMCEEditor');" +
                    "return ta ? ta.value : '';" +
                    "})()"
            );
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            System.err.println("getDialogTinyMCEContent error: " + e.getMessage());
            return "";
        }
    }

    private void setDialogTinyMCEContent(String htmlContent) {
        if (dialogWebEngine == null) {
            System.err.println("Dialog WebEngine is null - cannot set TinyMCE content");
            return;
        }

        javafx.application.Platform.runLater(() -> {
            try {
                Thread.sleep(500); // Attendre l'initialisation

                String escaped = htmlContent
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "")
                    .replace("\"", "\\\"");

                Object checkResult = dialogWebEngine.executeScript(
                    "typeof tinymce !== 'undefined' && tinymce.editors && tinymce.editors.length > 0"
                );

                if (Boolean.TRUE.equals(checkResult)) {
                    dialogWebEngine.executeScript(
                        "tinymce.editors[0].setContent('" + escaped + "');"
                    );
                    System.out.println("Dialog TinyMCE content set successfully");
                } else {
                    dialogWebEngine.executeScript(
                        "var textarea = document.getElementById('tinyMCEEditor'); if (textarea) { textarea.value = '" + escaped + "'; }"
                    );
                    System.out.println("Dialog fallback: TinyMCE textarea content set");
                }
            } catch (Exception e) {
                System.err.println("setDialogTinyMCEContent error: " + e.getMessage());
            }
        });
    }
}
