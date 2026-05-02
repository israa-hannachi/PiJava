package tn.esprit.entities.game;

public class Game_Question {
    private int id;
    private String questionText;
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private String correctAnswer;
    private int gameId;

    // 🔗 Liaison logique : chaque question appartient à un jeu
    private Game game;

    // Constructeur vide
    public Game_Question() {}

    // Constructeur SANS id → pour l'ajout
    public Game_Question(String questionText, String option1, String option2,
                         String option3, String option4,
                         String correctAnswer, int gameId) {
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswer = correctAnswer;
        this.gameId = gameId;
    }

    // Constructeur AVEC id → pour SELECT
    public Game_Question(int id, String questionText, String option1, String option2,
                         String option3, String option4,
                         String correctAnswer, int gameId) {
        this.id = id;
        this.questionText = questionText;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswer = correctAnswer;
        this.gameId = gameId;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }

    public String getOption1() { return option1; }
    public void setOption1(String option1) { this.option1 = option1; }

    public String getOption2() { return option2; }
    public void setOption2(String option2) { this.option2 = option2; }

    public String getOption3() { return option3; }
    public void setOption3(String option3) { this.option3 = option3; }

    public String getOption4() { return option4; }
    public void setOption4(String option4) { this.option4 = option4; }

    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }

    public int getGameId() { return gameId; }
    public void setGameId(int gameId) { this.gameId = gameId; }

    public Game getGame() { return game; }
    public void setGame(Game game) { this.game = game; }

    @Override
    public String toString() {
        return "GameQuestion{" +
                "id=" + id +
                ", questionText='" + questionText + '\'' +
                ", option1='" + option1 + '\'' +
                ", option2='" + option2 + '\'' +
                ", option3='" + option3 + '\'' +
                ", option4='" + option4 + '\'' +
                ", correctAnswer='" + correctAnswer + '\'' +
                ", gameId=" + gameId +
                '}';
    }
}
