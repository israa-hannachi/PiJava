package tn.esprit.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.Timeline;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.services.HuggingFaceService;
import tn.esprit.entities.users.Users;
import tn.esprit.controllers.front.FrontForumController;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ChatbotController implements Initializable {

    @FXML private VBox rootBox;
    @FXML private StackPane headerBox;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox messagesBox;
    @FXML private TextField messageInput;
    @FXML private Button sendButton;
    @FXML private Button micButton;
    @FXML private Label titleLabel;
    @FXML private Button backButton;
    @FXML private ComboBox<String> languageSelector;
    @FXML private Button themeToggleButton;
    @FXML private Label globeIcon;

    private final HuggingFaceService huggingFaceService = HuggingFaceService.getInstance();
    private boolean isTyping = false;
    private boolean isListening = false;
    private FadeTransition micPulseAnimation;
    
    // Audio recording variables
    private TargetDataLine targetDataLine;
    private File audioFile;
    private Thread recordingThread;
    
    // User state
    private Users currentUser;
    
    // Persist theme choice during the session
    private static boolean isDarkMode = false;

    public void initData(Users user) {
        this.currentUser = user;
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        titleLabel.setText("✨ Assistant IA ✨");
        addBotBubble("Bonjour ! Je suis votre assistant IA. Posez votre question et je vous répondrai.");
        
        // Setup language selector dynamically with all available languages
        java.util.List<String> allLanguages = java.util.Arrays.stream(java.util.Locale.getISOLanguages())
                .map(langCode -> new java.util.Locale(langCode).getDisplayLanguage(java.util.Locale.FRENCH))
                .filter(name -> name != null && !name.isEmpty())
                .map(name -> name.substring(0, 1).toUpperCase() + name.substring(1))
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
                
        languageSelector.getItems().addAll(allLanguages);
        languageSelector.setValue("Français");
        
        // Setup send button action
        sendButton.setOnAction(e -> handleSend());
        
        // Setup Enter key press for sending messages
        messageInput.setOnAction(e -> handleSend());
        
        // Auto-scroll to bottom when new messages are added
        messagesBox.heightProperty().addListener((obs, oldVal, newVal) -> {
            scrollPane.setVvalue(1.0);
        });
        
        // Initialize mic animation
        micPulseAnimation = new FadeTransition(Duration.seconds(0.5), micButton);
        micPulseAnimation.setFromValue(1.0);
        micPulseAnimation.setToValue(0.3);
        micPulseAnimation.setCycleCount(Timeline.INDEFINITE);
        micPulseAnimation.setAutoReverse(true);
        
        // Apply persisted theme on load
        applyTheme();
    }

    @FXML
    private void toggleTheme(ActionEvent event) {
        isDarkMode = !isDarkMode;
        applyTheme();
    }

    private void applyTheme() {
        if (isDarkMode) {
            themeToggleButton.setText("☀️");
            themeToggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20; -fx-cursor: hand; -fx-padding: 0 10 0 0;");
            globeIcon.setStyle("-fx-text-fill: white; -fx-font-size: 20;");
            
            rootBox.setStyle("-fx-background-color: #1e1e2e;");
            headerBox.setStyle("-fx-background-color: #2a2a35; -fx-padding: 0 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
            scrollPane.lookup(".viewport").setStyle("-fx-background-color: transparent;"); // Fix scrollpane background
            backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #a6adc8; -fx-font-size: 14; -fx-font-weight: bold; -fx-cursor: hand;");
            languageSelector.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: black; -fx-background-radius: 8; -fx-font-size: 14; -fx-padding: 2;"); // Identique au mode clair pour que les langues soient visibles
            
            // Re-style message input container
            if (messageInput.getParent() instanceof HBox) {
                ((HBox) messageInput.getParent()).setStyle("-fx-background-color: #313244; -fx-border-color: #45475a; -fx-border-radius: 24; -fx-padding: 5 15; -fx-background-radius: 24;");
            }
            if (messageInput.getParent() != null && messageInput.getParent().getParent() instanceof VBox) {
                 ((VBox) messageInput.getParent().getParent()).setStyle("-fx-background-color: #1e1e2e; -fx-padding: 20;");
            }
            messageInput.setStyle("-fx-background-color: transparent; -fx-text-fill: #cdd6f4; -fx-border-width: 0; -fx-font-size: 15;");
            
            if (!isListening) micButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #cdd6f4; -fx-font-size: 18; -fx-cursor: hand; -fx-background-radius: 20;");
        } else {
            themeToggleButton.setText("🌙");
            themeToggleButton.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-font-size: 20; -fx-cursor: hand; -fx-padding: 0 10 0 0;");
            globeIcon.setStyle("-fx-text-fill: black; -fx-font-size: 20;");
            
            rootBox.setStyle("-fx-background-color: #f8fafc;");
            headerBox.setStyle("-fx-background-color: white; -fx-padding: 0 30; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 10, 0, 0, 2);");
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-width: 0;");
            backButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 14; -fx-font-weight: bold; -fx-cursor: hand;");
            languageSelector.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: black; -fx-background-radius: 8; -fx-font-size: 14; -fx-padding: 2;");
            
            if (messageInput.getParent() instanceof HBox) {
                ((HBox) messageInput.getParent()).setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #e2e8f0; -fx-border-radius: 24; -fx-padding: 5 15; -fx-background-radius: 24;");
            }
            if (messageInput.getParent() != null && messageInput.getParent().getParent() instanceof VBox) {
                 ((VBox) messageInput.getParent().getParent()).setStyle("-fx-background-color: white; -fx-padding: 20;");
            }
            messageInput.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-border-width: 0; -fx-font-size: 15;");
            
            if (!isListening) micButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #64748b; -fx-font-size: 18; -fx-cursor: hand; -fx-background-radius: 20;");
        }
        
        // Refresh all bubbles to apply the new theme
        messagesBox.getChildren().forEach(node -> {
            if (node instanceof HBox) {
                HBox container = (HBox) node;
                boolean isUser = container.getAlignment() == Pos.CENTER_RIGHT;
                if (!container.getChildren().isEmpty() && container.getChildren().get(0) instanceof VBox) {
                    VBox bubble = (VBox) container.getChildren().get(0);
                    
                    String backgroundColor;
                    String textColor;
                    String effect;
                    
                    if (isDarkMode) {
                        backgroundColor = isUser ? "#0FB5A9" : "#313244";
                        textColor = isUser ? "#ffffff" : "#cdd6f4";
                        effect = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);";
                    } else {
                        backgroundColor = isUser ? "#0FB5A9" : "#ffffff";
                        textColor = isUser ? "#ffffff" : "#1e293b";
                        effect = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);";
                    }
                    
                    bubble.setStyle(String.format(
                        "-fx-background-color: %s; " +
                        "-fx-background-radius: 15; " +
                        "%s",
                        backgroundColor, effect
                    ));
                    
                    if (!bubble.getChildren().isEmpty() && bubble.getChildren().get(0) instanceof TextFlow) {
                        TextFlow tf = (TextFlow) bubble.getChildren().get(0);
                        if (!tf.getChildren().isEmpty() && tf.getChildren().get(0) instanceof Text) {
                            ((Text) tf.getChildren().get(0)).setFill(Color.web(textColor));
                        }
                    } else if (!bubble.getChildren().isEmpty() && bubble.getChildren().get(0) instanceof Label) {
                         // For typing indicator label
                         ((Label) bubble.getChildren().get(0)).setStyle("-fx-text-fill: " + (isDarkMode ? "#a6adc8" : "#64748b") + "; -fx-font-size: 13; -fx-font-style: italic;");
                    }
                }
            }
        });
    }

    @FXML
    private void handleVoiceInput(ActionEvent event) {
        if (!isListening) {
            // Start recording
            try {
                AudioFormat format = new AudioFormat(16000.0f, 16, 1, true, false);
                DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
                
                if (!AudioSystem.isLineSupported(info)) {
                    addBotBubble("⚠️ Microphone non supporté par votre système.");
                    return;
                }
                
                targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
                targetDataLine.open(format);
                targetDataLine.start();
                
                audioFile = File.createTempFile("voice_record", ".wav");
                
                recordingThread = new Thread(() -> {
                    try (AudioInputStream audioInputStream = new AudioInputStream(targetDataLine)) {
                        AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, audioFile);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });
                recordingThread.setDaemon(true);
                recordingThread.start();
                
                isListening = true;
                micButton.setStyle("-fx-background-color: rgba(239, 68, 68, 0.1); -fx-text-fill: #ef4444; -fx-font-size: 18; -fx-cursor: hand; -fx-background-radius: 20;");
                micPulseAnimation.play();
                messageInput.setPromptText("Écoute en cours...");
                
            } catch (Exception ex) {
                ex.printStackTrace();
                addBotBubble("⚠️ Erreur lors de l'accès au microphone: " + ex.getMessage());
            }
        } else {
            // Stop recording
            if (targetDataLine != null) {
                targetDataLine.stop();
                targetDataLine.close();
            }
            
            isListening = false;
            micPulseAnimation.stop();
            micButton.setOpacity(1.0);
            applyTheme(); // Reset to normal mic color based on theme
            messageInput.setPromptText("Traitement vocal...");
            
            String selectedLanguage = languageSelector.getValue();
            
            // Process audio with API
            Task<String> transcriptionTask = new Task<String>() {
                @Override
                protected String call() throws Exception {
                    return huggingFaceService.transcribeAudio(audioFile, selectedLanguage);
                }
                
                @Override
                protected void succeeded() {
                    String transcribedText = getValue();
                    messageInput.setPromptText("Posez votre question à l'IA...");
                    if (transcribedText != null && !transcribedText.trim().isEmpty()) {
                        messageInput.setText(transcribedText);
                    }
                }
                
                @Override
                protected void failed() {
                    getException().printStackTrace();
                    messageInput.setPromptText("Posez votre question à l'IA...");
                    addBotBubble("⚠️ Erreur de transcription: " + getException().getMessage());
                }
            };
            
            Thread thread = new Thread(transcriptionTask);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_forum.fxml"));
            Parent root = loader.load();
            
            FrontForumController controller = loader.getController();
            controller.initUser(currentUser);
            
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSend() {
        String userMessage = messageInput.getText().trim();
        
        if (userMessage.isEmpty() || isTyping) {
            return;
        }

        // Add user message
        addUserBubble(userMessage);
        
        // Clear input field
        messageInput.clear();
        
        // Disable input during processing
        setTypingState(true);
        
        // Show typing indicator
        showTypingIndicator();

        String targetLanguage = languageSelector.getValue();

        // Create background task for API call
        Task<String> apiTask = new Task<String>() {
            @Override
            protected String call() throws Exception {
                return huggingFaceService.sendMessage(userMessage, targetLanguage);
            }
            
            @Override
            protected void succeeded() {
                hideTypingIndicator();
                String response = getValue();
                addBotBubble(response);
                setTypingState(false);
            }
            
            @Override
            protected void failed() {
                hideTypingIndicator();
                addBotBubble("Erreur lors de l'appel API. Vérifiez votre connexion internet et la clé API.");
                setTypingState(false);
            }
        };

        // Start the background task
        Thread thread = new Thread(apiTask);
        thread.setDaemon(true);
        thread.start();
    }

    private void addUserBubble(String text) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER_RIGHT);
        messageContainer.setPadding(new Insets(5, 10, 5, 50));
        
        VBox bubble = createBubble(text, true);
        messageContainer.getChildren().add(bubble);
        
        messagesBox.getChildren().add(messageContainer);
    }

    private void addBotBubble(String text) {
        HBox messageContainer = new HBox();
        messageContainer.setAlignment(Pos.CENTER_LEFT);
        messageContainer.setPadding(new Insets(5, 50, 5, 10));
        
        VBox bubble = createBubble(text, false);
        messageContainer.getChildren().add(bubble);
        
        messagesBox.getChildren().add(messageContainer);
    }

    private VBox createBubble(String text, boolean isUser) {
        VBox bubble = new VBox();
        bubble.setPadding(new Insets(10, 15, 10, 15));
        bubble.setMaxWidth(600);
        bubble.setSpacing(5);
        
        String backgroundColor;
        String textColor;
        String effect;
        
        if (isDarkMode) {
            backgroundColor = isUser ? "#0FB5A9" : "#313244";
            textColor = isUser ? "#ffffff" : "#cdd6f4";
            effect = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);";
        } else {
            backgroundColor = isUser ? "#0FB5A9" : "#ffffff";
            textColor = isUser ? "#ffffff" : "#1e293b";
            effect = "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.05), 5, 0, 0, 1);";
        }
        
        bubble.setStyle(String.format(
            "-fx-background-color: %s; " +
            "-fx-background-radius: 15; " +
            "%s",
            backgroundColor, effect
        ));
        
        // Create text with word wrap
        TextFlow textFlow = new TextFlow();
        Text messageText = new Text(text);
        messageText.setFill(Color.web(textColor));
        messageText.setFont(Font.font("System", 14));
        textFlow.getChildren().add(messageText);
        
        bubble.getChildren().add(textFlow);
        return bubble;
    }

    private void showTypingIndicator() {
        HBox typingContainer = new HBox();
        typingContainer.setAlignment(Pos.CENTER_LEFT);
        typingContainer.setPadding(new Insets(5, 50, 5, 10));
        
        VBox typingBubble = new VBox();
        typingBubble.setPadding(new Insets(10, 15, 10, 15));
        typingBubble.setStyle("-fx-background-color: " + (isDarkMode ? "#313244" : "#ffffff") + "; -fx-background-radius: 15;");
        
        Label typingLabel = new Label("L'assistant IA est en train de réfléchir...");
        typingLabel.setStyle("-fx-text-fill: " + (isDarkMode ? "#a6adc8" : "#64748b") + "; -fx-font-size: 13; -fx-font-style: italic;");
        
        typingBubble.getChildren().add(typingLabel);
        typingContainer.getChildren().add(typingBubble);
        
        // Store reference to remove later
        typingContainer.setId("typingIndicator");
        messagesBox.getChildren().add(typingContainer);
    }

    private void hideTypingIndicator() {
        messagesBox.getChildren().removeIf(node -> "typingIndicator".equals(node.getId()));
    }

    private void setTypingState(boolean typing) {
        this.isTyping = typing;
        messageInput.setDisable(typing);
        sendButton.setDisable(typing);
        micButton.setDisable(typing);
        
        if (typing) {
            sendButton.setText("...");
        } else {
            sendButton.setText("Envoyer");
        }
    }
}
