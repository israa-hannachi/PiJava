package tn.esprit.utils;

import javafx.scene.Node;
import javafx.stage.Stage;

/**
 * Utilitaire pour uniformiser la taille des fenêtres dans toute l'application.
 */
public final class StageUtils {

    private StageUtils() {
        // utilitaire
    }

    /**
     * Applique une taille optimale et maximise la fenêtre pour afficher
     * tout le contenu sans coupure.
     */
    public static void applyUniformSize(Stage stage) {
        if (stage == null) return;
        // Maximiser pour utiliser tout l'écran
        stage.setMaximized(true);
        stage.setResizable(true);
    }

    /**
     * Applique la taille uniforme à partir d'un noeud UI.
     */
    public static void applyUniformSizeFromNode(Node source) {
        if (source == null) return;
        Object window = source.getScene().getWindow();
        if (window instanceof Stage) {
            applyUniformSize((Stage) window);
        }
    }
}
