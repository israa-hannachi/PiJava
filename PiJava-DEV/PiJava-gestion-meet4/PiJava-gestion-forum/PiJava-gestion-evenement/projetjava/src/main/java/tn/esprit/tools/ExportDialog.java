package tn.esprit.tools;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ExportDialog extends Dialog<String> {

    private TextField fileNameField;

    public ExportDialog() {
        setTitle("Exportation du Rapport");
        initModality(Modality.APPLICATION_MODAL);
        initStyle(StageStyle.UNDECORATED); 

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-radius: 15; -fx-border-color: #e2e8f0; -fx-border-width: 1;");
        root.setPrefWidth(550);

        // --- HEADER ---
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(20, 25, 20, 25));
        header.setStyle("-fx-background-color: linear-gradient(to right, #1e293b, #334155); -fx-background-radius: 15 15 0 0;");

        Circle iconCircle = new Circle(18, Color.web("#3b82f6"));
        Label iconLabel = new Label("📊");
        iconLabel.setStyle("-fx-font-size: 18; -fx-text-fill: white;");
        StackPane iconPane = new StackPane(iconCircle, iconLabel);

        VBox titleBox = new VBox(2);
        Label titleLabel = new Label("Exportation du Rapport");
        titleLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18; -fx-font-weight: bold;");
        Label subTitleLabel = new Label("Nommez votre fichier et choisissez un format");
        subTitleLabel.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12;");
        titleBox.getChildren().addAll(titleLabel, subTitleLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #94a3b8; -fx-font-size: 16; -fx-font-weight: bold; -fx-cursor: hand;");
        closeBtn.setOnAction(e -> {
            setResult(null);
            hide();
        });
        getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        getDialogPane().lookupButton(ButtonType.CANCEL).setVisible(false);


        header.getChildren().addAll(iconPane, titleBox, spacer, closeBtn);

        // --- CONTENT ---
        VBox content = new VBox(25);
        content.setPadding(new Insets(30, 25, 30, 25));
        content.setAlignment(Pos.CENTER);

        VBox inputContainer = new VBox(10);
        inputContainer.setAlignment(Pos.CENTER_LEFT);
        Label nameLabel = new Label("Nom du fichier :");
        nameLabel.setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-font-size: 14;");
        
        fileNameField = new TextField("Rapport_Stats_Forums");
        fileNameField.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-background-radius: 8; -fx-padding: 12; -fx-font-size: 14;");
        fileNameField.setPromptText("Saisissez le nom ici...");
        inputContainer.getChildren().addAll(nameLabel, fileNameField);

        HBox buttonBox = new HBox(12);
        buttonBox.setAlignment(Pos.CENTER);

        Button btnPdf = createStyledButton("📄 PDF", "#ef4444", "#dc2626");
        Button btnWord = createStyledButton("📝 Word", "#3b82f6", "#2563eb");
        Button btnExcel = createStyledButton("📊 Excel", "#10b981", "#059669");
        Button btnCsv = createStyledButton("📁 CSV", "#f59e0b", "#d97706");

        btnPdf.setOnAction(e -> handleAction("PDF"));
        btnWord.setOnAction(e -> handleAction("WORD"));
        btnExcel.setOnAction(e -> handleAction("EXCEL"));
        btnCsv.setOnAction(e -> handleAction("CSV"));

        buttonBox.getChildren().addAll(btnPdf, btnWord, btnExcel, btnCsv);
        
        Label hintLabel = new Label("Le bilan inclura une analyse détaillée des statistiques et de l'engagement.");
        hintLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12; -fx-font-style: italic;");

        content.getChildren().addAll(inputContainer, buttonBox, hintLabel);

        root.getChildren().addAll(header, content);
        getDialogPane().setContent(root);
        getDialogPane().setStyle("-fx-background-color: transparent;");
        
        final Delta dragDelta = new Delta();
        header.setOnMousePressed(mouseEvent -> {
            if (!(mouseEvent.getTarget() instanceof Button)) {
                dragDelta.x = getX() - mouseEvent.getScreenX();
                dragDelta.y = getY() - mouseEvent.getScreenY();
            }
        });
        header.setOnMouseDragged(mouseEvent -> {
            if (!(mouseEvent.getTarget() instanceof Button)) {
                setX(mouseEvent.getScreenX() + dragDelta.x);
                setY(mouseEvent.getScreenY() + dragDelta.y);
            }
        });
    }

    private Button createStyledButton(String text, String color1, String color2) {
        Button btn = new Button(text);
        btn.setPrefWidth(110);
        btn.setPrefHeight(45);
        btn.setCursor(javafx.scene.Cursor.HAND);
        
        String style = String.format(
            "-fx-background-color: linear-gradient(to bottom, %s, %s); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 13; " +
            "-fx-background-radius: 10; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0, 0, 3);",
            color1, color2
        );
        btn.setStyle(style);

        btn.setOnMouseEntered(e -> btn.setStyle(style + "-fx-scale-x: 1.05; -fx-scale-y: 1.05;"));
        btn.setOnMouseExited(e -> btn.setStyle(style + "-fx-scale-x: 1.0; -fx-scale-y: 1.0;"));

        return btn;
    }

    private void handleAction(String format) {
        String name = fileNameField.getText().trim();
        if (name.isEmpty()) name = "Rapport_Forum";
        setResult(format + ":" + name);
        hide();
    }

    private static class Delta { double x, y; }
}
