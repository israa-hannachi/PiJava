package tn.esprit.services.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Sponsor;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class SponsorServiceTest {

    private SponsorService sponsorService;
    private EventService eventService;
    private int testEventId = -1;

    @BeforeEach
    void setUp() throws SQLException {
        sponsorService = new SponsorService();
        eventService = new EventService();
        
        // Create a test event to associate sponsors to
        Event event = new Event(0, "Test Event For Sponsor", "Description Test", 
                new Timestamp(System.currentTimeMillis()), 
                new Timestamp(System.currentTimeMillis() + 86400000), 
                100, "image.jpg", "Tech", new BigDecimal("50.00"), "Tunis", "A_VENIR");
        eventService.ajouter(event);
        
        // Retrieve the created event ID
        List<Event> events = eventService.recuperer();
        Event addedEvent = events.stream()
                .filter(e -> "Test Event For Sponsor".equals(e.getTitre()))
                .findFirst().orElse(null);
        if (addedEvent != null) {
            testEventId = addedEvent.getId();
        }
    }

    @Test
    @Order(1)
    void testAjouterSponsor() throws SQLException {
        assertTrue(testEventId != -1, "Test event should be created");
        Sponsor sponsor = createTestSponsor("Test Sponsor Ajout", "info@ajout.com");
        sponsorService.ajouter(sponsor);
        
        List<Sponsor> sponsors = sponsorService.recuperer();
        assertFalse(sponsors.isEmpty(), "Sponsors list should not be empty after adding");
        assertTrue(sponsors.stream().anyMatch(s -> "Test Sponsor Ajout".equals(s.getNom())), "Added sponsor should be found in list");
    }

    @Test
    @Order(2)
    void testModifierSponsor() throws SQLException {
        Sponsor sponsor = createTestSponsor("Test Sponsor Modif", "info@modif.com");
        sponsorService.ajouter(sponsor);
        
        List<Sponsor> sponsors = sponsorService.recuperer();
        Sponsor addedSponsor = sponsors.stream().filter(s -> "Test Sponsor Modif".equals(s.getNom())).findFirst().orElse(null);
        assertNotNull(addedSponsor, "Sponsor should be created before modification");
        
        addedSponsor.setNom("Test Sponsor Modif Updated");
        addedSponsor.setMontant(new BigDecimal("1500.00"));
        sponsorService.modifier(addedSponsor);
        
        Sponsor updatedSponsor = sponsorService.findById(addedSponsor.getId());
        assertNotNull(updatedSponsor, "Sponsor should exist");
        assertEquals("Test Sponsor Modif Updated", updatedSponsor.getNom(), "Sponsor name should be updated");
        assertEquals(new BigDecimal("1500.00"), updatedSponsor.getMontant(), "Sponsor amount should be updated");
    }

    @Test
    @Order(3)
    void testSupprimerSponsor() throws SQLException {
        Sponsor sponsor = createTestSponsor("Test Sponsor Suppr", "info@suppr.com");
        sponsorService.ajouter(sponsor);
        
        List<Sponsor> sponsors = sponsorService.recuperer();
        Sponsor addedSponsor = sponsors.stream().filter(s -> "Test Sponsor Suppr".equals(s.getNom())).findFirst().orElse(null);
        assertNotNull(addedSponsor, "Sponsor should be created before deletion");
        
        int sponsorId = addedSponsor.getId();
        sponsorService.supprimer(sponsorId);
        
        Sponsor deletedSponsor = sponsorService.findById(sponsorId);
        assertNull(deletedSponsor, "Deleted sponsor should not be found");
    }

    @Test
    @Order(4)
    void testRecupererSponsors() throws SQLException {
        List<Sponsor> sponsors = sponsorService.recuperer();
        assertNotNull(sponsors, "Sponsors list should not be null");
    }

    @Test
    @Order(5)
    void testFindById() throws SQLException {
        Sponsor sponsor = createTestSponsor("Test Sponsor Find", "info@find.com");
        sponsorService.ajouter(sponsor);
        
        List<Sponsor> sponsors = sponsorService.recuperer();
        Sponsor addedSponsor = sponsors.stream().filter(s -> "Test Sponsor Find".equals(s.getNom())).findFirst().orElse(null);
        assertNotNull(addedSponsor, "Sponsor should exist");
        
        Sponsor found = sponsorService.findById(addedSponsor.getId());
        assertNotNull(found, "Sponsor should be found by id");
        assertEquals("Test Sponsor Find", found.getNom());
    }

    private Sponsor createTestSponsor(String nom, String email) {
        Sponsor s = new Sponsor(0, testEventId, nom, "GOLD", new BigDecimal("1000.00"), 
                new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 86400000), "ACTIF");
        s.setContactEmail(email);
        return s;
    }

    @AfterEach
    void cleanUp() {
        try {
            // Clean up Test Sponsors
            List<Sponsor> sponsors = sponsorService.recuperer();
            if (sponsors != null && !sponsors.isEmpty()) {
                sponsors.stream()
                    .filter(s -> s.getNom() != null && s.getNom().contains("Test Sponsor"))
                    .forEach(s -> {
                        try {
                            sponsorService.supprimer(s.getId());
                        } catch (SQLException ex) {
                            System.err.println("Cleanup Sponsor error: " + ex.getMessage());
                        }
                    });
            }

            // Clean up Test Event
            if (testEventId != -1) {
                eventService.supprimer(testEventId);
            }
        } catch (Exception e) {
            System.err.println("CleanUp exception: " + e.getMessage());
        }
    }
}
