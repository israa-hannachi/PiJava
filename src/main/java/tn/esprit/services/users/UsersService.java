package tn.esprit.services.users;

import tn.esprit.entities.users.Users;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UsersService implements IUsersService {
    private Connection cnx;

    public UsersService() {
        cnx = MyDatabase.getInstance().getCnx();
    }

    @Override
    public void ajouter(Users u) throws SQLException {
        String req = "INSERT INTO users (email, password, first_name, last_name, profile_picture, profession, experience_level, role, statut, date_creation) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, u.getEmail());
        ps.setString(2, u.getPassword());
        ps.setString(3, u.getFirstName());
        ps.setString(4, u.getLastName());
        ps.setString(5, u.getProfilePicture());
        ps.setString(6, u.getProfession());
        ps.setString(7, u.getExperienceLevel());
        ps.setString(8, u.getRole());
        ps.setString(9, u.getStatut() != null ? u.getStatut() : "ACTIF");
        ps.setTimestamp(10, new Timestamp(System.currentTimeMillis()));
        ps.executeUpdate();
    }

    @Override
    public List<Users> recuperer() throws SQLException {
        List<Users> list = new ArrayList<>();
        String req = "SELECT * FROM users";
        Statement st = cnx.createStatement();
        ResultSet rs = st.executeQuery(req);
        while (rs.next()) {
            list.add(mapResultSet(rs));
        }
        return list;
    }

    @Override
    public void modifier(Users u) throws SQLException {
        String req = "UPDATE users SET email=?, first_name=?, last_name=?, profession=?, experience_level=?, role=?, statut=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, u.getEmail());
        ps.setString(2, u.getFirstName());
        ps.setString(3, u.getLastName());
        ps.setString(4, u.getProfession());
        ps.setString(5, u.getExperienceLevel());
        ps.setString(6, u.getRole());
        ps.setString(7, u.getStatut());
        ps.setInt(8, u.getId());
        ps.executeUpdate();
    }

    @Override
    public void supprimer(int id) throws SQLException {
        String req = "DELETE FROM users WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    @Override
    public void updatePassword(int id, String hashedPassword) throws SQLException {
        String req = "UPDATE users SET password=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, hashedPassword);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    @Override
    public Users findById(int id) throws SQLException {
        String req = "SELECT * FROM users WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapResultSet(rs);
        return null;
    }

    @Override
    public Users findByEmail(String email) throws SQLException {
        String req = "SELECT * FROM users WHERE email=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) return mapResultSet(rs);
        return null;
    }

    @Override
    public List<Users> findByRole(String role) throws SQLException {
        List<Users> list = new ArrayList<>();
        String req = "SELECT * FROM users WHERE role=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, role);
        ResultSet rs = ps.executeQuery();
        while (rs.next()) list.add(mapResultSet(rs));
        return list;
    }

    @Override
    public void changerStatut(int id, String statut) throws SQLException {
        String req = "UPDATE users SET statut=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, statut);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    @Override
    public void updateProfileMedia(int id, String profilePicture, String coverPicture) throws SQLException {
        String req = "UPDATE users SET profile_picture=?, cover_picture=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, profilePicture);
        ps.setString(2, coverPicture);
        ps.setInt(3, id);
        ps.executeUpdate();
    }

    @Override
    public void updateBiometricDescriptor(int id, String descriptor) throws SQLException {
        String req = "UPDATE users SET biometric_descriptor=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, descriptor);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    @Override
    public void updateGoogleAuthenticatorSecret(int id, String secret) throws SQLException {
        String req = "UPDATE users SET google_authenticator_secret=? WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setString(1, secret);
        ps.setInt(2, id);
        ps.executeUpdate();
    }

    @Override
    public void clearGoogleAuthenticatorSecret(int id) throws SQLException {
        String req = "UPDATE users SET google_authenticator_secret=NULL WHERE id=?";
        PreparedStatement ps = cnx.prepareStatement(req);
        ps.setInt(1, id);
        ps.executeUpdate();
    }

    private Users mapResultSet(ResultSet rs) throws SQLException {
        Users u = new Users();
        u.setId(rs.getInt("id"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setFirstName(rs.getString("first_name"));
        u.setLastName(rs.getString("last_name"));
        u.setProfilePicture(rs.getString("profile_picture"));
        u.setProfession(rs.getString("profession"));
        u.setExperienceLevel(rs.getString("experience_level"));
        u.setRole(rs.getString("role"));
        u.setStatut(rs.getString("statut"));
        u.setDateCreation(rs.getTimestamp("date_creation"));
        u.setCoverPicture(rs.getString("cover_picture"));
        u.setBiometricDescriptor(rs.getString("biometric_descriptor"));
        u.setGoogleAuthenticatorSecret(rs.getString("google_authenticator_secret"));
        return u;
    }
}