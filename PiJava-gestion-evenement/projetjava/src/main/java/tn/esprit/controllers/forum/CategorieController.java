package tn.esprit.controllers.forum;

import java.util.List;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.services.forum.ServiceCategorie;

public class CategorieController {

    // CORRECTION : Passé en public pour l'accès depuis le Main (Erreur private access)
    public final ServiceCategorie serviceCategorie = new ServiceCategorie();

    @FXML private TextField txtTitreCat;
    @FXML private TextArea txtDescCat;
    @FXML private TextField txtIconeCat;

    private int idCategorieSelectionnee;

    // --- MÉTHODES ACTION (JavaFX - Interface Graphique) ---

    @FXML
    public void ajouterNouvelleCategorie(ActionEvent event) {
        String titre = txtTitreCat.getText();
        String desc = txtDescCat.getText();
        String icone = txtIconeCat.getText();

        // Appelle la version logique métier
        ajouterNouvelleCategorie(titre, desc, icone);
    }

    @FXML
    public void modifier(ActionEvent event) {
        String titre = txtTitreCat.getText();
        String desc = txtDescCat.getText();
        String icone = txtIconeCat.getText();

        if (titre != null && !titre.isEmpty()) {
            Categorie c = new Categorie(idCategorieSelectionnee, titre, desc, icone);
            this.serviceCategorie.modifier(c);
            System.out.println("✅ Modification réussie");
        }
    }

    @FXML
    public void supprimerCategorie(ActionEvent event) {
        if (idCategorieSelectionnee != 0) {
            supprimerCategorie(idCategorieSelectionnee);
        }
    }

    // --- MÉTHODES CORRIGÉES POUR LE MAIN (Console / CLI) ---

    /**
     * CORRECTION : Cette méthode accepte maintenant les 3 arguments String
     * demandés par votre Main (Résout : Expected 1 argument but found 3)
     */
    public void ajouterNouvelleCategorie(String titre, String description, String icone) {
        if (titre != null && !titre.isEmpty()) {
            Categorie c = new Categorie(titre, description, icone);
            this.serviceCategorie.ajouter(c);
            System.out.println("✅ Ajout réussi (Mode Console)");
        } else {
            System.err.println("❌ Erreur : Le titre est obligatoire.");
        }
    }

    /**
     * CORRECTION : Cette méthode accepte un int
     * (Résout : cannot be applied to (int) dans le Main)
     */
    public void supprimerCategorie(int id) {
        if (id != 0) {
            this.serviceCategorie.supprimer(id);
            System.out.println("✅ Suppression réussie (ID: " + id + ")");
        }
    }

    // --- MÉTHODES MÉTIERS & UTILITAIRES ---

    public List<Categorie> listerToutesLesCategories() {
        return this.serviceCategorie.afficher();
    }

    public Categorie trouverCategorie(String titre) {
        return this.serviceCategorie.trouverParTitre(titre);
    }

    public void chargerDonnees(Categorie c) {
        this.idCategorieSelectionnee = c.getId();
        if (txtTitreCat != null) this.txtTitreCat.setText(c.getTitre());
        if (txtDescCat != null) this.txtDescCat.setText(c.getDescription());
        if (txtIconeCat != null) this.txtIconeCat.setText(c.getIcone());
    }

    public List<Categorie> rechercherCategories(String valeur) {
        return this.serviceCategorie.rechercher(valeur);
    }

    public List<Categorie> trierCategoriesParTitre() {
        return this.serviceCategorie.trierParTitre();
    }

    @FXML
    public void retourListe(ActionEvent event) {
        try {
            System.out.println("🔄 Retour à la liste des catégories...");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}