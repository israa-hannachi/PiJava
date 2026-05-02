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
import tn.esprit.services.meet.AISchedulingService;
import tn.esprit.services.meet.MeetService;

import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
    @FXML private Button checkScheduleBtn;

    private final MeetController meetController = new MeetController();
    private final ParticipantController participantController = new ParticipantController();
    private final MeetParticipantsController mpController = new MeetParticipantsController();
    private final MeetService meetService = new MeetService();

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

        // Setup AI scheduling check button
        if (checkScheduleBtn != null) {
            checkScheduleBtn.setOnAction(e -> checkScheduleConflicts());
        }
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
        if (titre.length() < 3) { showError("Le titre doit contenir au moins 3 caracteres."); return; }

        if (dateDebutPicker.getValue() == null) { showError("La date de debut est obligatoire."); return; }
        if (dateFinPicker.getValue() == null) { showError("La date de fin est obligatoire."); return; }

        LocalTime heureDebut, heureFin;
        try {
            heureDebut = LocalTime.parse(heureDebutField.getText().trim());
        } catch (Exception e) { showError("Format heure debut invalide (HH:mm)."); return; }
        try {
            heureFin = LocalTime.parse(heureFinField.getText().trim());
        } catch (Exception e) { showError("Format heure fin invalide (HH:mm)."); return; }

        LocalDateTime dateDebut = LocalDateTime.of(dateDebutPicker.getValue(), heureDebut);
        LocalDateTime dateFin   = LocalDateTime.of(dateFinPicker.getValue(), heureFin);

        if (!dateFin.isAfter(dateDebut)) { showError("La date de fin doit etre apres la date de debut."); return; }

        if (organisateurCombo.getValue() == null) { showError("Veuillez choisir un organisateur."); return; }
        int organisateurId = Integer.parseInt(organisateurCombo.getValue().split(" - ")[0]);

        String lien = lienMeetField.getText().trim();
        if (!lien.isEmpty() && !lien.startsWith("http")) { showError("Le lien doit commencer par http:// ou https://"); return; }

        // ── AI Scheduling Check ──
        List<Integer> selectedParticipantIds = new ArrayList<>();
        for (String sel : participantsSelected.getItems()) {
            selectedParticipantIds.add(Integer.parseInt(sel.split(" - ")[0]));
        }
        // Add organizer if not already in list
        if (!selectedParticipantIds.contains(organisateurId)) {
            selectedParticipantIds.add(organisateurId);
        }

        // Check conflicts before saving
        if (!selectedParticipantIds.isEmpty()) {
            try {
                AISchedulingService.SchedulingSuggestion suggestion = meetService.checkConflictsAndSuggest(
                    Timestamp.valueOf(dateDebut), Timestamp.valueOf(dateFin),
                    selectedParticipantIds, meetToEdit != null ? meetToEdit.getId() : null
                );

                if (suggestion.hasConflicts()) {
                    boolean proceed = showConflictWarning(suggestion);
                    if (!proceed) return;
                }
            } catch (SQLException e) {
                // Log error but don't block save
                System.err.println("AI Scheduling check failed: " + e.getMessage());
            }
        }

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

        // Ajouter participants selectionnes
        for (String sel : participantsSelected.getItems()) {
            int pId = Integer.parseInt(sel.split(" - ")[0]);
            mpController.ajouterParticipantAuMeet(m.getId(), pId);
        }

        parentController.refreshTable();
        ((Stage) titreField.getScene().getWindow()).close();
    }

    @FXML
    public void checkScheduleConflicts() {
        errorLabel.setVisible(false);

        // Validate dates first
        if (dateDebutPicker.getValue() == null || dateFinPicker.getValue() == null) {
            showError("Veuillez definir les dates de debut et fin.");
            return;
        }

        LocalTime heureDebut, heureFin;
        try {
            heureDebut = LocalTime.parse(heureDebutField.getText().trim());
        } catch (Exception e) { showError("Format heure debut invalide (HH:mm)."); return; }
        try {
            heureFin = LocalTime.parse(heureFinField.getText().trim());
        } catch (Exception e) { showError("Format heure fin invalide (HH:mm)."); return; }

        LocalDateTime dateDebut = LocalDateTime.of(dateDebutPicker.getValue(), heureDebut);
        LocalDateTime dateFin   = LocalDateTime.of(dateFinPicker.getValue(), heureFin);

        // Get selected participants
        List<Integer> selectedParticipantIds = new ArrayList<>();
        for (String sel : participantsSelected.getItems()) {
            selectedParticipantIds.add(Integer.parseInt(sel.split(" - ")[0]));
        }

        // Add organizer
        if (organisateurCombo.getValue() != null) {
            int orgId = Integer.parseInt(organisateurCombo.getValue().split(" - ")[0]);
            if (!selectedParticipantIds.contains(orgId)) {
                selectedParticipantIds.add(orgId);
            }
        }

        if (selectedParticipantIds.isEmpty()) {
            showError("Veuillez selectionner au moins un participant.");
            return;
        }

        // Check conflicts
        try {
            AISchedulingService.SchedulingSuggestion suggestion = meetService.checkConflictsAndSuggest(
                Timestamp.valueOf(dateDebut), Timestamp.valueOf(dateFin),
                selectedParticipantIds, meetToEdit != null ? meetToEdit.getId() : null
            );

            showSchedulingDialog(suggestion);

        } catch (SQLException e) {
            showError("Erreur lors de la verification: " + e.getMessage());
        }
    }

    private boolean showConflictWarning(AISchedulingService.SchedulingSuggestion suggestion) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conflits detectes");
        alert.setHeaderText(suggestion.getConflicts().size() + " participant(s) deja occupes");

        StringBuilder content = new StringBuilder();
        content.append("Conflicts details:\n\n");

        // Group conflicts by meet
        java.util.Map<Integer, List<AISchedulingService.ConflictInfo>> byMeet = new java.util.HashMap<>();
        for (AISchedulingService.ConflictInfo c : suggestion.getConflicts()) {
            byMeet.computeIfAbsent(c.getMeetId(), k -> new ArrayList<>()).add(c);
        }

        for (List<AISchedulingService.ConflictInfo> conflicts : byMeet.values()) {
            AISchedulingService.ConflictInfo first = conflicts.get(0);
            content.append("\"").append(first.getMeetTitle()).append("\"\n");
            content.append("  ").append(first.getMeetStart().toString().substring(0, 16))
                   .append(" - ").append(first.getMeetEnd().toString().substring(11, 16)).append("\n");
            content.append("  ").append(conflicts.size()).append(" participant(s) en commun\n\n");
        }

        if (suggestion.hasAlternatives()) {
            content.append("\nCreneau alternatif suggere:\n");
            AISchedulingService.TimeSlot best = suggestion.getBestAlternative();
            content.append(best.formatRange()).append(" (").append(best.getExplanation()).append(")");
        }

        content.append("\n\nVoulez-vous continuer malgre les conflits?");

        alert.setContentText(content.toString());

        ButtonType btnContinue = new ButtonType("Continuer", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancel = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnContinue, btnCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == btnContinue;
    }

    private void showSchedulingDialog(AISchedulingService.SchedulingSuggestion suggestion) {
        if (!suggestion.hasConflicts()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Planification");
            alert.setHeaderText("Creneau optimal");
            alert.setContentText("Aucun conflit detecte pour ce creneau.\n\n" + suggestion.getReasoning());
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Planification intelligente");
        alert.setHeaderText(suggestion.getConflicts().size() + " conflit(s) detecte(s)");

        StringBuilder content = new StringBuilder();
        content.append("=== CONFLITS ===\n\n");

        // Group by meet
        java.util.Map<Integer, List<AISchedulingService.ConflictInfo>> byMeet = new java.util.HashMap<>();
        for (AISchedulingService.ConflictInfo c : suggestion.getConflicts()) {
            byMeet.computeIfAbsent(c.getMeetId(), k -> new ArrayList<>()).add(c);
        }

        for (List<AISchedulingService.ConflictInfo> conflicts : byMeet.values()) {
            AISchedulingService.ConflictInfo first = conflicts.get(0);
            content.append("\"").append(first.getMeetTitle()).append("\"\n");
            content.append("  Date: ").append(first.getMeetStart().toString().substring(0, 16)).append("\n");
            content.append("  Participants en conflit:\n");
            for (AISchedulingService.ConflictInfo c : conflicts) {
                content.append("    - ").append(c.getParticipantName()).append("\n");
            }
            content.append("\n");
        }

        if (suggestion.hasAlternatives()) {
            content.append("=== SUGGESTIONS ===\n\n");
            content.append("Creneaux alternatifs trouves:\n");
            for (int i = 0; i < Math.min(3, suggestion.getAlternatives().size()); i++) {
                AISchedulingService.TimeSlot slot = suggestion.getAlternatives().get(i);
                content.append((i+1)).append(". ").append(slot.formatRange()).append("\n");
                content.append("   ").append(slot.getExplanation()).append("\n\n");
            }
        }

        if (suggestion.hasSubgroupSuggestions()) {
            content.append("=== SOUS-GROUPES ===\n\n");
            for (AISchedulingService.SubgroupSuggestion sg : suggestion.getSubgroups()) {
                content.append(sg.getName()).append(" (").append(sg.getParticipantIds().size()).append(" participants)\n");
                content.append("  ").append(sg.getExplanation()).append("\n");
                content.append("  Score: ").append(sg.getFeasibilityScore()).append("%\n\n");
            }
        }

        content.append("\n=== ANALYSE ===\n");
        content.append(suggestion.getReasoning());

        alert.setContentText(content.toString());

        // Add button to apply best alternative
        if (suggestion.hasAlternatives()) {
            ButtonType btnApply = new ButtonType("Appliquer suggestion", ButtonBar.ButtonData.APPLY);
            ButtonType btnClose = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnApply, btnClose);

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == btnApply) {
                AISchedulingService.TimeSlot best = suggestion.getBestAlternative();
                LocalDateTime start = best.getStart().toLocalDateTime();
                LocalDateTime end = best.getEnd().toLocalDateTime();

                dateDebutPicker.setValue(start.toLocalDate());
                heureDebutField.setText(String.format("%02d:%02d", start.getHour(), start.getMinute()));
                dateFinPicker.setValue(end.toLocalDate());
                heureFinField.setText(String.format("%02d:%02d", end.getHour(), end.getMinute()));
            }
        } else {
            alert.showAndWait();
        }
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
