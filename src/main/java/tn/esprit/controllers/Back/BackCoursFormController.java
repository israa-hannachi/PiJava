package tn.esprit.controllers.Back;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.entities.cours.cours;
import tn.esprit.entities.cours.cours_module;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BackCoursFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField dureeField;
    @FXML private TextField ordreField;
    @FXML private ComboBox<String> moduleCombo;
    @FXML private ComboBox<String> actifCombo;
    @FXML private ComboBox<String> visibleCombo;
    @FXML private TextArea contenuField;
    @FXML private TextField fichierField;
    @FXML private Button voirPdfBtn;
    @FXML private Label titreError;
    @FXML private Label dureeError;
    @FXML private Label ordreError;
    @FXML private Label moduleError;
    @FXML private Label pdfError;
    @FXML private Label globalMessage;
    @FXML private Button submitBtn;

    private final CoursController controller = new CoursController();
    private final CoursModuleController moduleController = new CoursModuleController();
    private cours coursAModifier = null;
    private File selectedPdfFile = null;
    private final Map<String, Integer> moduleNameToId = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        actifCombo.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        visibleCombo.setItems(FXCollections.observableArrayList("Visible", "Caché"));
        actifCombo.getSelectionModel().selectFirst();
        visibleCombo.getSelectionModel().selectFirst();
        loadModules();
    }

    private void loadModules() {
        List<String> names = new ArrayList<>();
        for (cours_module m : moduleController.recupererModules()) {
            if (m.getActif() == 1) {
                names.add(m.getTitre());
                moduleNameToId.put(m.getTitre(), m.getId());
            }
        }
        moduleCombo.setItems(FXCollections.observableArrayList(names));
    }

    public void initForm(cours c) {
        coursAModifier = c;
        if (c != null) {
            titleLabel.setText("Modifier le Cours");
            submitBtn.setText("Mettre à jour");
            titreField.setText(c.getTitre());
            descriptionField.setText(c.getDescription() != null ? c.getDescription() : "");
            dureeField.setText(String.valueOf(c.getDuree()));
            ordreField.setText(String.valueOf(c.getOrdre()));
            actifCombo.getSelectionModel().select(c.getActif() == 1 ? "Actif" : "Inactif");
            visibleCombo.getSelectionModel().select(c.getVisible() == 1 ? "Visible" : "Caché");
            contenuField.setText(c.getContenu() != null ? c.getContenu() : "");

            if (c.getFichierContenu() != null && !c.getFichierContenu().isEmpty()) {
                fichierField.setText(c.getFichierContenu());
                voirPdfBtn.setVisible(true);
                voirPdfBtn.setManaged(true);
            }

            // select module
            moduleNameToId.entrySet().stream()
                    .filter(e -> e.getValue() == c.getModuleId())
                    .findFirst()
                    .ifPresent(e -> moduleCombo.getSelectionModel().select(e.getKey()));
        }
    }

    @FXML
    public void handleChoisirPdf(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Choisir un fichier PDF");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichiers PDF", "*.pdf"));
        Stage stage = (Stage) titreField.getScene().getWindow();
        File file = chooser.showOpenDialog(stage);
        if (file != null) {
            // Validate size (max 10MB)
            if (file.length() > 10 * 1024 * 1024) {
                showError(pdfError, "Le fichier PDF ne doit pas dépasser 10MB.");
                return;
            }
            selectedPdfFile = file;
            fichierField.setText(file.getAbsolutePath());
            voirPdfBtn.setVisible(true);
            voirPdfBtn.setManaged(true);
            hideLabel(pdfError);
        }
    }

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

    @FXML
    public void handleSubmit(ActionEvent event) {
        if (!validateForm()) return;

        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();
        int duree = Integer.parseInt(dureeField.getText().trim());
        int ordre = Integer.parseInt(ordreField.getText().trim());
        int moduleId = moduleNameToId.get(moduleCombo.getValue());
        int actif = "Actif".equals(actifCombo.getValue()) ? 1 : 0;
        int visible = "Visible".equals(visibleCombo.getValue()) ? 1 : 0;
        String contenu = contenuField.getText().trim();

        // Handle PDF path: use new file path if selected, otherwise keep existing
        String fichierPath = fichierField.getText().trim();
        if (selectedPdfFile != null) {
            fichierPath = selectedPdfFile.getAbsolutePath();
        } else if (fichierPath.isEmpty()) {
            fichierPath = null;
        }

        if (coursAModifier == null) {
            cours c = new cours(titre,
                    description.isEmpty() ? null : description,
                    contenu.isEmpty() ? null : contenu,
                    duree, ordre,
                    new Timestamp(System.currentTimeMillis()),
                    actif, moduleId, fichierPath, 0, visible);
            controller.ajouterCours(c);
        } else {
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

    private boolean validateForm() {
        clearErrors();
        boolean valid = true;

        String titre = titreField.getText() == null ? "" : titreField.getText().trim();
        if (titre.isEmpty()) {
            showError(titreError, "Le titre est obligatoire.");
            valid = false;
        } else if (titre.length() < 3) {
            showError(titreError, "Le titre doit contenir au moins 3 caractères.");
            valid = false;
        } else if (titre.length() > 150) {
            showError(titreError, "Le titre ne doit pas dépasser 150 caractères.");
            valid = false;
        }

        String dureeStr = dureeField.getText() == null ? "" : dureeField.getText().trim();
        if (dureeStr.isEmpty()) {
            showError(dureeError, "La durée est obligatoire.");
            valid = false;
        } else {
            try {
                int d = Integer.parseInt(dureeStr);
                if (d <= 0) { showError(dureeError, "La durée doit être un nombre positif."); valid = false; }
                else if (d > 9999) { showError(dureeError, "Durée maximale : 9999 minutes."); valid = false; }
            } catch (NumberFormatException e) {
                showError(dureeError, "La durée doit être un entier.");
                valid = false;
            }
        }

        String ordreStr = ordreField.getText() == null ? "" : ordreField.getText().trim();
        if (ordreStr.isEmpty()) {
            showError(ordreError, "L'ordre est obligatoire.");
            valid = false;
        } else {
            try {
                int o = Integer.parseInt(ordreStr);
                if (o < 0) { showError(ordreError, "L'ordre doit être ≥ 0."); valid = false; }
            } catch (NumberFormatException e) {
                showError(ordreError, "L'ordre doit être un entier.");
                valid = false;
            }
        }

        if (moduleCombo.getValue() == null) {
            showError(moduleError, "Veuillez sélectionner un module.");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        hideLabel(titreError); hideLabel(dureeError); hideLabel(ordreError);
        hideLabel(moduleError); hideLabel(pdfError); hideLabel(globalMessage);
    }

    private void showError(Label label, String msg) {
        label.setText("⚠️ " + msg);
        label.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11;");
        label.setVisible(true); label.setManaged(true);
    }

    private void hideLabel(Label label) { label.setVisible(false); label.setManaged(false); }

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
