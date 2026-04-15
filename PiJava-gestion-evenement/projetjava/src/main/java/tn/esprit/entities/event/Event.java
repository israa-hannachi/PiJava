package tn.esprit.entities.event;

import java.sql.Timestamp;
import java.math.BigDecimal;

public class Event {
    private int id;
    private String titre;
    private String description;
    private Timestamp dateCreation;
    private Timestamp dateDebut;
    private Timestamp dateFin;
    private int capacite;
    private int inscrits;
    private String image;
    private String categorie;
    private BigDecimal prix;
    private String lieu;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String statut;
    private String timeZone;
    private boolean isRecurring;
    private String recurrenceFrequency;
    private Integer recurrenceCount;
    private String attendeesEmails;
    private String organizerEmail;
    private String icalId;

    public Event() {}

    public Event(int id, String titre, String description, Timestamp dateDebut, Timestamp dateFin, int capacite, String image, String categorie, BigDecimal prix, String lieu, String statut) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.capacite = capacite;
        this.image = image;
        this.categorie = categorie;
        this.prix = prix;
        this.lieu = lieu;
        this.statut = statut;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public Timestamp getDateDebut() { return dateDebut; }
    public void setDateDebut(Timestamp dateDebut) { this.dateDebut = dateDebut; }

    public Timestamp getDateFin() { return dateFin; }
    public void setDateFin(Timestamp dateFin) { this.dateFin = dateFin; }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }

    public int getInscrits() { return inscrits; }
    public void setInscrits(int inscrits) { this.inscrits = inscrits; }

    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }

    public BigDecimal getPrix() { return prix; }
    public void setPrix(BigDecimal prix) { this.prix = prix; }

    public String getLieu() { return lieu; }
    public void setLieu(String lieu) { this.lieu = lieu; }

    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }

    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getTimeZone() { return timeZone; }
    public void setTimeZone(String timeZone) { this.timeZone = timeZone; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getRecurrenceFrequency() { return recurrenceFrequency; }
    public void setRecurrenceFrequency(String recurrenceFrequency) { this.recurrenceFrequency = recurrenceFrequency; }

    public Integer getRecurrenceCount() { return recurrenceCount; }
    public void setRecurrenceCount(Integer recurrenceCount) { this.recurrenceCount = recurrenceCount; }

    public String getAttendeesEmails() { return attendeesEmails; }
    public void setAttendeesEmails(String attendeesEmails) { this.attendeesEmails = attendeesEmails; }

    public String getOrganizerEmail() { return organizerEmail; }
    public void setOrganizerEmail(String organizerEmail) { this.organizerEmail = organizerEmail; }

    public String getIcalId() { return icalId; }
    public void setIcalId(String icalId) { this.icalId = icalId; }

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", titre='" + titre + '\'' +
                ", dateDebut=" + dateDebut +
                ", lieu='" + lieu + '\'' +
                '}';
    }
}
