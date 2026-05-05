package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import tn.esprit.entities.cours.Cours;
import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.entities.cours.Cours_Module;
import tn.esprit.entities.users.Users;
import tn.esprit.services.cours.FavorisCours;
import tn.esprit.utils.NavigationHelper;
import tn.esprit.utils.StageUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FrontCoursParcoursInteractiveController implements Initializable {

    // FXML Components - Nouvelle structure
    @FXML private TextField searchField;
    @FXML private Button saveParcoursBtn;
    @FXML private Label xpLabel;
    @FXML private Label badgesLabel;
    @FXML private VBox modulesContainer;
    @FXML private ScrollPane modulesScrollPane;
    @FXML private VBox roadmapContainer;
    @FXML private ScrollPane roadmapScrollPane;
    
    // Avatar et profil
    @FXML private Circle userAvatar;
    @FXML private Label userInitial;
    
    // Anciens composants (conservés pour compatibilité)
    @FXML private Label moduleLabel;
    @FXML private Label moduleTitleLabel;
    @FXML private Label moduleInfoLabel;
    @FXML private Label progressLabel;
    @FXML private ProgressBar progressBar;
    @FXML private Button favBtn;
    @FXML private Button exportPdfBtn;
    @FXML private Button prevBtn;
    @FXML private Button nextBtn;
    @FXML private Button viewPdfBtn;
    @FXML private VBox currentCourseContainer;
    @FXML private TextArea contentTextArea;
    @FXML private HBox pdfSection;
    @FXML private Label currentCourseTitle;
    @FXML private Label currentCourseInfo;

    // State
    private Users currentUser;
    private Cours_Categorie currentCategorie;
    private Cours_Module currentModule;
    private List<Cours> coursList;
    private List<Cours> originalCoursList; // Pour reset
    private int currentIndex = 0;
    private int userId = -1;
    private final FavorisCours favoris = FavorisCours.getInstance();

    private final Map<Integer, String> courseStatus = new HashMap<>();
    private final List<Integer> moduleOrder = new ArrayList<>();
    private final Map<Integer, List<Integer>> courseOrderByModule = new HashMap<>();
    private final Map<Integer, Boolean> moduleExpanded = new HashMap<>();
    private int xp = 0;
    private static final int XP_PER_COURSE = 10;

    private final ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Initialisation de la nouvelle structure
        initializeNewStructure();
    }

    public void initData(Users user, Cours_Categorie cat, Cours_Module mod, List<Cours> list) {
        this.currentUser = user;
        this.currentCategorie = cat;
        this.currentModule = mod;
        this.coursList = new ArrayList<>(list);
        this.originalCoursList = new ArrayList<>(list);
        this.userId = user != null ? user.getId() : -1;

        loadParcoursState();
        initializeNewStructure();

        // Setup UI legacy (ancienne page) : uniquement si les composants existent encore dans le FXML
        boolean legacyLayout = (moduleLabel != null) || (moduleTitleLabel != null) || (currentCourseContainer != null);
        if (legacyLayout) {
            if (moduleLabel != null && moduleTitleLabel != null && moduleInfoLabel != null && currentModule != null && currentCategorie != null) {
                setupModuleInfo();
            }
            if (currentCourseContainer != null && contentTextArea != null && currentCourseTitle != null && currentCourseInfo != null) {
                buildInteractiveRoadmap();
                displayCurrentCourse();
            }
        }
    }

    private void setupModuleInfo() {
        moduleLabel.setText("Module: " + currentModule.getTitre() + " > " + currentCategorie.getNom());
        moduleTitleLabel.setText(currentModule.getTitre());
        moduleInfoLabel.setText("Level: " + currentModule.getNiveau() + " | Duration: " + currentModule.getDuree() + "h");
    }

    private void buildInteractiveRoadmap() {
        roadmapContainer.getChildren().clear();

        for (int i = 0; i < coursList.size(); i++) {
            final int index = i;
            Cours cours = coursList.get(i);

            // Create draggable course card
            VBox courseCard = createRoadmapCourseCard(cours, index);
            
            // Setup drag and drop
            setupDragAndDrop(courseCard, index);
            
            roadmapContainer.getChildren().add(courseCard);
        }
    }

    private VBox createRoadmapCourseCard(Cours cours, int index) {
        VBox card = new VBox(12);
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-padding:16; " +
                "-fx-border-color:#e5e7eb; -fx-border-radius:12; -fx-cursor:hand; " +
                "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),6,0,0,2);");

        // Header with order number
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label orderLabel = new Label(String.valueOf(index + 1));
        orderLabel.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; " +
                "-fx-background-radius:99; -fx-font-weight:700; -fx-font-size:12; -fx-padding:4 8;");
        orderLabel.setMinWidth(30);
        orderLabel.setAlignment(javafx.geometry.Pos.CENTER);

        Label titleLabel = new Label(cours.getTitre());
        titleLabel.setStyle("-fx-font-weight:700; -fx-text-fill:#1f2937; -fx-font-size:14;");
        titleLabel.setWrapText(true);

        header.getChildren().addAll(orderLabel, titleLabel);

        // Duration and status
        HBox info = new HBox(8);
        info.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        Label durationLabel = new Label("Duration: " + cours.getDuree() + " min");
        durationLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Status indicator
        boolean isCompleted = index < currentIndex;
        boolean isCurrent = index == currentIndex;
        
        Label statusLabel = new Label();
        if (isCompleted) {
            statusLabel.setText("Completed");
            statusLabel.setStyle("-fx-background-color:#dcfce7; -fx-text-fill:#166534; " +
                    "-fx-background-radius:6; -fx-padding:2 8; -fx-font-size:11; -fx-font-weight:700;");
        } else if (isCurrent) {
            statusLabel.setText("Current");
            statusLabel.setStyle("-fx-background-color:#fef3c7; -fx-text-fill:#92400e; " +
                    "-fx-background-radius:6; -fx-padding:2 8; -fx-font-size:11; -fx-font-weight:700;");
        } else {
            statusLabel.setText("Locked");
            statusLabel.setStyle("-fx-background-color:#f3f4f6; -fx-text-fill:#6b7280; " +
                    "-fx-background-radius:6; -fx-padding:2 8; -fx-font-size:11; -fx-font-weight:700;");
        }

        info.getChildren().addAll(durationLabel, spacer, statusLabel);

        // Description (truncated)
        if (cours.getDescription() != null && !cours.getDescription().isEmpty()) {
            String desc = cours.getDescription();
            if (desc.length() > 80) desc = desc.substring(0, 77) + "...";
            
            Label descLabel = new Label(desc);
            descLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:11; -fx-wrap-text:true;");
            card.getChildren().add(descLabel);
        }

        // Notes section
        VBox notesSection = new VBox(4);
        notesSection.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:8; -fx-padding:8; -fx-margin-top:8;");
        
        Label notesTitle = new Label("Notes:");
        notesTitle.setStyle("-fx-font-size:12; -fx-font-weight:700; -fx-text-fill:#374151;");
        
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Ajoutez vos notes ici...");
        notesArea.setStyle("-fx-background-color:white; -fx-border-color:#e5e7eb; -fx-border-radius:4; -fx-font-size:11; -fx-wrap-text:true;");
        notesArea.setPrefRowCount(3);
        notesArea.setMaxWidth(Double.MAX_VALUE);
        
        // Charger les notes existantes si disponibles
        String courseNotes = loadCourseNotes(cours.getId());
        notesArea.setText(courseNotes != null ? courseNotes : "");
        
        // Sauvegarder les notes lors du changement
        notesArea.textProperty().addListener((obs, oldVal, newVal) -> {
            saveCourseNotes(cours.getId(), newVal);
        });
        
        notesSection.getChildren().addAll(notesTitle, notesArea);
        
        // Export PDF button for each course
        Button exportPdfBtn = new Button("Export PDF");
        exportPdfBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-size:11; -fx-font-weight:700; -fx-background-radius:6; -fx-padding:6 12; -fx-cursor:hand;");
        exportPdfBtn.setOnAction(e -> exportCurrentCoursePdf(cours));
        
        card.getChildren().addAll(header, info, notesSection, exportPdfBtn);

        // Highlight current course
        if (isCurrent) {
            card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-padding:16; " +
                    "-fx-border-color:#0FB5A9; -fx-border-width:2; -fx-border-radius:12; " +
                    "-fx-cursor:hand; -fx-effect:dropshadow(three-pass-box,rgba(15,181,169,0.25),12,0,0,4);");
        }

        // Click to navigate
        card.setOnMouseClicked(e -> {
            if (index <= currentIndex) {
                currentIndex = index;
                displayCurrentCourse();
                buildInteractiveRoadmap(); // Refresh to update status
            }
        });

        return card;
    }

    private void setupDragAndDrop(VBox card, int index) {
        // Only allow dragging of completed courses or current course
        if (index > currentIndex) return;

        card.setOnDragDetected(event -> {
            try {
                if (event.getButton() == MouseButton.PRIMARY) {
                    Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(String.valueOf(index));
                    db.setContent(content);
                    event.consume();
                }
            } catch (Exception e) {
                System.err.println("Drag detected error: " + e.getMessage());
                event.consume();
            }
        });

        card.setOnDragOver(event -> {
            try {
                if (event.getGestureSource() != card && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            } catch (Exception e) {
                System.err.println("Drag over error: " + e.getMessage());
            }
            event.consume();
        });

        card.setOnDragDropped(event -> {
            try {
                Dragboard db = event.getDragboard();
                if (db.hasString()) {
                    int draggedIndex = Integer.parseInt(db.getString());
                    if (draggedIndex != index && draggedIndex <= currentIndex && index <= currentIndex) {
                        // Swap courses
                        Collections.swap(coursList, draggedIndex, index);
                        buildInteractiveRoadmap();
                        if (draggedIndex == currentIndex) {
                            currentIndex = index;
                        } else if (index == currentIndex) {
                            currentIndex = draggedIndex;
                        }
                        displayCurrentCourse();
                    }
                }
                event.setDropCompleted(true);
            } catch (Exception e) {
                System.err.println("Drag dropped error: " + e.getMessage());
                event.setDropCompleted(false);
            }
            event.consume();
        });
    }

    private void displayCurrentCourse() {
        if (currentIndex >= 0 && currentIndex < coursList.size()) {
            Cours cours = coursList.get(currentIndex);

            // Update course info
            currentCourseTitle.setText(cours.getTitre());
            currentCourseInfo.setText("Duration: " + cours.getDuree() + " min | Order: #" + cours.getOrdre());

            // Update content
            if (cours.getContenu() != null && !cours.getContenu().isEmpty()) {
                String cleanContent = cours.getContenu().replaceAll("<[^>]+>", " ");
                contentTextArea.setText(cleanContent);
            } else {
                contentTextArea.setText("No content available.");
            }
            
            boolean hasPdf = cours.getFichierContenu() != null && !cours.getFichierContenu().isEmpty();
            pdfSection.setVisible(hasPdf);
            pdfSection.setManaged(hasPdf);

            // Update favorite button
            boolean isFav = favoris.estFavori(userId, cours.getId());
            favBtn.setText(isFav ? "Remove from Favorites" : "Add to Favorites");
            favBtn.setStyle(isFav ? "-fx-background-color:#fef3c7; -fx-text-fill:#92400e;" : "-fx-background-color:white; -fx-text-fill:#0FB5A9;");

            // Update navigation buttons
            prevBtn.setDisable(currentIndex == 0);
            nextBtn.setDisable(currentIndex >= coursList.size() - 1);

            // Update progress
            double progress = coursList.isEmpty() ? 0 : (double)(currentIndex + 1) / coursList.size();
            progressBar.setProgress(progress);
            progressLabel.setText("Course " + (currentIndex + 1) + " / " + coursList.size());

            // Refresh roadmap to update current course highlighting
            buildInteractiveRoadmap();
        }
    }

    // Event Handlers
    @FXML public void handlePrevious() {
        if (currentIndex > 0) {
            currentIndex--;
            displayCurrentCourse();
        }
    }

    @FXML public void handleNext() {
        if (currentIndex < coursList.size() - 1) {
            currentIndex++;
            displayCurrentCourse();
        } else {
            showCompletionDialog();
        }
    }

    @FXML public void handleFavori() {
        if (currentIndex >= 0 && currentIndex < coursList.size()) {
            Cours cours = coursList.get(currentIndex);
            boolean nowFav = favoris.toggle(userId, cours);
            favBtn.setText(nowFav ? "Remove from Favorites" : "Add to Favorites");
            favBtn.setStyle(nowFav ? "-fx-background-color:#fef3c7; -fx-text-fill:#92400e;" : "-fx-background-color:white; -fx-text-fill:#0FB5A9;");
        }
    }

    @FXML public void handleExportPdf() {
        if (currentIndex >= 0 && currentIndex < coursList.size()) {
            Cours cours = coursList.get(currentIndex);
            exportCurrentCoursePdf(cours);
        }
    }

    private void exportCurrentCoursePdf(Cours cours) {
        try {
            String fileName = "cours_" + cours.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + "_" + 
                             System.currentTimeMillis() + ".pdf";
            java.io.File destFile = new java.io.File(System.getProperty("user.home"), fileName);
            
            exportSingleCoursePdf(destFile, cours);
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export réussi");
            success.setHeaderText("PDF du cours généré");
            success.setContentText("Le cours \"" + cours.getTitre() + "\" a été exporté vers : " + destFile.getAbsolutePath());
            success.showAndWait();
            
            try {
                if (destFile.exists()) {
                    java.awt.Desktop.getDesktop().open(destFile);
                }
            } catch (Exception e) {
                System.err.println("Impossible d'ouvrir le PDF : " + e.getMessage());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Erreur lors de l'export PDF du cours : " + e.getMessage()).showAndWait();
        }
    }

    private void exportSingleCoursePdf(java.io.File destFile, Cours cours) throws Exception {
        com.itextpdf.text.Document doc = new com.itextpdf.text.Document(com.itextpdf.text.PageSize.A4);
        com.itextpdf.text.pdf.PdfWriter writer = com.itextpdf.text.pdf.PdfWriter.getInstance(doc, new java.io.FileOutputStream(destFile));
        
        writer.setPageEvent(new SimpleHeaderFooterEvent());
        doc.open();
        
        com.itextpdf.text.Font titleFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 20, com.itextpdf.text.Font.BOLD, 
                new com.itextpdf.text.BaseColor(15, 181, 169));
        com.itextpdf.text.Paragraph title = new com.itextpdf.text.Paragraph(cours.getTitre(), titleFont);
        title.setAlignment(com.itextpdf.text.Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        doc.add(title);
        
        com.itextpdf.text.Font infoFont = new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.NORMAL);
        
        com.itextpdf.text.Paragraph info = new com.itextpdf.text.Paragraph();
        info.add(new com.itextpdf.text.Chunk("Module : ", new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        info.add(new com.itextpdf.text.Chunk(currentModule.getTitre() + "\n", infoFont));
        info.add(new com.itextpdf.text.Chunk("Durée : ", new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        info.add(new com.itextpdf.text.Chunk(cours.getDuree() + " minutes\n", infoFont));
        info.add(new com.itextpdf.text.Chunk("Ordre : ", new com.itextpdf.text.Font(
                com.itextpdf.text.Font.FontFamily.HELVETICA, 12, com.itextpdf.text.Font.BOLD)));
        info.add(new com.itextpdf.text.Chunk("#" + cours.getOrdre(), infoFont));
        info.setSpacingAfter(20);
        doc.add(info);
        
        if (cours.getDescription() != null && !cours.getDescription().isEmpty()) {
            com.itextpdf.text.Font descTitleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph descTitle = new com.itextpdf.text.Paragraph("Description", descTitleFont);
            descTitle.setSpacingAfter(10);
            doc.add(descTitle);
            
            com.itextpdf.text.Font descFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Paragraph desc = new com.itextpdf.text.Paragraph(cours.getDescription(), descFont);
            desc.setSpacingAfter(20);
            doc.add(desc);
        }
        
        if (cours.getContenu() != null && !cours.getContenu().isEmpty()) {
            com.itextpdf.text.Font contentTitleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph contentTitle = new com.itextpdf.text.Paragraph("Contenu du cours", contentTitleFont);
            contentTitle.setSpacingAfter(10);
            doc.add(contentTitle);
            
            String cleanContent = cours.getContenu().replaceAll("<[^>]+>", " ");
            com.itextpdf.text.Font contentFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL);
            com.itextpdf.text.Paragraph content = new com.itextpdf.text.Paragraph(cleanContent, contentFont);
            content.setSpacingAfter(20);
            doc.add(content);
        }
        
        if (cours.getFichierContenu() != null && !cours.getFichierContenu().isEmpty()) {
            com.itextpdf.text.Font pdfTitleFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 14, com.itextpdf.text.Font.BOLD);
            com.itextpdf.text.Paragraph pdfTitle = new com.itextpdf.text.Paragraph("Ressource PDF", pdfTitleFont);
            pdfTitle.setSpacingAfter(10);
            doc.add(pdfTitle);
            
            com.itextpdf.text.Font pdfFont = new com.itextpdf.text.Font(
                    com.itextpdf.text.Font.FontFamily.HELVETICA, 11, com.itextpdf.text.Font.NORMAL,
                    new com.itextpdf.text.BaseColor(37, 99, 235));
            String pdfInfo = cours.getFichierContenu().startsWith("http") ? 
                    "Disponible sur Cloudinary" : "Fichier local disponible";
            com.itextpdf.text.Paragraph pdf = new com.itextpdf.text.Paragraph(pdfInfo, pdfFont);
            pdf.setSpacingAfter(20);
            doc.add(pdf);
        }
        
        doc.close();
    }

    @FXML public void handleViewPdf() {
        if (currentIndex >= 0 && currentIndex < coursList.size()) {
            Cours cours = coursList.get(currentIndex);
            if (cours.getFichierContenu() != null && !cours.getFichierContenu().isEmpty()) {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(cours.getFichierContenu()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @FXML public void handleResetOrder() {
        coursList = new ArrayList<>(originalCoursList);
        currentIndex = 0;
        buildInteractiveRoadmap();
        displayCurrentCourse();
    }

    @FXML public void handleStartLearning() {
        currentIndex = 0;
        displayCurrentCourse();
        buildInteractiveRoadmap();
    }

    @FXML public void handleBackToList() {
        NavigationHelper.navigateTo("/tn/esprit/view/front_CoursList.fxml", modulesContainer, currentUser, ctrl -> {
            if (ctrl instanceof FrontCoursListController) {
                ((FrontCoursListController) ctrl).initData(currentUser, currentCategorie, currentModule);
            }
        });
    }

    @FXML public void handleCoursCategories() {
        NavigationHelper.navigateTo("/tn/esprit/view/front_CoursCategories.fxml", modulesContainer, currentUser);
    }

    @FXML public void handleHome() {
        NavigationHelper.navigateTo("/tn/esprit/view/front_user_dashboard.fxml", modulesContainer, currentUser);
    }

    @FXML public void handleProfile() {
        NavigationHelper.navigateTo("/tn/esprit/view/front_profile.fxml", modulesContainer, currentUser);
    }

    @FXML public void handleMeets() {
        NavigationHelper.navigateTo("/tn/esprit/view/front_MeetList.fxml", modulesContainer, currentUser);
    }

    @FXML public void handleGameList() {
        NavigationHelper.navigateTo("/tn/esprit/view/front_GameList.fxml", modulesContainer, currentUser);
    }

    @FXML public void handleEvents() {
        NavigationHelper.navigateTo("/tn/esprit/view/frontEvent.fxml", modulesContainer, currentUser);
    }

    @FXML public void handleForums() {
        NavigationHelper.navigateTo("/tn/esprit/view/front_forum.fxml", modulesContainer, currentUser);
    }

    @FXML public void handleLogout() {
        NavigationHelper.navigateTo("/tn/esprit/view/front_login.fxml", modulesContainer, null);
    }

    private void exportCoursContentPdf(Cours cours) {
        // Implementation similar to FrontCoursListController
        try {
            String fileName = "cours_" + cours.getTitre().replaceAll("[^a-zA-Z0-9]", "_") + "_" + 
                             System.currentTimeMillis() + ".pdf";
            java.io.File destFile = new java.io.File(System.getProperty("user.home"), fileName);
            
            exportSingleCoursePdf(destFile, cours);
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Export Successful");
            success.setHeaderText("Course PDF Generated");
            success.setContentText("The course \"" + cours.getTitre() + "\" has been exported to: " + destFile.getAbsolutePath());
            success.showAndWait();
            
            try {
                if (destFile.exists()) {
                    java.awt.Desktop.getDesktop().open(destFile);
                }
            } catch (Exception e) {
                System.err.println("Cannot open PDF: " + e.getMessage());
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Error exporting course PDF: " + e.getMessage()).showAndWait();
        }
    }

    private static class SimpleHeaderFooterEvent extends com.itextpdf.text.pdf.PdfPageEventHelper {
        @Override
        public void onEndPage(com.itextpdf.text.pdf.PdfWriter writer, com.itextpdf.text.Document document) {
            com.itextpdf.text.pdf.PdfContentByte cb = writer.getDirectContent();
            
            cb.setColorStroke(new com.itextpdf.text.BaseColor(226, 232, 240));
            cb.setLineWidth(0.5f);
            cb.moveTo(document.leftMargin(), document.bottomMargin() - 5);
            cb.lineTo(document.right(), document.bottomMargin() - 5);
            cb.stroke();
            
            try {
                com.itextpdf.text.pdf.BaseFont baseFont = com.itextpdf.text.pdf.BaseFont.createFont(
                        com.itextpdf.text.pdf.BaseFont.HELVETICA, com.itextpdf.text.pdf.BaseFont.WINANSI, 
                        com.itextpdf.text.pdf.BaseFont.EMBEDDED);
                
                cb.beginText();
                cb.setFontAndSize(baseFont, 8);
                cb.showTextAligned(com.itextpdf.text.Element.ALIGN_LEFT, "Naja7ni - Course",
                        document.leftMargin(), document.bottomMargin() - 18, 0);
                cb.showTextAligned(com.itextpdf.text.Element.ALIGN_RIGHT, "Page " + writer.getPageNumber(),
                        document.right(), document.bottomMargin() - 18, 0);
                cb.endText();
            } catch (Exception e) {
                // Si le BaseFont échoue, on saute le footer
                System.err.println("Erreur BaseFont: " + e.getMessage());
            }
        }
    }

    private void showCompletionDialog() {
        Alert dlg = new Alert(Alert.AlertType.INFORMATION);
        dlg.setTitle("Learning Path Complete!");
        dlg.setHeaderText("Congratulations! ");
        dlg.setContentText("You have completed all " + coursList.size() + " courses in the \"" + 
                currentModule.getTitre() + "\" module.");
        dlg.showAndWait();
    }

    // Notes management methods
    private String loadCourseNotes(int courseId) {
        try {
            String notesFile = "course_notes_" + currentUser.getId() + "_" + courseId + ".txt";
            java.io.File file = new java.io.File(System.getProperty("user.home"), notesFile);
            if (file.exists()) {
                return new String(java.nio.file.Files.readAllBytes(file.toPath()));
            }
        } catch (Exception e) {
            System.err.println("Error loading notes: " + e.getMessage());
        }
        return null;
    }

    private void saveCourseNotes(int courseId, String notes) {
        try {
            String notesFile = "course_notes_" + currentUser.getId() + "_" + courseId + ".txt";
            java.io.File file = new java.io.File(System.getProperty("user.home"), notesFile);
            java.nio.file.Files.write(file.toPath(), notes.getBytes());
        } catch (Exception e) {
            System.err.println("Error saving notes: " + e.getMessage());
        }
    }

    // ===== NOUVELLES MÉTHODES POUR LA NOUVELLE STRUCTURE =====
    
    @FXML
    private void handleSort(ActionEvent event) {
        // Implémentation du tri par date
        if (coursList != null && !coursList.isEmpty()) {
            // Tri par date de création (si disponible) ou par ordre
            coursList.sort((c1, c2) -> {
                // Comparaison simple - à améliorer avec de vraies dates
                return Integer.compare(c1.getOrdre(), c2.getOrdre());
            });
            // Rafraîchir l'affichage
            initializeNewStructure();
            saveParcoursState();
        }
    }
    
    @FXML
    private void handleSaveParcours(ActionEvent event) {
        saveParcoursState();

        // Sauvegarder l'ordre du parcours
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sauvegarde");
        alert.setHeaderText("Parcours sauvegardé");
        alert.setContentText("Votre parcours d'apprentissage a été sauvegardé avec succès.");
        alert.showAndWait();
    }
    
    // Méthode pour créer les cartes de modules
    private VBox createModuleCard(Cours_Module module) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-padding:16; -fx-border-color:#e5e7eb; -fx-border-radius:12;");
        card.setPrefWidth(280);
        
        // Titre du module
        Label titleLabel = new Label(module.getTitre());
        titleLabel.setStyle("-fx-font-size:16; -fx-font-weight:700; -fx-text-fill:#1f2937;");
        
        // Nombre de cours
        int coursCount = this.coursList.stream().filter(c -> c.getModuleId() == module.getId()).collect(Collectors.toList()).size();
        Label countLabel = new Label(coursCount + " cours");
        countLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:14;");
        
        // Bouton Voir
        Button viewBtn = new Button("Voir");
        viewBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-font-weight:700; -fx-background-radius:6; -fx-padding:6 12;");
        
        // Liste des cours dans le module
        VBox coursListBox = new VBox(8);
        coursListBox.setStyle("-fx-padding-top:8;");

        boolean expanded = moduleExpanded.getOrDefault(module.getId(), true);
        coursListBox.setVisible(expanded);
        coursListBox.setManaged(expanded);
        viewBtn.setText(expanded ? "Masquer" : "Voir");
        viewBtn.setOnAction(e -> {
            boolean now = !moduleExpanded.getOrDefault(module.getId(), true);
            moduleExpanded.put(module.getId(), now);
            coursListBox.setVisible(now);
            coursListBox.setManaged(now);
            viewBtn.setText(now ? "Masquer" : "Voir");
        });

        List<Cours> moduleCourses = getOrderedCoursesForModule(module.getId());
        for (Cours cours : moduleCourses) {
            VBox courseCard = createCourseCard(cours);
            setupCourseDragAndDrop(courseCard, cours.getId(), module.getId());
            coursListBox.getChildren().add(courseCard);
        }
        
        card.getChildren().addAll(titleLabel, countLabel, viewBtn, coursListBox);
        
        // Configuration du drag & drop
        setupModuleDragAndDrop(card, module);
        
        return card;
    }
    
    // Méthode pour créer les cartes de cours éditables
    private VBox createCourseCard(Cours cours) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color:#f8fafc; -fx-background-radius:8; -fx-padding:12; -fx-border-color:#e2e8f0; -fx-border-radius:8;");
        
        // Titre
        Label titleLabel = new Label(cours.getTitre());
        titleLabel.setStyle("-fx-font-size:14; -fx-font-weight:600; -fx-text-fill:#1f2937;");
        
        // Statut (dropdown)
        ComboBox<String> statusCombo = new ComboBox<>();
        statusCombo.getItems().addAll("À faire", "En cours", "Terminé");
        String current = courseStatus.getOrDefault(cours.getId(), "À faire");
        statusCombo.setValue(current);
        statusCombo.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:4;");

        statusCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String prev = oldVal == null ? courseStatus.getOrDefault(cours.getId(), "À faire") : oldVal;
            courseStatus.put(cours.getId(), newVal);

            if (!"Terminé".equals(prev) && "Terminé".equals(newVal)) {
                xp += XP_PER_COURSE;
            } else if ("Terminé".equals(prev) && !"Terminé".equals(newVal)) {
                xp = Math.max(0, xp - XP_PER_COURSE);
            }
            if (xpLabel != null) {
                xpLabel.setText(String.valueOf(xp));
            }
            refreshRoadmap();
            saveParcoursState();
        });
        
        // Objectif (input)
        TextField objectifField = new TextField();
        objectifField.setPromptText("Objectif...");
        objectifField.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:4; -fx-padding:6;");
        
        // Notes (textarea)
        TextArea notesArea = new TextArea();
        notesArea.setPromptText("Notes...");
        notesArea.setStyle("-fx-background-color:white; -fx-border-color:#e2e8f0; -fx-border-radius:4; -fx-padding:6; -fx-font-size:12;");
        notesArea.setPrefRowCount(3);
        
        card.getChildren().addAll(titleLabel, statusCombo, objectifField, notesArea);
        
        return card;
    }
    
    // Configuration du drag & drop pour les modules
    private void setupModuleDragAndDrop(VBox card, Cours_Module module) {
        card.setOnDragDetected(event -> {
            Dragboard db = card.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("MODULE_" + module.getId());
            db.setContent(content);
            event.consume();
        });
        
        card.setOnDragOver(event -> {
            if (event.getGestureSource() != card && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });
        
        card.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            
            if (db.hasString()) {
                String data = db.getString();
                if (data.startsWith("MODULE_")) {
                    try {
                        int draggedId = Integer.parseInt(data.substring("MODULE_".length()));
                        int targetId = module.getId();
                        if (draggedId != targetId) {
                            int from = moduleOrder.indexOf(draggedId);
                            int to = moduleOrder.indexOf(targetId);
                            if (from >= 0 && to >= 0) {
                                moduleOrder.remove(from);
                                if (to > from) to--;
                                moduleOrder.add(to, draggedId);
                                renderModulesAndRoadmap();
                                saveParcoursState();
                            }
                        }
                        success = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            event.setDropCompleted(success);
        });
    }
    
    // Méthode pour créer les cartes de roadmap
    private VBox createRoadmapCard(Cours_Module module) {
        VBox card = new VBox(12);
        card.setStyle("-fx-background-color:white; -fx-background-radius:12; -fx-padding:16; -fx-border-color:#e5e7eb; -fx-border-radius:12;");
        
        // Titre du module
        Label titleLabel = new Label(module.getTitre());
        titleLabel.setStyle("-fx-font-size:16; -fx-font-weight:700; -fx-text-fill:#1f2937;");
        
        // Progression
        List<Cours> moduleCourses = getOrderedCoursesForModule(module.getId());
        long completedCount = moduleCourses.stream().filter(c -> "Terminé".equals(courseStatus.getOrDefault(c.getId(), "À faire"))).count();
        String progressText = completedCount + "/" + moduleCourses.size() + " (" + 
                (moduleCourses.isEmpty() ? 0 : (int)((completedCount * 100) / moduleCourses.size())) + "%)";
        
        Label progressLabel = new Label(progressText);
        progressLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:14;");
        
        // Barre de progression
        ProgressBar progressBar = new ProgressBar();
        progressBar.setProgress(moduleCourses.isEmpty() ? 0 : (double)completedCount / moduleCourses.size());
        progressBar.setStyle("-fx-accent:#0FB5A9; -fx-background-radius:99;");
        
        // Liste des cours avec statut
        VBox coursListBox = new VBox(4);
        for (Cours cours : moduleCourses) {
            String st = courseStatus.getOrDefault(cours.getId(), "À faire");
            Label courseLabel = new Label("• " + cours.getTitre() + " — " + st);
            courseLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
            coursListBox.getChildren().add(courseLabel);
        }
        
        card.getChildren().addAll(titleLabel, progressLabel, progressBar, coursListBox);
        
        return card;
    }
    
    // Initialisation de la nouvelle structure
    private void initializeNewStructure() {
        if (modulesContainer == null || roadmapContainer == null) {
            return;
        }

        // Initialiser l'avatar utilisateur
        if (currentUser != null) {
            String firstName = currentUser.getFirstName();
            String lastName = currentUser.getLastName();
            String fullName = (firstName != null ? firstName : "") + (lastName != null ? lastName : "");
            if (!fullName.isEmpty()) {
                userInitial.setText(fullName.substring(0, 1).toUpperCase());
            } else {
                userInitial.setText("U");
            }
            if (badgesLabel != null) {
                badgesLabel.setText("0");
            }
        }
        
        if (coursList == null || coursList.isEmpty()) {
            modulesContainer.getChildren().clear();
            roadmapContainer.getChildren().clear();
            if (xpLabel != null) xpLabel.setText(String.valueOf(xp));
            return;
        }

        if (moduleOrder.isEmpty()) {
            Set<Integer> moduleIds = coursList.stream().map(Cours::getModuleId).collect(Collectors.toCollection(LinkedHashSet::new));
            moduleOrder.addAll(moduleIds);
        }

        // Init course order per module (based on cours.ordre)
        if (courseOrderByModule.isEmpty()) {
            Map<Integer, List<Cours>> grouped = coursList.stream()
                    .collect(Collectors.groupingBy(Cours::getModuleId));
            for (Map.Entry<Integer, List<Cours>> e : grouped.entrySet()) {
                List<Integer> ids = e.getValue().stream()
                        .sorted(Comparator.comparingInt(Cours::getOrdre))
                        .map(Cours::getId)
                        .collect(Collectors.toList());
                courseOrderByModule.put(e.getKey(), new ArrayList<>(ids));
            }
        }

        xp = (int) coursList.stream().filter(c -> "Terminé".equals(courseStatus.getOrDefault(c.getId(), "À faire"))).count() * XP_PER_COURSE;
        if (xpLabel != null) xpLabel.setText(String.valueOf(xp));

        renderModulesAndRoadmap();
    }

    private Path getParcoursStatePath() {
        String uid = String.valueOf(userId);
        Path dir = Paths.get(System.getProperty("user.home"), ".naja7ni", "parcours");
        return dir.resolve("parcours_" + uid + ".json");
    }

    private void loadParcoursState() {
        if (userId <= 0) return;
        Path p = getParcoursStatePath();
        if (!Files.exists(p)) return;
        try {
            ParcoursState st = objectMapper.readValue(p.toFile(), ParcoursState.class);

            moduleOrder.clear();
            if (st.moduleOrder != null) moduleOrder.addAll(st.moduleOrder);

            courseOrderByModule.clear();
            if (st.courseOrderByModule != null) {
                for (Map.Entry<String, List<Integer>> e : st.courseOrderByModule.entrySet()) {
                    try {
                        courseOrderByModule.put(Integer.parseInt(e.getKey()), new ArrayList<>(e.getValue()));
                    } catch (NumberFormatException ignore) {
                    }
                }
            }

            courseStatus.clear();
            if (st.courseStatus != null) {
                for (Map.Entry<String, String> e : st.courseStatus.entrySet()) {
                    try {
                        courseStatus.put(Integer.parseInt(e.getKey()), e.getValue());
                    } catch (NumberFormatException ignore) {
                    }
                }
            }

            moduleExpanded.clear();
            if (st.moduleExpanded != null) {
                for (Map.Entry<String, Boolean> e : st.moduleExpanded.entrySet()) {
                    try {
                        moduleExpanded.put(Integer.parseInt(e.getKey()), Boolean.TRUE.equals(e.getValue()));
                    } catch (NumberFormatException ignore) {
                    }
                }
            }

            if (st.xp != null) {
                xp = Math.max(0, st.xp);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveParcoursState() {
        if (userId <= 0) return;
        try {
            Path p = getParcoursStatePath();
            Files.createDirectories(p.getParent());

            ParcoursState st = new ParcoursState();
            st.userId = userId;
            st.xp = xp;
            st.moduleOrder = new ArrayList<>(moduleOrder);

            st.courseOrderByModule = new LinkedHashMap<>();
            for (Map.Entry<Integer, List<Integer>> e : courseOrderByModule.entrySet()) {
                st.courseOrderByModule.put(String.valueOf(e.getKey()), new ArrayList<>(e.getValue()));
            }

            st.courseStatus = new LinkedHashMap<>();
            for (Map.Entry<Integer, String> e : courseStatus.entrySet()) {
                st.courseStatus.put(String.valueOf(e.getKey()), e.getValue());
            }

            st.moduleExpanded = new LinkedHashMap<>();
            for (Map.Entry<Integer, Boolean> e : moduleExpanded.entrySet()) {
                st.moduleExpanded.put(String.valueOf(e.getKey()), e.getValue());
            }

            objectMapper.writeValue(p.toFile(), st);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class ParcoursState {
        public Integer userId;
        public Integer xp;
        public List<Integer> moduleOrder;
        public Map<String, List<Integer>> courseOrderByModule;
        public Map<String, String> courseStatus;
        public Map<String, Boolean> moduleExpanded;
    }

    private String getModuleTitle(int moduleId) {
        if (currentModule != null && currentModule.getId() == moduleId) {
            return currentModule.getTitre();
        }
        return "Module " + moduleId;
    }

    private void renderModulesAndRoadmap() {
        if (modulesContainer == null || roadmapContainer == null) return;

        modulesContainer.getChildren().clear();
        roadmapContainer.getChildren().clear();

        for (Integer moduleId : moduleOrder) {
            Cours_Module tempModule = new Cours_Module();
            tempModule.setId(moduleId);
            tempModule.setTitre(getModuleTitle(moduleId));
            modulesContainer.getChildren().add(createModuleCard(tempModule));
        }

        refreshRoadmap();
    }

    private void refreshRoadmap() {
        if (roadmapContainer == null || coursList == null) return;
        roadmapContainer.getChildren().clear();

        for (Integer moduleId : moduleOrder) {
            Cours_Module tempModule = new Cours_Module();
            tempModule.setId(moduleId);
            tempModule.setTitre(getModuleTitle(moduleId));
            roadmapContainer.getChildren().add(createRoadmapCard(tempModule));
        }
    }

    private List<Cours> getOrderedCoursesForModule(int moduleId) {
        if (coursList == null) return Collections.emptyList();
        Map<Integer, Cours> byId = coursList.stream().collect(Collectors.toMap(Cours::getId, c -> c, (a, b) -> a));

        List<Integer> order = courseOrderByModule.get(moduleId);
        if (order == null || order.isEmpty()) {
            return coursList.stream()
                    .filter(c -> c.getModuleId() == moduleId)
                    .sorted(Comparator.comparingInt(Cours::getOrdre))
                    .collect(Collectors.toList());
        }

        List<Cours> out = new ArrayList<>();
        for (Integer id : order) {
            Cours c = byId.get(id);
            if (c != null && c.getModuleId() == moduleId) out.add(c);
        }
        // Ajouter des cours non présents dans la liste d'ordre
        Set<Integer> seen = out.stream().map(Cours::getId).collect(Collectors.toSet());
        coursList.stream()
                .filter(c -> c.getModuleId() == moduleId && !seen.contains(c.getId()))
                .sorted(Comparator.comparingInt(Cours::getOrdre))
                .forEach(out::add);
        return out;
    }

    private void setupCourseDragAndDrop(VBox courseCard, int courseId, int moduleId) {
        courseCard.setOnDragDetected(event -> {
            Dragboard db = courseCard.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("COURSE_" + courseId + "_" + moduleId);
            db.setContent(content);
            event.consume();
        });

        courseCard.setOnDragOver(event -> {
            if (event.getGestureSource() != courseCard && event.getDragboard().hasString()) {
                String s = event.getDragboard().getString();
                if (s != null && s.startsWith("COURSE_")) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
            }
            event.consume();
        });

        courseCard.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                String s = db.getString();
                if (s != null && s.startsWith("COURSE_")) {
                    try {
                        String[] parts = s.substring("COURSE_".length()).split("_");
                        int draggedCourseId = Integer.parseInt(parts[0]);
                        int draggedModuleId = Integer.parseInt(parts[1]);

                        if (draggedModuleId == moduleId && draggedCourseId != courseId) {
                            List<Integer> order = courseOrderByModule.computeIfAbsent(moduleId, k -> new ArrayList<>());
                            if (order.isEmpty()) {
                                order.addAll(getOrderedCoursesForModule(moduleId).stream().map(Cours::getId).collect(Collectors.toList()));
                            }
                            int from = order.indexOf(draggedCourseId);
                            int to = order.indexOf(courseId);
                            if (from >= 0 && to >= 0) {
                                order.remove(from);
                                if (to > from) to--;
                                order.add(to, draggedCourseId);
                                renderModulesAndRoadmap();
                                saveParcoursState();
                            }
                        }
                        success = true;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }
}
