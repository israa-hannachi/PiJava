package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;

import java.io.IOException;
import java.util.List;

public class AdminDashboardController {

    @FXML private VBox comptesSubmenu;
    @FXML private VBox coursSubmenu;
    @FXML private VBox jeuxSubmenu;
    @FXML private VBox forumSubmenu;
    @FXML private VBox eventsSubmenu;
    @FXML private VBox meetSubmenu;
    @FXML private VBox mailingSubmenu;

    @FXML private Label adminNameLabel;
    @FXML private Label totalUsersLabel;

    private Users currentUser;

    public void initAdmin(Users user) {
        this.currentUser = user;
        if (user != null) {
            adminNameLabel.setText("👑 " + user.getFirstName() + " " + user.getLastName());
        }
        // Load real user count from DB
        try {
            UsersController uc = new UsersController();
            List<Users> allUsers = uc.recupererUsers();
            totalUsersLabel.setText(String.valueOf(allUsers.size()));
        } catch (Exception e) {
            totalUsersLabel.setText("—");
        }
    }

    // ─── SIDEBAR TOGGLE HANDLERS ────────────────────────────────────────────────

    @FXML public void toggleComptesMenu(ActionEvent event) { toggleMenu(comptesSubmenu); }
    @FXML public void toggleCoursMenu(ActionEvent event)   { toggleMenu(coursSubmenu); }
    @FXML public void toggleJeuxMenu(ActionEvent event)    { toggleMenu(jeuxSubmenu); }
    @FXML public void toggleForumMenu(ActionEvent event)   { toggleMenu(forumSubmenu); }
    @FXML public void toggleEventsMenu(ActionEvent event)  { toggleMenu(eventsSubmenu); }
    @FXML public void toggleMeetMenu(ActionEvent event)    { toggleMenu(meetSubmenu); }
    @FXML public void toggleMailingMenu(ActionEvent event) { toggleMenu(mailingSubmenu); }

    private void toggleMenu(VBox submenu) {
        boolean showing = submenu.isVisible();
        submenu.setVisible(!showing);
        submenu.setManaged(!showing);
    }

    // ─── NAVIGATION HANDLERS ─────────────────────────────────────────────────────

    @FXML
    public void handleDashboard(ActionEvent event) {
        initAdmin(currentUser);
    }

    @FXML
    public void handleListeComptes(ActionEvent event) {
        navigateTo("/tn/esprit/view/user_index.fxml", event, UserIndexController.class,
                (ctrl) -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_profile.fxml", event, FrontProfileController.class,
                (ctrl) -> ctrl.initUser(currentUser));
    }

    @FXML
    public void handleBackFront(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_user_dashboard.fxml", event, FrontUserDashboardController.class,
                (ctrl) -> ctrl.initUser(currentUser));
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/tn/esprit/view/front_login.fxml"));
            showScene(root, event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleGameList(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/tn/esprit/view/front_GameList.fxml"));
            showScene(root, event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleBackList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/back_GameList.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    // ─── GENERIC NAVIGATION UTILITY ──────────────────────────────────────────────

    @FunctionalInterface
    interface ControllerInit<T> { void init(T controller); }

    private <T> void navigateTo(String fxmlPath, ActionEvent event, Class<T> controllerClass, ControllerInit<T> init) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Parent root = loader.load();
            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            init.init(controller);
            showScene(root, event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showScene(javafx.scene.Parent root, ActionEvent event) {
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }

}
