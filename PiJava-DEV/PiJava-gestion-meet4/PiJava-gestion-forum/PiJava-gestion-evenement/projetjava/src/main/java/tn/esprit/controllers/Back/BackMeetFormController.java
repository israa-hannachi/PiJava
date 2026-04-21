// src/main/java/tn/esprit/controllers/Back/BackMeetFormController.java
package tn.esprit.controllers.Back;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.controllers.meet.MeetController;
import tn.esprit.controllers.meet.MeetParticipantsController;
import tn.esprit.controllers.meet.ParticipantController;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.participant;
import tn.esprit.entities.users.Users;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;

public class BackMeetFormController implements Initializable {

    @FXML private Label titleLabel;
    @FXML private TextField titreField;
    @FXML private TextArea descriptionArea;
    @FXML private DatePicker dateDebutPicker;
    @FXML private TextField heureDebutField;
    @FXML private DatePicker dateFinPicker;
    @FXML private TextField heureFinField;
    @FXML private TextField lienMeetField;
    @FXML private ComboBox<String> organisateurCombo;
    @FXML private ListView<String> participantsAvailable;
    @FXML private ListView<String> participantsSelected;
    @FXML private Label errorLabel;

    private final MeetController meetController = new MeetController();
    private final ParticipantController participantController = new ParticipantController();
    private final MeetParticipantsController mpController = new MeetParticipantsController();

