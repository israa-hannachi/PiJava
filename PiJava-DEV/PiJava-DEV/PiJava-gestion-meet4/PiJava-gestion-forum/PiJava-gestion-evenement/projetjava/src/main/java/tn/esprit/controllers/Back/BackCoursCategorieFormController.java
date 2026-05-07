package tn.esprit.controllers.Back;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursCategorieController;
import tn.esprit.entities.cours.Cours_Categorie;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ResourceBundle;

public class BackCoursCategorieFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextArea descriptionField;
    @FXML private ComboBox<String> actifCombo;
    @FXML private Label nomError;
    @FXML private Label actifError;
    @FXML private Label globalMessage;
    @FXML private Button submitBtn;

    private final CoursCategorieController controller = new CoursCategorieController();
    private Cours_Categorie categorieAModifier = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        actifCombo.setItems(FXCollections.observableArrayList("Actif", "Inactif"));
        actifCombo.getSelectionModel().selectFirst();
    }

    /** Called before showing — null = add, non-null = edit */
    public void initForm(Cours_Categorie cat) {
        categorieAModifier = cat;
        if (cat != null) {
            titleLabel.setText("Modifier la Catégorie");
            submitBtn.setText("Mettre à jour");
            nomField.setText(cat.getNom());
            descriptionField.setText(cat.getDescription() != null ? cat.getDescription() : "");
            actifCombo.getSelectionModel().select(cat.getActif() == 1 ? "Actif" : "Inactif");
        }
    }

    @FXML
    public void handleSubmit(ActionEvent event) {
        if (!validateForm()) return;

        String nom = nomField.getText().trim();
        String description = descriptionField.getText().trim();
        int actif = "Actif".equals(actifCombo.getValue()) ? 1 : 0;

        if (categorieAModifier == null) {
            // ADD
            Cours_Categorie cat = new Cours_Categorie(nom,
                    description.isEmpty() ? null : description,
                    new Timestamp(System.currentTimeMillis()), actif);
            controller.ajouterCategorie(cat);
            showSuccess("✅ Catégorie ajoutée avec succès !");
        } else {
            // EDIT
            categorieAModifier.setNom(nom);
            categorieAModifier.setDescription(description.isEmpty() ? null : description);
            categorieAModifier.setActif(actif);
            controller.modifierCategorie(categorieAModifier);
            showSuccess("✅ Catégorie modifiée avec succès !");
        }

        retourListe();
    }

    private boolean validateForm() {
        boolean valid = true;
        clearErrors();

        String nom = nomField.getText() == null ? "" : nomField.getText().trim();
        if (nom.isEmpty()) {
            showError(nomError, "Le nom est obligatoire.");
            valid = false;
        } else if (nom.length() < 2) {
            showError(nomError, "Le nom doit contenir au moins 2 caractères.");
            valid = false;
        } else if (nom.length() > 50) {
            showError(nomError, "Le nom ne doit pas dépasser 50 caractères.");
            valid = false;
        } else if (!nom.matches("[\\p{L}0-9 \\-_&().]+")) {
            showError(nomError, "Le nom contient des caractères non autorisés.");
            valid = false;
        }

        if (actifCombo.getValue() == null) {
            showError(actifError, "Veuillez sélectionner un statut.");
            valid = false;
        }

        return valid;
    }

    private void clearErrors() {
        hideLabel(nomError);
        hideLabel(actifError);
        hideLabel(globalMessage);
    }

    private void showError(Label label, String msg) {
        label.setText("⚠️ " + msg);
        label.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 11;");
        label.setVisible(true);
        label.setManaged(true);
        nomField.setStyle(label == nomError
                ? "-fx-border-color: #dc2626; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 10 12; -fx-font-size: 13;"
                : nomField.getStyle());
    }

    private void showSuccess(String msg) {
        globalMessage.setText(msg);
        globalMessage.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 12; -fx-font-weight: bold;");
        globalMessage.setVisible(true);
        globalMessage.setManaged(true);
    }

    private void hideLabel(Label label) {
        label.setVisible(false);
        label.setManaged(false);
    }

    @FXML
    public void handleRetour(ActionEvent event) {
        retourListe();
    }

    private void retourListe() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/back_CoursCategorieList.fxml"));
            Stage stage = (Stage) nomField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
