//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.controllers.forum;

import java.sql.Date;
import java.util.List;
import tn.esprit.entities.forum.Forum;
import tn.esprit.entities.forum.Message;
import tn.esprit.services.forum.ServiceMessage;

public class MessageController {
    private final ServiceMessage serviceMessage = new ServiceMessage();

    public void posterMessage(String contenu, String auteur, Forum forum) {
        if (forum == null) {
            System.out.println("Erreur : Impossible de poster sans forum cible.");
        } else {
            Message m = new Message();
            m.setContenu(contenu);
            m.setCreatedBy(auteur);
            m.setDatePublication(new Date(System.currentTimeMillis()));
            m.setEtat("publié");
            m.setForum(forum);
            this.serviceMessage.ajouter(m);
        }
    }

    public List<Message> voirMessagesForum(int forumId) {
        return this.serviceMessage.getMessagesByForum(forumId);
    }

    public int statistiquesUtilisateur(String auteur) {
        return this.serviceMessage.compterMessagesParUtilisateur(auteur);
    }

    public void supprimerMessageInapproprié(int id) {
        this.serviceMessage.supprimer(id);
    }
}
