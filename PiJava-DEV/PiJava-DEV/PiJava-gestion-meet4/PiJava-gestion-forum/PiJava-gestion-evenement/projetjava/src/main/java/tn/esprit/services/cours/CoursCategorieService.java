package tn.esprit.services.cours;

import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CoursCategorieService implements ICoursCategorieService {
    private Connection cnx;

    public CoursCategorieService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Cours_Categorie c) throws SQLException {
        String req = "INSERT INTO cours_categorie (nom, description, date_creation, actif) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.setTimestamp(3, c.getDateCreation() != null ? c.getDateCreation() : new Timestamp(System.currentTimeMillis()));
        ps.setInt(4, c.getActif());
        ps.executeUpdate();
    }

    @Override
    public List<Cours_Categorie> recuperer() throws SQLException {
        List<Cours_Categorie> list = new ArrayList<>();
        String req = "SELECT * FROM cours_categorie";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            Cours_Categorie c = new Cours_Categorie();
            c.setId(rs.getInt("id"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            c.setDateCreation(rs.getTimestamp("date_creation"));
            c.setActif(rs.getInt("actif"));
            list.add(c);
        }
        return list;
    }

    @Override
    public void modifier(Cours_Categorie c) throws SQLException {
        String req = "UPDATE cours_categorie SET nom=?, description=?, actif=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, c.getNom());
        ps.setString(2, c.getDescription());
        ps.setInt(3, c.getActif());
        ps.setInt(4, c.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM cours_categorie WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public Cours_Categorie findById(int id) throws SQLException {
        String req = "SELECT * FROM cours_categorie WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            Cours_Categorie c = new Cours_Categorie();
            c.setId(rs.getInt("id"));
            c.setNom(rs.getString("nom"));
            c.setDescription(rs.getString("description"));
            c.setDateCreation(rs.getTimestamp("date_creation"));
            c.setActif(rs.getInt("actif"));
            return c;
        }
        return null;
    }
}