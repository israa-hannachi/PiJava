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
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import javafx.scene.control.Alert;
import tn.esprit.tools.ExportDialog;

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
    @FXML
    public void handleSummaryReport(ActionEvent event) {
        ExportDialog dialog = new ExportDialog();
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent() && result.get() != null) {
            String[] parts = result.get().split(":");
            String format = parts[0];
            String fileName = parts.length > 1 ? parts[1] : "Rapport_Dashboard";
            
            // Prepare statistics
            Map<String, Object> stats = new HashMap<>();
            List<tn.esprit.entities.forum.Categorie> categories = serviceCategorie.afficher();
            List<tn.esprit.entities.forum.Forum> allForums = serviceForum.afficher();
            
            stats.put("totalCategories", categories.size());
            stats.put("totalForums", allForums.size());
            stats.put("totalMessages", serviceMessage.afficher().size());
            
            long activeCount = allForums.stream().filter(f -> "actif".equalsIgnoreCase(f.getEtat())).count();
            stats.put("activeCount", (int) activeCount);
            
            stats.put("growth", 5.2); // Default mock growth value
            
            int engagement = allForums.isEmpty() ? 0 : serviceMessage.afficher().size() / allForums.size();
            stats.put("engagement", engagement);
            
            String health = (activeCount > allForums.size() / 2) ? "Excellent" : "Stable";
            stats.put("healthStatus", health);

            Map<Integer, Integer> msgCounts = new HashMap<>();
            for (tn.esprit.entities.forum.Forum f : allForums) {
                msgCounts.put(f.getId(), 0); 
            }

            try {
                String userHome = System.getProperty("user.home");
                String path = userHome + "/Downloads/" + fileName;

                if ("PDF".equals(format)) {
                    path += ".pdf";
                    tn.esprit.tools.ExportUtil.exportToPDF(path, stats, categories, allForums, msgCounts, new HashMap<>());
                } else if ("WORD".equals(format)) {
                    path += ".docx";
                    tn.esprit.tools.ExportUtil.exportToWord(path, stats, categories, allForums, msgCounts);
                } else if ("EXCEL".equals(format)) {
                    path += ".xlsx";
                    tn.esprit.tools.ExportUtil.exportToExcel(path, stats, categories);
                } else if ("CSV".equals(format)) {
                    path += ".csv";
                    tn.esprit.tools.ExportUtil.exportToCSV(path, stats, categories);
                }

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Export réussi");
                alert.setHeaderText(null);
                alert.setContentText("Le rapport a été exporté avec succès dans vos Téléchargements :\n" + path);
                alert.showAndWait();

            } catch (Exception e) {
                e.printStackTrace();
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur d'exportation");
                alert.setHeaderText("Une erreur est survenue");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
