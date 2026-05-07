package tn.esprit.controllers.event;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import tn.esprit.controllers.front.AdminDashboardController;
import tn.esprit.entities.users.Users;
import tn.esprit.controllers.front.UserIndexController;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Registration;
import tn.esprit.entities.event.Sponsor;
import tn.esprit.services.event.EventChatBotService;
import tn.esprit.services.event.EventService;
import tn.esprit.services.event.RegistrationService;
import tn.esprit.services.event.SponsorService;
import tn.esprit.services.event.EventContentGenerationService;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class BackEventController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private TableView<Event> eventTable;
    @FXML private TableColumn<Event, Integer> colEventId;
    @FXML private TableColumn<Event, String> colEventTitle;
    @FXML private TableColumn<Event, Timestamp> colEventDate;
    @FXML private TableColumn<Event, String> colEventLocation;
    @FXML private TableColumn<Event, Integer> colEventCapacity;
    @FXML private TableColumn<Event, Integer> colEventInscrits;
    @FXML private TableColumn<Event, BigDecimal> colEventPrice;
    @FXML private TextField searchEvent;

    @FXML private TableView<Sponsor> subSponsorTable;
    @FXML private TableColumn<Sponsor, String> colSubSponsorNom;
    @FXML private TableColumn<Sponsor, String> colSubSponsorType;

    @FXML private TableView<Registration> subRegistrationTable;
    @FXML private TableColumn<Registration, String> colSubRegName;
    @FXML private TableColumn<Registration, String> colSubRegStatus;

    @FXML private TableView<Registration> allRegistrationTable;
    @FXML private TableColumn<Registration, Integer> colRegId;
    @FXML private TableColumn<Registration, String> colRegEvent;
    @FXML private TableColumn<Registration, String> colRegName;
    @FXML private TableColumn<Registration, String> colRegEmail;
    @FXML private TableColumn<Registration, Timestamp> colRegDate;
    @FXML private TableColumn<Registration, String> colRegStatus;
    @FXML private TableColumn<Registration, BigDecimal> colRegAmount;
    @FXML private TableColumn<Registration, String> colRegPaymentStatus;
    @FXML private TableColumn<Registration, String> colRegMode;
    @FXML private TextField searchReg;

    @FXML private TableView<Sponsor> allSponsorTable;
    @FXML private TableColumn<Sponsor, Integer> colSponId;
    @FXML private TableColumn<Sponsor, String> colSponNom;
    @FXML private TableColumn<Sponsor, String> colSponType;
    @FXML private TableColumn<Sponsor, String> colSponEmail;
    @FXML private TableColumn<Sponsor, String> colSponEvent;
    @FXML private TableColumn<Sponsor, BigDecimal> colSponMontant;
    @FXML private TextField searchSponsor;

    // Charts
    @FXML private BarChart<String, Number> capacityChart;
    @FXML private PieChart sponsorPieChart;
    @FXML private Label selectedEventMetaLabel;
    @FXML private TextArea backChatArea;
    @FXML private TextField backChatInput;

    // Sidebar Submenus
    @FXML private VBox comptesSubmenu;
    @FXML private VBox coursSubmenu;
    @FXML private VBox jeuxSubmenu;
    @FXML private VBox forumSubmenu;
    @FXML private VBox eventsSubmenu;
    @FXML private VBox meetSubmenu;
    @FXML private VBox mailingSubmenu;
    @FXML private Label adminNameLabel;

    private final EventService eventService = new EventService();
    private final SponsorService sponsorService = new SponsorService();
    private final EventContentGenerationService contentGenerationService = new EventContentGenerationService();
    private final RegistrationService registrationService = new RegistrationService();
    private final EventChatBotService eventChatBotService =
            new EventChatBotService(eventService, sponsorService, registrationService);

    private Users currentUser;
    private ObservableList<Event> eventData = FXCollections.observableArrayList();
    private ObservableList<Registration> registrationData = FXCollections.observableArrayList();
    private ObservableList<Sponsor> sponsorData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadAllData();
        setupSearch();
        setupSelectionListeners();
        initializeBackAssistant();
        
        // Ensure events submenu is expanded by default since we are in Events module
        if (eventsSubmenu != null) {
            eventsSubmenu.setVisible(true);
            eventsSubmenu.setManaged(true);
        }
    }

    public void initAdmin(Users user) {
        this.currentUser = user;
        if (user != null && adminNameLabel != null) {
            adminNameLabel.setText("👑 " + user.getFirstName() + " " + user.getLastName());
        }
    }

    public void selectTab(int index) {
        if (tabPane != null && index >= 0 && index < tabPane.getTabs().size()) {
            tabPane.getSelectionModel().select(index);
        }
    }

    private void setupTableColumns() {
        // Hide IDs
        colEventId.setVisible(false);
        colRegId.setVisible(false);
        colSponId.setVisible(false);

        // Events
        colEventId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colEventTitle.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colEventDate.setCellValueFactory(new PropertyValueFactory<>("dateDebut"));
        colEventLocation.setCellValueFactory(new PropertyValueFactory<>("lieu"));
        colEventCapacity.setCellValueFactory(new PropertyValueFactory<>("capacite"));
        colEventInscrits.setCellValueFactory(new PropertyValueFactory<>("inscrits"));
        colEventPrice.setCellValueFactory(new PropertyValueFactory<>("prix"));

        // Sub Tables
        colSubSponsorNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSubSponsorType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSubRegName.setCellValueFactory(new PropertyValueFactory<>("visitorName"));
        colSubRegStatus.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // All Registrations
        colRegId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRegEvent.setCellValueFactory(cellData -> {
            int eventId = cellData.getValue().getEvenementId();
            Event e = eventData.stream().filter(ev -> ev.getId() == eventId).findFirst().orElse(null);
            return new javafx.beans.property.SimpleStringProperty(e != null ? e.getTitre() : "Inconnu (" + eventId + ")");
        });
        colRegName.setCellValueFactory(new PropertyValueFactory<>("visitorName"));
        colRegEmail.setCellValueFactory(new PropertyValueFactory<>("visitorEmail"));
        colRegDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colRegStatus.setCellValueFactory(new PropertyValueFactory<>("statut"));
        colRegAmount.setCellValueFactory(new PropertyValueFactory<>("montantPaye"));
        colRegPaymentStatus.setCellValueFactory(new PropertyValueFactory<>("paiementStatut"));
        colRegMode.setCellValueFactory(new PropertyValueFactory<>("modePaiement"));

        // All Sponsors
        colSponId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSponNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSponType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSponEmail.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        colSponEvent.setCellValueFactory(cellData -> {
            int eventId = cellData.getValue().getEventId();
            Event e = eventData.stream().filter(ev -> ev.getId() == eventId).findFirst().orElse(null);
            return new javafx.beans.property.SimpleStringProperty(e != null ? e.getTitre() : "Inconnu (" + eventId + ")");
        });
        colSponMontant.setCellValueFactory(new PropertyValueFactory<>("montant"));
    }

    private void loadAllData() {
        try {
            eventData.setAll(eventService.recuperer());
            eventTable.setItems(eventData);

            registrationData.setAll(registrationService.recuperer());
            allRegistrationTable.setItems(registrationData);

            sponsorData.setAll(sponsorService.recuperer());
            allSponsorTable.setItems(sponsorData);
            
            updateStatistics();
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
        }
    }

    private void updateStatistics() {
        if (capacityChart != null && sponsorPieChart != null) {
            // BarChart
            capacityChart.getData().clear();
            XYChart.Series<String, Number> seriesCapacite = new XYChart.Series<>();
            seriesCapacite.setName("Capacité Max");
            XYChart.Series<String, Number> seriesInscrits = new XYChart.Series<>();
            seriesInscrits.setName("Inscrits");

            for (Event e : eventData) {
                // Shorten title for display with null safety
                String title = e.getTitre() != null ? e.getTitre() : "Sans titre";
                String lbl = title.length() > 15 ? title.substring(0, 15) + "..." : title;
                seriesCapacite.getData().add(new XYChart.Data<>(lbl, e.getCapacite()));
                seriesInscrits.getData().add(new XYChart.Data<>(lbl, e.getInscrits()));
            }
            capacityChart.getData().addAll(seriesCapacite, seriesInscrits);

            // PieChart
            sponsorPieChart.getData().clear();
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
            sponsorData.stream()
                .sorted((s1, s2) -> {
                    if (s1.getMontant() == null) return 1;
                    if (s2.getMontant() == null) return -1;
                    return s2.getMontant().compareTo(s1.getMontant());
                })
                .limit(5)
                .forEach(s -> {
                    if (s.getMontant() != null && s.getMontant().compareTo(BigDecimal.ZERO) > 0) {
                        pieData.add(new PieChart.Data(s.getNom() + " (" + s.getMontant() + "DT)", s.getMontant().doubleValue()));
                    }
                });
            sponsorPieChart.setData(pieData);
        }
    }

    private void setupSearch() {
        searchEvent.textProperty().addListener((obs, oldV, newV) -> {
            eventTable.setItems(eventData.filtered(e -> e.getTitre().toLowerCase().contains(newV.toLowerCase()) || e.getLieu().toLowerCase().contains(newV.toLowerCase())));
        });
        searchReg.textProperty().addListener((obs, oldV, newV) -> {
            allRegistrationTable.setItems(registrationData.filtered(r -> r.getVisitorName().toLowerCase().contains(newV.toLowerCase()) || r.getVisitorEmail().toLowerCase().contains(newV.toLowerCase())));
        });
        searchSponsor.textProperty().addListener((obs, oldV, newV) -> {
            allSponsorTable.setItems(sponsorData.filtered(s -> s.getNom().toLowerCase().contains(newV.toLowerCase())));
        });
    }

    private void setupSelectionListeners() {
        eventTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                updateSubTables(newSelection.getId());
                refreshBackAssistantContext(newSelection);
            } else {
                subSponsorTable.getItems().clear();
                subRegistrationTable.getItems().clear();
                refreshBackAssistantContext(null);
            }
        });
    }

    private void updateSubTables(int eventId) {
        try {
            subSponsorTable.setItems(FXCollections.observableArrayList(sponsorService.findByEventId(eventId)));
            subRegistrationTable.setItems(FXCollections.observableArrayList(registrationService.recuperer().stream().filter(r -> r.getEvenementId() == eventId).collect(Collectors.toList())));
        } catch (SQLException e) {
            System.err.println("Erreur maj tables liées: " + e.getMessage());
        }
    }

    private void initializeBackAssistant() {
        if (backChatArea != null) {
            backChatArea.setText("Bot: Bonjour admin. Je peux vous aider sur les evenements, les sponsors, les inscriptions et les recommandations de gestion.\n");
        }
        refreshBackAssistantContext(eventTable != null ? eventTable.getSelectionModel().getSelectedItem() : null);
    }

    private void refreshBackAssistantContext(Event selectedEvent) {
        if (selectedEventMetaLabel == null) {
            return;
        }
        if (selectedEvent == null) {
            selectedEventMetaLabel.setText("Selectionnez un evenement pour obtenir une analyse plus precise.");
            return;
        }
        selectedEventMetaLabel.setText("Event selectionne: " + selectedEvent.getTitre()
                + " | Lieu: " + selectedEvent.getLieu()
                + " | Places: " + selectedEvent.getInscrits() + "/" + selectedEvent.getCapacite());
    }

    @FXML
    void handleAddEvent(ActionEvent event) {
        showEventDialog(null);
    }

    @FXML
    void handleEditEvent(ActionEvent event) {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showEventDialog(selected);
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un événement.");
        }
    }

    @FXML
    void handleDeleteEvent(ActionEvent event) {
        Event selected = eventTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Êtes-vous sûr de vouloir supprimer cet événement ? Cela supprimera également tous les sponsors et inscriptions liés.", ButtonType.YES, ButtonType.NO);
            if (confirm.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                try {
                    eventService.supprimer(selected.getId());
                    loadAllData();
                } catch (SQLException e) {
                    showAlert("Erreur", "Suppression impossible : " + e.getMessage());
                }
            }
        }
    }

    @FXML
    void handleAddSponsor(ActionEvent event) {
        showSponsorDialog(null);
    }

    @FXML
    void handleEditSponsor(ActionEvent event) {
        Sponsor selected = allSponsorTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showSponsorDialog(selected);
        } else {
            showAlert("Avertissement", "Veuillez sélectionner un sponsor.");
        }
    }

    @FXML
    void handleDeleteSponsor(ActionEvent event) {
        Sponsor selected = allSponsorTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (confirmDelete()) {
                try {
                    sponsorService.supprimer(selected.getId());
                    loadAllData();
                } catch (SQLException e) {
                    showAlert("Erreur", "Suppression impossible : " + e.getMessage());
                }
            }
        }
    }

    @FXML
    void handleAddRegistration(ActionEvent event) {
        showRegistrationDialog(null);
    }

    @FXML
    void handleEditRegistration(ActionEvent event) {
        Registration selected = allRegistrationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            showRegistrationDialog(selected);
        } else {
            showAlert("Avertissement", "Veuillez sélectionner une inscription.");
        }
    }

    @FXML
    void handleDeleteRegistration(ActionEvent event) {
        Registration selected = allRegistrationTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            if (confirmDelete()) {
                try {
                    registrationService.supprimer(selected.getId());
                    loadAllData();
                } catch (SQLException e) {
                    showAlert("Erreur", "Suppression impossible : " + e.getMessage());
                }
            }
        }
    }

    @FXML
    void handleBackToDashboard(ActionEvent event) {
        handleDashboard(event);
    }

    @FXML
    void processBackChat(ActionEvent event) {
        if (backChatInput == null || backChatArea == null) {
            return;
        }

        String input = backChatInput.getText();
        if (input == null || input.trim().isEmpty()) {
            return;
        }

        Event selectedEvent = eventTable != null ? eventTable.getSelectionModel().getSelectedItem() : null;
        backChatArea.appendText("Vous: " + input.trim() + "\n");
        backChatArea.appendText("Bot: " + eventChatBotService.buildResponse(input, currentUser, selectedEvent) + "\n");
        backChatInput.clear();
    }

    @FXML
    void clearBackChat(ActionEvent event) {
        if (backChatArea != null) {
            backChatArea.clear();
            initializeBackAssistant();
        }
    }

    // ─── SIDEBAR TOGGLE HANDLERS ────────────────────────────────────────────────

    @FXML public void toggleComptesMenu(ActionEvent event) { toggleMenu(comptesSubmenu); }
    @FXML public void toggleCoursMenu(ActionEvent event)   { toggleMenu(coursSubmenu); }
    @FXML public void toggleJeuxMenu(ActionEvent event)    { toggleMenu(jeuxSubmenu); }
    @FXML public void toggleForumMenu(ActionEvent event)   { toggleMenu(forumSubmenu); }
    @FXML public void toggleEventsMenu(ActionEvent event)  { toggleMenu(eventsSubmenu); }
    @FXML public void toggleMeetMenu(ActionEvent event)    { toggleMenu(meetSubmenu); }
    @FXML public void toggleMailingMenu(ActionEvent event) { toggleMenu(mailingSubmenu); }

    private void toggleMenu(VBox submenu) {
        if (submenu == null) return;
        boolean showing = submenu.isVisible();
        submenu.setVisible(!showing);
        submenu.setManaged(!showing);
    }

    // ─── NAVIGATION HANDLERS ─────────────────────────────────────────────────────

    @FXML
    public void handleDashboard(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_admin.fxml", event, AdminDashboardController.class,
                (ctrl) -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleListeComptes(ActionEvent event) {
        navigateTo("/tn/esprit/view/user_index.fxml", event, UserIndexController.class,
                (ctrl) -> ctrl.initAdmin(currentUser));
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_profile.fxml", event, AdminDashboardController.class, // Reusing logic
                (ctrl) -> { /* init if needed */ });
    }

    @FXML
    public void handleBackFront(ActionEvent event) {
        // Implementation depends on Front dashboard controller
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/front_login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur logout/navigation: " + e.getMessage());
        }
    }

    @FXML public void handleCategories(ActionEvent event) { navigateSimple("/tn/esprit/view/back_CoursCategorieList.fxml", event); }
    @FXML public void handleModules(ActionEvent event)    { navigateSimple("/tn/esprit/view/back_CoursModuleList.fxml", event); }
    @FXML public void handleCours(ActionEvent event)      { navigateSimple("/tn/esprit/view/back_CoursList.fxml", event); }
    @FXML public void handleBackList(ActionEvent event)   { navigateSimple("/tn/esprit/view/back_GameList.fxml", event); }
    
    @FXML public void handleEventsList(ActionEvent event) { selectTab(0); }
    @FXML public void handleEventsCalendrier(ActionEvent event) { selectTab(0); }
    @FXML public void handleEventsSponsors(ActionEvent event) { selectTab(2); }
    @FXML public void handleEventsInscriptions(ActionEvent event) { selectTab(1); }

    private <T> void navigateTo(String fxmlPath, ActionEvent event, Class<T> controllerClass, AdminDashboardController.ControllerInit<T> init) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            T controller = loader.getController();
            if (init != null) init.init(controller);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
        }
    }

    private void navigateSimple(String fxmlPath, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation simple: " + e.getMessage());
        }
    }

    // ─── DIALOGS & VALIDATION ───────────────────────────────────────────────────

    private void applyDialogStyle(Dialog<?> dialog) {
        DialogPane dialogPane = dialog.getDialogPane();
        String cssPath = getClass().getResource("/tn/esprit/view/back_Event.css").toExternalForm();
        dialogPane.getStylesheets().add(cssPath);
        dialogPane.getStyleClass().add("root");
        
        // Style buttons
        Button okButton = (Button) dialogPane.lookupButton(ButtonType.OK);
        if (okButton != null) okButton.getStyleClass().add("button");
        Button cancelButton = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        if (cancelButton != null) cancelButton.getStyleClass().add("button");
    }

    private void showEventDialog(Event event) {
        Dialog<Event> dialog = new Dialog<>();
        applyDialogStyle(dialog);
        dialog.setTitle(event == null ? "Ajouter un événement" : "Modifier l'événement");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titre = new TextField(event != null ? event.getTitre() : "");
        TextField lieu = new TextField(event != null ? event.getLieu() : "");
        lieu.setPromptText("Ex: Tunis, Ariana...");
        
        TextArea description = new TextArea(event != null ? event.getDescription() : "");
        description.setWrapText(true);
        description.setPrefRowCount(3);
        
        Button generateAiBtn = new Button("✨ Générer avec IA");
        generateAiBtn.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #15803d; -fx-font-weight: bold; -fx-border-color: #bbf7d0; -fx-border-radius: 4;");
        generateAiBtn.setOnAction(evt -> {
            if (titre.getText().isEmpty()) {
                showAlert("Info", "Veuillez d'abord saisir un titre pour orienter l'IA.");
                return;
            }
            generateAiBtn.setDisable(true);
            generateAiBtn.setText("⏳ Génération...");
            new Thread(() -> {
                String prompt = "Rédige une description attractive pour un événement nommé '" + titre.getText() + "'";
                if (!lieu.getText().isEmpty()) prompt += " se déroulant à " + lieu.getText();
                prompt += ". Le ton doit être professionnel et invitant. Max 3 phrases.";
                
                String aiResponse = contentGenerationService.generateContent(prompt);
                javafx.application.Platform.runLater(() -> {
                    if (aiResponse != null && !aiResponse.startsWith("Erreur")) {
                        description.setText(aiResponse);
                    }
                    generateAiBtn.setDisable(false);
                    generateAiBtn.setText("✨ Générer avec IA");
                });
            }).start();
        });

        TextField capacite = new TextField(event != null ? String.valueOf(event.getCapacite()) : "");
        TextField prix = new TextField(event != null ? event.getPrix().toString() : "");
        TextField image = new TextField(event != null ? event.getImage() : "");
        image.setPromptText("URL ou fichier image...");
        ImageView imagePreview = new ImageView();
        imagePreview.setFitWidth(220);
        imagePreview.setFitHeight(120);
        imagePreview.setPreserveRatio(true);
        imagePreview.setSmooth(true);
        imagePreview.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #dbe7f2; -fx-border-radius: 12;");

        Button uploadImageBtn = new Button("Choisir une image");
        uploadImageBtn.setStyle("-fx-background-color: #eef6ff; -fx-text-fill: #1d4ed8; -fx-font-weight: bold;");
        uploadImageBtn.setOnAction(evt -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Choisir une image d'evenement");
            chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.webp"));
            File selectedFile = chooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (selectedFile != null) {
                image.setText(selectedFile.toURI().toString());
                loadPreviewImage(image.getText(), imagePreview);
            }
        });

        image.textProperty().addListener((obs, oldValue, newValue) -> loadPreviewImage(newValue, imagePreview));
        loadPreviewImage(image.getText(), imagePreview);
        TextField categorie = new TextField(event != null ? event.getCategorie() : "");
        TextField latitude = new TextField(event != null && event.getLatitude() != null ? event.getLatitude().toString() : "");
        TextField longitude = new TextField(event != null && event.getLongitude() != null ? event.getLongitude().toString() : "");
        TextField organizer = new TextField(event != null ? event.getOrganizerEmail() : "");
        
        DatePicker dateDebut = new DatePicker(event != null ? event.getDateDebut().toLocalDateTime().toLocalDate() : null);
        DatePicker dateFin = new DatePicker(event != null ? event.getDateFin().toLocalDateTime().toLocalDate() : null);

        // Auto-fill latitude/longitude based on Lieu using Nominatim API
        lieu.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused && !lieu.getText().trim().isEmpty()) {
                new Thread(() -> {
                    try {
                        String query = java.net.URLEncoder.encode(lieu.getText().trim(), "UTF-8");
                        String urlString = "https://nominatim.openstreetmap.org/search?format=json&q=" + query;
                        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new URL(urlString).openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("User-Agent", "Naja7ni-App");
                        
                        java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) sb.append(line);
                        br.close();
                        
                        String jsonResponse = sb.toString();
                        if (jsonResponse.length() > 5) {
                            int latIdx = jsonResponse.indexOf("\"lat\":\"");
                            int lonIdx = jsonResponse.indexOf("\"lon\":\"");
                            if (latIdx != -1 && lonIdx != -1) {
                                String latVal = jsonResponse.substring(latIdx + 7, jsonResponse.indexOf("\"", latIdx + 7));
                                String lonVal = jsonResponse.substring(lonIdx + 7, jsonResponse.indexOf("\"", lonIdx + 7));
                                javafx.application.Platform.runLater(() -> {
                                    if (latitude.getText().isEmpty()) latitude.setText(latVal);
                                    if (longitude.getText().isEmpty()) longitude.setText(lonVal);
                                });
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Geocoding failed: " + ex.getMessage());
                    }
                }).start();
            }
        });

        grid.add(new Label("Titre (min 3 chars) :"), 0, 0); grid.add(titre, 1, 0);
        grid.add(new Label("Lieu:"), 0, 1);
        grid.add(lieu, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        VBox descBox = new VBox(5, description, generateAiBtn);
        grid.add(descBox, 1, 2);
        VBox imageBox = new VBox(8, image, uploadImageBtn, imagePreview);
        grid.addRow(3, new Label("Image :"), imageBox);
        grid.addRow(4, new Label("Catégorie :"), categorie);
        grid.addRow(5, new Label("Capacité :"), capacite);
        grid.addRow(6, new Label("Prix :"), prix);
        grid.addRow(7, new Label("Latitude (Requis) :"), latitude);
        grid.addRow(8, new Label("Longitude (Requis) :"), longitude);
        grid.addRow(9, new Label("Email Organisateur :"), organizer);
        grid.addRow(10, new Label("Date Début :"), dateDebut);
        grid.addRow(11, new Label("Date Fin :"), dateFin);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                // VALIDATION (Contrôle de saisie)
                if (titre.getText().length() < 3) { showAlert("Validation", "Le titre doit faire au moins 3 caractères."); return null; }
                if (lieu.getText().isEmpty()) { showAlert("Validation", "Le lieu est obligatoire."); return null; }
                if (dateDebut.getValue() == null || dateFin.getValue() == null) { showAlert("Validation", "Dates obligatoires."); return null; }
                if (dateFin.getValue().isBefore(dateDebut.getValue())) { showAlert("Validation", "La date de fin doit être après le début."); return null; }
                if (latitude.getText().isEmpty() || longitude.getText().isEmpty()) { showAlert("Validation", "Latitude et Longitude sont obligatoires."); return null; }

                try {
                    int cap = Integer.parseInt(capacite.getText());
                    BigDecimal pr = new BigDecimal(prix.getText());
                    BigDecimal lat = new BigDecimal(latitude.getText());
                    BigDecimal lon = new BigDecimal(longitude.getText());
                    
                    if (cap <= 0 || pr.compareTo(BigDecimal.ZERO) < 0) {
                        showAlert("Validation", "Capacité et Prix doivent être positifs.");
                        return null;
                    }

                    Event e = (event == null) ? new Event() : event;
                    e.setTitre(titre.getText());
                    e.setLieu(lieu.getText());
                    e.setImage(image.getText());
                    e.setCategorie(categorie.getText());
                    e.setCapacite(cap);
                    e.setPrix(pr);
                    e.setLatitude(lat);
                    e.setLongitude(lon);
                    e.setOrganizerEmail(organizer.getText());
                    e.setDescription(description.getText());
                    e.setDateDebut(Timestamp.valueOf(dateDebut.getValue().atStartOfDay()));
                    e.setDateFin(Timestamp.valueOf(dateFin.getValue().atTime(23, 59)));
                    e.setStatut("OUVERT");
                    
                    // Initialiser d'autres champs pour éviter les nulls en DB
                    if (event == null) {
                        e.setInscrits(0);
                        e.setDateCreation(new Timestamp(System.currentTimeMillis()));
                        e.setTimeZone("UTC");
                        e.setRecurring(false);
                    }
                    
                    return e;
                } catch (NumberFormatException ex) {
                    showAlert("Validation", "Format numérique invalide.");
                    return null;
                }
            }
            return null;
        });

        Optional<Event> result = dialog.showAndWait();
        result.ifPresent(e -> {
            try {
                if (event == null) eventService.ajouter(e);
                else eventService.modifier(e);
                loadAllData();
            } catch (SQLException ex) {
                showAlert("Erreur", "Enregistrement échoué : " + ex.getMessage());
            }
        });
    }

    private void showSponsorDialog(Sponsor sponsor) {
        Dialog<Sponsor> dialog = new Dialog<>();
        applyDialogStyle(dialog);
        dialog.setTitle(sponsor == null ? "Ajouter un Sponsor" : "Modifier le Sponsor");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nom = new TextField(sponsor != null ? sponsor.getNom() : "");
        TextArea desc = new TextArea(sponsor != null ? sponsor.getDescription() : "");
        desc.setPrefRowCount(3);
        TextField type = new TextField(sponsor != null ? sponsor.getType() : "");
        TextField logo = new TextField(sponsor != null ? sponsor.getLogo() : "");
        TextField website = new TextField(sponsor != null ? sponsor.getSiteWeb() : "");
        TextField contactPers = new TextField(sponsor != null ? sponsor.getContactPersonne() : "");
        TextField email = new TextField(sponsor != null ? sponsor.getContactEmail() : "");
        TextField phone = new TextField(sponsor != null ? sponsor.getContactTelephone() : "");
        TextField montant = new TextField(sponsor != null && sponsor.getMontant() != null ? sponsor.getMontant().toString() : "");
        
        ComboBox<String> statut = new ComboBox<>(FXCollections.observableArrayList("ACTIF", "INACTIF", "EN_ATTENTE"));
        statut.setValue(sponsor != null ? sponsor.getStatut() : "ACTIF");
        
        ComboBox<Event> eventCombo = new ComboBox<>(eventData);
        if (sponsor != null) {
            eventCombo.getSelectionModel().select(eventData.stream().filter(e -> e.getId() == sponsor.getEventId()).findFirst().orElse(null));
        }

        grid.addRow(0, new Label("Nom Sponsor :"), nom);
        grid.addRow(1, new Label("Description :"), desc);
        grid.addRow(2, new Label("Type :"), type);
        grid.addRow(3, new Label("Montant :"), montant);
        grid.addRow(4, new Label("Événement :"), eventCombo);
        grid.addRow(5, new Label("Logo URL :"), logo);
        grid.addRow(6, new Label("Site Web :"), website);
        grid.addRow(7, new Label("Personne Contact :"), contactPers);
        grid.addRow(8, new Label("Email Contact :"), email);
        grid.addRow(9, new Label("Téléphone :"), phone);
        grid.addRow(10, new Label("Statut :"), statut);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                // VALIDATION (Contrôle de saisie)
                if (nom.getText().isEmpty()) { showAlert("Champs requis", "Le nom est obligatoire."); return null; }
                if (!email.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) { showAlert("Format Invalide", "Veuillez entrer un email valide."); return null; }
                if (eventCombo.getValue() == null) { showAlert("Champs requis", "Veuillez lier un événement."); return null; }
                
                try {
                    BigDecimal mnt = new BigDecimal(montant.getText());
                    if (mnt.compareTo(BigDecimal.ZERO) < 0) throw new NumberFormatException();
                    
                    Sponsor s = (sponsor == null) ? new Sponsor() : sponsor;
                    s.setNom(nom.getText());
                    s.setDescription(desc.getText());
                    s.setType(type.getText());
                    s.setLogo(logo.getText());
                    s.setSiteWeb(website.getText());
                    s.setContactPersonne(contactPers.getText());
                    s.setContactEmail(email.getText());
                    s.setContactTelephone(phone.getText());
                    s.setMontant(mnt);
                    s.setEventId(eventCombo.getValue().getId());
                    s.setStatut(statut.getValue());
                    
                    if (sponsor == null) {
                        s.setDateCreation(new Timestamp(System.currentTimeMillis()));
                        s.setDateDebut(new Timestamp(System.currentTimeMillis()));
                        s.setDateFin(new Timestamp(System.currentTimeMillis() + 31536000000L)); // +1 an
                    }
                    
                    return s;
                } catch (NumberFormatException ex) {
                    showAlert("Format Invalide", "Le montant doit être un nombre positif.");
                    return null;
                }
            }
            return null;
        });

        Optional<Sponsor> result = dialog.showAndWait();
        result.ifPresent(s -> {
            try {
                if (sponsor == null) sponsorService.ajouter(s);
                else sponsorService.modifier(s);
                loadAllData();
            } catch (SQLException ex) {
                showAlert("Erreur", "Enregistrement échoué : " + ex.getMessage());
            }
        });
    }

    private void showRegistrationDialog(Registration reg) {
        Dialog<Registration> dialog = new Dialog<>();
        applyDialogStyle(dialog);
        dialog.setTitle(reg == null ? "Ajouter une Inscription" : "Modifier l'Inscription");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField(reg != null ? reg.getVisitorName() : "");
        TextField email = new TextField(reg != null ? reg.getVisitorEmail() : "");
        ComboBox<Event> eventCombo = new ComboBox<>(eventData);
        if (reg != null) {
            eventCombo.getSelectionModel().select(eventData.stream().filter(e -> e.getId() == reg.getEvenementId()).findFirst().orElse(null));
        }

        grid.addRow(0, new Label("Nom Visiteur :"), name);
        grid.addRow(1, new Label("Email Visiteur :"), email);
        grid.addRow(2, new Label("Événement :"), eventCombo);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                if (name.getText().isEmpty() || email.getText().isEmpty() || eventCombo.getValue() == null) return null;
                Registration r = (reg == null) ? new Registration() : reg;
                r.setEvenementId(eventCombo.getValue().getId());
                r.setVisitorName(name.getText());
                r.setVisitorEmail(email.getText());
                r.setStatut("CONFIRME");
                r.setPresence(false);
                r.setModePaiement("ESPECE");
                r.setMontantPaye(eventCombo.getValue().getPrix());
                r.setPaiementStatut("PAYE");
                return r;
            }
            return null;
        });

        Optional<Registration> result = dialog.showAndWait();
        result.ifPresent(r -> {
            try {
                if (reg == null) registrationService.ajouter(r);
                else registrationService.modifier(r);
                loadAllData();
            } catch (SQLException ex) {
                showAlert("Erreur", "Enregistrement échoué : " + ex.getMessage());
            }
        });
    }

    private boolean confirmDelete() {
        return new Alert(Alert.AlertType.CONFIRMATION, "Confirmer la suppression ?", ButtonType.YES, ButtonType.NO).showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }

    private void loadPreviewImage(String imageSource, ImageView imagePreview) {
        if (imagePreview == null) {
            return;
        }
        if (imageSource == null || imageSource.trim().isEmpty()) {
            imagePreview.setImage(null);
            return;
        }

        try {
            Image image = buildImage(imageSource.trim());
            imagePreview.setImage(image);
        } catch (Exception e) {
            imagePreview.setImage(null);
        }
    }

    private Image buildImage(String imageSource) {
        if (imageSource.startsWith("http://") || imageSource.startsWith("https://") || imageSource.startsWith("file:")) {
            return new Image(imageSource, true);
        }

        URL resource = getClass().getResource(imageSource.startsWith("/") ? imageSource : "/" + imageSource);
        if (resource != null) {
            return new Image(resource.toExternalForm(), true);
        }

        File file = new File(imageSource);
        if (file.exists()) {
            return new Image(file.toURI().toString(), true);
        }

        return new Image(imageSource, true);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
