//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.entities.forum;

import java.sql.Date;

public class Message {
    private int id;
    private String contenu;
    private Date datePublication;
    private String etat;
    private String createdBy;
    private Forum forum;

    public Message() {
    }

    public Message(int id, String contenu, Date datePublication, String etat, String createdBy, Forum forum) {
        this.id = id;
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.etat = etat;
        this.createdBy = createdBy;
        this.forum = forum;
    }

    public Message(String contenu, Date datePublication, String etat, String createdBy, Forum forum) {
        this.contenu = contenu;
        this.datePublication = datePublication;
        this.etat = etat;
        this.createdBy = createdBy;
        this.forum = forum;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContenu() {
        return this.contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }

    public Date getDatePublication() {
        return this.datePublication;
    }

    public void setDatePublication(Date datePublication) {
        this.datePublication = datePublication;
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

    public Forum getForum() {
        return this.forum;
    }

    public void setForum(Forum forum) {
        this.forum = forum;
    }

    public String toString() {
        int var10000 = this.id;
        return "Message{id=" + var10000 + ", contenu='" + this.contenu + "', createdBy='" + this.createdBy + "', forum=" + (this.forum != null ? this.forum.getTitre() : "N/A") + "}";
    }
}
