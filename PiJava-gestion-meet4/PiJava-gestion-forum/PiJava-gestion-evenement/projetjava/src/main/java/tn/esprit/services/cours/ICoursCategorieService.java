package tn.esprit.services.cours;

import tn.esprit.entities.cours.Cours_Categorie;
import java.sql.SQLException;
import java.util.List;

public interface ICoursCategorieService {
    void ajouter(Cours_Categorie c) throws SQLException;
    List<Cours_Categorie> recuperer() throws SQLException;
    void modifier(Cours_Categorie c) throws SQLException;
    void supprimer(int id) throws SQLException;
    Cours_Categorie findById(int id) throws SQLException;
}
