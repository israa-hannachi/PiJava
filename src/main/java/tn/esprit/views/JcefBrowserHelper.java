package tn.esprit.views;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import java.io.File;

/**
 * Singleton helper that initializes JCEF (Java Chromium Embedded Framework).
 * Provides a full Chromium engine with WebRTC support for embedding Jitsi Meet
 * directly inside the JavaFX application.
 */
public class JcefBrowserHelper {

    private static JcefBrowserHelper instance;
    private CefApp cefApp;
    private CefClient cefClient;
    private boolean initialized = false;

    private JcefBrowserHelper() {}

    public static synchronized JcefBrowserHelper getInstance() {
        if (instance == null) {
            instance = new JcefBrowserHelper();
        }
        return instance;
    }

    /**
     * Initialize JCEF. Must be called once before creating browsers.
     * This downloads Chromium binaries on first run (~200MB).
     */
    public synchronized void initialize() throws Exception {
        if (initialized) return;

        // Use a persistent directory for JCEF binaries
        File installDir = new File(System.getProperty("user.home"), ".jcef-bundle");
        if (!installDir.exists()) installDir.mkdirs();

        CefAppBuilder builder = new CefAppBuilder();
        builder.setInstallDir(installDir);

        // Add progress handler to see download status in console
        builder.setProgressHandler(new ConsoleProgressHandler());

        // Configuration Settings
        CefSettings settings = builder.getCefSettings();
        settings.windowless_rendering_enabled = false; // Important for Swing/JavaFX embedding
        settings.persist_session_cookies = true;
        settings.user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Safari/537.36";

        // Essential Chromium arguments for WebRTC and Stability
        builder.addJcefArgs("--enable-media-stream");
        builder.addJcefArgs("--use-fake-ui-for-media-stream"); // Auto-allow camera/mic
        builder.addJcefArgs("--autoplay-policy=no-user-gesture-required");
        builder.addJcefArgs("--no-sandbox");
        builder.addJcefArgs("--disable-gpu-process-crash-limit");
        builder.addJcefArgs("--allow-file-access-from-files");
        builder.addJcefArgs("--disable-web-security");

        builder.setAppHandler(new MavenCefAppHandlerAdapter() {});

        // Build and initialize CefApp
        cefApp = builder.build();
        cefClient = cefApp.createClient();

        // Handle OAuth/Login popups by allowing them to open in a new window
        cefClient.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, org.cef.browser.CefFrame frame,
                                         String target_url, String target_frame_name) {
                return false; // Return false to allow the default popup behavior (essential for Google Login)
            }
        });

        initialized = true;
        System.out.println("✅ JCEF initialized successfully.");
    }

    /**
     * Create a new CefBrowser that can be embedded in a Swing panel.
     */
    public CefBrowser createBrowser(String url) {
        if (!initialized) {
            try {
                initialize();
            } catch (Exception e) {
                throw new RuntimeException("Failed to initialize JCEF", e);
            }
        }
        return cefClient.createBrowser(url, false, false);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public CefClient getCefClient() {
        return cefClient;
    }

    /**
     * Dispose resources on application exit.
     */
    public void dispose() {
        if (cefApp != null) {
            cefApp.dispose();
            cefApp = null;
            cefClient = null;
            initialized = false;
        }
    }
}
