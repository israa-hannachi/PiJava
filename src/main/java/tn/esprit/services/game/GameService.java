package tn.esprit.services.game;

import tn.esprit.entities.game.Game;   // <-- adapte l'import à ton dossier entities/game
import tn.esprit.tools.MyDatabase;
import java.sql.*;
import java.util.*;

public class GameService implements IGameService {
    Connection cn;

    public GameService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    // Ajouter un Game
    public void ajouter(Game g) throws SQLException {
        String sql = "INSERT INTO game(titre, type, niveau, score_max, last_score, avg_score, duration, attempt_number, created_at, course_id) VALUES (?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, g.getTitre());
        ps.setString(2, g.getType());
        ps.setString(3, g.getNiveau());
        ps.setInt(4, g.getScoreMax());
        ps.setInt(5, g.getLastScore());
        ps.setDouble(6, g.getAvgScore());
        ps.setInt(7, g.getDuration());
        ps.setInt(8, g.getAttemptNumber());
        ps.setTimestamp(9, g.getCreatedAt());
        ps.setInt(10, g.getCourseId());

        ps.executeUpdate();
        // 🔑 Récupérer l’ID auto-généré
        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            g.setId(rs.getInt(1)); // ton objet Java reçoit l’ID réel
        }

        System.out.println("Game ajouté avec succès !");
    }

    // Récupérer tous les Games
    public List<Game> recuperer() throws SQLException {
        String sql = "SELECT * FROM game";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);
        List<Game> games = new ArrayList<>();

        while (rs.next()) {
            Game g = new Game(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("type"),
                    rs.getString("niveau"),
                    rs.getInt("score_max"),
                    rs.getInt("last_score"),
                    rs.getDouble("avg_score"),
                    rs.getInt("duration"),
                    rs.getInt("attempt_number"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("course_id")
            );
            games.add(g);
        }
        return games;
    }

    // Supprimer un Game par id
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM game WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
        System.out.println("Game supprimé avec succès !");
    }
    public Game findById(int id) throws SQLException {
        String req = "SELECT * FROM game WHERE id = ?";
        PreparedStatement ps = cn.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Game(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("type"),
                    rs.getString("niveau"),
                    rs.getInt("score_max"),
                    rs.getInt("last_score"),
                    rs.getDouble("avg_score"),  // ✅ corrigé
                    rs.getInt("duration"),
                    rs.getInt("attempt_number"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("course_id")
            );
        }
        return null; // aucun jeu trouvé
    }
    // Modifier un Game
    public void modifier(Game g) throws SQLException {
        String sql = "UPDATE game SET titre=?, type=?, niveau=?, score_max=?, last_score=?, avg_score=?, duration=?, attempt_number=?, created_at=?, course_id=? WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);

        ps.setString(1, g.getTitre());
        ps.setString(2, g.getType());
        ps.setString(3, g.getNiveau());
        ps.setInt(4, g.getScoreMax());
        ps.setInt(5, g.getLastScore());
        ps.setDouble(6, g.getAvgScore());
        ps.setInt(7, g.getDuration());
        ps.setInt(8, g.getAttemptNumber());
        ps.setTimestamp(9, g.getCreatedAt());
        ps.setInt(10, g.getCourseId());
        ps.setInt(11, g.getId());

        ps.executeUpdate();
        System.out.println("Game modifié avec succès !");
    }
    public List<Game> rechercherParTitre(String titre) throws SQLException {
        List<Game> games = new ArrayList<>();
        // ✅ Le % permet de chercher même un mot partiel
        String req = "SELECT * FROM game WHERE titre LIKE ?";
        PreparedStatement ps = cn.prepareStatement(req);
        ps.setString(1, "%" + titre + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            games.add(new Game(
                    rs.getInt("id"),
                    rs.getString("titre"),
                    rs.getString("type"),
                    rs.getString("niveau"),
                    rs.getInt("score_max"),
                    rs.getInt("last_score"),
                    rs.getDouble("avg_score"),
                    rs.getInt("duration"),
                    rs.getInt("attempt_number"),
                    rs.getTimestamp("created_at"),
                    rs.getInt("course_id")
            ));
        }
        return games;
    }
}
