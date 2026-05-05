package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;
import tn.esprit.utils.ResetContext;

import java.io.IOException;

public class FrontResetPasswordController {

    @FXML private TextField emailField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label messageLabel;

    private final UsersController usersController = new UsersController();

    @FXML
    public void initialize() {
        if (ResetContext.email != null) {
            emailField.setText(ResetContext.email);
        }
    }

    @FXML
    public void handleResetPassword(ActionEvent event) {
        String email = ResetContext.email;
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (email == null || email.isBlank()) {
            showMessage("No reset session found. Please restart the forgot password flow.", true);
            return;
        }

        if (newPassword == null || newPassword.isBlank() || confirmPassword == null || confirmPassword.isBlank()) {
            showMessage("Please fill in both password fields.", true);
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showMessage("Passwords do not match.", true);
            return;
        }

        Users user = usersController.findByEmail(email);
        if (user == null) {
            showMessage("User not found for this email.", true);
            return;
        }

        String error = usersController.changePassword(user.getId(), newPassword);
        if (error != null) {
            showMessage(error, true);
            return;
        }

        ResetContext.email = null;
        showMessage("Password updated successfully. Redirecting to login...", false);
        navigateTo("/tn/esprit/view/front_login.fxml", event);
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        ResetContext.email = null;
        navigateTo("/tn/esprit/view/front_login.fxml", event);
    }

    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
        if (isError) {
            messageLabel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 10; -fx-background-radius: 8;");
        } else {
            messageLabel.setStyle("-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 10; -fx-background-radius: 8;");
        }
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
}
