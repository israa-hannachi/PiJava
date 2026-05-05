package tn.esprit.services.cours;

import tn.esprit.entities.cours.Cours;
import java.sql.SQLException;
import java.util.List;

public interface ICoursService {
    void ajouter(Cours c) throws SQLException;
    List<Cours> recuperer() throws SQLException;
    void modifier(Cours c) throws SQLException;
    void supprimer(int id) throws SQLException;
    Cours findById(int id) throws SQLException;
    List<Cours> findByModuleId(int moduleId) throws SQLException;
}
