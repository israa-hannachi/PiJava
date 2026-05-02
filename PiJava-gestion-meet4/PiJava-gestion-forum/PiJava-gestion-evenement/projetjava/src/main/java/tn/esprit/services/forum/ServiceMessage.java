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
import tn.esprit.entities.forum.Message;
import tn.esprit.tools.MyDatabase;

public class ServiceMessage implements IMessageService {
    private Connection cnx = MyDatabase.getInstance().getCnx();

    public void ajouter(Message m) {
        String sql = "INSERT INTO `message` (`contenu`, `date_publication`, `etat`, `created_by`, `forum_id`) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setString(1, m.getContenu());
            ps.setDate(2, new Date(m.getDatePublication().getTime()));
            ps.setString(3, m.getEtat());
            ps.setString(4, m.getCreatedBy());
            ps.setInt(5, m.getForum() != null ? m.getForum().getId() : 0);
            ps.executeUpdate();
            System.out.println("Message ajouté avec succès !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de l'ajout : " + e.getMessage());
        }

    }

    public void modifier(Message m) {
        String sql = "UPDATE `message` SET `contenu` = ?, `etat` = ? WHERE `id` = ?";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setString(1, m.getContenu());
            ps.setString(2, m.getEtat());
            ps.setInt(3, m.getId());
            ps.executeUpdate();
            System.out.println("Message modifié !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
        }

    }

    public void supprimer(int id) {
        String sql = "DELETE FROM `message` WHERE `id` = ?";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
            System.out.println("Message supprimé !");
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }

    }

    public List<Message> afficher() {
        List<Message> messages = new ArrayList();
        String sql = "SELECT m.*, f.id as forum_id, f.titre as forum_titre, f.description as forum_desc, f.date_creation as forum_date, f.etat as forum_etat, f.created_by as forum_creator FROM `message` m LEFT JOIN forum f ON m.forum_id = f.id";

        try (
                Statement st = this.cnx.createStatement();
                ResultSet rs = st.executeQuery(sql);
        ) {
            while(rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setContenu(rs.getString("contenu"));
                m.setDatePublication(rs.getDate("date_publication"));
                m.setEtat(rs.getString("etat"));
                m.setCreatedBy(rs.getString("created_by"));

                // Charger le forum
                int forumId = rs.getInt("forum_id");
                if (forumId > 0) {
                    Forum f = new Forum();
                    f.setId(forumId);
                    f.setTitre(rs.getString("forum_titre"));
                    f.setDescription(rs.getString("forum_desc"));
                    f.setDateCreation(rs.getDate("forum_date"));
                    f.setEtat(rs.getString("forum_etat"));
                    f.setCreatedBy(rs.getString("forum_creator"));
                    m.setForum(f);
                }

                messages.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Erreur affichage : " + e.getMessage());
        }

        return messages;
    }

    public List<Message> getMessagesByForum(int idForum) {
        List<Message> messages = new ArrayList();
        String sql = "SELECT * FROM `message` WHERE `forum_id` = ?";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setInt(1, idForum);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
                Message m = new Message();
                m.setId(rs.getInt("id"));
                m.setContenu(rs.getString("contenu"));
                m.setCreatedBy(rs.getString("created_by"));
                messages.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération par forum : " + e.getMessage());
        }

        return messages;
    }

    public int compterMessagesParUtilisateur(String nomUtilisateur) {
        String sql = "SELECT COUNT(*) FROM `message` WHERE `created_by` = ?";

        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setString(1, nomUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Erreur comptage messages : " + e.getMessage());
        }

        return 0;
    }

    public int statistiquesUtilisateur(String auteur) {
        int total = 0;
        String qry = "SELECT COUNT(*) FROM message WHERE created_by LIKE ?";

        try (PreparedStatement pstm = this.cnx.prepareStatement(qry)) {
            pstm.setString(1, "%" + auteur + "%");
            ResultSet rs = pstm.executeQuery();
            if (rs.next()) {
                total = rs.getInt(1);
            }
        } catch (SQLException e) {
            System.out.println("Erreur Stats : " + e.getMessage());
        }

        return total;
    }
}
