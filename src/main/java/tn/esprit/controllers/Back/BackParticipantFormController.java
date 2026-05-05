// src/main/java/tn/esprit/controllers/Back/BackParticipantFormController.java
package tn.esprit.controllers.Back;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.controllers.meet.ParticipantController;
import tn.esprit.entities.meet.participant;
import tn.esprit.entities.users.Users;

import java.net.URL;
import java.util.ResourceBundle;

public class BackParticipantFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField nomField;
    @FXML private TextField prenomField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private TextField smtpEmailField;
    @FXML private TextField smtpPasswordField;
    @FXML private Label errorLabel;

    private final ParticipantController participantController = new ParticipantController();
    private participant participantToEdit;
    private BackParticipantController parentController;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        roleCombo.getItems().addAll("enseignant", "etudiant");
        errorLabel.setVisible(false);
    }

    public void initForm(participant p, Users user, BackParticipantController parent) {
        this.participantToEdit = p;
        this.parentController = parent;
        if (p != null) {
            titleLabel.setText("✏️ Modifier le participant");
            nomField.setText(p.getNom());
            prenomField.setText(p.getPrenom());
            emailField.setText(p.getEmail());
            roleCombo.setValue(p.getRole());
            smtpEmailField.setText(p.getSmtpEmail() != null ? p.getSmtpEmail() : "");
            smtpPasswordField.setText(p.getSmtpAppPassword() != null ? p.getSmtpAppPassword() : "");
        } else {
            titleLabel.setText("➕ Ajouter un participant");
        }
    }

    @FXML
    public void handleSave() {
        errorLabel.setVisible(false);

        String nom    = nomField.getText().trim();
        String prenom = prenomField.getText().trim();
        String email  = emailField.getText().trim();
        String role   = roleCombo.getValue();

        if (nom.isEmpty())    { showError("Le nom est obligatoire."); return; }
        if (nom.length() < 2) { showError("Le nom doit contenir au moins 2 caractères."); return; }
        if (prenom.isEmpty()) { showError("Le prénom est obligatoire."); return; }
        if (email.isEmpty())  { showError("L'email est obligatoire."); return; }
        if (!email.matches("^[\\w._%+\\-]+@[\\w.\\-]+\\.[a-zA-Z]{2,}$")) {
            showError("Format d'email invalide."); return;
        }
        if (role == null) { showError("Le rôle est obligatoire."); return; }

        participant p = participantToEdit != null ? participantToEdit : new participant();
        p.setNom(nom); p.setPrenom(prenom); p.setEmail(email); p.setRole(role);
        p.setSmtpEmail(smtpEmailField.getText().trim().isEmpty() ? null : smtpEmailField.getText().trim());
        p.setSmtpAppPassword(smtpPasswordField.getText().trim().isEmpty() ? null : smtpPasswordField.getText().trim());

        if (participantToEdit == null) {
            participantController.ajouterParticipant(p);
        } else {
            participantController.modifierParticipant(p);
        }

        parentController.refreshTable();
        ((Stage) nomField.getScene().getWindow()).close();
    }

    @FXML
    public void handleCancel() {
        ((Stage) nomField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText("⚠️ " + msg);
        errorLabel.setVisible(true);
    }
}
