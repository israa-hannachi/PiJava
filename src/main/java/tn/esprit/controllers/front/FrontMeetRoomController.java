package tn.esprit.controllers.front;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.esprit.controllers.meet.MeetParticipantsController;
import tn.esprit.controllers.meet.ParticipantController;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.Meet_Participants;
import tn.esprit.entities.meet.participant;
import tn.esprit.entities.users.Users;
import tn.esprit.services.JitsiMeetService;
import tn.esprit.views.JcefBrowserHelper;

import javax.swing.*;
import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

public class FrontMeetRoomController implements Initializable {

    @FXML private Label meetTitleLabel;
    @FXML private Label meetStatusLabel;
    @FXML private Label connectedAsLabel;
    @FXML private StackPane browserContainer;
    @FXML private VBox loadingOverlay;
    @FXML private Label loadingStatusLabel;
    @FXML private Label infoTitleLabel;
    @FXML private Label infoDescLabel;
    @FXML private Label infoTimeLabel;
    @FXML private Label concentrationStatusLabel;
    @FXML private ProgressBar concentrationBar;
    @FXML private Label participantCountLabel;
    @FXML private VBox participantListBox;

    private Users currentUser;
    private Meet currentMeet;
    private participant currentParticipant;

    private final JitsiMeetService jitsiService = new JitsiMeetService();
    private final MeetParticipantsController mpController = new MeetParticipantsController();
    private final ParticipantController participantController = new ParticipantController();
    private final DateTimeFormatter dtFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private String roomUrl;
    private org.cef.browser.CefBrowser cefBrowser;
    private JFrame browserFrame;

    private int distractionSeconds = 0;
    private javax.sound.sampled.SourceDataLine alertLine;
    private ChangeListener<Number> xListener, yListener, wListener, hListener;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void initData(Users user, Meet meet, participant participantObj) {
        this.currentUser = user;
        this.currentMeet = meet;
        this.currentParticipant = participantObj;
        this.roomUrl = buildJitsiUrl();
        updateMeetInfo();
        loadParticipants();
        initializeBrowser();
    }

    private String buildJitsiUrl() {
        String roomName = jitsiService.generateRoomName(currentMeet);
        String name = (currentParticipant != null) ? (currentParticipant.getPrenom() + " " + currentParticipant.getNom()).trim() : "Participant";
        try { name = java.net.URLEncoder.encode(name, "UTF-8"); } catch (Exception ignored) {}
        return "https://" + jitsiService.getJitsiDomain() + "/" + roomName + "#userInfo.displayName=\"" + name + "\"&config.prejoinPageEnabled=false&config.startWithAudioMuted=true&config.disableDeepLinking=true";
    }

    private void initializeBrowser() {
        loadingOverlay.setVisible(true);
        Thread initThread = new Thread(() -> {
            try {
                JcefBrowserHelper helper = JcefBrowserHelper.getInstance();
                helper.initialize();
                SwingUtilities.invokeAndWait(() -> {
                    cefBrowser = helper.createBrowser(roomUrl);
                    org.cef.browser.CefMessageRouter msgRouter = org.cef.browser.CefMessageRouter.create();
                    msgRouter.addHandler(new org.cef.handler.CefMessageRouterHandlerAdapter() {
                        @Override
                        public boolean onQuery(org.cef.browser.CefBrowser browser, org.cef.browser.CefFrame frame, long queryId, String request, boolean persistent, org.cef.callback.CefQueryCallback callback) {
                            if (request.startsWith("concentration:")) {
                                try {
                                    double score = Double.parseDouble(request.split(":")[1]);
                                    Platform.runLater(() -> updateConcentrationUI(score));
                                } catch (Exception ignored) {}
                                return true;
                            }
                            if (request.startsWith("participantCount:")) {
                                try {
                                    int count = Integer.parseInt(request.split(":")[1]);
                                    Platform.runLater(() -> participantCountLabel.setText(count + " Participants"));
                                } catch (Exception ignored) {}
                                return true;
                            }
                            return false;
                        }
                    }, true);
                    helper.getCefClient().addMessageRouter(msgRouter);
                    browserFrame = new JFrame();
                    browserFrame.setUndecorated(true);
                    browserFrame.setAlwaysOnTop(true);
                    browserFrame.getContentPane().setLayout(new BorderLayout());
                    browserFrame.getContentPane().add(cefBrowser.getUIComponent(), BorderLayout.CENTER);
                });
                Platform.runLater(() -> {
                    updateBrowserFramePosition();
                    SwingUtilities.invokeLater(() -> { if (browserFrame != null) browserFrame.setVisible(true); });
                    new java.util.Timer().schedule(new java.util.TimerTask() { @Override public void run() { injectConcentrationIA(); } }, 8000);
                    loadingOverlay.setVisible(false);
                    attachPositionListeners();
                });
            } catch (Exception e) { e.printStackTrace(); }
        });
        initThread.start();
    }

