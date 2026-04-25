package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;

import tn.esprit.services.WhiteboardService;

public class WhiteboardController implements Initializable {

    public enum Tool { SELECT, PAN, PEN, MARKER, ERASER, TEXT, NOTE, RECT, CIRCLE, LINE, ARROW }

    // --- FXML Injections ---
    @FXML private StackPane rootStackPane;
    @FXML private StackPane drawingArea;
    @FXML private javafx.scene.Group drawGroup;
    @FXML private Canvas bgCanvas;
    @FXML private Canvas canvas;
    @FXML private Pane overlayPane;
    @FXML private HBox toolbar;

    // Undo / Redo
    @FXML private Button undoBtn;
    @FXML private Button redoBtn;

    // Outils
    @FXML private Button selectBtn;
    @FXML private Button panBtn;
    @FXML private Button penBtn;
    @FXML private Button markerBtn;
    @FXML private Button eraserBtn;
    @FXML private Button rectBtn;
    @FXML private Button circleBtn;
    @FXML private Button lineBtn;
    @FXML private Button arrowBtn;
    @FXML private Button textBtn;
    @FXML private Button noteBtn;

    // Palette de couleurs
    @FXML private Button colorBlack;
    @FXML private Button colorRed;
    @FXML private Button colorOrange;
    @FXML private Button colorGreen;
    @FXML private Button colorBlue;
    @FXML private Button colorPurple;
    @FXML private Button colorWhite;
    @FXML private ColorPicker customColorPicker;

    // Taille du trait
    @FXML private Slider sizeSlider;

    // Actions
    @FXML private Button clearBtn;
    @FXML private Button downloadBtn;
    @FXML private Button moreBtn;
    @FXML private Button publishBtn;

    // Zoom
    @FXML private Button zoomOutBtn;
    @FXML private Button zoomInBtn;
    @FXML private Label zoomLabel;

    // Pages
    @FXML private Button page1Tab;
    @FXML private Button page2Tab;
    @FXML private Button addPageBtn;

    // --- État interne ---
    private GraphicsContext gc;
    private Tool currentTool = Tool.PEN;
    private Color currentPenColor = Color.BLACK;
    private double currentPenSize = 4.0;
    private double startX, startY;
    private double lastX, lastY;
    private double translateX = 0, translateY = 0;
    private double currentScale = 1.0;

    private final Stack<WritableImage> undoStack = new Stack<>();
    private final Stack<WritableImage> redoStack = new Stack<>();
    private static final int MAX_STACK_SIZE = 50;
    private final Stack<List<Node>> nodeUndoStack = new Stack<>();
    private final Stack<List<Node>> nodeRedoStack = new Stack<>();

    private Rectangle selectionRect;
    private Node selectedNode;

    private Runnable onPublishAction;
    private final WhiteboardService whiteboardService = new WhiteboardService();

    private WritableImage shapeSnapshot;
    private final String[] STICKY_COLORS = {"#fff9a3", "#b5ead7", "#c9b1ff", "#ffb7c5", "#ffd6a5"};
    private int stickyColorIdx = 0;

    // État pour les pages
    private int currentPage = 1;
    private WritableImage page1Snapshot;
    private List<Node> page1Nodes = new ArrayList<>();
    private WritableImage page2Snapshot;
    private List<Node> page2Nodes = new ArrayList<>();

    // Styles boutons (Premium Light Style)
    private static final String BTN_NORMAL = "-fx-background-color: transparent; -fx-background-radius: 6px; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-cursor: hand; -fx-padding: 7;";
    private static final String BTN_HOVER  = "-fx-background-color: #f1f5f9; -fx-background-radius: 6px; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-cursor: hand; -fx-padding: 7;";
    private static final String BTN_ACTIVE = "-fx-background-color: #e3f0ff; -fx-background-radius: 6px; -fx-min-width: 32px; -fx-min-height: 32px; -fx-max-width: 32px; -fx-max-height: 32px; -fx-cursor: hand; -fx-padding: 7;";

    private static final String COLOR_INACTIVE = "#64748b";
    private static final String COLOR_ACTIVE   = "#1a5fb4";
    private static final String COLOR_HOVER    = "#1a1a1a";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gc = canvas.getGraphicsContext2D();
        drawGrid();
        setupIcons();
        setupColorButtons();
        setupSizeSlider();
        setupZoomBar();
        setupEvents();
        setActiveTool(Tool.PEN);
        
