package tn.esprit.controllers.users;

import tn.esprit.entities.users.Users;
import tn.esprit.services.users.UsersService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsersController {

    // Rôles autorisés
    private static final List<String> ROLES_AUTORISES = Arrays.asList("admin", "etudiant", "enseignant");

    private UsersService service;

    public UsersController() {
        service = new UsersService();
    }

    // ─────────────────────────────────────────────────────────
    //  Validation centralisée de tous les attributs
    // ─────────────────────────────────────────────────────────

    /**
     * Valide tous les champs d'un utilisateur.
     * Retourne null si tout est OK, sinon le message d'erreur.
     */
    private String validerUser(Users u) {

        // ── Prénom ──────────────────────────────────────────
        if (u.getFirstName() == null || u.getFirstName().trim().isEmpty()) {
            return "❌ Erreur : Le prénom est obligatoire !";
        }
        if (!u.getFirstName().trim().matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            return "❌ Erreur : Le prénom ne doit contenir que des lettres !";
        }

        // ── Nom ─────────────────────────────────────────────
        if (u.getLastName() == null || u.getLastName().trim().isEmpty()) {
            return "❌ Erreur : Le nom est obligatoire !";
        }
        if (!u.getLastName().trim().matches("[a-zA-ZÀ-ÿ\\s\\-']+")) {
            return "❌ Erreur : Le nom ne doit contenir que des lettres !";
        }

        // ── Email ────────────────────────────────────────────
        if (u.getEmail() == null || u.getEmail().trim().isEmpty()) {
            return "❌ Erreur : L'email est obligatoire !";
        }
        // Doit contenir @ et au moins un point après @
        if (!u.getEmail().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            return "❌ Erreur : L'email est invalide ! Il doit contenir '@' et un '.' (ex: exemple@domaine.com)";
        }

        // ── Mot de passe ─────────────────────────────────────
        boolean isHashed = u.getPassword() != null && u.getPassword().length() == 64 && u.getPassword().matches("^[a-f0-9]{64}$");
        if (!isHashed) {
            if (u.getPassword() == null || u.getPassword().trim().isEmpty()) {
                return "❌ Erreur : Le mot de passe est obligatoire !";
            }
            if (u.getPassword().length() < 8) {
                return "❌ Erreur : Le mot de passe doit contenir au moins 8 caractères !";
            }
            if (!u.getPassword().matches(".*[A-Z].*")) {
                return "❌ Erreur : Le mot de passe doit contenir au moins une lettre majuscule !";
            }
            if (!u.getPassword().matches(".*[0-9].*")) {
                return "❌ Erreur : Le mot de passe doit contenir au moins un chiffre !";
            }
        }

        // ── Rôle ─────────────────────────────────────────────
        if (u.getRole() == null || u.getRole().trim().isEmpty()) {
            return "❌ Erreur : Le rôle est obligatoire !";
        }
        if (!ROLES_AUTORISES.contains(u.getRole().trim().toLowerCase())) {
            return "❌ Erreur : Le rôle doit être l'un des suivants : admin, etudiant, enseignant !";
        }

        // ── Profession ────────────────────────────────────────
        if (u.getProfession() == null || u.getProfession().trim().isEmpty()) {
            return "❌ Erreur : La profession est obligatoire !";
        }

        // ── Niveau d'expérience ───────────────────────────────
        if (u.getExperienceLevel() == null || u.getExperienceLevel().trim().isEmpty()) {
            return "❌ Erreur : Le niveau d'expérience est obligatoire !";
        }

        // ── Statut ────────────────────────────────────────────
        if (u.getStatut() == null || u.getStatut().trim().isEmpty()) {
            return "❌ Erreur : Le statut est obligatoire !";
        }

        return null; // Aucune erreur
    }

    // ─────────────────────────────────────────────────────────
    //  CRUD
    // ─────────────────────────────────────────────────────────

    public String ajouterUser(Users u) {
        try {
            // Validation complète
            String erreur = validerUser(u);
            if (erreur != null) {
                System.out.println(erreur);
                return erreur;
            }

            // Vérifier unicité email
            if (service.findByEmail(u.getEmail()) != null) {
                String errEmail = "❌ Erreur : Cet email est déjà utilisé !";
                System.out.println(errEmail);
                return errEmail;
            }

            // Hachage SHA-256 avant insertion
            u.setPassword(hashPassword(u.getPassword()));

            service.ajouter(u);
            System.out.println("✅ Utilisateur ajouté avec succès !");
            return null; // Succès
        } catch (SQLException ex) {
            String errSql = "❌ Erreur lors de l'ajout : " + ex.getMessage();
            System.out.println(errSql);
            return errSql;
        }
    }

    public static String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Users> recupererUsers() {
        try {
            return service.recuperer();
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la récupération : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public String modifierUser(Users u) {
        try {
            if (u.getId() <= 0) {
                String err = "❌ Erreur : ID invalide !";
                System.out.println(err);
                return err;
            }

            // Validation complète avant modification
            String erreur = validerUser(u);
            if (erreur != null) {
                System.out.println(erreur);
                return erreur;
            }

            // Si l'email a changé, vérifier qu'il n'est pas déjà pris par un autre utilisateur
            Users existing = service.findByEmail(u.getEmail());
            if (existing != null && existing.getId() != u.getId()) {
                String err = "❌ Erreur : Cet email est déjà utilisé par un autre utilisateur !";
                System.out.println(err);
                return err;
            }

            service.modifier(u);
            System.out.println("✅ Utilisateur modifié avec succès !");
            return null;
        } catch (SQLException ex) {
            String err = "❌ Erreur lors de la modification : " + ex.getMessage();
            System.out.println(err);
            return err;
        }
    }

    public String changePassword(int userId, String rawNewPassword) {
        // Validate password rules on the raw (plain text) input
        if (rawNewPassword == null || rawNewPassword.trim().isEmpty()) {
            return "❌ Le mot de passe ne peut pas être vide !";
        }
        if (rawNewPassword.length() < 6) {
            return "❌ Le mot de passe doit contenir au moins 6 caractères !";
        }
        if (!rawNewPassword.matches(".*[A-Z].*")) {
            return "❌ Le mot de passe doit contenir au moins une majuscule !";
        }
        if (!rawNewPassword.matches(".*[0-9].*")) {
            return "❌ Le mot de passe doit contenir au moins un chiffre !";
        }
        try {
            String hashed = hashPassword(rawNewPassword);
            service.updatePassword(userId, hashed);
            System.out.println("✅ Mot de passe modifié avec succès !");
            return null;
        } catch (SQLException ex) {
            String err = "❌ Erreur lors du changement de mot de passe : " + ex.getMessage();
            System.out.println(err);
            return err;
        }
    }

    public void supprimerUser(int id) {
        try {
            if (service.findById(id) == null) {
                System.out.println("❌ Utilisateur introuvable !");
                return;
            }
            service.supprimer(id);
            System.out.println("✅ Utilisateur supprimé avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur lors de la suppression : " + e.getMessage());
        }
    }

    public Users findById(int id) {
        try {
            return service.findById(id);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return null;
        }
    }

    public Users findByEmail(String email) {
        try {
            return service.findByEmail(email);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return null;
        }
    }

    public List<Users> findByRole(String role) {
        try {
            return service.findByRole(role);
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public void changerStatut(int id, String statut) {
        try {
            if (service.findById(id) == null) {
                System.out.println("❌ Utilisateur introuvable !");
                return;
            }
            service.changerStatut(id, statut);
            System.out.println("✅ Statut modifié avec succès !");
        } catch (SQLException e) {
            System.out.println("❌ Erreur : " + e.getMessage());
        }
    }
}
