package tn.esprit.controllers.Back;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.controllers.front.AdminDashboardController;
import tn.esprit.controllers.meet.MeetController;
import tn.esprit.controllers.meet.ParticipantController;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.participant;
import tn.esprit.entities.users.Users;
import tn.esprit.services.meet.MeetService;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Dashboard Admin pour les statistiques Meet
 * Adaptation du PIDEV Symfony Analytics vers JavaFX
 */
public class AdminMeetDashboardController implements Initializable {

    // ═══════════════════════════════════════════════════════════════════════
    // FXML INJECTIONS - KPI Cards
    // ═══════════════════════════════════════════════════════════════════════
    @FXML private Label kpiTotalMeets;
    @FXML private Label kpiTrendMeets;
    @FXML private Label kpiUpcoming;
    @FXML private Label kpiCurrent;
    @FXML private Label kpiCompleted;
    @FXML private Label kpiParticipants;
    @FXML private Label kpiAvgParticipants;
    @FXML private Label kpiTotalHours;
    @FXML private Label kpiAvgDuration;
    @FXML private Label kpiNextMeet;

    // Filtres
    @FXML private ComboBox<String> periodCombo;
    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;
    @FXML private ComboBox<String> teacherCombo;
    @FXML private Button applyFilterBtn;
    @FXML private Button exportCsvBtn;

    // Charts
    @FXML private AreaChart<String, Number> dailyChart;
    @FXML private PieChart statusChart;
    @FXML private BarChart<String, Number> hourlyChart;
    @FXML private TableView<TopTeacherRow> topTeachersTable;
    @FXML private TableColumn<TopTeacherRow, String> colTeacherName;
    @FXML private TableColumn<TopTeacherRow, Number> colTeacherMeets;

    // Navigation
    @FXML private VBox meetSubmenu;
    @FXML private Label adminNameLabel;

    // Services
    private final MeetService meetService = new MeetService();
    private final MeetController meetController = new MeetController();
    private final ParticipantController participantController = new ParticipantController();

    private Users currentUser;
    private LocalDate currentFrom;
    private LocalDate currentTo;

    // ═══════════════════════════════════════════════════════════════════════
    // INITIALISATION
    // ═══════════════════════════════════════════════════════════════════════

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPeriodCombo();
        setupDatePickers();
        setupTeacherCombo();
        setupCharts();
        setupTable();

        // Période par défaut: 30 jours
        setDefaultPeriod();

