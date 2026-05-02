package tn.esprit.controllers.Back;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursCategorieController;
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.entities.cours.Cours_Module;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

public class BackCoursModuleFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionField;
    @FXML private TextField dureeField;
    @FXML private ComboBox<String> niveauCombo;
    @FXML private ComboBox<String> categorieCombo;
    @FXML private ComboBox<String> actifCombo;
    @FXML private Label titreError;
    @FXML private Label dureeError;
    @FXML private Label niveauError;
    @FXML private Label categorieError;
    @FXML private Label globalMessage;
    @FXML private Button submitBtn;

    private final CoursModuleController controller = new CoursModuleController();
    private final CoursCategorieController catController = new CoursCategorieController();
    private Cours_Module moduleAModifier = null;
    private final Map<String, Integer> categorieNameToId = new HashMap<>();
    private boolean dialogMode = false;

    public void setDialogMode(boolean dialogMode) {
        this.dialogMode = dialogMode;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        niveauCombo.setItems(FXCollections.observableArrayList("Débutant", "Intermédiaire", "Avancé"));
        actifCombo.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        actifCombo.getSelectionModel().selectFirst();
        niveauCombo.getSelectionModel().selectFirst();
        loadCategories();
    }

    private void loadCategories() {
        List<String> names = new ArrayList<>();
        for (Cours_Categorie c : catController.recupererCategories()) {
            if (c.getActif() == 1) {
                names.add(c.getNom());
                categorieNameToId.put(c.getNom(), c.getId());
            }
        }
        categorieCombo.setItems(FXCollections.observableArrayList(names));
    }

    public void initForm(Cours_Module mod) {
        moduleAModifier = mod;
        if (mod != null) {
            titleLabel.setText("Modifier le Module");
            submitBtn.setText("Mettre à jour");
            titreField.setText(mod.getTitre());
            descriptionField.setText(mod.getDescription() != null ? mod.getDescription() : "");
            dureeField.setText(String.valueOf(mod.getDuree()));
            niveauCombo.getSelectionModel().select(mod.getNiveau());
            actifCombo.getSelectionModel().select(mod.getActif() == 1 ? "Actif" : "Inactif");
            // select category by id
            categorieNameToId.entrySet().stream()
                    .filter(e -> e.getValue() == mod.getCategorieId())
                    .findFirst()
                    .ifPresent(e -> categorieCombo.getSelectionModel().select(e.getKey()));
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        if (!validateForm()) return;

        String titre = titreField.getText().trim();
        String description = descriptionField.getText().trim();
        int duree = Integer.parseInt(dureeField.getText().trim());
        String niveau = niveauCombo.getValue();
        int categorieId = categorieNameToId.get(categorieCombo.getValue());
        int actif = "Actif".equals(actifCombo.getValue()) ? 1 : 0;

        if (moduleAModifier == null) {
            Cours_Module mod = new Cours_Module(titre,
                    description.isEmpty() ? null : description,
                    duree, niveau, new Timestamp(System.currentTimeMillis()),
                    actif, categorieId, 0);
            controller.ajouterModule(mod);
        } else {
            moduleAModifier.setTitre(titre);
            moduleAModifier.setDescription(description.isEmpty() ? null : description);
            moduleAModifier.setDuree(duree);
            moduleAModifier.setNiveau(niveau);
            moduleAModifier.setActif(actif);
            moduleAModifier.setCategorieId(categorieId);
            controller.modifierModule(moduleAModifier);
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
        } else if (titre.length() < 2) {
            showError(titreError, "Le titre doit contenir au moins 2 caractères.");
            valid = false;
        } else if (titre.length() > 100) {
            showError(titreError, "Le titre ne doit pas dépasser 100 caractères.");
            valid = false;
        }

        String dureeStr = dureeField.getText() == null ? "" : dureeField.getText().trim();
        if (dureeStr.isEmpty()) {
            showError(dureeError, "La durée est obligatoire.");
            valid = false;
        } else {
            try {
                int d = Integer.parseInt(dureeStr);
                if (d <= 0) { showError(dureeError, "La durée doit être > 0."); valid = false; }
                else if (d > 999) { showError(dureeError, "La durée maximale est 999h."); valid = false; }
            } catch (NumberFormatException e) {
                showError(dureeError, "La durée doit être un nombre entier.");
                valid = false;
            }
        }

        if (niveauCombo.getValue() == null) {
            showError(niveauError, "Veuillez sélectionner un niveau.");
            valid = false;
        }

        if (categorieCombo.getValue() == null) {
            showError(categorieError, "Veuillez sélectionner une catégorie.");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        hideLabel(titreError); hideLabel(dureeError);
        hideLabel(niveauError); hideLabel(categorieError); hideLabel(globalMessage);
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
        if (dialogMode) {
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.close();
            return;
        }
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/back_CoursModuleList.fxml"));
            Stage stage = (Stage) titreField.getScene().getWindow();
            stage.setScene(new Scene(root));
            tn.esprit.utils.StageUtils.applyUniformSize(stage);
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
