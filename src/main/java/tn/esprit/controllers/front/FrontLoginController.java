package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.application.Platform;
import javafx.concurrent.Task;
import java.io.IOException;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Alert;
import javafx.scene.control.TextInputDialog;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;
import tn.esprit.tools.GoogleOAuthService;
import tn.esprit.tools.FaceAuthDialog;

import java.util.Optional;

public class FrontLoginController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private CheckBox rememberCheck;

    @FXML
    public void handleForgotPassword() {
        navigateTo("/tn/esprit/view/front_forgot_password.fxml");
    }

    @FXML
    public void handleSignup() {
        navigateTo("/tn/esprit/view/front_register.fxml");
    }

    @FXML
    public void handleGoogleLogin() {
        Task<Users> googleLoginTask = new Task<>() {
            @Override
            protected Users call() throws Exception {
                UsersController usersController = new UsersController();
                GoogleOAuthService.GoogleUserInfo googleUser = usersController.authenticateWithGoogle();
                return usersController.findOrCreateGoogleUser(googleUser);
            }
        };

        googleLoginTask.setOnSucceeded(workerStateEvent -> {
            Users user = googleLoginTask.getValue();
            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Google Login", "Unable to create or load your account from Google.");
                return;
            }

            try {
                UsersController usersController = new UsersController();
                if (user.isTwoFactorEnabled()) {
                    TextInputDialog otpDialog = new TextInputDialog();
                    otpDialog.setTitle("Two-Factor Authentication");
                    otpDialog.setHeaderText("Enter your authenticator code");
                    otpDialog.setContentText("6-digit code:");

                    Optional<String> otpResult = otpDialog.showAndWait();
                    if (otpResult.isEmpty()) {
                        showAlert(Alert.AlertType.WARNING, "Login Cancelled", "Authentication code is required to continue.");
                        return;
                    }

                    String otpCode = otpResult.get().trim();
                    if (!usersController.verify2FACode(user.getGoogleAuthenticatorSecret(), otpCode)) {
                        showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid authenticator code.");
                        return;
                    }
                }

                System.out.println("✅ Google user logged in: " + user.getEmail());
                navigateToDashboard("/tn/esprit/view/front_user_dashboard.fxml", user);
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Google Login", "Google sign-in failed: " + e.getMessage());
            }
        });

        googleLoginTask.setOnFailed(workerStateEvent -> {
            Throwable error = googleLoginTask.getException();
            showAlert(Alert.AlertType.ERROR, "Google Login", "Google sign-in failed: " + (error != null ? error.getMessage() : "Unknown error"));
        });

        Thread googleLoginThread = new Thread(googleLoginTask, "google-login-task");
        googleLoginThread.setDaemon(true);
        googleLoginThread.start();
    }

    @FXML
    public void handleFaceLogin(ActionEvent event) {
        FaceAuthDialog.openLogin((email, biometricDescriptorJson) -> Platform.runLater(() -> {
            UsersController usersController = new UsersController();
            Users user = usersController.authenticateWithFace(email, biometricDescriptorJson);

            if (user == null) {
                showAlert(Alert.AlertType.ERROR, "Face Login", "Face verification failed or Face ID is not registered.");
                return;
            }

            handleAuthenticatedUser(user);
        }), message -> Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Face Login", message)));
    }

    @FXML
    public void handleLogin() {
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

        handleAuthenticatedUser(user);
    }

    private void handleAuthenticatedUser(Users user) {
        UsersController usersController = new UsersController();

        if (user.isTwoFactorEnabled()) {
            TextInputDialog otpDialog = new TextInputDialog();
            otpDialog.setTitle("Two-Factor Authentication");
            otpDialog.setHeaderText("Enter your authenticator code");
            otpDialog.setContentText("6-digit code:");

            Optional<String> otpResult = otpDialog.showAndWait();
            if (otpResult.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Login Cancelled", "Authentication code is required to continue.");
                return;
            }

            String otpCode = otpResult.get().trim();
            if (!usersController.verify2FACode(user.getGoogleAuthenticatorSecret(), otpCode)) {
                showAlert(Alert.AlertType.ERROR, "Login Error", "Invalid authenticator code.");
                return;
            }
        }

        System.out.println("✅ User Logged In: " + user.getFirstName() + " " + user.getLastName());
        navigateToDashboard("/tn/esprit/view/front_user_dashboard.fxml", user);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void navigateTo(String fxmlPath) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(fxmlPath));
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not load " + fxmlPath + "\nError: " + e.getMessage());
        }
    }

    private void navigateToDashboard(String fxmlPath, Users user) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            
            FrontUserDashboardController controller = loader.getController();
            if (controller != null) {
                controller.initUser(user);
            }

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) emailField.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Dashboard Error", "Failed to open dashboard: " + e.getMessage());
        }
    }
}
