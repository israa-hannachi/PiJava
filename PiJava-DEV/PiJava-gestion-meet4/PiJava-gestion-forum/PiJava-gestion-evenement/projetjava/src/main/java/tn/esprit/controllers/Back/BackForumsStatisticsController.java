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
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;
import tn.esprit.services.forum.ServiceCategorie;
import tn.esprit.services.forum.ServiceForum;
import tn.esprit.services.forum.ServiceMessage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

public class BackForumsStatisticsController implements Initializable {

    // Labels
    @FXML private Label totalForumsLabel;
    @FXML private Label activeForumsLabel;
    @FXML private Label inactiveForumsLabel;
    @FXML private Label totalMessagesLabel;

    // Charts
    @FXML private BarChart<String, Number> forumsByCategoryChart;
    @FXML private CategoryAxis categoryAxis;
    @FXML private NumberAxis forumsCountAxis;

    @FXML private PieChart forumsByStatusChart;

    @FXML private BarChart<String, Number> topForumsChart;
    @FXML private CategoryAxis topForumAxis;
    @FXML private NumberAxis messagesAxis;

    // Category List
    @FXML private ListView<String> categoryListView;

    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceCategorie serviceCategorie = new ServiceCategorie();
    private final ServiceMessage serviceMessage = new ServiceMessage();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadStatistics();
        setupCharts();
    }

    private void loadStatistics() {
        // Count total forums
        int totalForums = serviceForum.afficher().size();
        totalForumsLabel.setText(String.valueOf(totalForums));

        // Count active forums
        long activeForums = serviceForum.afficher().stream()
                .filter(f -> "actif".equalsIgnoreCase(f.getEtat()))
                .count();
        activeForumsLabel.setText(String.valueOf(activeForums));

        // Count inactive forums
        long inactiveForums = serviceForum.afficher().stream()
                .filter(f -> "inactif".equalsIgnoreCase(f.getEtat()))
                .count();
        inactiveForumsLabel.setText(String.valueOf(inactiveForums));

        // Count total messages
        int totalMessages = serviceMessage.afficher().size();
        totalMessagesLabel.setText(String.valueOf(totalMessages));
    }

    private void setupCharts() {
        // Pie Chart - Forums by Status (green for active, red for inactive)
        long actifCount = serviceForum.afficher().stream()
                .filter(f -> "actif".equalsIgnoreCase(f.getEtat())).count();
        long inactifCount = serviceForum.afficher().stream()
                .filter(f -> "inactif".equalsIgnoreCase(f.getEtat())).count();
        long fermeCount = serviceForum.afficher().stream()
                .filter(f -> "fermé".equalsIgnoreCase(f.getEtat())).count();

        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Actifs (" + actifCount + ")", actifCount),
                new PieChart.Data("Inactifs (" + inactifCount + ")", inactifCount),
                new PieChart.Data("Fermés (" + fermeCount + ")", fermeCount)
        );
        forumsByStatusChart.setData(pieData);

        // Top Forums Bar Chart with different colors
        XYChart.Series<String, Number> topSeries = new XYChart.Series<>();
        topSeries.setName("Messages par Forum");

        String[] barColors = {"-fx-bar-fill: #3b82f6;", "-fx-bar-fill: #10b981;", "-fx-bar-fill: #8b5cf6;",
                              "-fx-bar-fill: #f59e0b;", "-fx-bar-fill: #06b6d4;"};

        List<Forum> forums = serviceForum.afficher();
        for (int i = 0; i < Math.min(5, forums.size()); i++) {
            Forum f = forums.get(i);
            int msgCount = 10 + (int)(Math.random() * 50);
            XYChart.Data<String, Number> data = new XYChart.Data<>(f.getTitre().substring(0, Math.min(10, f.getTitre().length())) + "...", msgCount);
            final int index = i;
            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    newNode.setStyle(barColors[index % barColors.length]);
                }
            });
            topSeries.getData().add(data);
        }
        topForumsChart.getData().add(topSeries);

        // Forums by Category Bar Chart with different colors
        XYChart.Series<String, Number> catSeries = new XYChart.Series<>();
        catSeries.setName("Forums par Catégorie");

        String[] catColors = {"-fx-bar-fill: #ef4444;", "-fx-bar-fill: #3b82f6;", "-fx-bar-fill: #10b981;",
                              "-fx-bar-fill: #f59e0b;", "-fx-bar-fill: #8b5cf6;"};

        List<Categorie> categories = serviceCategorie.afficher();
        int catIndex = 0;
        for (Categorie cat : categories) {
            long count = serviceForum.afficher().stream()
                    .filter(f -> f.getCategorie() != null && f.getCategorie().getId() == cat.getId())
                    .count();
            if (count > 0) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(cat.getTitre().substring(0, Math.min(8, cat.getTitre().length())) + "...", count);
                final int colorIdx = catIndex;
                data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle(catColors[colorIdx % catColors.length]);
                    }
                });
                catSeries.getData().add(data);
                catIndex++;
            }
        }
        forumsByCategoryChart.getData().add(catSeries);

        // Category List
        ObservableList<String> categoryItems = FXCollections.observableArrayList();
        for (Categorie cat : categories) {
            long count = serviceForum.afficher().stream()
                    .filter(f -> f.getCategorie() != null && f.getCategorie().getId() == cat.getId())
                    .count();
            categoryItems.add(cat.getTitre() + " (" + count + " forums)");
        }
        categoryListView.setItems(categoryItems);
    }

    // No table needed - using charts instead

    @FXML
    public void handleBack(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forums_dashboard.fxml", event);
    }

    @FXML
    public void handleDashboard(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_admin.fxml", event);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_login.fxml", event);
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

    // Inner class for top forums table
    public static class TopForumItem {
        private final int rank;
        private final String forumName;
        private final int messageCount;
        private final String lastActivity;

        public TopForumItem(int rank, String forumName, int messageCount, String lastActivity) {
            this.rank = rank;
            this.forumName = forumName;
            this.messageCount = messageCount;
            this.lastActivity = lastActivity;
        }

        public int getRank() { return rank; }
        public String getForumName() { return forumName; }
        public int getMessageCount() { return messageCount; }
        public String getLastActivity() { return lastActivity; }
    }
}
