//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.services.forum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.tools.MyDatabase;

public class ServiceCategorie implements ICategorieService {
    private Connection cnx = MyDatabase.getInstance().getCnx();

    public void ajouter(Categorie c) {
        String sql = "INSERT INTO `categorie`(`titre`, `description`, `icone`) VALUES (?,?,?)";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getIcone());
            ps.executeUpdate();
            System.out.println("Catégorie ajoutée avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur Ajout Categorie : " + e.getMessage());
        }

    }

    public void modifier(Categorie c) {
        String sql = "UPDATE `categorie` SET `titre`=?, `description`=?, `icone`=? WHERE `id`=?";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setString(1, c.getTitre());
            ps.setString(2, c.getDescription());
            ps.setString(3, c.getIcone());
            ps.setInt(4, c.getId());
            ps.executeUpdate();
            System.out.println("Catégorie modifiée !");
        } catch (SQLException e) {
            System.err.println("Erreur Modif Categorie : " + e.getMessage());
        }

    }

    public void supprimer(int id) {
        String qry = "DELETE FROM categorie WHERE id = ?";

        try (PreparedStatement pstm = this.cnx.prepareStatement(qry)) {
            pstm.setInt(1, id);
            int res = pstm.executeUpdate();
            if (res > 0) {
                System.out.println("✅ Catégorie supprimée avec succès !");
            } else {
                System.out.println("⚠️ Aucune catégorie trouvée avec l'ID : " + id);
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }

    }

    public List<Categorie> afficher() {
        List<Categorie> categories = new ArrayList();
        String sql = "SELECT * FROM `categorie`";

        try (
                Statement st = this.cnx.createStatement();
                ResultSet rs = st.executeQuery(sql);
        ) {
            while(rs.next()) {
                categories.add(new Categorie(rs.getInt("id"), rs.getString("titre"), rs.getString("description"), rs.getString("icone")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur Affichage Categorie : " + e.getMessage());
        }

        return categories;
    }

    public Categorie trouverParTitre(String titre) {
        String sql = "SELECT * FROM `categorie` WHERE `titre` = ?";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setString(1, titre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Categorie(rs.getInt("id"), rs.getString("titre"), rs.getString("description"), rs.getString("icone"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur Recherche Categorie : " + e.getMessage());
        }

        return null;
    }
}
