package tn.esprit.entities.users;



import java.sql.Timestamp;



public class Users {

   private int id;
   private String email;
   private String password;

   private String firstName;

   private String lastName;

   private String profilePicture;

   private String profession;

   private String experienceLevel;

   private String role;

   private String statut;

   private Timestamp dateCreation;

   private String coverPicture;
   private String biometricDescriptor;
   private String googleAuthenticatorSecret;



   public Users() {}



   public Users(int id, String email, String password, String firstName, String lastName,

                String profession, String experienceLevel, String role, String statut) {

       this.id = id;

       this.email = email;

       this.password = password;

       this.firstName = firstName;

       this.lastName = lastName;

       this.profession = profession;

       this.experienceLevel = experienceLevel;

       this.role = role;

       this.statut = statut;

   }



   public Users(String email, String password, String firstName, String lastName,

                String profession, String experienceLevel, String role, String statut) {

       this.email = email;

       this.password = password;

       this.firstName = firstName;

       this.lastName = lastName;

       this.profession = profession;

       this.experienceLevel = experienceLevel;

       this.role = role;

       this.statut = statut;

   }



   public int getId() { return id; }

   public void setId(int id) { this.id = id; }



   public String getEmail() { return email; }

   public void setEmail(String email) { this.email = email; }



   public String getPassword() { return password; }

   public void setPassword(String password) { this.password = password; }



   public String getFirstName() { return firstName; }

   public void setFirstName(String firstName) { this.firstName = firstName; }



   public String getLastName() { return lastName; }

   public void setLastName(String lastName) { this.lastName = lastName; }



   public String getProfilePicture() { return profilePicture; }

   public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }



   public String getProfession() { return profession; }

   public void setProfession(String profession) { this.profession = profession; }



   public String getExperienceLevel() { return experienceLevel; }

   public void setExperienceLevel(String experienceLevel) { this.experienceLevel = experienceLevel; }



   public String getRole() { return role; }

   public void setRole(String role) { this.role = role; }



   public String getStatut() { return statut; }

   public void setStatut(String statut) { this.statut = statut; }



   public Timestamp getDateCreation() { return dateCreation; }

   public void setDateCreation(Timestamp dateCreation) { this.dateCreation = dateCreation; }



   public String getCoverPicture() { return coverPicture; }

   public void setCoverPicture(String coverPicture) { this.coverPicture = coverPicture; }

   public String getBiometricDescriptor() { return biometricDescriptor; }
   public void setBiometricDescriptor(String biometricDescriptor) { this.biometricDescriptor = biometricDescriptor; }

   public String getGoogleAuthenticatorSecret() { return googleAuthenticatorSecret; }
   public void setGoogleAuthenticatorSecret(String googleAuthenticatorSecret) { this.googleAuthenticatorSecret = googleAuthenticatorSecret; }

   public boolean isTwoFactorEnabled() {
      return googleAuthenticatorSecret != null && !googleAuthenticatorSecret.trim().isEmpty();
   }

   public boolean isFaceAuthEnabled() {
      return biometricDescriptor != null && !biometricDescriptor.trim().isEmpty();
   }

   @Override

   public String toString() {

;       return "Users{id=" + id +

                              ", email='" + email + "'" +

                              ", nom='" + firstName + " " + lastName + "'" +

                              ", role='" + role + "'" +

                              ", statut='" + statut + "'" + "}";

   }

}