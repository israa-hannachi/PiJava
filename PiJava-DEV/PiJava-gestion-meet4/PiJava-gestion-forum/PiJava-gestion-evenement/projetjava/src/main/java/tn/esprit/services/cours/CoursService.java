package tn.esprit.services.cours;

import tn.esprit.entities.cours.Cours;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursService implements ICoursService {
    private Connection cnx;

    public CoursService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Cours c) throws SQLException {
        String req = "INSERT INTO cours (titre, description, contenu, duree, ordre, date_creation, actif, module_id, fichier_contenu, cree_par_admin, visible) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, c.getTitre());
        ps.setString(2, c.getDescription());
        ps.setString(3, c.getContenu());
        ps.setInt(4, c.getDuree());
        ps.setInt(5, c.getOrdre());
        ps.setTimestamp(6, c.getDateCreation() != null ? c.getDateCreation() : new Timestamp(System.currentTimeMillis()));
        ps.setInt(7, c.getActif());
        ps.setInt(8, c.getModuleId());
        ps.setString(9, c.getFichierContenu());
        ps.setInt(10, c.getCreeParAdmin());
        ps.setInt(11, c.getVisible());
        ps.executeUpdate();
    }

    @Override
    public List<Cours> recuperer() throws SQLException {
        List<Cours> list = new ArrayList<>();
        String req = "SELECT * FROM cours";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    @Override
    public void modifier(Cours c) throws SQLException {
        String req = "UPDATE cours SET titre=?, description=?, contenu=?, duree=?, ordre=?, actif=?, module_id=?, fichier_contenu=?, visible=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, c.getTitre());
        ps.setString(2, c.getDescription());
        ps.setString(3, c.getContenu());
        ps.setInt(4, c.getDuree());
        ps.setInt(5, c.getOrdre());
        ps.setInt(6, c.getActif());
        ps.setInt(7, c.getModuleId());
        ps.setString(8, c.getFichierContenu());
        ps.setInt(9, c.getVisible());
        ps.setInt(10, c.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Supprimer les réclamations liées
        String reqReclamation = "DELETE FROM reclamation_cours WHERE cours_id=?";
        PreparedStatement psR = cnx.prepareStatement(reqReclamation);
        psR.setInt(1, id);
        psR.executeUpdate();

        String req = "DELETE FROM cours WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public Cours findById(int id) throws SQLException {
        String req = "SELECT * FROM cours WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return mapResultSet(rs);
        }
        return null;
    }

    @Override
    public List<Cours> findByModuleId(int moduleId) throws SQLException {
        List<Cours> list = new ArrayList<>();
        String req = "SELECT * FROM cours WHERE module_id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, moduleId);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    private Cours mapResultSet(ResultSet rs) throws SQLException {
        Cours c = new Cours();
        c.setId(rs.getInt("id"));
        c.setTitre(rs.getString("titre"));
        c.setDescription(rs.getString("description"));
        c.setContenu(rs.getString("contenu"));
        c.setDuree(rs.getInt("duree"));
        c.setOrdre(rs.getInt("ordre"));
        c.setDateCreation(rs.getTimestamp("date_creation"));
        c.setDateModification(rs.getTimestamp("date_modification"));
        c.setActif(rs.getInt("actif"));
        c.setModuleId(rs.getInt("module_id"));
        c.setFichierContenu(rs.getString("fichier_contenu"));
        c.setCreeParAdmin(rs.getInt("cree_par_admin"));
        c.setVisible(rs.getInt("visible"));
        c.setVisibleFrom(rs.getTimestamp("visible_from"));
        c.setResumeAi(rs.getString("resume_ai"));
        return c;
    }
}