package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import tn.esprit.services.security.PasswordResetService;
import tn.esprit.utils.ResetContext;
import java.io.IOException;

public class FrontVerifyResetController {
    @FXML private Label emailLabel;
    @FXML private TextField codeField;
    @FXML private Label errorMessageLabel;
    @FXML private Label successMessageLabel;

    private final PasswordResetService resetService = new PasswordResetService();

    @FXML
    public void initialize() {
        // Display the email address stored in ResetContext (set by forgot password flow)
        if (ResetContext.email != null) {
            emailLabel.setText("Email: " + ResetContext.email);
        }
    }

    @FXML
    public void handleVerifyCode(ActionEvent event) {
        String email = ResetContext.email;
        String code = codeField.getText();
        if (email == null || email.isBlank()) {
            showError("No email stored. Return to login.");
            return;
        }
        if (code == null || code.isBlank()) {
            showError("Please enter the verification code.");
            return;
        }
        boolean ok = resetService.verifyCode(email, code.trim());
        if (ok) {
            // success – move to the password‑reset screen where the user can set a new password
            navigateTo("/tn/esprit/view/front_reset_password.fxml", event);
        } else {
            showError("Invalid or expired code. Please request a new one.");
        }
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
}
