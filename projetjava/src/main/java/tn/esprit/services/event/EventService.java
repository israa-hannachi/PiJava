package tn.esprit.services.event;

import tn.esprit.entities.event.Event;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventService implements IEventService {
    private Connection cnx;

    public EventService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("🚨 EventService: La connexion à la base de données est nulle !");
        }
    }

    @Override
    public void ajouter(Event e) throws SQLException {
        String req = "INSERT INTO events (titre, description, date_debut, date_fin, capacite, inscrits, image, categorie, prix, lieu, latitude, longitude, statut, time_zone, is_recurring, recurrence_frequency, recurrence_count, attendees_emails, organizer_email, ical_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, e.getTitre());
        ps.setString(2, e.getDescription());
        ps.setTimestamp(3, e.getDateDebut());
        ps.setTimestamp(4, e.getDateFin());
        ps.setInt(5, e.getCapacite());
        ps.setInt(6, e.getInscrits());
        ps.setString(7, e.getImage());
        ps.setString(8, e.getCategorie());
        ps.setBigDecimal(9, e.getPrix());
        ps.setString(10, e.getLieu());
        ps.setBigDecimal(11, e.getLatitude());
        ps.setBigDecimal(12, e.getLongitude());
        ps.setString(13, e.getStatut());
        ps.setString(14, e.getTimeZone());
        ps.setBoolean(15, e.isRecurring());
        ps.setString(16, e.getRecurrenceFrequency());
        if (e.getRecurrenceCount() != null) ps.setInt(17, e.getRecurrenceCount()); else ps.setNull(17, Types.INTEGER);
        ps.setString(18, e.getAttendeesEmails());
        ps.setString(19, e.getOrganizerEmail());
        ps.setString(20, e.getIcalId());
        ps.executeUpdate();
    }

    @Override
    public List<Event> recuperer() throws SQLException {
        List<Event> events = new ArrayList<>();
        String req = "SELECT * FROM events";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setTitre(rs.getString("titre"));
            e.setDescription(rs.getString("description"));
            e.setDateCreation(rs.getTimestamp("date_creation"));
            e.setDateDebut(rs.getTimestamp("date_debut"));
            e.setDateFin(rs.getTimestamp("date_fin"));
            e.setCapacite(rs.getInt("capacite"));
            e.setInscrits(rs.getInt("inscrits"));
            e.setImage(rs.getString("image"));
            e.setCategorie(rs.getString("categorie"));
            e.setPrix(rs.getBigDecimal("prix"));
            e.setLieu(rs.getString("lieu"));
            e.setLatitude(rs.getBigDecimal("latitude"));
            e.setLongitude(rs.getBigDecimal("longitude"));
            e.setStatut(rs.getString("statut"));
            e.setTimeZone(rs.getString("time_zone"));
            e.setRecurring(rs.getBoolean("is_recurring"));
            e.setRecurrenceFrequency(rs.getString("recurrence_frequency"));
            e.setRecurrenceCount(rs.getInt("recurrence_count"));
            e.setAttendeesEmails(rs.getString("attendees_emails"));
            e.setOrganizerEmail(rs.getString("organizer_email"));
            e.setIcalId(rs.getString("ical_id"));
            events.add(e);
        }
        return events;
    }

    @Override
    public void modifier(Event e) throws SQLException {
        String req = "UPDATE events SET titre=?, description=?, date_debut=?, date_fin=?, capacite=?, inscrits=?, image=?, categorie=?, prix=?, lieu=?, latitude=?, longitude=?, statut=?, time_zone=?, is_recurring=?, recurrence_frequency=?, recurrence_count=?, attendees_emails=?, organizer_email=?, ical_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, e.getTitre());
        ps.setString(2, e.getDescription());
        ps.setTimestamp(3, e.getDateDebut());
        ps.setTimestamp(4, e.getDateFin());
        ps.setInt(5, e.getCapacite());
        ps.setInt(6, e.getInscrits());
        ps.setString(7, e.getImage());
        ps.setString(8, e.getCategorie());
        ps.setBigDecimal(9, e.getPrix());
        ps.setString(10, e.getLieu());
        ps.setBigDecimal(11, e.getLatitude());
        ps.setBigDecimal(12, e.getLongitude());
        ps.setString(13, e.getStatut());
        ps.setString(14, e.getTimeZone());
        ps.setBoolean(15, e.isRecurring());
        ps.setString(16, e.getRecurrenceFrequency());
        if (e.getRecurrenceCount() != null) ps.setInt(17, e.getRecurrenceCount()); else ps.setNull(17, Types.INTEGER);
        ps.setString(18, e.getAttendeesEmails());
        ps.setString(19, e.getOrganizerEmail());
        ps.setString(20, e.getIcalId());
        ps.setInt(21, e.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Supprimer les sponsors liés à l'événement
        String reqSponsors = "DELETE FROM sponsors WHERE event_id=?";
        PreparedStatement psSponsors = cnx.prepareStatement(reqSponsors);
        psSponsors.setInt(1, id);
        psSponsors.executeUpdate();

        // Supprimer les registrations liées à l'événement
        String reqRegistrations = "DELETE FROM registrations WHERE evenement_id=?";
        PreparedStatement psRegistrations = cnx.prepareStatement(reqRegistrations);
        psRegistrations.setInt(1, id);
        psRegistrations.executeUpdate();

        // Enfin, supprimer l'événement lui-même
        String reqEvent = "DELETE FROM events WHERE id=?";
        PreparedStatement psEvent = cnx.prepareStatement(reqEvent);
        psEvent.setInt(1, id);
        psEvent.executeUpdate();
    }

    @Override
    public Event findById(int id) throws SQLException {
        String req = "SELECT * FROM events WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Event e = new Event();
            e.setId(rs.getInt("id"));
            e.setTitre(rs.getString("titre"));
            e.setDescription(rs.getString("description"));
            e.setDateCreation(rs.getTimestamp("date_creation"));
            e.setDateDebut(rs.getTimestamp("date_debut"));
            e.setDateFin(rs.getTimestamp("date_fin"));
            e.setCapacite(rs.getInt("capacite"));
            e.setInscrits(rs.getInt("inscrits"));
            e.setImage(rs.getString("image"));
            e.setCategorie(rs.getString("categorie"));
            e.setPrix(rs.getBigDecimal("prix"));
            e.setLieu(rs.getString("lieu"));
            e.setLatitude(rs.getBigDecimal("latitude"));
            e.setLongitude(rs.getBigDecimal("longitude"));
            e.setStatut(rs.getString("statut"));
            e.setTimeZone(rs.getString("time_zone"));
            e.setRecurring(rs.getBoolean("is_recurring"));
            e.setRecurrenceFrequency(rs.getString("recurrence_frequency"));
            e.setRecurrenceCount(rs.getInt("recurrence_count"));
            e.setAttendeesEmails(rs.getString("attendees_emails"));
            e.setOrganizerEmail(rs.getString("organizer_email"));
            e.setIcalId(rs.getString("ical_id"));
            return e;
        }
        return null;
    }
}
