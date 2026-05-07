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
        List<Categorie> categories = new ArrayList<>();
        if (this.cnx == null) {
            System.err.println("ERREUR CRITIQUE: Connexion DB null dans ServiceCategorie");
            return categories;
        }

        String sql = "SELECT id, titre, description, icone FROM categorie ORDER BY id ASC";
        System.out.println("🔍 ServiceCategorie: Exécution de: " + sql);

        try {
            // Utiliser un ResultSet scrollable pour s'assurer de récupérer toutes les lignes
            Statement st = this.cnx.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, 
                ResultSet.CONCUR_READ_ONLY
            );
            ResultSet rs = st.executeQuery(sql);

            // Aller à la fin pour compter
            rs.last();
            int totalRows = rs.getRow();
            System.out.println("📊 Total de lignes dans ResultSet: " + totalRows);
            rs.beforeFirst();

            int count = 0;
            while(rs.next()) {
                int id = rs.getInt("id");
                String titre = rs.getString("titre");
                String desc = rs.getString("description");
                String icone = rs.getString("icone");
                System.out.println("  [" + (count+1) + "] ID=" + id + ", Titre=" + titre);
                categories.add(new Categorie(id, titre, desc, icone));
                count++;
            }
            System.out.println("✅ ServiceCategorie: " + count + "/" + totalRows + " catégories récupérées");
            rs.close();
            st.close();
        } catch (SQLException e) {
            System.err.println("❌ ERREUR SQL: " + e.getMessage());
            e.printStackTrace();
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

    public List<Categorie> rechercher(String valeur) {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM `categorie` WHERE `titre` LIKE ? OR `description` LIKE ?";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            String searchPattern = "%" + valeur + "%";
            ps.setString(1, searchPattern);
            ps.setString(2, searchPattern);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                categories.add(new Categorie(rs.getInt("id"), rs.getString("titre"), rs.getString("description"), rs.getString("icone")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur Recherche Categories : " + e.getMessage());
        }

        return categories;
    }

    public List<Categorie> trierParTitre() {
        List<Categorie> categories = new ArrayList<>();
        String sql = "SELECT * FROM `categorie` ORDER BY `titre` ASC";

        try (Statement st = this.cnx.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(new Categorie(rs.getInt("id"), rs.getString("titre"), rs.getString("description"), rs.getString("icone")));
            }
        } catch (SQLException e) {
            System.err.println("Erreur Tri Categories : " + e.getMessage());
        }

        return categories;
    }
}
