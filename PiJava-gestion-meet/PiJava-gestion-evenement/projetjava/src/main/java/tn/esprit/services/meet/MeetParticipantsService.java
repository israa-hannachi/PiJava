package tn.esprit.services.meet;

import tn.esprit.entities.meet.meet_participants;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeetParticipantsService implements IMeetParticipantsService {
    private Connection cnx;

    public MeetParticipantsService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("🚨 MeetParticipantsService: La connexion à la base de données est nulle !");
        }
    }

    @Override
    public void ajouter(meet_participants mp) throws SQLException {
        if (exists(mp.getMeetId(), mp.getParticipantId())) {
            System.out.println("⚠️ Ce participant est déjà inscrit à ce meet.");
            return;
        }
        String req = "INSERT INTO meet_participants (meet_id, participant_id) VALUES (?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, mp.getMeetId());
        ps.setInt(2, mp.getParticipantId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int meetId, int participantId) throws SQLException {
        String req = "DELETE FROM meet_participants WHERE meet_id=? AND participant_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, meetId);
        ps.setInt(2, participantId);
        ps.executeUpdate();
    }

    @Override
    public List<meet_participants> findByMeetId(int meetId) throws SQLException {
        List<meet_participants> list = new ArrayList<>();
        String req = "SELECT * FROM meet_participants WHERE meet_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, meetId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new meet_participants(rs.getInt("meet_id"), rs.getInt("participant_id")));
        }
        return list;
    }

    @Override
    public List<meet_participants> findByParticipantId(int participantId) throws SQLException {
        List<meet_participants> list = new ArrayList<>();
        String req = "SELECT * FROM meet_participants WHERE participant_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, participantId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(new meet_participants(rs.getInt("meet_id"), rs.getInt("participant_id")));
        }
        return list;
    }

    @Override
    public boolean exists(int meetId, int participantId) throws SQLException {
        String req = "SELECT COUNT(*) FROM meet_participants WHERE meet_id=? AND participant_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, meetId);
        ps.setInt(2, participantId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1) > 0;
        }
        return false;
    }
}
