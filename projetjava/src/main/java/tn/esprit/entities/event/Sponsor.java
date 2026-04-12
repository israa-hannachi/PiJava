package tn.esprit.entities.event;

import java.sql.Timestamp;
import java.math.BigDecimal;

public class Sponsor {
    private int id;
    private int eventId;
    private String nom;
    private String description;
    private String logo;
    private String siteWeb;
    private String type;
    private BigDecimal montant;
    private Timestamp dateDebut;
    private Timestamp dateFin;
    private String statut;
    private String contactPersonne;
    private String contactEmail;
    private String contactTelephone;
    private Timestamp dateCreation;

    public Sponsor() {}

    public Sponsor(int id, int eventId, String nom, String type, BigDecimal montant, Timestamp dateDebut, Timestamp dateFin, String statut) {
        this.id = id;
        this.eventId = eventId;
        this.nom = nom;
        this.type = type;
        this.montant = montant;
        this.dateDebut = dateDebut;
        this.dateFin = dateFin;
        this.statut = statut;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLogo() { return logo; }
    public void setLogo(String logo) { this.logo = logo; }

    public String getSiteWeb() { return siteWeb; }
    public void setSiteWeb(String siteWeb) { this.siteWeb = siteWeb; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public BigDecimal getMontant() { return montant; }
    public void setMontant(BigDecimal montant) { this.montant = montant; }

    public Timestamp getDateDebut() { return dateDebut; }
    public void setDateDebut(Timestamp dateDebut) { this.dateDebut = dateDebut; }

    public Timestamp getDateFin() { return dateFin; }
    public void setDateFin(Timestamp dateFin) { this.dateFin = dateFin; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public String getContactPersonne() { return contactPersonne; }
    public void setContactPersonne(String contactPersonne) { this.contactPersonne = contactPersonne; }

    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }

    public String getContactTelephone() { return contactTelephone; }
    public void setContactTelephone(String contactTelephone) { this.contactTelephone = contactTelephone; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    @Override
    public String toString() {
        return "Sponsor{" +
                "id=" + id +
                ", eventId=" + eventId +
                ", nom='" + nom + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
