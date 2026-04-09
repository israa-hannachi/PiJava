package tn.esprit.services.event;

import tn.esprit.entities.event.Registration;
import java.sql.SQLException;
import java.util.List;

public interface IRegistrationService {
    void ajouter(Registration r) throws SQLException;
    List<Registration> recuperer() throws SQLException;
    void modifier(Registration r) throws SQLException;
    void supprimer(int id) throws SQLException;
    Registration findById(int id) throws SQLException;
}
