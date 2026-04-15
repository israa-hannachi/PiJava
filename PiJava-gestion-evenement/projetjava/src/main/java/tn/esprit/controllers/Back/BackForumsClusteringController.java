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
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.services.forum.ServiceForum;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BackForumsClusteringController implements Initializable {

    // Controls
    @FXML private ComboBox<String> clusteringMethodCombo;
    @FXML private Spinner<Integer> clusterCountSpinner;

    // Cluster Cards
    @FXML private VBox cluster1Card;
    @FXML private Label cluster1Forum;
    @FXML private Label cluster1Messages;
    @FXML private Label cluster1Users;
    @FXML private Label cluster1Topics;

    @FXML private VBox cluster2Card;
    @FXML private Label cluster2Forum;
    @FXML private Label cluster2Messages;
    @FXML private Label cluster2Users;
    @FXML private Label cluster2Topics;

    @FXML private VBox cluster3Card;
    @FXML private Label cluster3Forum;
    @FXML private Label cluster3Messages;
    @FXML private Label cluster3Users;
    @FXML private Label cluster3Topics;

    private final ServiceForum serviceForum = new ServiceForum();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupControls();
        loadSampleData();
    }

    private void setupControls() {
        if (clusteringMethodCombo != null) {
            clusteringMethodCombo.setItems(FXCollections.observableArrayList(
                    "K-Means (par activité)",
                    "DBSCAN (par densité)",
                    "Hiérarchique (par contenu)",
                    "Temporal (par date de création)"
            ));
            clusteringMethodCombo.getSelectionModel().selectFirst();
        }

        if (clusterCountSpinner != null) {
            SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                    new SpinnerValueFactory.IntegerSpinnerValueFactory(2, 10, 6);
            clusterCountSpinner.setValueFactory(valueFactory);
        }
    }

    private void loadSampleData() {
        // Sample data loading - cards only, no tables
    }

    @FXML
    public void handleRunClustering() {
        // Simulate clustering algorithm execution
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Clustering");
        info.setHeaderText("Analyse en cours");
        info.setContentText("L'algorithme " + clusteringMethodCombo.getValue() + " est en cours d'exécution avec " + clusterCountSpinner.getValue() + " clusters...");
        info.showAndWait();

        // Reload data after "clustering"
        loadSampleData();

        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Clustering");
        success.setHeaderText("Analyse terminée");
        success.setContentText("Le clustering a été effectué avec succès ! Les résultats sont affichés ci-dessous.");
        success.showAndWait();
    }

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

    // Inner class for cluster items
    public static class ClusterItem {
        private final String forumName;
        private final int messagesPerDay;
        private final int activeUsers;
        private final String mainTopics;

        public ClusterItem(String forumName, int messagesPerDay, int activeUsers, String mainTopics) {
            this.forumName = forumName;
            this.messagesPerDay = messagesPerDay;
            this.activeUsers = activeUsers;
            this.mainTopics = mainTopics;
        }

        public String getForumName() { return forumName; }
        public int getMessagesPerDay() { return messagesPerDay; }
        public int getActiveUsers() { return activeUsers; }
        public String getMainTopics() { return mainTopics; }
    }
}