    private void updateBrowserFramePosition() {
        if (browserFrame == null || browserContainer == null) return;
        Scene scene = browserContainer.getScene();
        if (scene == null || scene.getWindow() == null) return;
        Window window = scene.getWindow();
        Bounds bounds = browserContainer.localToScene(browserContainer.getBoundsInLocal());
        double x = window.getX() + scene.getX() + bounds.getMinX();
        double y = window.getY() + scene.getY() + bounds.getMinY();
        double w = bounds.getWidth();
        double h = Math.min(bounds.getHeight(), window.getY() + window.getHeight() - y - 10);
        if (w > 50 && h > 50) {
            SwingUtilities.invokeLater(() -> { if (browserFrame != null) browserFrame.setBounds((int)x, (int)y, (int)w, (int)h); });
        }
    }

    private void attachPositionListeners() {
        Stage stage = (Stage) browserContainer.getScene().getWindow();
        xListener = (obs, o, n) -> updateBrowserFramePosition();
        stage.xProperty().addListener(xListener);
        stage.yProperty().addListener(xListener);
        stage.widthProperty().addListener(xListener);
        stage.heightProperty().addListener(xListener);
        browserContainer.widthProperty().addListener(xListener);
        browserContainer.heightProperty().addListener(xListener);
    }

    private void closeBrowser() {
        if (browserFrame != null) { SwingUtilities.invokeLater(() -> { browserFrame.setVisible(false); browserFrame.dispose(); browserFrame = null; }); }
        if (cefBrowser != null) { cefBrowser.close(false); cefBrowser = null; }
        stopAlertSound();
    }

    private void updateMeetInfo() {
        if (currentMeet == null) return;
        meetTitleLabel.setText(currentMeet.getTitre());
        infoTitleLabel.setText(currentMeet.getTitre());
        infoDescLabel.setText(currentMeet.getDescription());
        String debut = currentMeet.getDateDebut() != null ? currentMeet.getDateDebut().toLocalDateTime().format(dtFormatter) : "N/A";
        infoTimeLabel.setText(debut);
    }

    private void loadParticipants() {
        if (currentMeet == null) return;
        participantListBox.getChildren().clear();
        List<Meet_Participants> meetParts = mpController.getParticipantsDuMeet(currentMeet.getId());
        participantCountLabel.setText(meetParts.size() + " participant(s)");
        List<participant> allParts = participantController.recupererParticipants();
        for (Meet_Participants mp : meetParts) {
            participant p = allParts.stream().filter(pt -> pt.getId() == mp.getParticipantId()).findFirst().orElse(null);
            if (p != null) {
                Label lbl = new Label(p.getPrenom() + " " + p.getNom());
                lbl.setStyle("-fx-text-fill:#475569; -fx-padding:4 8;");
                participantListBox.getChildren().add(lbl);
            }
        }
    }

    private void updateConcentrationUI(double score) {
        concentrationBar.setProgress(score);
        if (score > 0.7) {
            distractionSeconds = 0;
            concentrationStatusLabel.setText("✅ Concentré");
            concentrationBar.setStyle("-fx-accent: #10b981;");
            cefBrowser.executeJavaScript("if(window.hideAlert) hideAlert();", "", 0);
            stopAlertSound();
        } else {
            distractionSeconds++;
            concentrationStatusLabel.setText("⚠️ Inattentif (" + distractionSeconds + "s)");
            concentrationBar.setStyle("-fx-accent: #ef4444;");
            if (distractionSeconds == 10) {
                cefBrowser.executeJavaScript("if(window.showAlert) showAlert();", "", 0);
                playAlertSound();
            }
        }
    }

