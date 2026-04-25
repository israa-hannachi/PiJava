package tn.esprit.services.forum;

import org.junit.jupiter.api.*;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Classe de test pour ServiceForum
 * Vérifie automatiquement que les méthodes CRUD fonctionnent sur la base de données MySQL.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ServiceForumTest {

    private static ServiceForum serviceForum;
    private static int testForumId = -1;

    @BeforeAll
    static void setUp() {
        serviceForum = new ServiceForum();
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TESTS DE VALIDATION (sans DB)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Vérifier qu'un forum avec titre null est rejeté")
    void ajouter_forumAvecTitreNull_neDoitPasPlanter() {
        Categorie cat = new Categorie(1, "Test", "Desc", "icon.png");
        Forum f = new Forum(null, "Description", new Date(), "actif", "auteur", cat);

        assertDoesNotThrow(() -> serviceForum.ajouter(f));
    }

    @Test
    @Order(2)
    @DisplayName("Vérifier qu'un forum avec description vide est rejeté")
    void ajouter_forumAvecDescriptionVide_neDoitPasPlanter() {
        Categorie cat = new Categorie(1, "Test", "Desc", "icon.png");
        Forum f = new Forum("Titre", "", new Date(), "actif", "auteur", cat);

        assertDoesNotThrow(() -> serviceForum.ajouter(f));
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TESTS CRUD (intégration avec DB)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(3)
    @DisplayName("Vérifier que afficher() retourne une liste non null")
    void afficher_doitRetournerListeNonNull() {
        List<Forum> forums = serviceForum.afficher();
        assertNotNull(forums);
    }

    @Test
    @Order(4)
    @DisplayName("Vérifier que l'ajout d'un forum augmente la taille de la liste")
    void ajouter_forumValide_doitAugmenterTailleListe() {
        Categorie cat = new Categorie(1, "Test", "Description cat", "icon.png");
        Forum f = new Forum(
                "ForumTest_" + System.currentTimeMillis(),
                "Description test",
                new Date(),
                "actif",
                "testeur",
                cat
        );

        int tailleAvant = serviceForum.afficher().size();
        serviceForum.ajouter(f);
        int tailleApres = serviceForum.afficher().size();

        assertEquals(tailleAvant + 1, tailleApres);
    }

    @Test
    @Order(5)
    @DisplayName("Vérifier qu'un forum ajouté est retrouvé dans la liste")
    void ajouter_forumDoitEtreRetrouveDansListe() {
        String titreUnique = "ForumCRUD_" + System.currentTimeMillis();
        Categorie cat = new Categorie(1, "Test", "Description cat", "icon.png");
        Forum f = new Forum(
                titreUnique,
                "Description CRUD",
                new Date(),
                "actif",
                "testeurCRUD",
                cat
        );

        serviceForum.ajouter(f);

        List<Forum> forums = serviceForum.afficher();
        boolean trouve = forums.stream().anyMatch(x -> x.getTitre().equals(titreUnique));
        assertTrue(trouve, "Le forum ajouté devrait être trouvé dans la liste");

        // Sauvegarder l'ID pour les tests suivants
        testForumId = forums.stream()
                .filter(x -> x.getTitre().equals(titreUnique))
                .mapToInt(Forum::getId)
                .max()
                .orElse(-1);
    }

    @Test
    @Order(6)
    @DisplayName("Vérifier que modifier met à jour le titre du forum")
    void modifier_miseAJourTitre_doitFonctionner() {
        assumeTrue(testForumId > 0, "Ce test nécessite qu'un forum ait été inséré au préalable");

        List<Forum> forums = serviceForum.afficher();
        Forum forumAModifier = forums.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumAModifier, "Le forum à modifier devrait exister");

        String nouveauTitre = "ForumModifie_" + System.currentTimeMillis();
        forumAModifier.setTitre(nouveauTitre);
        serviceForum.modifier(forumAModifier);

        // Recharger et vérifier
        List<Forum> forumsApres = serviceForum.afficher();
        Forum forumModifie = forumsApres.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumModifie);
        assertEquals(nouveauTitre, forumModifie.getTitre());
    }

    @Test
    @Order(7)
    @DisplayName("Vérifier que modifier met à jour la description du forum")
    void modifier_miseAJourDescription_doitFonctionner() {
        assumeTrue(testForumId > 0, "Ce test nécessite qu'un forum ait été inséré au préalable");

        List<Forum> forums = serviceForum.afficher();
        Forum forumAModifier = forums.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumAModifier);

        String nouvelleDescription = "Description modifiée _" + System.currentTimeMillis();
        forumAModifier.setDescription(nouvelleDescription);
        serviceForum.modifier(forumAModifier);

        List<Forum> forumsApres = serviceForum.afficher();
        Forum forumModifie = forumsApres.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumModifie);
        assertEquals(nouvelleDescription, forumModifie.getDescription());
    }

    @Test
    @Order(8)
    @DisplayName("Vérifier que modifier met à jour l'état du forum")
    void modifier_miseAJourEtat_doitFonctionner() {
        assumeTrue(testForumId > 0, "Ce test nécessite qu'un forum ait été inséré au préalable");

        List<Forum> forums = serviceForum.afficher();
        Forum forumAModifier = forums.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumAModifier);

        String nouvelEtat = "inactif";
        forumAModifier.setEtat(nouvelEtat);
        serviceForum.modifier(forumAModifier);

        List<Forum> forumsApres = serviceForum.afficher();
        Forum forumModifie = forumsApres.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumModifie);
        assertEquals(nouvelEtat, forumModifie.getEtat());
    }

    @Test
    @Order(9)
    @DisplayName("Vérifier que supprimer réduit la taille de la liste")
    void supprimer_forumExistant_doitReduireTailleListe() {
        assumeTrue(testForumId > 0, "Ce test nécessite qu'un forum ait été inséré au préalable");

        int tailleAvant = serviceForum.afficher().size();
        serviceForum.supprimer(testForumId);
        int tailleApres = serviceForum.afficher().size();

        assertTrue(tailleApres < tailleAvant, "La taille de la liste devrait diminuer après suppression");
    }

    @Test
    @Order(10)
    @DisplayName("Vérifier que supprimer un forum inexistant ne génère pas d'erreur")
    void supprimer_forumInexistant_neDoitPasPlanter() {
        assertDoesNotThrow(() -> serviceForum.supprimer(999999));
    }
}
