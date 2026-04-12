//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package tn.esprit.services.forum;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import tn.esprit.entities.forum.Forum;
import tn.esprit.tools.MyDatabase;

public class ServiceForum implements IForumService<Forum> {
    private Connection cnx = MyDatabase.getInstance().getCnx();

    public void ajouter(Forum f) {
        String qry = "INSERT INTO `forum`(`titre`, `description`, `date_creation`, `etat`, `created_by`, `categorie_id`) VALUES (?,?,?,?,?,?)";

        try (PreparedStatement pstm = this.cnx.prepareStatement(qry)) {
            pstm.setString(1, f.getTitre());
            pstm.setString(2, f.getDescription());
            pstm.setDate(3, new Date(f.getDateCreation().getTime()));
            pstm.setString(4, f.getEtat());
            pstm.setString(5, f.getCreatedBy());
            pstm.setInt(6, f.getCategorie() != null ? f.getCategorie().getId() : 0);
            pstm.executeUpdate();
            System.out.println("Succès : Forum '" + f.getTitre() + "' ajouté.");
        } catch (SQLException e) {
            System.out.println("Erreur (Ajout) : " + e.getMessage());
        }

    }

    public List<Forum> afficher() {
        List<Forum> forums = new ArrayList();
        String qry = "SELECT * FROM forum";

        try (
                Statement stm = this.cnx.createStatement();
                ResultSet rs = stm.executeQuery(qry);
        ) {
            while(rs.next()) {
                Forum f = new Forum();
                f.setId(rs.getInt("id"));
                f.setTitre(rs.getString("titre"));
                f.setDescription(rs.getString("description"));
                f.setDateCreation(rs.getDate("date_creation"));
                f.setEtat(rs.getString("etat"));
                f.setCreatedBy(rs.getString("created_by"));
                forums.add(f);
            }
        } catch (SQLException e) {
            System.out.println("Erreur (Affichage) : " + e.getMessage());
        }

        return forums;
    }

    public void modifier(Forum f) {
        String qry = "UPDATE `forum` SET `titre`=?, `description`=?, `etat`=? WHERE `id`=?";

        try (PreparedStatement pstm = this.cnx.prepareStatement(qry)) {
            pstm.setString(1, f.getTitre());
            pstm.setString(2, f.getDescription());
            pstm.setString(3, f.getEtat());
            pstm.setInt(4, f.getId());
            pstm.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Erreur (Modification) : " + e.getMessage());
        }

    }

    public void supprimer(int id) {
        try {
            String deleteMessagesReq = "DELETE FROM message WHERE forum_id = ?";
            PreparedStatement psMsg = this.cnx.prepareStatement(deleteMessagesReq);
            psMsg.setInt(1, id);
            psMsg.executeUpdate();
            String deleteForumReq = "DELETE FROM forum WHERE id = ?";
            PreparedStatement psForum = this.cnx.prepareStatement(deleteForumReq);
            psForum.setInt(1, id);
            int rowsAffected = psForum.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("✅ Forum et ses messages supprimés avec succès.");
            }
        } catch (SQLException e) {
            System.out.println("❌ Erreur SQL : " + e.getMessage());
        }

    }
}
