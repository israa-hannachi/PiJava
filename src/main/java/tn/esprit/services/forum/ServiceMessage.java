package tn.esprit.services.forum;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import tn.esprit.entities.forum.Forum;
import tn.esprit.entities.forum.Message;
import tn.esprit.tools.MyDatabase;

public class ServiceMessage implements IMessageService {
    private Connection cnx = MyDatabase.getInstance().getCnx();

    public ServiceMessage() {
        ensureReactionColumnsExist();
    }

    private void ensureReactionColumnsExist() {
        try (Statement st = cnx.createStatement()) {
            // Upgrade contenu to MEDIUMTEXT to support large Base64 images
            try { st.executeUpdate("ALTER TABLE message MODIFY COLUMN contenu MEDIUMTEXT"); } catch (SQLException ignored) {}
            
            try { st.executeUpdate("ALTER TABLE message ADD COLUMN likes_users TEXT"); } catch (SQLException ignored) {}
            try { st.executeUpdate("ALTER TABLE message ADD COLUMN dislikes_users TEXT"); } catch (SQLException ignored) {}
        } catch (SQLException e) {
            System.err.println("Erreur colonnes reactions : " + e.getMessage());
        }
    }

    public void addOrUpdateReaction(int messageId, String username, String type) {
        // We use | as a delimiter to avoid issues with names containing commas
        String userMarker = "|" + username + "|";
        
        String sqlSelect = "SELECT likes_users, dislikes_users FROM message WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sqlSelect)) {
            ps.setInt(1, messageId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String likes = rs.getString("likes_users");
                if (likes == null) likes = "";
                String dislikes = rs.getString("dislikes_users");
                if (dislikes == null) dislikes = "";

                boolean alreadyLiked = likes.contains(userMarker);
                boolean alreadyDisliked = dislikes.contains(userMarker);

                if ("LIKE".equals(type)) {
                    if (alreadyLiked) {
                        likes = likes.replace(userMarker, ""); // Toggle off
                    } else {
                        likes += userMarker;
                        if (alreadyDisliked) dislikes = dislikes.replace(userMarker, ""); // Switch
                    }
                } else if ("DISLIKE".equals(type)) {
                    if (alreadyDisliked) {
                        dislikes = dislikes.replace(userMarker, ""); // Toggle off
                    } else {
                        dislikes += userMarker;
                        if (alreadyLiked) likes = likes.replace(userMarker, ""); // Switch
                    }
                }

                String sqlUpdate = "UPDATE message SET likes_users = ?, dislikes_users = ? WHERE id = ?";
                try (PreparedStatement ups = cnx.prepareStatement(sqlUpdate)) {
                    ups.setString(1, likes);
                    ups.setString(2, dislikes);
                    ups.setInt(3, messageId);
                    ups.executeUpdate();
                }
            }
        } catch (SQLException e) {
            System.err.println("Erreur update reaction : " + e.getMessage());
        }
    }

    public List<String> getReactedUsers(int messageId, String type) {
        String column = "LIKE".equals(type) ? "likes_users" : "dislikes_users";
        String sql = "SELECT " + column + " FROM message WHERE id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, messageId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String raw = rs.getString(1);
                if (raw == null || raw.isEmpty()) return new ArrayList<>();
                return Arrays.stream(raw.split("\\|"))
                             .filter(s -> !s.isEmpty())
                             .collect(Collectors.toList());
            }
        } catch (SQLException e) {
            System.err.println("Erreur fetch reacted users : " + e.getMessage());
        }
        return new ArrayList<>();
    }

    public int countReactions(int messageId, String type) {
        return getReactedUsers(messageId, type).size();
    }

    public void ajouter(Message m) {
        String sql = "INSERT INTO `message` (`contenu`, `date_publication`, `etat`, `created_by`, `forum_id`, `likes_users`, `dislikes_users`) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setString(1, m.getContenu());
            ps.setDate(2, new Date(m.getDatePublication().getTime()));
            ps.setString(3, m.getEtat());
            ps.setString(4, m.getCreatedBy());
            ps.setInt(5, m.getForum() != null ? m.getForum().getId() : 0);
            ps.setString(6, "");
            ps.setString(7, "");
            ps.executeUpdate();
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
        } catch (SQLException e) {
            System.err.println("Erreur lors de la modification : " + e.getMessage());
        }
    }

    public void supprimer(int id) {
        String sql = "DELETE FROM `message` WHERE `id` = ?";
        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public List<Message> afficher() {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, f.id as forum_id, f.titre as forum_titre, f.description as forum_desc, f.date_creation as forum_date, f.etat as forum_etat, f.created_by as forum_creator FROM `message` m LEFT JOIN forum f ON m.forum_id = f.id";
        try (Statement st = this.cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while(rs.next()) {
                Message m = populateMessage(rs);
                messages.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Erreur affichage : " + e.getMessage());
        }
        return messages;
    }

    public List<Message> getMessagesByForum(int idForum) {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT m.*, f.id as forum_id, f.titre as forum_titre, f.description as forum_desc, f.date_creation as forum_date, f.etat as forum_etat, f.created_by as forum_creator FROM `message` m LEFT JOIN forum f ON m.forum_id = f.id WHERE m.forum_id = ?";
        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setInt(1, idForum);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                Message m = populateMessage(rs);
                messages.add(m);
            }
        } catch (SQLException e) {
            System.err.println("Erreur récupération par forum : " + e.getMessage());
        }
        return messages;
    }

    private Message populateMessage(ResultSet rs) throws SQLException {
        Message m = new Message();
        m.setId(rs.getInt("id"));
        m.setContenu(rs.getString("contenu"));
        m.setDatePublication(rs.getDate("date_publication"));
        m.setEtat(rs.getString("etat"));
        m.setCreatedBy(rs.getString("created_by"));
        m.setLikesUsers(rs.getString("likes_users"));
        m.setDislikesUsers(rs.getString("dislikes_users"));
        
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
        return m;
    }

    public int compterMessagesParUtilisateur(String nomUtilisateur) {
        String sql = "SELECT COUNT(*) FROM `message` WHERE `created_by` = ?";
        try (PreparedStatement ps = this.cnx.prepareStatement(sql)) {
            ps.setString(1, nomUtilisateur);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur compter : " + e.getMessage());
        }
        return 0;
    }

    public int statistiquesUtilisateur(String auteur) {
        String sql = "SELECT COUNT(*) FROM message WHERE created_by LIKE ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setString(1, "%" + auteur + "%");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur Stats : " + e.getMessage());
        }
        return 0;
    }

    public int getMessageCountForForum(int forumId) {
        String sql = "SELECT COUNT(*) FROM message WHERE forum_id = ?";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, forumId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur getMessageCountForForum : " + e.getMessage());
        }
        return 0;
    }

    public int getTotalMessageCount() {
        String sql = "SELECT COUNT(*) FROM message";
        try (Statement st = cnx.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur getTotalMessageCount : " + e.getMessage());
        }
        return 0;
    }

    public Map<String, Integer> getMessagesCountByDate(int days) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        String sql = "SELECT date_publication, COUNT(*) as count FROM message " +
                     "WHERE date_publication >= DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "GROUP BY date_publication ORDER BY date_publication ASC";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, days);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                counts.put(rs.getDate("date_publication").toString(), rs.getInt("count"));
            }
        } catch (SQLException e) {
            System.err.println("Erreur getMessagesCountByDate : " + e.getMessage());
        }
        return counts;
    }

    public int getForumActivityCount(int forumId, int daysOffsetStart, int daysOffsetEnd) {
        String sql = "SELECT COUNT(*) FROM message WHERE forum_id = ? " +
                     "AND date_publication BETWEEN DATE_SUB(CURDATE(), INTERVAL ? DAY) " +
                     "AND DATE_SUB(CURDATE(), INTERVAL ? DAY)";
        try (PreparedStatement ps = cnx.prepareStatement(sql)) {
            ps.setInt(1, forumId);
            ps.setInt(2, daysOffsetStart);
            ps.setInt(3, daysOffsetEnd);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("Erreur getForumActivityCount : " + e.getMessage());
        }
        return 0;
    }
}
