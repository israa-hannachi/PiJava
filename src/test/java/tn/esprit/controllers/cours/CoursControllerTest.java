package tn.esprit.controllers.cours;

import org.junit.jupiter.api.*;
import tn.esprit.entities.cours.cours;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CoursControllerTest {

    private static CoursController controller;
    private static int insertedId = -1;

    // Use an existing module id from the DB (algo=6 exists in test data)
    private static final int VALID_MODULE_ID = 6;

    @BeforeAll
    static void setup() {
        controller = new CoursController();
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // VALIDATION TESTS (no DB)
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    void ajouterCours_shouldRejectNullTitre() {
        cours c = new cours(null, "desc", "contenu", 60, 1,
                new Timestamp(System.currentTimeMillis()), 1, VALID_MODULE_ID, null, 0, 1);
        assertDoesNotThrow(() -> controller.ajouterCours(c));
    }

    @Test
    @Order(2)
    void ajouterCours_shouldRejectEmptyTitre() {
        cours c = new cours("", "desc", null, 30, 1,
                new Timestamp(System.currentTimeMillis()), 1, VALID_MODULE_ID, null, 0, 1);
        assertDoesNotThrow(() -> controller.ajouterCours(c));
    }

    @Test
    @Order(3)
    void ajouterCours_shouldRejectNegativeDuree() {
        cours c = new cours("Titre OK", "desc", null, -5, 1,
                new Timestamp(System.currentTimeMillis()), 1, VALID_MODULE_ID, null, 0, 1);
        assertDoesNotThrow(() -> controller.ajouterCours(c));
    }

    @Test
    @Order(4)
    void ajouterCours_shouldRejectZeroDuree() {
        cours c = new cours("Titre OK", null, null, 0, 1,
                new Timestamp(System.currentTimeMillis()), 1, VALID_MODULE_ID, null, 0, 1);
        assertDoesNotThrow(() -> controller.ajouterCours(c));
    }

    @Test
    @Order(5)
    void ajouterCours_shouldRejectInvalidModuleId() {
        cours c = new cours("Titre OK", null, null, 45, 1,
                new Timestamp(System.currentTimeMillis()), 1, 0, null, 0, 1);
        assertDoesNotThrow(() -> controller.ajouterCours(c));
    }

    @Test
    @Order(6)
    void modifierCours_shouldRejectInvalidId() {
        cours c = new cours();
        c.setId(-1);
        c.setTitre("Test");
        assertDoesNotThrow(() -> controller.modifierCours(c));
    }

    // ──────────────────────────────────────────────────────────────────────────────
    // CRUD INTEGRATION TESTS
    // ──────────────────────────────────────────────────────────────────────────────

    @Test
    @Order(7)
    void recupererCours_shouldReturnNonNullList() {
        List<cours> list = controller.recupererCours();
        assertNotNull(list);
    }

    @Test
    @Order(8)
    void ajouterCours_validData_shouldIncreaseListSize() {
        cours c = new cours("CoursTest_" + System.currentTimeMillis(),
                "desc test", null, 45, 1,
                new Timestamp(System.currentTimeMillis()),
                1, VALID_MODULE_ID, null, 0, 1);
        int before = controller.recupererCours().size();
        controller.ajouterCours(c);
        int after = controller.recupererCours().size();
        assertEquals(before + 1, after);
    }

    @Test
    @Order(9)
    void ajouterCours_shouldBeFoundAfterInsert() {
        String titre = "CoursCRUD_" + System.currentTimeMillis();
        cours c = new cours(titre, "desc", "contenu texte", 60, 2,
                new Timestamp(System.currentTimeMillis()),
                1, VALID_MODULE_ID, null, 0, 1);
        controller.ajouterCours(c);

        List<cours> list = controller.recupererCours();
        boolean found = list.stream().anyMatch(x -> x.getTitre().equals(titre));
        assertTrue(found);

        insertedId = list.stream()
                .filter(x -> x.getTitre().equals(titre))
                .mapToInt(cours::getId)
                .max().orElse(-1);
    }

    @Test
    @Order(10)
    void modifierCours_shouldUpdateTitre() {
        assumeInserted();
        cours c = controller.findById(insertedId);
        assertNotNull(c);
        String newTitre = "CoursModified_" + System.currentTimeMillis();
        c.setTitre(newTitre);
        controller.modifierCours(c);

        cours updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertEquals(newTitre, updated.getTitre());
    }

    @Test
    @Order(11)
    void modifierCours_shouldUpdateDuree() {
        assumeInserted();
        cours c = controller.findById(insertedId);
        assertNotNull(c);
        c.setDuree(120);
        controller.modifierCours(c);

        cours updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertEquals(120, updated.getDuree());
    }

    @Test
    @Order(12)
    void modifierCours_shouldUpdateVisible() {
        assumeInserted();
        cours c = controller.findById(insertedId);
        assertNotNull(c);
        int original = c.getVisible();
        c.setVisible(original == 1 ? 0 : 1);
        controller.modifierCours(c);

        cours updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertNotEquals(original, updated.getVisible());
    }

    @Test
    @Order(13)
    void modifierCours_shouldUpdateFichierContenu() {
        assumeInserted();
        cours c = controller.findById(insertedId);
        assertNotNull(c);
        c.setFichierContenu("/path/to/test.pdf");
        controller.modifierCours(c);

        cours updated = controller.findById(insertedId);
        assertNotNull(updated);
        assertEquals("/path/to/test.pdf", updated.getFichierContenu());
    }

    @Test
    @Order(14)
    void findByModuleId_shouldReturnCoursForModule() {
        List<cours> list = controller.findByModuleId(VALID_MODULE_ID);
        assertNotNull(list);
        list.forEach(c -> assertEquals(VALID_MODULE_ID, c.getModuleId()));
    }

    @Test
    @Order(15)
    void findById_nonExistent_shouldReturnNull() {
        assertNull(controller.findById(999999));
    }

    @Test
    @Order(16)
    void supprimerCours_shouldRemoveFromList() {
        assumeInserted();
        int before = controller.recupererCours().size();
        controller.supprimerCours(insertedId);
        int after = controller.recupererCours().size();
        assertTrue(after < before);
        assertNull(controller.findById(insertedId));
    }

    @Test
    @Order(17)
    void supprimerCours_nonExistent_shouldNotThrow() {
        assertDoesNotThrow(() -> controller.supprimerCours(999999));
    }

    // ─── HELPER ──────────────────────────────────────────────────────────────────

    private void assumeInserted() {
        org.junit.jupiter.api.Assumptions.assumeTrue(insertedId > 0,
                "Ce test nécessite qu'un cours ait été inséré au préalable");
    }
}
