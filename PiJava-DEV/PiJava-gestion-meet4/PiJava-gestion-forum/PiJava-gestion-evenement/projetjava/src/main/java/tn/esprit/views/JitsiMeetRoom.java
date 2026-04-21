package tn.esprit.views;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.Meet_Participants;
import tn.esprit.entities.meet.participant;
import tn.esprit.services.JitsiMeetService;
import tn.esprit.services.meet.MeetParticipantsService;
import tn.esprit.services.meet.ParticipantService;

import java.awt.Desktop;
import java.net.URI;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class JitsiMeetRoom {

    private final Meet meet;
    private final JitsiMeetService jitsiService;
    private final MeetParticipantsService meetParticipantsService;
    private final ParticipantService participantService;
    private participant authorizedParticipant;

    public JitsiMeetRoom(Meet meet) {
        this.meet = meet;
        this.jitsiService = new JitsiMeetService();
        this.meetParticipantsService = new MeetParticipantsService();
        this.participantService = new ParticipantService();
    }

    public JitsiMeetRoom(Meet meet, String jitsiDomain) {
        this.meet = meet;
        this.jitsiService = new JitsiMeetService(jitsiDomain);
        this.meetParticipantsService = new MeetParticipantsService();
        this.participantService = new ParticipantService();
    }

    public void show() {
        if (meet == null) {
            showError("Réunion invalide.");
            return;
        }

        // 1. Vérifier que la réunion a commencé (comme dans PIDEV join())
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        if (meet.getDateDebut() != null && meet.getDateDebut().after(now)) {
            showError("Cette réunion n'a pas encore commencé.\n\nDébut prévu : " + meet.getDateDebut());
            return;
        }

        // 2. Vérifier que la réunion n'est pas terminée (comme dans PIDEV join())
        if (meet.getDateFin() != null && meet.getDateFin().before(now)) {
            showError("Cette réunion est terminée.\n\nFin : " + meet.getDateFin());
            return;
        }

        // 3. Demander l'email et vérifier l'autorisation (comme dans PIDEV join())
        if (!authenticateParticipant()) {
            return; // L'utilisateur a annulé ou n'est pas autorisé
        }

        // 4. Afficher la salle Jitsi
        String roomUrl = jitsiService.generateRoomUrl(meet);
        String roomName = jitsiService.generateRoomName(meet);

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rejoindre la réunion");
        alert.setHeaderText(meet.getTitre());
        alert.setContentText("Bienvenue " + authorizedParticipant.getPrenom() + " " + authorizedParticipant.getNom() + " !\n\n" +
                "La réunion va s'ouvrir dans votre navigateur par défaut.\n\n" +
                "Room: " + roomName + "\n" +
                "URL: " + roomUrl);

        ButtonType openBtn = new ButtonType("Ouvrir dans le navigateur");
        ButtonType copyBtn = new ButtonType("Copier le lien");
        ButtonType cancelBtn = new ButtonType("Annuler", ButtonType.CANCEL.getButtonData());

        alert.getButtonTypes().setAll(openBtn, copyBtn, cancelBtn);

        alert.showAndWait().ifPresent(result -> {
            if (result == openBtn) {
                openInBrowser(roomUrl);
            } else if (result == copyBtn) {
                copyRoomLink(roomUrl);
            }
        });
    }

    private boolean authenticateParticipant() {
        // Créer une fenêtre moderne personnalisée
        Stage authStage = new Stage();
        authStage.initModality(Modality.APPLICATION_MODAL);
        authStage.setTitle("Accès à la réunion");
        authStage.setResizable(false);

        // Conteneur principal
        VBox mainContainer = new VBox(0);
        mainContainer.setAlignment(javafx.geometry.Pos.CENTER);
        mainContainer.setStyle("-fx-background-color: white; -fx-background-radius: 6px;");

        // En-tête simple gris
        VBox header = new VBox(12);
        header.setAlignment(javafx.geometry.Pos.CENTER);
        header.setPadding(new Insets(30, 40, 25, 40));
        header.setStyle("-fx-background-color: #334155; -fx-background-radius: 6px 6px 0 0;");

        // Titre de la réunion
        Label titleLabel = new Label(meet.getTitre());
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white; -fx-wrap-text: true;");
        titleLabel.setMaxWidth(350);

        // Sous-titre
        Label subtitleLabel = new Label("Verification d'email requise");
        subtitleLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");

        header.getChildren().addAll(titleLabel, subtitleLabel);

        // Corps du formulaire
        VBox formBody = new VBox(20);
        formBody.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        formBody.setPadding(new Insets(30, 40, 30, 40));
        formBody.setStyle("-fx-background-color: white;");

        // Label email
        Label emailLabel = new Label("Adresse email");
        emailLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #475569;");

        // Champ email
        TextField emailField = new TextField();
        emailField.setPromptText("votre.email@exemple.com");
        emailField.setStyle(
            "-fx-background-color: #f1f5f9; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-width: 1px; " +
            "-fx-border-radius: 6px; " +
            "-fx-background-radius: 6px; " +
            "-fx-padding: 10 14; " +
            "-fx-font-size: 13px; " +
            "-fx-text-fill: #1e293b;"
        );
        emailField.setPrefWidth(320);

        // Focus effect
        emailField.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) {
                emailField.setStyle(
                    "-fx-background-color: #ffffff; " +
                    "-fx-border-color: #64748b; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-radius: 6px; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 10 14; " +
                    "-fx-font-size: 13px; " +
                    "-fx-text-fill: #1e293b;"
                );
            } else {
                emailField.setStyle(
                    "-fx-background-color: #f1f5f9; " +
                    "-fx-border-color: #e2e8f0; " +
                    "-fx-border-width: 1px; " +
                    "-fx-border-radius: 6px; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-padding: 10 14; " +
                    "-fx-font-size: 13px; " +
                    "-fx-text-fill: #1e293b;"
                );
            }
        });

        // Info text
        Label infoLabel = new Label("Seuls les participants inscrits peuvent rejoindre cette reunion.");
        infoLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-wrap-text: true;");
        infoLabel.setMaxWidth(320);

        formBody.getChildren().addAll(emailLabel, emailField, infoLabel);

        // Pied avec boutons
        HBox footer = new HBox(12);
        footer.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
        footer.setPadding(new Insets(0, 40, 30, 40));
        footer.setStyle("-fx-background-color: white; -fx-background-radius: 0 0 6px 6px;");

        // Bouton Annuler
        Button cancelBtn = new Button("Annuler");
        cancelBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #64748b; " +
            "-fx-font-weight: 500; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6px; " +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
            "-fx-background-color: #f1f5f9; " +
            "-fx-text-fill: #475569; " +
            "-fx-font-weight: 500; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6px; " +
            "-fx-cursor: hand;"
        ));
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
            "-fx-background-color: transparent; " +
            "-fx-text-fill: #64748b; " +
            "-fx-font-weight: 500; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6px; " +
            "-fx-cursor: hand;"
        ));

        // Bouton Rejoindre
        Button joinBtn = new Button("Rejoindre la reunion");
        joinBtn.setStyle(
            "-fx-background-color: #475569; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: 600; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 6px; " +
            "-fx-cursor: hand;"
        );
        joinBtn.setDisable(true);

        // Hover effect pour bouton rejoindre
        joinBtn.setOnMouseEntered(e -> {
            if (!joinBtn.isDisabled()) {
                joinBtn.setStyle(
                    "-fx-background-color: #334155; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 10 20; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-cursor: hand;"
                );
            }
        });
        joinBtn.setOnMouseExited(e -> {
            if (!joinBtn.isDisabled()) {
                joinBtn.setStyle(
                    "-fx-background-color: #475569; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 10 20; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-cursor: hand;"
                );
            }
        });

        // Activer/desactiver selon saisie
        emailField.textProperty().addListener((obs, old, newVal) -> {
            boolean hasText = newVal != null && !newVal.trim().isEmpty();
            joinBtn.setDisable(!hasText);
            if (hasText) {
                joinBtn.setStyle(
                    "-fx-background-color: #475569; " +
                    "-fx-text-fill: white; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 10 20; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-cursor: hand;"
                );
            } else {
                joinBtn.setStyle(
                    "-fx-background-color: #cbd5e1; " +
                    "-fx-text-fill: #94a3b8; " +
                    "-fx-font-weight: 600; " +
                    "-fx-padding: 10 20; " +
                    "-fx-background-radius: 6px; " +
                    "-fx-cursor: default;"
                );
            }
        });

        footer.getChildren().addAll(cancelBtn, joinBtn);

        // Assembler
        mainContainer.getChildren().addAll(header, formBody, footer);

        // Ombre portee subtile
        mainContainer.setEffect(new javafx.scene.effect.DropShadow(8, 0, 4, javafx.scene.paint.Color.rgb(0, 0, 0, 0.08)));

        Scene scene = new Scene(mainContainer);
        scene.setFill(javafx.scene.paint.Color.TRANSPARENT);
        authStage.setScene(scene);

        // Variable pour stocker le résultat
        final String[] emailResult = { null };

        // Actions
        cancelBtn.setOnAction(e -> authStage.close());
        joinBtn.setOnAction(e -> {
            emailResult[0] = emailField.getText().trim();
            authStage.close();
        });

        // Entrée = rejoindre
        emailField.setOnAction(e -> {
            if (!joinBtn.isDisabled()) {
                emailResult[0] = emailField.getText().trim();
                authStage.close();
            }
        });

        authStage.showAndWait();

        // Vérifier si l'utilisateur a annulé
        if (emailResult[0] == null || emailResult[0].isEmpty()) {
            return false;
        }

        String email = emailResult[0];

        // Vérifier que l'email appartient à un participant inscrit
        try {
            List<Meet_Participants> meetParticipants = meetParticipantsService.findByMeetId(meet.getId());

            boolean authorized = false;
            for (Meet_Participants mp : meetParticipants) {
                participant p = participantService.findById(mp.getParticipantId());
                if (p != null && p.getEmail() != null &&
                    p.getEmail().trim().equalsIgnoreCase(email)) {
                    authorized = true;
                    authorizedParticipant = p;
                    break;
                }
            }

            if (!authorized) {
                showModernError("Accès refusé", "L'email \"" + email + "\" n'est pas inscrit à cette réunion.", authStage);
                return false;
            }

            return true;

        } catch (SQLException e) {
            showModernError("Erreur", "Impossible de vérifier l'accès : " + e.getMessage(), authStage);
            return false;
        }
    }

    private void showModernError(String title, String message, Stage owner) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 6px;"
        );
        alert.showAndWait();
    }

    private void openInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                showError("Impossible d'ouvrir le navigateur. Veuillez copier le lien manuellement.");
            }
        } catch (Exception e) {
            showError("Erreur lors de l'ouverture du navigateur: " + e.getMessage());
        }
    }

    private void copyRoomLink(String roomUrl) {
        javafx.scene.input.Clipboard clipboard = javafx.scene.input.Clipboard.getSystemClipboard();
        javafx.scene.input.ClipboardContent content = new javafx.scene.input.ClipboardContent();
        content.putString(roomUrl);
        clipboard.setContent(content);

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Lien copié");
        info.setHeaderText(null);
        info.setContentText("Le lien de la réunion a été copié dans le presse-papiers.\n\n" + roomUrl);
        info.showAndWait();
    }

    private void showError(String message) {
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Erreur");
        error.setHeaderText(null);
        error.setContentText(message);
        error.showAndWait();
    }
}
