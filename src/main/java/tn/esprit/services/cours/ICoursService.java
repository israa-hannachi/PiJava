package tn.esprit.services.cours;

import tn.esprit.entities.cours.cours;
import java.sql.SQLException;
import java.util.List;

public interface ICoursService {
    void ajouter(cours c) throws SQLException;
    List<cours> recuperer() throws SQLException;
    void modifier(cours c) throws SQLException;
    void supprimer(int id) throws SQLException;
    cours findById(int id) throws SQLException;
    List<cours> findByModuleId(int moduleId) throws SQLException;
}
