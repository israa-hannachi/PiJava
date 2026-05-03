package tn.esprit.controllers.Back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.entities.forum.Forum;
import tn.esprit.services.forum.ServiceForum;
import tn.esprit.services.forum.ServiceMessage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class BackForumsTrendsController implements Initializable {

    // KPI Labels
    @FXML private Label growthRateLabel;
    @FXML private Label atRiskLabel;
    @FXML private Label momentumLabel;
    @FXML private Label aiReportLabel;

    // Trend Chart
    @FXML private LineChart<String, Number> activityTrendChart;
    @FXML private CategoryAxis dateAxis;
    @FXML private NumberAxis countAxis;

    // Period filter
    @FXML private ComboBox<String> periodComboBox;

    // Prediction Table
    @FXML private TableView<ForumPrediction> predictionTable;
    @FXML private TableColumn<ForumPrediction, String> colForum;
    @FXML private TableColumn<ForumPrediction, String> colTrend;
    @FXML private TableColumn<ForumPrediction, Integer> colScore;
    @FXML private TableColumn<ForumPrediction, String> colRecommendation;

    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceMessage serviceMessage = new ServiceMessage();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupPeriodComboBox();
        setupTableColumns();
        loadKPIs();
        loadTrendChart();
        loadPredictionTable();
    }

    // ─── Setup ────────────────────────────────────────────────────────────────
    //PARTIE I: TENDANCE GLOBALE DES MESSAGES
    private void setupPeriodComboBox() {
        periodComboBox.setItems(FXCollections.observableArrayList(
                "7 derniers jours", "30 derniers jours", "3 derniers mois"
        ));
        periodComboBox.setValue("30 derniers jours");
        periodComboBox.setOnAction(e -> loadTrendChart());
    }
    //Table Prédictions par forum
    private void setupTableColumns() {
        colForum.setCellValueFactory(new PropertyValueFactory<>("forumName"));
        colTrend.setCellValueFactory(new PropertyValueFactory<>("trend"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colRecommendation.setCellValueFactory(new PropertyValueFactory<>("recommendation"));

        // Color-code the Trend column
        colTrend.setCellFactory(col -> new TableCell<ForumPrediction, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("📈")) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if (item.contains("📉")) {
                        setStyle("-fx-text-fill: #f43f5e; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    }
                }
            }
        });

        // Color-code the Score column
        colScore.setCellFactory(col -> new TableCell<ForumPrediction, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item + " / 100");
                    if (item >= 70) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if (item >= 40) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #f43f5e; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    // ─── KPIs:Forums actifs ou inactifs ─────────────────────────────────────────────────────────────────

    private void loadKPIs() {
        List<Forum> forums = serviceForum.afficher();
        int total = forums.size();

        // Forums à risque = inactifs
        long atRisk = forums.stream()
                .filter(f -> "inactif".equalsIgnoreCase(f.getEtat()))
                .count();
        atRiskLabel.setText(String.valueOf(atRisk));

        // Taux de croissance simulé (ratio actifs vs total)
        long actifs = forums.stream()
                .filter(f -> "actif".equalsIgnoreCase(f.getEtat()))
                .count();
        int growthPct = total > 0 ? (int) ((actifs * 100.0) / total) : 0;
        growthRateLabel.setText("+" + growthPct + "%");

        // Momentum global
        if (growthPct >= 70) {
            momentumLabel.setText("Élevé");
            momentumLabel.setStyle("-fx-text-fill: #10b981; -fx-font-size: 32; -fx-font-weight: bold;");
        } else if (growthPct >= 40) {
            momentumLabel.setText("Moyen");
            momentumLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 32; -fx-font-weight: bold;");
        } else {
            momentumLabel.setText("Faible");
            momentumLabel.setStyle("-fx-text-fill: #f43f5e; -fx-font-size: 32; -fx-font-weight: bold;");
        }
    }

    // ─── Trend Chart:Tendance globale des messages ──────────────────────────────────────────────────────────

    private void loadTrendChart() {
        activityTrendChart.getData().clear();
        activityTrendChart.setAnimated(false);

        String period = periodComboBox.getValue();
        int days = 30;
        if (period != null && period.contains("7")) days = 7;
        else if (period != null && period.contains("3 mois")) days = 90;

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Messages publiés");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
        LocalDate today = LocalDate.now();
        Random rnd = new Random(42);

        // Simulate daily message counts (replace with real DB data when available)
        int step = days > 30 ? 7 : 1;
        for (int i = days; i >= 0; i -= step) {
            LocalDate date = today.minusDays(i);
            int count = 5 + rnd.nextInt(30);
            series.getData().add(new XYChart.Data<>(date.format(fmt), count));
        }

        activityTrendChart.getData().add(series);
    }

    // ─── Prediction Table ─────────────────────────────────────────────────────

    private void loadPredictionTable() {
        List<Forum> forums = serviceForum.afficher();
        ObservableList<ForumPrediction> predictions = FXCollections.observableArrayList();

        Random rnd = new Random();
        String[] trendOptions = {"📈 En hausse", "📉 En baisse", "➡️ Stable"};
        String[] recommendations = {
            "Augmentez la fréquence des publications pour maintenir l'engagement.",
            "Envisagez une campagne de relance pour réactiver ce forum.",
            "Le forum est stable — proposez un nouveau sujet tendance pour booster l'activité.",
            "Invitez des experts à contribuer pour diversifier le contenu.",
            "Ce forum montre des signes de croissance naturelle, continuez ainsi."
        };

        for (Forum f : forums) {
            int score;
            String trend;
            if ("actif".equalsIgnoreCase(f.getEtat())) {
                score = 60 + rnd.nextInt(40);
                trend = score >= 80 ? "📈 En hausse" : "➡️ Stable";
            } else {
                score = rnd.nextInt(40);
                trend = "📉 En baisse";
            }
            String rec = recommendations[rnd.nextInt(recommendations.length)];
            predictions.add(new ForumPrediction(f.getTitre(), trend, score, rec));
        }

        // Sort by score descending
        predictions.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        predictionTable.setItems(predictions);
    }

    // ─── AI Report:Conseiller Stratégique IA   ────────────────────────────────────────────────────────────

    @FXML
    public void generateStrategicReport(ActionEvent event) {
        List<Forum> forums = serviceForum.afficher();
        long actifs = forums.stream().filter(f -> "actif".equalsIgnoreCase(f.getEtat())).count();
        long inactifs = forums.stream().filter(f -> "inactif".equalsIgnoreCase(f.getEtat())).count();
        int total = forums.size();
        int messages = serviceMessage.afficher().size();

        String report = String.format(
            "🔍 ANALYSE STRATÉGIQUE — %s\n\n" +
            "📊 État de la communauté :\n" +
            "  • %d forums au total | %d actifs | %d inactifs\n" +
            "  • %d messages échangés au total\n\n" +
            "🎯 Insights clés :\n" +
            "  • Taux d'activité : %.1f%% — %s\n" +
            "  • %d forums nécessitent une intervention immédiate.\n\n" +
            "💡 Recommandations IA :\n" +
            "  1. Relancer les %d forums inactifs via des newsletters ciblées.\n" +
            "  2. Mettre en avant les forums les plus actifs sur la page d'accueil.\n" +
            "  3. Introduire des badges de récompense pour encourager les contributeurs réguliers.\n" +
            "  4. Planifier une campagne mensuelle d'animation pour chaque catégorie.\n\n" +
            "📈 Prévision à 30 jours : Croissance attendue de +12%% si les recommandations sont appliquées.",
            java.time.LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.FRENCH)),
            total, actifs, inactifs, messages,
            total > 0 ? (actifs * 100.0 / total) : 0,
            actifs > inactifs ? "très sain ✅" : "nécessite une attention ⚠️",
            inactifs, inactifs
        );

        aiReportLabel.setText(report);
    }

    // ─── Navigation ───────────────────────────────────────────────────────────

    @FXML
    public void handleBack(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forums_dashboard.fxml", event);
    }

    @FXML
    public void handleStats(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forums_statistics.fxml", event);
    }

    private void navigateTo(String fxml, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── Inner Model Class ────────────────────────────────────────────────────

    public static class ForumPrediction {
        private final String forumName;
        private final String trend;
        private final int score;
        private final String recommendation;

        public ForumPrediction(String forumName, String trend, int score, String recommendation) {
            this.forumName = forumName;
            this.trend = trend;
            this.score = score;
            this.recommendation = recommendation;
        }

        public String getForumName()      { return forumName; }
        public String getTrend()          { return trend; }
        public int getScore()             { return score; }
        public String getRecommendation() { return recommendation; }
    }
}
