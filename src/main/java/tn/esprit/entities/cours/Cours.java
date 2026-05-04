package tn.esprit.entities.cours;

import java.sql.Timestamp;

public class Cours {
    private int id;
    private String titre;
    private String description;
    private String contenu;
    private int duree;
    private int ordre;
    private Timestamp dateCreation;
    private Timestamp dateModification;
    private int actif;
    private int moduleId;
    private String fichierContenu;
    private int creeParAdmin;
    private int visible;
    private Timestamp visibleFrom;
    private String resumeAi;

    public Cours() {}

    public Cours(int id, String titre, String description, String contenu, int duree, int ordre,
                 Timestamp dateCreation, int actif, int moduleId, String fichierContenu,
                 int creeParAdmin, int visible) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.contenu = contenu;
        this.duree = duree;
        this.ordre = ordre;
        this.dateCreation = dateCreation;
        this.actif = actif;
        this.moduleId = moduleId;
        this.fichierContenu = fichierContenu;
        this.creeParAdmin = creeParAdmin;
        this.visible = visible;
    }

    public Cours(String titre, String description, String contenu, int duree, int ordre,
                 Timestamp dateCreation, int actif, int moduleId, String fichierContenu,
                 int creeParAdmin, int visible) {
        this.titre = titre;
        this.description = description;
        this.contenu = contenu;
        this.duree = duree;
        this.ordre = ordre;
        this.dateCreation = dateCreation;
        this.actif = actif;
        this.moduleId = moduleId;
        this.fichierContenu = fichierContenu;
        this.creeParAdmin = creeParAdmin;
        this.visible = visible;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public int getDuree() { return duree; }
    public void setDuree(int duree) { this.duree = duree; }

    public int getOrdre() { return ordre; }
    public void setOrdre(int ordre) { this.ordre = ordre; }

    public Timestamp getDateCreation() { return dateCreation; }
    public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }

    public Timestamp getDateModification() { return dateModification; }
    public void setDateModification(Timestamp dateModification) { this.dateModification = dateModification; }

    public int getActif() { return actif; }
    public void setActif(int actif) { this.actif = actif; }

    public int getModuleId() { return moduleId; }
    public void setModuleId(int moduleId) { this.moduleId = moduleId; }

    public String getFichierContenu() { return fichierContenu; }
    public void setFichierContenu(String fichierContenu) { this.fichierContenu = fichierContenu; }

    public int getCreeParAdmin() { return creeParAdmin; }
    public void setCreeParAdmin(int creeParAdmin) { this.creeParAdmin = creeParAdmin; }

    public int getVisible() { return visible; }
    public void setVisible(int visible) { this.visible = visible; }

    public Timestamp getVisibleFrom() { return visibleFrom; }
    public void setVisibleFrom(Timestamp visibleFrom) { this.visibleFrom = visibleFrom; }

    public String getResumeAi() { return resumeAi; }
    public void setResumeAi(String resumeAi) { this.resumeAi = resumeAi; }

    @Override
    public String toString() {
        return "cours{id=" + id + ", titre='" + titre + "', duree=" + duree + ", moduleId=" + moduleId + ", actif=" + actif + "}";
    }
}