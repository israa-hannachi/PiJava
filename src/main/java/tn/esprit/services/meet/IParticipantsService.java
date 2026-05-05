package tn.esprit.services.meet;

import tn.esprit.entities.meet.participant;
import java.sql.SQLException;
import java.util.List;

public interface IParticipantsService {
    void ajouter(participant p) throws SQLException;
    List<participant> recuperer() throws SQLException;
    void modifier(participant p) throws SQLException;
    void supprimer(int id) throws SQLException;
    participant findById(int id) throws SQLException;
    List<participant> rechercherParNom(String nom) throws SQLException;
    List<participant> filtrerParRole(String role) throws SQLException;
    List<participant> trierParNom(boolean asc) throws SQLException;
}
