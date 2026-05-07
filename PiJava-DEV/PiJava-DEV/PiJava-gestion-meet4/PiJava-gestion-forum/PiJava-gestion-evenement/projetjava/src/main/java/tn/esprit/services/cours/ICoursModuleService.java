package tn.esprit.services.cours;

import tn.esprit.entities.cours.Cours_Module;
import java.sql.SQLException;
import java.util.List;

public interface ICoursModuleService {
    void ajouter(Cours_Module m) throws SQLException;
    List<Cours_Module> recuperer() throws SQLException;
    void modifier(Cours_Module m) throws SQLException;
    void supprimer(int id) throws SQLException;
    Cours_Module findById(int id) throws SQLException;
}
