package tn.esprit.controllers.Back;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import tn.esprit.services.forum.ServiceCategorie;
import tn.esprit.services.forum.ServiceForum;
import tn.esprit.services.forum.ServiceMessage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class BackForumsDashboardController implements Initializable {

    @FXML private Label totalCategoriesLabel;
    @FXML private Label totalForumsLabel;
    @FXML private Label totalMessagesLabel;
    @FXML private Label activeForumsLabel;

    private final ServiceCategorie serviceCategorie = new ServiceCategorie();
    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceMessage serviceMessage = new ServiceMessage();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadDashboardStats();
    }

    private void loadDashboardStats() {
        int totalCategories = serviceCategorie.afficher().size();
        int totalForums = serviceForum.afficher().size();
        int totalMessages = serviceMessage.afficher().size();
        long activeForums = serviceForum.afficher().stream()
                .filter(f -> "actif".equalsIgnoreCase(f.getEtat())).count();

        totalCategoriesLabel.setText(String.valueOf(totalCategories));
        totalForumsLabel.setText(String.valueOf(totalForums));
        totalMessagesLabel.setText(String.valueOf(totalMessages));
        activeForumsLabel.setText(String.valueOf(activeForums));
    }

    // Action buttons from Photo 2
    @FXML
    public void handleCreateForum(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forums_gestion.fxml", event);
    }

    @FXML
    public void handleAddCategory(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forum_categories.fxml", event);
    }

    @FXML
    public void handleStatistics(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forums_statistics.fxml", event);
    }

    @FXML
    public void handleClustering(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forums_clustering.fxml", event);
    }

    @FXML
    public void handleGoToFront(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_forum.fxml", event);
    }

    // Secondary buttons from Photo 1
    @FXML
    public void handleManageCategories(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forum_categories.fxml", event);
    }

    @FXML
    public void handleManageForums(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forums_gestion.fxml", event);
    }

    @FXML
    public void handleManageMessages(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forum_messages.fxml", event);
    }

    // Top navbar
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
}
