package tn.esprit.services.meet;

import tn.esprit.entities.meet.meet;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeetService implements IMeetService {
    private Connection cnx;

    public MeetService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("🚨 MeetService: La connexion à la base de données est nulle !");
        }
    }

    @Override
    public void ajouter(meet m) throws SQLException {
        String req = "INSERT INTO meet (titre, description, date_debut, date_fin, lien_meet, created_at, participant_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, m.getTitre());
        ps.setString(2, m.getDescription());
        ps.setTimestamp(3, m.getDateDebut());
        ps.setTimestamp(4, m.getDateFin());
        ps.setString(5, m.getLienMeet());
        ps.setTimestamp(6, m.getCreatedAt() != null ? m.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
        ps.setInt(7, m.getParticipantId());
        ps.executeUpdate();
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            m.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    public List<meet> recuperer() throws SQLException {
        List<meet> meets = new ArrayList<>();
        String req = "SELECT * FROM meet";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            meets.add(mapRow(rs));
        }
        return meets;
    }

    @Override
    public void modifier(meet m) throws SQLException {
        String req = "UPDATE meet SET titre=?, description=?, date_debut=?, date_fin=?, lien_meet=?, participant_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, m.getTitre());
        ps.setString(2, m.getDescription());
        ps.setTimestamp(3, m.getDateDebut());
        ps.setTimestamp(4, m.getDateFin());
        ps.setString(5, m.getLienMeet());
        ps.setInt(6, m.getParticipantId());
        ps.setInt(7, m.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Supprimer d'abord les meet_participants liés
        String reqMp = "DELETE FROM meet_participants WHERE meet_id=?";
        PreparedStatement psMp = cnx.prepareStatement(reqMp);
        psMp.setInt(1, id);
        psMp.executeUpdate();

        // Supprimer le meet
        String req = "DELETE FROM meet WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public meet findById(int id) throws SQLException {
        String req = "SELECT * FROM meet WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    @Override
    public List<meet> findByParticipantId(int participantId) throws SQLException {
        List<meet> meets = new ArrayList<>();
        String req = "SELECT * FROM meet WHERE participant_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, participantId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            meets.add(mapRow(rs));
        }
        return meets;
    }

    private meet mapRow(ResultSet rs) throws SQLException {
        meet m = new meet();
        m.setId(rs.getInt("id"));
        m.setTitre(rs.getString("titre"));
        m.setDescription(rs.getString("description"));
        m.setDateDebut(rs.getTimestamp("date_debut"));
        m.setDateFin(rs.getTimestamp("date_fin"));
        m.setLienMeet(rs.getString("lien_meet"));
        m.setCreatedAt(rs.getTimestamp("created_at"));
        m.setParticipantId(rs.getInt("participant_id"));
        return m;
    }
}
