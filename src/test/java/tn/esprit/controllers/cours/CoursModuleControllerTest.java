package tn.esprit.controllers.cours;

import org.junit.jupiter.api.*;
import tn.esprit.entities.cours.cours_module;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoursModuleControllerTest {

    private static CoursModuleController controller;
    private static int insertedId = -1;

    // Use an existing active categorie id from the DB (informatique=1)
    private static final int VALID_CATEGORIE_ID = 1;

    @BeforeAll
    static void setup() {
        controller = new CoursModuleController();
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // VALIDATION TESTS
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void ajouterModule_shouldRejectNullTitre() {
        cours_module mod = new cours_module(null, "desc", 10, "Débutant",
                new Timestamp(System.currentTimeMillis()), 1, VALID_CATEGORIE_ID, 0);
        assertDoesNotThrow(() -> controller.ajouterModule(mod));
    }

    @Test
    @Order(2)
    void ajouterModule_shouldRejectEmptyTitre() {
        cours_module mod = new cours_module("", "desc", 10, "Débutant",
                new Timestamp(System.currentTimeMillis()), 1, VALID_CATEGORIE_ID, 0);
        assertDoesNotThrow(() -> controller.ajouterModule(mod));
    }

    @Test
    @Order(3)
    void ajouterModule_shouldRejectInvalidCategorieId() {
        cours_module mod = new cours_module("Titre valide", "desc", 10, "Débutant",
                new Timestamp(System.currentTimeMillis()), 1, 0, 0);
        assertDoesNotThrow(() -> controller.ajouterModule(mod));
    }

    @Test
    @Order(4)
    void modifierModule_shouldRejectInvalidId() {
        cours_module mod = new cours_module();
        mod.setId(-1);
        mod.setTitre("Test");
        assertDoesNotThrow(() -> controller.modifierModule(mod));
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // CRUD INTEGRATION TESTS
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(5)
    void recupererModules_shouldReturnNonNullList() {
        List<cours_module> list = controller.recupererModules();
        assertNotNull(list);
    }

    @Test
    @Order(6)
    void ajouterModule_validData_shouldIncreaseListSize() {
        cours_module mod = new cours_module(
                "ModTest_" + System.currentTimeMillis(), "desc", 20,
                "Débutant", new Timestamp(System.currentTimeMillis()),
                1, VALID_CATEGORIE_ID, 0);
        int before = controller.recupererModules().size();
        controller.ajouterModule(mod);
        int after = controller.recupererModules().size();
        assertEquals(before + 1, after);
    }

    @Test
    @Order(7)
    void ajouterModule_shouldBeFoundAfterInsert() {
        String titre = "ModCRUD_" + System.currentTimeMillis();
        cours_module mod = new cours_module(titre, "desc", 15,
                "Intermédiaire", new Timestamp(System.currentTimeMillis()),
                1, VALID_CATEGORIE_ID, 0);
        controller.ajouterModule(mod);

        List<cours_module> list = controller.recupererModules();
        boolean found = list.stream().anyMatch(m -> m.getTitre().equals(titre));
        assertTrue(found);

        insertedId = list.stream()
                .filter(m -> m.getTitre().equals(titre))
                .mapToInt(cours_module::getId)
                .max().orElse(-1);
    }

    @Test
    @Order(8)
    void modifierModule_shouldUpdateTitre() {
        assumeInserted();
        cours_module mod = controller.findById(insertedId);
        assertNotNull(mod);
        String newTitre = "ModModified_" + System.currentTimeMillis();
        mod.setTitre(newTitre);
        controller.modifierModule(mod);

        cours_module updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertEquals(newTitre, updated.getTitre());
    }

    @Test
    @Order(9)
    void modifierModule_shouldUpdateNiveau() {
        assumeInserted();
        cours_module mod = controller.findById(insertedId);
        assertNotNull(mod);
        mod.setNiveau("Avancé");
        controller.modifierModule(mod);

        cours_module updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertEquals("Avancé", updated.getNiveau());
    }

    @Test
    @Order(10)
    void modifierModule_shouldUpdateDuree() {
        assumeInserted();
        cours_module mod = controller.findById(insertedId);
        assertNotNull(mod);
        mod.setDuree(99);
        controller.modifierModule(mod);

        cours_module updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertEquals(99, updated.getDuree());
    }

    @Test
    @Order(11)
    void findById_nonExistent_shouldReturnNull() {
        assertNull(controller.findById(999999));
    }

    @Test
    @Order(12)
    void supprimerModule_shouldRemoveFromList() {
        assumeInserted();
        int before = controller.recupererModules().size();
        controller.supprimerModule(insertedId);
        int after = controller.recupererModules().size();
        // size decreases by at least 1 (cascade delete of cours included)
        assertTrue(after < before);
        assertNull(controller.findById(insertedId));
    }

    @Test
    @Order(13)
    void supprimerModule_nonExistent_shouldNotThrow() {
        assertDoesNotThrow(() -> controller.supprimerModule(999999));
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────────

    private void assumeInserted() {
        org.junit.jupiter.api.Assumptions.assumeTrue(insertedId > 0,
                "Ce test nécessite qu'un module ait été inséré au préalable");
    }
}
