package tn.esprit.services.meet;

import tn.esprit.entities.meet.Meet;
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
    public void ajouter(Meet m) throws SQLException {
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
    public List<Meet> recuperer() throws SQLException {
        List<Meet> meets = new ArrayList<>();
        String req = "SELECT * FROM meet";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            meets.add(mapRow(rs));
        }
        return meets;
    }

    @Override
    public void modifier(Meet m) throws SQLException {
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
    public Meet findById(int id) throws SQLException {
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
    public List<Meet> findByParticipantId(int participantId) throws SQLException {
        List<Meet> meets = new ArrayList<>();
        String req = "SELECT * FROM meet WHERE participant_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, participantId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            meets.add(mapRow(rs));
        }
        return meets;
    }

    private Meet mapRow(ResultSet rs) throws SQLException {
        Meet m = new Meet();
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

    @Override
    public List<Meet> rechercherParTitre(String kw) throws SQLException {
        List<Meet> meets = new ArrayList<>();
        String req = "SELECT * FROM meet WHERE titre LIKE ? OR description LIKE ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, "%" + kw + "%");
        ps.setString(2, "%" + kw + "%");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            meets.add(mapRow(rs));
        }
        return meets;
    }

    @Override
    public List<Meet> trierParTitre(boolean asc) throws SQLException {
        List<Meet> meets = new ArrayList<>();
        String order = asc ? "ASC" : "DESC";
        String req = "SELECT * FROM meet ORDER BY titre " + order;
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            meets.add(mapRow(rs));
        }
        return meets;
    }

    @Override
    public List<Meet> trierParDateDebut(boolean asc) throws SQLException {
        List<Meet> meets = new ArrayList<>();
        String order = asc ? "ASC" : "DESC";
        String req = "SELECT * FROM meet ORDER BY date_debut " + order;
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            meets.add(mapRow(rs));
        }
        return meets;
    }

    @Override
    public List<Meet> filtrerParOrganisateur(int participantId) throws SQLException {
        return findByParticipantId(participantId);
    }

    // ═══════════════════════════════════════════════════════════════════════
    // STATISTIQUES DASHBOARD
    // ═══════════════════════════════════════════════════════════════════════

    public int countMeets() throws SQLException {
        String req = "SELECT COUNT(*) FROM meet";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public int countMeetsByPeriod(Timestamp from, Timestamp to) throws SQLException {
        String req = "SELECT COUNT(*) FROM meet WHERE date_debut >= ? AND date_debut <= ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setTimestamp(1, from);
        ps.setTimestamp(2, to);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public int countMeetsByStatus(String status) throws SQLException {
        Timestamp now = new Timestamp(System.currentTimeMillis());
        String req;
        PreparedStatement ps;

        switch (status) {
            case "upcoming":
                req = "SELECT COUNT(*) FROM meet WHERE date_debut > ?";
                ps = cnx.prepareStatement(req);
                ps.setTimestamp(1, now);
                break;
            case "current":
                req = "SELECT COUNT(*) FROM meet WHERE date_debut <= ? AND date_fin >= ?";
                ps = cnx.prepareStatement(req);
                ps.setTimestamp(1, now);
                ps.setTimestamp(2, now);
                break;
            case "completed":
                req = "SELECT COUNT(*) FROM meet WHERE date_fin < ?";
                ps = cnx.prepareStatement(req);
                ps.setTimestamp(1, now);
                break;
            default:
                return 0;
        }
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public double getAverageDurationMinutes() throws SQLException {
        String req = "SELECT AVG(TIMESTAMPDIFF(MINUTE, date_debut, date_fin)) FROM meet WHERE date_fin > date_debut";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            return rs.getDouble(1);
        }
        return 0.0;
    }

    public int getTotalDurationHours() throws SQLException {
        String req = "SELECT SUM(TIMESTAMPDIFF(MINUTE, date_debut, date_fin)) FROM meet WHERE date_fin > date_debut";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            return rs.getInt(1) / 60; // Convert to hours
        }
        return 0;
    }

    public List<Meet> getMeetsByPeriod(Timestamp from, Timestamp to) throws SQLException {
        List<Meet> meets = new ArrayList<>();
        String req = "SELECT * FROM meet WHERE date_debut >= ? AND date_debut <= ? ORDER BY date_debut";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setTimestamp(1, from);
        ps.setTimestamp(2, to);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            meets.add(mapRow(rs));
        }
        return meets;
    }

    public int countParticipantsTotal() throws SQLException {
        String req = "SELECT COUNT(DISTINCT participant_id) FROM meet_participants";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            return rs.getInt(1);
        }
        return 0;
    }

    public double getAverageParticipantsPerMeet() throws SQLException {
        String req = "SELECT AVG(cnt) FROM (SELECT meet_id, COUNT(*) as cnt FROM meet_participants GROUP BY meet_id) as avg_table";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        if (rs.next()) {
            return rs.getDouble(1);
        }
        return 0.0;
    }

    public Meet getNextMeet() throws SQLException {
        String req = "SELECT * FROM meet WHERE date_debut > ? ORDER BY date_debut LIMIT 1";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapRow(rs);
        }
        return null;
    }

    public List<TopTeacher> getTopTeachers(int limit) throws SQLException {
        List<TopTeacher> teachers = new ArrayList<>();
        String req = "SELECT p.id, p.nom, p.prenom, COUNT(m.id) as meet_count " +
                     "FROM participant p JOIN meet m ON p.id = m.participant_id " +
                     "GROUP BY p.id, p.nom, p.prenom " +
                     "ORDER BY meet_count DESC LIMIT ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, limit);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            teachers.add(new TopTeacher(
                rs.getInt("id"),
                rs.getString("nom") + " " + rs.getString("prenom"),
                rs.getInt("meet_count")
            ));
        }
        return teachers;
    }

    public List<DailyStat> getDailyStats(Timestamp from, Timestamp to) throws SQLException {
        List<DailyStat> stats = new ArrayList<>();
        String req = "SELECT DATE(date_debut) as day, COUNT(*) as count " +
                     "FROM meet WHERE date_debut >= ? AND date_debut <= ? " +
                     "GROUP BY DATE(date_debut) ORDER BY day";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setTimestamp(1, from);
        ps.setTimestamp(2, to);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            stats.add(new DailyStat(rs.getDate("day").toString(), rs.getInt("count")));
        }
        return stats;
    }

    public List<HourlyStat> getHourlyStats() throws SQLException {
        List<HourlyStat> stats = new ArrayList<>();
        String req = "SELECT HOUR(date_debut) as hour, COUNT(*) as count " +
                     "FROM meet GROUP BY HOUR(date_debut) ORDER BY hour";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            stats.add(new HourlyStat(rs.getInt("hour"), rs.getInt("count")));
        }
        return stats;
    }

    // Classes utilitaires pour les statistiques
    public static class TopTeacher {
        private final int id;
        private final String name;
        private final int meetCount;

        public TopTeacher(int id, String name, int meetCount) {
            this.id = id;
            this.name = name;
            this.meetCount = meetCount;
        }

        public int getId() { return id; }
        public String getName() { return name; }
        public int getMeetCount() { return meetCount; }
    }

    public static class DailyStat {
        private final String date;
        private final int count;

        public DailyStat(String date, int count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() { return date; }
        public int getCount() { return count; }
    }

    public static class HourlyStat {
        private final int hour;
        private final int count;

        public HourlyStat(int hour, int count) {
            this.hour = hour;
            this.count = count;
        }

        public int getHour() { return hour; }
        public int getCount() { return count; }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // AI SCHEDULING INTEGRATION
    // ═══════════════════════════════════════════════════════════════════════

    private AISchedulingService aiSchedulingService;

    /**
     * Verifie les conflits et suggere des creneaux alternatifs
     */
    public AISchedulingService.SchedulingSuggestion checkConflictsAndSuggest(
            Timestamp proposedStart, Timestamp proposedEnd,
            List<Integer> participantIds, Integer excludeMeetId) throws SQLException {
        if (aiSchedulingService == null) {
            aiSchedulingService = new AISchedulingService();
        }
        int durationMinutes = (int)((proposedEnd.getTime() - proposedStart.getTime()) / (60 * 1000));
        return aiSchedulingService.generateSmartSuggestion(proposedStart, proposedEnd,
                                                            participantIds, durationMinutes, excludeMeetId);
    }
}
