package tn.esprit.services.cours;

import tn.esprit.entities.cours.cours_module;
import java.sql.SQLException;
import java.util.List;

public interface ICoursModuleService {
    void ajouter(cours_module m) throws SQLException;
    List<cours_module> recuperer() throws SQLException;
    void modifier(cours_module m) throws SQLException;
    void supprimer(int id) throws SQLException;
    cours_module findById(int id) throws SQLException;
}
