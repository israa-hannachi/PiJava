package tn.esprit.services.event;

import tn.esprit.entities.event.Event;
import java.sql.SQLException;
import java.util.List;

public interface IEventService {
    void ajouter(Event e) throws SQLException;
    List<Event> recuperer() throws SQLException;
    void modifier(Event e) throws SQLException;
    void supprimer(int id) throws SQLException;
    Event findById(int id) throws SQLException;
}