    private void playAlertSound() {
        if (alertLine != null && alertLine.isRunning()) return;
        new Thread(() -> {
            try {
                float sampleRate = 8000f;
                byte[] buf = new byte[8000];
                for (int i = 0; i < buf.length; i++) {
                    double angle = 2.0 * Math.PI * i * 440 / sampleRate;
                    buf[i] = (byte)(Math.sin(angle) * 60);
                }
                javax.sound.sampled.AudioFormat af = new javax.sound.sampled.AudioFormat(sampleRate, 8, 1, true, false);
                alertLine = javax.sound.sampled.AudioSystem.getSourceDataLine(af);
                alertLine.open(af);
                alertLine.start();
                while (distractionSeconds >= 10 && alertLine != null && alertLine.isOpen()) {
                    alertLine.write(buf, 0, buf.length);
                    Thread.sleep(100);
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void stopAlertSound() {
        if (alertLine != null) {
            try { alertLine.stop(); alertLine.close(); } catch (Exception ignored) {}
            alertLine = null;
        }
    }

    private void injectConcentrationIA() {
        if (cefBrowser == null) return;
        String js = "const style = document.createElement('style');" +
            "style.innerHTML = '#concentration-toast { position: fixed; left: -300px; top: 100px; width: 250px; background: rgba(239, 68, 68, 0.2); backdrop-filter: blur(10px); border-left: 5px solid #ef4444; color: white; padding: 20px; border-radius: 12px; font-family: sans-serif; transition: all 0.5s; z-index: 9999; } #concentration-toast.show { left: 20px; } .pulse { width: 10px; height: 10px; background: red; border-radius: 50%; display: inline-block; margin-right: 10px; animation: p 1s infinite; } @keyframes p { 0% { transform: scale(1); opacity: 1; } 100% { transform: scale(3); opacity: 0; } }';" +
            "document.head.appendChild(style);" +
            "const t = document.createElement('div'); t.id = 'concentration-toast'; t.innerHTML = '<div><span class=\"pulse\"></span><strong>Attention !</strong></div><div style=\"margin-top:10px\">Concentration faible</div><div id=\"sc\" style=\"margin-top:5px\">Score: 0%</div>';" +
            "document.body.appendChild(t);" +
            "window.showAlert = () => t.classList.add('show'); window.hideAlert = () => t.classList.remove('show');" +
            "window.updateHUDScore = s => { const el = document.getElementById('sc'); if(el) el.innerText = 'Score: ' + Math.round(s*100) + '%'; };" +
            "const s = document.createElement('script'); s.src = 'https://cdn.jsdelivr.net/npm/@mediapipe/face_mesh'; document.head.appendChild(s);" +
            "s.onload = () => {" +
            "  const fm = new FaceMesh({locateFile: f => `https://cdn.jsdelivr.net/npm/@mediapipe/face_mesh/${f}`});" +
            "  fm.setOptions({maxNumFaces:1, refineLandmarks:true, minDetectionConfidence:0.5});" +
            "  fm.onResults(res => {" +
            "    let score = 0; if (res.multiFaceLandmarks && res.multiFaceLandmarks.length > 0) {" +
            "      const lm = res.multiFaceLandmarks[0];" +
            "      const yaw = Math.abs(lm[1].x - (lm[33].x + lm[263].x) / 2) * 10;" +
            "      const pitch = Math.abs(lm[1].y - (lm[152].y + lm[10].y) / 2) * 6;" +
            "      score = Math.max(0, 1 - Math.max(yaw, pitch));" +
            "    }" +
            "    window.cefQuery({request: 'concentration:' + score.toFixed(2)});" +
            "    if(window.updateHUDScore) window.updateHUDScore(score);" +
            "    const p = document.querySelectorAll('.remote-video-container, .videocontainer').length;" +
            "    window.cefQuery({request: 'participantCount:' + (p + 1)});" +
            "  });" +
            "  const loop = () => {" +
            "    const v = document.querySelector('video[id^=\"localVideo\"]') || document.querySelector('video');" +
            "    if (v && v.videoWidth > 0) setInterval(() => fm.send({image: v}), 1000);" +
            "    else setTimeout(loop, 2000);" +
            "  }; loop();" +
            "};";
        cefBrowser.executeJavaScript(js, "", 0);
    }

    @FXML public void handleCopyLink() {
        javafx.scene.input.ClipboardContent c = new javafx.scene.input.ClipboardContent();
        c.putString(roomUrl);
        javafx.scene.input.Clipboard.getSystemClipboard().setContent(c);
    }
    @FXML public void handleBackToList() { closeBrowser(); nav("/tn/esprit/view/front_MeetList.fxml"); }
    @FXML public void handleHome() { closeBrowser(); nav("/tn/esprit/view/front_user_dashboard.fxml"); }
    @FXML public void handleProfile() { closeBrowser(); nav("/tn/esprit/view/front_profile.fxml"); }
    @FXML public void handleCours() { closeBrowser(); nav("/tn/esprit/view/front_CoursCategories.fxml"); }
    @FXML public void handleJeux() { closeBrowser(); nav("/tn/esprit/view/front_GameList.fxml"); }
    @FXML public void handleEvents() { closeBrowser(); nav("/tn/esprit/view/frontEvent.fxml"); }
    @FXML public void handleMeets() { handleBackToList(); }
    @FXML public void handleForums() { closeBrowser(); nav("/tn/esprit/view/front_forum.fxml"); }
    @FXML public void handleLogout() { closeBrowser(); nav("/tn/esprit/view/front_login.fxml"); }

    private void nav(String f) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(f));
            Parent r = l.load();
            Object ctrl = l.getController();
            
            if (ctrl instanceof FrontUserDashboardController) ((FrontUserDashboardController)ctrl).initUser(currentUser);
            else if (ctrl instanceof FrontProfileController) ((FrontProfileController)ctrl).initUser(currentUser);
            else if (ctrl instanceof FrontCoursCategorieController) ((FrontCoursCategorieController)ctrl).initUser(currentUser);
            else if (ctrl instanceof FrontMeetListController) ((FrontMeetListController)ctrl).initUser(currentUser);
            else if (ctrl instanceof FrontMeetCalendarController) ((FrontMeetCalendarController)ctrl).initUser(currentUser);
            else if (ctrl instanceof EventFrontController) ((EventFrontController)ctrl).initUser(currentUser);
            else if (ctrl instanceof FrontForumController) ((FrontForumController)ctrl).initUser(currentUser);
            else if (ctrl instanceof FrontGameListController) ((FrontGameListController)ctrl).initUser(currentUser);

            Stage s = (Stage) meetTitleLabel.getScene().getWindow();
            s.setScene(new Scene(r));
            s.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