    private Meet meetToEdit;
    private BackMeetController parentController;
    private Users currentUser;
    private List<participant> allParticipants;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        errorLabel.setVisible(false);
        // heure format hint
        heureDebutField.setPromptText("HH:mm (ex: 09:00)");
        heureFinField.setPromptText("HH:mm (ex: 11:00)");
    }

    public void initForm(Meet m, Users user, BackMeetController parent) {
        this.meetToEdit = m;
        this.parentController = parent;
        this.currentUser = user;

        loadParticipants();

        if (m != null) {
            titleLabel.setText("✏️ Modifier la réunion");
            titreField.setText(m.getTitre());
            descriptionArea.setText(m.getDescription() != null ? m.getDescription() : "");
            if (m.getDateDebut() != null) {
                LocalDateTime ldt = m.getDateDebut().toLocalDateTime();
                dateDebutPicker.setValue(ldt.toLocalDate());
                heureDebutField.setText(String.format("%02d:%02d", ldt.getHour(), ldt.getMinute()));
            }
            if (m.getDateFin() != null) {
                LocalDateTime ldt = m.getDateFin().toLocalDateTime();
                dateFinPicker.setValue(ldt.toLocalDate());
                heureFinField.setText(String.format("%02d:%02d", ldt.getHour(), ldt.getMinute()));
            }
            lienMeetField.setText(m.getLienMeet() != null ? m.getLienMeet() : "");
            // select organisateur
            for (String item : organisateurCombo.getItems()) {
                if (item.startsWith(m.getParticipantId() + " - ")) {
                    organisateurCombo.getSelectionModel().select(item);
                    break;
                }
            }
            // load current participants
            List<tn.esprit.entities.meet.Meet_Participants> existing = mpController.getParticipantsDuMeet(m.getId());
            for (tn.esprit.entities.meet.Meet_Participants mp : existing) {
                for (participant p : allParticipants) {
                    if (p.getId() == mp.getParticipantId()) {
                        String label = p.getId() + " - " + p.getNom() + " " + p.getPrenom();
                        if (!participantsSelected.getItems().contains(label)) {
                            participantsSelected.getItems().add(label);
                            participantsAvailable.getItems().remove(label);
                        }
                        break;
                    }
                }
            }
        } else {
            titleLabel.setText("➕ Ajouter une réunion");
        }
    }

    private void loadParticipants() {
        allParticipants = participantController.recupererParticipants();
        organisateurCombo.getItems().clear();
        participantsAvailable.getItems().clear();
        for (participant p : allParticipants) {
            String label = p.getId() + " - " + p.getNom() + " " + p.getPrenom();
            organisateurCombo.getItems().add(label);
            participantsAvailable.getItems().add(label);
        }
    }

    @FXML
    public void handleAddParticipant() {
        String sel = participantsAvailable.getSelectionModel().getSelectedItem();
        if (sel != null) {
            participantsAvailable.getItems().remove(sel);
            participantsSelected.getItems().add(sel);
        }
    }

    @FXML
    public void handleRemoveParticipant() {
        String sel = participantsSelected.getSelectionModel().getSelectedItem();
        if (sel != null) {
            participantsSelected.getItems().remove(sel);
            participantsAvailable.getItems().add(sel);
        }
    }

    @FXML
    public void handleSave() {
        errorLabel.setVisible(false);

        // ── Validation ──
        String titre = titreField.getText().trim();
        if (titre.isEmpty()) { showError("Le titre est obligatoire."); return; }
        if (titre.length() < 3) { showError("Le titre doit contenir au moins 3 caractères."); return; }

        if (dateDebutPicker.getValue() == null) { showError("La date de début est obligatoire."); return; }
        if (dateFinPicker.getValue() == null) { showError("La date de fin est obligatoire."); return; }

        LocalTime heureDebut, heureFin;
        try {
            heureDebut = LocalTime.parse(heureDebutField.getText().trim());
        } catch (Exception e) { showError("Format heure début invalide (HH:mm)."); return; }
        try {
            heureFin = LocalTime.parse(heureFinField.getText().trim());
        } catch (Exception e) { showError("Format heure fin invalide (HH:mm)."); return; }

        LocalDateTime dateDebut = LocalDateTime.of(dateDebutPicker.getValue(), heureDebut);
        LocalDateTime dateFin   = LocalDateTime.of(dateFinPicker.getValue(), heureFin);

        if (!dateFin.isAfter(dateDebut)) { showError("La date de fin doit être après la date de début."); return; }

        if (organisateurCombo.getValue() == null) { showError("Veuillez choisir un organisateur."); return; }
        int organisateurId = Integer.parseInt(organisateurCombo.getValue().split(" - ")[0]);

        String lien = lienMeetField.getText().trim();
        if (!lien.isEmpty() && !lien.startsWith("http")) { showError("Le lien doit commencer par http:// ou https://"); return; }

        // ── Save ──
        Meet m = meetToEdit != null ? meetToEdit : new Meet();
        m.setTitre(titre);
        m.setDescription(descriptionArea.getText().trim());
        m.setDateDebut(Timestamp.valueOf(dateDebut));
        m.setDateFin(Timestamp.valueOf(dateFin));
        m.setLienMeet(lien.isEmpty() ? null : lien);
        m.setParticipantId(organisateurId);

        if (meetToEdit == null) {
            meetController.ajouterMeet(m);
        } else {
            meetController.modifierMeet(m);
            // retirer anciens participants
            List<tn.esprit.entities.meet.Meet_Participants> existing = mpController.getParticipantsDuMeet(m.getId());
            for (tn.esprit.entities.meet.Meet_Participants ex : existing) {
                mpController.retirerParticipantDuMeet(m.getId(), ex.getParticipantId());
            }
        }

        // Ajouter participants sélectionnés
        for (String sel : participantsSelected.getItems()) {
            int pId = Integer.parseInt(sel.split(" - ")[0]);
            mpController.ajouterParticipantAuMeet(m.getId(), pId);
        }

        parentController.refreshTable();
        ((Stage) titreField.getScene().getWindow()).close();
    }

    @FXML
    public void handleCancel() {
        ((Stage) titreField.getScene().getWindow()).close();
    }

    private void showError(String msg) {
        errorLabel.setText("⚠️ " + msg);
        errorLabel.setVisible(true);
    }
}
