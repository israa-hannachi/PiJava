package tn.esprit.services.event;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import tn.esprit.entities.event.Event;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(OrderAnnotation.class)
class EventServiceTest {

    private EventService service;

    @BeforeEach
    void setUp() {
        service = new EventService();
    }

    @Test
    @Order(1)
    void testAjouterEvent() throws SQLException {
        Event event = createTestEvent("Test Event Ajout");
        service.ajouter(event);
        
        List<Event> events = service.recuperer();
        assertFalse(events.isEmpty(), "Events list should not be empty after adding");
        assertTrue(events.stream().anyMatch(e -> "Test Event Ajout".equals(e.getTitre())), "Added event should be found in list");
    }

    @Test
    @Order(2)
    void testModifierEvent() throws SQLException {
        Event event = createTestEvent("Test Event Modif");
        service.ajouter(event);
        
        // Find the added event
        List<Event> events = service.recuperer();
        Event addedEvent = events.stream().filter(e -> "Test Event Modif".equals(e.getTitre())).findFirst().orElse(null);
        assertNotNull(addedEvent, "Event should be created before modification");
        
        // Modify the event
        addedEvent.setTitre("Test Event Modif Updated");
        addedEvent.setPrix(new BigDecimal("150.00"));
        service.modifier(addedEvent);
        
        // Verify modification
        Event updatedEvent = service.findById(addedEvent.getId());
        assertNotNull(updatedEvent, "Event should exist");
        assertEquals("Test Event Modif Updated", updatedEvent.getTitre(), "Event title should be updated");
        assertEquals(new BigDecimal("150.00"), updatedEvent.getPrix(), "Event price should be updated");
    }

    @Test
    @Order(3)
    void testSupprimerEvent() throws SQLException {
        Event event = createTestEvent("Test Event Suppr");
        service.ajouter(event);
        
        List<Event> events = service.recuperer();
        Event addedEvent = events.stream().filter(e -> "Test Event Suppr".equals(e.getTitre())).findFirst().orElse(null);
        assertNotNull(addedEvent, "Event should be created before deletion");
        
        int eventId = addedEvent.getId();
        
        // Delete the event
        service.supprimer(eventId);
        
        // Verify deletion
        Event deletedEvent = service.findById(eventId);
        assertNull(deletedEvent, "Deleted event should not be found");
    }

    @Test
    @Order(4)
    void testRecupererEvents() throws SQLException {
        List<Event> events = service.recuperer();
        assertNotNull(events, "Events list should not be null");
        assertTrue(events.size() >= 0, "Events list size should be non-negative");
    }
    
    @Test
    @Order(5)
    void testFindById() throws SQLException {
        Event event = createTestEvent("Test Event Find");
        service.ajouter(event);
        
        List<Event> events = service.recuperer();
        Event addedEvent = events.stream().filter(e -> "Test Event Find".equals(e.getTitre())).findFirst().orElse(null);
        assertNotNull(addedEvent, "Event should exist");
        
        Event found = service.findById(addedEvent.getId());
        assertNotNull(found, "Event should be found by id");
        assertEquals("Test Event Find", found.getTitre());
    }

    private Event createTestEvent(String titre) {
        Event e = new Event(0, titre, "Description Test", 
                new Timestamp(System.currentTimeMillis()), 
                new Timestamp(System.currentTimeMillis() + 86400000), 
                100, "image.jpg", "Tech", new BigDecimal("50.00"), "Tunis", "A_VENIR");
        e.setRecurring(false);
        e.setTimeZone("UTC");
        return e;
    }

    @AfterEach
    void cleanUp() {
        try {
            List<Event> events = service.recuperer();
            if (events != null && !events.isEmpty()) {
                events.stream()
                    .filter(e -> e.getTitre() != null && e.getTitre().startsWith("Test Event"))
                    .forEach(e -> {
                        try {
                            service.supprimer(e.getId());
                        } catch (SQLException ex) {
                            System.err.println("Cleanup error: " + ex.getMessage());
                        }
                    });
            }
        } catch (Exception e) {
            System.err.println("CleanUp exception: " + e.getMessage());
        }
    }
}
