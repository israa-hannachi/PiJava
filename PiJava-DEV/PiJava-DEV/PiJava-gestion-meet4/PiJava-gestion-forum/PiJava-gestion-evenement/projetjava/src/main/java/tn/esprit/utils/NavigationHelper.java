package tn.esprit.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import tn.esprit.entities.users.Users;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * Helper pour naviguer entre les pages tout en appliquant une taille uniforme à la fenêtre.
 */
public final class NavigationHelper {

    private NavigationHelper() {}

    /**
     * Charge un FXML, injecte un utilisateur si le controller le supporte, applique la taille uniforme et change de scène.
     *
     * @param fxmlPath chemin du FXML depuis /tn/esprit/view/
     * @param source    n'ud UI pour récupérer le Stage actuel (ex: button)
     * @param currentUser utilisateur à injecter (peut être null)
     */
    public static void navigateTo(String fxmlPath, Node source, Users currentUser) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource(fxmlPath));
            Parent root = loader.load();
            Object ctrl = loader.getController();

            // Injection utilisateur si supporté
            if (currentUser != null && ctrl != null) {
                if (ctrl instanceof tn.esprit.controllers.front.FrontUserDashboardController) {
                    ((tn.esprit.controllers.front.FrontUserDashboardController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontProfileController) {
                    ((tn.esprit.controllers.front.FrontProfileController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontCoursCategorieController) {
                    ((tn.esprit.controllers.front.FrontCoursCategorieController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontMeetListController) {
                    ((tn.esprit.controllers.front.FrontMeetListController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontMeetCalendarController) {
                    ((tn.esprit.controllers.front.FrontMeetCalendarController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontGameListController) {
                    ((tn.esprit.controllers.front.FrontGameListController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.EventFrontController) {
                    ((tn.esprit.controllers.front.EventFrontController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontForumController) {
                    ((tn.esprit.controllers.front.FrontForumController) ctrl).initUser(currentUser);
                }
            }

            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            StageUtils.applyUniformSize(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Surcharge sans utilisateur.
     */
    public static void navigateTo(String fxmlPath, Node source) {
        navigateTo(fxmlPath, source, null);
    }

    /**
     * Surcharge avec callback pour injecter des données spécifiques (ex: initData).
     */
    public static void navigateTo(String fxmlPath, Node source, Users currentUser, Consumer<Object> postLoad) {
        try {
            FXMLLoader loader = new FXMLLoader(NavigationHelper.class.getResource(fxmlPath));
            Parent root = loader.load();
            Object ctrl = loader.getController();

            // Injection utilisateur si supporté
            if (currentUser != null && ctrl != null) {
                if (ctrl instanceof tn.esprit.controllers.front.FrontUserDashboardController) {
                    ((tn.esprit.controllers.front.FrontUserDashboardController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontProfileController) {
                    ((tn.esprit.controllers.front.FrontProfileController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontCoursCategorieController) {
                    ((tn.esprit.controllers.front.FrontCoursCategorieController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontMeetListController) {
                    ((tn.esprit.controllers.front.FrontMeetListController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontMeetCalendarController) {
                    ((tn.esprit.controllers.front.FrontMeetCalendarController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontGameListController) {
                    ((tn.esprit.controllers.front.FrontGameListController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.EventFrontController) {
                    ((tn.esprit.controllers.front.EventFrontController) ctrl).initUser(currentUser);
                } else if (ctrl instanceof tn.esprit.controllers.front.FrontForumController) {
                    ((tn.esprit.controllers.front.FrontForumController) ctrl).initUser(currentUser);
                }
            }

            // Callback post-chargement
            if (postLoad != null) {
                postLoad.accept(ctrl);
            }

            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));
            StageUtils.applyUniformSize(stage);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
