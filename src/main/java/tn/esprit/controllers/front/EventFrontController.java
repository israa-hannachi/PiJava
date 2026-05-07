package tn.esprit.controllers.front;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import javafx.util.Duration;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Registration;
import tn.esprit.entities.event.Sponsor;
import tn.esprit.entities.event.Rating;
import tn.esprit.entities.users.Users;
import tn.esprit.services.event.EventService;
import tn.esprit.services.event.EventChatBotService;
import tn.esprit.services.event.EventContentGenerationService;
import tn.esprit.services.event.HateSpeechChecker;
import tn.esprit.services.event.PaymentService;
import tn.esprit.services.event.RegistrationService;
import tn.esprit.services.event.SponsorService;
import tn.esprit.services.event.RatingService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.awt.Desktop;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

public class EventFrontController implements Initializable {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy 'a' HH:mm", Locale.FRANCE);
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.FRANCE);

    // ————————————————————————————————————————————————————————————————————————————————
    @FXML private FlowPane eventContainer;
    @FXML private FlowPane recommendedEventContainer;
    @FXML private VBox forYouBox;
    @FXML private TextField searchBar;

    // Registration form
    @FXML private TextField visitorName;
    @FXML private TextField visitorEmail;
    @FXML private TextField numTel;
    @FXML private Label ticketCountLabel;
    @FXML private Label feedbackLabel;
    @FXML private FlowPane sponsorContainer;
    @FXML private Pagination pagination;
    @FXML private VBox selectedEventPreview;

    // Price summary labels
    @FXML private Label summaryEventName;
    @FXML private Label summaryUnitPrice;
    @FXML private Label summaryQty;
    @FXML private Label summaryTotal;

    @FXML private WebView mapWebView;

    // Stepper circles
    @FXML private Label stepCircle1;
    @FXML private Label stepCircle2;
    @FXML private Label stepCircle3;

    // Nav buttons
    @FXML private Button adminButton;
    @FXML private Button profileButton;

    // ——————————————————————————————————————————————————————————————————————————————————————
    private final EventService eventService = new EventService();
    private final RegistrationService registrationService = new RegistrationService();
    private final SponsorService sponsorService = new SponsorService();
    private final RatingService ratingService = new RatingService();
    private final PaymentService paymentService = new PaymentService();

    private final EventContentGenerationService contentGenerationService = new EventContentGenerationService();
    private final HateSpeechChecker hateSpeechChecker = new HateSpeechChecker();
    private final EventChatBotService eventChatBotService =
            new EventChatBotService(eventService, sponsorService, registrationService);

    // ——————————————————————————————————————————————————————————————————————————————————————
    private List<Event> allEvents = new ArrayList<>();
    private List<Event> filteredEvents = new ArrayList<>();
    private Event selectedEvent;
    private Users currentUserObj;
    private int ticketCount = 1;

    private static final int ITEMS_PER_PAGE = 6;

    // ——————————————————————————————————————————————————————————————————————————————————————
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadEvents();
        setupSearch();
        updatePriceSummary();
        initializeMap();
    }

    public void initUser(Users user) {
        this.currentUserObj = user;
        if (user == null) return;

        profileButton.setText(user.getFirstName());

        // Auto-fill and lock name/email for logged-in users
        visitorName.setText(user.getFirstName() + " " + user.getLastName());
        visitorEmail.setText(user.getEmail());
        visitorName.setDisable(true);
        visitorEmail.setDisable(true);

        boolean isAdmin = "ADMIN".equals(user.getRole());
        adminButton.setVisible(isAdmin);
        adminButton.setManaged(isAdmin);

        // Show personalised recommendations section
        if (forYouBox != null) {
            forYouBox.setVisible(true);
            forYouBox.setManaged(true);
        }
        updateRecommendedEvents();
    }

    // ——————————————————————————————————————————————————————————————————————————————————————
    private void loadEvents() {
        try {
            allEvents = eventService.recuperer();
            filteredEvents = new ArrayList<>(allEvents);
            rebuildPagination(filteredEvents);
            updateRecommendedEvents();
        } catch (SQLException e) {
            showFeedback("Erreur lors du chargement des événements.", true);
        }
    }

    private void updateRecommendedEvents() {
        if (recommendedEventContainer == null) {
            return;
        }

        recommendedEventContainer.getChildren().clear();
        List<Event> recommendations = allEvents.stream()
                .sorted(Comparator.comparing(Event::getDateDebut))
                .limit(3)
                .toList();

        for (Event event : recommendations) {
            VBox card = createEventCard(event);
            card.setPrefWidth(300);
            recommendedEventContainer.getChildren().add(card);
        }

        if (forYouBox != null) {
            boolean hasRecommendations = !recommendations.isEmpty() && currentUserObj != null;
            forYouBox.setManaged(hasRecommendations);
            forYouBox.setVisible(hasRecommendations);
        }
    }

    private void rebuildPagination(List<Event> events) {
        int pageCount = (int) Math.ceil((double) events.size() / ITEMS_PER_PAGE);
        pagination.setPageCount(Math.max(pageCount, 1));
        pagination.setCurrentPageIndex(0);
        pagination.setPageFactory(pageIndex -> {
            int from = pageIndex * ITEMS_PER_PAGE;
            int to   = Math.min(from + ITEMS_PER_PAGE, events.size());
            eventContainer.getChildren().clear();
            if (from < events.size()) displayEvents(events.subList(from, to));
            return new Region(); // dummy — we drive eventContainer directly
        });
    }

    private void setupSearch() {
        searchBar.textProperty().addListener((obs, oldVal, newVal) -> {
            String q = newVal.trim().toLowerCase();
            filteredEvents = q.isEmpty() ? new ArrayList<>(allEvents)
                    : allEvents.stream()
                    .filter(e -> e.getTitre().toLowerCase().contains(q)
                            || e.getDescription().toLowerCase().contains(q)
                            || e.getLieu().toLowerCase().contains(q)
                            || e.getCategorie().toLowerCase().contains(q))
                    .toList();
            rebuildPagination(filteredEvents);
        });
    }

    // ——————————————————————————————————————————————————————————————————————————————————————
    private void displayEvents(List<Event> events) {
        eventContainer.getChildren().clear();
        for (Event event : events) {
            VBox card = createEventCard(event);
            eventContainer.getChildren().add(card);
            FadeTransition ft = new FadeTransition(Duration.millis(250), card);
            ft.setFromValue(0); ft.setToValue(1); ft.play();
        }
    }

    private VBox createEventCard(Event event) {
        boolean isSelected = selectedEvent != null && selectedEvent.getId() == event.getId();

        VBox card = new VBox(0);
        card.setPrefSize(270, 310);
        card.setStyle(getCardStyle(isSelected));
        card.setAlignment(Pos.TOP_LEFT);

        // ——— Header ———————————————————————————————————————————————————————————————————————
        StackPane header = new StackPane();
        header.setPrefHeight(120);
        header.setStyle("-fx-background-color: " + getCategoryGradient(event.getCategorie())
                + "; -fx-background-radius: 16 16 0 0;");

        ImageView eventImageView = createEventImageView(event.getImage());
        if (eventImageView != null) {
            eventImageView.setOpacity(0.92);
            header.getChildren().add(eventImageView);
        }

        Label catBadge = new Label(getCategoryEmoji(event.getCategorie()) + " " + event.getCategorie());
        catBadge.setStyle("-fx-background-color: rgba(255,255,255,0.92); -fx-text-fill: #334155;"
                + "-fx-font-size: 10px; -fx-font-weight: 700; -fx-padding: 3 8; -fx-background-radius: 20;");
        StackPane.setAlignment(catBadge, Pos.TOP_LEFT);
        StackPane.setMargin(catBadge, new Insets(10));

        int day = event.getDateDebut().toLocalDateTime().getDayOfMonth();
        String month = event.getDateDebut().toLocalDateTime().getMonth().name().substring(0, 3);
        Label dateBadge = new Label(day + "\n" + month);
        dateBadge.setAlignment(Pos.CENTER);
        dateBadge.setStyle("-fx-background-color: #0FB5A9; -fx-text-fill: white;"
                + "-fx-font-size: 11px; -fx-font-weight: 900; -fx-padding: 4 8;"
                + "-fx-background-radius: 8; -fx-text-alignment: center;");
        StackPane.setAlignment(dateBadge, Pos.TOP_RIGHT);
        StackPane.setMargin(dateBadge, new Insets(10));

        Label icon = new Label(getCategoryEmoji(event.getCategorie()));
        icon.setFont(Font.font(34));
        icon.setStyle(eventImageView != null
                ? "-fx-background-color: rgba(15,23,42,0.45); -fx-background-radius: 999; -fx-padding: 10;"
                : "");

        header.getChildren().addAll(icon, catBadge, dateBadge);

        // ——— Body —————————————————————————————————————————————————————————————————————————
        VBox body = new VBox(8);
        body.setPadding(new Insets(12, 14, 14, 14));

        Label title = new Label(event.getTitre());
        title.setFont(Font.font("Segoe UI", FontWeight.BOLD, 14));
        title.setTextFill(Color.web("#0f172a"));
        title.setWrapText(true);
        title.setMaxHeight(42);

        Label loc = new Label("📍 " + event.getLieu());
        loc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        // Sponsor badge
        Label sponsorBadge = new Label("");
        try {
            List<tn.esprit.entities.event.Sponsor> listSpons = sponsorService.findByEventId(event.getId());
            if (!listSpons.isEmpty()) {
                sponsorBadge.setText("🤝 " + listSpons.size() + " Partenaire(s) Officiel(s)");
                sponsorBadge.setStyle("-fx-text-fill: #0FB5A9; -fx-font-size: 11px; -fx-font-weight: bold;");
            }
        } catch (SQLException ignored) {}

        // Capacity bar
        double fill = event.getCapacite() > 0
                ? Math.min(1.0, (double) event.getInscrits() / event.getCapacite()) : 0;
        int remaining = event.getCapacite() - event.getInscrits();

        HBox barInfo = new HBox();
        barInfo.setAlignment(Pos.CENTER_LEFT);
        Label barLeft = new Label("Inscriptions");
        barLeft.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 10px;");
        Region barSpacer = new Region(); HBox.setHgrow(barSpacer, Priority.ALWAYS);
        Label barRight = new Label(event.getInscrits() + " / " + event.getCapacite());
        barRight.setStyle("-fx-text-fill: " + (remaining <= 5 ? "#e53e3e" : "#64748b")
                + "; -fx-font-size: 10px; -fx-font-weight: 700;");
        barInfo.getChildren().addAll(barLeft, barSpacer, barRight);

        ProgressBar pb = new ProgressBar(fill);
        pb.setPrefWidth(Double.MAX_VALUE);
        pb.setMaxWidth(Double.MAX_VALUE);
        pb.setPrefHeight(5);
        String barColor = fill >= 0.9 ? "#e53e3e" : fill >= 0.7 ? "#ed8936" : "#0FB5A9";
        pb.setStyle("-fx-accent: " + barColor
                + "; -fx-background-radius: 5; -fx-control-inner-background: #f1f5f9;");

        // Footer
        HBox footer = new HBox(8);
        footer.setAlignment(Pos.CENTER_LEFT);
        String priceStr = event.getPrix().compareTo(BigDecimal.ZERO) == 0
                ? "Gratuit" : event.getPrix() + " DT";
        Label price = new Label(priceStr);
        price.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        Region fSpacer = new Region(); HBox.setHgrow(fSpacer, Priority.ALWAYS);

        Button btnDetails = new Button(isSelected ? "✓ Sélectionné" : "Voir Détails");
        btnDetails.setStyle(isSelected
                ? "-fx-background-color: #0FB5A9; -fx-text-fill: white; -fx-font-weight: 700;"
                  + "-fx-background-radius: 8; -fx-padding: 5 12; -fx-cursor: hand;"
                : "-fx-background-color: transparent; -fx-border-color: #0FB5A9; -fx-border-radius: 8;"
                  + "-fx-text-fill: #0FB5A9; -fx-font-weight: 700; -fx-background-radius: 8;"
                  + "-fx-padding: 5 12; -fx-cursor: hand;");
        btnDetails.setOnAction(e -> showEventDetailsModern(event));

        footer.getChildren().addAll(price, fSpacer, btnDetails);
        body.getChildren().addAll(title, loc, sponsorBadge, barInfo, pb, footer);
        card.getChildren().addAll(header, body);

        // ——— Hover ———————————————————————————————————————————————————————————————————————
        card.setOnMouseEntered(e -> {
            if (!isSelected)
                card.setStyle(getCardStyle(false)
                        + "-fx-border-color: #0FB5A9; -fx-border-width: 1.5; -fx-border-radius: 16;");
            card.setTranslateY(-3);
        });
        card.setOnMouseExited(e -> {
            card.setStyle(getCardStyle(isSelected));
            card.setTranslateY(0);
        });

        return card;
    }

    private String getCardStyle(boolean selected) {
        String base = "-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 0;";
        if (selected)
            return base + "-fx-border-color: #0FB5A9; -fx-border-width: 2; -fx-border-radius: 16;"
                    + "-fx-effect: dropshadow(three-pass-box, rgba(15,181,169,0.25), 12, 0, 0, 4);";
        return base + "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.06), 10, 0, 0, 4);";
    }

    private String getCategoryGradient(String categorie) {
        if (categorie == null) return "linear-gradient(to bottom right, #f1f5f9, #e2e8f0)";
        return switch (categorie.toLowerCase()) {
            case "technologie", "tech", "ia" -> "linear-gradient(135deg, #e0f2fe, #f0fdfb)";
            case "design"                    -> "linear-gradient(135deg, #fef9f0, #fdf4ff)";
            case "business"                  -> "linear-gradient(135deg, #f0fdf4, #ecfdf5)";
            case "dev", "cloud"              -> "linear-gradient(135deg, #fff7ed, #fef3c7)";
            case "ml", "data"                -> "linear-gradient(135deg, #fdf2f8, #fce7f3)";
            default                          -> "linear-gradient(135deg, #f1f5f9, #e2e8f0)";
        };
    }

    private String getCategoryEmoji(String categorie) {
        if (categorie == null) return "📅";
        return switch (categorie.toLowerCase()) {
            case "technologie", "tech" -> "💻";
            case "ia"                  -> "🤖";
            case "design"              -> "🎨";
            case "business"            -> "💼";
            case "dev", "cloud"        -> "⚡";
            case "ml", "data"          -> "🧠";
            case "green"               -> "🌱";
            default                    -> "📅";
        };
    }

    private void showEventDetailsModern(Event event) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Details - " + event.getTitre());

        DialogPane pane = dialog.getDialogPane();
        pane.getButtonTypes().add(ButtonType.CLOSE);
        pane.setStyle("-fx-background-color: white; -fx-padding: 0;");
        Node closeBtn = pane.lookupButton(ButtonType.CLOSE);
        if (closeBtn != null) {
            closeBtn.setStyle("-fx-background-color: white; -fx-border-color: #dbe7f2; -fx-border-radius: 10;"
                    + "-fx-background-radius: 10; -fx-padding: 8 18;");
        }

        HBox root = new HBox(18);
        root.setPrefWidth(1020);
        root.setPadding(new Insets(18));
        root.setStyle("-fx-background-color: linear-gradient(to bottom, #f9fcff, white);");

        VBox leftColumn = new VBox(16);
        leftColumn.setPrefWidth(640);
        leftColumn.setStyle("-fx-background-color: white; -fx-background-radius: 22; -fx-padding: 0 0 18 0;"
                + "-fx-border-color: #dff0ef; -fx-border-radius: 22;");

        VBox headerBox = new VBox(10);
        headerBox.setStyle("-fx-background-color: linear-gradient(to right, #f5fffd, #eef6ff);"
                + "-fx-padding: 22 24 20 24; -fx-background-radius: 22 22 0 0;");

        Label catLbl = new Label((event.getCategorie() == null ? "Event" : event.getCategorie()).toUpperCase(Locale.ROOT));
        catLbl.setStyle("-fx-background-color: #dff8f4; -fx-text-fill: #0fb5a9; -fx-font-size: 10px;"
                + "-fx-font-weight: 800; -fx-padding: 5 10; -fx-background-radius: 999;");

        Label titleLbl = new Label(event.getTitre());
        titleLbl.setStyle("-fx-font-size: 29px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");
        titleLbl.setWrapText(true);

        Label aiBadge = new Label("INTELLIGENCE IA");
        aiBadge.setStyle("-fx-background-color: #4f46e5; -fx-text-fill: white; -fx-font-size: 10px;"
                + "-fx-font-weight: 800; -fx-padding: 6 10; -fx-background-radius: 999;");

        Region titleSpacer = new Region();
        HBox.setHgrow(titleSpacer, Priority.ALWAYS);
        HBox titleRow = new HBox(12, titleLbl, titleSpacer, aiBadge);

        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(18);
        metaGrid.setVgap(14);
        metaGrid.add(createMetricCard("Date debut", formatDateTime(event.getDateDebut().toLocalDateTime())), 0, 0);
        metaGrid.add(createMetricCard("Lieu", safeValue(event.getLieu())), 1, 0);
        metaGrid.add(createMetricCard("Prix", formatPrice(event.getPrix())), 2, 0);
        metaGrid.add(createMetricCard("Places", (event.getCapacite() - event.getInscrits()) + " libres"), 3, 0);
        headerBox.getChildren().addAll(catLbl, titleRow, metaGrid);

        VBox aboutBox = new VBox(10);
        aboutBox.setPadding(new Insets(0, 24, 0, 24));
        Label aboutTitle = new Label("A propos de cet evenement");
        aboutTitle.setStyle("-fx-font-size: 19px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label aboutText = new Label(safeValue(event.getDescription()));
        aboutText.setWrapText(true);
        aboutText.setStyle("-fx-font-size: 13px; -fx-text-fill: #475569; -fx-line-spacing: 3;");
        aboutBox.getChildren().addAll(aboutTitle, aboutText);

        HBox avisHeader = new HBox(14);
        avisHeader.setAlignment(Pos.CENTER_LEFT);
        avisHeader.setPadding(new Insets(0, 24, 0, 24));

        double average = 0.0;
        int ratingCount = 0;
        try {
            List<Rating> ratings = ratingService.getRatingsByEvent(event.getId());
            average = ratings.stream().mapToInt(Rating::getStars).average().orElse(0.0);
            ratingCount = ratings.size();
        } catch (SQLException e) {
            System.err.println("Erreur chargement avis: " + e.getMessage());
        }

        VBox ratingSummary = new VBox(4);
        Label avisTitle = new Label("Avis et notes");
        avisTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        Label avgLabel = new Label(String.format(Locale.US, "%.1f", average));
        avgLabel.setStyle("-fx-font-size: 34px; -fx-font-weight: 900; -fx-text-fill: #1e3a8a;");
        Label countLabel = new Label(ratingCount + " avis");
        countLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        ratingSummary.getChildren().addAll(avisTitle, avgLabel, countLabel);

        VBox shareBox = new VBox(4);
        Label shareTitle = new Label("Partagez votre experience");
        shareTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #334155;");
        Label shareSub = new Label("Aidez les autres a choisir leurs evenements.");
        shareSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        Button writeReviewBtn = new Button("Ecrire un avis");
        writeReviewBtn.setStyle("-fx-background-color: white; -fx-border-color: #d6e2ef; -fx-border-radius: 10;"
                + "-fx-background-radius: 10; -fx-text-fill: #334155; -fx-font-weight: 800;"
                + "-fx-padding: 8 12; -fx-cursor: hand;");
        writeReviewBtn.setOnAction(e -> {
            if (openEnhancedRatingDialog(event)) {
                dialog.close();
                showEventDetailsModern(event);
            }
        });
        shareBox.getChildren().addAll(shareTitle, shareSub, writeReviewBtn);

        Region avisSpacer = new Region();
        HBox.setHgrow(avisSpacer, Priority.ALWAYS);
        avisHeader.getChildren().addAll(ratingSummary, avisSpacer, shareBox);

        VBox avisList = new VBox(10);
        avisList.setPadding(new Insets(0, 24, 0, 24));
        try {
            List<Rating> ratings = ratingService.getRatingsByEvent(event.getId());
            if (ratings.isEmpty()) {
                Label noAvis = new Label("Soyez le premier a donner votre avis.");
                noAvis.setStyle("-fx-text-fill: #94a3b8; -fx-font-style: italic;");
                avisList.getChildren().add(noAvis);
            } else {
                for (Rating r : ratings) {
                    VBox singleAvis = new VBox(6);
                    singleAvis.setStyle("-fx-background-color: #f8fbff; -fx-padding: 12 14; -fx-background-radius: 12;"
                            + "-fx-border-color: #e2e8f0; -fx-border-radius: 12;");
                    Label stars = new Label("Note: " + "★".repeat(Math.max(0, r.getStars())));
                    stars.setStyle("-fx-text-fill: #f59e0b; -fx-font-size: 13px; -fx-font-weight: 800;");
                    Label comment = new Label(hasText(r.getComment()) ? r.getComment() : "Sans commentaire");
                    comment.setStyle("-fx-text-fill: #475569; -fx-font-size: 12px;");
                    comment.setWrapText(true);
                    singleAvis.getChildren().addAll(stars, comment);
                    avisList.getChildren().add(singleAvis);
                }
            }
        } catch (SQLException e) {
            Label errorLabel = new Label("Impossible de charger les avis pour le moment.");
            errorLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            avisList.getChildren().add(errorLabel);
        }

        leftColumn.getChildren().addAll(headerBox, aboutBox, createSponsorsSection(event.getId()), avisHeader, avisList);

        VBox rightColumn = new VBox(16);
        rightColumn.setPrefWidth(300);

        VBox aiCard = new VBox(12);
        aiCard.setStyle("-fx-background-color: linear-gradient(to bottom right, #122347, #1f315d);"
                + "-fx-background-radius: 22; -fx-padding: 18;");
        Label aiTitle = new Label("Analyse IA");
        aiTitle.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: 900;");
        Label aiBody = new Label(buildAiInsight(event));
        aiBody.setWrapText(true);
        aiBody.setStyle("-fx-text-fill: rgba(255,255,255,0.82); -fx-font-size: 12px; -fx-line-spacing: 3;");
        Button aiChatBtn = new Button("Assistant event");
        aiChatBtn.setMaxWidth(Double.MAX_VALUE);
        aiChatBtn.setStyle("-fx-background-color: rgba(255,255,255,0.12); -fx-text-fill: white;"
                + "-fx-font-weight: 800; -fx-background-radius: 12; -fx-padding: 10 12; -fx-cursor: hand;");
        aiChatBtn.setOnAction(e -> openEventChatBotDialog(event));
        aiCard.getChildren().addAll(aiTitle, aiBody, aiChatBtn);

        VBox participationCard = new VBox(14);
        participationCard.setStyle("-fx-background-color: white; -fx-background-radius: 22; -fx-padding: 18;"
                + "-fx-border-color: #dff0ef; -fx-border-radius: 22;");

        Label participationTitle = new Label("PARTICIPATION");
        participationTitle.setStyle("-fx-font-size: 24px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");

        GridPane sideGrid = new GridPane();
        sideGrid.setHgap(14);
        sideGrid.setVgap(10);
        addSummaryRow(sideGrid, 0, "Prix", formatPrice(event.getPrix()));
        addSummaryRow(sideGrid, 1, "Places disponibles", (event.getCapacite() - event.getInscrits()) + " / " + event.getCapacite());
        addSummaryRow(sideGrid, 2, "Periode", formatDateRange(event));

        Button btnReserver = new Button("Je m'inscris maintenant");
        btnReserver.setMaxWidth(Double.MAX_VALUE);
        btnReserver.setStyle("-fx-background-color: linear-gradient(to right, #12c4b5, #0fb5a9);"
                + "-fx-text-fill: white; -fx-font-weight: 900; -fx-font-size: 14px;"
                + "-fx-background-radius: 14; -fx-padding: 13; -fx-cursor: hand;");
        btnReserver.setOnAction(e -> {
            dialog.close();
            selectEvent(event);
        });

        Label secureLabel = new Label("🔒 Paiement sécurisé via Stripe");
        secureLabel.setStyle("-fx-background-color: #f0fdf4; -fx-text-fill: #16a34a; -fx-font-size: 11px;"
                + "-fx-font-weight: 700; -fx-padding: 9 12; -fx-background-radius: 12;"
                + "-fx-border-color: #bbf7d0; -fx-border-radius: 12;");

        participationCard.getChildren().addAll(participationTitle, sideGrid, btnReserver, secureLabel);
        rightColumn.getChildren().addAll(aiCard, participationCard);

        root.getChildren().addAll(leftColumn, rightColumn);
        pane.setContent(root);
        dialog.showAndWait();
    }

    private void addDetailRow(GridPane grid, int row, String label, String value, boolean wrap) {
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight: 700; -fx-text-fill: #334155; -fx-font-size: 12px;");
        Label val = new Label(value);
        val.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        val.setWrapText(wrap);
        grid.add(lbl, 0, row);
        grid.add(val, 1, row);
    }

    private VBox createMetricCard(String label, String value) {
        VBox box = new VBox(4);
        box.setPrefWidth(130);
        box.setStyle("-fx-background-color: white; -fx-padding: 12; -fx-background-radius: 14;"
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 14;");

        Label title = new Label(label);
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: 800; -fx-text-fill: #94a3b8;");

        Label content = new Label(value);
        content.setWrapText(true);
        content.setStyle("-fx-font-size: 13px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        box.getChildren().addAll(title, content);
        return box;
    }

    private String formatDateTime(java.time.LocalDateTime dateTime) {
        return dateTime == null ? "-" : DATE_TIME_FORMATTER.format(dateTime);
    }

    private String formatDateRange(Event event) {
        if (event == null || event.getDateDebut() == null || event.getDateFin() == null) {
            return "-";
        }
        return DATE_FORMATTER.format(event.getDateDebut().toLocalDateTime())
                + " -> " + DATE_FORMATTER.format(event.getDateFin().toLocalDateTime());
    }

    private String formatPrice(BigDecimal price) {
        if (price == null || price.compareTo(BigDecimal.ZERO) == 0) {
            return "Gratuit";
        }
        return price.stripTrailingZeros().toPlainString() + " DT";
    }

    private ImageView createEventImageView(String imageSource) {
        if (!hasText(imageSource)) {
            return null;
        }

        try {
            Image image = buildEventImage(imageSource.trim());
            if (image == null || image.isError()) {
                return null;
            }

            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(270);
            imageView.setFitHeight(120);
            imageView.setPreserveRatio(false);
            imageView.setSmooth(true);
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(270, 120);
            clip.setArcWidth(16);
            clip.setArcHeight(16);
            imageView.setClip(clip);
            return imageView;
        } catch (Exception e) {
            return null;
        }
    }

    private Image buildEventImage(String imageSource) {
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

    private String safeValue(String value) {
        return hasText(value) ? value : "Non renseigne";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String buildAiInsight(Event event) {
        List<String> insights = new ArrayList<>();

        if (event.getPrix() == null || event.getPrix().compareTo(BigDecimal.ZERO) == 0) {
            insights.add("Cet evenement est gratuit, donc l'inscription reste simple et accessible.");
        } else {
            insights.add("Le prix affiche une formule payante, ideale pour un format plus encadre.");
        }

        int remaining = Math.max(0, event.getCapacite() - event.getInscrits());
        if (remaining <= 5) {
            insights.add("Il reste peu de places disponibles, ce qui signale une forte demande.");
        } else {
            insights.add("La capacite laisse encore de la place pour s'inscrire sereinement.");
        }

        if (hasText(event.getLieu())) {
            insights.add("La localisation est clairement definie: " + event.getLieu() + ".");
        }

        return String.join(" ", insights);
    }

    private VBox createSponsorsSection(int eventId) {
        VBox sponsorsBox = new VBox(10);
        sponsorsBox.setPadding(new Insets(0, 24, 0, 24));

        Label title = new Label("Sponsors");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: 900; -fx-text-fill: #1e293b;");
        FlowPane sponsorsFlow = new FlowPane();
        sponsorsFlow.setHgap(8);
        sponsorsFlow.setVgap(8);

        try {
            List<Sponsor> sponsors = sponsorService.findByEventId(eventId);
            if (sponsors.isEmpty()) {
                Label empty = new Label("Aucun sponsor pour cet evenement.");
                empty.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px;");
                sponsorsFlow.getChildren().add(empty);
            } else {
                for (Sponsor sponsor : sponsors) {
                    VBox sponsorCard = new VBox(4);
                    sponsorCard.setPrefWidth(220);
                    sponsorCard.setStyle("-fx-background-color: #f8fbff; -fx-padding: 12 14; -fx-background-radius: 14;"
                            + "-fx-border-color: #dce9f5; -fx-border-radius: 14;");

                    Label sponsorName = new Label(sponsor.getNom());
                    sponsorName.setStyle("-fx-text-fill: #0f172a; -fx-font-size: 13px; -fx-font-weight: 900;");

                    Label sponsorType = new Label("Type: " + safeValue(sponsor.getType()));
                    sponsorType.setStyle("-fx-text-fill: #0fb5a9; -fx-font-size: 11px; -fx-font-weight: 800;");

                    Label sponsorDescription = new Label(safeValue(sponsor.getDescription()));
                    sponsorDescription.setWrapText(true);
                    sponsorDescription.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11px;");

                    sponsorCard.getChildren().addAll(sponsorName, sponsorType, sponsorDescription);

                    if (hasText(sponsor.getSiteWeb())) {
                        Label sponsorSite = new Label(sponsor.getSiteWeb());
                        sponsorSite.setWrapText(true);
                        sponsorSite.setStyle("-fx-text-fill: #2563eb; -fx-font-size: 11px;");
                        sponsorCard.getChildren().add(sponsorSite);
                    }

                    sponsorsFlow.getChildren().add(sponsorCard);
                }
            }
        } catch (SQLException e) {
            Label error = new Label("Impossible de charger les sponsors.");
            error.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 12px;");
            sponsorsFlow.getChildren().add(error);
        }

        sponsorsBox.getChildren().addAll(title, sponsorsFlow);
        return sponsorsBox;
    }

    private String buildRegistrationNotes(String phone, int tickets) {
        return "Tel: " + phone + " | Tickets: " + tickets;
    }

    private int extractTicketQuantity(Registration registration, Event event) {
        if (registration != null && hasText(registration.getNotes())) {
            String notes = registration.getNotes();
            int index = notes.toLowerCase(Locale.ROOT).indexOf("tickets:");
            if (index >= 0) {
                String qty = notes.substring(index + 8).replaceAll("[^0-9]", "");
                if (hasText(qty)) {
                    try {
                        return Math.max(1, Integer.parseInt(qty));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        if (event != null && event.getPrix() != null && event.getPrix().compareTo(BigDecimal.ZERO) > 0
                && registration != null && registration.getMontantPaye() != null) {
            try {
                return Math.max(1, registration.getMontantPaye()
                        .divide(event.getPrix(), java.math.RoundingMode.HALF_UP)
                        .intValue());
            } catch (ArithmeticException ignored) {
            }
        }

        return 1;
    }

    private void updateRegistrationTicketQuantity(Registration registration, Event event, int remainingTickets) throws SQLException {
        if (remainingTickets <= 0) {
            registrationService.supprimer(registration.getId());
            return;
        }

        registration.setNotes(buildRegistrationNotes(extractPhoneFromNotes(registration.getNotes()), remainingTickets));

        if (event != null && event.getPrix() != null) {
            registration.setMontantPaye(event.getPrix().multiply(BigDecimal.valueOf(remainingTickets)));
        }

        registrationService.modifier(registration);
    }

    private String extractPhoneFromNotes(String notes) {
        if (!hasText(notes)) {
            return "";
        }

        int telIndex = notes.toLowerCase(Locale.ROOT).indexOf("tel:");
        if (telIndex < 0) {
            return "";
        }

        int endIndex = notes.indexOf("|", telIndex);
        String phone = endIndex >= 0 ? notes.substring(telIndex + 4, endIndex) : notes.substring(telIndex + 4);
        return phone.trim();
    }
    private void openTicketCancellationDialog(Registration registration, Event bookedEvent, Dialog<Void> parentDialog) {
        int reservedTickets = extractTicketQuantity(registration, bookedEvent);
        List<Integer> choices = java.util.stream.IntStream.rangeClosed(1, reservedTickets)
                .boxed()
                .toList();

        ChoiceDialog<Integer> quantityDialog = new ChoiceDialog<>(1, choices);
        quantityDialog.setTitle("Annuler des tickets");
        quantityDialog.setHeaderText("Combien de tickets voulez-vous annuler ?");
        quantityDialog.setContentText("Nombre de tickets :");

        quantityDialog.showAndWait().ifPresent(quantityToCancel -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                    "Confirmer l'annulation de " + quantityToCancel + " ticket(s) ?");
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }

            try {
                int remainingTickets = reservedTickets - quantityToCancel;
                updateRegistrationTicketQuantity(registration, bookedEvent, remainingTickets);

                bookedEvent.setInscrits(Math.max(0, bookedEvent.getInscrits() - quantityToCancel));
                eventService.modifier(bookedEvent);

                showAlert("Succes", quantityToCancel + " ticket(s) annule(s) avec succes.");
                parentDialog.close();
                loadEvents();
            } catch (SQLException ex) {
                showAlert("Erreur", "Erreur lors de l'annulation: " + ex.getMessage());
            }
        });
    }


    // ——————————————————————————————————————————————————————————————————————————————————————
    private void selectEvent(Event event) {
        this.selectedEvent = event;
        this.ticketCount = 1;
        if (ticketCountLabel != null) ticketCountLabel.setText("1");
        updateSelectedEventUI();
        updatePriceSummary();
        loadSponsors(event.getId());
        advanceStepper(2);
        // Re-trigger current page to refresh selected card styling
        int cur = pagination.getCurrentPageIndex();
        pagination.setCurrentPageIndex(cur == 0 ? 0 : cur - 1);
        pagination.setCurrentPageIndex(cur);
    }

    private void updateSelectedEventUI() {
        if (selectedEventPreview == null) return;
        selectedEventPreview.getChildren().clear();
        selectedEventPreview.setStyle("-fx-background-color: #f0fdfb; -fx-padding: 14;"
                + "-fx-background-radius: 12; -fx-border-color: #0FB5A9; -fx-border-radius: 12;"
                + "-fx-border-width: 1.5;");

        Label title = new Label(selectedEvent.getTitre());
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");
        title.setWrapText(true);

        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER_LEFT);
        chips.getChildren().addAll(
                makeChip("📍 " + selectedEvent.getLieu()),
                makeChip("📅 " + selectedEvent.getDateDebut().toLocalDateTime().toLocalDate()),
                makeChip("👥 " + (selectedEvent.getCapacite() - selectedEvent.getInscrits()) + " places")
        );
        selectedEventPreview.getChildren().addAll(title, chips);
    }

    private Label makeChip(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-background-color: white; -fx-text-fill: #475569; -fx-font-size: 11px;"
                + "-fx-padding: 3 8; -fx-background-radius: 20; -fx-border-color: #e2e8f0;"
                + "-fx-border-radius: 20;");
        return l;
    }

    // ——————————————————————————————————————————————————————————————————————————————————————
    private void initializeMap() {
        if (mapWebView == null) return;
        WebEngine engine = mapWebView.getEngine();

        StringBuilder markersParams = new StringBuilder();
        try {
            List<Event> allEvts = eventService.recuperer();
            for (Event e : allEvts) {
                if (e.getLatitude() != null && e.getLongitude() != null) {
                    markersParams.append("addMarker(")
                            .append(e.getLatitude()).append(",")
                            .append(e.getLongitude()).append(",'")
                            .append(e.getTitre().replace("'", "\\'")).append(" - ").append(e.getPrix()).append(" DT');\n");
                }
            }
        } catch (Exception ex) {}

        String mapHtml =
                "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "    <title>Leaflet Map</title>\n" +
                        "    <link rel=\"stylesheet\" href=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.css\" />\n" +
                        "    <script src=\"https://unpkg.com/leaflet@1.9.4/dist/leaflet.js\"></script>\n" +
                        "    <style>\n" +
                        "        #map { height: 330px; width: 100%; border-radius: 8px; }\n" +
                        "        body { margin: 0; padding: 0; }\n" +
                        "    </style>\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "    <div id=\"map\"></div>\n" +
                        "    <script>\n" +
                        "        var map = L.map('map').setView([36.8065, 10.1815], 6);\n" +
                        "        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {\n" +
                        "            maxZoom: 19,\n" +
                        "            attribution: '&copy; OpenStreetMap'\n" +
                        "        }).addTo(map);\n" +
                        "        function addMarker(lat, lng, title) {\n" +
                        "             L.marker([lat, lng]).addTo(map).bindPopup('<b>' + title + '</b>');\n" +
                        "        }\n" +
                        markersParams.toString() +
                        "    </script>\n" +
                        "</body>\n" +
                        "</html>";

        engine.loadContent(mapHtml);
    }

    private boolean openEnhancedRatingDialog(Event event) {
        Dialog<Rating> dialog = new Dialog<>();
        dialog.setTitle("Noter cet evenement");
        dialog.setHeaderText("Partagez votre avis sur : " + event.getTitre());

        ButtonType btnSubmit = new ButtonType("Publier mon avis", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSubmit, ButtonType.CANCEL);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        ComboBox<Integer> starsCombo = new ComboBox<>();
        starsCombo.getItems().addAll(1, 2, 3, 4, 5);
        starsCombo.setValue(5);
        starsCombo.setMaxWidth(Double.MAX_VALUE);

        TextArea commentArea = new TextArea();
        commentArea.setPromptText("Qu'avez-vous pense de l'evenement ?");
        commentArea.setWrapText(true);
        commentArea.setPrefRowCount(5);
        commentArea.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-radius: 12;");

        Label aiHint = new Label("Besoin d'aide ? L'IA peut generer une premiere version de votre description.");
        aiHint.setWrapText(true);
        aiHint.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        Button generateBtn = new Button("Generer une description avec IA");
        generateBtn.setStyle("-fx-background-color: #eef2ff; -fx-text-fill: #4f46e5; -fx-font-weight: 800;"
                + "-fx-background-radius: 10; -fx-padding: 9 12; -fx-cursor: hand;");

        Label generationStatus = new Label();
        generationStatus.setWrapText(true);
        generationStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");

        generateBtn.setOnAction(e -> {
            generateBtn.setDisable(true);
            generationStatus.setText("Generation IA en cours...");

            new Thread(() -> {
                String prompt = buildReviewPrompt(event, starsCombo.getValue(), commentArea.getText());
                String suggestion = contentGenerationService.generateContent(prompt);

                Platform.runLater(() -> {
                    generateBtn.setDisable(false);
                    if (hasText(suggestion) && !suggestion.toLowerCase(Locale.ROOT).startsWith("erreur")) {
                        commentArea.setText(suggestion.trim());
                        generationStatus.setText("Description generee. Vous pouvez encore la modifier avant publication.");
                    } else {
                        generationStatus.setText("Impossible de generer la description pour le moment.");
                    }
                });
            }).start();
        });

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(8));
        vbox.getChildren().addAll(
                new Label("Votre note"),
                starsCombo,
                new Label("Votre avis"),
                commentArea,
                aiHint,
                generateBtn,
                generationStatus
        );
        dialog.getDialogPane().setContent(vbox);

        dialog.setResultConverter(b -> {
            if (b == btnSubmit) {
                return new Rating(event.getId(), starsCombo.getValue(), commentArea.getText());
            }
            return null;
        });

        Optional<Rating> result = dialog.showAndWait();
        if (result.isEmpty()) {
            return false;
        }

        Rating rating = result.get();
        if (hateSpeechChecker.containsBadWord(rating.getComment())) {
            showAlert(Alert.AlertType.WARNING, "Erreur", "Votre avis contient des mots inappropries.");
            return false;
        }

        try {
            ratingService.ajouter(rating);
            showAlert("Merci !", "Votre avis a bien ete enregistre.");
            loadEvents();
            return true;
        } catch (Exception e) {
            showAlert("Erreur", "Impossible de sauvegarder l'avis.");
            return false;
        }
    }

    private String buildReviewPrompt(Event event, int stars, String currentDraft) {
        String tone = stars >= 4 ? "positif et enthousiaste" : (stars == 3 ? "equilibre et constructif" : "critique mais poli");
        String currentText = hasText(currentDraft) ? currentDraft.trim() : "aucun brouillon";

        return "Redige un avis court en francais pour un evenement. "
                + "Ton: " + tone + ". "
                + "Event: " + event.getTitre() + ". "
                + "Categorie: " + safeValue(event.getCategorie()) + ". "
                + "Lieu: " + safeValue(event.getLieu()) + ". "
                + "Description: " + safeValue(event.getDescription()) + ". "
                + "Note utilisateur: " + stars + "/5. "
                + "Brouillon actuel: " + currentText + ". "
                + "Retourne uniquement un paragraphe naturel de 2 ou 3 phrases maximum.";
    }

    private void updatePriceSummary() {
        // Defensive null check in case FXML injection fails (avoids crash)
        if (summaryUnitPrice == null || summaryEventName == null || summaryQty == null || summaryTotal == null) {
            return;
        }
        if (selectedEvent == null) {
            summaryEventName.setText("—");
            summaryUnitPrice.setText("—");
            summaryQty.setText("—");
            summaryTotal.setText("—");
            return;
        }
        BigDecimal unit  = selectedEvent.getPrix();
        BigDecimal total = unit.multiply(new BigDecimal(ticketCount));
        summaryEventName.setText(selectedEvent.getTitre());
        summaryUnitPrice.setText(unit + " DT");
        summaryQty.setText("× " + ticketCount);
        summaryTotal.setText(total + " DT");
    }

    // ——————————————————————————————————————————————————————————————————————————————————————
    @FXML
    private void incrementTickets() {
        if (selectedEvent == null) return;
        int available = selectedEvent.getCapacite() - selectedEvent.getInscrits();
        if (ticketCount < available) {
            ticketCount++;
            if (ticketCountLabel != null) ticketCountLabel.setText(String.valueOf(ticketCount));
            updatePriceSummary();
        }
    }

    @FXML
    private void decrementTickets() {
        if (ticketCount > 1) {
            ticketCount--;
            if (ticketCountLabel != null) ticketCountLabel.setText(String.valueOf(ticketCount));
            updatePriceSummary();
        }
    }

    // ——————————————————————————————————————————————————————————————————————————————————————
    private void loadSponsors(int eventId) {
        sponsorContainer.getChildren().clear();
        try {
            List<Sponsor> sponsors = sponsorService.findByEventId(eventId);
            if (sponsors.isEmpty()) {
                Label none = new Label("Aucun partenaire pour cet événement.");
                none.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 12px; -fx-font-style: italic;");
                sponsorContainer.getChildren().add(none);
            } else {
                for (Sponsor s : sponsors) {
                    Label tag = new Label(s.getNom());
                    tag.setStyle("-fx-background-color: #f0fdfb; -fx-text-fill: #0FB5A9;"
                            + "-fx-font-weight: 700; -fx-font-size: 11px; -fx-padding: 4 10;"
                            + "-fx-background-radius: 6; -fx-border-color: #b2f0e8; -fx-border-radius: 6;");
                    sponsorContainer.getChildren().add(tag);
                }
            }
        } catch (SQLException e) {
            showFeedback("Erreur sponsors : " + e.getMessage(), true);
        }
    }

    // ——————————————————————————————————————————————————————————————————————————————————————
    @FXML
    private void reserver() {
        clearFieldErrors();

        if (selectedEvent == null) {
            showFeedback("Veuillez d'abord selectionner un evenement dans la liste.", true);
            return;
        }
        if (!validateInput()) {
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la reservation");
        confirm.setHeaderText(null);
        DialogPane dp = confirm.getDialogPane();
        dp.setStyle("-fx-background-color: white;");

        VBox content = new VBox(14);
        content.setPadding(new Insets(4, 0, 8, 0));

        Label evtName = new Label(selectedEvent.getTitre());
        evtName.setStyle("-fx-font-size: 15px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

        GridPane summary = new GridPane();
        summary.setHgap(40);
        summary.setVgap(8);
        summary.setStyle("-fx-background-color: #f8fafc; -fx-padding: 14; -fx-background-radius: 10;");
        addSummaryRow(summary, 0, "Participant", visitorName.getText());
        addSummaryRow(summary, 1, "Email", visitorEmail.getText());
        addSummaryRow(summary, 2, "Telephone", numTel.getText());
        addSummaryRow(summary, 3, "Tickets", String.valueOf(ticketCount));
        addSummaryRow(summary, 4, "Prix unitaire", selectedEvent.getPrix() + " DT");

        BigDecimal total = selectedEvent.getPrix().multiply(new BigDecimal(ticketCount));
        Label totalLabel = new Label("Total a regler : " + total + " DT");
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: 800; -fx-text-fill: #0FB5A9;");

        content.getChildren().addAll(evtName, summary, new Separator(), totalLabel);
        dp.setContent(content);

        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            String paymentMode;
            String paymentStatus;
            String paymentNote;

            if (total.compareTo(BigDecimal.ZERO) <= 0) {
                paymentMode = "GRATUIT";
                paymentStatus = "PAYE";
                paymentNote = "Paiement: GRATUIT";
            } else {
                ChoiceDialog<String> paymentDialog = new ChoiceDialog<>("Espece (sur place)", 
                        List.of("Espece (sur place)", "Carte Stripe"));
                paymentDialog.setTitle("Mode de paiement");
                paymentDialog.setHeaderText("Choisissez comment vous voulez regler votre reservation");
                paymentDialog.setContentText("Mode :");

                Optional<String> selectedPayment = paymentDialog.showAndWait();
                if (selectedPayment.isEmpty()) {
                    return;
                }

                if ("Carte Stripe".equals(selectedPayment.get())) {
                    String stripePaymentLink = "https://buy.stripe.com/test_7sY5kwbkyaOBaqSdUlgIo00";
                    openStripePopup(stripePaymentLink);
                    
                    paymentMode   = "CARTE_STRIPE";
                    paymentStatus = "EN_ATTENTE";
                    paymentNote   = "Paiement Stripe - ouvert en popup";
                } else {
                    paymentMode = "ESPECE";
                    paymentStatus = "NON_PAYE";
                    paymentNote = "Paiement sur place";
                }
            }

            Registration reg = new Registration();
            reg.setEvenementId(selectedEvent.getId());
            reg.setVisitorName(visitorName.getText().trim());
            reg.setVisitorEmail(visitorEmail.getText().trim());
            reg.setStatut("CONFIRME");
            reg.setPresence(false);
            reg.setModePaiement(paymentMode);
            reg.setMontantPaye(total);
            reg.setPaiementStatut(paymentStatus);
            reg.setNotes(buildRegistrationNotes(numTel.getText().trim(), ticketCount)
                    + (hasText(paymentNote) ? " | " + paymentNote : ""));

            registrationService.ajouter(reg);

            selectedEvent.setInscrits(selectedEvent.getInscrits() + ticketCount);
            eventService.modifier(selectedEvent);

            advanceStepper(3);
            showFeedback("Reservation confirmee avec succes.", false);
            clearForm();
            loadEvents();
        } catch (Exception e) {
            showFeedback("Erreur lors de la reservation : " + e.getMessage(), true);
        }
    }

    private void addSummaryRow(GridPane grid, int row, String key, String value) {
        Label k = new Label(key);
        k.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        Label v = new Label(value);
        v.setStyle("-fx-font-weight: 700; -fx-font-size: 12px; -fx-text-fill: #0f172a;");
        grid.add(k, 0, row); grid.add(v, 1, row);
    }

    // ——— Validation ——————————————————————————————————————————————————————————————————————
    private boolean validateInput() {
        boolean ok = true;

        if (visitorName.getText().trim().isEmpty()) {
            markFieldError(visitorName); showFeedback("Le nom est requis.", true); ok = false;
        } else if (visitorEmail.getText().trim().isEmpty()) {
            markFieldError(visitorEmail); showFeedback("L'email est requis.", true); ok = false;
        } else if (!visitorEmail.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            markFieldError(visitorEmail); showFeedback("Format d'email invalide.", true); ok = false;
        } else if (numTel.getText().trim().isEmpty()) {
            markFieldError(numTel); showFeedback("Le numéro de téléphone est requis.", true); ok = false;
        } else if (!numTel.getText().matches("^\\d{8}$")) {
            markFieldError(numTel); showFeedback("Le téléphone doit contenir exactement 8 chiffres.", true); ok = false;
        } else if (ticketCount <= 0) {
            showFeedback("Le nombre de tickets doit être au moins 1.", true); ok = false;
        } else if (selectedEvent.getInscrits() + ticketCount > selectedEvent.getCapacite()) {
            int avail = selectedEvent.getCapacite() - selectedEvent.getInscrits();
            showFeedback("Capacité insuffisante. Places disponibles : " + avail, true); ok = false;
        }
        return ok;
    }

    private void markFieldError(TextField field) {
        field.setStyle("-fx-border-color: #e53e3e; -fx-border-width: 1.5; -fx-border-radius: 8;"
                + "-fx-background-color: #fff5f5; -fx-background-radius: 8; -fx-padding: 8 12;");
    }

    private void clearFieldErrors() {
        String base = "-fx-background-color: #f8fafc; -fx-background-radius: 8;"
                + "-fx-border-color: #e2e8f0; -fx-border-radius: 8; -fx-padding: 8 12;";
        for (TextField f : new TextField[]{visitorName, visitorEmail, numTel}) {
            if (!f.isDisabled()) f.setStyle(base);
        }
    }

    private void showFeedback(String message, boolean isError) {
        feedbackLabel.setText(message);
        feedbackLabel.setTextFill(isError ? Color.web("#e53e3e") : Color.web("#15803d"));
        feedbackLabel.setStyle(isError
                ? "-fx-background-color: #fff5f5; -fx-padding: 8 12; -fx-background-radius: 8;"
                  + "-fx-border-color: #fed7d7; -fx-border-radius: 8;"
                : "-fx-background-color: #f0fdf4; -fx-padding: 8 12; -fx-background-radius: 8;"
                  + "-fx-border-color: #bbf7d0; -fx-border-radius: 8;");
    }

    private void clearForm() {
        if (currentUserObj == null) {
            visitorName.clear();
            visitorEmail.clear();
        }
        numTel.clear();
        ticketCount = 1;
        if (ticketCountLabel != null) ticketCountLabel.setText("1");
        updatePriceSummary();
    }

    // ——— Stepper —————————————————————————————————————————————————————————————————————————
    private void advanceStepper(int activeStep) {
        if (stepCircle1 == null) return;
        String active   = "-fx-background-color: #0FB5A9; -fx-text-fill: white; -fx-font-weight: 800;"
                + "-fx-background-radius: 999; -fx-min-width: 24; -fx-min-height: 24;"
                + "-fx-max-width: 24; -fx-max-height: 24; -fx-alignment: CENTER;";
        String done     = "-fx-background-color: #ecfdf5; -fx-text-fill: #15803d; -fx-font-weight: 800;"
                + "-fx-background-radius: 999; -fx-min-width: 24; -fx-min-height: 24;"
                + "-fx-max-width: 24; -fx-max-height: 24; -fx-alignment: CENTER;";
        String inactive = "-fx-background-color: #f1f5f9; -fx-text-fill: #94a3b8; -fx-font-weight: 700;"
                + "-fx-background-radius: 999; -fx-min-width: 24; -fx-min-height: 24;"
                + "-fx-max-width: 24; -fx-max-height: 24; -fx-alignment: CENTER;";
        stepCircle1.setStyle(activeStep >= 1 ? (activeStep > 1 ? done : active) : inactive);
        stepCircle2.setStyle(activeStep >= 2 ? (activeStep > 2 ? done : active) : inactive);
        stepCircle3.setStyle(activeStep >= 3 ? active : inactive);
    }

    // ——— Navigation ——————————————————————————————————————————————————————————————————————
    @FXML public void handleAccueil(javafx.event.Event event) { navigateTo("/tn/esprit/view/front_user_dashboard.fxml", event); }
    @FXML public void handleProfile(javafx.event.Event event)  { navigateTo("/tn/esprit/view/front_profile.fxml", event); }
    @FXML public void handleCours(javafx.event.Event event)    { navigateTo("/tn/esprit/view/front_CoursCategories.fxml", event); }
    @FXML public void handleGameList(javafx.event.Event event) { navigateTo("/tn/esprit/view/front_GameList.fxml", event); }

    @FXML
    private void handleBackOffice(javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_admin.fxml"));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof AdminDashboardController)
                ((AdminDashboardController) ctrl).initAdmin(currentUserObj);
            showScene(root, event);
        } catch (IOException e) {
            System.err.println("Erreur back-office: " + e.getMessage());
        }
    }

    @FXML
    private void filterNearbyEvents() {
        TextInputDialog latDialog = new TextInputDialog("36.8065");
        latDialog.setTitle("Coordonnées GPS");
        latDialog.setHeaderText("Filtre de Proximité - Entrez votre Latitude (ex: Tunis=36.80)");
        latDialog.showAndWait().ifPresent(latStr -> {
            TextInputDialog lonDialog = new TextInputDialog("10.1815");
            lonDialog.setTitle("Coordonnées GPS");
            lonDialog.setHeaderText("Entrez votre Longitude (ex: Tunis=10.18)");
            lonDialog.showAndWait().ifPresent(lonStr -> {
                try {
                    double userLat = Double.parseDouble(latStr.replace(',', '.'));
                    double userLon = Double.parseDouble(lonStr.replace(',', '.'));

                    List<Event> nearby = eventService.getNearbyEvents(userLat, userLon, 50.0); // 50km radius
                    eventContainer.getChildren().clear();
                    if (nearby.isEmpty()) {
                        Label emptyLbl = new Label("Aucun événement à moins de 50km.");
                        emptyLbl.setStyle("-fx-font-size: 16px; -fx-text-fill: gray;");
                        eventContainer.getChildren().add(emptyLbl);
                    } else {
                        for (Event e : nearby) {
                            eventContainer.getChildren().add(createEventCard(e));
                        }
                    }
                } catch (Exception e) {
                    showAlert("Erreur", "Format des coordonnées invalide.");
                }
            });
        });
    }

    @FXML
    private void askAiAssistant() {
        try {
            List<Event> allEvts = eventService.recuperer();
            if(allEvts.isEmpty()) {
                showAlert("✨ IA Conseil", "Aucun événement disponible pour le moment.");
                return;
            }

            StringBuilder prompt = new StringBuilder("Tu es 'Naja7ni AI', un expert en recommandation d'événements. Voici les événements disponibles :\n");
            for(Event e : allEvts) {
                prompt.append("- [").append(e.getTitre()).append("] à ").append(e.getLieu()).append(" (").append(e.getPrix()).append(" DT).\n");
            }
            prompt.append("\nAnalyse ces événements et donne un conseil engageant invitant l'utilisateur à y participer. Ne fais pas de longue introduction.");

            Alert loading = new Alert(Alert.AlertType.INFORMATION);
            loading.setTitle("Patience...");
            loading.setHeaderText("L'IA analyse les opportunités pour vous...");
            loading.show();

            new Thread(() -> {
                tn.esprit.services.event.GeminiAiService ai = new tn.esprit.services.event.GeminiAiService();
                String response = ai.generateRecommendation(prompt.toString());

                javafx.application.Platform.runLater(() -> {
                    loading.close();
                    Alert a = new Alert(Alert.AlertType.INFORMATION);
                    a.setTitle("✨ Recommandation IA Naja7ni");
                    a.setHeaderText("L'Assistant Personnel vous conseille :");

                    TextArea area = new TextArea(response);
                    area.setWrapText(true);
                    area.setEditable(false);
                    area.setStyle("-fx-font-size: 14px; -fx-font-family: 'Segoe UI';");
                    a.getDialogPane().setContent(area);

                    a.showAndWait();
                });
            }).start();

        } catch (Exception e) {
            showAlert("Erreur", e.getMessage());
        }
    }

    @FXML
    private void openEventChatBot() {
        openEventChatBotDialog(selectedEvent);
    }

    @FXML private void voirTickets() {
        if (currentUserObj == null) {
            showAlert("Action requise", "Veuillez vous connecter pour gérer vos tickets.");
            return;
        }

        try {
            List<Registration> userRegs = registrationService.recuperer().stream()
                    .filter(r -> currentUserObj.getEmail() != null && currentUserObj.getEmail().equalsIgnoreCase(r.getVisitorEmail()))
                    .sorted(Comparator.comparing(Registration::getDateInscription).reversed())
                    .toList();

            if (userRegs.isEmpty()) {
                showAlert("Mes Tickets", "Vous n'avez aucune inscription existante.");
                return;
            }

            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Mes Tickets et Inscriptions");
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
            dialog.getDialogPane().setStyle("-fx-background-color: white;");

            VBox root = new VBox(15);
            root.setPadding(new Insets(20));
            root.setPrefWidth(550);

            Label title = new Label("Vos Inscriptions Actives");
            title.setStyle("-fx-font-size: 20px; -fx-font-weight: 800; -fx-text-fill: #0f172a;");

            Button cancelAllBtn = new Button("Tout annuler");
            cancelAllBtn.setStyle("-fx-background-color: #fff1f2; -fx-text-fill: #be123c; -fx-font-weight: bold;"
                    + "-fx-background-radius: 8; -fx-cursor: hand;");
            cancelAllBtn.setOnAction(e -> {
                Alert confirmAll = new Alert(Alert.AlertType.CONFIRMATION, "Voulez-vous annuler toutes vos inscriptions ?");
                if (confirmAll.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    try {
                        for (Registration registration : userRegs) {
                            Event event = eventService.findById(registration.getEvenementId());
                            if (event != null) {
                                int qty = extractTicketQuantity(registration, event);
                                registrationService.supprimer(registration.getId());
                                event.setInscrits(Math.max(0, event.getInscrits() - qty));
                                eventService.modifier(event);
                            }
                        }
                        dialog.close();
                        loadEvents();
                        showAlert("Succes", "Toutes les inscriptions ont ete annulees.");
                    } catch (Exception ex) {
                        showAlert("Erreur", "Impossible d'annuler tous les tickets.");
                    }
                }
            });

            HBox ticketsHeader = new HBox(12, title, new Region(), cancelAllBtn);
            HBox.setHgrow(ticketsHeader.getChildren().get(1), Priority.ALWAYS);
            root.getChildren().add(ticketsHeader);

            for (Registration reg : userRegs) {
                Event bookedEvent = eventService.findById(reg.getEvenementId());
                if (bookedEvent == null) continue;

                VBox card = new VBox(10);
                card.setStyle("-fx-background-color: #f8fafc; -fx-padding: 15; -fx-background-radius: 10; -fx-border-color: #e2e8f0; -fx-border-radius: 10;");

                Label evTitle = new Label("🎟️ " + bookedEvent.getTitre());
                evTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

                Label evDate = new Label("📅 Date : " + bookedEvent.getDateDebut());
                Label stStatus = new Label("💳 Statut : " + reg.getPaiementStatut());
                stStatus.setStyle("PAYE".equalsIgnoreCase(reg.getPaiementStatut()) ? "-fx-text-fill: #15803d; -fx-font-weight: bold;" : "-fx-text-fill: #b91c1c; -fx-font-weight: bold;");

                HBox actions = new HBox(10);
                actions.setAlignment(Pos.CENTER_RIGHT);

                Button btnDownload = new Button("📥 Télécharger Billet");
                btnDownload.setStyle("-fx-background-color: #0FB5A9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                btnDownload.setOnAction(e -> generatePdfTicket(reg, bookedEvent));

                Button btnCancel = new Button("✖ Annuler Inscription");
                btnCancel.setStyle("-fx-background-color: #fee2e2; -fx-text-fill: #b91c1c; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
                btnCancel.setOnAction(e -> openTicketCancellationDialog(reg, bookedEvent, dialog));

                actions.getChildren().addAll(btnCancel, btnDownload);
                card.getChildren().addAll(evTitle, evDate, new Label("Tickets : " + extractTicketQuantity(reg, bookedEvent)), stStatus, actions);
                root.getChildren().add(card);
            }

            ScrollPane scroll = new ScrollPane(root);
            scroll.setFitToWidth(true);
            scroll.setPrefHeight(400);
            scroll.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");

            dialog.getDialogPane().setContent(scroll);
            dialog.showAndWait();

        } catch (Exception e) {
            System.err.println("Erreur chargement tickets: " + e.getMessage());
        }
    }

    private void generatePdfTicket(Registration reg, Event bookedEvent) {
        try {
            // Define PDF file path
            String userHome = System.getProperty("user.home");
            File downloadsDir = new File(userHome, "Downloads");
            if (!downloadsDir.exists()) downloadsDir = new File(userHome);

            File pdfFile = new File(downloadsDir, "Ticket_Naja7ni_" + reg.getId() + ".pdf");

            // Generate PDF with compact ticket size
            Document document = new Document(new Rectangle(400, 600));
            document.setMargins(20, 20, 20, 20);
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));
            document.open();

            // Stylish Fonts
            com.itextpdf.text.Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, new BaseColor(15, 181, 169));
            com.itextpdf.text.Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.DARK_GRAY);
            com.itextpdf.text.Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.BLACK);
            com.itextpdf.text.Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 11, BaseColor.BLACK);

            // Elegant Header
            Paragraph header = new Paragraph("TICKET OFFICIEL NAJA7NI", titleFont);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setSpacingAfter(20);
            document.add(header);

            // Event Details Box
            PdfPTable table = new PdfPTable(1);
            table.setWidthPercentage(100);

            PdfPCell eventCell = new PdfPCell();
            eventCell.setBackgroundColor(new BaseColor(241, 245, 249)); // soft UI gray
            eventCell.setBorder(Rectangle.NO_BORDER);
            eventCell.setPadding(15);
            eventCell.addElement(new Paragraph("EVENT: " + bookedEvent.getTitre(), boldFont));
            eventCell.addElement(new Paragraph("Date: " + bookedEvent.getDateDebut() + " -> " + bookedEvent.getDateFin(), normalFont));
            eventCell.addElement(new Paragraph("Lieu: " + bookedEvent.getLieu(), normalFont));
            table.addCell(eventCell);
            document.add(table);

            document.add(new Paragraph("\n"));

            // Participant Info
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            PdfPCell c1 = new PdfPCell(new Phrase("Participant:", subtitleFont)); c1.setBorder(Rectangle.NO_BORDER);
            PdfPCell c2 = new PdfPCell(new Phrase(reg.getVisitorName(), boldFont)); c2.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(c1); infoTable.addCell(c2);

            c1 = new PdfPCell(new Phrase("Email:", subtitleFont)); c1.setBorder(Rectangle.NO_BORDER);
            c2 = new PdfPCell(new Phrase(reg.getVisitorEmail(), normalFont)); c2.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(c1); infoTable.addCell(c2);

            c1 = new PdfPCell(new Phrase("Montant Paye:", subtitleFont)); c1.setBorder(Rectangle.NO_BORDER);
            c2 = new PdfPCell(new Phrase(reg.getMontantPaye() + " DT", normalFont)); c2.setBorder(Rectangle.NO_BORDER);
            infoTable.addCell(c1); infoTable.addCell(c2);

            document.add(infoTable);

            // Barcode simulator (Aesthetic block)
            Paragraph sep = new Paragraph("\n------------------------------------------------------------\n\n", subtitleFont);
            sep.setAlignment(Element.ALIGN_CENTER);
            document.add(sep);

            Paragraph barcode = new Paragraph("|| ||| | || |||| | || | |||||", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 35, BaseColor.BLACK));
            barcode.setAlignment(Element.ALIGN_CENTER);
            document.add(barcode);

            Paragraph codeLabel = new Paragraph(String.format("REF-N7-%03d-%04d", bookedEvent.getId(), reg.getId()), boldFont);
            codeLabel.setAlignment(Element.ALIGN_CENTER);
            document.add(codeLabel);

            Paragraph footer = new Paragraph("\nCe billet est unique et strictement personnel.\nMerci de votre confiance !", subtitleFont);
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();

            // Open the PDF automatically
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
                Desktop.getDesktop().open(pdfFile);
            } else {
                showAlert("Succès", "Ticket téléchargé dans : " + pdfFile.getAbsolutePath());
            }

        } catch (Exception e) {
            System.err.println("Erreur gen PDF: " + e.getMessage());
            showAlert("Erreur", "Impossible de générer le ticket PDF : " + e.getMessage());
        }
    }

    @FXML private void openWebPageCit() { 
        try {
            URL calendarUrl = getClass().getResource("/tn/esprit/view/Calendar.fxml");
            if (calendarUrl == null) {
                showAlert("Erreur", "Le fichier Calendar.fxml est introuvable.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(calendarUrl);
            Parent root = loader.load();
            CalendarController ctrl = loader.getController();
            ctrl.initUser(currentUserObj);
            Stage stage = new Stage();
            stage.setTitle("Mon Calendrier");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur ouverture calendrier: " + e.getMessage());
            showAlert("Erreur", "Impossible d'ouvrir le calendrier: " + e.getMessage());
        }
    }

    private void navigateTo(String fxmlPath, javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object ctrl = loader.getController();
            if (ctrl instanceof FrontUserDashboardController)
                ((FrontUserDashboardController) ctrl).initUser(currentUserObj);
            else if (ctrl instanceof FrontCoursCategorieController)
                ((FrontCoursCategorieController) ctrl).initUser(currentUserObj);
            else if (ctrl instanceof FrontGameListController)
                ((FrontGameListController) ctrl).initUser(currentUserObj);
            else if (ctrl instanceof FrontProfileController)
                ((FrontProfileController) ctrl).initUser(currentUserObj);
            showScene(root, event);
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
        }
    }

    private void showScene(Parent root, javafx.event.Event event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void openEventChatBotDialog(Event contextEvent) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Assistant evenement");
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        VBox root = new VBox(12);
        root.setPadding(new Insets(18));
        root.setPrefWidth(520);

        Label title = new Label("Assistant intelligent des evenements");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: 900; -fx-text-fill: #0f172a;");

        Label subtitle = new Label("Posez des questions sur les events, les sponsors, les recommandations et les inscriptions.");
        subtitle.setWrapText(true);
        subtitle.setStyle("-fx-font-size: 12px; -fx-text-fill: #64748b;");

        TextArea chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPrefRowCount(14);
        chatArea.setStyle("-fx-control-inner-background: #f8fbff; -fx-background-radius: 16; -fx-border-radius: 16;");
        chatArea.setText("Bot: " + eventChatBotService.buildWelcomeMessage(currentUserObj, contextEvent) + "\n");

        TextField userInput = new TextField();
        userInput.setPromptText("Ex: recommande moi un event, montre les sponsors, mes inscriptions...");
        userInput.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-radius: 12;");

        Button sendBtn = new Button("Envoyer");
        sendBtn.setStyle("-fx-background-color: linear-gradient(to right, #12c4b5, #0fb5a9);"
                + "-fx-text-fill: white; -fx-font-weight: 800; -fx-background-radius: 12; -fx-cursor: hand;");

        Button hintBtn = new Button("Suggestions");
        hintBtn.setStyle("-fx-background-color: #eef6ff; -fx-text-fill: #1d4ed8;"
                + "-fx-font-weight: 800; -fx-background-radius: 12; -fx-cursor: hand;");
        hintBtn.setOnAction(e -> chatArea.appendText(
                "Bot: Essayez par exemple: recommande moi un event, montre les sponsors, mes inscriptions, event gratuit, prochain event.\n"
        ));

        Runnable sendMessage = () -> {
            String input = userInput.getText();
            if (!hasText(input)) {
                return;
            }
            chatArea.appendText("Vous: " + input.trim() + "\n");
            chatArea.appendText("Bot: " + eventChatBotService.buildResponse(input, currentUserObj, contextEvent) + "\n");
            userInput.clear();
        };

        sendBtn.setOnAction(e -> sendMessage.run());
        userInput.setOnAction(e -> sendMessage.run());

        HBox controls = new HBox(10, userInput, sendBtn, hintBtn);
        HBox.setHgrow(userInput, Priority.ALWAYS);
        root.getChildren().addAll(title, subtitle, chatArea, controls);
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showAlert(String title, String content) {
        showAlert(Alert.AlertType.INFORMATION, title, content);
    }

    private void openStripePopup(String url) {
        try {
            WebView webView = new WebView();
            webView.getEngine().load(url);

            VBox root = new VBox(webView);
            VBox.setVgrow(webView, Priority.ALWAYS);
            Scene scene = new Scene(root, 1000, 700);
            
            Stage stage = new Stage();
            stage.setTitle("Paiement Sécurisé Stripe");
            stage.getIcons().add(new Image("https://stripe.com/favicon.ico"));
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            showFeedback("Erreur lors de l'ouverture du popup de paiement: " + e.getMessage(), true);
        }
    }
}
