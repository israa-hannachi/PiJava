package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.entities.cours.Cours;
import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.entities.cours.Cours_Module;
import tn.esprit.entities.users.Users;
import tn.esprit.services.cours.FavorisCours;
import tn.esprit.services.cours.GeminiService;
import tn.esprit.services.cours.SummaryService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur du parcours séquentiel de cours.
 * <p>
 * Fonctionnalités :
 *   - Navigation Précédent / Suivant
 *   - Affichage du contenu HTML dans un WebView
 *   - Affichage du PDF (lien Cloudinary ou local)
 *   - Barre de progression du parcours
 *   - Marquage ⭐ favori depuis le parcours
 *   - Retour à la liste des cours
 */
public class FrontCoursParcoursController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────────
    @FXML private Label    titreCoursLabel;
    @FXML private Label    moduleLabel;
    @FXML private Label    progressLabel;       // "Cours 2 / 7"
    @FXML private ProgressBar progressBar;
    @FXML private Label    descriptionLabel;
    @FXML private Label    dureeLabel;
    @FXML private Label    ordreLabel;
    @FXML private WebView  contenuWebView;      // contenu HTML
    @FXML private HBox     pdfSection;          // section PDF
    @FXML private Button   voirPdfBtn;
    @FXML private Label    pdfUrlLabel;
    @FXML private Button   prevBtn;
    @FXML private Button   nextBtn;
    @FXML private Button   favBtn;
    @FXML private VBox     courseNavContainer;  // mini-sommaire lateral
    @FXML private ScrollPane navScrollPane;

    @FXML private Button   aiSummaryBtn;
    @FXML private VBox     aiSummarySection;
    @FXML private Label    aiSummaryLabel;

    // ── État ─────────────────────────────────────────────────────────────────────
    private Users           currentUser;
    private Cours_Categorie currentCategorie;
    private Cours_Module    currentModule;
    private List<Cours>     coursList;
    private int             currentIndex = 0;
    private int             userId       = -1;

    private final FavorisCours favoris = FavorisCours.getInstance();
    private final SummaryService summaryService = SummaryService.getInstance();
    private final CoursController coursController = new CoursController();

    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // rien — on attend initParcours()
    }

    /**
     * Point d'entrée — appelé depuis FrontCoursListController.
     *
     * @param user            utilisateur connecté
     * @param cat             catégorie de cours
     * @param mod             module courant
     * @param list            liste complète des cours du module
     * @param departureCours  cours depuis lequel démarre le parcours
     */
    public void initParcours(Users user, Cours_Categorie cat, Cours_Module mod,
                             List<Cours> list, Cours departureCours) {
        this.currentUser      = user;
        this.currentCategorie = cat;
        this.currentModule    = mod;
        this.coursList        = list;
        this.userId           = user != null ? user.getId() : -1;

        // Trouver l'index du cours de départ
        currentIndex = 0;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getId() == departureCours.getId()) {
                currentIndex = i;
                break;
            }
        }

        buildNavSommaire();
        displayCours(currentIndex);
    }

    // ── Affichage d'un cours ──────────────────────────────────────────────────────
    private void displayCours(int index) {
        Cours c = coursList.get(index);

        // Titre & infos
        titreCoursLabel.setText(c.getTitre());
        moduleLabel.setText("📦 " + currentModule.getTitre() + "  ›  " + currentCategorie.getNom());
        ordreLabel.setText("Cours #" + c.getOrdre());
        dureeLabel.setText("⏱ " + c.getDuree() + " min");

        // Description
        descriptionLabel.setText(c.getDescription() != null && !c.getDescription().isEmpty()
            ? c.getDescription() : "");

        // Barre de progression
        double progress = coursList.isEmpty() ? 0 : (double)(index + 1) / coursList.size();
        progressBar.setProgress(progress);
        progressLabel.setText("Cours " + (index + 1) + " / " + coursList.size());

        // Contenu HTML dans WebView
        WebEngine engine = contenuWebView.getEngine();
        if (c.getContenu() != null && !c.getContenu().isEmpty()) {
            String html = "<html><head><style>" +
                "body { font-family: 'Segoe UI', sans-serif; font-size:14px; " +
                "color:#1f2937; background:#fff; padding:16px; line-height:1.6; }" +
                "h1,h2,h3 { color:#0FB5A9; }" +
                "table { border-collapse:collapse; width:100%; }" +
                "td,th { border:1px solid #e2e8f0; padding:8px; }" +
                "img { max-width:100%; border-radius:8px; }" +
                "</style></head><body>" + c.getContenu() + "</body></html>";
            engine.loadContent(html);
        } else {
            engine.loadContent("<html><body style='color:#94a3b8; font-family:Segoe UI; padding:20px;'>" +
                "<p>Aucun contenu texte pour ce cours.</p></body></html>");
        }

        // Section PDF
        String fichier = c.getFichierContenu();
        boolean hasPdf = fichier != null && !fichier.isEmpty();
        pdfSection.setVisible(hasPdf);
        pdfSection.setManaged(hasPdf);
        if (hasPdf) {
            // Afficher l'URL ou le nom du fichier
            String displayPath = fichier.startsWith("https://res.cloudinary.com")
                ? "☁️ Fichier Cloudinary" : "📄 " + new java.io.File(fichier).getName();
            pdfUrlLabel.setText(displayPath);
        }

        // Reset AI summary view
        aiSummarySection.setVisible(false);
        aiSummarySection.setManaged(false);
        aiSummaryBtn.setDisable(false);
        aiSummaryBtn.setText("✨ Résumé IA");

        // Si le résumé existe déjà, on peut l'indiquer (ou l'afficher directement)
        if (c.getResumeAi() != null && !c.getResumeAi().isEmpty()) {
            aiSummaryBtn.setText("✨ Voir le Résumé IA");
        }

        // Bouton favori
        boolean isFav = favoris.estFavori(userId, c.getId());
        favBtn.setText(isFav ? "⭐ En favori" : "☆ Ajouter aux favoris");
        favBtn.setStyle("-fx-background-color:" + (isFav ? "#fef9c3" : "white") +
            "; -fx-text-fill:" + (isFav ? "#92400e" : "#64748b") +
            "; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; " +
            "-fx-padding:7 16; -fx-cursor:hand;");

        // Navigation Prev/Next
        prevBtn.setDisable(index == 0);
        nextBtn.setDisable(index == coursList.size() - 1);

        // Texte des boutons
        if (index < coursList.size() - 1) {
            nextBtn.setText("Suivant : " + truncate(coursList.get(index + 1).getTitre(), 30) + " →");
        } else {
            nextBtn.setText("✅ Fin du parcours");
        }
        if (index > 0) {
            prevBtn.setText("← " + truncate(coursList.get(index - 1).getTitre(), 30));
        } else {
            prevBtn.setText("← Début");
        }

        // Rafraîchir le mini-sommaire
        highlightNavItem(index);
    }

    // ── Navigation parcours ───────────────────────────────────────────────────────
    @FXML
    public void handlePrev() {
        if (currentIndex > 0) {
            currentIndex--;
            displayCours(currentIndex);
        }
    }

    @FXML
    public void handleNext() {
        if (currentIndex < coursList.size() - 1) {
            currentIndex++;
            displayCours(currentIndex);
        } else {
            // Fin du parcours — afficher dialogue de félicitations
            showEndDialog();
        }
    }

    // ── Favori ────────────────────────────────────────────────────────────────────
    @FXML
    public void handleFavori() {
        Cours c = coursList.get(currentIndex);
        boolean nowFav = favoris.toggle(userId, c);
        favBtn.setText(nowFav ? "⭐ En favori" : "☆ Ajouter aux favoris");
        favBtn.setStyle("-fx-background-color:" + (nowFav ? "#fef9c3" : "white") +
            "; -fx-text-fill:" + (nowFav ? "#92400e" : "#64748b") +
            "; -fx-border-color:#e2e8f0; -fx-border-radius:8; -fx-background-radius:8; " +
            "-fx-padding:7 16; -fx-cursor:hand;");
    }

    // ── Voir PDF ──────────────────────────────────────────────────────────────────
    @FXML
    public void handleVoirPdf() {
        String path = coursList.get(currentIndex).getFichierContenu();
        if (path == null || path.isEmpty()) return;
        try {
            if (path.startsWith("http")) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(path));
            } else {
                java.io.File f = new java.io.File(path);
                if (f.exists()) java.awt.Desktop.getDesktop().open(f);
                else new Alert(Alert.AlertType.WARNING, "Fichier introuvable : " + path).showAndWait();
            }
        } catch (Exception e) { //noinspection CallToPrintStackTrace
            e.printStackTrace(); }
    }

    // ── Résumé IA ─────────────────────────────────────────────────────────────────
    @FXML
    public void handleAiSummary(ActionEvent event) {
        Cours c = coursList.get(currentIndex);

        // Si le résumé existe déjà, on l'affiche simplement
        if (c.getResumeAi() != null && !c.getResumeAi().isEmpty()) {
            aiSummaryLabel.setText(c.getResumeAi());
            aiSummarySection.setVisible(true);
            aiSummarySection.setManaged(true);
            return;
        }

        // Sinon, on le génère
        String contenu = c.getContenu();
        String pdf = c.getFichierContenu();
        
        // Diagnostic du contenu
        System.out.println("🔍 [DIAGNOSTIC] Contenu du cours ID=" + c.getId());
        System.out.println("   - Contenu texte: " + (contenu != null ? contenu.length() + " caractères" : "NULL"));
        System.out.println("   - URL PDF: " + (pdf != null ? pdf : "NULL"));
        
        if ((contenu == null || contenu.trim().isEmpty()) && (pdf == null || pdf.trim().isEmpty())) {
            // Afficher un message dans la section IA au lieu d'une alerte
            aiSummaryLabel.setText("⚠️ Ce cours ne contient aucun texte ou PDF à résumer.\n\n" +
                    "Pour générer un résumé, le cours doit avoir :\n" +
                    "• Du contenu texte (éditeur de contenu)\n" +
                    "• OU un fichier PDF associé");
            aiSummarySection.setVisible(true);
            aiSummarySection.setManaged(true);
            aiSummaryBtn.setDisable(false);
            aiSummaryBtn.setText("✨ Résumé IA");
            return;
        }

        aiSummaryBtn.setDisable(true);
        aiSummaryBtn.setText("⌛ Analyse en cours...");

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                return summaryService.generateSummary(contenu, c.getFichierContenu());
            }
        };

        task.setOnSucceeded(e -> {
            String result = task.getValue();
            c.setResumeAi(result);
            aiSummaryLabel.setText(result);
            aiSummarySection.setVisible(true);
            aiSummarySection.setManaged(true);
            aiSummaryBtn.setDisable(false);
            aiSummaryBtn.setText("✨ Voir le Résumé IA");

            // Sauvegarder en base de données pour les autres
            new Thread(() -> {
                coursController.modifierCours(c);
            }).start();
        });

        task.setOnFailed(e -> {
            new Alert(Alert.AlertType.ERROR, "Erreur lors de la génération du résumé.").showAndWait();
            aiSummaryBtn.setDisable(false);
            aiSummaryBtn.setText("✨ Résumé IA");
        });

        new Thread(task).start();
    }

    @FXML
    public void handleCloseSummary() {
        aiSummarySection.setVisible(false);
        aiSummarySection.setManaged(false);
    }

    // ── Fin du parcours ──────────────────────────────────────────────────────────
    private void showEndDialog() {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("🎉 Parcours terminé !");
        dlg.setHeaderText("Félicitations !");
        dlg.setContentText("Vous avez terminé tous les cours du module \"" +
            currentModule.getTitre() + "\".\n" +
            "Total : " + coursList.size() + " cours parcourus.");
        dlg.showAndWait();
        retourListe();
    }

    // ── Sommaire latéral ─────────────────────────────────────────────────────────
    private void buildNavSommaire() {
        if (courseNavContainer == null) return;
        courseNavContainer.getChildren().clear();

        for (int i = 0; i < coursList.size(); i++) {
            Cours c = coursList.get(i);
            final int idx = i;

            HBox item = new HBox(8);
            item.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            item.setStyle("-fx-padding:8 12; -fx-cursor:hand; -fx-background-radius:8;");

            Label num = new Label(String.valueOf(i + 1));
            num.setMinWidth(22);
            num.setStyle("-fx-background-color:#e2e8f0; -fx-background-radius:99; " +
                "-fx-text-fill:#475569; -fx-font-size:11; -fx-font-weight:700; -fx-padding:2 6;");

            Label titre = new Label(truncate(c.getTitre(), 26));
            titre.setStyle("-fx-text-fill:#475569; -fx-font-size:12;");
            titre.setWrapText(false);

            item.getChildren().addAll(num, titre);
            item.setOnMouseClicked(e -> {
                currentIndex = idx;
                displayCours(currentIndex);
            });
            item.setOnMouseEntered(e ->
                item.setStyle("-fx-padding:8 12; -fx-cursor:hand; " +
                    "-fx-background-radius:8; -fx-background-color:#f0fffe;"));
            item.setOnMouseExited(e -> {
                boolean current = (idx == currentIndex);
                item.setStyle("-fx-padding:8 12; -fx-cursor:hand; " +
                    "-fx-background-radius:8;" +
                    (current ? "-fx-background-color:#e0fefe;" : ""));
            });

            courseNavContainer.getChildren().add(item);
        }
    }

    private void highlightNavItem(int activeIndex) {
        if (courseNavContainer == null) return;
        for (int i = 0; i < courseNavContainer.getChildren().size(); i++) {
            HBox item = (HBox) courseNavContainer.getChildren().get(i);
            if (i == activeIndex) {
                item.setStyle("-fx-padding:8 12; -fx-cursor:hand; " +
                    "-fx-background-radius:8; -fx-background-color:#e0fefe; " +
                    "-fx-border-color:#0FB5A9; -fx-border-width:0 0 0 3; -fx-border-radius:0;");
                // Numéro en surbrillance
                if (!item.getChildren().isEmpty()) {
                    Label num = (Label) item.getChildren().get(0);
                    num.setStyle("-fx-background-color:#0FB5A9; -fx-background-radius:99; " +
                        "-fx-text-fill:white; -fx-font-size:11; -fx-font-weight:700; -fx-padding:2 6;");
                }
            } else {
                item.setStyle("-fx-padding:8 12; -fx-cursor:hand; -fx-background-radius:8;");
                if (!item.getChildren().isEmpty()) {
                    Label num = (Label) item.getChildren().get(0);
                    num.setStyle("-fx-background-color:#e2e8f0; -fx-background-radius:99; " +
                        "-fx-text-fill:#475569; -fx-font-size:11; -fx-font-weight:700; -fx-padding:2 6;");
                }
            }
        }
    }

    // ── Retour ────────────────────────────────────────────────────────────────────
    @FXML
    public void handleRetourListe() { retourListe(); }

    private void retourListe() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/tn/esprit/view/front_CoursList.fxml"));
            Parent root = loader.load();
            FrontCoursListController ctrl = loader.getController();
            ctrl.initData(currentUser, currentCategorie, currentModule);
            Stage stage = (Stage) titreCoursLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            tn.esprit.utils.StageUtils.applyUniformSize(stage);
            stage.show();
        } catch (IOException e) { //noinspection CallToPrintStackTrace
            e.printStackTrace(); }
    }

    @FXML
    public void handleCoursCategories() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/tn/esprit/view/front_CoursCategories.fxml"));
            Parent root = loader.load();
            ((FrontCoursCategorieController) loader.getController()).initUser(currentUser);
            Stage stage = (Stage) titreCoursLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            tn.esprit.utils.StageUtils.applyUniformSize(stage);
            stage.show();
        } catch (IOException e) { //noinspection CallToPrintStackTrace
            e.printStackTrace(); }
    }

    @FXML public void handleHome() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/tn/esprit/view/front_user_dashboard.fxml"));
            Parent root = loader.load();
            ((FrontUserDashboardController) loader.getController()).initUser(currentUser);
            Stage stage = (Stage) titreCoursLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            tn.esprit.utils.StageUtils.applyUniformSize(stage);
            stage.show();
        } catch (IOException e) { //noinspection CallToPrintStackTrace
            e.printStackTrace(); }
    }

    // ── Util ──────────────────────────────────────────────────────────────────────
    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() > max ? s.substring(0, max - 1) + "…" : s;
    }
}
