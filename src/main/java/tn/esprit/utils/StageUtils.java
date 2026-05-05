package tn.esprit.utils;

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Utilitaire pour uniformiser la taille des fenêtres dans toute l'application.
 */
public final class StageUtils {

    // Taille uniforme pour toutes les fenêtres
    private static final double DEFAULT_WIDTH = 1200.0;
    private static final double DEFAULT_HEIGHT = 880.0;

    private StageUtils() {
        // utilitaire
    }

    /**
     * Applique une taille fixe au stage et le rend non maximisé.
     * À appeler juste après stage.setScene(scene) et avant stage.show().
     */
    public static void applyUniformSize(Stage stage) {
        if (stage == null) return;
        // Si la fenêtre est maximisée, forcer la sortie du mode maximisé
        if (stage.isMaximized()) {
            stage.setMaximized(false);
        }
        // Appliquer la taille
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        // Centrer la fenêtre pour garantir un affichage correct
        stage.centerOnScreen();
        stage.setResizable(true); // permet le redimensionnement manuel si souhaité
    }

    /**
     * Applique la taille uniforme à partir d'un n'ud UI (ex: button.getScene().getWindow()).
     */
    public static void applyUniformSizeFromNode(Node source) {
        if (source == null) return;
        Object window = source.getScene().getWindow();
        if (window instanceof Stage) {
            applyUniformSize((Stage) window);
        }
    }
}
