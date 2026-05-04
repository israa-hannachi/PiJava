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
import tn.esprit.controllers.event.BackEventController;
import tn.esprit.controllers.Back.BackGameListController;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;
import tn.esprit.controllers.front.UserIndexController;
import tn.esprit.controllers.Back.BackMeetController;
import tn.esprit.controllers.Back.BackParticipantController;

import java.io.IOException;
import java.net.URL;
import java.util.List;

public class AdminDashboardController {

    @FXML private VBox comptesSubmenu;
    @FXML private VBox coursSubmenu;
    @FXML private VBox jeuxSubmenu;
    @FXML private VBox forumSubmenu;
    @FXML private VBox eventsSubmenu;
    @FXML private VBox meetSubmenu;

    @FXML private Label adminNameLabel;
    @FXML private Label totalUsersLabel;

    private Users currentUser;

    public void initAdmin(Users user) {
        this.currentUser = user;
        if (user != null) {
            adminNameLabel.setText("👑 " + user.getFirstName() + " " + user.getLastName());
        }
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
            URL fxmlUrl = getClass().getResource("/tn/esprit/view/front_login.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML non trouvé: /tn/esprit/view/front_login.fxml");
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            showScene(root, event);
        } catch (IOException e) {
            System.err.println("Erreur logout: " + e.getMessage());
        }
    }

    @FXML
    public void handleGameList(ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource("/tn/esprit/view/front_GameList.fxml");
            if (fxmlUrl == null) {
                System.err.println("FXML non trouvé: /tn/esprit/view/front_GameList.fxml");
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            showScene(root, event);
        } catch (IOException e) {
            System.err.println("Erreur navigation jeux: " + e.getMessage());
        }
    }

    @FXML
    public void handleBackList(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_GameList.fxml", event, BackGameListController.class,
                (ctrl) -> ctrl.initAdmin(currentUser));
    }

    // ─── COURS NAVIGATION ────────────────────────────────────────────────────────

    @FXML
    public void handleCategories(ActionEvent event) {
        navigateSimple("/tn/esprit/view/back_CoursCategorieList.fxml", event);
    }

    @FXML
    public void handleModules(ActionEvent event) {
        navigateSimple("/tn/esprit/view/back_CoursModuleList.fxml", event);
    }

    @FXML
    public void handleCours(ActionEvent event) {
        navigateSimple("/tn/esprit/view/back_CoursList.fxml", event);
    }

    @FXML
    public void handleEventsList(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_Event.fxml", event, BackEventController.class,
                (ctrl) -> {
                    ctrl.initAdmin(currentUser);
                    ctrl.selectTab(0);
                });
    }

    @FXML
    public void handleEventsCalendrier(ActionEvent event) {
        handleEventsList(event); // For now, points to events tab
    }

    @FXML
    public void handleEventsSponsors(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_Event.fxml", event, BackEventController.class,
                (ctrl) -> {
                    ctrl.initAdmin(currentUser);
                    ctrl.selectTab(2);
                });
    }

    @FXML
    public void handleEventsInscriptions(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_Event.fxml", event, BackEventController.class,
                (ctrl) -> {
                    ctrl.initAdmin(currentUser);
                    ctrl.selectTab(1);
                });
    }

    @FXML
    public void handleForums(ActionEvent event) {
        navigateSimple("/tn/esprit/view/back_forum_dashboard.fxml", event);
    }

    @FXML
    public void handleMeetList(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_MeetList.fxml", event, BackMeetController.class,
                (ctrl) -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleMeetDashboard(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_MeetDashboard.fxml", event,
                tn.esprit.controllers.Back.AdminMeetDashboardController.class,
                (ctrl) -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleParticipants(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_ParticipantList.fxml", event, BackParticipantController.class,
                (ctrl) -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleSentEmails(ActionEvent event) {
        // Navigate to sent emails view - using meet list as placeholder until dedicated view is created
        navigateTo("/tn/esprit/view/back_MeetList.fxml", event, BackMeetController.class,
                (ctrl) -> ctrl.initAdmin(currentUser));
    }

    // ─── GENERIC NAVIGATION UTILITY ──────────────────────────────────────────────

    @FunctionalInterface
    public interface ControllerInit<T> { void init(T controller); }

    private <T> void navigateTo(String fxmlPath, ActionEvent event, Class<T> controllerClass, ControllerInit<T> init) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML non trouvé: " + fxmlPath);
                return;
            }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();
            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            init.init(controller);
            showScene(root, event);
        } catch (IOException e) {
            System.err.println("Erreur navigation complexe: " + e.getMessage());
        }
    }

    private void navigateSimple(String fxmlPath, ActionEvent event) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) {
                System.err.println("FXML non trouvé: " + fxmlPath);
                return;
            }
            Parent root = FXMLLoader.load(fxmlUrl);
            showScene(root, event);
        } catch (IOException e) {
            System.err.println("Erreur navigation simple: " + e.getMessage());
        }
    }

    private void showScene(Parent root, ActionEvent event) {
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.show();
    }
}
