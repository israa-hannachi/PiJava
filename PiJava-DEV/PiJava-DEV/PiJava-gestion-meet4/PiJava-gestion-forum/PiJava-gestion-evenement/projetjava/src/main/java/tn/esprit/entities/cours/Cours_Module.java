package tn.esprit.entities.cours;

import java.sql.Timestamp;

public class Cours_Module {
    private int id;
    private String titre;
    private String description;
    private int duree;
    private String niveau;
    private Timestamp dateCreation;
    private int actif;
    private int categorieId;
    private int creeParAdmin;

    public Cours_Module() {}

    public Cours_Module(int id, String titre, String description, int duree, String niveau, Timestamp dateCreation, int actif, int categorieId, int creeParAdmin) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.duree = duree;
        this.niveau = niveau;
        this.dateCreation = dateCreation;
        this.actif = actif;
        this.categorieId = categorieId;
        this.creeParAdmin = creeParAdmin;
    }

    public Cours_Module(String titre, String description, int duree, String niveau, Timestamp dateCreation, int actif, int categorieId, int creeParAdmin) {
        this.titre = titre;
        this.description = description;
        this.duree = duree;
        this.niveau = niveau;
        this.dateCreation = dateCreation;
        this.actif = actif;
        this.categorieId = categorieId;
        this.creeParAdmin = creeParAdmin;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public String getNiveau() { return niveau; }
    public void setNiveau(String niveau) { this.niveau = niveau; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }

    public int getCategorieId() { return categorieId; }
    public void setCategorieId(int categorieId) { this.categorieId = categorieId; }

    public int getCreeParAdmin() { return creeParAdmin; }
    public void setCreeParAdmin(int creeParAdmin) { this.creeParAdmin = creeParAdmin; }

    @Override
    public String toString() {
        return "Cours_Module{id=" + id + ", titre='" + titre + "', niveau='" + niveau + "', duree=" + duree + ", categorieId=" + categorieId + "}";
    }
}