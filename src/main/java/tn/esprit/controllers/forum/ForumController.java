//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.controllers.forum;

import java.sql.Date;
import java.util.List;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;
import tn.esprit.services.forum.ServiceForum;

public class ForumController {
    private final ServiceForum serviceForum = new ServiceForum();

    public void creerForum(String titre, String desc, String auteur, Categorie cat) {
        if (cat != null && cat.getId() != 0) {
            Forum f = new Forum(titre, desc, new Date(System.currentTimeMillis()), "actif", auteur, cat);
            this.serviceForum.ajouter(f);
        } else {
            System.out.println("Erreur : Un forum doit être lié à une catégorie valide.");
        }
    }

    public List<Forum> recupererListeForums() {
        return this.serviceForum.afficher();
    }

    public void supprimerForum(int id) {
        this.serviceForum.supprimer(id);
    }

    public void modifierForum(Forum f) {
        this.serviceForum.modifier(f);
    }

    public void mettreAJourForum(Forum f) {
        this.serviceForum.modifier(f);
    }
}
