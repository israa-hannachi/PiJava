package tn.esprit.controllers.front;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.game.Game;
import tn.esprit.entities.game.Game_Question;
import tn.esprit.services.game.GameQuestionService;
import tn.esprit.services.game.GameService;
import tn.esprit.services.game.HuggingFaceService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FrontGamePlayController {

    // ── Labels ────────────────────────────────────────────────────
    @FXML private Label gameTitleLabel;
    @FXML private Label timerLabel;
    @FXML private Label lastScoreLabel;
    @FXML private Label avgScoreLabel;
    @FXML private Label attemptLabel;
    @FXML private Label badgeLabel;
    @FXML private Label feedbackLabel;
    @FXML private Label scoreDetailLabel;

    // ── Progress + Container ──────────────────────────────────────
    @FXML private ProgressBar progressBar;
    @FXML private VBox questionsContainer;

    // ── Data ──────────────────────────────────────────────────────
    private Game currentGame;
    private List<Game_Question> questions;
    private int timeLeft;
    private Timer timer;

    // ── Services ──────────────────────────────────────────────────
    private final GameService gameService
            = new GameService();
    private final GameQuestionService questionService
            = new GameQuestionService();
    private final HuggingFaceService hfService
            = new HuggingFaceService();

    // ─────────────────────────────────────────────────────────────
    // INIT
    // ─────────────────────────────────────────────────────────────
    public void initGame(Game game) {
        this.currentGame = game;
        gameTitleLabel.setText("Jouer : " + game.getTitre());
        lastScoreLabel.setText(String.valueOf(game.getLastScore()));
        avgScoreLabel.setText(String.valueOf(game.getAvgScore()));
        attemptLabel.setText(String.valueOf(game.getAttemptNumber()));

        if (feedbackLabel != null)   feedbackLabel.setText("");
        if (scoreDetailLabel != null) scoreDetailLabel.setText("");

        timeLeft = game.getDuration();
        startTimer();
        loadQuestions();
    }

    // ─────────────────────────────────────────────────────────────
    // TIMER
    // ─────────────────────────────────────────────────────────────
    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    if (timeLeft <= 0) {
                        timer.cancel();
                        timerLabel.setText("Temps ecoule !");
                        timerLabel.setStyle(
                                "-fx-text-fill:#dc2626; -fx-font-weight:bold;");
                        disableInputs();
                    } else {
                        timerLabel.setText(timeLeft + " secondes");
                        if (timeLeft <= 10) {
                            timerLabel.setStyle(
                                    "-fx-text-fill:#dc2626; -fx-font-weight:bold;");
                        }
                        timeLeft--;
                    }
                });
            }
        }, 0, 1000);
    }

    private void disableInputs() {
        questionsContainer.getChildren()
                .forEach(n -> n.setDisable(true));
    }

    // ─────────────────────────────────────────────────────────────
    // CHARGEMENT DES QUESTIONS
    // ─────────────────────────────────────────────────────────────
    private void loadQuestions() {
        try {
            questions = questionService
                    .recupererParGame(currentGame.getId());
            questionsContainer.getChildren().clear();

            for (Game_Question q : questions) {
                VBox questionBox = new VBox(8);
                questionBox.setStyle(
                        "-fx-background-color:white;" +
                                "-fx-background-radius:10;" +
                                "-fx-padding:16;" +
                                "-fx-border-color:#e2e8f0;" +
                                "-fx-border-radius:10;" +
                                "-fx-effect:dropshadow(gaussian," +
                                "rgba(0,0,0,0.05),6,0,0,1);"
                );

                // Texte de la question
                Label qLabel = new Label(q.getQuestionText());
                qLabel.setStyle(
                        "-fx-font-size:15px;" +
                                "-fx-font-weight:700;" +
                                "-fx-text-fill:#1e293b;");
                qLabel.setWrapText(true);
                questionBox.getChildren().add(qLabel);

                // ── QCM ───────────────────────────────────────
                if ("qcm".equalsIgnoreCase(currentGame.getType())) {
                    ToggleGroup group = new ToggleGroup();
                    for (String opt : new String[]{
                            q.getOption1(), q.getOption2(),
                            q.getOption3(), q.getOption4()}) {
                        if (opt != null && !opt.isEmpty()) {
                            RadioButton rb = new RadioButton(opt);
                            rb.setToggleGroup(group);
                            rb.setStyle(
                                    "-fx-font-size:13px;" +
                                            "-fx-text-fill:#334155;");
                            questionBox.getChildren().add(rb);
                        }
                    }

                    // ── Vrai / Faux ───────────────────────────────
                } else if ("vraie ou faux"
                        .equalsIgnoreCase(currentGame.getType())) {
                    ToggleGroup group = new ToggleGroup();
                    for (String opt : new String[]{"Vraie", "Faux"}) {
                        RadioButton rb = new RadioButton(opt);
                        rb.setToggleGroup(group);
                        rb.setStyle(
                                "-fx-font-size:13px;" +
                                        "-fx-text-fill:#334155;");
                        questionBox.getChildren().add(rb);
                    }

                    // ── Libre — NLP ───────────────────────────────
                } else if ("libre"
                        .equalsIgnoreCase(currentGame.getType())) {
                    TextArea ta = new TextArea();
                    ta.setPromptText("Ecris ta reponse ici...");
                    ta.setPrefRowCount(3);
                    ta.setStyle(
                            "-fx-background-color:#f8fafc;" +
                                    "-fx-background-radius:8;" +
                                    "-fx-border-color:#e2e8f0;" +
                                    "-fx-font-size:13px;");
                    questionBox.getChildren().add(ta);
                }

                // Stocker la question dans le VBox
                questionBox.setUserData(q);
                questionsContainer.getChildren().add(questionBox);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // SUBMIT — meme logique que PHP play()
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleSubmit() {
        if (questions == null || questions.isEmpty()) return;

        disableInputs();
        if (timer != null) timer.cancel();

        int total = questions.size();
        boolean isLibre = "libre"
                .equalsIgnoreCase(currentGame.getType());

        if (isLibre) {
            // ── Libre : appel HuggingFace dans un thread ──────
            if (feedbackLabel != null)
                feedbackLabel.setText("Analyse en cours...");

            new Thread(() -> {
                int[] score = {0};
                StringBuilder feedback = new StringBuilder();

                for (javafx.scene.Node node
                        : questionsContainer.getChildren()) {
                    if (!(node instanceof VBox qBox)) continue;
                    Game_Question question =
                            (Game_Question) qBox.getUserData();
                    if (question == null) continue;

                    // Recuperer le texte tape
                    String userAnswer = "";
                    for (javafx.scene.Node child : qBox.getChildren()) {
                        if (child instanceof TextArea ta) {
                            userAnswer = ta.getText().trim();
                            break;
                        }
                    }

                    if (!userAnswer.isEmpty()) {
                        // Appel NLP — meme logique que PHP
                        float similarity = hfService.compareWithNLP(
                                userAnswer,
                                question.getCorrectAnswer()
                        );

                        // Memes seuils que le PHP
                        if (similarity > 0.8f) {
                            score[0] += 10;
                            feedback.append("Bonne reponse !\n");
                        } else if (similarity > 0.5f) {
                            score[0] += 5;
                            feedback.append("Reponse proche.\n");
                        } else {
                            feedback.append(
                                    "Reponse incorrecte.\n");
                        }
                    }
                }

                final int finalScore = score[0];
                final String finalFeedback = feedback.toString();
                Platform.runLater(() ->
                        updateScoreUI(finalScore, total, finalFeedback)
                );

            }).start();

        } else {
            // ── QCM / Vrai-Faux : compare directement ─────────
            int score = 0;

            for (javafx.scene.Node node
                    : questionsContainer.getChildren()) {
                if (!(node instanceof VBox qBox)) continue;
                Game_Question question =
                        (Game_Question) qBox.getUserData();
                if (question == null) continue;

                for (javafx.scene.Node child : qBox.getChildren()) {
                    if (child instanceof RadioButton rb
                            && rb.isSelected()) {
                        if (rb.getText().equals(
                                question.getCorrectAnswer())) {
                            score += 10;
                        }
                    }
                }
            }

            updateScoreUI(score, total, null);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // MISE A JOUR UI + BDD — meme logique PHP persist/flush
    // ─────────────────────────────────────────────────────────────
    private void updateScoreUI(
            int score, int total, String feedbackText) {

        int maxScore = total * 10;
        double ratio = maxScore > 0
                ? (double) score / maxScore : 0;

        // Score
        lastScoreLabel.setText(String.valueOf(score));
        if (scoreDetailLabel != null)
            scoreDetailLabel.setText(score + " / " + maxScore
                    + " points");

        // Barre de progression
        progressBar.setProgress(ratio);

        // Moyenne — meme logique PHP : (oldAvg + score) / 2
        double oldAvg = currentGame.getAvgScore();
        double newAvg = oldAvg == 0
                ? score : (oldAvg + score) / 2.0;
        avgScoreLabel.setText(String.format("%.1f", newAvg));

        // Badge selon ratio
        if (ratio >= 0.9) {
            badgeLabel.setText("Excellent !");
            badgeLabel.setStyle(
                    "-fx-text-fill:#16a34a;" +
                            "-fx-font-weight:bold;" +
                            "-fx-font-size:16;");
        } else if (ratio >= 0.7) {
            badgeLabel.setText("Bien joue !");
            badgeLabel.setStyle(
                    "-fx-text-fill:#0FB5A9;" +
                            "-fx-font-weight:bold;" +
                            "-fx-font-size:16;");
        } else if (ratio >= 0.5) {
            badgeLabel.setText("Peut mieux faire.");
            badgeLabel.setStyle(
                    "-fx-text-fill:#f59e0b;" +
                            "-fx-font-weight:bold;" +
                            "-fx-font-size:16;");
        } else {
            badgeLabel.setText("Continue les efforts !");
            badgeLabel.setStyle(
                    "-fx-text-fill:#dc2626;" +
                            "-fx-font-weight:bold;" +
                            "-fx-font-size:16;");
        }

        // Feedback NLP (libre uniquement)
        if (feedbackText != null
                && feedbackLabel != null
                && !feedbackText.isEmpty()) {
            feedbackLabel.setText(feedbackText);
            feedbackLabel.setStyle(
                    "-fx-text-fill:#334155; -fx-font-size:12;");
        }

        // Tentatives + 1
        int newAttempt = currentGame.getAttemptNumber() + 1;
        attemptLabel.setText(String.valueOf(newAttempt));

        // Sauvegarder BDD
        try {
            currentGame.setLastScore(score);
            currentGame.setAvgScore((int) newAvg);
            currentGame.setAttemptNumber(newAttempt);
            gameService.modifier(currentGame);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RETOUR
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleBack() {
        if (timer != null) timer.cancel();
        try {
            Parent root = FXMLLoader.load(getClass()
                    .getResource(
                            "/tn/esprit/view/front_GameList.fxml"));
            Stage stage = (Stage) gameTitleLabel
                    .getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CALCULATRICE
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void openCalculator() {
        Stage calcStage = new Stage();
        VBox layout = new VBox(10);
        layout.setStyle("-fx-padding:20;");
        TextField calcInput = new TextField();
        Label resultLabel = new Label();
        Button btnEqual = new Button("=");
        btnEqual.setOnAction(e -> {
            try {
                double result = eval(calcInput.getText());
                resultLabel.setText("Resultat : " + result);
            } catch (Exception ex) {
                resultLabel.setText("Erreur");
            }
        });
        layout.getChildren().addAll(
                calcInput, btnEqual, resultLabel);
        calcStage.setScene(new Scene(layout, 250, 200));
        calcStage.setTitle("Calculatrice");
        calcStage.show();
    }

    private double eval(String expr) throws Exception {
        return ((Number) new javax.script.ScriptEngineManager()
                .getEngineByName("JavaScript")
                .eval(expr)).doubleValue();
    }

    // ─────────────────────────────────────────────────────────────
    // NAVIGATION
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void handleAccueil(ActionEvent event) {
        if (timer != null) timer.cancel();
        navigateTo(
                "/tn/esprit/view/front_user_dashboard.fxml", event);
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        if (timer != null) timer.cancel();
        navigateTo(
                "/tn/esprit/view/front_profile.fxml", event);
    }

    @FXML
    private void handleCours(ActionEvent event) {
        if (timer != null) timer.cancel();
        navigateTo(
                "/tn/esprit/view/front_CoursCategories.fxml", event);
    }

    @FXML
    private void handleGameList(ActionEvent event) {
        if (timer != null) timer.cancel();
        navigateTo(
                "/tn/esprit/view/front_GameList.fxml", event);
    }

    @FXML
    private void handleEvents(ActionEvent event) {
        if (timer != null) timer.cancel();
        navigateTo(
                "/tn/esprit/view/frontEvent.fxml", event);
    }

    @FXML
    private void handleForums(ActionEvent event) {
        if (timer != null) timer.cancel();
        navigateTo(
                "/tn/esprit/view/front_forum.fxml", event);
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(
                    getClass().getResource(fxmlPath));
            Stage stage = (Stage)
                    ((javafx.scene.Node) event.getSource())
                            .getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println(
                    "Erreur navigation: " + e.getMessage());
        }
    }
}