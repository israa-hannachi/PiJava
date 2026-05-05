package tn.esprit.controllers.front;

import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import tn.esprit.entities.users.Users;

public class FrontHomeController {

    @FXML private Button btnSignIn;
    @FXML private Button btnSignUp;
    @FXML private HBox userMenu;
    @FXML private Label userNameLabel;

    @FXML private ImageView heroImage;
    @FXML private ImageView brandLogo;
    @FXML private ImageView course1;
    @FXML private ImageView course2;
    @FXML private ImageView course3;
    @FXML private ImageView course4;
    @FXML private ImageView course5;
    @FXML private ImageView course6;
    @FXML private ImageView course7;
    @FXML private ImageView course8;

    private final List<String> missing = new ArrayList<>();

    @FXML
    public void initialize() {
        load(brandLogo, "/assets/images/logo.png");
        load(heroImage, "/assets/images/element/07.png");

        load(course1, "/assets/images/courses/4by3/08.jpg");
        load(course2, "/assets/images/courses/4by3/02.jpg");
        load(course3, "/assets/images/courses/4by3/03.jpg");
        load(course4, "/assets/images/courses/4by3/04.jpg");
        load(course5, "/assets/images/courses/4by3/05.jpg");
        load(course6, "/assets/images/courses/4by3/06.jpg");
        load(course7, "/assets/images/courses/4by3/07.jpg");
        load(course8, "/assets/images/courses/4by3/09.jpg");

        if (!missing.isEmpty()) {
            System.out.println("Missing images:");
            for (String path : missing) {
                System.out.println(" - " + path);
            }
        }

        FadeTransition ft = new FadeTransition(Duration.millis(450), heroImage);
        ft.setFromValue(0.0);
        ft.setToValue(1.0);
        ft.play();
    }

    private void load(ImageView target, String resourcePath) {
        try {
            URL url = getClass().getResource(resourcePath);
            if (url == null) {
                missing.add(resourcePath);
                return;
            }
            target.setImage(new Image(url.toExternalForm(), true));
        } catch (Exception ex) {
            missing.add(resourcePath);
        }
    }
    @FXML
    public void handleSignIn(javafx.event.ActionEvent event) {
        navigateTo("/tn/esprit/view/front_login.fxml", event);
    }

    @FXML
    public void handleSignUp(javafx.event.ActionEvent event) {
        navigateTo("/tn/esprit/view/front_register.fxml", event);
    }

    private void navigateTo(String fxmlPath, javafx.event.ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource(fxmlPath));
            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            javafx.stage.Stage stage = (javafx.stage.Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void setLoggedInUser(Users user) {
        if (user != null) {
            // Hide standard buttons
            if(btnSignIn != null) { btnSignIn.setVisible(false); btnSignIn.setManaged(false); }
            if(btnSignUp != null) { btnSignUp.setVisible(false); btnSignUp.setManaged(false); }
            
            // Show new user menu block
            if(userMenu != null) {
                userMenu.setVisible(true);
                userMenu.setManaged(true);
                userNameLabel.setText("👋 Welcome, " + user.getFirstName() + " " + user.getLastName());
            }
        }
    }

    @FXML
    public void handleLogout(javafx.event.ActionEvent event) {
        // Logs out securely and drops back to the login screen
        navigateTo("/tn/esprit/view/front_login.fxml", event);
    }
}
