package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
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
    @FXML private StackPane contentArea;

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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_admin.fxml"));
            Parent root = loader.load();
            AdminDashboardController ctrl = loader.getController();
            ctrl.initAdmin(currentUser);
            showScene(root, event);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleListeComptes(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/user_index.fxml", UserIndexController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        navigateFullScene("/tn/esprit/view/front_profile.fxml", event, FrontProfileController.class,
                ctrl -> ctrl.initUser(currentUser));
    }

    @FXML
    public void handleBackFront(ActionEvent event) {
        navigateFullScene("/tn/esprit/view/front_user_dashboard.fxml", event, FrontUserDashboardController.class,
                ctrl -> ctrl.initUser(currentUser));
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/front_login.fxml"));
            showScene(root, event);
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── JEUX ────────────────────────────────────────────────────────────────────

    @FXML
    public void handleGameList(ActionEvent event) {
        loadIntoContent("/tn/esprit/view/front_GameList.fxml");
    }

    @FXML
    public void handleBackList(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_GameList.fxml", BackGameListController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    // ─── COURS ───────────────────────────────────────────────────────────────────

    @FXML
    public void handleCategories(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_CoursCategorieList.fxml", tn.esprit.controllers.Back.BackCoursCategorieController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleModules(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_CoursModuleList.fxml", tn.esprit.controllers.Back.BackCoursModuleController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleCours(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_CoursList.fxml", tn.esprit.controllers.Back.BackCoursListController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    // ─── EVENTS ──────────────────────────────────────────────────────────────────

    @FXML
    public void handleEventsList(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_Event.fxml", BackEventController.class,
                ctrl -> { ctrl.initAdmin(currentUser); ctrl.selectTab(0); });
    }

    @FXML
    public void handleEventsCalendrier(ActionEvent event) {
        handleEventsList(event);
    }

    @FXML
    public void handleEventsSponsors(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_Event.fxml", BackEventController.class,
                ctrl -> { ctrl.initAdmin(currentUser); ctrl.selectTab(2); });
    }

    @FXML
    public void handleEventsInscriptions(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_Event.fxml", BackEventController.class,
                ctrl -> { ctrl.initAdmin(currentUser); ctrl.selectTab(1); });
    }

    // ─── FORUMS ──────────────────────────────────────────────────────────────────

    @FXML
    public void handleForums(ActionEvent event) {
        loadIntoContent("/tn/esprit/view/back_forum_dashboard.fxml");
    }

    // ─── MEET ────────────────────────────────────────────────────────────────────

    @FXML
    public void handleMeetList(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_MeetList.fxml", BackMeetController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleMeetDashboard(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_MeetDashboard.fxml",
                tn.esprit.controllers.Back.AdminMeetDashboardController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleParticipants(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_ParticipantList.fxml", BackParticipantController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleSentEmails(ActionEvent event) {
        loadIntoContentWithInit("/tn/esprit/view/back_MeetList.fxml", BackMeetController.class,
                ctrl -> ctrl.initAdmin(currentUser));
    }

    // ═══════════════════════════════════════════════════════════════════════════════
    // UTILITIES
    // ═══════════════════════════════════════════════════════════════════════════════

    /**
     * Loads FXML into contentArea (keeps admin sidebar visible).
     */
    private void loadIntoContent(String fxmlPath) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) { System.err.println("FXML non trouvé: " + fxmlPath); return; }
            Parent loaded = FXMLLoader.load(fxmlUrl);
            injectIntoContentArea(loaded);
        } catch (IOException e) {
            System.err.println("Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FunctionalInterface
    public interface ControllerInit<T> { void init(T controller); }

    /**
     * Loads FXML into contentArea WITH controller initialization.
     */
    private <T> void loadIntoContentWithInit(String fxmlPath, Class<T> ctrlClass, ControllerInit<T> init) {
        try {
            URL fxmlUrl = getClass().getResource(fxmlPath);
            if (fxmlUrl == null) { System.err.println("FXML non trouvé: " + fxmlPath); return; }
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent loaded = loader.load();
            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            if (controller != null) init.init(controller);
            injectIntoContentArea(loaded);
        } catch (IOException e) {
            System.err.println("Erreur chargement: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Extracts center+top from BorderPane and injects into contentArea.
     */
    private void injectIntoContentArea(Parent loaded) {
        if (loaded instanceof BorderPane bp) {
            VBox wrapper = new VBox();
            wrapper.setStyle("-fx-background-color: #f8fafc;");
            bp.setLeft(null);
            if (bp.getTop() != null) {
                javafx.scene.Node topNode = bp.getTop();
                bp.setTop(null);
                wrapper.getChildren().add(topNode);
            }
            if (bp.getCenter() != null) {
                javafx.scene.Node centerNode = bp.getCenter();
                bp.setCenter(null);
                VBox.setVgrow(centerNode, Priority.ALWAYS);
                wrapper.getChildren().add(centerNode);
            }
            ScrollPane pageScroll = new ScrollPane(wrapper);
            pageScroll.setFitToWidth(true);
            pageScroll.setFitToHeight(true);
            pageScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            pageScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            pageScroll.setStyle("-fx-background-color: #f8fafc;");
            contentArea.getChildren().setAll(pageScroll);
        } else {
            contentArea.getChildren().setAll(loaded);
        }
    }

    /**
     * Full scene replacement (only for leaving admin: logout, front-office, profile).
     */
    private <T> void navigateFullScene(String fxmlPath, ActionEvent event, Class<T> ctrlClass, ControllerInit<T> init) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            @SuppressWarnings("unchecked")
            T controller = (T) loader.getController();
            init.init(controller);
            showScene(root, event);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showScene(Parent root, ActionEvent event) {
        Scene scene = new Scene(root);
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.show();
    }
}
