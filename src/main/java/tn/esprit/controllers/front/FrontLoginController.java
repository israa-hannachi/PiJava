package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import java.io.IOException;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;

public class FrontLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberCheck;

    @FXML
    public void handleForgotPassword(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_forgot_password.fxml", event);
    }

    @FXML
    public void handleSignup(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_register.fxml", event);
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Please provide both email and password.");
            return;
        }

        UsersController usersController = new UsersController();
        Users user = usersController.findByEmail(email);

        if (user == null) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "No account found with this email.");
            return;
        }

        String hashedAttempt = UsersController.hashPassword(password);
        if (!hashedAttempt.equals(user.getPassword())) {
            showAlert(Alert.AlertType.ERROR, "Login Error", "Incorrect password. Please try again.");
            return;
        }

        // Login successful!
        System.out.println("✅ User Logged In: " + user.getFirstName() + " " + user.getLastName());
        navigateToDashboard("/tn/esprit/view/front_user_dashboard.fxml", event, user);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(fxmlPath));
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigateToDashboard(String fxmlPath, ActionEvent event, Users user) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            
            FrontUserDashboardController controller = loader.getController();
            controller.initUser(user);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
