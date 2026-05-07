package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Rating;
import tn.esprit.entities.event.Registration;
import tn.esprit.entities.users.Users;
import tn.esprit.services.event.EventService;
import tn.esprit.services.event.RatingService;
import tn.esprit.services.event.RegistrationService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CalendarController {

    @FXML private GridPane calendarGrid;
    @FXML private Button prevMonthBtn;
    @FXML private Button nextMonthBtn;
    @FXML private Button todayBtn;
    @FXML private Label monthLabel;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private YearMonth currentYearMonth;
    private Map<LocalDate, List<Event>> eventsByDate;

    private final RegistrationService registrationService = new RegistrationService();
    private final EventService eventService = new EventService();
    private final RatingService ratingService = new RatingService();

    private Users currentUserObj;

    public void initUser(Users user) {
        this.currentUserObj = user;
        eventsByDate = new HashMap<>();
        loadUserEvents();
        buildCalendar(currentYearMonth);
    }

    @FXML
    public void initialize() {
        eventsByDate = new HashMap<>();
        currentYearMonth = YearMonth.now();
        updateMonthLabel();
        buildCalendar(currentYearMonth);

        prevMonthBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateMonthLabel();
            buildCalendar(currentYearMonth);
        });

        nextMonthBtn.setOnAction(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateMonthLabel();
            buildCalendar(currentYearMonth);
        });

        if (todayBtn != null) {
            todayBtn.setOnAction(e -> {
                currentYearMonth = YearMonth.now();
                updateMonthLabel();
                buildCalendar(currentYearMonth);
            });
        }
    }

    private void updateMonthLabel() {
        String month = currentYearMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.FRANCE);
        monthLabel.setText(month.substring(0, 1).toUpperCase(Locale.ROOT) + month.substring(1) + " " + currentYearMonth.getYear());
    }

    private void buildCalendar(YearMonth yearMonth) {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();
        calendarGrid.setHgap(10);
        calendarGrid.setVgap(10);

        for (int i = 0; i < 7; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setPercentWidth(100.0 / 7);
            col.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(col);
        }

        RowConstraints headerRow = new RowConstraints();
        headerRow.setPercentHeight(8);
        calendarGrid.getRowConstraints().add(headerRow);

        for (int i = 0; i < 6; i++) {
            RowConstraints row = new RowConstraints();
            row.setPercentHeight(92.0 / 6);
            row.setVgrow(Priority.ALWAYS);
            calendarGrid.getRowConstraints().add(row);
        }

        String[] days = {"LUN", "MAR", "MER", "JEU", "VEN", "SAM", "DIM"};
        for (int i = 0; i < days.length; i++) {
            Label header = new Label(days[i]);
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            header.setStyle("-fx-font-size: 11px; -fx-font-weight: 900; -fx-text-fill: #94a3b8;"
                    + "-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-padding: 10 0;");
            calendarGrid.add(header, i, 0);
        }

        int daysInMonth = yearMonth.lengthOfMonth();
        LocalDate firstDay = yearMonth.atDay(1);
        int firstColumn = firstDay.getDayOfWeek().getValue() - 1;

        int day = 1;
        for (int row = 1; row <= 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (row == 1 && col < firstColumn || day > daysInMonth) {
                    Region emptyCell = new Region();
                    emptyCell.setStyle("-fx-background-color: rgba(248,250,252,0.65); -fx-background-radius: 18;");
                    calendarGrid.add(emptyCell, col, row);
                    continue;
                }

                LocalDate currentDate = yearMonth.atDay(day++);
                calendarGrid.add(createDayCell(currentDate), col, row);
            }
        }
    }

    private VBox createDayCell(LocalDate currentDate) {
        List<Event> events = eventsByDate.getOrDefault(currentDate, new ArrayList<>());
        boolean isToday = LocalDate.now().equals(currentDate);
        boolean hasEvents = !events.isEmpty();

        VBox cell = new VBox(8);
        cell.setPadding(new Insets(12));
        cell.setPrefHeight(96);
        cell.setMaxWidth(Double.MAX_VALUE);
        cell.setStyle(getDayStyle(isToday, hasEvents));

        HBox top = new HBox();
        Label dayNumber = new Label(String.valueOf(currentDate.getDayOfMonth()));
        dayNumber.setStyle("-fx-font-size: 16px; -fx-font-weight: 900; -fx-text-fill: " + (isToday ? "#2563eb" : "#0f172a") + ";");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label badge = new Label(hasEvents ? events.size() + " evt" : "");
        badge.setVisible(hasEvents);
        badge.setManaged(hasEvents);
        badge.setStyle("-fx-background-color: #dff8f4; -fx-text-fill: #0fb5a9; -fx-font-size: 10px;"
                + "-fx-font-weight: 800; -fx-padding: 4 8; -fx-background-radius: 999;");
        top.getChildren().addAll(dayNumber, spacer, badge);

        Label status = new Label(hasEvents ? summariseEvents(events) : "Aucune inscription");
        status.setWrapText(true);
        status.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        cell.getChildren().addAll(top, status);
        cell.setOnMouseClicked(e -> {
            if (hasEvents) {
                showEventsPopup(currentDate, events);
            }
        });
        return cell;
    }

    private String getDayStyle(boolean isToday, boolean hasEvents) {
        if (isToday) {
            return "-fx-background-color: #eff6ff; -fx-background-radius: 18; -fx-border-color: #93c5fd;"
                    + "-fx-border-radius: 18; -fx-border-width: 1.5;";
        }
        if (hasEvents) {
            return "-fx-background-color: linear-gradient(to bottom, #f1fffd, #ffffff); -fx-background-radius: 18;"
                    + "-fx-border-color: #bfeee8; -fx-border-radius: 18; -fx-border-width: 1.5;";
        }
        return "-fx-background-color: #ffffff; -fx-background-radius: 18; -fx-border-color: #eef2f7;"
                + "-fx-border-radius: 18; -fx-border-width: 1;";
    }

    private String summariseEvents(List<Event> events) {
        if (events.isEmpty()) {
            return "Aucune inscription";
        }
        if (events.size() == 1) {
            return events.get(0).getTitre();
        }
        return events.get(0).getTitre() + " +" + (events.size() - 1);
    }

    private void showEventsPopup(LocalDate date, List<Event> events) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Programme du " + date.format(DATE_FORMATTER));
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox contentBox = new VBox(14);
        contentBox.setPadding(new Insets(18));

        for (Event ev : events) {
            VBox eventCard = new VBox(10);
            eventCard.setStyle("-fx-background-color: white; -fx-padding: 16; -fx-background-radius: 18;"
                    + "-fx-border-color: #dfeef0; -fx-border-radius: 18;");

            Label title = new Label(ev.getTitre());
            title.setStyle("-fx-font-size: 17px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

            Label description = new Label(ev.getDescription());
            description.setWrapText(true);
            description.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

            Label details = new Label("Du " + ev.getDateDebut().toLocalDateTime().toLocalDate().format(DATE_FORMATTER)
                    + " au " + ev.getDateFin().toLocalDateTime().toLocalDate().format(DATE_FORMATTER)
                    + "  •  " + ev.getLieu());
            details.setWrapText(true);
            details.setStyle("-fx-font-size: 12px; -fx-font-weight: 700; -fx-text-fill: #334155;");

            Button rateBtn = new Button("Donner un avis");
            rateBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; -fx-font-weight: 800;"
                    + "-fx-background-radius: 10; -fx-padding: 8 12; -fx-cursor: hand;");
            rateBtn.setOnAction(e -> openRatingDialog(ev));

            eventCard.getChildren().addAll(title, description, details, rateBtn);
            contentBox.getChildren().add(eventCard);
        }

        ScrollPane scrollPane = new ScrollPane(contentBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefViewportHeight(420);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

        dialog.getDialogPane().setContent(scrollPane);
        dialog.showAndWait();
    }

    private void openRatingDialog(Event event) {
        Dialog<Rating> dialog = new Dialog<>();
        dialog.setTitle("Evaluer l'evenement");
        dialog.setHeaderText("Donnez votre avis sur : " + event.getTitre());

        ButtonType btnSubmit = new ButtonType("Envoyer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, ButtonType.CANCEL);

        ComboBox<Integer> starsCombo = new ComboBox<>();
        starsCombo.getItems().addAll(1, 2, 3, 4, 5);
        starsCombo.setValue(5);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Votre commentaire...");
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(4);

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("Note"), starsCombo, new Label("Commentaire"), commentArea);
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(b -> {
            if (b == btnSubmit) {
                return new Rating(event.getId(), starsCombo.getValue(), commentArea.getText());
            }
            return null;
        });

        dialog.showAndWait().ifPresent(rating -> {
            try {
                ratingService.ajouter(rating);
                new Alert(Alert.AlertType.INFORMATION, "Votre avis a bien ete enregistre.").show();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Impossible de sauvegarder l'evaluation.").show();
            }
        });
    }

    private void loadUserEvents() {
        try {
            eventsByDate.clear();

            if (currentUserObj == null || currentUserObj.getEmail() == null) {
                for (Event event : eventService.recuperer()) {
                    registerEventDates(event);
                }
                return;
            }

            List<Registration> registrations = registrationService.recuperer();
            boolean hasUserRegistrations = false;

            for (Registration reg : registrations) {
                if (reg.getVisitorEmail() != null && reg.getVisitorEmail().equalsIgnoreCase(currentUserObj.getEmail())) {
                    Event event = eventService.findById(reg.getEvenementId());
                    if (event != null) {
                        hasUserRegistrations = true;
                        registerEventDates(event);
                    }
                }
            }

            if (!hasUserRegistrations) {
                for (Event event : eventService.recuperer()) {
                    registerEventDates(event);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerEventDates(Event event) {
        LocalDate start = event.getDateDebut().toLocalDateTime().toLocalDate();
        LocalDate end = event.getDateFin().toLocalDateTime().toLocalDate();

        while (!start.isAfter(end)) {
            eventsByDate.computeIfAbsent(start, key -> new ArrayList<>()).add(event);
            start = start.plusDays(1);
        }
    }
}
