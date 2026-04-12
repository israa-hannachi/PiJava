package tn.esprit.controllers.front;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.entities.game.Game;
import tn.esprit.entities.game.Game_Question;
import tn.esprit.services.game.GameService;
import tn.esprit.services.game.GameQuestionService;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FrontGamePlayController {
    @FXML private Label gameTitleLabel, timerLabel, lastScoreLabel, avgScoreLabel, attemptLabel, badgeLabel;
    @FXML private ProgressBar progressBar;
    @FXML private VBox questionsContainer;

    private Game currentGame;
    private int timeLeft;
    private Timer timer;

    private GameService gameService = new GameService();
    private GameQuestionService questionService = new GameQuestionService();

    // Initialisation du jeu
    public void initGame(Game game) {
        this.currentGame = game;
        gameTitleLabel.setText("🎮 Jouer : " + game.getTitre());
        lastScoreLabel.setText(String.valueOf(game.getLastScore()));
        avgScoreLabel.setText(String.valueOf(game.getAvgScore()));
        attemptLabel.setText(String.valueOf(game.getAttemptNumber()));

        timeLeft = game.getDuration();
        startTimer();

        loadQuestions();
    }

    // Timer avec Platform.runLater
    private void startTimer() {
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                Platform.runLater(() -> {
                    if (timeLeft <= 0) {
                        timer.cancel();
                        timerLabel.setText("⏰ Temps écoulé !");
                        disableInputs();
                    } else {
                        timerLabel.setText(timeLeft + " secondes restantes");
                        timeLeft--;
                    }
                });
            }
        }, 0, 1000);
    }

    private void disableInputs() {
        questionsContainer.getChildren().forEach(node -> node.setDisable(true));
    }

    // Chargement des questions
    private void loadQuestions() {
        try {
            List<Game_Question> questions = questionService.recupererParGame(currentGame.getId());
            for (Game_Question q : questions) {
                VBox questionBox = new VBox(6);
                Label qLabel = new Label(q.getQuestionText());
                qLabel.setStyle("-fx-font-size:16px; -fx-font-weight:700;");
                questionBox.getChildren().add(qLabel);

                if ("qcm".equalsIgnoreCase(currentGame.getType())) {
                    ToggleGroup group = new ToggleGroup();
                    for (String opt : new String[]{q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4()}) {
                        if (opt != null && !opt.isEmpty()) {
                            RadioButton rb = new RadioButton(opt);
                            rb.setToggleGroup(group);
                            questionBox.getChildren().add(rb);
                        }
                    }
                } else if ("vraie ou faux".equalsIgnoreCase(currentGame.getType())) {
                    ToggleGroup group = new ToggleGroup();
                    RadioButton rbTrue = new RadioButton("Vraie");
                    rbTrue.setToggleGroup(group);
                    RadioButton rbFalse = new RadioButton("Faux");
                    rbFalse.setToggleGroup(group);
                    questionBox.getChildren().addAll(rbTrue, rbFalse);
                } else if ("libre".equalsIgnoreCase(currentGame.getType())) {
                    TextArea ta = new TextArea();
                    ta.setPromptText("Écris ta réponse ici...");
                    ta.setPrefRowCount(4);
                    questionBox.getChildren().add(ta);
                }

                questionsContainer.getChildren().add(questionBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSubmit() {
        int score = 10;
        int total = questionsContainer.getChildren().size();

        for (javafx.scene.Node node : questionsContainer.getChildren()) {
            if (node instanceof VBox qBox) {
                // ✅ récupérer l'objet Game_Question associé au VBox
                Game_Question question = (Game_Question) qBox.getUserData();

                if (question != null) { // éviter NullPointerException
                    for (javafx.scene.Node child : qBox.getChildren()) {
                        if (child instanceof RadioButton rb && rb.isSelected()) {
                            if (rb.getText().equals(question.getCorrectAnswer())) {
                                score++;
                            }
                        }
                    }
                }
            }
        }

        // ✅ mise à jour des labels et de la barre de progression
        lastScoreLabel.setText(String.valueOf(score));
        avgScoreLabel.setText(String.valueOf((score * 100) / total));
        progressBar.setProgress((double) score / total);
        badgeLabel.setText(score == total ? "🏆 Excellent !" : "🎯 Continue !");
    }


    // Bouton Retour : revenir à la liste des jeux
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_GameList.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) gameTitleLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Bouton Calculatrice : popup simple
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
                resultLabel.setText("Résultat : " + result);
            } catch (Exception ex) {
                resultLabel.setText("Erreur");
            }
        });

        layout.getChildren().addAll(calcInput, btnEqual, resultLabel);
        calcStage.setScene(new Scene(layout, 250, 200));
        calcStage.setTitle("Calculatrice");
        calcStage.show();
    }

    // Évaluation simple d'une expression mathématique
    private double eval(String expr) throws Exception {
        return ((Number) new javax.script.ScriptEngineManager()
                .getEngineByName("JavaScript")
                .eval(expr)).doubleValue();
    }
}
