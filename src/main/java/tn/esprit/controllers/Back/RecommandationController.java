package tn.esprit.controllers.Back;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import tn.esprit.services.game.QuizApiService;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RecommandationController {

    @FXML private ComboBox<String>  categoryCombo;
    @FXML private ComboBox<String>  typeCombo;
    @FXML private ComboBox<Integer> amountCombo;
    @FXML private Label statusLabel;
    @FXML private Label kpiTotal;
    @FXML private Label kpiMatiere;
    @FXML private Label kpiType;
    @FXML private Label kpiDifficulte;
    @FXML
    private TextArea notesArea;

    @FXML private TableView<Map<String, String>>         questionsTable;
    @FXML private TableColumn<Map<String, String>, String> colNum;
    @FXML private TableColumn<Map<String, String>, String> colQuestion;
    @FXML private TableColumn<Map<String, String>, String> colAnswers;
    @FXML private TableColumn<Map<String, String>, String> colDifficulty;

    private final QuizApiService quizApiService = new QuizApiService();
    private List<Map<String, String>> currentQuestions = new ArrayList<>();

    @FXML
    public void initialize() {
        // ComboBox Matiere
        categoryCombo.setItems(FXCollections.observableArrayList(
                new ArrayList<>(QuizApiService.CATEGORY_MAP.keySet())
        ));
        categoryCombo.getSelectionModel().selectFirst();

        // ComboBox Type
        typeCombo.setItems(FXCollections.observableArrayList(
                "Choix multiples", "Vrai / Faux"
        ));
        typeCombo.getSelectionModel().selectFirst();

        // ComboBox Nombre
        amountCombo.setItems(FXCollections.observableArrayList(5, 10, 15, 20));
        amountCombo.getSelectionModel().select(1);

        // Colonnes
        colNum.setCellValueFactory(data -> {
            int index = questionsTable.getItems().indexOf(data.getValue()) + 1;
            return new SimpleStringProperty(String.valueOf(index));
        });
        colQuestion.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get("question")));
        colAnswers.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get("answers")));
        colDifficulty.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().get("difficulty")));

        // Wrap text Question
        colQuestion.setCellFactory(col -> new TableCell<>() {
            private final Label label = new Label();
            { label.setWrapText(true); setGraphic(label); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                label.setText(empty || item == null ? "" : item);
            }
        });

        // Wrap text Reponses
        colAnswers.setCellFactory(col -> new TableCell<>() {
            private final Label label = new Label();
            { label.setWrapText(true); setGraphic(label); }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                label.setText(empty || item == null ? "" : item);
            }

        });

        // Charger notes sauvegardées
        String savedNotes = java.util.prefs.Preferences.userNodeForPackage(RecommandationController.class)
                .get("quizNotes", "");
        notesArea.setText(savedNotes);

        // Sauvegarder automatiquement
        notesArea.textProperty().addListener((obs, oldVal, newVal) -> {
            java.util.prefs.Preferences.userNodeForPackage(RecommandationController.class)
                    .put("quizNotes", newVal);
        });

    }

    @FXML
    private void handleSearch(ActionEvent event) {
        String selected   = categoryCombo.getValue();
        String typeStr    = typeCombo.getValue();
        int amount        = amountCombo.getValue();
        int categoryId    = QuizApiService.CATEGORY_MAP.getOrDefault(selected, 9);
        String difficulty = QuizApiService.DIFFICULTY_MAP.getOrDefault(selected, "easy");
        String type       = typeStr.equals("Vrai / Faux") ? "boolean" : "multiple";

        statusLabel.setText("Chargement en cours...");
        questionsTable.setItems(FXCollections.observableArrayList());
        currentQuestions.clear();

        new Thread(() -> {
            try {
                List<Map<String, String>> questions =
                        quizApiService.fetchQuestions(categoryId, difficulty, type, amount);

                Platform.runLater(() -> {
                    currentQuestions = questions;
                    questionsTable.setItems(
                            FXCollections.observableArrayList(questions));
                    kpiTotal.setText(String.valueOf(questions.size()));
                    kpiMatiere.setText(selected);
                    kpiType.setText(typeStr);
                    kpiDifficulte.setText(difficulty.toUpperCase());
                    statusLabel.setText(questions.size()
                            + " questions chargees avec succes.");
                });

            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText(
                        "Erreur : verifiez votre connexion internet."));
                e.printStackTrace();
            }
        }).start();
    }

    @FXML
    private void exportPdf(ActionEvent event) {
        if (currentQuestions.isEmpty()) {
            statusLabel.setText("Aucune question a exporter.");
            return;
        }
        try {
            String path = System.getProperty("user.home")
                    + "/Desktop/recommandation_quiz.pdf";
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // Titre
            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 18);
            doc.add(new Paragraph("Recommandation de Quiz", titleFont));
            doc.add(new Paragraph(" "));

            // Infos
            Font infoFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            doc.add(new Paragraph("Matiere : "
                    + categoryCombo.getValue(), infoFont));
            doc.add(new Paragraph("Type : "
                    + typeCombo.getValue(), infoFont));
            doc.add(new Paragraph("Nombre : "
                    + currentQuestions.size() + " questions", infoFont));
            doc.add(new Paragraph(" "));

            // Tableau
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{0.5f, 3f, 2f});

            Font headerFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD, 11, BaseColor.WHITE);
            BaseColor headerColor = new BaseColor(15, 181, 169);

            for (String h : new String[]{"#", "Question", "Reponses"}) {
                PdfPCell cell = new PdfPCell(new Phrase(h, headerFont));
                cell.setBackgroundColor(headerColor);
                cell.setPadding(8);
                table.addCell(cell);
            }

            Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            int num = 1;
            for (Map<String, String> q : currentQuestions) {
                table.addCell(new PdfPCell(
                        new Phrase(String.valueOf(num++), cellFont)));
                PdfPCell qCell = new PdfPCell(
                        new Phrase(q.get("question"), cellFont));
                qCell.setPadding(6);
                table.addCell(qCell);
                PdfPCell aCell = new PdfPCell(
                        new Phrase(q.get("answers"), cellFont));
                aCell.setPadding(6);
                table.addCell(aCell);
            }
            doc.add(table);
            doc.close();

            // Ouvre directement
            File pdfFile = new File(path);
            if (Desktop.isDesktopSupported() && pdfFile.exists())
                Desktop.getDesktop().open(pdfFile);

            statusLabel.setText("PDF exporte et ouvert !");

        } catch (Exception e) {
            e.printStackTrace();
            statusLabel.setText("Erreur lors de l export PDF.");
        }
    }

    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/tn/esprit/view/back_GameList.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}