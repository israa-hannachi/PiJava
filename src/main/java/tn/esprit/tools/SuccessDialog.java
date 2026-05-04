package tn.esprit.tools;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import java.awt.Desktop;
import java.io.File;

public class SuccessDialog extends Dialog<Void> {

    public SuccessDialog(String fileName, String filePath) {
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED);

        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(30));
        root.setPrefWidth(400);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-border-radius: 20; -fx-border-color: #f1f5f9; -fx-border-width: 2; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);");

        // --- ICON ---
        Circle circle = new Circle(35, Color.web("#ecfdf5"));
        Label checkIcon = new Label("✓");
        checkIcon.setStyle("-fx-font-size: 35; -fx-text-fill: #10b981; -fx-font-weight: bold;");
        StackPane iconPane = new StackPane(circle, checkIcon);
        iconPane.setStyle("-fx-effect: dropshadow(gaussian, rgba(16, 185, 129, 0.2), 20, 0, 0, 0);");

        // --- TEXT ---
        VBox textBox = new VBox(8);
        textBox.setAlignment(Pos.CENTER);
        
        Label title = new Label("Exportation Réussie !");
        title.setStyle("-fx-font-size: 20; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        Label desc = new Label("Votre rapport a été généré avec succès.");
        desc.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b;");
        
        Label nameLabel = new Label(fileName);
        nameLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #3b82f6; -fx-font-weight: bold; -fx-background-color: #eff6ff; -fx-padding: 5 15; -fx-background-radius: 20;");
        
        textBox.getChildren().addAll(title, desc, nameLabel);

        // --- BUTTONS ---
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnOpen = new Button("Ouvrir le fichier");
        btnOpen.setCursor(javafx.scene.Cursor.HAND);
        btnOpen.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10;");
        btnOpen.setOnAction(e -> {
            try {
                Desktop.getDesktop().open(new File(filePath));
                hide();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        Button btnClose = new Button("Fermer");
        btnClose.setCursor(javafx.scene.Cursor.HAND);
        btnClose.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 10;");
        btnClose.setOnAction(e -> hide());

        // Add a hidden cancel button type to allow the dialog to close properly
        getDialogPane().getButtonTypes().add(javafx.scene.control.ButtonType.CANCEL);
        getDialogPane().lookupButton(javafx.scene.control.ButtonType.CANCEL).setVisible(false);

        buttonBox.getChildren().addAll(btnClose, btnOpen);

        root.getChildren().addAll(iconPane, textBox, buttonBox);
        getDialogPane().setContent(root);
        getDialogPane().setStyle("-fx-background-color: transparent;");
    }
}
