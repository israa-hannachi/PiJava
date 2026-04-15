package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.scene.Node;
import tn.esprit.entities.users.Users;

import java.io.IOException;

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

            boolean isAdmin = "ADMIN".equals(user.getRole());
            adminButton.setVisible(isAdmin);
            adminButton.setManaged(isAdmin);
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/front_login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackOffice(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_admin.fxml"));
            Parent root = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.initAdmin(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_profile.fxml"));
            Parent root = loader.load();
            FrontProfileController controller = loader.getController();
            controller.initUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleGameList(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_GameList.fxml"));
            Parent root = loader.load();
            
            FrontGameListController controller = loader.getController();
            controller.initUser(currentUser);
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Navigates to the front-office cours catalogue.
     * Passes the current user so role-based logic (Student vs Enseignant) works.
     */
    @FXML
    public void handleCours(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/view/front_CoursCategories.fxml"));
            Parent root = loader.load();
            FrontCoursCategorieController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEvents(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/frontEvent.fxml"));
            Parent root = loader.load();
            EventFrontController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForums(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_forum.fxml"));
            Parent root = loader.load();
            FrontForumController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
