package tn.esprit.controllers.cours;

import org.junit.jupiter.api.*;
import tn.esprit.entities.cours.cours_categorie;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoursCategorieControllerTest {

    private static CoursCategorieController controller;
    private static int insertedId = -1;

    @BeforeAll
    static void setup() {
        controller = new CoursCategorieController();
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // VALIDATION TESTS (no DB needed)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void ajouterCategorie_shouldRejectNullNom() {
        cours_categorie cat = new cours_categorie(null, "desc", new Timestamp(System.currentTimeMillis()), 1);
        // Should not throw; controller prints error and returns
        assertDoesNotThrow(() -> controller.ajouterCategorie(cat));
    }

    @Test
    @Order(2)
    void ajouterCategorie_shouldRejectEmptyNom() {
        cours_categorie cat = new cours_categorie("  ", "desc", new Timestamp(System.currentTimeMillis()), 1);
        assertDoesNotThrow(() -> controller.ajouterCategorie(cat));
    }

    @Test
    @Order(3)
    void modifierCategorie_shouldRejectInvalidId() {
        cours_categorie cat = new cours_categorie();
        cat.setId(-1);
        cat.setNom("Test");
        assertDoesNotThrow(() -> controller.modifierCategorie(cat));
    }

    @Test
    @Order(4)
    void modifierCategorie_shouldRejectZeroId() {
        cours_categorie cat = new cours_categorie();
        cat.setId(0);
        cat.setNom("Test");
        assertDoesNotThrow(() -> controller.modifierCategorie(cat));
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // CRUD INTEGRATION TESTS (requires DB connection)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    void recupererCategories_shouldReturnNonNullList() {
        List<cours_categorie> list = controller.recupererCategories();
        assertNotNull(list, "La liste ne doit pas être null");
    }

    @Test
    @Order(6)
    void ajouterCategorie_validData_shouldSucceed() {
        cours_categorie cat = new cours_categorie(
                "CatTest_" + System.currentTimeMillis(),
                "Description test",
                new Timestamp(System.currentTimeMillis()), 1);
        int sizeBefore = controller.recupererCategories().size();
        controller.ajouterCategorie(cat);
        int sizeAfter = controller.recupererCategories().size();
        assertEquals(sizeBefore + 1, sizeAfter, "La taille doit augmenter de 1 après ajout");
    }

    @Test
    @Order(7)
    void ajouterEtRecuperer_shouldFindInsertedCategorie() {
        String nom = "Test_CRUD_" + System.currentTimeMillis();
        cours_categorie cat = new cours_categorie(nom, "desc", new Timestamp(System.currentTimeMillis()), 1);
        controller.ajouterCategorie(cat);

        List<cours_categorie> list = controller.recupererCategories();
        boolean found = list.stream().anyMatch(c -> c.getNom().equals(nom));
        assertTrue(found, "La catégorie ajoutée doit être retrouvée dans la liste");

        // Keep id for later tests
        insertedId = list.stream()
                .filter(c -> c.getNom().equals(nom))
                .mapToInt(cours_categorie::getId)
                .max().orElse(-1);
    }

    @Test
    @Order(8)
    void modifierCategorie_shouldUpdateNom() {
        assumeInsertedIdValid();
        cours_categorie cat = controller.findById(insertedId);
        assertNotNull(cat, "findById doit retourner une catégorie valide");

        String newNom = "Modified_" + System.currentTimeMillis();
        cat.setNom(newNom);
        controller.modifierCategorie(cat);

        cours_categorie updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertEquals(newNom, updated.getNom(), "Le nom doit être mis à jour");
    }

    @Test
    @Order(9)
    void modifierCategorie_shouldToggleActif() {
        assumeInsertedIdValid();
        cours_categorie cat = controller.findById(insertedId);
        assertNotNull(cat);
        int original = cat.getActif();
        cat.setActif(original == 1 ? 0 : 1);
        controller.modifierCategorie(cat);

        cours_categorie updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertNotEquals(original, updated.getActif(), "Le statut actif doit être modifié");
    }

    @Test
    @Order(10)
    void findById_shouldReturnNullForNonExistentId() {
        cours_categorie result = controller.findById(999999);
        assertNull(result, "findById doit retourner null pour un ID inexistant");
    }

    @Test
    @Order(11)
    void supprimerCategorie_shouldRemoveFromList() {
        assumeInsertedIdValid();
        int sizeBefore = controller.recupererCategories().size();
        controller.supprimerCategorie(insertedId);
        int sizeAfter = controller.recupererCategories().size();
        assertEquals(sizeBefore - 1, sizeAfter, "La taille doit diminuer de 1 après suppression");
        assertNull(controller.findById(insertedId), "L'entité supprimée ne doit plus exister");
    }

    @Test
    @Order(12)
    void supprimerCategorie_nonExistentId_shouldNotThrow() {
        assertDoesNotThrow(() -> controller.supprimerCategorie(999999),
                "Supprimer un ID inexistant ne doit pas lever d'exception");
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────────

    private void assumeInsertedIdValid() {
        org.junit.jupiter.api.Assumptions.assumeTrue(insertedId > 0,
                "Ce test nécessite qu'une catégorie ait été insérée au préalable");
    }
}
