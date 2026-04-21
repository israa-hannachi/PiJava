package tn.esprit.entities.event;

import java.sql.Timestamp;
import java.math.BigDecimal;

public class Registration {
    private int id;
    private int evenementId;
    private String visitorName;
    private String visitorEmail;
    private Timestamp dateInscription;
    private String statut;
    private boolean presence;
    private String modePaiement;
    private BigDecimal montantPaye;
    private String paiementStatut;
    private String notes;

    public Registration() {}

    public Registration(int id, int evenementId, String visitorName, String visitorEmail, String statut, String modePaiement, BigDecimal montantPaye, String paiementStatut) {
        this.id = id;
        this.evenementId = evenementId;
        this.visitorName = visitorName;
        this.visitorEmail = visitorEmail;
        this.statut = statut;
        this.modePaiement = modePaiement;
        this.montantPaye = montantPaye;
        this.paiementStatut = paiementStatut;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEvenementId() { return evenementId; }
    public void setEvenementId(int evenementId) { this.evenementId = evenementId; }

    public String getVisitorName() { return visitorName; }
    public void setVisitorName(String visitorName) { this.visitorName = visitorName; }

    public String getVisitorEmail() { return visitorEmail; }
    public void setVisitorEmail(String visitorEmail) { this.visitorEmail = visitorEmail; }

    public Timestamp getDateInscription() { return dateInscription; }
    public void setDateInscription(Timestamp dateInscription) { this.dateInscription = dateInscription; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public boolean isPresence() { return presence; }
    public void setPresence(boolean presence) { this.presence = presence; }

    public String getModePaiement() { return modePaiement; }
    public void setModePaiement(String modePaiement) { this.modePaiement = modePaiement; }

    public BigDecimal getMontantPaye() { return montantPaye; }
    public void setMontantPaye(BigDecimal montantPaye) { this.montantPaye = montantPaye; }

    public String getPaiementStatut() { return paiementStatut; }
    public void setPaiementStatut(String paiementStatut) { this.paiementStatut = paiementStatut; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return "Registration{" +
                "id=" + id +
                ", evenementId=" + evenementId +
                ", visitorName='" + visitorName + '\'' +
                ", statut='" + statut + '\'' +
                '}';
    }
}
