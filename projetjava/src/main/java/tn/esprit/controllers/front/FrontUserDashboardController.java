package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import tn.esprit.entities.users.Users;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;


public class FrontUserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button profileButton;
    @FXML private Button adminButton;

    private Users currentUser;

    public void initUser(Users user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Bienvenue sur Naja7ni, " + user.getFirstName() + " 👋");
            profileButton.setText(user.getFirstName());

            // Show Back Office button only for ADMIN role (stored in DB as uppercase "ADMIN")
            boolean isAdmin = "ADMIN".equals(user.getRole());
            adminButton.setVisible(isAdmin);
            adminButton.setManaged(isAdmin);
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/tn/esprit/view/front_login.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackOffice(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/view/back_admin.fxml"));
            javafx.scene.Parent root = loader.load();

            AdminDashboardController controller = loader.getController();
            controller.initAdmin(currentUser);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/tn/esprit/view/front_profile.fxml"));
            javafx.scene.Parent root = loader.load();
            
            FrontProfileController controller = loader.getController();
            controller.initUser(currentUser);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleGameList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/front_GameList.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
