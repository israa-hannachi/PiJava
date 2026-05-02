package tn.esprit.entities.meet;

import java.sql.Timestamp;

public class Meet {
    private int id;
    private String titre;
    private String description;
    private Timestamp dateDebut;
    private Timestamp dateFin;
    private String lienMeet;
    private Timestamp createdAt;
    private int participantId;

    public Meet() {}

    public Meet(String titre, String description, Timestamp dateDebut, Timestamp dateFin, String lienMeet, int participantId) {
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lienMeet = lienMeet;
        this.createdAt = new Timestamp(System.currentTimeMillis());
        this.participantId = participantId;
    }

    public Meet(int id, String titre, String description, Timestamp dateDebut, Timestamp dateFin, String lienMeet, Timestamp createdAt, int participantId) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.lienMeet = lienMeet;
        this.createdAt = createdAt;
        this.participantId = participantId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getDateDebut() { return dateDebut; }
    public void setDateDebut(Timestamp dateDebut) { this.dateDebut = dateDebut; }

    public Timestamp getDateFin() { return dateFin; }
    public void setDateFin(Timestamp dateFin) { this.dateFin = dateFin; }

    public String getLienMeet() { return lienMeet; }
    public void setLienMeet(String lienMeet) { this.lienMeet = lienMeet; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }

    @Override
    public String toString() {
        return "Meet{id=" + id + ", titre='" + titre + "', dateDebut=" + dateDebut +
            ", dateFin=" + dateFin + ", lienMeet='" + lienMeet + "', participantId=" + participantId + "}";
    }
}
