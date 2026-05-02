package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Registration;
import tn.esprit.entities.event.Sponsor;
import tn.esprit.entities.users.Users;
import tn.esprit.services.event.EventService;
import tn.esprit.services.event.RegistrationService;
import tn.esprit.services.event.SponsorService;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class EventFrontController implements Initializable {

    @FXML private FlowPane eventContainer;
    @FXML private FlowPane recommendedEventContainer;
    @FXML private VBox forYouBox;
    @FXML private TextField searchBar;
    @FXML private TextField visitorName;
    @FXML private TextField visitorEmail;
    @FXML private TextField nbrTickets;
    @FXML private TextField numTel;
    @FXML private Label feedbackLabel;
    @FXML private FlowPane sponsorContainer;
    @FXML private Pagination pagination;
    @FXML private VBox selectedEventPreview;
    
    @FXML private Button adminButton;
    @FXML private Button profileButton;

    private final EventService eventService = new EventService();
    private final RegistrationService registrationService = new RegistrationService();
    private final SponsorService sponsorService = new SponsorService();

    private List<Event> allEvents = new ArrayList<>();
    private Event selectedEvent;
    private Users currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadEvents();
        setupSearch();
        setupPagination();
    }

    private void setupPagination() {
        pagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        int itemsPerPage = 6;
        int fromIndex = pageIndex * itemsPerPage;
        int toIndex = Math.min(fromIndex + itemsPerPage, allEvents.size());
        
        eventContainer.getChildren().clear();
        if (fromIndex < allEvents.size()) {
            displayEvents(allEvents.subList(fromIndex, toIndex));
        }
        
        return new Label(); // Dummy return as we update eventContainer directly
    }

    public void initUser(Users user) {
        this.currentUser = user;
        if (user != null) {
            profileButton.setText(user.getFirstName());
            boolean isAdmin = "ADMIN".equals(user.getRole());
            adminButton.setVisible(isAdmin);
            adminButton.setManaged(isAdmin);
            
            // Auto-fill visitor info if user is logged in
            visitorName.setText(user.getFirstName() + " " + user.getLastName());
            visitorEmail.setText(user.getEmail());
        }
    }

    private void loadEvents() {
        try {
            allEvents = eventService.recuperer();
            int pageCount = (int) Math.ceil((double) allEvents.size() / 6);
            pagination.setPageCount(pageCount > 0 ? pageCount : 1);
            pagination.setCurrentPageIndex(0);
            createPage(0); // Manually trigger first page
        } catch (SQLException e) {
            showFeedback("Erreur lors de la récupération des événements : " + e.getMessage(), true);
        }
    }

    private void setupSearch() {
        searchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            filterEvents(newValue);
        });
    }

    private void filterEvents(String query) {
        List<Event> filtered = allEvents.stream()
                .filter(e -> e.getTitre().toLowerCase().contains(query.toLowerCase()) || 
                            e.getDescription().toLowerCase().contains(query.toLowerCase()) ||
                            e.getLieu().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
        
        // Temporarily override allEvents for the view without losing the main list
        int pageCount = (int) Math.ceil((double) filtered.size() / 6);
        pagination.setPageCount(pageCount > 0 ? pageCount : 1);
        pagination.setPageFactory(index -> {
            int itemsPerPage = 6;
            int from = index * itemsPerPage;
            int to = Math.min(from + itemsPerPage, filtered.size());
            eventContainer.getChildren().clear();
            if (from < filtered.size()) displayEvents(filtered.subList(from, to));
            return new Label();
        });
    }

    private void displayEvents(List<Event> events) {
        eventContainer.getChildren().clear();
        for (Event event : events) {
            VBox card = createEventCard(event);
            eventContainer.getChildren().add(card);
        }
    }

    private VBox createEventCard(Event event) {
        VBox card = new VBox(12);
        card.setPrefSize(250, 360); // Minimized size
        card.setStyle("-fx-background-color: white; -fx-background-radius: 18; " +
                      "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-padding: 0;");
        card.setAlignment(Pos.TOP_LEFT);

        // Header Image Area (Smaller)
        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(140);
        imageArea.setStyle("-fx-background-color: linear-gradient(to bottom right, #f1f5f9, #e2e8f0); -fx-background-radius: 18 18 0 0;");
        
        Label dateBadge = new Label(event.getDateDebut().toLocalDateTime().getDayOfMonth() + "\n" + 
                                   event.getDateDebut().toLocalDateTime().getMonth().name().substring(0, 3));
        dateBadge.setAlignment(Pos.CENTER);
        dateBadge.setStyle("-fx-background-color: white; -fx-text-fill: #0FB5A9; -fx-font-weight: 900; -fx-padding: 6; -fx-background-radius: 10; -fx-font-size: 11px;");
        StackPane.setAlignment(dateBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(dateBadge, new Insets(12));

        Label imgEmoji = new Label("📅");
        imgEmoji.setFont(Font.font(40));
        imageArea.getChildren().addAll(imgEmoji, dateBadge);

        // Content Area
        VBox content = new VBox(8);
        content.setPadding(new Insets(12, 15, 15, 15));

        Label title = new Label(event.getTitre());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
        title.setWrapText(true);
        title.setPrefHeight(40);
        title.setTextFill(Color.web("#1e293b"));

        HBox meta = new HBox(8);
        Label loc = new Label("📍 " + event.getLieu());
        loc.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");
        meta.getChildren().add(loc);

        int remaining = event.getCapacite() - event.getInscrits();
        ProgressBar pb = new ProgressBar((double) event.getInscrits() / event.getCapacite());
        pb.setPrefWidth(220);
        pb.setProgress(Math.min(1.0, (double) event.getInscrits() / event.getCapacite()));
        pb.setStyle("-fx-accent: #0FB5A9; -fx-control-inner-background: #f1f5f9; -fx-background-radius: 10;");

        HBox footer = new HBox(5);
        footer.setAlignment(Pos.CENTER_LEFT);
        
        Label price = new Label(event.getPrix() + " DT");
        price.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnDetails = new Button("Voir Détails");
        btnDetails.setStyle("-fx-background-color: #0FB5A9; -fx-text-fill: white; -fx-font-weight: 700; -fx-background-radius: 10; -fx-padding: 6 12; -fx-cursor: hand;");
        btnDetails.setOnAction(e -> showEventDetails(event));

        footer.getChildren().addAll(price, spacer, btnDetails);

        content.getChildren().addAll(title, meta, pb, footer);
        card.getChildren().addAll(imageArea, content);

        // Hover animation
        card.setOnMouseEntered(e -> {
            card.setStyle(card.getStyle() + "-fx-border-color: #0FB5A9; -fx-border-width: 1; -fx-border-radius: 18;");
            card.setTranslateY(-3);
        });
        card.setOnMouseExited(e -> {
            card.setStyle("-fx-background-color: white; -fx-background-radius: 18; " +
                          "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5); -fx-padding: 0;");
            card.setTranslateY(0);
        });

        return card;
    }

    private void showEventDetails(Event event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Détails de l'événement");
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().add(ButtonType.CLOSE);
        dialogPane.setStyle("-fx-background-color: white; -fx-padding: 0;");
        
        VBox root = new VBox(20);
        root.setPrefWidth(500);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: white;");

        // Header with Gradient
        VBox header = new VBox(10);
        header.setStyle("-fx-background-color: linear-gradient(to right, #0FB5A9, #04B6D5); -fx-padding: 20; -fx-background-radius: 15;");
        
        Label title = new Label(event.getTitre());
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: 800; -fx-text-fill: white;");
        
        Label cat = new Label("🏷️ " + event.getCategorie());
        cat.setStyle("-fx-text-fill: white; -fx-font-weight: 600; -fx-opacity: 0.9;");
        
        header.getChildren().addAll(title, cat);

        // Details Body
        GridPane grid = new GridPane();
        grid.setHgap(30);
        grid.setVgap(15);
        
        Label descLabel = new Label("📝 Description");
        descLabel.setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b;");
        Label descText = new Label(event.getDescription());
        descText.setWrapText(true);
        descText.setStyle("-fx-text-fill: #64748b;");

        Label dateLabel = new Label("📅 Date & Heure");
        dateLabel.setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b;");
        Label dateText = new Label("Du " + event.getDateDebut() + "\nau " + event.getDateFin());
        dateText.setStyle("-fx-text-fill: #64748b;");

        Label locLabel = new Label("📍 Lieu");
        locLabel.setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b;");
        Label locText = new Label(event.getLieu());
        locText.setStyle("-fx-text-fill: #64748b;");

        Label priceLabel = new Label("💰 Participation");
        priceLabel.setStyle("-fx-font-weight: 800; -fx-text-fill: #1e293b;");
        Label priceText = new Label(event.getPrix() + " DT");
        priceText.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #0FB5A9;");

        grid.add(descLabel, 0, 0); grid.add(descText, 1, 0);
        grid.add(dateLabel, 0, 1); grid.add(dateText, 1, 1);
        grid.add(locLabel, 0, 2);  grid.add(locText, 1, 2);
        grid.add(priceLabel, 0, 3); grid.add(priceText, 1, 3);

        Separator sep = new Separator();
        
        Button btnReserver = new Button("S'inscrire Maintenant");
        btnReserver.setMaxWidth(Double.MAX_VALUE);
        btnReserver.setStyle("-fx-background-color: #0FB5A9; -fx-text-fill: white; -fx-font-weight: 800; -fx-font-size: 16px; -fx-background-radius: 12; -fx-padding: 12; -fx-cursor: hand;");
        btnReserver.setOnAction(e -> {
            dialog.close();
            selectEvent(event);
        });

        root.getChildren().addAll(header, grid, sep, btnReserver);
        dialogPane.setContent(root);
        
        dialog.showAndWait();
    }

    private void selectEvent(Event event) {
        this.selectedEvent = event;
        updateSelectedEventUI();
        loadSponsors(event.getId());
    }

    private void updateSelectedEventUI() {
        selectedEventPreview.getChildren().clear();
        selectedEventPreview.setStyle("-fx-background-color: #f0f9ff; -fx-padding: 15; -fx-background-radius: 12; -fx-border-color: #0FB5A9; -fx-border-radius: 12; -fx-border-width: 2;");
        
        Label title = new Label(selectedEvent.getTitre());
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        
        Label price = new Label("💰 " + selectedEvent.getPrix() + " DT");
        price.setStyle("-fx-font-weight: 700; -fx-text-fill: #0FB5A9;");
        
        Label location = new Label("📍 " + selectedEvent.getLieu());
        location.setStyle("-fx-text-fill: #64748b;");
        
        selectedEventPreview.getChildren().addAll(title, price, location);
    }

    private void loadSponsors(int eventId) {
        sponsorContainer.getChildren().clear();
        try {
            List<Sponsor> sponsors = sponsorService.findByEventId(eventId);
            if (sponsors.isEmpty()) {
                sponsorContainer.getChildren().add(new Label("Aucun sponsor."));
            } else {
                for (Sponsor s : sponsors) {
                    VBox sCard = new VBox(5);
                    sCard.setAlignment(Pos.CENTER);
                    sCard.setStyle("-fx-background-color: #F7FFFE; -fx-background-radius: 8; -fx-padding: 8; -fx-border-color: rgba(15,181,169,0.2); -fx-border-radius: 8;");
                    
                    Label name = new Label(s.getNom());
                    name.setFont(Font.font("System", FontWeight.BOLD, 11));
                    name.setTextFill(Color.web("#0FB5A9"));
                    
                    sCard.getChildren().addAll(name);
                    sponsorContainer.getChildren().add(sCard);
                }
            }
        } catch (SQLException e) {
            showFeedback("Erreur sponsors : " + e.getMessage(), true);
        }
    }

    @FXML
    private void reserver() {
        if (selectedEvent == null) {
            showFeedback("❌ Veuillez sélectionner un événement dans la liste.", true);
            return;
        }

        if (!validateInput()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Confirmer votre réservation ?");
        confirm.setContentText("Événement : " + selectedEvent.getTitre() + "\nTickets : " + nbrTickets.getText());

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        try {
            int tickets = Integer.parseInt(nbrTickets.getText());
            Registration reg = new Registration();
            reg.setEvenementId(selectedEvent.getId());
            reg.setVisitorName(visitorName.getText());
            reg.setVisitorEmail(visitorEmail.getText());
            reg.setStatut("CONFIRME");
            reg.setPresence(false);
            reg.setModePaiement("ESPECE");
            reg.setMontantPaye(selectedEvent.getPrix().multiply(new BigDecimal(tickets)));
            reg.setPaiementStatut("NON_PAYE");
            reg.setNotes("Réservation via front-office. Tel: " + numTel.getText());

            registrationService.ajouter(reg);

            // Important: Update capacity counter
            selectedEvent.setInscrits(selectedEvent.getInscrits() + tickets);
            eventService.modifier(selectedEvent);

            showFeedback("✅ Réservation réussie ! Votre place est réservée.", false);
            clearForm();
            loadEvents(); // Refresh UI to update counters
        } catch (Exception e) {
            showFeedback("❌ Erreur : " + e.getMessage(), true);
        }
    }

    private boolean validateInput() {
        if (visitorName.getText().isEmpty() || visitorEmail.getText().isEmpty() || 
            nbrTickets.getText().isEmpty() || numTel.getText().isEmpty()) {
            showFeedback("⚠️ Tous les champs sont requis.", true);
            return false;
        }

        if (!visitorEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            showFeedback("⚠️ Format d'email invalide.", true);
            return false;
        }

        try {
            int n = Integer.parseInt(nbrTickets.getText());
            if (n <= 0) {
                showFeedback("⚠️ Le nombre de tickets doit être positif.", true);
                return false;
            }
            if (selectedEvent.getInscrits() + n > selectedEvent.getCapacite()) {
                showFeedback("⚠️ Capacité insuffisante ! Reste: " + (selectedEvent.getCapacite() - selectedEvent.getInscrits()), true);
                return false;
            }
        } catch (NumberFormatException e) {
            showFeedback("⚠️ Le nombre de tickets doit être un nombre.", true);
            return false;
        }

        if (!numTel.getText().matches("^\\d{8}$")) {
            showFeedback("⚠️ Le téléphone doit contenir 8 chiffres.", true);
            return false;
        }

        return true;
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(isError ? Color.web("#e53e3e") : Color.web("#38a169"));
    }

    private void clearForm() {
        if (currentUser == null) {
            visitorName.clear();
            visitorEmail.clear();
        }
        nbrTickets.clear();
        numTel.clear();
    }

    // ─── NAVIGATION HANDLERS ───────────────────────────────────────────────────

    @FXML
    private void handleAccueil(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_user_dashboard.fxml", event);
    }

    @FXML
    private void handleProfile(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_profile.fxml", event);
    }

    @FXML
    private void handleCours(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_CoursCategories.fxml", event);
    }

    @FXML
    private void handleGameList(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_GameList.fxml", event);
    }

    @FXML
    private void handleForums(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_forum.fxml", event);
    }

    @FXML
    private void handleBackOffice(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_admin.fxml"));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof AdminDashboardController) {
                ((AdminDashboardController) controller).initAdmin(currentUser);
            }
            showScene(root, event);
        } catch (IOException e) {
            System.err.println("Erreur navigation back-office: " + e.getMessage());
        }
    }

    @FXML
    private void voirTickets() {
        showAlert("Information", "La vue 'Mes Tickets' sera bientôt disponible !");
    }

    @FXML
    private void openWebPageCit() {
        showAlert("Calendrier", "Intégration Google Calendar en cours...");
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof FrontUserDashboardController) {
                ((FrontUserDashboardController) controller).initUser(currentUser);
            } else if (controller instanceof FrontCoursCategorieController) {
                ((FrontCoursCategorieController) controller).initUser(currentUser);
            } else if (controller instanceof FrontGameListController) {
                ((FrontGameListController) controller).initUser(currentUser);
            } else if (controller instanceof FrontProfileController) {
                ((FrontProfileController) controller).initUser(currentUser);
            } else if (controller instanceof FrontForumController) {
                ((FrontForumController) controller).initUser(currentUser);
            }

            showScene(root, event);
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
        }
    }

    private void showScene(Parent root, ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
