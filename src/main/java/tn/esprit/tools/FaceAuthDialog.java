package tn.esprit.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.friwi.jcefmaven.CefAppBuilder;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;
import tn.esprit.views.JcefBrowserHelper;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public final class FaceAuthDialog {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private FaceAuthDialog() {
    }

    /** Opens the Face ID window in 'Register' mode to capture and save a new user's face. */
    public static void openRegistration(Users user, Consumer<String> onSuccess, Consumer<String> onError) {
        openDialog("register", user != null ? user.getEmail() : null, payload -> {
            if (payload.getDescriptor() == null || payload.getDescriptor().isBlank()) {
                onError.accept("No face descriptor was captured.");
                return;
            }
            onSuccess.accept(payload.getDescriptor());
        }, onError);
    }

    /** Opens the Face ID window in 'Login' mode to authenticate an existing user. */
    public static void openLogin(BiConsumer<String, String> onSuccess, Consumer<String> onError) {
        openDialog("login", null, payload -> {
            if (payload.getEmail() == null || payload.getEmail().isBlank()) {
                onError.accept("Please enter your email address.");
                return;
            }
            if (payload.getDescriptor() == null || payload.getDescriptor().isBlank()) {
                onError.accept("No face descriptor was captured.");
                return;
            }
            onSuccess.accept(payload.getEmail(), payload.getDescriptor());
        }, onError);
    }

    /** Main logic to initialize JCEF, prepare files, and show the Camera window. */
    private static void openDialog(String mode, String email, Consumer<FaceAuthPayload> onSuccess, Consumer<String> onError) {
        // Inform the user if this is the first time (blocks slightly to show the alert)
        if (!JcefBrowserHelper.getInstance().isInitialized()) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Face ID Setup");
                alert.setHeaderText("Initializing Face ID Components");
                alert.setContentText("We are setting up secure browser components (approx. 200MB).\n" +
                        "This only happens once. The Face ID window will open automatically when ready.\n\n" +
                        "Please check your console for download progress.");
                alert.show();
            });
        }

        // Run in a background thread to avoid blocking the UI thread
        new Thread(() -> {
            try {
                // 1. Prepare assets (copies HTML and model files)
                Path assetsDirectory = FaceAuthAssets.prepareAssetsDirectory();
                Path htmlFile = assetsDirectory.resolve("face_auth.html");
                Path modelsDirectory = assetsDirectory.resolve("models");

                StringBuilder urlBuilder = new StringBuilder(htmlFile.toUri().toString());
                urlBuilder.append("?mode=").append(URLEncoder.encode(mode, StandardCharsets.UTF_8));
                // Use a relative path for models to avoid CORS/file:// issues in some environments
                urlBuilder.append("&models=").append(URLEncoder.encode("models/", StandardCharsets.UTF_8));
                if (email != null && !email.isBlank()) {
                    urlBuilder.append("&email=").append(URLEncoder.encode(email, StandardCharsets.UTF_8));
                }

                // 2. Initialize JCEF (blocks if downloading binaries)
                JcefBrowserHelper helper = JcefBrowserHelper.getInstance();
                helper.initialize();

                // 3. Open the frame on the Swing Event Dispatch Thread
                String url = urlBuilder.toString();
                SwingUtilities.invokeLater(() -> createAndShowFrame(helper, url, onSuccess, onError));
            } catch (Exception e) {
                // Report error back to JavaFX thread and print stack trace
                e.printStackTrace();
                Platform.runLater(() -> onError.accept(e.getMessage() != null ? e.getMessage() : "Unable to start face authentication."));
            }
        }, "face-auth-init-thread").start();
    }

    private static void createAndShowFrame(JcefBrowserHelper helper,
                                           String url,
                                           Consumer<FaceAuthPayload> onSuccess,
                                           Consumer<String> onError) {
        AtomicBoolean completed = new AtomicBoolean(false);

        CefBrowser browser = helper.createBrowser(url);
        CefMessageRouter router = CefMessageRouter.create();
        router.addHandler(new CefMessageRouterHandlerAdapter() {
            @Override
            public boolean onQuery(CefBrowser browser,
                                   org.cef.browser.CefFrame frame,
                                   long queryId,
                                   String request,
                                   boolean persistent,
                                   CefQueryCallback callback) {
                if (!request.startsWith("face-auth:")) {
                    return false;
                }

                String payloadJson = request.substring("face-auth:".length());
                System.out.println("📥 Received Face Auth Query: " + request);
                try {
                    FaceAuthPayload payload = MAPPER.readValue(payloadJson, FaceAuthPayload.class);
                    System.out.println("✅ Payload parsed. Mode: " + payload.getMode());
                    callback.success("ok");
                    if (completed.compareAndSet(false, true)) {
                        SwingUtilities.invokeLater(() -> {
                            java.awt.Window window = SwingUtilities.getWindowAncestor(browser.getUIComponent());
                            if (window != null) window.dispose();
                        });
                        onSuccess.accept(payload);
                    }
                } catch (Exception ex) {
                    callback.failure(1, ex.getMessage() != null ? ex.getMessage() : "Invalid face payload");
                    if (completed.compareAndSet(false, true)) {
                        SwingUtilities.invokeLater(() -> {
                            java.awt.Window window = SwingUtilities.getWindowAncestor(browser.getUIComponent());
                            if (window != null) window.dispose();
                        });
                        onError.accept(ex.getMessage() != null ? ex.getMessage() : "Invalid face payload");
                    }
                }
                return true;
            }
        }, true);
        helper.getCefClient().addMessageRouter(router);

        JFrame frame = new JFrame("Face Authentication");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(browser.getUIComponent(), BorderLayout.CENTER);
        frame.setSize(1200, 760);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                if (completed.compareAndSet(false, true)) {
                    onError.accept("Face authentication cancelled.");
                }
            }
        });
        frame.setVisible(true);
    }
}
