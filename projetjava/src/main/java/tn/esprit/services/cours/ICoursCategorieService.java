package tn.esprit.services.cours;

import tn.esprit.entities.cours.cours_categorie;
import java.sql.SQLException;
import java.util.List;

public interface ICoursCategorieService {
    void ajouter(cours_categorie c) throws SQLException;
    List<cours_categorie> recuperer() throws SQLException;
    void modifier(cours_categorie c) throws SQLException;
    void supprimer(int id) throws SQLException;
    cours_categorie findById(int id) throws SQLException;
}
