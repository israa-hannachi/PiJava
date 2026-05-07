package tn.esprit.entities.event;

import java.sql.Timestamp;

public class Rating {
    private int id;
    private int eventId;
    private int stars;
    private String comment;
    private Timestamp createdAt;

    public Rating() {}

    public Rating(int id, int eventId, int stars, String comment, Timestamp createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.stars = stars;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Rating(int eventId, int stars, String comment) {
        this.eventId = eventId;
        this.stars = stars;
        this.comment = comment;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getStars() { return stars; }
    public void setStars(int stars) { this.stars = stars; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
