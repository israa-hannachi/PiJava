package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import tn.esprit.entities.users.Users;
import java.io.IOException;

public class FrontProfileController {

    @FXML private javafx.scene.image.ImageView coverImage;
    @FXML private javafx.scene.image.ImageView profileImage;
    @FXML private javafx.scene.control.TextField searchField;

    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel2;
    @FXML private Label emailLabel2;

    private Users currentUser;

    public void initUser(Users user) {
        this.currentUser = user;
        if (user != null) {
            fullNameLabel.setText(user.getFirstName() + " " + user.getLastName());
            roleLabel.setText(user.getRole() != null ? user.getRole().toUpperCase() : "UTILISATEUR");
            firstNameField.setText(user.getFirstName());
            lastNameField.setText(user.getLastName());
            emailLabel.setText(user.getEmail());
            emailLabel2.setText(user.getEmail());
            roleLabel2.setText(user.getRole() != null ? user.getRole().toUpperCase() : "UTILISATEUR");
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
    public void handleHome(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_user_dashboard.fxml", event);
    }
    
    @FXML
    public void handleCours(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_CoursCategories.fxml", event);
    }
    
    @FXML
    public void handleJeux(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_GameList.fxml", event);
    }
    
    @FXML
    public void handleEvents(ActionEvent event) {
        navigateTo("/tn/esprit/view/frontEvent.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            
            // Pass the current user if the controller supports it
            Object controller = loader.getController();
            if (controller instanceof FrontUserDashboardController) {
                ((FrontUserDashboardController) controller).initUser(currentUser);
            } else if (controller instanceof FrontCoursCategorieController) {
                ((FrontCoursCategorieController) controller).initUser(currentUser);
            } else if (controller instanceof EventFrontController) {
                ((EventFrontController) controller).initUser(currentUser);
            }

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleSaveProfile(ActionEvent event) {
        String newFirst = firstNameField.getText().trim();
        String newLast = lastNameField.getText().trim();
        
        if (newFirst.isEmpty() || newLast.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Le nom et le prénom ne peuvent pas être vides.");
            return;
        }

        currentUser.setFirstName(newFirst);
        currentUser.setLastName(newLast);

        tn.esprit.controllers.users.UsersController uc = new tn.esprit.controllers.users.UsersController();
        String errorMsg = uc.modifierUser(currentUser);

        if (errorMsg == null) {
            fullNameLabel.setText(newFirst + " " + newLast);
            showAlert(Alert.AlertType.INFORMATION, "Profil Sauvegardé", "Vos informations personnelles ont été mises à jour dans la base de données !");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur de Modification", "La modification a échoué :\n" + errorMsg);
        }
    }

    @FXML
    public void handleEnable2FA(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Security", "2FA Feature integration pending.");
    }

    @FXML
    public void handleRegisterFace(ActionEvent event) {
        showAlert(Alert.AlertType.INFORMATION, "Biometrics", "Face ID setup pending.");
    }

    @FXML
    public void handleChangePassword(ActionEvent event) {
        javafx.scene.control.TextInputDialog dialog = new javafx.scene.control.TextInputDialog();
        dialog.setTitle("Sécurité du Compte");
        dialog.setHeaderText("Mise à jour de votre mot de passe");
        dialog.setContentText("Nouveau mot de passe :");

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newPass = result.get().trim();

            tn.esprit.controllers.users.UsersController uc = new tn.esprit.controllers.users.UsersController();
            String errorMsg = uc.changePassword(currentUser.getId(), newPass);

            if (errorMsg == null) {
                // Keep local session password in sync
                currentUser.setPassword(tn.esprit.controllers.users.UsersController.hashPassword(newPass));
                showAlert(Alert.AlertType.INFORMATION, "Sécurité Renforcée", "Le mot de passe a été chiffré et modifié avec succès !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", errorMsg);
            }
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
