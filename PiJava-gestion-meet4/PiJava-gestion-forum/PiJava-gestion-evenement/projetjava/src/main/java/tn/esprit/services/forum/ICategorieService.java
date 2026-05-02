//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.services.forum;

import java.util.List;
import tn.esprit.entities.forum.Categorie;

public interface ICategorieService extends IService<Categorie> {
    List<Categorie> afficher();

    Categorie trouverParTitre(String var1);
}
