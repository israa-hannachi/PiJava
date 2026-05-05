package tn.esprit.controllers.event;

import tn.esprit.entities.event.Event;
import tn.esprit.services.event.EventService;

import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class EventController {
    private EventService eventService;

    public EventController() {
        eventService = new EventService();
    }

    public void ajouterEvent(Event e) {
        try {
            if (e.getTitre() == null || e.getTitre().trim().isEmpty()) {
                System.out.println("Erreur : Le titre est obligatoire !");
                return;
            }
            if (e.getCapacite() <= 0) {
                System.out.println("Erreur : La capacité doit être positive !");
                return;
            }
            eventService.ajouter(e);
            System.out.println("Event ajouté avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur lors de l'ajout de l'event : " + ex.getMessage());
        }
    }

    public List<Event> recupererEvents() {
        try {
            return eventService.recuperer();
        } catch (SQLException e) {
            System.out.println("Erreur lors de la récupération des events : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void modifierEvent(Event e) {
        try {
            if (e.getId() <= 0) {
                System.out.println("Erreur : ID invalide !");
                return;
            }
            eventService.modifier(e);
            System.out.println("Event modifié avec succès !");
        } catch (SQLException ex) {
            System.out.println("Erreur lors de la modification de l'event : " + ex.getMessage());
        }
    }

    public void supprimerEvent(int id) {
        try {
            eventService.supprimer(id);
            System.out.println("Event supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("Erreur lors de la suppression de l'event : " + e.getMessage());
        }
    }

    public Event findById(int id) {
        try {
            return eventService.findById(id);
        } catch (SQLException e) {
            System.out.println("Erreur : " + e.getMessage());
            return null;
        }
    }
}
