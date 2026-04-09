package tn.esprit.services.meet;

import tn.esprit.entities.meet.meet;
import java.sql.SQLException;
import java.util.List;

public interface IMeetService {
    void ajouter(meet m) throws SQLException;
    List<meet> recuperer() throws SQLException;
    void modifier(meet m) throws SQLException;
    void supprimer(int id) throws SQLException;
    meet findById(int id) throws SQLException;
    List<meet> findByParticipantId(int participantId) throws SQLException;
}
