package tn.esprit.controllers.front;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
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
import tn.esprit.views.JitsiMeetRoom;
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
                // Etudiant Logic or other Teacher logic
                if (isJoined) {
                    Label joinedLbl = new Label("✓ Inscrit");
                    joinedLbl.setStyle("-fx-text-fill:#0FB5A9; -fx-font-weight:800; -fx-background-color: rgba(15,181,169,0.1); -fx-padding: 8 16; -fx-background-radius: 12;");

                    Button linkBtn = new Button("📹 Rejoindre");
                    linkBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:12; -fx-padding:8 16; -fx-font-weight:800; -fx-cursor:hand; -fx-effect: dropshadow(gaussian, rgba(15,181,169,0.3), 10, 0, 0, 4);");
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
        JitsiMeetRoom room = new JitsiMeetRoom(meet);
        room.show();
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

        GridPane form = new GridPane();
        form.setHgap(12); form.setVgap(12);
        form.setPrefWidth(500);

        TextField titreField = new TextField(mToEdit != null ? mToEdit.getTitre() : "");
        titreField.setPromptText("Titre de la réunion");

        TextArea descField = new TextArea(mToEdit != null && mToEdit.getDescription() != null ? mToEdit.getDescription() : "");
        descField.setPromptText("Description ou ordre du jour");
        descField.setPrefRowCount(2);

        // Date selection
        DatePicker datePickDebut = new DatePicker();
        TextField timeDebut = new TextField(mToEdit != null && mToEdit.getDateDebut() != null
            ? mToEdit.getDateDebut().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "10:00");
        timeDebut.setPromptText("HH:mm");
        if(mToEdit != null && mToEdit.getDateDebut() != null) datePickDebut.setValue(mToEdit.getDateDebut().toLocalDateTime().toLocalDate());

        DatePicker datePickFin = new DatePicker();
        TextField timeFin = new TextField(mToEdit != null && mToEdit.getDateFin() != null
            ? mToEdit.getDateFin().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm")) : "11:00");
        timeFin.setPromptText("HH:mm");
        if(mToEdit != null && mToEdit.getDateFin() != null) datePickFin.setValue(mToEdit.getDateFin().toLocalDateTime().toLocalDate());

        HBox debBox = new HBox(8, datePickDebut, timeDebut);
        HBox finBox = new HBox(8, datePickFin, timeFin);

        TextField lienField = new TextField(mToEdit != null && mToEdit.getLienMeet() != null ? mToEdit.getLienMeet() : "");
        lienField.setPromptText("URL : Meet.google.com/xyz");

        Label titreErr = new Label(); titreErr.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11;");
        Label dateErr = new Label(); dateErr.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11;");

        ScrollPane sp = new ScrollPane();
        VBox participantBox = new VBox(5);
        sp.setContent(participantBox);
        sp.setPrefViewportHeight(100);
        sp.setStyle("-fx-background-color: transparent;");

        List<participant> allParticipants = participantController.recupererParticipants();
        Map<CheckBox, participant> cbMap = new HashMap<>();

        for(participant p : allParticipants) {
            if(currentParticipant == null || p.getId() != currentParticipant.getId()) {
                CheckBox cb = new CheckBox(p.getNom() + " " + p.getPrenom() + " (" + p.getEmail() + ")");
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
        form.add(new Label("Lien *"), 0, 6); form.add(lienField, 1, 6);
        form.add(new Label("Participants"), 0, 7); form.add(sp, 1, 7);

        dlg.getDialogPane().setContent(form);

        // Validation - Contrôle de Saisie
        Button okButton = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Enregistrer");
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
                    String[] td = timeDebut.getText().split(":");
                    String[] tf = timeFin.getText().split(":");
                    LocalDateTime dtD = datePickDebut.getValue().atTime(Integer.parseInt(td[0]), Integer.parseInt(td[1]));
                    LocalDateTime dtF = datePickFin.getValue().atTime(Integer.parseInt(tf[0]), Integer.parseInt(tf[1]));
                    if (!dtD.isBefore(dtF)) {
                        dateErr.setText("⚠️ Date de fin doit être après.");
                        valid = false;
                    }
                } catch(Exception ex) {
                    dateErr.setText("⚠️ Format heure (HH:mm).");
                    valid = false;
                }
            }
            if(lienField.getText().trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Mettez un lien !").show();
                valid = false;
            }

            if (!valid) e.consume();
        });

        dlg.showAndWait().ifPresent(res -> {
            if (res == ButtonType.OK) {
                String[] td = timeDebut.getText().split(":");
                String[] tf = timeFin.getText().split(":");
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
                    Meet nM = new Meet(titreField.getText(), descField.getText(), tsD, tsF, lienField.getText(), currentParticipant.getId());
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
                    mToEdit.setLienMeet(lienField.getText());
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
}
