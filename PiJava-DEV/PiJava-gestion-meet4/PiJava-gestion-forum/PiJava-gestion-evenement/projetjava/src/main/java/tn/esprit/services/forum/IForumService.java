//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.services.forum;

import java.util.List;

public interface IForumService<T> {
    void ajouter(T var1);

    void modifier(T var1);

    void supprimer(int var1);

    List<T> afficher();
}
