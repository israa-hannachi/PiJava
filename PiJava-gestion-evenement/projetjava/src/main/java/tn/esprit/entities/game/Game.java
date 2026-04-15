package tn.esprit.entities.game;

import java.sql.Timestamp;
import java.util.List;

public class Game {
    private int id;
    private String titre;
    private String type;
    private String niveau;
    private int scoreMax;
    private int lastScore;
    private double avgScore;
    private int duration;
    private int attemptNumber;
    private Timestamp createdAt;
    private int courseId;

    // 🔗 Liaison logique : un jeu a plusieurs questions
    private List<Game_Question> questions;

    // Constructeur vide (optionnel mais utile)
    public Game() {}

    // Constructeur SANS id → utilisé pour l’ajout
    public Game(String titre, String type, String niveau,
                int scoreMax, int lastScore, double avgScore,
                int duration, int attemptNumber,
                Timestamp createdAt, int courseId) {
        this.titre = titre;
        this.type = type;
        this.niveau = niveau;
        this.scoreMax = scoreMax;
        this.lastScore = lastScore;
        this.avgScore = avgScore;
        this.duration = duration;
        this.attemptNumber = attemptNumber;
        this.createdAt = createdAt;
        this.courseId = courseId;
    }

    // Constructeur AVEC id → utilisé pour SELECT (lecture depuis la base)
    public Game(int id, String titre, String type, String niveau,
                int scoreMax, int lastScore, double avgScore,
                int duration, int attemptNumber,
                Timestamp createdAt, int courseId) {
        this.id = id;
        this.titre = titre;
        this.type = type;
        this.niveau = niveau;
        this.scoreMax = scoreMax;
        this.lastScore = lastScore;
        this.avgScore = avgScore;
        this.duration = duration;
        this.attemptNumber = attemptNumber;
        this.createdAt = createdAt;
        this.courseId = courseId;
    }

    // Getters et setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public int getScoreMax() { return scoreMax; }
    public void setScoreMax(int scoreMax) { this.scoreMax = scoreMax; }

    public int getLastScore() { return lastScore; }
    public void setLastScore(int lastScore) { this.lastScore = lastScore; }

    public double getAvgScore() { return avgScore; }
    public void setAvgScore(double avgScore) { this.avgScore = avgScore; }

    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = duration; }

    public int getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(int attemptNumber) { this.attemptNumber = attemptNumber; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getCourseId() { return courseId; }
    public void setCourseId(int courseId) { this.courseId = courseId; }

    public List<Game_Question> getQuestions() { return questions; }
    public void setQuestions(List<Game_Question> questions) { this.questions = questions; }

    @Override
    public String toString() {
        return "Game{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", type='" + type + '\'' +
                ", niveau='" + niveau + '\'' +
                ", scoreMax=" + scoreMax +
                ", lastScore=" + lastScore +
                ", avgScore=" + avgScore +
                ", duration=" + duration +
                ", attemptNumber=" + attemptNumber +
                ", createdAt=" + createdAt +
                ", courseId=" + courseId +
                '}';
    }
}
