package tn.esprit.services.meet;

import tn.esprit.entities.meet.participant;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ParticipantService implements IParticipantsService {
    private Connection cnx;

    public ParticipantService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("🚨 ParticipantService: La connexion à la base de données est nulle !");
        }
    }

    @Override
    public void ajouter(participant p) throws SQLException {
        String req = "INSERT INTO participant (nom, prenom, email, role, created_at, smtp_email, smtp_app_password) VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, p.getNom());
        ps.setString(2, p.getPrenom());
        ps.setString(3, p.getEmail());
        ps.setString(4, p.getRole());
        ps.setTimestamp(5, p.getCreatedAt() != null ? p.getCreatedAt() : new Timestamp(System.currentTimeMillis()));
        ps.setString(6, p.getSmtpEmail());
        ps.setString(7, p.getSmtpAppPassword());
        ps.executeUpdate();
        ResultSet generatedKeys = ps.getGeneratedKeys();
        if (generatedKeys.next()) {
            p.setId(generatedKeys.getInt(1));
        }
    }

    @Override
    public List<participant> recuperer() throws SQLException {
        List<participant> participants = new ArrayList<>();
        String req = "SELECT * FROM participant";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            participant p = new participant();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nom"));
            p.setPrenom(rs.getString("prenom"));
            p.setEmail(rs.getString("email"));
            p.setRole(rs.getString("role"));
            p.setCreatedAt(rs.getTimestamp("created_at"));
            p.setSmtpEmail(rs.getString("smtp_email"));
            p.setSmtpAppPassword(rs.getString("smtp_app_password"));
            participants.add(p);
        }
        return participants;
    }

    @Override
    public void modifier(participant p) throws SQLException {
        String req = "UPDATE participant SET nom=?, prenom=?, email=?, role=?, smtp_email=?, smtp_app_password=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, p.getNom());
        ps.setString(2, p.getPrenom());
        ps.setString(3, p.getEmail());
        ps.setString(4, p.getRole());
        ps.setString(5, p.getSmtpEmail());
        ps.setString(6, p.getSmtpAppPassword());
        ps.setInt(7, p.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Supprimer d'abord les meet_participants liés
        String reqMp = "DELETE FROM meet_participants WHERE participant_id=?";
        PreparedStatement psMp = cnx.prepareStatement(reqMp);
        psMp.setInt(1, id);
        psMp.executeUpdate();

        // Supprimer les meets dont ce participant est l'organisateur
        String reqMeet = "DELETE FROM meet WHERE participant_id=?";
        PreparedStatement psMeet = cnx.prepareStatement(reqMeet);
        psMeet.setInt(1, id);
        psMeet.executeUpdate();

        // Supprimer le participant
        String req = "DELETE FROM participant WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public participant findById(int id) throws SQLException {
        String req = "SELECT * FROM participant WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            participant p = new participant();
            p.setId(rs.getInt("id"));
            p.setNom(rs.getString("nom"));
            p.setPrenom(rs.getString("prenom"));
            p.setEmail(rs.getString("email"));
            p.setRole(rs.getString("role"));
            p.setCreatedAt(rs.getTimestamp("created_at"));
            p.setSmtpEmail(rs.getString("smtp_email"));
            p.setSmtpAppPassword(rs.getString("smtp_app_password"));
            return p;
        }
        return null;
    }
}
