package tn.esprit.controllers.front;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.controllers.meet.MeetController;
import tn.esprit.controllers.meet.MeetParticipantsController;
import tn.esprit.controllers.meet.ParticipantController;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.Meet_Participants;
import tn.esprit.entities.meet.participant;
import tn.esprit.entities.users.Users;
import tn.esprit.services.meet.MeetService;
import tn.esprit.services.mail.EmailService;
import tn.esprit.services.mail.SmtpConfig;

import jakarta.mail.MessagingException;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class FrontMeetListController implements Initializable {

    @FXML private FlowPane meetContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterCombo;
    @FXML private Label countLabel;
    @FXML private Label profBadge;
    @FXML private Button addMeetBtn;
    @FXML private Button profileButton;
    @FXML private VBox emptyState;

    private final MeetController meetController = new MeetController();
    private final MeetService meetService = new MeetService();
    private final ParticipantController participantController = new ParticipantController();
    private final MeetParticipantsController mpController = new MeetParticipantsController();

    private Users currentUser;
    private participant currentParticipant;
    private List<Meet> allMeets = new ArrayList<>();
    private boolean isProf = false;

    // A formatter for Timestamp visualization
    private final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterCombo.setItems(FXCollections.observableArrayList("Tous", "À venir", "Passés"));
        filterCombo.getSelectionModel().selectFirst();
    }

    public void initUser(Users user) {
        this.currentUser = user;

        if (user != null) {
            profileButton.setText(user.getFirstName());
            String role = user.getRole();
            // Prof = "Enseignant" role
            isProf = "Enseignant".equalsIgnoreCase(role) || "Professeur".equalsIgnoreCase(role);

            // Sync with participant table based on email
            syncParticipant();
        } else {
            // Test debug: default to true to allow creating without login during dev
            isProf = false;
        }

        if (isProf) {
            addMeetBtn.setVisible(true);
            addMeetBtn.setManaged(true);
            profBadge.setVisible(true);
            profBadge.setManaged(true);
        }

        loadData();
    }

    private void syncParticipant() {
        if (currentUser == null) return;
        List<participant> parts = participantController.recupererParticipants();
        Optional<participant> match = parts.stream()
            .filter(p -> p.getEmail().equalsIgnoreCase(currentUser.getEmail()))
            .findFirst();

        if (match.isPresent()) {
            this.currentParticipant = match.get();
        } else {
            // Need to auto-register
            participant newP = new participant(
                currentUser.getLastName(),
                currentUser.getFirstName(),
                currentUser.getEmail(),
                currentUser.getRole()
            );
            participantController.ajouterParticipant(newP);
            // Fetch again avoiding manual ID fetching implementation hassle
            List<participant> updatedParts = participantController.recupererParticipants();
            this.currentParticipant = updatedParts.stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(currentUser.getEmail()))
                .findFirst().orElse(newP); // Fail safe
        }
    }

    private void loadData() {
        List<Meet> fetchedMeets = meetController.recupererMeets();
        if (currentParticipant != null) {
            if (isProf) {
                allMeets = fetchedMeets.stream()
                    .filter(m -> m.getParticipantId() == currentParticipant.getId())
                    .collect(Collectors.toList());
            } else {
                List<Meet_Participants> joinedMeets = mpController.getMeetsDuParticipant(currentParticipant.getId());
                Set<Integer> joinedMeetIds = joinedMeets.stream().map(mp -> mp.getMeetId()).collect(Collectors.toSet());
                allMeets = fetchedMeets.stream()
                    .filter(m -> joinedMeetIds.contains(m.getId()))
                    .collect(Collectors.toList());
            }
        } else {
            allMeets = new ArrayList<>();
        }
        applyFilters();
    }

    private void renderCards(List<Meet> list) {
        meetContainer.getChildren().clear();
        boolean empty = list.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        countLabel.setText(list.size() + " session(s)");

        for (Meet m : list) {
            VBox card = new VBox(12);
            card.setPrefWidth(340);
            card.setStyle("-fx-background-color:white; -fx-background-radius:20; -fx-padding:24; " +
                "-fx-border-color:rgba(15,181,169,0.15); -fx-border-radius:20; " +
                "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.06), 18, 0, 0, 8);");

            // Top row: status + dates
            HBox topRow = new HBox(12);
            topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            boolean isPast = m.getDateFin() != null && m.getDateFin().before(new Date());

            Label statusLabel = new Label(isPast ? "Inactif / Passé" : "En cours");
            statusLabel.setStyle(isPast ?
                "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-background-radius:12; -fx-padding:4 14; -fx-font-weight:800; -fx-font-size:12;" :
                "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-background-radius:12; -fx-padding:4 14; -fx-font-weight:800; -fx-font-size:12;"
            );

            Region sp = new Region();
            HBox.setHgrow(sp, Priority.ALWAYS);

            String startDateStr = m.getDateDebut() != null ? m.getDateDebut().toLocalDateTime().format(dtFormatter) : "N/A";
            Label dateLabel = new Label("📅 " + startDateStr);
            dateLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-font-weight:700;");

            topRow.getChildren().addAll(statusLabel, sp, dateLabel);

            Label titreLabel = new Label(m.getTitre());
            titreLabel.setStyle("-fx-font-size:18px; -fx-font-weight:900; -fx-text-fill:#1f2937; -fx-wrap-text:true;");
            titreLabel.setWrapText(true);

            Label descLabel = new Label(m.getDescription() != null && !m.getDescription().isEmpty()
                ? m.getDescription() : "");
            descLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:13; -fx-wrap-text:true; -fx-line-spacing: 4px;");
            descLabel.setWrapText(true);

            // Footer actions -> separate line, logic role specific
            HBox footer = new HBox(8);
            footer.setStyle("-fx-border-color:#f1f5f9; -fx-border-width:1 0 0 0; -fx-padding:14 0 0 0;");
            footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

            // Check if current participant belongs to this Meet
            boolean isJoined = isUserJoined(m);
            boolean isOwner = (currentParticipant != null && m.getParticipantId() == currentParticipant.getId());

            if (isProf && isOwner) {
                // Own Meet Management
                Button editBtn = new Button("✏️");
                editBtn.setStyle("-fx-background-color:#fef9c3; -fx-text-fill:#ca8a04; " +
                    "-fx-background-radius:10; -fx-padding:8 12; -fx-font-size:13; -fx-cursor:hand;");
                editBtn.setTooltip(new Tooltip("Modifier ce Meet"));
                editBtn.setOnAction(e -> handleEditMeet(m));

                Button delBtn = new Button("🗑️");
                delBtn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#ef4444; " +
                    "-fx-background-radius:10; -fx-padding:8 12; -fx-font-size:13; -fx-cursor:hand;");
                delBtn.setTooltip(new Tooltip("Supprimer ce Meet"));
                delBtn.setOnAction(e -> handleDeleteMeet(m));

                Button linkBtn = new Button("📹 Rejoindre");
                linkBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:12; -fx-padding:8 16; -fx-font-weight:800; -fx-cursor:hand; -fx-effect: dropshadow(gaussian, rgba(15,181,169,0.3), 10, 0, 0, 4);");
                linkBtn.setOnAction(e -> openJitsiRoom(m));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);
                footer.getChildren().addAll(linkBtn, spacer, editBtn, delBtn);

            } else {
                // Etudiant Logic or other Teacher logic (Uniformized colors with Prof mode)
                if (isJoined) {
                    Label joinedLbl = new Label("Inscrit");
                    joinedLbl.setStyle("-fx-text-fill:#0FB5A9; -fx-font-weight:800; -fx-background-color: rgba(15,181,169,0.1); -fx-padding: 8 16; -fx-background-radius: 12;");

                    Button linkBtn = new Button("📹 Rejoindre");
                    linkBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:12; -fx-padding:8 18; -fx-font-weight:800; -fx-cursor:hand; -fx-effect: dropshadow(gaussian, rgba(15,181,169,0.3), 10, 0, 0, 4);");
                    linkBtn.setOnAction(e -> openJitsiRoom(m));
                    
                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);
                    footer.getChildren().addAll(joinedLbl, spacer, linkBtn);
                } else {
                    Button joinBtn = new Button("✨ Participer");
                    joinBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:12; -fx-padding:8 18; -fx-font-weight:800; -fx-cursor:hand; -fx-effect: dropshadow(gaussian, rgba(15,181,169,0.3), 10, 0, 0, 4);");
                    joinBtn.setOnAction(e -> handleJoinMeet(m));
                    footer.getChildren().add(joinBtn);
                }
            }

            card.getChildren().addAll(topRow, titreLabel);
            if (!descLabel.getText().isEmpty()) card.getChildren().add(descLabel);
            card.getChildren().add(footer);

            meetContainer.getChildren().add(card);
        }
    }

    private boolean isUserJoined(Meet m) {
        if (currentParticipant == null) return false;
        List<Meet_Participants> parts = mpController.getParticipantsDuMeet(m.getId());
        return parts.stream().anyMatch(mp -> mp.getParticipantId() == currentParticipant.getId());
    }

    private void handleJoinMeet(Meet m) {
        if (currentParticipant == null) {
            new Alert(Alert.AlertType.ERROR, "Vous n'êtes pas reconnu comme participant, rejoignez avec le bon compte !").show();
            return;
        }
        mpController.ajouterParticipantAuMeet(m.getId(), currentParticipant.getId());

        new Alert(Alert.AlertType.INFORMATION, "Vous vous êtes enregistré sur le Meet : " + m.getTitre()).showAndWait();
        loadData();
    }

    private void openJitsiRoom(Meet meet) {
        // Vérifier que la réunion a commencé
        java.sql.Timestamp now = java.sql.Timestamp.valueOf(LocalDateTime.now());
        if (meet.getDateDebut() != null && meet.getDateDebut().after(now)) {
            new Alert(Alert.AlertType.WARNING, "Cette réunion n'a pas encore commencé.\n\nDébut prévu : " + meet.getDateDebut()).show();
            return;
        }
        // Vérifier que la réunion n'est pas terminée
        if (meet.getDateFin() != null && meet.getDateFin().before(now)) {
            new Alert(Alert.AlertType.WARNING, "Cette réunion est terminée.\n\nFin : " + meet.getDateFin()).show();
            return;
        }

        // Naviguer vers la page Meet Room intégrée
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_MeetRoom.fxml"));
            Parent root = loader.load();
            FrontMeetRoomController ctrl = loader.getController();
            ctrl.initData(currentUser, meet, currentParticipant);

            Stage stage = (Stage) meetContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Impossible d'ouvrir la salle : " + e.getMessage()).show();
        }
    }

    // ─── CRUD (PROF ONLY) ────────────────────────────────────────────────────────
    @FXML
    public void handleAddMeet() {
        showMeetFormDialog(null);
    }

    private void handleEditMeet(Meet m) {
        showMeetFormDialog(m);
    }

    private void handleDeleteMeet(Meet m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la réunion");
        confirm.setContentText("Supprimer \"" + m.getTitre() + "\" définitivement ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            meetController.supprimerMeet(m.getId());
            loadData();
        }
    }

    private void showMeetFormDialog(Meet mToEdit) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(mToEdit == null ? "Ajouter une Réunion" : "Modifier la Réunion");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setPrefWidth(640);
        dlg.getDialogPane().setStyle("-fx-background-color:#F0FFFE; -fx-font-family:'Segoe UI';");

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(10);
        form.setPadding(new Insets(18, 18, 18, 18));
        form.setStyle("-fx-background-color:white; -fx-background-radius:18; -fx-border-color:rgba(15,181,169,0.15); -fx-border-radius:18; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 18, 0, 0, 8);");

        TextField titreField = new TextField(mToEdit != null ? mToEdit.getTitre() : "");
        titreField.setPromptText("Titre de la réunion");
        titreField.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:12; -fx-border-color:#E2E8F0; -fx-border-radius:12; -fx-padding:10 12; -fx-font-weight:700;");

        TextArea descField = new TextArea(mToEdit != null && mToEdit.getDescription() != null ? mToEdit.getDescription() : "");
        descField.setPromptText("Description ou ordre du jour");
        descField.setPrefRowCount(2);
        descField.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:12; -fx-border-color:#E2E8F0; -fx-border-radius:12; -fx-padding:10 12;");

        // Date selection
        DatePicker datePickDebut = new DatePicker();
        datePickDebut.setStyle("-fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#E2E8F0;");
        TextField timeDebut = new TextField(mToEdit != null && mToEdit.getDateDebut() != null
            ? mToEdit.getDateDebut().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "10:00");
        timeDebut.setPromptText("HH:mm");
        timeDebut.setPrefWidth(90);
        timeDebut.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:12; -fx-border-color:#E2E8F0; -fx-border-radius:12; -fx-padding:10 12; -fx-font-weight:700;");
        if(mToEdit != null && mToEdit.getDateDebut() != null) datePickDebut.setValue(mToEdit.getDateDebut().toLocalDateTime().toLocalDate());

        DatePicker datePickFin = new DatePicker();
        datePickFin.setStyle("-fx-background-radius:12; -fx-border-radius:12; -fx-border-color:#E2E8F0;");
        TextField timeFin = new TextField(mToEdit != null && mToEdit.getDateFin() != null
            ? mToEdit.getDateFin().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "11:00");
        timeFin.setPromptText("HH:mm");
        timeFin.setPrefWidth(90);
        timeFin.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:12; -fx-border-color:#E2E8F0; -fx-border-radius:12; -fx-padding:10 12; -fx-font-weight:700;");
        if(mToEdit != null && mToEdit.getDateFin() != null) datePickFin.setValue(mToEdit.getDateFin().toLocalDateTime().toLocalDate());

        HBox debBox = new HBox(8, datePickDebut, timeDebut);
        HBox finBox = new HBox(8, datePickFin, timeFin);

        TextField lienField = new TextField(mToEdit != null && mToEdit.getLienMeet() != null ? mToEdit.getLienMeet() : "");
        lienField.setPromptText("Lien (optionnel)");
        lienField.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:12; -fx-border-color:#E2E8F0; -fx-border-radius:12; -fx-padding:10 12;");

        Label titreErr = new Label(); titreErr.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11; -fx-font-weight:700;");
        Label dateErr = new Label(); dateErr.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11; -fx-font-weight:700;");

        ScrollPane sp = new ScrollPane();
        VBox participantBox = new VBox(5);
        sp.setContent(participantBox);
        sp.setPrefViewportHeight(100);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");

        List<participant> allParticipants = participantController.recupererParticipants();
        Map<CheckBox, participant> cbMap = new HashMap<>();

        for(participant p : allParticipants) {
            if(currentParticipant == null || p.getId() != currentParticipant.getId()) {
                String displayName = ((p.getNom() == null ? "" : p.getNom().trim()) + " " + (p.getPrenom() == null ? "" : p.getPrenom().trim())).trim();
                if (displayName.isEmpty()) {
                    displayName = p.getEmail() != null ? p.getEmail().trim() : ("Participant #" + p.getId());
                }
                String displayEmail = p.getEmail() != null && !p.getEmail().trim().isEmpty() ? p.getEmail().trim() : "";
                CheckBox cb = new CheckBox(displayEmail.isEmpty() ? displayName : (displayName + " (" + displayEmail + ")"));
                cb.setWrapText(true);
                cb.setMaxWidth(Double.MAX_VALUE);
                cb.setStyle("-fx-text-fill:#334155; -fx-font-weight:700;");
                cbMap.put(cb, p);
                participantBox.getChildren().add(cb);

                if(mToEdit != null && mpController.isParticipantInscrit(mToEdit.getId(), p.getId())) {
                    cb.setSelected(true);
                }
            }
        }

        form.add(new Label("Titre *"), 0, 0); form.add(titreField, 1, 0);
        form.add(titreErr, 1, 1);
        form.add(new Label("Description"), 0, 2); form.add(descField, 1, 2);
        form.add(new Label("Date Début *"), 0, 3); form.add(debBox, 1, 3);
        form.add(new Label("Date Fin *"), 0, 4); form.add(finBox, 1, 4);
        form.add(dateErr, 1, 5);
        form.add(new Label("Lien"), 0, 6); form.add(lienField, 1, 6);
        form.add(new Label("Participants"), 0, 7); form.add(sp, 1, 7);

        // --- AI SCHEDULING BUTTON ---
        Button checkScheduleBtn = new Button("🔍 Vérifier Disponibilité");
        checkScheduleBtn.setStyle("-fx-background-color:white; -fx-text-fill:#0FB5A9; -fx-font-weight:800; -fx-background-radius:12; -fx-padding:8 16; -fx-border-color:#0FB5A9; -fx-border-radius:12; -fx-cursor:hand;");
        form.add(checkScheduleBtn, 1, 8);

        checkScheduleBtn.setOnAction(ev -> {
            try {
                String tdRaw = timeDebut.getText() == null ? "" : timeDebut.getText().trim();
                String tfRaw = timeFin.getText() == null ? "" : timeFin.getText().trim();
                String[] td = tdRaw.split(":");
                String[] tf = tfRaw.split(":");
                LocalDateTime dtD = datePickDebut.getValue().atTime(Integer.parseInt(td[0]), Integer.parseInt(td[1]));
                LocalDateTime dtF = datePickFin.getValue().atTime(Integer.parseInt(tf[0]), Integer.parseInt(tf[1]));

                List<Integer> selectedIds = new ArrayList<>();
                cbMap.forEach((cb, p) -> { if(cb.isSelected()) selectedIds.add(p.getId()); });
                if(currentParticipant != null) selectedIds.add(currentParticipant.getId());

                if(selectedIds.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Veuillez sélectionner au moins un participant.").show();
                    return;
                }

                tn.esprit.services.meet.AISchedulingService.SchedulingSuggestion suggestion = meetService.checkConflictsAndSuggest(
                    java.sql.Timestamp.valueOf(dtD), java.sql.Timestamp.valueOf(dtF),
                    selectedIds, mToEdit != null ? mToEdit.getId() : null
                );

                showSchedulingDialog(suggestion, datePickDebut, timeDebut, datePickFin, timeFin);

            } catch(Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur de vérification : Vérifiez le format de l'heure (HH:mm)").show();
            }
        });

        GridPane.setHgrow(titreField, Priority.ALWAYS);
        GridPane.setHgrow(descField, Priority.ALWAYS);
        GridPane.setHgrow(lienField, Priority.ALWAYS);

        VBox header = new VBox(6);
        header.setPadding(new Insets(18, 18, 14, 18));
        header.setStyle("-fx-background-color:linear-gradient(to right, #0FB5A9, #04B6D5); -fx-background-radius:18 18 0 0;");
        Label hTitle = new Label(mToEdit == null ? "✨ Nouvelle Réunion" : "✏️ Modifier la Réunion");
        hTitle.setStyle("-fx-font-size:18px; -fx-font-weight:900; -fx-text-fill:white;");
        Label hSub = new Label("Planifiez une session et invitez vos participants");
        hSub.setStyle("-fx-font-size:12px; -fx-text-fill:rgba(255,255,255,0.9); -fx-font-weight:600;");
        header.getChildren().addAll(hTitle, hSub);

        VBox content = new VBox(12, header, form);
        content.setPadding(new Insets(0, 0, 0, 0));
        dlg.getDialogPane().setContent(content);

        // Validation - Contrôle de Saisie
        Button okButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Enregistrer");
        okButton.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-font-weight:900; -fx-background-radius:12; -fx-padding:10 20; -fx-cursor:hand; -fx-effect: dropshadow(gaussian, rgba(15,181,169,0.25), 12, 0, 0, 6);");
        Button cancelButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setStyle("-fx-background-color:white; -fx-text-fill:#64748b; -fx-font-weight:800; -fx-background-radius:12; -fx-padding:10 20; -fx-border-color:#E2E8F0; -fx-border-radius:12; -fx-cursor:hand;");
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, e -> {
            boolean valid = true;
            titreErr.setText("");
            dateErr.setText("");

            if (titreField.getText() == null || titreField.getText().trim().isEmpty()) {
                titreErr.setText("⚠️ Le titre est obligatoire.");
                valid = false;
            } else if (titreField.getText().trim().length() < 3) {
                titreErr.setText("⚠️ Titre trop court (> 3 chars).");
                valid = false;
            }

            if (datePickDebut.getValue() == null || datePickFin.getValue() == null) {
                dateErr.setText("⚠️ Remplissez les jours.");
                valid = false;
            } else {
                try {
                    String tdRaw = timeDebut.getText() == null ? "" : timeDebut.getText().trim();
                    String tfRaw = timeFin.getText() == null ? "" : timeFin.getText().trim();
                    String[] td = tdRaw.split(":");
                    String[] tf = tfRaw.split(":");
                    LocalDateTime dtD = datePickDebut.getValue().atTime(Integer.parseInt(td[0]), Integer.parseInt(td[1]));
                    LocalDateTime dtF = datePickFin.getValue().atTime(Integer.parseInt(tf[0]), Integer.parseInt(tf[1]));
                    if (!dtD.isBefore(dtF)) {
                        dateErr.setText("⚠️ La date/heure de fin doit être après la date/heure de début.");
                        valid = false;
                    }
                } catch(Exception ex) {
                    dateErr.setText("⚠️ Format heure invalide (HH:mm attendu). Exemple: 09:30");
                    valid = false;
                }
            }

            if (!valid) e.consume();
        });

        dlg.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String[] td = timeDebut.getText().trim().split(":");
                String[] tf = timeFin.getText().trim().split(":");
                LocalDateTime dtD = datePickDebut.getValue().atTime(Integer.parseInt(td[0]), Integer.parseInt(td[1]));
                LocalDateTime dtF = datePickFin.getValue().atTime(Integer.parseInt(tf[0]), Integer.parseInt(tf[1]));

                Timestamp tsD = Timestamp.valueOf(dtD);
                Timestamp tsF = Timestamp.valueOf(dtF);

                List<participant> selectedParticipants = new ArrayList<>();
                for (Map.Entry<CheckBox, participant> entry : cbMap.entrySet()) {
                    if (entry.getKey().isSelected()) {
                        selectedParticipants.add(entry.getValue());
                    }
                }

                if (mToEdit == null) {
                    String lien = lienField.getText() == null ? "" : lienField.getText().trim();
                    if (!lien.isEmpty() && !lien.startsWith("http")) lien = "https://" + lien;
                    Meet nM = new Meet(titreField.getText(), descField.getText(), tsD, tsF, lien.isEmpty() ? null : lien, currentParticipant.getId());
                    meetController.ajouterMeet(nM);

                    for (participant p : selectedParticipants) {
                        mpController.ajouterParticipantAuMeet(nM.getId(), p.getId());
                    }

                    sendEmailInvitationsAsync(nM, selectedParticipants);
                } else {
                    mToEdit.setTitre(titreField.getText());
                    mToEdit.setDescription(descField.getText());
                    mToEdit.setDateDebut(tsD);
                    mToEdit.setDateFin(tsF);
                    String lien = lienField.getText() == null ? "" : lienField.getText().trim();
                    if (!lien.isEmpty() && !lien.startsWith("http")) lien = "https://" + lien;
                    mToEdit.setLienMeet(lien.isEmpty() ? null : lien);
                    meetController.modifierMeet(mToEdit);

                    List<Meet_Participants> existing = mpController.getParticipantsDuMeet(mToEdit.getId());
                    Set<Integer> existingIds = existing.stream().map(mp -> mp.getParticipantId()).collect(Collectors.toSet());

                    List<participant> newParticipants = new ArrayList<>();
                    for (Map.Entry<CheckBox, participant> entry : cbMap.entrySet()) {
                        int pId = entry.getValue().getId();
                        boolean isSelected = entry.getKey().isSelected();
                        if (isSelected && !existingIds.contains(pId)) {
                            mpController.ajouterParticipantAuMeet(mToEdit.getId(), pId);
                            newParticipants.add(entry.getValue());
                        } else if (!isSelected && existingIds.contains(pId)) {
                            mpController.retirerParticipantDuMeet(mToEdit.getId(), pId);
                        }
                    }

                    if (!newParticipants.isEmpty()) {
                        sendEmailInvitationsAsync(mToEdit, newParticipants);
                    }
                }
                loadData();
            }
        });
    }

    // ─── FILTERS / SORTING ───────────────────────────────────────────────────

    @FXML
    public void handleSearch() { applyFilters(); }

    @FXML
    public void handleFilter() { applyFilters(); }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String vis = filterCombo.getValue();

        List<Meet> filtered = allMeets.stream().filter(m -> {
            boolean matchQ = q.isEmpty()
                || m.getTitre().toLowerCase().contains(q)
                || (m.getDescription() != null && m.getDescription().toLowerCase().contains(q));

            boolean matchVis = true;
            if ("À venir".equals(vis)) {
                matchVis = m.getDateFin() == null || m.getDateFin().after(new Date());
            } else if ("Passés".equals(vis)) {
                matchVis = m.getDateFin() != null && m.getDateFin().before(new Date());
            }
            return matchQ && matchVis;
        }).collect(Collectors.toList());

        renderCards(filtered);
    }

    @FXML public void sortAZ() {
        allMeets.sort(Comparator.comparing(Meet::getTitre, String.CASE_INSENSITIVE_ORDER));
        applyFilters();
    }
    @FXML public void sortByDateAsc() {
        allMeets.sort(Comparator.comparing(m -> m.getDateDebut() == null ? new Date(0) : m.getDateDebut()));
        applyFilters();
    }
    @FXML public void sortByDateDesc() {
        allMeets.sort(Comparator.comparing((Meet m) -> m.getDateDebut() == null ? new Date(0) : m.getDateDebut()).reversed());
        applyFilters();
    }

    @FXML public void handleHome() { navigateTo("/tn/esprit/view/front_user_dashboard.fxml"); }
    @FXML public void handleCours() { navigateTo("/tn/esprit/view/front_CoursCategories.fxml"); }
    @FXML public void handleJeux() { navigateTo("/tn/esprit/view/front_GameList.fxml"); }
    @FXML public void handleEvents() { navigateTo("/tn/esprit/view/frontEvent.fxml"); }
    @FXML public void handleForums() { navigateTo("/tn/esprit/view/front_forum.fxml"); }
    @FXML public void handleLogout() { navigateTo("/tn/esprit/view/front_login.fxml"); }
    @FXML public void handleProfile() { navigateTo("/tn/esprit/view/front_profile.fxml"); }

    @FXML
    public void handleCalendar() {
        navigateTo("/tn/esprit/view/front_MeetCalendar.fxml");
    }

    private void sendEmailInvitationsAsync(Meet meet, List<participant> participants) {
        if (currentParticipant == null || participants.isEmpty()) return;
        if (currentParticipant.getSmtpEmail() == null || currentParticipant.getSmtpAppPassword() == null) {
            System.out.println("⚠️ Organisateur sans credentials SMTP - emails non envoyés");
            return;
        }

        Thread emailThread = new Thread(() -> {
            try {
                EmailService emailService = new EmailService(SmtpConfig.gmail());
                emailService.sendMeetInvitation(currentParticipant, participants, meet);
                System.out.println("✅ Emails d'invitation envoyés à " + participants.size() + " participant(s)");
            } catch (MessagingException e) {
                System.err.println("❌ Erreur envoi emails: " + e.getMessage());
            }
        });
        emailThread.setDaemon(true);
        emailThread.start();
    }

    private void navigateTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            Object ctrl = loader.getController();
            if (ctrl instanceof FrontUserDashboardController) {
                ((FrontUserDashboardController) ctrl).initUser(currentUser);
            } else if (ctrl instanceof FrontProfileController) {
                ((FrontProfileController) ctrl).initUser(currentUser);
            } else if (ctrl instanceof FrontCoursCategorieController) {
                ((FrontCoursCategorieController) ctrl).initUser(currentUser);
            } else if (ctrl instanceof FrontMeetCalendarController) {
                ((FrontMeetCalendarController) ctrl).initUser(currentUser);
            }

            Stage stage = (Stage) meetContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
    private void showSchedulingDialog(tn.esprit.services.meet.AISchedulingService.SchedulingSuggestion suggestion, DatePicker dpD, TextField tD, DatePicker dpF, TextField tF) {
        if (!suggestion.hasConflicts()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Planification");
            alert.setHeaderText("✨ Créneau Optimal !");
            alert.setContentText("Aucun conflit détecté pour ce créneau.\n\n" + suggestion.getReasoning());
            alert.showAndWait();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Planification Intelligente");
        alert.setHeaderText(suggestion.getConflicts().size() + " conflit(s) détecté(s)");

        StringBuilder sb = new StringBuilder();
        sb.append("=== CONFLITS ===\n\n");
        java.util.Map<Integer, List<tn.esprit.services.meet.AISchedulingService.ConflictInfo>> byMeet = new java.util.HashMap<>();
        for (tn.esprit.services.meet.AISchedulingService.ConflictInfo c : suggestion.getConflicts()) {
            byMeet.computeIfAbsent(c.getMeetId(), k -> new ArrayList<>()).add(c);
        }

        for (List<tn.esprit.services.meet.AISchedulingService.ConflictInfo> conflicts : byMeet.values()) {
            tn.esprit.services.meet.AISchedulingService.ConflictInfo first = conflicts.get(0);
            sb.append("\"").append(first.getMeetTitle()).append("\"\n");
            sb.append("  📅 Date: ").append(first.getMeetStart().toString().substring(0, 16)).append("\n");
            sb.append("  ❌ Participants en conflit:\n");
            for (tn.esprit.services.meet.AISchedulingService.ConflictInfo c : conflicts) {
                sb.append("    - ").append(c.getParticipantName()).append("\n");
            }
            sb.append("\n");
        }

        if (suggestion.hasAlternatives()) {
            sb.append("=== SUGGESTIONS ===\n\n");
            for (int i = 0; i < Math.min(3, suggestion.getAlternatives().size()); i++) {
                tn.esprit.services.meet.AISchedulingService.TimeSlot slot = suggestion.getAlternatives().get(i);
                sb.append((i+1)).append(". ").append(slot.formatRange()).append("\n");
                sb.append("   💡 ").append(slot.getExplanation()).append("\n\n");
            }
        }

        sb.append("=== ANALYSE ===\n").append(suggestion.getReasoning());
        alert.getDialogPane().setPrefWidth(500);
        alert.setContentText(sb.toString());

        if (suggestion.hasAlternatives()) {
            ButtonType btnApply = new ButtonType("Appliquer suggestion", ButtonBar.ButtonData.APPLY);
            ButtonType btnClose = new ButtonType("Fermer", ButtonBar.ButtonData.CANCEL_CLOSE);
            alert.getButtonTypes().setAll(btnApply, btnClose);

            Optional<ButtonType> res = alert.showAndWait();
            if (res.isPresent() && res.get() == btnApply) {
                tn.esprit.services.meet.AISchedulingService.TimeSlot best = suggestion.getBestAlternative();
                LocalDateTime start = best.getStart().toLocalDateTime();
                LocalDateTime end = best.getEnd().toLocalDateTime();
                dpD.setValue(start.toLocalDate());
                tD.setText(String.format("%02d:%02d", start.getHour(), start.getMinute()));
                dpF.setValue(end.toLocalDate());
                tF.setText(String.format("%02d:%02d", end.getHour(), end.getMinute()));
            }
        } else {
            alert.showAndWait();
        }
    }
}
