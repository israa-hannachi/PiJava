package tn.esprit.services.event;

import tn.esprit.entities.event.Rating;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RatingService implements IRatingService {

    private Connection cnx;

    public RatingService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Rating rating) throws SQLException {
        String req = "INSERT INTO rating (event_id, stars, comment, created_at) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, rating.getEventId());
        ps.setInt(2, rating.getStars());
        ps.setString(3, rating.getComment());
        ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
        ps.executeUpdate();
    }

    @Override
    public void modifier(Rating rating) throws SQLException {
        String req = "UPDATE rating SET stars = ?, comment = ? WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, rating.getStars());
        ps.setString(2, rating.getComment());
        ps.setInt(3, rating.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM rating WHERE id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public List<Rating> recuperer() throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String req = "SELECT * FROM rating";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Rating r = new Rating();
            r.setId(rs.getInt("id"));
            r.setEventId(rs.getInt("event_id"));
            r.setStars(rs.getInt("stars"));
            r.setComment(rs.getString("comment"));
            r.setCreatedAt(rs.getTimestamp("created_at"));
            ratings.add(r);
        }
        return ratings;
    }

    @Override
    public double getAverageRatingByEvent(int eventId) throws SQLException {
        String req = "SELECT AVG(stars) as avg_stars FROM rating WHERE event_id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getDouble("avg_stars");
        }
        return 0;
    }

    @Override
    public List<Rating> getRatingsByEvent(int eventId) throws SQLException {
        List<Rating> ratings = new ArrayList<>();
        String req = "SELECT * FROM rating WHERE event_id = ?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, eventId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            Rating r = new Rating();
            r.setId(rs.getInt("id"));
            r.setEventId(rs.getInt("event_id"));
            r.setStars(rs.getInt("stars"));
            r.setComment(rs.getString("comment"));
            r.setCreatedAt(rs.getTimestamp("created_at"));
            ratings.add(r);
        }
        return ratings;
    }
}
