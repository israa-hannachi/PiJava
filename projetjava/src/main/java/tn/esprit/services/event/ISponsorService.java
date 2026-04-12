package tn.esprit.services.event;

import tn.esprit.entities.event.Sponsor;
import java.sql.SQLException;
import java.util.List;

public interface ISponsorService {
    void ajouter(Sponsor s) throws SQLException;
    List<Sponsor> recuperer() throws SQLException;
    void modifier(Sponsor s) throws SQLException;
    void supprimer(int id) throws SQLException;
    Sponsor findById(int id) throws SQLException;
}
