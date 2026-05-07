package tn.esprit.services.event;

import tn.esprit.entities.event.Rating;
import java.sql.SQLException;
import java.util.List;

public interface IRatingService {
    void ajouter(Rating rating) throws SQLException;
    void modifier(Rating rating) throws SQLException;
    void supprimer(int id) throws SQLException;
    List<Rating> recuperer() throws SQLException;
    double getAverageRatingByEvent(int eventId) throws SQLException;
    List<Rating> getRatingsByEvent(int eventId) throws SQLException;
}