        // Ensure overlayPane captures events for drawing
        overlayPane.setPickOnBounds(true);
        canvas.setMouseTransparent(true); // Let events pass to overlayPane if needed, but we'll put listeners on overlayPane
    }

    public void setOnPublish(Runnable action) {
        this.onPublishAction = action;
    }

    // -------------------------------------------------------------------------
    // GRILLE (identique au bgCanvas HTML : grille pointillée 30px, #F8FAFC)
    // -------------------------------------------------------------------------
    private void drawGrid() {
        double w = bgCanvas.getWidth(), h = bgCanvas.getHeight();
        GraphicsContext bgc = bgCanvas.getGraphicsContext2D();
        bgc.setFill(Color.WHITE);
        bgc.fillRect(0, 0, w, h);
        bgc.setFill(Color.web("rgba(180,180,200,0.25)"));
        double spacing = 30;
        for (double x = 0; x < w; x += spacing) {
            bgc.setLineWidth(0.5);
            bgc.strokeLine(x, 0, x, h);
        }
        for (double y = 0; y < h; y += spacing) {
            bgc.setLineWidth(0.5);
            bgc.strokeLine(0, y, w, y);
        }
    }

    // -------------------------------------------------------------------------
    // ICÔNES (SVGPath identiques au HTML — même jeu d'icônes)
    // -------------------------------------------------------------------------
    private void setupIcons() {
        setIcon(undoBtn,    "M12.5 8c-2.65 0-5.05.99-6.9 2.6L2 7v9h9l-3.62-3.62c1.39-1.16 3.16-1.88 5.12-1.88 3.54 0 6.55 2.31 7.6 5.5l2.37-.78C21.08 11.03 17.15 8 12.5 8z", "Annuler");
        setIcon(redoBtn,    "M18.4 10.6C16.55 8.99 14.15 8 11.5 8c-4.65 0-8.58 3.03-9.96 7.22L3.9 16c1.05-3.19 4.05-5.5 7.6-5.5 1.95 0 3.73.72 5.12 1.88L13 16h9V7l-3.6 3.6z", "Rétablir");
        setIcon(selectBtn,  "M13.64,21.89C13.15,22.44 12.31,22.42 11.85,21.82L7.67,16.16L4,19.83V2L18.47,16.47L13.64,21.89Z", "Sélectionner");
        setIcon(panBtn,     "M22,14.5c0-1.1-0.9-2-2-2s-2,.9-2,2v1c0,.28-.22,.5-.5,.5s-.5-.22-.5-.5V6.5c0-1.1-.9-2-2-2s-2,.9-2,2v9c0,.28-.22,.5-.5,.5s-.5-.22-.5-.5V4.5c0-1.1-.9-2-2-2s-2,.9-2,2v11c0,.28-.22,.5-.5,.5s-.5-.22-.5-.5V7.5c0-1.1-.9-2-2-2s-2,.9-2,2v10.32c0,.61,.22,1.21,.62,1.67l4.3,5.01c.4,.47,1.01,.75,1.65,.75h5.43c1.38,0,2.5-1.12,2.5-2.5V14.5z", "Déplacer");
        setIcon(penBtn,     "M3,17.25V21H6.75L17.81,9.93L14.06,6.18L3,17.25Z", "Stylo");
        setIcon(markerBtn,  "M18.5,1.15C17.97,1.15 17.46,1.34 17.07,1.73L11.26,7.55L16.91,13.2L22.73,7.39C23.5,6.61 23.5,5.35 22.73,4.56L19.97,1.73C19.55,1.34 19.04,1.15 18.5,1.15M10.3,8.5L4.34,14.46C3.56,15.24 3.56,16.5 4.34,17.28L7.1,20.04C7.5,20.44 8,20.62 8.5,20.62C9,20.62 9.5,20.44 9.89,20.04L15.85,14.08L10.3,8.5M2,22L5.5,20.5L3.5,18.5L2,22Z", "Marqueur");
        setIcon(eraserBtn,  "M16.24,3.56L21.19,8.51C21.97,9.29 21.97,10.55 21.19,11.33L14.6,17.92L18.13,21.45L16.71,22.87L13.18,19.34L11.77,20.75C11,21.53 9.74,21.53 8.96,20.75L3.5,15.29C2.72,14.5 2.72,13.24 3.5,12.46L10.09,5.87L6.56,2.34L7.97,0.93L11.5,4.46L16.24,3.56Z", "Gomme");
        setIcon(rectBtn,    "M3,3H21V21H3V3M5,5V19H19V5H5Z", "Rectangle");
        setIcon(circleBtn,  "M12,2C6.47,2 2,6.47 2,12C2,17.53 6.47,22 12,22C17.53,22 22,17.53 22,12C22,6.47 17.53,2 12,2M12,20C7.59,20 4,16.41 4,12C4,7.59 7.59,4 12,4C16.41,4 20,7.59 20,12C20,16.41 16.41,20 12,20Z", "Cercle");
        setIcon(lineBtn,    "M21,11H3V13H21V11Z", "Ligne");
        setIcon(arrowBtn,   "M4,11V13H16L10.5,18.5L11.92,19.92L19.84,12L11.92,4.08L10.5,5.5L16,11H4Z", "Flèche");
        setIcon(textBtn,    "M5,3H19V5H13V19H11V5H5V3Z", "Texte");
        setIcon(noteBtn,    "M14,2H6C4.9,2 4,2.9 4,4V20C4,21.1 4.9,22 6,22H18C19.1,22 20,21.1 20,20V8L14,2M13,9V3.5L18.5,9H13Z", "Note");
        setIcon(clearBtn,   "M9,3V4H4V6H5V19A2,2 0 0,0 7,21H17A2,2 0 0,0 19,19V6H20V4H15V3H9M7,6H17V19H7V6M9,8V17H11V8H9M13,8V17H15V8H13Z", "Effacer tout");
        setIcon(downloadBtn,"M5,20H19V18H5M19,9H15V3H9V9H5L12,16L19,9Z", "Télécharger");
        setIcon(moreBtn,    "M6,10C4.9,10 4,10.9 4,12C4,13.1 4.9,14 6,14C7.1,14 8,13.1 8,12C8,10.9 7.1,10 6,10M18,10C16.9,10 16,10.9 16,12C16,13.1 16.9,14 18,14C19.1,14 20,13.1 20,12C20,10.9 19.1,10 18,10M12,10C10.9,10 10,10.9 10,12C10,13.1 10.9,14 12,14C13.1,14 14,13.1 14,12C14,10.9 13.1,10 12,10Z", "Plus");
    }

    private void setIcon(Button btn, String svgData, String tooltip) {
        if (btn == null) return;
        SVGPath icon = new SVGPath();
        icon.setContent(svgData);
        icon.setFill(Color.web(COLOR_INACTIVE));
        icon.setScaleX(0.75);
        icon.setScaleY(0.75);
        btn.setGraphic(icon);
        btn.setText(null);
        btn.setUserData(icon);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle(BTN_NORMAL);
        btn.setFocusTraversable(false);
        btn.setOnMouseEntered(e -> {
            if (!isActive(btn)) {
                btn.setStyle(BTN_HOVER);
                icon.setFill(Color.web(COLOR_HOVER));
            }
        });
        btn.setOnMouseExited(e  -> {
            if (!isActive(btn)) {
                btn.setStyle(BTN_NORMAL);
                icon.setFill(Color.web(COLOR_INACTIVE));
            }
        });
    }

    private void setupColorButtons() {
        applyColorBtn(colorBlack,  "#1a1a1a", Color.web("#1a1a1a"));
        applyColorBtn(colorRed,    "#e74c3c", Color.web("#e74c3c"));
        applyColorBtn(colorOrange, "#f39c12", Color.web("#f39c12"));
        applyColorBtn(colorGreen,  "#2ecc71", Color.web("#2ecc71"));
        applyColorBtn(colorBlue,   "#3498db", Color.web("#3498db"));
        applyColorBtn(colorPurple, "#9b59b6", Color.web("#9b59b6"));
        applyColorBtn(colorWhite,  "#ffffff", Color.WHITE);
        highlightColorBtn(colorBlack);

        if (customColorPicker != null) {
            customColorPicker.setValue(Color.web("#1a1a1a"));
            customColorPicker.setOnAction(e -> {
                currentPenColor = customColorPicker.getValue();
                clearColorSelections();
            });
        }
    }

    private void applyColorBtn(Button btn, String hex, Color color) {
        if (btn == null) return;
        btn.setGraphic(null);
        btn.setText(null);
        btn.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 50%; -fx-min-width: 18px; -fx-min-height: 18px; -fx-max-width: 18px; -fx-max-height: 18px; -fx-cursor: hand; -fx-border-radius: 50%; -fx-border-width: 2px; -fx-border-color: transparent;");
        btn.setFocusTraversable(false);
        btn.setOnAction(e -> {
            currentPenColor = color;
            clearColorSelections();
            highlightColorBtn(btn);
        });
        btn.setUserData(hex);
    }

    private void clearColorSelections() {
        Button[] colorBtns = {colorBlack, colorRed, colorOrange, colorGreen, colorBlue, colorPurple, colorWhite};
        for (Button b : colorBtns) {
            if (b == null) continue;
            String hex = (String) b.getUserData();
            if (hex != null)
                b.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 50%; -fx-min-width: 18px; -fx-min-height: 18px; -fx-max-width: 18px; -fx-max-height: 18px; -fx-cursor: hand; -fx-border-radius: 50%; -fx-border-width: 2px; -fx-border-color: transparent;");
        }
    }

    private void highlightColorBtn(Button btn) {
        if (btn == null) return;
        String hex = (String) btn.getUserData();
        if (hex != null)
            btn.setStyle("-fx-background-color: " + hex + "; -fx-background-radius: 50%; -fx-min-width: 18px; -fx-min-height: 18px; -fx-max-width: 18px; -fx-max-height: 18px; -fx-cursor: hand; -fx-border-radius: 50%; -fx-border-width: 2px; -fx-border-color: #1a1a1a;");
    }

    private void setupSizeSlider() {
        if (sizeSlider == null) return;
        sizeSlider.setValue(currentPenSize);
        sizeSlider.valueProperty().addListener((obs, old, nv) -> {
            currentPenSize = nv.doubleValue();
            gc.setLineWidth(currentPenSize);
        });
    }

    private void setupZoomBar() {
        updateZoomLabel();
    }

    private void updateZoomLabel() {
        if (zoomLabel != null)
            zoomLabel.setText(Math.round(currentScale * 100) + "%");
    }

    @FXML private void onZoomIn() {
        currentScale = Math.min(3.0, currentScale + 0.1);
        drawGroup.setScaleX(currentScale);
        drawGroup.setScaleY(currentScale);
        drawGrid();
        updateZoomLabel();
    }

    @FXML private void onZoomOut() {
        currentScale = Math.max(0.3, currentScale - 0.1);
        drawGroup.setScaleX(currentScale);
        drawGroup.setScaleY(currentScale);
        drawGrid();
        updateZoomLabel();
    }

    @FXML private void onSelectPage1() { 
        if (currentPage == 1) return;
        saveCurrentPage();
        currentPage = 1;
        highlightPage(page1Tab); 
        loadCurrentPage();
    }
    
    @FXML private void onSelectPage2() { 
        if (currentPage == 2) return;
        saveCurrentPage();
        currentPage = 2;
        highlightPage(page2Tab); 
        loadCurrentPage();
    }

    private void saveCurrentPage() {
        SnapshotParameters p = new SnapshotParameters();
        p.setFill(Color.TRANSPARENT);
        if (currentPage == 1) {
            page1Snapshot = canvas.snapshot(p, null);
            page1Nodes = new ArrayList<>(overlayPane.getChildren());
        } else if (currentPage == 2) {
            page2Snapshot = canvas.snapshot(p, null);
            page2Nodes = new ArrayList<>(overlayPane.getChildren());
        }
    }

    private void loadCurrentPage() {
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        overlayPane.getChildren().clear();
        undoStack.clear();
        redoStack.clear();
        nodeUndoStack.clear();
        nodeRedoStack.clear();
        
        if (currentPage == 1) {
            if (page1Snapshot != null) gc.drawImage(page1Snapshot, 0, 0);
            if (page1Nodes != null) overlayPane.getChildren().setAll(page1Nodes);
        } else if (currentPage == 2) {
            if (page2Snapshot != null) gc.drawImage(page2Snapshot, 0, 0);
            if (page2Nodes != null) overlayPane.getChildren().setAll(page2Nodes);
        }
    }

    @FXML private void onAddPage() {
        if (addPageBtn != null)
            showTooltip("Nouvelle page créée !");
    }

    private void highlightPage(Button active) {
        String base = "-fx-background-color: white; -fx-background-radius: 10; -fx-font-size: 12px; -fx-padding: 8 16; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);";
        String normal = base + " -fx-text-fill: #64748b;";
        String activeStyle = base + " -fx-text-fill: #1a5fb4; -fx-border-color: #1a5fb4; -fx-border-width: 0 0 2 0; -fx-border-radius: 0 0 10 10;";
        
        for (Button b : new Button[]{page1Tab, page2Tab}) {
            if (b != null) b.setStyle(b == active ? activeStyle : normal);
        }
    }

    private void showTooltip(String msg) {
        Tooltip tp = new Tooltip(msg);
        tp.setAutoHide(true);
        if (rootStackPane != null && rootStackPane.getScene() != null)
            tp.show(rootStackPane.getScene().getWindow());
    }

    private void setupEvents() {
        // Zoom avec CTRL + Scroll sur le root
        rootStackPane.setOnScroll(e -> {
            if (e.isControlDown()) {
                double factor = e.getDeltaY() > 0 ? 1.1 : 0.9;
                currentScale = Math.min(3.0, Math.max(0.3, currentScale * factor));
                drawGroup.setScaleX(currentScale);
                drawGroup.setScaleY(currentScale);
                drawGrid();
                updateZoomLabel();
                e.consume();
            }
        });

        // Les événements de dessin sont maintenant sur l'overlayPane pour éviter d'être bloqués
        overlayPane.setOnMousePressed(e -> {
            // Ne pas commencer à dessiner si on clique sur un élément déjà présent (texte, note) en mode SELECT
            if (currentTool == Tool.SELECT && e.getTarget() != overlayPane) {
                return;
            }
            
            saveToUndoStack();
            
            // Coordonnées locales
            double x = e.getX();
            double y = e.getY();
            
            switch (currentTool) {
                case PEN -> startFreehand(x, y, false);
                case MARKER -> startFreehand(x, y, true);
                case ERASER -> eraseAt(x, y);
                case SELECT -> startSelect(e);
                case PAN -> startPan(e);
                case TEXT -> placeText(x, y);
                case NOTE -> placeNote(x, y);
                case RECT, CIRCLE, LINE, ARROW -> startShape(x, y);
            }
            e.consume();
        });

        overlayPane.setOnMouseDragged(e -> {
            double x = e.getX();
            double y = e.getY();
            
            switch (currentTool) {
                case PEN -> continueFreehand(x, y, false);
                case MARKER -> continueFreehand(x, y, true);
                case ERASER -> eraseAt(x, y);
                case PAN -> continuePan(e);
                case RECT, CIRCLE, LINE, ARROW -> previewShape(x, y);
                case SELECT -> updateSelection(e);
            }
            e.consume();
        });

        overlayPane.setOnMouseReleased(e -> {
            double x = e.getX();
            double y = e.getY();
            
            switch (currentTool) {
                case RECT, CIRCLE, LINE, ARROW -> finalizeShape(x, y);
                case PAN -> overlayPane.setCursor(Cursor.OPEN_HAND);
                case PEN, MARKER -> gc.closePath();
            }
            e.consume();
        });
    }

    private void startFreehand(double x, double y, boolean marker) {
        gc.setStroke(marker ? currentPenColor.deriveColor(0, 1, 1, 0.5) : currentPenColor);
        gc.setLineWidth(marker ? currentPenSize * 2.5 : currentPenSize);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setLineJoin(StrokeLineJoin.ROUND);
        gc.setGlobalAlpha(marker ? 0.5 : 1.0);
        gc.beginPath();
        gc.moveTo(x, y);
        lastX = x;
        lastY = y;
    }

    private void continueFreehand(double x, double y, boolean marker) {
        // Utilisation de quadraticCurveTo pour un trait lisse
        double xc = (lastX + x) / 2;
        double yc = (lastY + y) / 2;
        gc.quadraticCurveTo(lastX, lastY, xc, yc);
        lastX = x;
        lastY = y;
        gc.stroke();
    }

    private void eraseAt(double x, double y) {
        double r = currentPenSize * 3;
        gc.clearRect(x - r / 2, y - r / 2, r, r);
    }

    private void startShape(double x, double y) {
        startX = x;
        startY = y;
        SnapshotParameters params = new SnapshotParameters();
        params.setFill(Color.TRANSPARENT);
        shapeSnapshot = canvas.snapshot(params, null);
    }

    private void previewShape(double x, double y) {
        if (shapeSnapshot == null) return;
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(shapeSnapshot, 0, 0);
        gc.setGlobalAlpha(1.0);
        gc.setStroke(currentPenColor);
        gc.setLineWidth(currentPenSize);
        gc.setLineCap(StrokeLineCap.ROUND);
        gc.setFill(Color.TRANSPARENT);
        drawShapeOnContext(x, y, currentTool);
    }

    private void finalizeShape(double x, double y) {
        gc.setGlobalAlpha(1.0);
        gc.setStroke(currentPenColor);
        gc.setLineWidth(currentPenSize);
        gc.setLineCap(StrokeLineCap.ROUND);
        drawShapeOnContext(x, y, currentTool);
        shapeSnapshot = null;
    }

    private void placeText(double x, double y) {
        TextField tf = new TextField();
        tf.setStyle("-fx-background-color: transparent; -fx-border-color: #3498db; -fx-border-radius: 4; -fx-font-size: " + Math.max(14, currentPenSize * 3) + "px; -fx-text-fill: " + toHex(currentPenColor) + ";");
        tf.setLayoutX(x);
        tf.setLayoutY(y);
        tf.setPromptText("Texte...");
        overlayPane.getChildren().add(tf);
        tf.requestFocus();
        tf.setOnAction(ke -> finalizeText(tf));
        tf.focusedProperty().addListener((obs, old, nv) -> { if (!nv) finalizeText(tf); });
    }

    private void placeNote(double x, double y) {
        String bgColor = STICKY_COLORS[stickyColorIdx++ % STICKY_COLORS.length];
        VBox note = new VBox();
        note.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 2px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 8, 0, 2, 3); -fx-padding: 10;");
        note.setPrefSize(140, 120);
        note.setLayoutX(x);
        note.setLayoutY(y);
        TextArea ta = new TextArea();
        ta.setPromptText("Note...");
        ta.setWrapText(true);
        ta.setPrefRowCount(4);
        ta.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent; -fx-background-insets: 0; -fx-font-size: 13px; -fx-font-family: 'Segoe UI', sans-serif;");
        note.getChildren().add(ta);
        overlayPane.getChildren().add(note);
        makeDraggable(note);
    }

    private void startSelect(MouseEvent e) {
        startX = e.getX();
        startY = e.getY();
        if (selectionRect == null) {
            selectionRect = new Rectangle();
            selectionRect.setFill(Color.web("#3498db", 0.1));
            selectionRect.setStroke(Color.web("#3498db"));
            selectionRect.getStrokeDashArray().addAll(5.0, 5.0);
            overlayPane.getChildren().add(selectionRect);
        }
        selectionRect.setVisible(true);
        selectionRect.setX(startX);
        selectionRect.setY(startY);
        selectionRect.setWidth(0);
        selectionRect.setHeight(0);
        selectedNode = null;
        for (Node n : overlayPane.getChildren()) {
            if (n != selectionRect && n.getBoundsInParent().contains(startX, startY)) {
                selectedNode = n;
                break;
            }
        }
    }

    private void updateSelection(MouseEvent e) {
        if (selectedNode != null) {
            selectedNode.setLayoutX(e.getX() - startX);
            selectedNode.setLayoutY(e.getY() - startY);
        } else if (selectionRect != null) {
            selectionRect.setWidth(Math.abs(e.getX() - startX));
            selectionRect.setHeight(Math.abs(e.getY() - startY));
            selectionRect.setX(Math.min(e.getX(), startX));
            selectionRect.setY(Math.min(e.getY(), startY));
        }
    }

    private void startPan(MouseEvent e) {
        startX = e.getSceneX();
        startY = e.getSceneY();
        overlayPane.setCursor(Cursor.CLOSED_HAND);
    }

    private void continuePan(MouseEvent e) {
        double dx = e.getSceneX() - startX;
        double dy = e.getSceneY() - startY;
        translateX += dx;
        translateY += dy;
        drawGroup.setTranslateX(translateX);
        drawGroup.setTranslateY(translateY);
        startX = e.getSceneX();
        startY = e.getSceneY();
    }

    private void finalizeText(TextField tf) {
        if (!tf.getText().isEmpty()) {
            Label label = new Label(tf.getText());
            label.setStyle("-fx-font-size: " + Math.max(14, currentPenSize * 3) + "px; -fx-text-fill: " + toHex(currentPenColor) + ";");
            label.setLayoutX(tf.getLayoutX());
            label.setLayoutY(tf.getLayoutY());
            overlayPane.getChildren().add(label);
            makeDraggable(label);
        }
        overlayPane.getChildren().remove(tf);
    }

    private void drawShapeOnContext(double ex, double ey, Tool t) {
        double w = Math.abs(ex - startX);
        double h = Math.abs(ey - startY);
        double x = Math.min(ex, startX);
        double y = Math.min(ey, startY);
        switch (t) {
            case RECT   -> { gc.setFill(currentPenColor.deriveColor(0,1,1,0.1)); gc.fillRect(x,y,w,h); gc.strokeRect(x,y,w,h); }
            case CIRCLE -> { double r = Math.max(w,h); gc.setFill(currentPenColor.deriveColor(0,1,1,0.1)); gc.fillOval(x,y,r,r); gc.strokeOval(x,y,r,r); }
            case LINE   -> gc.strokeLine(startX, startY, ex, ey);
            case ARROW  -> drawArrow(startX, startY, ex, ey);
        }
    }

    private void drawArrow(double x1, double y1, double x2, double y2) {
        gc.strokeLine(x1, y1, x2, y2);
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double hw = Math.max(currentPenSize * 3, 12);
        gc.strokeLine(x2, y2, x2 - hw * Math.cos(angle - Math.PI / 6), y2 - hw * Math.sin(angle - Math.PI / 6));
        gc.strokeLine(x2, y2, x2 - hw * Math.cos(angle + Math.PI / 6), y2 - hw * Math.sin(angle + Math.PI / 6));
    }

    private void setActiveTool(Tool tool) {
        this.currentTool = tool;
        if (overlayPane != null)
            overlayPane.setCursor(tool == Tool.PAN ? Cursor.OPEN_HAND : (tool == Tool.TEXT ? Cursor.TEXT : Cursor.CROSSHAIR));

        Button[] toolBtns = {selectBtn, panBtn, penBtn, markerBtn, eraserBtn,
                             rectBtn, circleBtn, lineBtn, arrowBtn, textBtn, noteBtn};
        Tool[]   toolEnums = {Tool.SELECT, Tool.PAN, Tool.PEN, Tool.MARKER, Tool.ERASER,
                              Tool.RECT, Tool.CIRCLE, Tool.LINE, Tool.ARROW, Tool.TEXT, Tool.NOTE};

        for (int i = 0; i < toolBtns.length; i++) {
            Button b = toolBtns[i];
            if (b == null) continue;
            boolean active = toolEnums[i] == tool;
            b.setStyle(active ? BTN_ACTIVE : BTN_NORMAL);
            if (b.getGraphic() instanceof SVGPath icon)
                icon.setFill(active ? Color.web(COLOR_ACTIVE) : Color.web(COLOR_INACTIVE));
        }
    }

    private boolean isActive(Button btn) {
        if (btn == null) return false;
        Tool[] toolEnums = {Tool.SELECT, Tool.PAN, Tool.PEN, Tool.MARKER, Tool.ERASER,
                            Tool.RECT, Tool.CIRCLE, Tool.LINE, Tool.ARROW, Tool.TEXT, Tool.NOTE};
        Button[] toolBtns = {selectBtn, panBtn, penBtn, markerBtn, eraserBtn,
                             rectBtn, circleBtn, lineBtn, arrowBtn, textBtn, noteBtn};
        for (int i = 0; i < toolBtns.length; i++)
            if (toolBtns[i] == btn) return toolEnums[i] == currentTool;
        return false;
    }

    @FXML private void onSelectTool()  { setActiveTool(Tool.SELECT); }
    @FXML private void onPanTool()     { setActiveTool(Tool.PAN); }
    @FXML private void onPenTool()     { setActiveTool(Tool.PEN); }
    @FXML private void onMarkerTool()  { setActiveTool(Tool.MARKER); }
    @FXML private void onEraserTool()  { setActiveTool(Tool.ERASER); }
    @FXML private void onRectTool()    { setActiveTool(Tool.RECT); }
    @FXML private void onCircleTool()  { setActiveTool(Tool.CIRCLE); }
    @FXML private void onLineTool()    { setActiveTool(Tool.LINE); }
    @FXML private void onArrowTool()   { setActiveTool(Tool.ARROW); }
    @FXML private void onTextTool()    { setActiveTool(Tool.TEXT); }
    @FXML private void onNoteTool()    { setActiveTool(Tool.NOTE); }

    @FXML private void onColorBlack()  { currentPenColor = Color.web("#1a1a1a"); clearColorSelections(); highlightColorBtn(colorBlack); }
    @FXML private void onColorRed()    { currentPenColor = Color.web("#e74c3c"); clearColorSelections(); highlightColorBtn(colorRed); }
    @FXML private void onColorOrange() { currentPenColor = Color.web("#f39c12"); clearColorSelections(); highlightColorBtn(colorOrange); }
    @FXML private void onColorGreen()  { currentPenColor = Color.web("#2ecc71"); clearColorSelections(); highlightColorBtn(colorGreen); }
    @FXML private void onColorBlue()   { currentPenColor = Color.web("#3498db"); clearColorSelections(); highlightColorBtn(colorBlue); }
    @FXML private void onColorPurple() { currentPenColor = Color.web("#9b59b6"); clearColorSelections(); highlightColorBtn(colorPurple); }
    @FXML private void onColorWhite()  { currentPenColor = Color.WHITE;          clearColorSelections(); highlightColorBtn(colorWhite); }
    @FXML private void onCustomColor() { if (customColorPicker != null) { currentPenColor = customColorPicker.getValue(); clearColorSelections(); } }

    @FXML private void onSizeChanged() {
        if (sizeSlider != null) currentPenSize = sizeSlider.getValue();
    }

    @FXML private void performUndo() {
        if (!undoStack.isEmpty()) {
            SnapshotParameters p = new SnapshotParameters();
            p.setFill(Color.TRANSPARENT);
            redoStack.push(canvas.snapshot(p, null));
            nodeRedoStack.push(new ArrayList<>(overlayPane.getChildren()));
            restoreSnapshot(undoStack.pop());
            if (!nodeUndoStack.isEmpty()) restoreNodes(nodeUndoStack.pop());
        }
    }

    @FXML private void performRedo() {
        if (!redoStack.isEmpty()) {
            SnapshotParameters p = new SnapshotParameters();
            p.setFill(Color.TRANSPARENT);
            undoStack.push(canvas.snapshot(p, null));
            nodeUndoStack.push(new ArrayList<>(overlayPane.getChildren()));
            restoreSnapshot(redoStack.pop());
            if (!nodeRedoStack.isEmpty()) restoreNodes(nodeRedoStack.pop());
        }
    }

    @FXML public void onClearAll() {
        saveToUndoStack();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        overlayPane.getChildren().clear();
    }

    @FXML private void onDownload() { exportCanvas("png"); }

    @FXML private void showMoreMenu() {
        ContextMenu menu = new ContextMenu();
        menu.getItems().addAll(
                createMenuItem("Export PNG",  () -> exportCanvas("png")),
                createMenuItem("Export SVG",  this::exportAsSVG),
                createMenuItem("Export ZIP",  this::exportAsZip),
                new SeparatorMenuItem(),
                createMenuItem("Effacer tout", this::onClearAll),
                createMenuItem("Annuler",      this::performUndo),
                createMenuItem("Rétablir",     this::performRedo)
        );
        menu.show(moreBtn, Side.BOTTOM, 0, 12);
    }

    @FXML private void onPublish() {
        if (onPublishAction != null) onPublishAction.run();
    }

    private void saveToUndoStack() {
        try {
            if (canvas.getWidth() > 0 && canvas.getHeight() > 0) {
                if (undoStack.size() >= MAX_STACK_SIZE) {
                    undoStack.remove(0);
                    if (!nodeUndoStack.isEmpty()) nodeUndoStack.remove(0);
                }
                SnapshotParameters p = new SnapshotParameters();
                p.setFill(Color.TRANSPARENT);
                undoStack.push(canvas.snapshot(p, null));
                nodeUndoStack.push(new ArrayList<>(overlayPane.getChildren()));
                redoStack.clear();
                nodeRedoStack.clear();
            }
        } catch (Exception ex) {
            System.err.println("saveToUndoStack: " + ex.getMessage());
        }
    }

    private void restoreSnapshot(WritableImage img) {
        gc.setGlobalAlpha(1.0);
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.drawImage(img, 0, 0);
    }

    private void restoreNodes(List<Node> nodes) {
        overlayPane.getChildren().setAll(nodes);
    }

    private void exportCanvas(String format) {
        WritableImage image = rootStackPane.snapshot(new SnapshotParameters(), null);
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("whiteboard." + format);
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(format.toUpperCase(), "*." + format));
        File file = fc.showSaveDialog(rootStackPane.getScene().getWindow());
        if (file != null) {
            try { whiteboardService.exportToPNG(image, file); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void exportAsSVG() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("whiteboard.svg");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("SVG", "*.svg"));
        File file = fc.showSaveDialog(rootStackPane.getScene().getWindow());
        if (file != null) {
            try { whiteboardService.exportToSVG(file); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void exportAsZip() {
        FileChooser fc = new FileChooser();
        fc.setInitialFileName("whiteboard_export.zip");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP", "*.zip"));
        File file = fc.showSaveDialog(rootStackPane.getScene().getWindow());
        if (file != null) {
            try { whiteboardService.exportToZIP(rootStackPane.snapshot(new SnapshotParameters(), null), file); } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void makeDraggable(Node node) {
        final double[] offset = new double[2];
        node.setOnMousePressed(e -> {
            if (currentTool == Tool.SELECT) {
                offset[0] = e.getSceneX() - node.getLayoutX();
                offset[1] = e.getSceneY() - node.getLayoutY();
                selectedNode = node;
            }
        });
        node.setOnMouseDragged(e -> {
            if (currentTool == Tool.SELECT) {
                node.setLayoutX(e.getSceneX() - offset[0]);
                node.setLayoutY(e.getSceneY() - offset[1]);
            }
        });
    }

    private MenuItem createMenuItem(String text, Runnable action) {
        MenuItem item = new MenuItem(text);
        item.setOnAction(e -> action.run());
        return item;
    }

    private String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int)(c.getRed() * 255),
                (int)(c.getGreen() * 255),
                (int)(c.getBlue() * 255));
    }

    public WritableImage getFullSnapshot() {
        return drawingArea.snapshot(new SnapshotParameters(), null);
    }

    public void clearCanvas() { onClearAll(); }
}