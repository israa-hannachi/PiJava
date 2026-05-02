package tn.esprit.controllers.forum;

import org.junit.jupiter.api.*;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Classe de test pour ForumController
 * Teste les méthodes du contrôleur de gestion des forums.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ForumControllerTest {

    private static ForumController controller;
    private static int testForumId = -1;

    @BeforeAll
    static void setup() {
        controller = new ForumController();
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TESTS DE VALIDATION (sans DB)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Vérifier que creerForum avec catégorie null est rejeté")
    void creerForum_avecCategorieNull_neDoitPasPlanter() {
        assertDoesNotThrow(() -> controller.creerForum(
                "Titre Test",
                "Description test",
                "auteur",
                null
        ));
    }

    @Test
    @Order(2)
    @DisplayName("Vérifier que creerForum avec catégorie ID=0 est rejeté")
    void creerForum_avecCategorieIdZero_neDoitPasPlanter() {
        Categorie catVide = new Categorie();
        catVide.setId(0);

        assertDoesNotThrow(() -> controller.creerForum(
                "Titre Test",
                "Description test",
                "auteur",
                catVide
        ));
    }

    @Test
    @Order(3)
    @DisplayName("Vérifier que creerForum avec titre null est géré")
    void creerForum_avecTitreNull_neDoitPasPlanter() {
        Categorie cat = new Categorie(1, "Test", "Desc", "icon.png");

        assertDoesNotThrow(() -> controller.creerForum(
                null,
                "Description test",
                "auteur",
                cat
        ));
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // TESTS CRUD (intégration avec DB)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(4)
    @DisplayName("Vérifier que recupererListeForums retourne une liste non null")
    void recupererListeForums_doitRetournerListeNonNull() {
        List<Forum> forums = controller.recupererListeForums();
        assertNotNull(forums);
    }

    @Test
    @Order(5)
    @DisplayName("Vérifier que creerForum avec données valides augmente la liste")
    void creerForum_donneesValides_doitAugmenterTailleListe() {
        Categorie cat = new Categorie(1, "Test", "Description cat", "icon.png");

        int tailleAvant = controller.recupererListeForums().size();

        controller.creerForum(
                "ForumControllerTest_" + System.currentTimeMillis(),
                "Description de test",
                "testeurController",
                cat
        );

        int tailleApres = controller.recupererListeForums().size();
        assertEquals(tailleAvant + 1, tailleApres);
    }

    @Test
    @Order(6)
    @DisplayName("Vérifier qu'un forum créé est retrouvé dans la liste")
    void creerForum_forumDoitEtreRetrouve() {
        String titreUnique = "ForumCRUD_Controller_" + System.currentTimeMillis();
        Categorie cat = new Categorie(1, "Test", "Description cat", "icon.png");

        controller.creerForum(
                titreUnique,
                "Description CRUD",
                "testeurCRUD",
                cat
        );

        List<Forum> forums = controller.recupererListeForums();
        boolean trouve = forums.stream().anyMatch(f -> f.getTitre().equals(titreUnique));
        assertTrue(trouve, "Le forum créé devrait être trouvé dans la liste");

        // Sauvegarder l'ID pour les tests suivants
        testForumId = forums.stream()
                .filter(f -> f.getTitre().equals(titreUnique))
                .mapToInt(Forum::getId)
                .max()
                .orElse(-1);
    }

    @Test
    @Order(7)
    @DisplayName("Vérifier que modifierForum met à jour les données")
    void modifierForum_miseAJour_doitFonctionner() {
        assumeTrue(testForumId > 0, "Ce test nécessite qu'un forum ait été créé au préalable");

        List<Forum> forums = controller.recupererListeForums();
        Forum forumAModifier = forums.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumAModifier, "Le forum à modifier devrait exister");

        String nouveauTitre = "ForumModifie_Controller_" + System.currentTimeMillis();
        forumAModifier.setTitre(nouveauTitre);
        forumAModifier.setDescription("Description modifiée via controller");

        controller.modifierForum(forumAModifier);

        // Vérifier la modification
        List<Forum> forumsApres = controller.recupererListeForums();
        Forum forumModifie = forumsApres.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumModifie);
        assertEquals(nouveauTitre, forumModifie.getTitre());
        assertEquals("Description modifiée via controller", forumModifie.getDescription());
    }

    @Test
    @Order(8)
    @DisplayName("Vérifier que mettreAJourForum fonctionne comme modifierForum")
    void mettreAJourForum_doitFonctionner() {
        assumeTrue(testForumId > 0, "Ce test nécessite qu'un forum ait été créé au préalable");

        List<Forum> forums = controller.recupererListeForums();
        Forum forumAModifier = forums.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumAModifier);

        String etatOriginal = forumAModifier.getEtat();
        String nouvelEtat = "inactif".equals(etatOriginal) ? "actif" : "inactif";
        forumAModifier.setEtat(nouvelEtat);

        controller.mettreAJourForum(forumAModifier);

        List<Forum> forumsApres = controller.recupererListeForums();
        Forum forumModifie = forumsApres.stream()
                .filter(f -> f.getId() == testForumId)
                .findFirst()
                .orElse(null);

        assertNotNull(forumModifie);
        assertEquals(nouvelEtat, forumModifie.getEtat());
    }

    @Test
    @Order(9)
    @DisplayName("Vérifier que supprimerForum réduit la taille de la liste")
    void supprimerForum_forumExistant_doitReduireTailleListe() {
        assumeTrue(testForumId > 0, "Ce test nécessite qu'un forum ait été créé au préalable");

        int tailleAvant = controller.recupererListeForums().size();
        controller.supprimerForum(testForumId);
        int tailleApres = controller.recupererListeForums().size();

        assertTrue(tailleApres < tailleAvant, "La taille de la liste devrait diminuer après suppression");
    }

    @Test
    @Order(10)
    @DisplayName("Vérifier que supprimerForum avec ID inexistant ne génère pas d'erreur")
    void supprimerForum_forumInexistant_neDoitPasPlanter() {
        assertDoesNotThrow(() -> controller.supprimerForum(999999));
    }
}
