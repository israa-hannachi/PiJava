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
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
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
    @FXML private WebView     contenuWebView;
    @FXML private TextField fichierField;
    @FXML private Button    voirPdfBtn;
    @FXML private Button    submitBtn;
    @FXML private Button    uploadCloudBtn;      // nouveau bouton upload cloud
    @FXML private Label     titreError;
    @FXML private Label     contenuError;
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
    private WebEngine webEngine = null;  // TinyMCE WebEngine

    // URL TinyMCE depuis CDNJS (pas besoin de clé API)
    private static final String TINYMCE_CDN_URL =
        "https://cdnjs.cloudflare.com/ajax/libs/tinymce/6.8.3/tinymce.min.js";

    // HTML d'initialisation TinyMCE avec config française
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
            "  height: 320," +
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
            "  convert_urls: false," +
            "  setup: function(editor) {" +
            "    editor.on('init', function() {" +
            "      console.log('TinyMCE initialized successfully');" +
            "    });" +
            "  }" +
            "});" +
            "</script>" +
            "</body>" +
            "</html>";
    }

    // ─────────────────────────────────────────────────────────────────────────────

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        actifCombo.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        visibleCombo.setItems(FXCollections.observableArrayList("Visible", "Caché"));
        actifCombo.getSelectionModel().selectFirst();
        visibleCombo.getSelectionModel().selectFirst();
        loadModules();
        hideUploadProgress();
        initTinyMCE("");
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

    // Initialise TinyMCE dans la WebView
    private void initTinyMCE(String initialContent) {
        if (contenuWebView == null) {
            System.err.println("WebView is null - TinyMCE cannot be initialized");
            return;
        }
        webEngine = contenuWebView.getEngine();
        
        // Enable JavaScript debugging
        webEngine.setJavaScriptEnabled(true);
        
        String html = buildTinyMCEHtml(initialContent);
        webEngine.loadContent(html);
        
        // Log erreurs JS et succès
        webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                System.out.println("TinyMCE HTML loaded successfully");
                // Vérifier si TinyMCE est bien initialisé après un court délai
                javafx.application.Platform.runLater(() -> {
                    try {
                        Object result = webEngine.executeScript(
                            "typeof tinymce !== 'undefined' && tinymce.editors.length > 0 ? 'TinyMCE initialized' : 'TinyMCE not found'"
                        );
                        System.out.println("TinyMCE status: " + result);
                    } catch (Exception e) {
                        System.err.println("Error checking TinyMCE status: " + e.getMessage());
                    }
                });
            } else if (newState == javafx.concurrent.Worker.State.FAILED) {
                System.err.println("TinyMCE load failed: " + webEngine.getLoadWorker().getException());
            } else if (newState == javafx.concurrent.Worker.State.CANCELLED) {
                System.err.println("TinyMCE load cancelled");
            }
        });
    }

    // Met à jour le contenu TinyMCE (appelé depuis initForm pour modification)
    private void setTinyMCEContent(String htmlContent) {
        if (webEngine == null) {
            System.err.println("WebEngine is null - cannot set TinyMCE content");
            return;
        }
        
        // Attendre que TinyMCE soit initialisé avant de définir le contenu
        javafx.application.Platform.runLater(() -> {
            try {
                String escaped = htmlContent
                    .replace("\\", "\\\\")
                    .replace("'", "\\'")
                    .replace("\n", "\\n")
                    .replace("\r", "")
                    .replace("\"", "\\\"");
                
                // Essayer de définir le contenu via TinyMCE si disponible
                Object checkResult = webEngine.executeScript(
                    "typeof tinymce !== 'undefined' && tinymce.editors && tinymce.editors.length > 0"
                );
                
                if (Boolean.TRUE.equals(checkResult)) {
                    webEngine.executeScript(
                        "tinymce.editors[0].setContent('" + escaped + "');"
                    );
                    System.out.println("TinyMCE content set successfully");
                } else {
                    // Fallback: définir directement le contenu du textarea
                    webEngine.executeScript(
                        "var textarea = document.getElementById('tinyMCEEditor'); if (textarea) { textarea.value = '" + escaped + "'; }"
                    );
                    System.out.println("Fallback: TinyMCE textarea content set");
                }
            } catch (Exception e) {
                System.err.println("setTinyMCEContent error: " + e.getMessage());
                // Dernier fallback: essayer sans échappement complexe
                try {
                    webEngine.executeScript(
                        "var textarea = document.getElementById('tinyMCEEditor'); if (textarea) { textarea.innerHTML = '" + htmlContent.replace("'", "\\'") + "'; }"
                    );
                } catch (Exception ex) {
                    System.err.println("Final fallback failed: " + ex.getMessage());
                }
            }
        });
    }

    // Récupère le HTML du contenu TinyMCE
    private String getTinyMCEContent() {
        if (webEngine == null) {
            System.err.println("WebEngine is null - cannot get TinyMCE content");
            return "";
        }
        try {
            Object value = webEngine.executeScript(
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
            System.err.println("getTinyMCEContent error: " + e.getMessage());
            return "";
        }
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
            // Contenu via TinyMCE (avec délai pour laisser TinyMCE s'initialiser)
            String contenuHtml = c.getContenu() != null ? c.getContenu() : "";
            contenuHtml = contenuHtml.isEmpty() ? "<p></p>" : contenuHtml;
            String finalContenuHtml = contenuHtml;
            javafx.application.Platform.runLater(() -> setTinyMCEContent(finalContenuHtml));

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
        String contenu     = getTinyMCEContent();

        String plainText = (contenu == null ? "" : contenu).replaceAll("<[^>]*>", "").trim();
        if (plainText.isEmpty()) {
            showError(contenuError, "Le contenu du cours est obligatoire.");
            return;
        }

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

        // Validation du contenu TinyMCE
        String contenuHtml = getTinyMCEContent();
        String plainText = contenuHtml.replaceAll("<[^>]*>", "").trim();
        if (plainText.isEmpty()) {
            showError(contenuError, "Le contenu du cours est obligatoire."); valid = false;
        }

        return valid;
    }

    // ── Helpers UI ───────────────────────────────────────────────────────────────
    private void clearErrors() {
        hideLabel(titreError); hideLabel(dureeError); hideLabel(ordreError);
        hideLabel(moduleError); hideLabel(pdfError); hideLabel(globalMessage);
        hideLabel(contenuError);
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