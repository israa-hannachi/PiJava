//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.entities.forum;

public class Categorie {
    private int id;
    private String titre;
    private String description;
    private String icone;

    public Categorie() {
    }

    public Categorie(int id, String titre, String description, String icone) {
        this.id = id;
        this.titre = titre;
        this.description = description;
        this.icone = icone;
    }

    public Categorie(String titre, String description, String icone) {
        this.titre = titre;
        this.description = description;
        this.icone = icone;
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

    public String getIcone() {
        return this.icone;
    }

    public void setIcone(String icone) {
        this.icone = icone;
    }

    public String toString() {
        return "Categorie{id=" + this.id + ", titre='" + this.titre + "', description='" + this.description + "', icone='" + this.icone + "'}";
    }
}
