package tn.esprit.services.event;

import tn.esprit.entities.event.Sponsor;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SponsorService implements ISponsorService {
    private Connection cnx;

    public SponsorService() {
        cnx = MyDatabase.getInstance().getCnx();
        if (cnx == null) {
            System.err.println("🚨 SponsorService: La connexion à la base de données est nulle !");
        }
    }

    @Override
    public void ajouter(Sponsor s) throws SQLException {
        String req = "INSERT INTO sponsors (event_id, nom, description, logo, site_web, type, montant, date_debut, date_fin, statut, contact_personne, contact_email, contact_telephone, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, s.getEventId());
        ps.setString(2, s.getNom());
        ps.setString(3, s.getDescription());
        ps.setString(4, s.getLogo());
        ps.setString(5, s.getSiteWeb());
        ps.setString(6, s.getType());
        ps.setBigDecimal(7, s.getMontant());
        ps.setTimestamp(8, s.getDateDebut());
        ps.setTimestamp(9, s.getDateFin());
        ps.setString(10, s.getStatut());
        ps.setString(11, s.getContactPersonne());
        ps.setString(12, s.getContactEmail());
        ps.setString(13, s.getContactTelephone());
        ps.setTimestamp(14, s.getDateCreation());
        ps.executeUpdate();
    }

    @Override
    public List<Sponsor> recuperer() throws SQLException {
        List<Sponsor> sponsors = new ArrayList<>();
        String req = "SELECT * FROM sponsors";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Sponsor s = new Sponsor();
            s.setId(rs.getInt("id"));
            s.setEventId(rs.getInt("event_id"));
            s.setNom(rs.getString("nom"));
            s.setDescription(rs.getString("description"));
            s.setLogo(rs.getString("logo"));
            s.setSiteWeb(rs.getString("site_web"));
            s.setType(rs.getString("type"));
            s.setMontant(rs.getBigDecimal("montant"));
            s.setDateDebut(rs.getTimestamp("date_debut"));
            s.setDateFin(rs.getTimestamp("date_fin"));
            s.setStatut(rs.getString("statut"));
            s.setContactPersonne(rs.getString("contact_personne"));
            s.setContactEmail(rs.getString("contact_email"));
            s.setContactTelephone(rs.getString("contact_telephone"));
            s.setDateCreation(rs.getTimestamp("date_creation"));
            sponsors.add(s);
        }
        return sponsors;
    }

    @Override
    public void modifier(Sponsor s) throws SQLException {
        String req = "UPDATE sponsors SET event_id=?, nom=?, description=?, logo=?, site_web=?, type=?, montant=?, date_debut=?, date_fin=?, statut=?, contact_personne=?, contact_email=?, contact_telephone=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, s.getEventId());
        ps.setString(2, s.getNom());
        ps.setString(3, s.getDescription());
        ps.setString(4, s.getLogo());
        ps.setString(5, s.getSiteWeb());
        ps.setString(6, s.getType());
        ps.setBigDecimal(7, s.getMontant());
        ps.setTimestamp(8, s.getDateDebut());
        ps.setTimestamp(9, s.getDateFin());
        ps.setString(10, s.getStatut());
        ps.setString(11, s.getContactPersonne());
        ps.setString(12, s.getContactEmail());
        ps.setString(13, s.getContactTelephone());
        ps.setInt(14, s.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM sponsors WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public Sponsor findById(int id) throws SQLException {
        String req = "SELECT * FROM sponsors WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Sponsor s = new Sponsor();
            s.setId(rs.getInt("id"));
            s.setEventId(rs.getInt("event_id"));
            s.setNom(rs.getString("nom"));
            s.setDescription(rs.getString("description"));
            s.setLogo(rs.getString("logo"));
            s.setSiteWeb(rs.getString("site_web"));
            s.setType(rs.getString("type"));
            s.setMontant(rs.getBigDecimal("montant"));
            s.setDateDebut(rs.getTimestamp("date_debut"));
            s.setDateFin(rs.getTimestamp("date_fin"));
            s.setStatut(rs.getString("statut"));
            s.setContactPersonne(rs.getString("contact_personne"));
            s.setContactEmail(rs.getString("contact_email"));
            s.setContactTelephone(rs.getString("contact_telephone"));
            s.setDateCreation(rs.getTimestamp("date_creation"));
            return s;
        }
        return null;
    }
}
