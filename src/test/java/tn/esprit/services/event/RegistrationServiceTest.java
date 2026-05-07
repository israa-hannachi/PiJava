package tn.esprit.services.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Registration;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class RegistrationServiceTest {

    private RegistrationService registrationService;
    private EventService eventService;
    private int testEventId = -1;

    @BeforeEach
    void setUp() throws SQLException {
        registrationService = new RegistrationService();
        eventService = new EventService();
        
        // Create a test event to associate registrations to
        Event event = new Event(0, "Test Event For Registration", "Description Test", 
                new Timestamp(System.currentTimeMillis()), 
                new Timestamp(System.currentTimeMillis() + 86400000), 
                100, "image.jpg", "Tech", new BigDecimal("50.00"), "Tunis", "A_VENIR");
        eventService.ajouter(event);
        
        // Retrieve the created event ID
        List<Event> events = eventService.recuperer();
        Event addedEvent = events.stream()
                .filter(e -> "Test Event For Registration".equals(e.getTitre()))
                .findFirst().orElse(null);
        if (addedEvent != null) {
            testEventId = addedEvent.getId();
        }
    }

    @Test
    @Order(1)
    void testAjouterRegistration() throws SQLException {
        assertTrue(testEventId != -1, "Test event should be created");
        Registration reg = createTestRegistration("TestVisitor Ajout", "test.ajout@example.com");
        registrationService.ajouter(reg);
        
        List<Registration> registrations = registrationService.recuperer();
        assertFalse(registrations.isEmpty(), "Registrations list should not be empty after adding");
        assertTrue(registrations.stream().anyMatch(r -> "test.ajout@example.com".equals(r.getVisitorEmail())), "Added registration should be found in list");
    }

    @Test
    @Order(2)
    void testModifierRegistration() throws SQLException {
        Registration reg = createTestRegistration("TestVisitor Modif", "test.modif@example.com");
        registrationService.ajouter(reg);
        
        List<Registration> registrations = registrationService.recuperer();
        Registration addedReg = registrations.stream().filter(r -> "test.modif@example.com".equals(r.getVisitorEmail())).findFirst().orElse(null);
        assertNotNull(addedReg, "Registration should be created before modification");
        
        addedReg.setVisitorName("TestVisitor Modif Updated");
        addedReg.setStatut("ANNULE");
        registrationService.modifier(addedReg);
        
        Registration updatedReg = registrationService.findById(addedReg.getId());
        assertNotNull(updatedReg, "Registration should exist");
        assertEquals("TestVisitor Modif Updated", updatedReg.getVisitorName(), "Visitor name should be updated");
        assertEquals("ANNULE", updatedReg.getStatut(), "Statut should be updated");
    }

    @Test
    @Order(3)
    void testSupprimerRegistration() throws SQLException {
        Registration reg = createTestRegistration("TestVisitor Suppr", "test.suppr@example.com");
        registrationService.ajouter(reg);
        
        List<Registration> registrations = registrationService.recuperer();
        Registration addedReg = registrations.stream().filter(r -> "test.suppr@example.com".equals(r.getVisitorEmail())).findFirst().orElse(null);
        assertNotNull(addedReg, "Registration should be created before deletion");
        
        int regId = addedReg.getId();
        registrationService.supprimer(regId);
        
        Registration deletedReg = registrationService.findById(regId);
        assertNull(deletedReg, "Deleted registration should not be found");
    }

    @Test
    @Order(4)
    void testRecupererRegistrations() throws SQLException {
        List<Registration> registrations = registrationService.recuperer();
        assertNotNull(registrations, "Registrations list should not be null");
    }

    @Test
    @Order(5)
    void testFindById() throws SQLException {
        Registration reg = createTestRegistration("TestVisitor Find", "test.find@example.com");
        registrationService.ajouter(reg);
        
        List<Registration> registrations = registrationService.recuperer();
        Registration addedReg = registrations.stream().filter(r -> "test.find@example.com".equals(r.getVisitorEmail())).findFirst().orElse(null);
        assertNotNull(addedReg, "Registration should exist");
        
        Registration found = registrationService.findById(addedReg.getId());
        assertNotNull(found, "Registration should be found by id");
        assertEquals("test.find@example.com", found.getVisitorEmail());
    }

    private Registration createTestRegistration(String visitorName, String visitorEmail) {
        return new Registration(0, testEventId, visitorName, visitorEmail, "CONFIRME", "PAYPAL", new BigDecimal("50.00"), "PAYE");
    }

    @AfterEach
    void cleanUp() {
        try {
            // Clean up Test Registrations
            List<Registration> registrations = registrationService.recuperer();
            if (registrations != null && !registrations.isEmpty()) {
                registrations.stream()
                    .filter(r -> r.getVisitorEmail() != null && r.getVisitorEmail().contains("test."))
                    .forEach(r -> {
                        try {
                            registrationService.supprimer(r.getId());
                        } catch (SQLException ex) {
                            System.err.println("Cleanup Registration error: " + ex.getMessage());
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
