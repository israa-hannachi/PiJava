package tn.esprit.entities.meet;

import java.sql.Timestamp;

public class participant {
    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String role;
    private Timestamp createdAt;
    private String smtpEmail;
    private String smtpAppPassword;

    public participant() {}

    public participant(String nom, String prenom, String email, String role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public participant(int id, String nom, String prenom, String email, String role, Timestamp createdAt, String smtpEmail, String smtpAppPassword) {
        this.id = id;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.role = role;
        this.createdAt = createdAt;
        this.smtpEmail = smtpEmail;
        this.smtpAppPassword = smtpAppPassword;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getSmtpEmail() { return smtpEmail; }
    public void setSmtpEmail(String smtpEmail) { this.smtpEmail = smtpEmail; }

    public String getSmtpAppPassword() { return smtpAppPassword; }
    public void setSmtpAppPassword(String smtpAppPassword) { this.smtpAppPassword = smtpAppPassword; }

    @Override
    public String toString() {
        return "Participant{id=" + id + ", nom='" + nom + "', prenom='" + prenom +
            "', email='" + email + "', role='" + role + "'}";
    }
}
