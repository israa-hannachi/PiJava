//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.entities.forum;

import java.util.Date;

public class Forum {
    private int id;
    private String titre;
    private String description;
    private Date dateCreation;
    private String etat;
    private String createdBy;
    private Categorie categorie;

    public Forum() {
    }

    public Forum(int id, String titre, String description, Date dateCreation, String etat, String createdBy, Categorie categorie) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.dateCreation = dateCreation;
        this.etat = etat;
        this.createdBy = createdBy;
        this.categorie = categorie;
    }

    public Forum(String titre, String description, Date dateCreation, String etat, String createdBy, Categorie categorie) {
        this.titre = titre;
        this.description = description;
        this.dateCreation = dateCreation;
        this.etat = etat;
        this.createdBy = createdBy;
        this.categorie = categorie;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitre() {
        return this.titre;
    }

    public void setTitre(String titre) {
        this.titre = titre;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDateCreation() {
        return this.dateCreation;
    }

    public void setDateCreation(Date dateCreation) {
        this.dateCreation = dateCreation;
    }

    public String getEtat() {
        return this.etat;
    }

    public void setEtat(String etat) {
        this.etat = etat;
    }

    public String getCreatedBy() {
        return this.createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Categorie getCategorie() {
        return this.categorie;
    }

    public void setCategorie(Categorie categorie) {
        this.categorie = categorie;
    }

    public String toString() {
        int var10000 = this.id;
        return "Forum{id=" + var10000 + ", titre='" + this.titre + "', description='" + this.description + "', dateCreation=" + String.valueOf(this.dateCreation) + ", etat='" + this.etat + "', createdBy='" + this.createdBy + "', categorie=" + (this.categorie != null ? this.categorie.getTitre() : "N/A") + "}";
    }
}
