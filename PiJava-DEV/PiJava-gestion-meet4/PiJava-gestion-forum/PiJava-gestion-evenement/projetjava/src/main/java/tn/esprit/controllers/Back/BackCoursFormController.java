package tn.esprit.controllers.Back;

import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.entities.cours.Cours;
import tn.esprit.entities.cours.Cours_Module;
import tn.esprit.services.cours.CloudinaryService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.*;

/**
 * Formulaire Ajouter / Modifier un cours (back-office).
 * Fonctionnalités ajoutées :
 *   – Upload PDF vers Cloudinary (asynchrone)
 *   – Suppression de l'ancien fichier Cloudinary lors du remplacement
 *   – Indicateur de progression upload
 */
public class BackCoursFormController implements Initializable {

    // ── FXML ─────────────────────────────────────────────────────────────────────
    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private TextArea  descriptionField;
    @FXML private TextField dureeField;
    @FXML private TextField ordreField;
    @FXML private ComboBox<String> moduleCombo;
    @FXML private ComboBox<String> actifCombo;
    @FXML private ComboBox<String> visibleCombo;
    @FXML private TextArea  contenuField;
    @FXML private TextField fichierField;
    @FXML private Button    voirPdfBtn;
    @FXML private Button    submitBtn;
    @FXML private Button    uploadCloudBtn;      // nouveau bouton upload cloud
    @FXML private Label     titreError;
    @FXML private Label     dureeError;
    @FXML private Label     ordreError;
    @FXML private Label     moduleError;
    @FXML private Label     pdfError;
    @FXML private Label     globalMessage;
    @FXML private VBox      uploadProgressBox;   // conteneur ProgressBar + label
    @FXML private ProgressBar uploadProgressBar; // barre progression
    @FXML private Label     uploadStatusLabel;   // statut upload

    // ── Services ─────────────────────────────────────────────────────────────────
    private final CoursController       controller       = new CoursController();
    private final CoursModuleController moduleController = new CoursModuleController();
    private final CloudinaryService     cloudinary       = CloudinaryService.getInstance();

