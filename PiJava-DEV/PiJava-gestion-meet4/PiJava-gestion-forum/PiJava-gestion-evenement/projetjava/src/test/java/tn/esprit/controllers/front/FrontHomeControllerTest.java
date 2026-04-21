package tn.esprit.controllers.front;

import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import tn.esprit.entities.users.Users;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FrontHomeControllerTest {

    @BeforeAll
    static void initJavaFxToolkit() throws InterruptedException {
        try {
            Platform.startup(() -> { });
        } catch (IllegalStateException ignored) {
            // JavaFX toolkit is already initialized.
        }
    }

    @Test
    void initializeShouldLoadConfiguredImages() throws Exception {
        FrontHomeController controller = new FrontHomeController();

        ImageView brandLogo = new ImageView();
        ImageView heroImage = new ImageView();
        ImageView course1 = new ImageView();
        ImageView course2 = new ImageView();
        ImageView course3 = new ImageView();
        ImageView course4 = new ImageView();
        ImageView course5 = new ImageView();
        ImageView course6 = new ImageView();
        ImageView course7 = new ImageView();
        ImageView course8 = new ImageView();

        setPrivateField(controller, "brandLogo", brandLogo);
        setPrivateField(controller, "heroImage", heroImage);
        setPrivateField(controller, "course1", course1);
        setPrivateField(controller, "course2", course2);
        setPrivateField(controller, "course3", course3);
        setPrivateField(controller, "course4", course4);
        setPrivateField(controller, "course5", course5);
        setPrivateField(controller, "course6", course6);
        setPrivateField(controller, "course7", course7);
        setPrivateField(controller, "course8", course8);

        runOnFxThreadAndWait(controller::initialize);

        assertNotNull(brandLogo.getImage());
        assertNotNull(heroImage.getImage());
        assertNotNull(course1.getImage());
        assertNotNull(course2.getImage());
        assertNotNull(course3.getImage());
        assertNotNull(course4.getImage());
        assertNotNull(course5.getImage());
        assertNotNull(course6.getImage());
        assertNotNull(course7.getImage());
        assertNotNull(course8.getImage());
    }

    @Test
    void loadShouldTrackMissingResourcePath() throws Exception {
        FrontHomeController controller = new FrontHomeController();
        ImageView target = new ImageView();
        String missingPath = "/assets/images/not-found.png";

        invokePrivateLoad(controller, target, missingPath);

        List<String> missing = getMissingList(controller);
        assertTrue(missing.contains(missingPath));
        assertNull(target.getImage());
    }

    @Test
    void setLoggedInUserShouldUpdateUiElements() throws Exception {
        FrontHomeController controller = new FrontHomeController();

        Button signIn = new Button();
        Button signUp = new Button();
        HBox userMenu = new HBox();
        Label userNameLabel = new Label();

        setPrivateField(controller, "btnSignIn", signIn);
        setPrivateField(controller, "btnSignUp", signUp);
        setPrivateField(controller, "userMenu", userMenu);
        setPrivateField(controller, "userNameLabel", userNameLabel);

        Users user = new Users();
        user.setFirstName("Ali");
        user.setLastName("Ben Salah");

        runOnFxThreadAndWait(() -> controller.setLoggedInUser(user));

        assertFalse(signIn.isVisible());
        assertFalse(signIn.isManaged());
        assertFalse(signUp.isVisible());
        assertFalse(signUp.isManaged());
        assertTrue(userMenu.isVisible());
        assertTrue(userMenu.isManaged());
        assertEquals("👋 Welcome, Ali Ben Salah", userNameLabel.getText());
    }

    @Test
    void setLoggedInUserWithNullShouldNotChangeUiElements() throws Exception {
        FrontHomeController controller = new FrontHomeController();

        Button signIn = new Button();
        Button signUp = new Button();
        HBox userMenu = new HBox();
        Label userNameLabel = new Label("Initial");

        userMenu.setVisible(false);
        userMenu.setManaged(false);

        setPrivateField(controller, "btnSignIn", signIn);
        setPrivateField(controller, "btnSignUp", signUp);
        setPrivateField(controller, "userMenu", userMenu);
        setPrivateField(controller, "userNameLabel", userNameLabel);

        runOnFxThreadAndWait(() -> controller.setLoggedInUser(null));

        assertTrue(signIn.isVisible());
        assertTrue(signIn.isManaged());
        assertTrue(signUp.isVisible());
        assertTrue(signUp.isManaged());
        assertFalse(userMenu.isVisible());
        assertFalse(userMenu.isManaged());
        assertEquals("Initial", userNameLabel.getText());
    }

    private static void invokePrivateLoad(FrontHomeController controller, ImageView target, String resourcePath) throws Exception {
        Method loadMethod = FrontHomeController.class.getDeclaredMethod("load", ImageView.class, String.class);
        loadMethod.setAccessible(true);
        runOnFxThreadAndWait(() -> {
            try {
                loadMethod.invoke(controller, target, resourcePath);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private static List<String> getMissingList(FrontHomeController controller) throws Exception {
        Field missingField = FrontHomeController.class.getDeclaredField("missing");
        missingField.setAccessible(true);
        return (List<String>) missingField.get(controller);
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void runOnFxThreadAndWait(Runnable action) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            try {
                action.run();
            } finally {
                latch.countDown();
            }
        });
        boolean finished = latch.await(10, TimeUnit.SECONDS);
        assertTrue(finished, "JavaFX action timed out");
    }
}
