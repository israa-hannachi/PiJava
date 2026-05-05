//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.controllers.forum;

import java.util.List;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.services.forum.ServiceCategorie;

public class CategorieController {
    private final ServiceCategorie serviceCategorie = new ServiceCategorie();

    public void ajouterNouvelleCategorie(String titre, String desc, String icone) {
        if (titre != null && !titre.isEmpty()) {
            Categorie c = new Categorie(titre, desc, icone);
            this.serviceCategorie.ajouter(c);
        } else {
            System.out.println("Erreur : Le titre est obligatoire.");
        }
    }

    public List<Categorie> listerToutesLesCategories() {
        return this.serviceCategorie.afficher();
    }

    public Categorie trouverCategorie(String titre) {
        return this.serviceCategorie.trouverParTitre(titre);
    }

    public void supprimerCategorie(int id) {
        this.serviceCategorie.supprimer(id);
    }
}
