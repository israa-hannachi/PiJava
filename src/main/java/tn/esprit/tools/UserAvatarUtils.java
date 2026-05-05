package tn.esprit.tools;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import tn.esprit.entities.users.Users;

import java.io.File;
import java.net.URL;

public final class UserAvatarUtils {

    private UserAvatarUtils() {}

    public static void applyAvatarToButton(Button button, Users user, Class<?> contextClass) {
        if (button == null || user == null) {
            return;
        }

        Image profile = resolveUserImage(user.getProfilePicture(), contextClass);
        if (profile == null) {
            String fallback = user.getFirstName() != null && !user.getFirstName().isBlank()
                    ? user.getFirstName()
                    : "Profile";
            button.setText(fallback);
            button.setGraphic(null);
            return;
        }

        ImageView avatar = new ImageView(profile);
        avatar.setFitWidth(34);
        avatar.setFitHeight(34);
        avatar.setPreserveRatio(false);
        avatar.setClip(new Circle(17, 17, 17));

        button.setText("");
        button.setGraphic(avatar);
        button.setStyle("-fx-background-color: transparent; -fx-background-radius: 999; -fx-padding: 2;");
    }

    public static Image resolveUserImage(String storedPath, Class<?> contextClass) {
        if (storedPath == null || storedPath.isBlank()) {
            return null;
        }

        String path = storedPath.trim();

        try {
            if (path.startsWith("http://") || path.startsWith("https://") || path.startsWith("file:/")) {
                return new Image(path, true);
            }

            File file = new File(path);
            if (file.isAbsolute() && file.exists()) {
                return new Image(file.toURI().toString(), true);
            }

            URL asResource = contextClass.getResource(path.startsWith("/") ? path : "/assets/images/" + path);
            if (asResource != null) {
                return new Image(asResource.toExternalForm(), true);
            }

            if (file.exists()) {
                return new Image(file.toURI().toString(), true);
            }
        } catch (Exception ignored) {
            return null;
        }

        return null;
    }
}
