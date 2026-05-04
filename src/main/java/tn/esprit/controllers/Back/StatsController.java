package tn.esprit.controllers.Back;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import tn.esprit.services.game.GameService;
import tn.esprit.entities.game.Game;
import javafx.scene.Node;
// METTRE ces imports iText 5 à la place :
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.pdf.PdfPCell;
import java.io.FileOutputStream;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class StatsController {

    // ── KPI Labels ────────────────────────────────────────────────
    @FXML private Label kpiTotalGames;
    @FXML private Label kpiQcm;
    @FXML private Label kpiVf;
    @FXML private Label kpiLibre;

    // ── Progress Bars ─────────────────────────────────────────────
    @FXML private ProgressBar qcmProgress;
    @FXML private ProgressBar vfProgress;
    @FXML private ProgressBar libreProgress;

    // ── Charts ────────────────────────────────────────────────────
    @FXML private PieChart gamesPieChart;
    @FXML private BarChart<String, Number> scoreBarChart;
    @FXML private LineChart<String, Number> attemptLineChart;

    // ── Analyse Labels ────────────────────────────────────────────
    @FXML private Label perfAnalysisLabel;
    @FXML private Label engagementLabel;

    private final GameService gameService = new GameService();
    private Game bestGame, worstGame, mostAttempted, leastAttempted;
    private List<Game> games;

    @FXML
    public void initialize() {
        try {
            games = gameService.recuperer();

            // ── Comptages ─────────────────────────────────────────
            long total    = games.size();
            long qcmCount = games.stream()
                    .filter(g -> "qcm".equalsIgnoreCase(g.getType())).count();
            long vfCount  = games.stream()
                    .filter(g -> "vraie ou faux".equalsIgnoreCase(g.getType())).count();
            long libreCount = games.stream()
                    .filter(g -> "libre".equalsIgnoreCase(g.getType())).count();

            // ── KPI Cards ─────────────────────────────────────────
            kpiTotalGames.setText(String.valueOf(total));
            kpiQcm.setText(String.valueOf(qcmCount));
            kpiVf.setText(String.valueOf(vfCount));
            kpiLibre.setText(String.valueOf(libreCount));

            // ── Progress Bars (proportion sur total) ──────────────
            if (total > 0) {
                qcmProgress.setProgress((double) qcmCount / total);
                vfProgress.setProgress((double) vfCount / total);
                libreProgress.setProgress((double) libreCount / total);
            }

            // ── PieChart ──────────────────────────────────────────
            if (qcmCount > 0)
                gamesPieChart.getData().add(new PieChart.Data("QCM", qcmCount));
            if (vfCount > 0)
                gamesPieChart.getData().add(new PieChart.Data("Vrai/Faux", vfCount));
            if (libreCount > 0)
                gamesPieChart.getData().add(new PieChart.Data("Libres", libreCount));
            gamesPieChart.setTitle("Repartition");

            // ── BarChart + LineChart ───────────────────────────────
            XYChart.Series<String, Number> scoreSeries = new XYChart.Series<>();
            scoreSeries.setName("Score moyen");

            XYChart.Series<String, Number> attemptSeries = new XYChart.Series<>();
            attemptSeries.setName("Tentatives");

            for (Game game : games) {
                if (game.getAvgScore() == 0 && game.getAttemptNumber() == 0)
                    continue;

                if (game.getAvgScore() > 0) {
                    scoreSeries.getData().add(
                            new XYChart.Data<>(game.getTitre(), game.getAvgScore())
                    );
                    if (bestGame == null || game.getAvgScore() > bestGame.getAvgScore())
                        bestGame = game;
                    if (worstGame == null || game.getAvgScore() < worstGame.getAvgScore())
                        worstGame = game;
                }

                if (game.getAttemptNumber() > 0) {
                    attemptSeries.getData().add(
                            new XYChart.Data<>(game.getTitre(), game.getAttemptNumber())
                    );
                    if (mostAttempted == null ||
                            game.getAttemptNumber() > mostAttempted.getAttemptNumber())
                        mostAttempted = game;
                    if (leastAttempted == null ||
                            game.getAttemptNumber() < leastAttempted.getAttemptNumber())
                        leastAttempted = game;
                }
            }

            if (!scoreSeries.getData().isEmpty())
                scoreBarChart.getData().add(scoreSeries);

            if (!attemptSeries.getData().isEmpty())
                attemptLineChart.getData().add(attemptSeries);

            // ── Analyse intelligente ──────────────────────────────
            if (bestGame != null && worstGame != null) {
                perfAnalysisLabel.setText(
                        "Les etudiants reussissent bien le quiz \""
                                + bestGame.getTitre()
                                + "\" avec un score moyen de " + bestGame.getAvgScore() + ".\n\n"
                                + "En revanche, \"" + worstGame.getTitre()
                                + "\" affiche le score le plus faible ("
                                + worstGame.getAvgScore() + ").\n\n"
                                + "Recommandation : revoir les questions ou ajouter un corrige pour \""
                                + worstGame.getTitre() + "\"."
                );
            } else {
                perfAnalysisLabel.setText(
                        "Pas encore de donnees de scores disponibles."
                );
            }

            if (mostAttempted != null && leastAttempted != null) {
                engagementLabel.setText(
                        "Le quiz le plus tente est \""
                                + mostAttempted.getTitre() + "\" avec "
                                + mostAttempted.getAttemptNumber() + " tentatives.\n\n"
                                + "A l inverse, \"" + leastAttempted.getTitre()
                                + "\" n a ete tente que "
                                + leastAttempted.getAttemptNumber() + " fois.\n\n"
                                + "Recommandation : encourager les etudiants a essayer \""
                                + leastAttempted.getTitre() + "\"."
                );
            } else {
                engagementLabel.setText(
                        "Pas encore de donnees de tentatives disponibles."
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ── Export PDF Jeux ───────────────────────────────────────────
    @FXML
    private void exportGamesPdf(ActionEvent event) {
        try {
            String path = System.getProperty("user.home") + "/Desktop/stats_jeux.pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            // Titre
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            doc.add(new Paragraph("Statistiques des Jeux", titleFont));
            doc.add(new Paragraph(" "));

            // Tableau
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(100);

            // Header
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12,
                    BaseColor.WHITE);
            PdfPCell h1 = new PdfPCell(new Paragraph("Titre", headerFont));
            PdfPCell h2 = new PdfPCell(new Paragraph("Score moyen", headerFont));
            PdfPCell h3 = new PdfPCell(new Paragraph("Tentatives", headerFont));
            h1.setBackgroundColor(new BaseColor(15, 181, 169));
            h2.setBackgroundColor(new BaseColor(15, 181, 169));
            h3.setBackgroundColor(new BaseColor(15, 181, 169));
            table.addCell(h1);
            table.addCell(h2);
            table.addCell(h3);

            // Lignes
            for (Game g : games) {
                if (g.getAvgScore() == 0 && g.getAttemptNumber() == 0) continue;
                table.addCell(g.getTitre());
                table.addCell(String.valueOf(g.getAvgScore()));
                table.addCell(String.valueOf(g.getAttemptNumber()));
            }
            doc.add(table);
            doc.add(new Paragraph(" "));

            // Analyse
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            if (bestGame != null) {
                doc.add(new Paragraph("Analyse des performances :", boldFont));
                doc.add(new Paragraph(
                        "Meilleur jeu : " + bestGame.getTitre()
                                + " (score: " + bestGame.getAvgScore() + ")", normalFont));
                doc.add(new Paragraph(
                        "Moins bon jeu : " + worstGame.getTitre()
                                + " (score: " + worstGame.getAvgScore() + ")", normalFont));
                doc.add(new Paragraph(" "));
            }

            if (mostAttempted != null) {
                doc.add(new Paragraph("Analyse de l engagement :", boldFont));
                doc.add(new Paragraph(
                        "Plus tente : " + mostAttempted.getTitre()
                                + " (" + mostAttempted.getAttemptNumber() + " fois)", normalFont));
                doc.add(new Paragraph(
                        "Moins tente : " + leastAttempted.getTitre()
                                + " (" + leastAttempted.getAttemptNumber() + " fois)", normalFont));
            }

            doc.close();
            ouvrirPdf(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Export PDF Questions ──────────────────────────────────────
    @FXML
    private void exportQuestionsPdf(ActionEvent event) {
        try {
            String path = System.getProperty("user.home") + "/Desktop/stats_questions.pdf";
            Document doc = new Document();
            PdfWriter.getInstance(doc, new FileOutputStream(path));
            doc.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font boldFont  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 13);
            Font normalFont= FontFactory.getFont(FontFactory.HELVETICA, 12);

            doc.add(new Paragraph("Statistiques des Questions", titleFont));
            doc.add(new Paragraph(" "));

            long qcm   = games.stream()
                    .filter(g -> "qcm".equalsIgnoreCase(g.getType())).count();
            long vf    = games.stream()
                    .filter(g -> "vraie ou faux".equalsIgnoreCase(g.getType())).count();
            long libre = games.stream()
                    .filter(g -> "libre".equalsIgnoreCase(g.getType())).count();

            doc.add(new Paragraph("Repartition par type :", boldFont));
            doc.add(new Paragraph("QCM : " + qcm, normalFont));
            doc.add(new Paragraph("Vrai/Faux : " + vf, normalFont));
            doc.add(new Paragraph("Libres : " + libre, normalFont));

            doc.close();
            ouvrirPdf(path);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ── Ouvre le PDF directement ──────────────────────────────────
    private void ouvrirPdf(String path) {
        try {
            File file = new File(path);
            if (Desktop.isDesktopSupported() && file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    private void handleRetour(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource("/tn/esprit/view/back_GameList.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}