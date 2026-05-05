package tn.esprit.controllers.front;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.esprit.entities.users.Users;
import tn.esprit.tools.NotificationCenter;
import tn.esprit.tools.NotificationCenter.NotificationItem;
import tn.esprit.tools.UserAvatarUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FrontUserDashboardController {

    @FXML private Label welcomeLabel;
    @FXML private Button profileButton;
    @FXML private Button adminButton;
    @FXML private Button notificationsButton;
    @FXML private Label notificationBadge;

    private Users currentUser;
    private List<NotificationItem> notifications;
    private final Set<String> knownNotificationKeys = new HashSet<>();
    private ScheduledExecutorService notificationScheduler;

    public void initUser(Users user) {
        this.currentUser = user;
        if (user != null) {
            welcomeLabel.setText("Bienvenue sur Naja7ni, " + user.getFirstName() + " 👋");
            UserAvatarUtils.applyAvatarToButton(profileButton, user, getClass());

            try {
                refreshNotifications(false);
                startNotificationPolling();
            } catch (Exception notificationError) {
                System.err.println("Notification initialization failed: " + notificationError.getMessage());
            }

            boolean isAdmin = "ADMIN".equals(user.getRole());
            adminButton.setVisible(isAdmin);
            adminButton.setManaged(isAdmin);
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        stopNotificationPolling();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/front_login.fxml"));
            Scene scene = new Scene(root);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBackOffice(ActionEvent event) {
        stopNotificationPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_admin.fxml"));
            Parent root = loader.load();
            AdminDashboardController controller = loader.getController();
            controller.initAdmin(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setMaximized(true);
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        stopNotificationPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_profile.fxml"));
            Parent root = loader.load();
            FrontProfileController controller = loader.getController();
            controller.initUser(currentUser);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleNotifications(ActionEvent event) {
        refreshNotifications(false);
        if (notifications != null) {
            NotificationCenter.showPopup(notificationsButton, notifications);
        }
    }

    private void refreshNotificationBadge() {
        if (notificationBadge == null || notifications == null) {
            return;
        }

        int count = notifications.size();
        notificationBadge.setText(String.valueOf(count));
        notificationBadge.setVisible(count > 0);
        notificationBadge.setManaged(count > 0);
    }

    private void refreshNotifications(boolean showToastForNewItems) {
        if (currentUser == null) {
            return;
        }

        List<NotificationItem> fetched;
        try {
            fetched = NotificationCenter.collectNotifications(currentUser);
        } catch (Exception e) {
            System.err.println("Notification refresh failed: " + e.getMessage());
            return;
        }

        Set<String> freshKeys = new HashSet<>();
        for (NotificationItem item : fetched) {
            freshKeys.add(item.getKey());
        }

        if (showToastForNewItems && notificationsButton != null && notificationsButton.getScene() != null) {
            for (NotificationItem item : fetched) {
                if (!knownNotificationKeys.contains(item.getKey())) {
                    Window window = notificationsButton.getScene().getWindow();
                    NotificationCenter.showToast(window, item);
                }
            }
        }

        notifications = fetched;
        knownNotificationKeys.clear();
        knownNotificationKeys.addAll(freshKeys);
        refreshNotificationBadge();
    }

    private void startNotificationPolling() {
        if (notificationScheduler != null && !notificationScheduler.isShutdown()) {
            return;
        }

        notificationScheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "dashboard-notification-poller");
            thread.setDaemon(true);
            return thread;
        });

        notificationScheduler.scheduleAtFixedRate(
                () -> Platform.runLater(() -> refreshNotifications(true)),
                30,
                45,
                TimeUnit.SECONDS
        );
    }

    private void stopNotificationPolling() {
        if (notificationScheduler != null) {
            notificationScheduler.shutdownNow();
            notificationScheduler = null;
        }
    }

    @FXML
    public void handleGameList(ActionEvent event) {
        stopNotificationPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_GameList.fxml"));
            Parent root = loader.load();

            FrontGameListController controller = loader.getController();
            controller.initUser(currentUser);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleCours() {
        stopNotificationPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_CoursCategories.fxml"));
            Parent root = loader.load();
            FrontCoursCategorieController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleEvents() {
        stopNotificationPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/frontEvent.fxml"));
            Parent root = loader.load();
            EventFrontController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleForums() {
        stopNotificationPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_forum.fxml"));
            Parent root = loader.load();
            FrontForumController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleMeets() {
        stopNotificationPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_MeetList.fxml"));
            Parent root = loader.load();
            FrontMeetListController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleGameList() {
        stopNotificationPolling();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_GameList.fxml"));
            Parent root = loader.load();
            FrontGameListController ctrl = loader.getController();
            ctrl.initUser(currentUser);
            Stage stage = (Stage) welcomeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
