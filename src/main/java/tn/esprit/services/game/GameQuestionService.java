package tn.esprit.services.game;

import tn.esprit.entities.game.Game_Question;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GameQuestionService {

    private Connection cn;

    public GameQuestionService() {
        cn = MyDatabase.getInstance().getCnx();
    }

    // ✅ Ajouter une question
    public void ajouter(Game_Question q) throws SQLException {
        String sql = "INSERT INTO game_question(question_text, option1, option2, option3, option4, correct_answer, game_id) VALUES (?,?,?,?,?,?,?)";
        PreparedStatement ps = cn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

        ps.setString(1, q.getQuestionText());
        ps.setString(2, q.getOption1());
        ps.setString(3, q.getOption2());
        ps.setString(4, q.getOption3());
        ps.setString(5, q.getOption4());
        ps.setString(6, q.getCorrectAnswer());
        ps.setInt(7, q.getGameId());

        ps.executeUpdate();

        ResultSet rs = ps.getGeneratedKeys();
        if (rs.next()) {
            q.setId(rs.getInt(1));
        }
    }

    // ✅ Récupérer toutes les questions
    public List<Game_Question> recuperer() throws SQLException {
        List<Game_Question> list = new ArrayList<>();
        String sql = "SELECT * FROM game_question";
        Statement st = cn.createStatement();
        ResultSet rs = st.executeQuery(sql);

        while (rs.next()) {
            list.add(new Game_Question(
                    rs.getInt("id"),
                    rs.getString("question_text"),
                    rs.getString("option1"),
                    rs.getString("option2"),
                    rs.getString("option3"),
                    rs.getString("option4"),
                    rs.getString("correct_answer"),
                    rs.getInt("game_id")
            ));
        }
        return list;
    }

    // ✅ Récupérer les questions d'un jeu spécifique
    public List<Game_Question> recupererParGame(int gameId) throws SQLException {
        List<Game_Question> list = new ArrayList<>();
        String sql = "SELECT * FROM game_question WHERE game_id = ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, gameId);
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Game_Question(
                    rs.getInt("id"),
                    rs.getString("question_text"),
                    rs.getString("option1"),
                    rs.getString("option2"),
                    rs.getString("option3"),
                    rs.getString("option4"),
                    rs.getString("correct_answer"),
                    rs.getInt("game_id")
            ));
        }
        return list;
    }

    // ✅ Modifier une question
    public void modifier(Game_Question q) throws SQLException {
        String sql = "UPDATE game_question SET question_text=?, option1=?, option2=?, option3=?, option4=?, correct_answer=? WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);

        ps.setString(1, q.getQuestionText());
        ps.setString(2, q.getOption1());
        ps.setString(3, q.getOption2());
        ps.setString(4, q.getOption3());
        ps.setString(5, q.getOption4());
        ps.setString(6, q.getCorrectAnswer());
        ps.setInt(7, q.getId());

        ps.executeUpdate();
    }

    // ✅ Supprimer une question
    public void supprimer(int id) throws SQLException {
        String sql = "DELETE FROM game_question WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    // ✅ Trouver par ID
    public Game_Question findById(int id) throws SQLException {
        String sql = "SELECT * FROM game_question WHERE id=?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new Game_Question(
                    rs.getInt("id"),
                    rs.getString("question_text"),
                    rs.getString("option1"),
                    rs.getString("option2"),
                    rs.getString("option3"),
                    rs.getString("option4"),
                    rs.getString("correct_answer"),
                    rs.getInt("game_id")
            );
        }
        return null;
    }

    // ✅ Rechercher par texte
    public List<Game_Question> rechercherParTexte(String texte) throws SQLException {
        List<Game_Question> list = new ArrayList<>();
        String sql = "SELECT * FROM game_question WHERE question_text LIKE ?";
        PreparedStatement ps = cn.prepareStatement(sql);
        ps.setString(1, "%" + texte + "%");
        ResultSet rs = ps.executeQuery();

        while (rs.next()) {
            list.add(new Game_Question(
                    rs.getInt("id"),
                    rs.getString("question_text"),
                    rs.getString("option1"),
                    rs.getString("option2"),
                    rs.getString("option3"),
                    rs.getString("option4"),
                    rs.getString("correct_answer"),
                    rs.getInt("game_id")
            ));
        }
        return list;
    }
}