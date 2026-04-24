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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import tn.esprit.controllers.front.AdminDashboardController;
import tn.esprit.entities.users.Users;
import tn.esprit.controllers.front.UserIndexController;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Registration;
import tn.esprit.entities.event.Sponsor;
import tn.esprit.services.event.EventService;
import tn.esprit.services.event.RegistrationService;
import tn.esprit.services.event.SponsorService;

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
    @FXML private TableColumn<Registration, Integer> colRegEvent;
    @FXML private TableColumn<Registration, String> colRegName;
    @FXML private TableColumn<Registration, String> colRegEmail;
    @FXML private TableColumn<Registration, Timestamp> colRegDate;
    @FXML private TableColumn<Registration, String> colRegStatus;
    @FXML private TextField searchReg;

    @FXML private TableView<Sponsor> allSponsorTable;
    @FXML private TableColumn<Sponsor, Integer> colSponId;
    @FXML private TableColumn<Sponsor, String> colSponNom;
    @FXML private TableColumn<Sponsor, String> colSponType;
    @FXML private TableColumn<Sponsor, String> colSponEmail;
    @FXML private TableColumn<Sponsor, Integer> colSponEvent;
    @FXML private TableColumn<Sponsor, BigDecimal> colSponMontant;
    @FXML private TextField searchSponsor;

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
    private final RegistrationService registrationService = new RegistrationService();

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
        colRegEvent.setCellValueFactory(new PropertyValueFactory<>("evenementId"));
        colRegName.setCellValueFactory(new PropertyValueFactory<>("visitorName"));
        colRegEmail.setCellValueFactory(new PropertyValueFactory<>("visitorEmail"));
        colRegDate.setCellValueFactory(new PropertyValueFactory<>("dateInscription"));
        colRegStatus.setCellValueFactory(new PropertyValueFactory<>("statut"));

        // All Sponsors
        colSponId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colSponNom.setCellValueFactory(new PropertyValueFactory<>("nom"));
        colSponType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colSponEmail.setCellValueFactory(new PropertyValueFactory<>("contactEmail"));
        colSponEvent.setCellValueFactory(new PropertyValueFactory<>("eventId"));
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
        } catch (SQLException e) {
            showAlert("Erreur", "Impossible de charger les données : " + e.getMessage());
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
            } else {
                subSponsorTable.getItems().clear();
                subRegistrationTable.getItems().clear();
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
        } catch (IOException e) { e.printStackTrace(); }
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
        TextArea description = new TextArea(event != null ? event.getDescription() : "");
        TextField lieu = new TextField(event != null ? event.getLieu() : "");
        TextField capacite = new TextField(event != null ? String.valueOf(event.getCapacite()) : "");
        TextField prix = new TextField(event != null ? event.getPrix().toString() : "");
        TextField image = new TextField(event != null ? event.getImage() : "");
        TextField categorie = new TextField(event != null ? event.getCategorie() : "");
        TextField latitude = new TextField(event != null && event.getLatitude() != null ? event.getLatitude().toString() : "");
        TextField longitude = new TextField(event != null && event.getLongitude() != null ? event.getLongitude().toString() : "");
        TextField organizer = new TextField(event != null ? event.getOrganizerEmail() : "");
        
        DatePicker dateDebut = new DatePicker(event != null ? event.getDateDebut().toLocalDateTime().toLocalDate() : null);
        DatePicker dateFin = new DatePicker(event != null ? event.getDateFin().toLocalDateTime().toLocalDate() : null);

        grid.addRow(0, new Label("Titre (min 3 chars) :"), titre);
        grid.addRow(1, new Label("Lieu :"), lieu);
        grid.addRow(2, new Label("Image (URL) :"), image);
        grid.addRow(3, new Label("Catégorie :"), categorie);
        grid.addRow(4, new Label("Capacité :"), capacite);
        grid.addRow(5, new Label("Prix :"), prix);
        grid.addRow(6, new Label("Latitude (Requis) :"), latitude);
        grid.addRow(7, new Label("Longitude (Requis) :"), longitude);
        grid.addRow(8, new Label("Email Organisateur :"), organizer);
        grid.addRow(9, new Label("Date Début :"), dateDebut);
        grid.addRow(10, new Label("Date Fin :"), dateFin);
        grid.addRow(11, new Label("Description :"), description);

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

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
