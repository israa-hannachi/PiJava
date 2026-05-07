package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.event.ActionEvent;
import java.io.IOException;

public class FrontForgotPasswordController {
    
    @FXML private TextField emailField;
    @FXML private Label successMessageLabel;
    @FXML private Label errorMessageLabel;

    @FXML
    public void handleSendResetCode(ActionEvent event) {
        if (emailField.getText() == null || emailField.getText().isEmpty()) {
            errorMessageLabel.setVisible(true);
            errorMessageLabel.setManaged(true);
            successMessageLabel.setVisible(false);
            successMessageLabel.setManaged(false);
            errorMessageLabel.setText("Please enter an email address.");
            return;
        }
        
        errorMessageLabel.setVisible(false);
        errorMessageLabel.setManaged(false);
        successMessageLabel.setVisible(true);
        successMessageLabel.setManaged(true);
        successMessageLabel.setText("If an account exists, a reset code was sent.");
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
}
