package tn.esprit.services.users;

import tn.esprit.entities.users.Users;
import java.sql.SQLException;
import java.util.List;

public interface IUsersService {
    void ajouter(Users u) throws SQLException;
    List<Users> recuperer() throws SQLException;
    void modifier(Users u) throws SQLException;
    void supprimer(int id) throws SQLException;
    Users findById(int id) throws SQLException;
    Users findByEmail(String email) throws SQLException;
    List<Users> findByRole(String role) throws SQLException;
    void changerStatut(int id, String statut) throws SQLException;
    void updatePassword(int id, String hashedPassword) throws SQLException;
    void updateProfileMedia(int id, String profilePicture, String coverPicture) throws SQLException;
    void updateBiometricDescriptor(int id, String descriptor) throws SQLException;
    void updateGoogleAuthenticatorSecret(int id, String secret) throws SQLException;
    void clearGoogleAuthenticatorSecret(int id) throws SQLException;
}