package tn.esprit.services.meet;

import tn.esprit.entities.meet.Meet;
import java.sql.SQLException;
import java.util.List;

public interface IMeetService {
    void ajouter(Meet m) throws SQLException;
    List<Meet> recuperer() throws SQLException;
    void modifier(Meet m) throws SQLException;
    void supprimer(int id) throws SQLException;
    Meet findById(int id) throws SQLException;
    List<Meet> findByParticipantId(int participantId) throws SQLException;
    List<Meet> rechercherParTitre(String kw) throws SQLException;
    List<Meet> trierParTitre(boolean asc) throws SQLException;
    List<Meet> trierParDateDebut(boolean asc) throws SQLException;
    List<Meet> filtrerParOrganisateur(int participantId) throws SQLException;
}
