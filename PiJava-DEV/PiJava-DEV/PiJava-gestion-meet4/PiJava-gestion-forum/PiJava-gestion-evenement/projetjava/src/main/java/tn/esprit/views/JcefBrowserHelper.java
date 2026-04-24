package tn.esprit.views;

import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.handler.CefLifeSpanHandlerAdapter;

import java.io.File;
import java.io.IOException;

/**
 * Singleton helper that initializes JCEF (Java Chromium Embedded Framework).
 * Provides a full Chromium engine with WebRTC support for embedding Jitsi Meet
 * directly inside the JavaFX application.
 *
 * On first run, jcefmaven auto-downloads Chromium binaries (~200MB) to a local cache.
 * Subsequent runs use the cached binaries.
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
     * This downloads Chromium binaries on first run.
     */
    public synchronized void initialize() throws Exception {
        if (initialized) return;

        File installDir = new File(System.getProperty("user.home"), ".jcef-bundle");
        File cacheDir = new File(installDir, "cache");
        if (!cacheDir.exists()) cacheDir.mkdirs();

        CefAppBuilder builder = new CefAppBuilder();
        builder.setInstallDir(installDir);
        
        // Settings
        CefSettings settings = builder.getCefSettings();
        settings.windowless_rendering_enabled = false;
        settings.cache_path = cacheDir.getAbsolutePath(); // Persist login/cookies
        settings.persist_session_cookies = true;
        settings.user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";

        // Enable essential features
        builder.addJcefArgs("--enable-media-stream");
        builder.addJcefArgs("--autoplay-policy=no-user-gesture-required");
        builder.addJcefArgs("--enable-begin-frame-scheduling");
        builder.addJcefArgs("--no-sandbox"); // Stability on some systems
        builder.addJcefArgs("--disable-gpu-process-crash-limit");

        cefApp = builder.build();
        cefClient = cefApp.createClient();

        // Improved Popup Handling for Login (OAuth)
        cefClient.addLifeSpanHandler(new CefLifeSpanHandlerAdapter() {
            @Override
            public boolean onBeforePopup(CefBrowser browser, org.cef.browser.CefFrame frame,
                                         String target_url, String target_frame_name) {
                // If it's a login popup (Google, GitHub, etc.), let it open in a new window
                // This is required for OAuth flows to work correctly.
                return false; // Return false to allow the default popup behavior
            }
        });

        initialized = true;
        System.out.println("✅ JCEF initialized with persistent cache");
    }

    /**
     * Create a new CefBrowser that can be embedded in a Swing panel.
     * Use SwingNode in JavaFX to display it.
     */
    public CefBrowser createBrowser(String url) {
        if (!initialized) {
            throw new IllegalStateException("JCEF not initialized. Call initialize() first.");
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
     * Dispose resources. Call when the application exits.
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
