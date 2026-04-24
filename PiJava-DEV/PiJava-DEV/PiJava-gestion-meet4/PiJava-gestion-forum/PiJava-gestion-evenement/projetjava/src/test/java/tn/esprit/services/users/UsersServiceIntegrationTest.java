package tn.esprit.services.users;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import tn.esprit.entities.users.Users;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UsersServiceIntegrationTest {

    private UsersService service;

    @BeforeEach
    void setUp() {
        service = new UsersService();
    }

    @Test
    @Order(1)
    void testAjouterUtilisateur() throws SQLException {
        Users user = new Users(
            "ajout.test@example.com",
            "HashedPassword123",
            "Ajout",
            "TestUser",
            "Developer",
            "Senior",
            "etudiant",
            "ACTIF"
        );
        
        service.ajouter(user);
        
        List<Users> utilisateurs = service.recuperer();
        assertFalse(utilisateurs.isEmpty(), "Users list should not be empty after adding");
        assertTrue(
            utilisateurs.stream()
                .anyMatch(u -> u.getEmail().equals("ajout.test@example.com")),
            "Added user should be found in list"
        );
    }

    @Test
    @Order(2)
    void testModifierUtilisateur() throws SQLException {
        // Create a user to modify
        Users newUser = new Users(
            "modif.test@example.com",
            "HashedPassword123",
            "Original",
            "Name",
            "Developer",
            "Senior",
            "etudiant",
            "ACTIF"
        );
        
        service.ajouter(newUser);
        Users added = service.findByEmail("modif.test@example.com");
        assertNotNull(added, "User should be created before modification");
        
        // Modify the user
        added.setFirstName("Modified");
        added.setLastName("Updated");
        service.modifier(added);
        
        // Verify modification
        List<Users> updated = service.recuperer();
        boolean trouve = updated.stream()
            .anyMatch(u -> u.getFirstName().equals("Modified") && u.getEmail().equals("modif.test@example.com"));
        assertTrue(trouve, "Modified user should have updated first name");
    }

    @Test
    @Order(3)
    void testSupprimerUtilisateur() throws SQLException {
        // Create a user to delete
        Users userToDelete = new Users(
            "suppr.test@example.com",
            "HashedPassword123",
            "ToDelete",
            "User",
            "Developer",
            "Senior",
            "etudiant",
            "ACTIF"
        );
        
        service.ajouter(userToDelete);
        Users added = service.findByEmail("suppr.test@example.com");
        assertNotNull(added, "User should be created before deletion");
        
        int userId = added.getId();
        
        // Delete the user
        service.supprimer(userId);
        
        // Verify deletion
        Users deleted = service.findById(userId);
        assertNull(deleted, "Deleted user should not be found");
    }

    @Test
    @Order(4)
    void testRecupererUtilisateurs() throws SQLException {
        List<Users> utilisateurs = service.recuperer();
        assertNotNull(utilisateurs, "Users list should not be null");
        assertTrue(utilisateurs.size() >= 0, "Users list size should be non-negative");
    }

    @Test
    @Order(5)
    void testFindByEmail() throws SQLException {
        Users user = new Users(
            "findbyemail.test@example.com",
            "HashedPassword123",
            "Email",
            "Test",
            "QA",
            "Junior",
            "admin",
            "ACTIF"
        );
        
        service.ajouter(user);
        
        Users found = service.findByEmail("findbyemail.test@example.com");
        assertNotNull(found, "User should be found by email");
        assertEquals("findbyemail.test@example.com", found.getEmail());
    }

    @Test
    @Order(6)
    void testFindByRole() throws SQLException {
        // Create a test user with specific role
        Users user = new Users(
            "roletest@example.com",
            "HashedPassword123",
            "Role",
            "Test",
            "Analyst",
            "Mid",
            "enseignant",
            "ACTIF"
        );
        
        service.ajouter(user);
        
        List<Users> enseignants = service.findByRole("enseignant");
        assertTrue(
            enseignants.stream()
                .anyMatch(u -> u.getEmail().equals("roletest@example.com")),
            "Added user with role should be found in findByRole results"
        );
    }

    @AfterEach
    void cleanUp() throws SQLException {
        try {
            List<Users> utilisateurs = service.recuperer();
            if (!utilisateurs.isEmpty()) {
                // Clean up test users by email patterns
                utilisateurs.stream()
                    .filter(u -> u.getEmail() != null && (
                        u.getEmail().contains("ajout.test") ||
                        u.getEmail().contains("modif.test") ||
                        u.getEmail().contains("suppr.test") ||
                        u.getEmail().contains("findbyemail.test") ||
                        u.getEmail().contains("roletest")
                    ))
                    .forEach(u -> {
                        try {
                            service.supprimer(u.getId());
                        } catch (SQLException e) {
                            System.err.println("Cleanup error: " + e.getMessage());
                        }
                    });
            }
        } catch (Exception e) {
            System.err.println("CleanUp exception: " + e.getMessage());
        }
    }
}
