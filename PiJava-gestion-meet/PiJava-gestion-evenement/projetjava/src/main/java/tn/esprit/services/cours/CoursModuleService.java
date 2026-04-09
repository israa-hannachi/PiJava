package tn.esprit.services.cours;

import tn.esprit.entities.cours.cours_module;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursModuleService implements ICoursModuleService {
    private Connection cnx;

    public CoursModuleService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(cours_module m) throws SQLException {
        String req = "INSERT INTO cours_module (titre, description, duree, niveau, date_creation, actif, categorie_id, cree_par_admin) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, m.getTitre());
        ps.setString(2, m.getDescription());
        ps.setInt(3, m.getDuree());
        ps.setString(4, m.getNiveau());
        ps.setTimestamp(5, m.getDateCreation() != null ? m.getDateCreation() : new Timestamp(System.currentTimeMillis()));
        ps.setInt(6, m.getActif());
        ps.setInt(7, m.getCategorieId());
        ps.setInt(8, m.getCreeParAdmin());
        ps.executeUpdate();
    }

    @Override
    public List<cours_module> recuperer() throws SQLException {
        List<cours_module> list = new ArrayList<>();
        String req = "SELECT * FROM cours_module";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            cours_module m = new cours_module();
            m.setId(rs.getInt("id"));
            m.setTitre(rs.getString("titre"));
            m.setDescription(rs.getString("description"));
            m.setDuree(rs.getInt("duree"));
            m.setNiveau(rs.getString("niveau"));
            m.setDateCreation(rs.getTimestamp("date_creation"));
            m.setActif(rs.getInt("actif"));
            m.setCategorieId(rs.getInt("categorie_id"));
            m.setCreeParAdmin(rs.getInt("cree_par_admin"));
            list.add(m);
        }
        return list;
    }

    @Override
    public void modifier(cours_module m) throws SQLException {
        String req = "UPDATE cours_module SET titre=?, description=?, duree=?, niveau=?, actif=?, categorie_id=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, m.getTitre());
        ps.setString(2, m.getDescription());
        ps.setInt(3, m.getDuree());
        ps.setString(4, m.getNiveau());
        ps.setInt(5, m.getActif());
        ps.setInt(6, m.getCategorieId());
        ps.setInt(7, m.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        // Supprimer d'abord les cours liés à ce module
        String reqCours = "DELETE FROM cours WHERE module_id=?";
        PreparedStatement psCours = cnx.prepareStatement(reqCours);
        psCours.setInt(1, id);
        psCours.executeUpdate();

        String req = "DELETE FROM cours_module WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public cours_module findById(int id) throws SQLException {
        String req = "SELECT * FROM cours_module WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            cours_module m = new cours_module();
            m.setId(rs.getInt("id"));
            m.setTitre(rs.getString("titre"));
            m.setDescription(rs.getString("description"));
            m.setDuree(rs.getInt("duree"));
            m.setNiveau(rs.getString("niveau"));
            m.setDateCreation(rs.getTimestamp("date_creation"));
            m.setActif(rs.getInt("actif"));
            m.setCategorieId(rs.getInt("categorie_id"));
            m.setCreeParAdmin(rs.getInt("cree_par_admin"));
            return m;
        }
        return null;
    }
}