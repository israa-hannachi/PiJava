package tn.esprit.services.game;

import tn.esprit.entities.game.Game;
import java.sql.SQLException;
import java.util.List;

public interface IGameService {
    void ajouter(Game g) throws SQLException;
    List<Game> recuperer() throws SQLException;
    void modifier(Game g) throws SQLException;
    void supprimer(int id) throws SQLException;
    Game findById(int id) throws SQLException;
    List<Game> rechercherParTitre(String titre) throws SQLException;
}