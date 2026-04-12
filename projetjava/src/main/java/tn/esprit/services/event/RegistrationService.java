package tn.esprit.services.event;

import tn.esprit.entities.event.Registration;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RegistrationService implements IRegistrationService {
    private Connection cnx;

    public RegistrationService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("🚨 RegistrationService: La connexion à la base de données est nulle !");
        }
    }

    @Override
    public void ajouter(Registration r) throws SQLException {
        String req = "INSERT INTO registrations (evenement_id, visitor_name, visitor_email, statut, presence, mode_paiement, montant_paye, paiement_statut, notes) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, r.getEvenementId());
        ps.setString(2, r.getVisitorName());
        ps.setString(3, r.getVisitorEmail());
        ps.setString(4, r.getStatut());
        ps.setBoolean(5, r.isPresence());
        ps.setString(6, r.getModePaiement());
        ps.setBigDecimal(7, r.getMontantPaye());
        ps.setString(8, r.getPaiementStatut());
        ps.setString(9, r.getNotes());
        ps.executeUpdate();
    }

    @Override
    public List<Registration> recuperer() throws SQLException {
        List<Registration> registrations = new ArrayList<>();
        String req = "SELECT * FROM registrations";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Registration r = new Registration();
            r.setId(rs.getInt("id"));
            r.setEvenementId(rs.getInt("evenement_id"));
            r.setVisitorName(rs.getString("visitor_name"));
            r.setVisitorEmail(rs.getString("visitor_email"));
            r.setDateInscription(rs.getTimestamp("date_inscription"));
            r.setStatut(rs.getString("statut"));
            r.setPresence(rs.getBoolean("presence"));
            r.setModePaiement(rs.getString("mode_paiement"));
            r.setMontantPaye(rs.getBigDecimal("montant_paye"));
            r.setPaiementStatut(rs.getString("paiement_statut"));
            r.setNotes(rs.getString("notes"));
            registrations.add(r);
        }
        return registrations;
    }

    @Override
    public void modifier(Registration r) throws SQLException {
        String req = "UPDATE registrations SET evenement_id=?, visitor_name=?, visitor_email=?, statut=?, presence=?, mode_paiement=?, montant_paye=?, paiement_statut=?, notes=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, r.getEvenementId());
        ps.setString(2, r.getVisitorName());
        ps.setString(3, r.getVisitorEmail());
        ps.setString(4, r.getStatut());
        ps.setBoolean(5, r.isPresence());
        ps.setString(6, r.getModePaiement());
        ps.setBigDecimal(7, r.getMontantPaye());
        ps.setString(8, r.getPaiementStatut());
        ps.setString(9, r.getNotes());
        ps.setInt(10, r.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM registrations WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public Registration findById(int id) throws SQLException {
        String req = "SELECT * FROM registrations WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Registration r = new Registration();
            r.setId(rs.getInt("id"));
            r.setEvenementId(rs.getInt("evenement_id"));
            r.setVisitorName(rs.getString("visitor_name"));
            r.setVisitorEmail(rs.getString("visitor_email"));
            r.setDateInscription(rs.getTimestamp("date_inscription"));
            r.setStatut(rs.getString("statut"));
            r.setPresence(rs.getBoolean("presence"));
            r.setModePaiement(rs.getString("mode_paiement"));
            r.setMontantPaye(rs.getBigDecimal("montant_paye"));
            r.setPaiementStatut(rs.getString("paiement_statut"));
            r.setNotes(rs.getString("notes"));
            return r;
        }
        return null;
    }
}
