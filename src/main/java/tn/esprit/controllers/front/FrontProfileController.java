package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import tn.esprit.entities.users.Users;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.tools.FaceAuthDialog;
import tn.esprit.tools.UserAvatarUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class FrontProfileController {

    private static final Path USER_MEDIA_DIR = Path.of(System.getProperty("user.home"), ".naja7ni", "media");

    @FXML private javafx.scene.image.ImageView coverImage;
    @FXML private javafx.scene.image.ImageView profileImage;
    @FXML private javafx.scene.image.ImageView navProfileImage;
    @FXML private javafx.scene.control.TextField searchField;

    @FXML private Label fullNameLabel;
    @FXML private Label roleLabel;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Label emailLabel;
    @FXML private Label roleLabel2;
    @FXML private Label emailLabel2;
    @FXML private Label twoFaStatusLabel;
    @FXML private Button twoFaButton;
    @FXML private Label faceAuthStatusLabel;
    @FXML private Button faceAuthButton;

    private Users currentUser;

    @FXML
    public void initialize() {
        // Ensure the cover always fills its container and the profile photo stays circular.
        Platform.runLater(() -> {
            if (coverImage != null && coverImage.getParent() instanceof javafx.scene.layout.Region region) {
                coverImage.fitWidthProperty().bind(region.widthProperty());
                coverImage.fitHeightProperty().bind(region.heightProperty());
                coverImage.setPreserveRatio(false);
            }
            applyCircularProfileClip();
        });
    }

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
            loadProfileMedia();
            refreshTwoFaUi();
            refreshFaceAuthUi();
        }
    }

    private void loadProfileMedia() {
        Image profile = UserAvatarUtils.resolveUserImage(currentUser.getProfilePicture(), getClass());
        if (profile != null) {
            profileImage.setImage(profile);
            if (navProfileImage != null) {
                navProfileImage.setImage(profile);
            }
        }
        applyCircularProfileClip();
        applyNavProfileClip();

        Image cover = UserAvatarUtils.resolveUserImage(currentUser.getCoverPicture(), getClass());
        if (cover != null) {
            coverImage.setImage(cover);
            coverImage.setVisible(true);
        } else {
            coverImage.setVisible(false);
        }
    }

    private void applyNavProfileClip() {
        if (navProfileImage == null) return;
        double radius = 20.0;
        Circle clip = new Circle(20.0, 20.0, radius);
        navProfileImage.setClip(clip);
    }

    private void applyCircularProfileClip() {
        if (profileImage == null) {
            return;
        }
        double radius = Math.min(profileImage.getFitWidth(), profileImage.getFitHeight()) / 2.0;
        Circle clip = new Circle(profileImage.getFitWidth() / 2.0, profileImage.getFitHeight() / 2.0, radius);
        profileImage.setClip(clip);
    }

    private void refreshTwoFaUi() {
        if (currentUser == null || twoFaStatusLabel == null || twoFaButton == null) {
            return;
        }

        if (currentUser.isTwoFactorEnabled()) {
            twoFaStatusLabel.setText("Two-factor authentication is enabled on your account.");
            twoFaStatusLabel.setStyle("-fx-text-fill:#166534; -fx-background-color:#DCFCE7; -fx-background-radius:10; -fx-padding:10;");
            twoFaButton.setText("Disable Two-Factor Authentication");
            twoFaButton.setStyle("-fx-background-color:#EF4444; -fx-text-fill:white; -fx-font-weight:700; -fx-background-radius:10;");
        } else {
            twoFaStatusLabel.setText("Your account is not protected with 2FA");
            twoFaStatusLabel.setStyle("-fx-text-fill:#854d0e; -fx-background-color:#FEF9C3; -fx-background-radius:10; -fx-padding:10;");
            twoFaButton.setText("Enable Two-Factor Authentication");
            twoFaButton.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-font-weight:700; -fx-background-radius:10;");
        }
    }

    private void refreshFaceAuthUi() {
        if (currentUser == null || faceAuthStatusLabel == null || faceAuthButton == null) {
            return;
        }

        if (currentUser.isFaceAuthEnabled()) {
            faceAuthStatusLabel.setText("Face ID is active on your account.");
            faceAuthStatusLabel.setStyle("-fx-text-fill:#166534; -fx-background-color:#DCFCE7; -fx-background-radius:10; -fx-padding:10;");
            faceAuthButton.setText("Update Face Data");
            faceAuthButton.setStyle("-fx-background-color:#8B5CF6; -fx-text-fill:white; -fx-font-weight:700; -fx-background-radius:10;");
        } else {
            faceAuthStatusLabel.setText("Face ID is not set up");
            faceAuthStatusLabel.setStyle("-fx-text-fill:#854d0e; -fx-background-color:#FEF9C3; -fx-background-radius:10; -fx-padding:10;");
            faceAuthButton.setText("Register Face");
            faceAuthButton.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-font-weight:700; -fx-background-radius:10;");
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
        navigateTo("/tn/esprit/view/front_user_dashboard.fxml");
    }
    
    @FXML
    public void handleCours(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_CoursCategories.fxml");
    }
    
    @FXML
    public void handleGameList(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_GameList.fxml");
    }
    
    @FXML
    public void handleEvents(ActionEvent event) {
        navigateTo("/tn/esprit/view/frontEvent.fxml");
    }

    @FXML
    public void handleForums(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_forum.fxml");
    }

    @FXML
    public void handleMeets(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_MeetList.fxml");
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        // Already on profile, just refresh if needed
        initUser(currentUser);
    }

    @FXML
    public void handleChangeProfilePhoto(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Photo Profil", "Session utilisateur introuvable.");
            return;
        }

        File selectedFile = chooseImageFile(event, "Choisir une photo de profil");
        if (selectedFile == null) {
            return;
        }

        try {
            String storedPath = copySelectedImageToStorage(selectedFile, "profile_" + currentUser.getId());
            currentUser.setProfilePicture(storedPath);
            String err = persistProfileMedia();
            if (err != null) {
                showAlert(Alert.AlertType.ERROR, "Photo Profil", err);
                return;
            }
            loadProfileMedia();
            showAlert(Alert.AlertType.INFORMATION, "Photo Profil", "Photo de profil mise a jour avec succes.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Photo Profil", "Impossible d'enregistrer l'image: " + e.getMessage());
        }
    }

    @FXML
    public void handleChangeCoverPhoto(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Photo Couverture", "Session utilisateur introuvable.");
            return;
        }

        File selectedFile = chooseImageFile(event, "Choisir une image de couverture");
        if (selectedFile == null) {
            return;
        }

        try {
            String storedPath = copySelectedImageToStorage(selectedFile, "cover_" + currentUser.getId());
            currentUser.setCoverPicture(storedPath);
            String err = persistProfileMedia();
            if (err != null) {
                showAlert(Alert.AlertType.ERROR, "Photo Couverture", err);
                return;
            }
            loadProfileMedia();
            showAlert(Alert.AlertType.INFORMATION, "Photo Couverture", "Image de couverture mise a jour avec succes.");
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Photo Couverture", "Impossible d'enregistrer l'image: " + e.getMessage());
        }
    }

    private String persistProfileMedia() {
        UsersController uc = new UsersController();
        return uc.updateProfileMedia(currentUser.getId(), currentUser.getProfilePicture(), currentUser.getCoverPicture());
    }

    private File chooseImageFile(ActionEvent event, String title) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle(title);
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(
                "Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"
        ));

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        return chooser.showOpenDialog(stage);
    }

    private String copySelectedImageToStorage(File sourceFile, String prefix) throws IOException {
        Files.createDirectories(USER_MEDIA_DIR);

        String ext = getExtension(sourceFile.getName());
        String fileName = prefix + "_" + System.currentTimeMillis() + ext;
        Path destination = USER_MEDIA_DIR.resolve(fileName);

        Files.copy(sourceFile.toPath(), destination, StandardCopyOption.REPLACE_EXISTING);
        return destination.toAbsolutePath().toString();
    }

    private String getExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return ".png";
        }
        return fileName.substring(dotIndex);
    }

    private void navigateTo(String fxmlPath) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            
            // Pass the current user if the controller supports it
            Object controller = loader.getController();
            if (controller instanceof FrontUserDashboardController) {
                ((FrontUserDashboardController) controller).initUser(currentUser);
            } else if (controller instanceof FrontProfileController) {
                ((FrontProfileController) controller).initUser(currentUser);
            } else if (controller instanceof FrontGameListController) {
                ((FrontGameListController) controller).initUser(currentUser);
            } else if (controller instanceof EventFrontController) {
                ((EventFrontController) controller).initUser(currentUser);
            } else if (controller instanceof FrontForumController) {
                ((FrontForumController) controller).initUser(currentUser);
            } else if (controller instanceof FrontMeetListController) {
                ((FrontMeetListController) controller).initUser(currentUser);
            } else if (controller instanceof FrontCoursCategorieController) {
                ((FrontCoursCategorieController) controller).initUser(currentUser);
            } else if (controller instanceof FrontMeetCalendarController) {
                ((FrontMeetCalendarController) controller).initUser(currentUser);
            } else if (controller instanceof FrontGameListController) {
                ((FrontGameListController) controller).initUser(currentUser);
            }

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) profileImage.getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Navigation Error: " + e.getMessage());
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur de navigation: " + e.toString()).show();
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
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Security", "Session utilisateur introuvable.");
            return;
        }

        UsersController usersController = new UsersController();

        if (currentUser.isTwoFactorEnabled()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Two-Factor Authentication");
            confirm.setHeaderText("Disable 2FA?");
            confirm.setContentText("You will no longer be asked for an authenticator code at login.");

            Optional<ButtonType> choice = confirm.showAndWait();
            if (choice.isPresent() && choice.get() == ButtonType.OK) {
                String err = usersController.disable2FA(currentUser.getId());
                if (err != null) {
                    showAlert(Alert.AlertType.ERROR, "2FA", err);
                    return;
                }
                currentUser.setGoogleAuthenticatorSecret(null);
                refreshTwoFaUi();
                showAlert(Alert.AlertType.INFORMATION, "2FA", "Two-factor authentication was disabled.");
            }
            return;
        }

        String secret = usersController.generate2FASecret();
        String otpAuthUri = usersController.buildOtpAuthUri(currentUser, secret);
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=240x240&data="
                + URLEncoder.encode(otpAuthUri, StandardCharsets.UTF_8);

        ImageView qrImage = new ImageView(new Image(qrUrl, true));
        qrImage.setFitWidth(240);
        qrImage.setFitHeight(240);
        qrImage.setPreserveRatio(true);

        Label help = new Label("1) Scan this QR code with Google/Microsoft Authenticator\n"
                + "2) Enter the 6-digit code to confirm activation");

        TextField codeField = new TextField();
        codeField.setPromptText("Enter 6-digit code");

        VBox content = new VBox(10, help, qrImage, new Label("Verification code:"), codeField);
        content.setPadding(new Insets(10));

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Enable Two-Factor Authentication");
        dialog.setHeaderText("Scan QR and verify your code");
        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        String verificationCode = codeField.getText() != null ? codeField.getText().trim() : "";
        if (!usersController.verify2FACode(secret, verificationCode)) {
            showAlert(Alert.AlertType.ERROR, "2FA", "Invalid code. Please scan again and retry.");
            return;
        }

        String err = usersController.enable2FA(currentUser.getId(), secret);
        if (err != null) {
            showAlert(Alert.AlertType.ERROR, "2FA", err);
            return;
        }

        currentUser.setGoogleAuthenticatorSecret(secret);
        refreshTwoFaUi();
        showAlert(Alert.AlertType.INFORMATION, "2FA", "Two-factor authentication is now enabled.");
    }

    @FXML
    public void handleRegisterFace(ActionEvent event) {
        if (currentUser == null) {
            showAlert(Alert.AlertType.ERROR, "Biometrics", "Session utilisateur introuvable.");
            return;
        }

        FaceAuthDialog.openRegistration(currentUser, descriptorJson -> {
            UsersController usersController = new UsersController();
            String err = usersController.updateBiometricDescriptor(currentUser.getId(), descriptorJson);
            Platform.runLater(() -> {
                if (err == null) {
                    currentUser.setBiometricDescriptor(descriptorJson);
                    refreshFaceAuthUi();
                    showAlert(Alert.AlertType.INFORMATION, "Biometrics", "Face ID registered successfully.");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Biometrics", err);
                }
            });
        }, message -> Platform.runLater(() -> showAlert(Alert.AlertType.ERROR, "Biometrics", message)));
    }

    @FXML
    public void handleChangePassword(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Sécurité du Compte");
        dialog.setHeaderText("Mise à jour de votre mot de passe");
        dialog.setContentText("Nouveau mot de passe :");

        java.util.Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String newPass = result.get().trim();

            UsersController uc = new UsersController();
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
