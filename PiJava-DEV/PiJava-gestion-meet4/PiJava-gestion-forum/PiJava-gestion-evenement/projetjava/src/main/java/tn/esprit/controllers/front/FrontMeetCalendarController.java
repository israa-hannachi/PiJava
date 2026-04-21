package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
import tn.esprit.services.mail.EmailService;
import tn.esprit.services.mail.SmtpConfig;
import tn.esprit.views.JitsiMeetRoom;
import jakarta.mail.MessagingException;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class FrontMeetCalendarController implements Initializable {

    @FXML private Label monthYearLabel;
    @FXML private GridPane calendarGrid;
    @FXML private Label detailDayLabel;
    @FXML private VBox detailContainer;
    @FXML private VBox detailEmpty;
    @FXML private Button addMeetBtn;
    @FXML private Button addFromDetailBtn;
    @FXML private Button profileButton;
    @FXML private Label profBadge;
    @FXML private Label statsMoisLabel;
    @FXML private Label statsAVenirLabel;
    @FXML private Label statsSelectedLabel;

    private final MeetController meetCtrl = new MeetController();
    private final ParticipantController partCtrl = new ParticipantController();
    private final MeetParticipantsController mpCtrl = new MeetParticipantsController();

    private Users currentUser;
    private participant currentParticipant;
    private boolean isProf;

    private YearMonth displayedMonth;
    private LocalDate selectedDay;

    private List<Meet> allMeets = new ArrayList<>();
    private final Map<LocalDate, List<Meet>> meetsByDay = new HashMap<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        displayedMonth = YearMonth.now();
        selectedDay = null;
    }

    public void initUser(Users user) {
        this.currentUser = user;

        if (user != null) {
            profileButton.setText(user.getFirstName());
            String role = user.getRole();
            isProf = "Enseignant".equalsIgnoreCase(role) || "Professeur".equalsIgnoreCase(role);
            syncParticipant();
        } else {
            isProf = false;
        }

        if (isProf) {
            addMeetBtn.setVisible(true);
            addMeetBtn.setManaged(true);
            addFromDetailBtn.setVisible(true);
            addFromDetailBtn.setManaged(true);
            profBadge.setVisible(true);
            profBadge.setManaged(true);
        }

        reload();
    }

    private void syncParticipant() {
        if (currentUser == null) return;
        List<participant> parts = partCtrl.recupererParticipants();
        Optional<participant> match = parts.stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(currentUser.getEmail()))
                .findFirst();

        if (match.isPresent()) {
            currentParticipant = match.get();
            return;
        }

        participant newP = new participant(
                currentUser.getLastName(),
                currentUser.getFirstName(),
                currentUser.getEmail(),
                currentUser.getRole()
        );
        partCtrl.ajouterParticipant(newP);

        List<participant> updated = partCtrl.recupererParticipants();
        currentParticipant = updated.stream()
                .filter(p -> p.getEmail().equalsIgnoreCase(currentUser.getEmail()))
                .findFirst()
                .orElse(newP);
    }

    private void loadAllMeets() {
        List<Meet> fetched = meetCtrl.recupererMeets();

        if (currentParticipant == null) {
            allMeets = new ArrayList<>();
            meetsByDay.clear();
            return;
        }

        if (isProf) {
            allMeets = fetched.stream()
                    .filter(m -> m.getParticipantId() == currentParticipant.getId())
                    .collect(Collectors.toList());
        } else {
            Set<Integer> joinedIds = mpCtrl.getMeetsDuParticipant(currentParticipant.getId())
                    .stream()
                    .map(Meet_Participants::getMeetId)
                    .collect(Collectors.toSet());

            allMeets = fetched.stream()
                    .filter(m -> joinedIds.contains(m.getId()))
                    .collect(Collectors.toList());
        }

        meetsByDay.clear();
        for (Meet m : allMeets) {
            if (m.getDateDebut() == null) continue;
            LocalDate d = m.getDateDebut().toLocalDateTime().toLocalDate();
            meetsByDay.computeIfAbsent(d, k -> new ArrayList<>()).add(m);
        }

        for (List<Meet> list : meetsByDay.values()) {
            list.sort(Comparator.comparing(Meet::getDateDebut, Comparator.nullsLast(Comparator.naturalOrder())));
        }
    }

    private void renderCalendar() {
        calendarGrid.getChildren().clear();
        calendarGrid.getRowConstraints().clear();

        String monthName = displayedMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRENCH);
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
        monthYearLabel.setText(monthName + " " + displayedMonth.getYear());

        LocalDate firstDay = displayedMonth.atDay(1);
        int startOffset = firstDay.getDayOfWeek().getValue() - 1;
        int daysInMonth = displayedMonth.lengthOfMonth();

        int totalCells = startOffset + daysInMonth;
        int rows = (int) Math.ceil(totalCells / 7.0);
        rows = Math.max(rows, 5);
        rows = Math.min(rows, 6);

        for (int r = 0; r < rows; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(90);
            rc.setPrefHeight(90);
            calendarGrid.getRowConstraints().add(rc);
        }

        int cellIndex = 0;
        for (int i = 0; i < startOffset; i++) {
            calendarGrid.add(buildEmptyCell(), i % 7, i / 7);
            cellIndex++;
        }

        LocalDate today = LocalDate.now();
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = displayedMonth.atDay(day);
            List<Meet> dayMeets = meetsByDay.getOrDefault(date, Collections.emptyList());

            boolean isToday = date.equals(today);
            boolean isSelected = selectedDay != null && date.equals(selectedDay);

            VBox cell = buildDayCell(date, day, dayMeets, isToday, isSelected);
            calendarGrid.add(cell, cellIndex % 7, cellIndex / 7);
            cellIndex++;
        }

        while (cellIndex % 7 != 0) {
            calendarGrid.add(buildEmptyCell(), cellIndex % 7, cellIndex / 7);
            cellIndex++;
        }
    }

    private VBox buildEmptyCell() {
        VBox cell = new VBox();
        cell.setStyle("-fx-background-color:rgba(241,245,249,0.5); -fx-background-radius:10;");
        cell.setMinHeight(90);
        return cell;
    }

    private VBox buildDayCell(LocalDate date, int dayNum, List<Meet> dayMeets, boolean isToday, boolean isSelected) {
        VBox cell = new VBox(4);
        cell.setAlignment(Pos.TOP_LEFT);
        cell.setPadding(new Insets(8));
        cell.setMinHeight(90);

        String bgColor;
        String borderColor;
        String textColor;

        if (isSelected) {
            bgColor = "rgba(15,181,169,0.15)";
            borderColor = "#0FB5A9";
            textColor = "#0FB5A9";
        } else if (isToday) {
            bgColor = "rgba(99,102,241,0.08)";
            borderColor = "#6366f1";
            textColor = "#6366f1";
        } else {
            bgColor = "white";
            borderColor = "rgba(226,232,240,0.8)";
            textColor = "#374151";
        }

        boolean isWeekend = (date.getDayOfWeek() == DayOfWeek.SATURDAY || date.getDayOfWeek() == DayOfWeek.SUNDAY);
        if (isWeekend && !isToday && !isSelected) {
            textColor = "#94a3b8";
        }

        cell.setStyle(String.format(
                "-fx-background-color:%s; -fx-background-radius:10; -fx-border-color:%s; -fx-border-radius:10; -fx-border-width:1.5; -fx-cursor:hand;",
                bgColor,
                borderColor
        ));

        Label numLabel = new Label(String.valueOf(dayNum));
        numLabel.setStyle(String.format(
                "-fx-font-size:15; -fx-font-weight:%s; -fx-text-fill:%s;",
                (isToday || isSelected) ? "900" : "700",
                textColor
        ));
        cell.getChildren().add(numLabel);

        if (isToday) {
            Label todayBadge = new Label("Auj.");
            todayBadge.setStyle("-fx-background-color:#6366f1; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:1 6; -fx-font-size:9; -fx-font-weight:800;");
            cell.getChildren().add(todayBadge);
        }

        if (!dayMeets.isEmpty()) {
            HBox dots = new HBox(3);
            int shown = Math.min(dayMeets.size(), 3);
            for (int i = 0; i < shown; i++) {
                Label dot = new Label("●");
                dot.setStyle("-fx-text-fill:#0FB5A9; -fx-font-size:9;");
                dots.getChildren().add(dot);
            }
            if (dayMeets.size() > 3) {
                Label plus = new Label("+" + (dayMeets.size() - 3));
                plus.setStyle("-fx-text-fill:#64748b; -fx-font-size:8; -fx-font-weight:700;");
                dots.getChildren().add(plus);
            }
            cell.getChildren().add(dots);

            String titre = dayMeets.get(0).getTitre() == null ? "" : dayMeets.get(0).getTitre();
            if (titre.length() > 14) titre = titre.substring(0, 12) + "…";
            Label meetLabel = new Label(titre);
            meetLabel.setStyle("-fx-font-size:10; -fx-text-fill:#0FB5A9; -fx-font-weight:700; -fx-wrap-text:true;");
            meetLabel.setWrapText(true);
            cell.getChildren().add(meetLabel);
        }

        cell.setOnMouseClicked(e -> {
            selectedDay = date;
            renderCalendar();
            showDayDetail(date);
            updateStats();
        });

        return cell;
    }

    private void showDayDetail(LocalDate date) {
        String dayStr = date.format(DateTimeFormatter.ofPattern("EEEE d MMMM yyyy", Locale.FRENCH));
        dayStr = dayStr.substring(0, 1).toUpperCase() + dayStr.substring(1);
        detailDayLabel.setText(dayStr);

        String[] parts = dayStr.split(" ");
        if (parts.length >= 3) {
            statsSelectedLabel.setText(parts[1] + " " + parts[2]);
        } else {
            statsSelectedLabel.setText(dayStr);
        }

        detailContainer.getChildren().clear();
        List<Meet> dayMeets = meetsByDay.getOrDefault(date, Collections.emptyList());

        boolean empty = dayMeets.isEmpty();
        detailEmpty.setVisible(empty);
        detailEmpty.setManaged(empty);

        for (Meet m : dayMeets) {
            detailContainer.getChildren().add(buildMeetCard(m));
        }
    }

    private VBox buildMeetCard(Meet m) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:14; -fx-padding:14; -fx-border-color:rgba(15,181,169,0.2); -fx-border-radius:14;");

        boolean isPast = m.getDateFin() != null && m.getDateFin().before(new Date());

        Label statusLbl = new Label(isPast ? "Passé" : "● En cours / À venir");
        statusLbl.setStyle(isPast
                ? "-fx-background-color:#fee2e2; -fx-text-fill:#991b1b; -fx-background-radius:8; -fx-padding:2 10; -fx-font-size:10; -fx-font-weight:800;"
                : "-fx-background-color:#dcfce7; -fx-text-fill:#166534; -fx-background-radius:8; -fx-padding:2 10; -fx-font-size:10; -fx-font-weight:800;"
        );

        Label titreLbl = new Label(m.getTitre() == null ? "(Sans titre)" : m.getTitre());
        titreLbl.setStyle("-fx-font-size:14; -fx-font-weight:900; -fx-text-fill:#1f2937; -fx-wrap-text:true;");
        titreLbl.setWrapText(true);

        String heureDebut = m.getDateDebut() != null
                ? m.getDateDebut().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "?";
        String heureFin = m.getDateFin() != null
                ? m.getDateFin().toLocalDateTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                : "?";

        Label hourLbl = new Label("" + heureDebut + " → " + heureFin);
        hourLbl.setStyle("-fx-font-size:12; -fx-text-fill:#64748b; -fx-font-weight:700;");

        card.getChildren().addAll(statusLbl, titreLbl, hourLbl);

        if (m.getDescription() != null && !m.getDescription().isEmpty()) {
            Label descLbl = new Label(m.getDescription());
            descLbl.setStyle("-fx-font-size:11; -fx-text-fill:#94a3b8; -fx-wrap-text:true;");
            descLbl.setWrapText(true);
            card.getChildren().add(descLbl);
        }

        HBox actions = new HBox(6);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setStyle("-fx-padding:6 0 0 0; -fx-border-color:#e2e8f0; -fx-border-width:1 0 0 0;");

        boolean isOwner = currentParticipant != null && m.getParticipantId() == currentParticipant.getId();
        boolean isJoined = isUserJoined(m);

        if (isProf && isOwner) {
            Button editBtn = new Button("✏️");
            editBtn.setStyle("-fx-background-color:#fef9c3; -fx-text-fill:#ca8a04; -fx-background-radius:8; -fx-padding:5 10; -fx-cursor:hand;");
            editBtn.setTooltip(new Tooltip("Modifier"));
            editBtn.setOnAction(e -> showMeetFormDialog(m));

            Button delBtn = new Button("🗑️");
            delBtn.setStyle("-fx-background-color:#fee2e2; -fx-text-fill:#ef4444; -fx-background-radius:8; -fx-padding:5 10; -fx-cursor:hand;");
            delBtn.setTooltip(new Tooltip("Supprimer"));
            delBtn.setOnAction(e -> handleDeleteMeet(m));

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Button linkBtn = new Button("� Rejoindre");
            linkBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:8; -fx-padding:5 12; -fx-font-weight:700; -fx-cursor:hand;");
            linkBtn.setOnAction(e -> openJitsiRoom(m));
            actions.getChildren().addAll(linkBtn, spacer, editBtn, delBtn);
        } else {
            if (isJoined) {
                Label joinedLbl = new Label("✓ Inscrit");
                joinedLbl.setStyle("-fx-text-fill:#0FB5A9; -fx-font-weight:800; -fx-background-color:rgba(15,181,169,0.1); -fx-padding:5 12; -fx-background-radius:8;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button joinCallBtn = new Button("📹 Rejoindre");
                joinCallBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:8; -fx-padding:5 12; -fx-font-weight:700; -fx-cursor:hand;");
                joinCallBtn.setOnAction(e -> openJitsiRoom(m));
                actions.getChildren().addAll(joinedLbl, spacer, joinCallBtn);
            } else {
                Button joinBtn = new Button("✨ Participer");
                joinBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:8; -fx-padding:5 14; -fx-font-weight:800; -fx-cursor:hand;");
                joinBtn.setOnAction(e -> handleJoinMeet(m));
                actions.getChildren().add(joinBtn);
            }
        }

        card.getChildren().add(actions);
        return card;
    }

    private boolean isUserJoined(Meet m) {
        if (currentParticipant == null) return false;
        return mpCtrl.getParticipantsDuMeet(m.getId()).stream()
                .anyMatch(mp -> mp.getParticipantId() == currentParticipant.getId());
    }

    private void handleJoinMeet(Meet m) {
        if (currentParticipant == null) {
            new Alert(Alert.AlertType.ERROR, "Vous n'êtes pas reconnu comme participant.").show();
            return;
        }
        mpCtrl.ajouterParticipantAuMeet(m.getId(), currentParticipant.getId());
        reload();
    }

    private void handleDeleteMeet(Meet m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la suppression");
        confirm.setHeaderText("Supprimer la réunion");
        confirm.setContentText("Supprimer \"" + m.getTitre() + "\" définitivement ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            meetCtrl.supprimerMeet(m.getId());
            reload();
        }
    }

    @FXML
    public void handleAddMeet() {
        showMeetFormDialog(null);
    }

    private void showMeetFormDialog(Meet mToEdit) {
        Dialog<ButtonType> dlg = new Dialog<>();
        dlg.setTitle(mToEdit == null ? "Ajouter une Réunion" : "Modifier la Réunion");
        dlg.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dlg.getDialogPane().setPrefWidth(540);

        GridPane form = new GridPane();
        form.setHgap(12);
        form.setVgap(12);
        form.setPadding(new Insets(16));

        TextField titreField = new TextField(mToEdit != null ? mToEdit.getTitre() : "");
        TextArea descField = new TextArea(mToEdit != null && mToEdit.getDescription() != null ? mToEdit.getDescription() : "");
        descField.setPrefRowCount(2);

        DatePicker dpDebut = new DatePicker();
        TextField tDebut = new TextField();
        tDebut.setPromptText("HH:mm");
        tDebut.setPrefWidth(80);

        DatePicker dpFin = new DatePicker();
        TextField tFin = new TextField();
        tFin.setPromptText("HH:mm");
        tFin.setPrefWidth(80);

        if (mToEdit != null) {
            if (mToEdit.getDateDebut() != null) {
                LocalDateTime ldt = mToEdit.getDateDebut().toLocalDateTime();
                dpDebut.setValue(ldt.toLocalDate());
                tDebut.setText(String.format("%02d:%02d", ldt.getHour(), ldt.getMinute()));
            }
            if (mToEdit.getDateFin() != null) {
                LocalDateTime ldt = mToEdit.getDateFin().toLocalDateTime();
                dpFin.setValue(ldt.toLocalDate());
                tFin.setText(String.format("%02d:%02d", ldt.getHour(), ldt.getMinute()));
            }
        } else if (selectedDay != null) {
            dpDebut.setValue(selectedDay);
            dpFin.setValue(selectedDay);
            tDebut.setText("09:00");
            tFin.setText("10:00");
        } else {
            tDebut.setText("09:00");
            tFin.setText("10:00");
        }

        TextField lienField = new TextField(mToEdit != null && mToEdit.getLienMeet() != null ? mToEdit.getLienMeet() : "");

        List<participant> allParts = partCtrl.recupererParticipants();
        VBox partBox = new VBox(4);
        Map<CheckBox, participant> cbMap = new LinkedHashMap<>();

        for (participant p : allParts) {
            if (currentParticipant == null || p.getId() != currentParticipant.getId()) {
                CheckBox cb = new CheckBox(p.getNom() + " " + p.getPrenom() + " (" + p.getEmail() + ")");
                cbMap.put(cb, p);
                partBox.getChildren().add(cb);
                if (mToEdit != null && mpCtrl.isParticipantInscrit(mToEdit.getId(), p.getId())) {
                    cb.setSelected(true);
                }
            }
        }

        ScrollPane partScroll = new ScrollPane(partBox);
        partScroll.setPrefViewportHeight(120);
        partScroll.setFitToWidth(true);
        partScroll.setStyle("-fx-background-color:transparent;");

        Label errTitre = new Label();
        errTitre.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11;");

        Label errDate = new Label();
        errDate.setStyle("-fx-text-fill:#dc2626; -fx-font-size:11;");

        form.addRow(0, new Label("Titre *"), titreField);
        form.addRow(1, new Label(""), errTitre);
        form.addRow(2, new Label("Description"), descField);
        form.addRow(3, new Label("Date début *"), new HBox(6, dpDebut, tDebut));
        form.addRow(4, new Label("Date fin *"), new HBox(6, dpFin, tFin));
        form.addRow(5, new Label(""), errDate);
        form.addRow(6, new Label("Lien Meet"), lienField);
        form.addRow(7, new Label("Participants"), partScroll);
        GridPane.setHgrow(titreField, Priority.ALWAYS);
        GridPane.setHgrow(descField, Priority.ALWAYS);

        dlg.getDialogPane().setContent(form);

        Button okBtn = (Button) dlg.getDialogPane().lookupButton(ButtonType.OK);
        okBtn.setText("Enregistrer");
        okBtn.addEventFilter(javafx.event.ActionEvent.ACTION, ev -> {
            boolean valid = true;
            errTitre.setText("");
            errDate.setText("");

            String titre = titreField.getText() == null ? "" : titreField.getText().trim();
            if (titre.isEmpty()) {
                errTitre.setText("Le titre est obligatoire.");
                valid = false;
            }

            if (dpDebut.getValue() == null || dpFin.getValue() == null) {
                errDate.setText("Les dates de début et de fin sont obligatoires.");
                valid = false;
            } else {
                try {
                    String[] td = tDebut.getText().trim().split(":");
                    String[] tf = tFin.getText().trim().split(":");
                    LocalDateTime dtD = dpDebut.getValue().atTime(Integer.parseInt(td[0]), Integer.parseInt(td[1]));
                    LocalDateTime dtF = dpFin.getValue().atTime(Integer.parseInt(tf[0]), Integer.parseInt(tf[1]));
                    if (!dtD.isBefore(dtF)) {
                        errDate.setText("La date de fin doit être après la date de début.");
                        valid = false;
                    }
                } catch (Exception ex) {
                    errDate.setText("Format heure invalide (HH:mm attendu).");
                    valid = false;
                }
            }

            if (!valid) ev.consume();
        });

        dlg.showAndWait().ifPresent(res -> {
            if (res != ButtonType.OK) return;

            String[] td = tDebut.getText().trim().split(":");
            String[] tf = tFin.getText().trim().split(":");
            LocalDateTime dtD = dpDebut.getValue().atTime(Integer.parseInt(td[0]), Integer.parseInt(td[1]));
            LocalDateTime dtF = dpFin.getValue().atTime(Integer.parseInt(tf[0]), Integer.parseInt(tf[1]));
            Timestamp tsD = Timestamp.valueOf(dtD);
            Timestamp tsF = Timestamp.valueOf(dtF);

            String lien = lienField.getText() == null ? "" : lienField.getText().trim();
            if (!lien.isEmpty() && !lien.startsWith("http")) lien = "https://" + lien;

            int orgId = currentParticipant != null ? currentParticipant.getId() : 0;

            List<participant> selectedParticipants = new ArrayList<>();
            for (Map.Entry<CheckBox, participant> entry : cbMap.entrySet()) {
                if (entry.getKey().isSelected()) {
                    selectedParticipants.add(entry.getValue());
                }
            }

            if (mToEdit == null) {
                Meet newM = new Meet(titreField.getText().trim(), descField.getText().trim(), tsD, tsF, lien.isEmpty() ? null : lien, orgId);
                meetCtrl.ajouterMeet(newM);

                for (participant p : selectedParticipants) {
                    mpCtrl.ajouterParticipantAuMeet(newM.getId(), p.getId());
                }

                sendEmailInvitationsAsync(newM, selectedParticipants);
            } else {
                mToEdit.setTitre(titreField.getText().trim());
                mToEdit.setDescription(descField.getText().trim());
                mToEdit.setDateDebut(tsD);
                mToEdit.setDateFin(tsF);
                mToEdit.setLienMeet(lien.isEmpty() ? null : lien);
                meetCtrl.modifierMeet(mToEdit);

                List<Meet_Participants> existing = mpCtrl.getParticipantsDuMeet(mToEdit.getId());
                Set<Integer> existingIds = existing.stream().map(Meet_Participants::getParticipantId).collect(Collectors.toSet());

                List<participant> newParticipants = new ArrayList<>();
                for (Map.Entry<CheckBox, participant> entry : cbMap.entrySet()) {
                    int pId = entry.getValue().getId();
                    boolean selected = entry.getKey().isSelected();
                    if (selected && !existingIds.contains(pId)) {
                        mpCtrl.ajouterParticipantAuMeet(mToEdit.getId(), pId);
                        newParticipants.add(entry.getValue());
                    } else if (!selected && existingIds.contains(pId)) {
                        mpCtrl.retirerParticipantDuMeet(mToEdit.getId(), pId);
                    }
                }

                if (!newParticipants.isEmpty()) {
                    sendEmailInvitationsAsync(mToEdit, newParticipants);
                }
            }

            reload();
        });
    }

    private void openJitsiRoom(Meet meet) {
        JitsiMeetRoom room = new JitsiMeetRoom(meet);
        room.show();
    }

    private void reload() {
        loadAllMeets();
        renderCalendar();
        updateStats();

        if (selectedDay != null) {
            showDayDetail(selectedDay);
        } else {
            detailContainer.getChildren().clear();
            detailDayLabel.setText("Sélectionnez un jour");
            detailEmpty.setVisible(true);
            detailEmpty.setManaged(true);
        }
    }

    private void updateStats() {
        long countMois = allMeets.stream().filter(m -> {
            if (m.getDateDebut() == null) return false;
            YearMonth ym = YearMonth.from(m.getDateDebut().toLocalDateTime().toLocalDate());
            return ym.equals(displayedMonth);
        }).count();
        statsMoisLabel.setText(countMois + " session(s)");

        long countFutur = allMeets.stream()
                .filter(m -> m.getDateFin() != null && m.getDateFin().after(new Date()))
                .count();
        statsAVenirLabel.setText(countFutur + " session(s)");

        if (selectedDay == null) {
            statsSelectedLabel.setText("Aucun jour");
        }
    }

    @FXML
    public void handlePrevMonth() {
        displayedMonth = displayedMonth.minusMonths(1);
        selectedDay = null;
        reload();
    }

    @FXML
    public void handleNextMonth() {
        displayedMonth = displayedMonth.plusMonths(1);
        selectedDay = null;
        reload();
    }

    @FXML
    public void handleToday() {
        displayedMonth = YearMonth.now();
        selectedDay = LocalDate.now();
        reload();
    }

    @FXML
    public void handleMeetList() {
        navigateTo("/tn/esprit/view/front_MeetList.fxml");
    }

    @FXML public void handleHome() { navigateTo("/tn/esprit/view/front_user_dashboard.fxml"); }
    @FXML public void handleProfile() { navigateTo("/tn/esprit/view/front_profile.fxml"); }
    @FXML public void handleCours() { navigateTo("/tn/esprit/view/front_CoursCategories.fxml"); }
    @FXML public void handleJeux() { navigateTo("/tn/esprit/view/front_GameList.fxml"); }
    @FXML public void handleEvents() { navigateTo("/tn/esprit/view/frontEvent.fxml"); }
    @FXML public void handleForums() { navigateTo("/tn/esprit/view/front_forum.fxml"); }
    @FXML public void handleLogout() { navigateTo("/tn/esprit/view/front_login.fxml"); }

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

            if (ctrl instanceof FrontMeetListController) {
                ((FrontMeetListController) ctrl).initUser(currentUser);
            } else if (ctrl instanceof FrontMeetCalendarController) {
                ((FrontMeetCalendarController) ctrl).initUser(currentUser);
            } else if (ctrl instanceof FrontUserDashboardController) {
                ((FrontUserDashboardController) ctrl).initUser(currentUser);
            } else if (ctrl instanceof FrontProfileController) {
                ((FrontProfileController) ctrl).initUser(currentUser);
            } else if (ctrl instanceof FrontCoursCategorieController) {
                ((FrontCoursCategorieController) ctrl).initUser(currentUser);
            }

            Stage stage = (Stage) monthYearLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR, "Navigation impossible : " + e.getMessage()).show();
        }
    }
}
