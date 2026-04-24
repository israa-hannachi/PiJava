//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.services.forum;

import java.util.List;
import tn.esprit.entities.forum.Message;

public interface IMessageService extends IService<Message> {
    List<Message> getMessagesByForum(int var1);

    int compterMessagesParUtilisateur(String var1);

    int statistiquesUtilisateur(String var1);
}
