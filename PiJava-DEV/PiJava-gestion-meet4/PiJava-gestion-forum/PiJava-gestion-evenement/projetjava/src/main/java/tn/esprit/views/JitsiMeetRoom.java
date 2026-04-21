package tn.esprit.views;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import tn.esprit.entities.meet.Meet;
import tn.esprit.services.JitsiMeetService;

import java.awt.Desktop;
import java.net.URI;

public class JitsiMeetRoom {

    private final Meet meet;
    private final JitsiMeetService jitsiService;

    public JitsiMeetRoom(Meet meet) {
        this.meet = meet;
        this.jitsiService = new JitsiMeetService();
    }

    public JitsiMeetRoom(Meet meet, String jitsiDomain) {
        this.meet = meet;
        this.jitsiService = new JitsiMeetService(jitsiDomain);
    }

    public void show() {
        String roomUrl = jitsiService.generateRoomUrl(meet);
        String roomName = jitsiService.generateRoomName(meet);

        // Show confirmation dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Rejoindre la réunion");
        alert.setHeaderText(meet != null ? meet.getTitre() : "Réunion");
        alert.setContentText("La réunion va s'ouvrir dans votre navigateur par défaut.\n\n" +
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
