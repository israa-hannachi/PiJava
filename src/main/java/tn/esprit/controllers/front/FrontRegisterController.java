package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;

import java.io.IOException;

public class FrontRegisterController {

    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> roleCombo;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private CheckBox termsCheck;
    @FXML private javafx.scene.canvas.Canvas captchaCanvas;
    @FXML private TextField captchaField;

    private final UsersController usersController = new UsersController();
    private String currentCaptcha;

    @FXML
    public void initialize() {
        generateCaptcha();
    }

    @FXML
    public void generateCaptcha() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        currentCaptcha = sb.toString();
        renderCaptcha();
    }

    private void renderCaptcha() {
        javafx.scene.canvas.GraphicsContext gc = captchaCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, captchaCanvas.getWidth(), captchaCanvas.getHeight());

        // Background noise
        gc.setStroke(javafx.scene.paint.Color.LIGHTGRAY);
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 15; i++) {
            gc.strokeLine(rnd.nextInt(150), rnd.nextInt(50), rnd.nextInt(150), rnd.nextInt(50));
        }

        // Draw text with some "messy" offsets
        gc.setFont(javafx.scene.text.Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 24));
        gc.setFill(javafx.scene.paint.Color.web("#0FB5A9"));
        
        for (int i = 0; i < currentCaptcha.length(); i++) {
            gc.save();
            // Smoother spacing and less aggressive vertical jitter
            gc.translate(15 + i * 22, 32 + rnd.nextInt(6) - 3);
            // Subtle rotation for readability
            gc.rotate(rnd.nextInt(16) - 8);
            gc.fillText(String.valueOf(currentCaptcha.charAt(i)), 0, 0);
            gc.restore();
        }
    }

    @FXML
    public void handleRegister(ActionEvent event) {
        if (captchaField.getText() == null || !captchaField.getText().equalsIgnoreCase(currentCaptcha)) {
            showAlert(Alert.AlertType.ERROR, "Security Verification", "Invalid CAPTCHA code. Please try again.");
            generateCaptcha();
            captchaField.clear();
            return;
        }

        if (!termsCheck.isSelected()) {
            showAlert(Alert.AlertType.WARNING, "Registration Error", "You must agree to the terms of service.");
            return;
        }

        if (passwordField.getText() == null || !passwordField.getText().equals(confirmPasswordField.getText())) {
            showAlert(Alert.AlertType.ERROR, "Password Error", "Passwords do not match.");
            return;
        }

        String role = roleCombo.getValue() != null ? roleCombo.getValue() : "";
        if(role.equalsIgnoreCase("Student")) {
            role = "etudiant";
        } else if (role.equalsIgnoreCase("Enseignant")) {
            role = "enseignant";
        }

        // Construct using your entity definition. Defaults applied to bypass strict Backend Validations.
        Users user = new Users(
                emailField.getText(),
                passwordField.getText(),
                firstNameField.getText(),
                lastNameField.getText(),
                "Non Defini", // default profession
                "Debutant", // default experience
                role,
                "Actif" // default status
        );

        String erreurBackend = usersController.ajouterUser(user);
        
        if (erreurBackend != null) {
            showAlert(Alert.AlertType.ERROR, "Registration Failed", erreurBackend);
        } else {
            showAlert(Alert.AlertType.INFORMATION, "Registration Status", "Votre compte a été créé avec succès!");
            // Optionally redirect to login immediately: handleSignInLink(event);
        }
    }

    @FXML
    public void handleSignInLink(ActionEvent event) {
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

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