        // Charger les données
        Platform.runLater(this::loadDashboardData);
    }

    public void initAdmin(Users user) {
        this.currentUser = user;
        if (user != null && adminNameLabel != null) {
            adminNameLabel.setText("👑 " + user.getFirstName() + " " + user.getLastName());
        }
    }

    private void setupPeriodCombo() {
        periodCombo.setItems(FXCollections.observableArrayList(
            "7 derniers jours",
            "30 derniers jours",
            "90 derniers jours",
            "Cette année",
            "Personnalisé"
        ));
        periodCombo.getSelectionModel().select(1); // 30 jours par défaut

        periodCombo.setOnAction(e -> {
            if (!"Personnalisé".equals(periodCombo.getValue())) {
                setDefaultPeriod();
            }
        });
    }

    private void setupDatePickers() {
        fromDate.setValue(LocalDate.now().minusDays(30));
        toDate.setValue(LocalDate.now());
    }

    private void setupTeacherCombo() {
        teacherCombo.getItems().add("Tous les enseignants");
        try {
            List<participant> teachers = participantController.recupererParticipants();
            for (participant p : teachers) {
                teacherCombo.getItems().add(p.getId() + " - " + p.getNom() + " " + p.getPrenom());
            }
        } catch (Exception e) {
            System.err.println("Erreur chargement enseignants: " + e.getMessage());
        }
        teacherCombo.getSelectionModel().selectFirst();
    }

    private void setupCharts() {
        // Configuration Area Chart (Daily)
        dailyChart.setLegendVisible(false);
        dailyChart.setAnimated(true);

        // Configuration Pie Chart (Status)
        statusChart.setLegendVisible(true);
        statusChart.setLabelsVisible(true);

        // Configuration Bar Chart (Hourly)
        hourlyChart.setLegendVisible(false);
        hourlyChart.setAnimated(true);
    }

    private void setupTable() {
        colTeacherName.setCellValueFactory(cell -> cell.getValue().nameProperty());
        colTeacherMeets.setCellValueFactory(cell -> cell.getValue().meetCountProperty());
    }

    private void setDefaultPeriod() {
        String period = periodCombo.getValue();
        LocalDate to = LocalDate.now();
        LocalDate from;

        switch (period) {
            case "7 derniers jours" -> from = to.minusDays(7);
            case "90 derniers jours" -> from = to.minusDays(90);
            case "Cette année" -> from = LocalDate.of(to.getYear(), 1, 1);
            default -> from = to.minusDays(30); // 30 jours
        }

        fromDate.setValue(from);
        toDate.setValue(to);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CHARGEMENT DES DONNÉES
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    private void loadDashboardData() {
        try {
            currentFrom = fromDate.getValue();
            currentTo = toDate.getValue();

            Timestamp from = Timestamp.valueOf(currentFrom.atStartOfDay());
            Timestamp to = Timestamp.valueOf(currentTo.atTime(23, 59, 59));

            // KPI Cards
            loadKPIs(from, to);

            // Charts
            loadDailyChart(from, to);
            loadStatusChart();
            loadHourlyChart();

            // Table
            loadTopTeachers();

            // Prochaine réunion
            loadNextMeet();

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible de charger les statistiques: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadKPIs(Timestamp from, Timestamp to) throws SQLException {
        // Totaux
        int totalMeets = meetService.countMeetsByPeriod(from, to);
        int upcoming = meetService.countMeetsByStatus("upcoming");
        int current = meetService.countMeetsByStatus("current");
        int completed = meetService.countMeetsByStatus("completed");
        int totalParticipants = meetService.countParticipantsTotal();
        double avgParticipants = meetService.getAverageParticipantsPerMeet();
        int totalHours = meetService.getTotalDurationHours();
        double avgDuration = meetService.getAverageDurationMinutes();

        // Mise à jour des labels
        kpiTotalMeets.setText(String.valueOf(totalMeets));
        kpiUpcoming.setText(String.valueOf(upcoming));
        kpiCurrent.setText(String.valueOf(current));
        kpiCompleted.setText(String.valueOf(completed));
        kpiParticipants.setText(String.valueOf(totalParticipants));
        kpiAvgParticipants.setText(String.format("%.1f", avgParticipants));
        kpiTotalHours.setText(totalHours + "h");
        kpiAvgDuration.setText(String.format("%.0f min", avgDuration));

        // Calcul des tendances (période précédente)
        long days = (to.getTime() - from.getTime()) / (1000 * 60 * 60 * 24);
        Timestamp prevFrom = new Timestamp(from.getTime() - days * 24 * 60 * 60 * 1000);
        Timestamp prevTo = new Timestamp(to.getTime() - days * 24 * 60 * 60 * 1000);

        int prevTotal = meetService.countMeetsByPeriod(prevFrom, prevTo);
        double trend = prevTotal > 0 ? ((double)(totalMeets - prevTotal) / prevTotal * 100) : 0;

        kpiTrendMeets.setText(String.format("%+.1f%%", trend));
        kpiTrendMeets.setTextFill(trend >= 0 ? Color.web("#10b981") : Color.web("#ef4444"));
    }

    private void loadDailyChart(Timestamp from, Timestamp to) throws SQLException {
        List<MeetService.DailyStat> stats = meetService.getDailyStats(from, to);

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réunions");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        for (MeetService.DailyStat stat : stats) {
            String date = stat.getDate().substring(5); // MM-dd format
            series.getData().add(new XYChart.Data<>(date, stat.getCount()));
        }

        dailyChart.getData().clear();
        dailyChart.getData().add(series);

        // Style
        series.getNode().setStyle("-fx-stroke: #0FB5A9; -fx-stroke-width: 2px;");
    }

    private void loadStatusChart() throws SQLException {
        int upcoming = meetService.countMeetsByStatus("upcoming");
        int current = meetService.countMeetsByStatus("current");
        int completed = meetService.countMeetsByStatus("completed");

        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
            new PieChart.Data("À venir", upcoming),
            new PieChart.Data("En cours", current),
            new PieChart.Data("Terminées", completed)
        );

        statusChart.setData(data);

        // Couleurs
        data.get(0).getNode().setStyle("-fx-pie-color: #3b82f6;"); // Bleu
        data.get(1).getNode().setStyle("-fx-pie-color: #f59e0b;"); // Orange
        data.get(2).getNode().setStyle("-fx-pie-color: #10b981;"); // Vert
    }

    private void loadHourlyChart() throws SQLException {
        List<MeetService.HourlyStat> stats = meetService.getHourlyStats();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Réunions");

        for (int i = 0; i < 24; i++) {
            final int hour = i;
            int count = stats.stream()
                .filter(s -> s.getHour() == hour)
                .findFirst()
                .map(MeetService.HourlyStat::getCount)
                .orElse(0);
            series.getData().add(new XYChart.Data<>(String.format("%02dh", i), count));
        }

        hourlyChart.getData().clear();
        hourlyChart.getData().add(series);
    }

    private void loadTopTeachers() throws SQLException {
        List<MeetService.TopTeacher> teachers = meetService.getTopTeachers(5);

        ObservableList<TopTeacherRow> rows = FXCollections.observableArrayList();
        for (MeetService.TopTeacher t : teachers) {
            rows.add(new TopTeacherRow(t.getName(), t.getMeetCount()));
        }

        topTeachersTable.setItems(rows);
    }

    private void loadNextMeet() throws SQLException {
        Meet next = meetService.getNextMeet();
        if (next != null) {
            LocalDateTime dateTime = next.getDateDebut().toLocalDateTime();
            String formatted = dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
            kpiNextMeet.setText("\"" + next.getTitre() + "\" le " + formatted);
        } else {
            kpiNextMeet.setText("Aucune réunion planifiée");
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // EXPORT CSV
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    private void exportToCsv() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les statistiques");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );
        fileChooser.setInitialFileName("meet_stats_" + LocalDate.now() + ".csv");

        File file = fileChooser.showSaveDialog(exportCsvBtn.getScene().getWindow());
        if (file != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
                // Headers
                writer.println("Statistiques Meet - " + currentFrom + " au " + currentTo);
                writer.println();

                // KPI Section
                writer.println("KPI,Valeur");
                writer.println("Total réunions," + kpiTotalMeets.getText());
                writer.println("À venir," + kpiUpcoming.getText());
                writer.println("En cours," + kpiCurrent.getText());
                writer.println("Terminées," + kpiCompleted.getText());
                writer.println("Participants total," + kpiParticipants.getText());
                writer.println("Moy. participants/réunion," + kpiAvgParticipants.getText());
                writer.println("Durée totale," + kpiTotalHours.getText());
                writer.println("Durée moyenne," + kpiAvgDuration.getText());
                writer.println();

                // Top Teachers
                writer.println("Classement,Enseignant,Nombre de réunions");
                List<TopTeacherRow> rows = topTeachersTable.getItems();
                for (int i = 0; i < rows.size(); i++) {
                    TopTeacherRow row = rows.get(i);
                    writer.printf("%d,%s,%d%n", i + 1, row.getName(), row.getMeetCount());
                }

                showAlert(Alert.AlertType.INFORMATION, "Export réussi",
                    "Les statistiques ont été exportées vers:\n" + file.getAbsolutePath());

            } catch (IOException e) {
                showAlert(Alert.AlertType.ERROR, "Erreur export", e.getMessage());
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // NAVIGATION
    // ═══════════════════════════════════════════════════════════════════════

    @FXML
    public void toggleMeetMenu(ActionEvent event) {
        boolean showing = meetSubmenu.isVisible();
        meetSubmenu.setVisible(!showing);
        meetSubmenu.setManaged(!showing);
    }

    @FXML
    public void handleDashboard(ActionEvent e) {
        navigate("/tn/esprit/view/back_admin.fxml", e);
    }

    @FXML
    public void handleMeetList(ActionEvent e) {
        navigate("/tn/esprit/view/back_MeetList.fxml", e);
    }

    @FXML
    public void handleParticipants(ActionEvent e) {
        navigate("/tn/esprit/view/back_ParticipantList.fxml", e);
    }

    @FXML
    public void handleLogout(ActionEvent e) {
        navigate("/tn/esprit/view/front_login.fxml", e);
    }

    private void navigate(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Erreur : " + ex.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // UTILITAIRES
    // ═══════════════════════════════════════════════════════════════════════

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CLASSES INTERNES
    // ═══════════════════════════════════════════════════════════════════════

    public static class TopTeacherRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleIntegerProperty meetCount;

        public TopTeacherRow(String name, int meetCount) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.meetCount = new javafx.beans.property.SimpleIntegerProperty(meetCount);
        }

        public String getName() { return name.get(); }
        public void setName(String value) { name.set(value); }
        public javafx.beans.property.StringProperty nameProperty() { return name; }

        public int getMeetCount() { return meetCount.get(); }
        public void setMeetCount(int value) { meetCount.set(value); }
        public javafx.beans.property.IntegerProperty meetCountProperty() { return meetCount; }
    }
}
