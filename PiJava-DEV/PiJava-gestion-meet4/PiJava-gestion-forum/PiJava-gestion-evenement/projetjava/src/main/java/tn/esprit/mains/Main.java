package tn.esprit.mains;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import tn.esprit.utils.StageUtils;
public class Main {

    public static class MainApp extends Application {
        @Override
        public void start(Stage primaryStage) {
            try {
                // Point d'entrée de l'application : Page de connexion
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_login.fxml"));
                Parent root = loader.load();
                Scene scene = new Scene(root);

                primaryStage.setTitle("Naja7ni - Apprentissage & Gaming");
                primaryStage.setScene(scene);
                StageUtils.applyUniformSize(primaryStage);
                primaryStage.show();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("❌ Erreur lors du chargement de l'interface graphique : " + e.getMessage());
            }
        }
    }

    public static void main(String[] args) {
        // Lancement de l'application JavaFX
        System.out.println("🚀 Lancement de Naja7ni...");
        Application.launch(MainApp.class, args);
    }
}
