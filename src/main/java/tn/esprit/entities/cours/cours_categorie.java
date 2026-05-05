package tn.esprit.entities.cours;

import java.sql.Timestamp;

public class cours_categorie {
    private int id;
    private String nom;
    private String description;
    private Timestamp dateCreation;
    private int actif;

    public cours_categorie() {}

    public cours_categorie(int id, String nom, String description, Timestamp dateCreation, int actif) {
        this.id = id;
        this.nom = nom;
        this.description = description;
        this.dateCreation = dateCreation;
        this.actif = actif;
    }

    public cours_categorie(String nom, String description, Timestamp dateCreation, int actif) {
        this.nom = nom;
        this.description = description;
        this.dateCreation = dateCreation;
        this.actif = actif;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }

    @Override
    public String toString() {
        return "cours_categorie{id=" + id + ", nom='" + nom + "', description='" + description + "', actif=" + actif + "}";
    }
}