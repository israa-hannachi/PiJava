package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import java.io.IOException;
import tn.esprit.services.security.PasswordResetService;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;
import tn.esprit.utils.ResetContext;

public class FrontForgotPasswordController {

    @FXML private TextField emailField;
    @FXML private TextField codeField;
    @FXML private Label errorMessageLabel;
    @FXML private Label successMessageLabel;
    @FXML private Label verifyCodeLabel;
    @FXML private VBox verifyCodeSection;
    @FXML private javafx.scene.control.Button verifyCodeButton;

    private final PasswordResetService resetService = new PasswordResetService();
    private final UsersController usersController = new UsersController();

    @FXML
    public void handleSendResetCode(ActionEvent event) {
        String email = emailField.getText();
        if (email == null || email.isBlank()) {
            showError("Please enter an email address.");
            return;
        }

        String normalizedEmail = email.trim();
        if (!normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            showError("Please enter a valid email address.");
            return;
        }

        try {
            Users user = usersController.findByEmail(normalizedEmail);
            if (user == null) {
                showError("No account found with this email.");
                return;
            }

            resetService.requestReset(normalizedEmail);
            ResetContext.email = normalizedEmail;

            successMessageLabel.setText("A verification code has been sent to your email.");
            successMessageLabel.setVisible(true);
            successMessageLabel.setManaged(true);
            errorMessageLabel.setVisible(false);
            errorMessageLabel.setManaged(false);
            showCodeEntrySection(true);
        } catch (Exception e) {
            showError("Failed to send reset code: " + e.getMessage());
        }
    }

    @FXML
    public void handleVerifyCode(ActionEvent event) {
        String email = ResetContext.email;
        String code = codeField.getText();

        if (email == null || email.isBlank()) {
            showError("No email stored. Please request a new code.");
            return;
        }
        if (code == null || code.isBlank()) {
            showError("Please enter the verification code.");
            return;
        }

        boolean ok = resetService.verifyCode(email, code.trim());
        if (!ok) {
            showError("Invalid or expired code. Please request a new one.");
            return;
        }

        successMessageLabel.setText("Code verified. You can now set a new password.");
        successMessageLabel.setVisible(true);
        successMessageLabel.setManaged(true);
        errorMessageLabel.setVisible(false);
        errorMessageLabel.setManaged(false);
        navigateTo("/tn/esprit/view/front_reset_password.fxml", event);
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_login.fxml", event);
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

    private void showError(String msg) {
        errorMessageLabel.setText(msg);
        errorMessageLabel.setVisible(true);
        errorMessageLabel.setManaged(true);
        successMessageLabel.setVisible(false);
        successMessageLabel.setManaged(false);
    }

    private void showCodeEntrySection(boolean show) {
        verifyCodeSection.setVisible(show);
        verifyCodeSection.setManaged(show);
        verifyCodeLabel.setVisible(show);
        verifyCodeLabel.setManaged(show);
        codeField.setVisible(show);
        codeField.setManaged(show);
        verifyCodeButton.setVisible(show);
        verifyCodeButton.setManaged(show);
    }
}