    private Cours  coursAModifier     = null;
    private File   selectedLocalFile  = null;          // fichier local sélectionné
    private String cloudinaryUrl      = null;          // URL Cloudinary après upload
    private final Map<String, Integer> moduleNameToId = new HashMap<>();

    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        actifCombo.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        visibleCombo.setItems(FXCollections.observableArrayList("Visible", "Caché"));
        actifCombo.getSelectionModel().selectFirst();
        visibleCombo.getSelectionModel().selectFirst();
        loadModules();
        hideUploadProgress();
    }

    private void loadModules() {
        List<String> names = new ArrayList<>();
        for (Cours_Module m : moduleController.recupererModules()) {
            if (m.getActif() == 1) {
                names.add(m.getTitre());
                moduleNameToId.put(m.getTitre(), m.getId());
            }
        }
        moduleCombo.setItems(FXCollections.observableArrayList(names));
    }

    /** Appelé avant l'affichage : null = ajout, non-null = modification */
    public void initForm(Cours c) {
        coursAModifier = c;
        if (c != null) {
            titleLabel.setText("Modifier le Cours");
            submitBtn.setText("Mettre à jour");
            titreField.setText(c.getTitre());
            descriptionField.setText(c.getDescription() != null ? c.getDescription() : "");
            dureeField.setText(String.valueOf(c.getDuree()));
            ordreField.setText(String.valueOf(c.getOrdre()));
            actifCombo.getSelectionModel().select(c.getActif()    == 1 ? "Actif"    : "Inactif");
            visibleCombo.getSelectionModel().select(c.getVisible() == 1 ? "Visible" : "Caché");
            contenuField.setText(c.getContenu() != null ? c.getContenu() : "");

            if (c.getFichierContenu() != null && !c.getFichierContenu().isEmpty()) {
                fichierField.setText(c.getFichierContenu());
                cloudinaryUrl = CloudinaryService.isCloudinaryUrl(c.getFichierContenu())
                        ? c.getFichierContenu() : null;
                voirPdfBtn.setVisible(true);
                voirPdfBtn.setManaged(true);
            }

            // Sélection module
            moduleNameToId.entrySet().stream()
                    .filter(e -> e.getValue() == c.getModuleId())
                    .findFirst()
                    .ifPresent(e -> moduleCombo.getSelectionModel().select(e.getKey()));
        }
    }

    // ── Choisir un fichier PDF local ──────────────────────────────────────────────
    @FXML
    public void handleChoisirPdf(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir un fichier PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        Stage stage = (Stage) titreField.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            if (file.length() > 10 * 1024 * 1024) {
                showError(pdfError, "Le fichier ne doit pas dépasser 10 MB.");
                return;
            }
            selectedLocalFile = file;
            cloudinaryUrl     = null;                        // on va l'uploader
            fichierField.setText(file.getAbsolutePath());
            voirPdfBtn.setVisible(true);
            voirPdfBtn.setManaged(true);
            hideLabel(pdfError);
            // Activer le bouton upload cloud
            uploadCloudBtn.setVisible(true);
            uploadCloudBtn.setManaged(true);
        }
    }

    // ── Upload vers Cloudinary ────────────────────────────────────────────────────
    @FXML
    public void handleUploadCloud(ActionEvent event) {
        if (selectedLocalFile == null) {
            showError(pdfError, "Sélectionnez d'abord un fichier PDF.");
            return;
        }
        showUploadProgress("Connexion à Cloudinary…");
        submitBtn.setDisable(true);
        uploadCloudBtn.setDisable(true);

        Task<String> uploadTask = new Task<>() {
            @Override
            protected String call() {
                updateMessage("Upload en cours…");
                return cloudinary.uploadPdf(selectedLocalFile);
            }
        };

        uploadProgressBar.progressProperty().bind(uploadTask.progressProperty());
        uploadStatusLabel.textProperty().bind(uploadTask.messageProperty());

        uploadTask.setOnSucceeded(e -> {
            String url = uploadTask.getValue();
            if (url != null) {
                cloudinaryUrl = url;
                fichierField.setText(url);
                showUploadDone("✅ Fichier uploadé sur Cloudinary !");
            } else {
                showUploadDone("❌ Échec upload — le chemin local sera utilisé.");
                cloudinaryUrl = null;
            }
            submitBtn.setDisable(false);
            uploadCloudBtn.setDisable(false);
            selectedLocalFile = null;
        });

        uploadTask.setOnFailed(e -> {
            showUploadDone("❌ Erreur : " + uploadTask.getException().getMessage());
            submitBtn.setDisable(false);
            uploadCloudBtn.setDisable(false);
        });

        Thread t = new Thread(uploadTask);
        t.setDaemon(true);
        t.start();
    }

    // ── Voir PDF ──────────────────────────────────────────────────────────────────
    @FXML
    public void handleVoirPdf(ActionEvent event) {
        String path = fichierField.getText();
        if (path == null || path.isEmpty()) return;
        try {
            if (path.startsWith("http")) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(path));
            } else {
                File f = new File(path);
                if (f.exists()) java.awt.Desktop.getDesktop().open(f);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Enregistrer ──────────────────────────────────────────────────────────────
    @FXML
    public void handleSubmit(ActionEvent event) {
        if (!validateForm()) return;

        String titre       = titreField.getText().trim();
        String description = descriptionField.getText().trim();
        int    duree       = Integer.parseInt(dureeField.getText().trim());
        int    ordre       = Integer.parseInt(ordreField.getText().trim());
        int    moduleId    = moduleNameToId.get(moduleCombo.getValue());
        int    actif       = "Actif".equals(actifCombo.getValue())    ? 1 : 0;
        int    visible     = "Visible".equals(visibleCombo.getValue()) ? 1 : 0;
        String contenu     = contenuField.getText().trim();

        // Résoudre le chemin final du fichier
        String fichierPath;
        if (cloudinaryUrl != null) {
            fichierPath = cloudinaryUrl;          // URL Cloudinary
        } else if (selectedLocalFile != null) {
            fichierPath = selectedLocalFile.getAbsolutePath(); // chemin local (pas encore uploadé)
        } else {
            String current = fichierField.getText().trim();
            fichierPath = current.isEmpty() ? null : current;
        }

        if (coursAModifier == null) {
            // AJOUT
            Cours c = new Cours(titre,
                    description.isEmpty() ? null : description,
                    contenu.isEmpty()     ? null : contenu,
                    duree, ordre,
                    new Timestamp(System.currentTimeMillis()),
                    actif, moduleId, fichierPath, 0, visible);
            controller.ajouterCours(c);
        } else {
            // MODIFICATION — supprimer l'ancien fichier Cloudinary si remplacé
            if (fichierPath != null
                    && !fichierPath.equals(coursAModifier.getFichierContenu())
                    && CloudinaryService.isCloudinaryUrl(coursAModifier.getFichierContenu())) {
                cloudinary.deletePdf(coursAModifier.getFichierContenu());
            }

            coursAModifier.setTitre(titre);
            coursAModifier.setDescription(description.isEmpty() ? null : description);
            coursAModifier.setContenu(contenu.isEmpty() ? null : contenu);
            coursAModifier.setDuree(duree);
            coursAModifier.setOrdre(ordre);
            coursAModifier.setModuleId(moduleId);
            coursAModifier.setActif(actif);
            coursAModifier.setVisible(visible);
            coursAModifier.setFichierContenu(fichierPath);
            controller.modifierCours(coursAModifier);
        }
        retourListe();
    }

    // ── Validation ────────────────────────────────────────────────────────────────
    private boolean validateForm() {
        clearErrors();
        boolean valid = true;

        String titre = titreField.getText() == null ? "" : titreField.getText().trim();
        if (titre.isEmpty()) {
            showError(titreError, "Le titre est obligatoire."); valid = false;
        } else if (titre.length() < 3) {
            showError(titreError, "Minimum 3 caractères."); valid = false;
        } else if (titre.length() > 150) {
            showError(titreError, "Maximum 150 caractères."); valid = false;
        }

        String dureeStr = dureeField.getText() == null ? "" : dureeField.getText().trim();
        if (dureeStr.isEmpty()) {
            showError(dureeError, "La durée est obligatoire."); valid = false;
        } else {
            try {
                int d = Integer.parseInt(dureeStr);
                if (d <= 0)   { showError(dureeError, "La durée doit être > 0."); valid = false; }
                else if (d > 9999) { showError(dureeError, "Maximum 9 999 minutes."); valid = false; }
            } catch (NumberFormatException e) {
                showError(dureeError, "La durée doit être un entier."); valid = false;
            }
        }

        String ordreStr = ordreField.getText() == null ? "" : ordreField.getText().trim();
        if (ordreStr.isEmpty()) {
            showError(ordreError, "L'ordre est obligatoire."); valid = false;
        } else {
            try {
                int o = Integer.parseInt(ordreStr);
                if (o < 0) { showError(ordreError, "L'ordre doit être ≥ 0."); valid = false; }
            } catch (NumberFormatException e) {
                showError(ordreError, "L'ordre doit être un entier."); valid = false;
            }
        }

        if (moduleCombo.getValue() == null) {
            showError(moduleError, "Veuillez sélectionner un module."); valid = false;
        }

        return valid;
    }

    // ── Helpers UI ───────────────────────────────────────────────────────────────
    private void clearErrors() {
        hideLabel(titreError); hideLabel(dureeError); hideLabel(ordreError);
        hideLabel(moduleError); hideLabel(pdfError); hideLabel(globalMessage);
    }

    private void showError(Label lbl, String msg) {
        lbl.setText("⚠️ " + msg);
        lbl.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11;");
        lbl.setVisible(true); lbl.setManaged(true);
    }

    private void hideLabel(Label lbl) { lbl.setVisible(false); lbl.setManaged(false); }

    private void showUploadProgress(String msg) {
        uploadProgressBox.setVisible(true);
        uploadProgressBox.setManaged(true);
        uploadStatusLabel.setText(msg);
        uploadProgressBar.setProgress(-1);   // indéterminé
    }

    private void showUploadDone(String msg) {
        uploadProgressBar.progressProperty().unbind();
        uploadStatusLabel.textProperty().unbind();
        uploadStatusLabel.setText(msg);
        uploadProgressBar.setProgress(1.0);
    }

    private void hideUploadProgress() {
        uploadProgressBox.setVisible(false);
        uploadProgressBox.setManaged(false);
    }

    // ── Navigation ───────────────────────────────────────────────────────────────
    @FXML
    public void handleRetour(ActionEvent event) { retourListe(); }

    private void retourListe() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/back_CoursList.fxml"));
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}