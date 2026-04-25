package tn.esprit.controllers.front;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Side;
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
import javafx.stage.Stage;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;
import tn.esprit.entities.forum.Message;
import tn.esprit.entities.users.Users;
import tn.esprit.services.forum.ServiceMessage;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Circle;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Base64;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import javafx.scene.SnapshotParameters;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.Cursor;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.web.HTMLEditor;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.WritableImage;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Task;
import javafx.application.Platform;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Shape;
import javafx.geometry.Point2D;
import org.json.JSONArray;
import org.json.JSONObject;

public class FrontForumMessagesController implements Initializable {
    private WhiteboardController whiteboard;
    private Node whiteboardNode;

    @FXML private Label breadcrumbCategory;
    @FXML private Label breadcrumbForum;
    @FXML private HBox breadcrumbContainer;
    @FXML private VBox forumHeader;
    @FXML private VBox newMessageHeader;
    @FXML private Label forumIcon;
    @FXML private Label forumTitle;
    @FXML private Label forumDescription;
    @FXML private HTMLEditor messageTitleEditor;
    @FXML private HTMLEditor messageContentEditor;
    @FXML private VBox titleToolbarContainer;
    @FXML private VBox contentToolbarContainer;
    @FXML private Label messagesCount;
    @FXML private VBox messagesContainer;
    @FXML private VBox textModeContainer;
    @FXML private VBox sidebar;
    @FXML private ScrollPane mainScrollPane;
    @FXML private VBox contentVBox;
    @FXML private VBox mainContentCard;
    @FXML private VBox whiteboardModeContainer;
    @FXML private StackPane whiteboardStack;
    @FXML private ToggleButton textModeBtn;
    @FXML private ToggleButton whiteboardModeBtn;
    @FXML private VBox newMessageTitleContainer;
    @FXML private HBox modeSelectionContainer;
    @FXML private VBox messagesListContainer;
    @FXML private Button backToCategoryBtn;
    @FXML private HBox publishButtonContainer;

    private boolean isWhiteboardMode = false;
    private BorderPane rootBorderPane = null;
    private Users currentUser;
    private Forum currentForum;
    private Categorie currentCategory;
    private final ServiceMessage messageService = new ServiceMessage();

    // Couleurs pour les différentes catégories
    private final String[] categoryColors = {"#8b5cf6", "#22c55e", "#3b82f6", "#f59e0b", "#ef4444", "#06b6d4"};

    private final ToggleGroup modeGroup = new ToggleGroup();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        textModeBtn.setToggleGroup(modeGroup);
        whiteboardModeBtn.setToggleGroup(modeGroup);
        textModeBtn.setSelected(true);

        setupToolbars();
        hideDefaultToolbars(messageTitleEditor);
        hideDefaultToolbars(messageContentEditor);
    }

    private void hideDefaultToolbars(HTMLEditor editor) {
        Node topToolbar = editor.lookup(".top-toolbar");
        Node bottomToolbar = editor.lookup(".bottom-toolbar");
        if (topToolbar != null) { topToolbar.setVisible(false); topToolbar.setManaged(false); }
        if (bottomToolbar != null) { bottomToolbar.setVisible(false); bottomToolbar.setManaged(false); }
    }

    private void setupToolbars() {
        titleToolbarContainer.getChildren().add(createToolbar(messageTitleEditor));
        contentToolbarContainer.getChildren().add(createToolbar(messageContentEditor));
    }

    private HBox createToolbar(HTMLEditor target) {
        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(5));
        toolbar.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 8 8 0 0; -fx-border-color: #e2e8f0; -fx-border-width: 1 1 0 1; -fx-border-radius: 8 8 0 0;");

        // 1. Font Size (Word/WPS style "petite barre")
        ComboBox<String> fontSize = new ComboBox<>();
        fontSize.getItems().addAll("8", "9", "10", "11", "12", "14", "16", "18", "20", "22", "24", "26", "28", "36", "48", "72");
        fontSize.setValue("12"); // Default size
        fontSize.setPrefWidth(65);
        fontSize.setStyle("-fx-background-color: white; -fx-font-size: 11; -fx-border-color: #e2e8f0; -fx-border-radius: 6; -fx-background-radius: 6;");
        fontSize.setOnAction(e -> executeCommand(target, "fontSize", fontSize.getValue()));

        // 2. Font Style (Visual)
        Button boldBtn = createToolbarButton("B", "Gras", "-fx-font-weight: bold;");
        boldBtn.setOnAction(e -> executeCommand(target, "bold", null));

        Button italicBtn = createToolbarButton("I", "Italique", "-fx-font-style: italic;");
        italicBtn.setOnAction(e -> executeCommand(target, "italic", null));

        Button underlineBtn = createToolbarButton("U", "Souligné", "-fx-underline: true;");
        underlineBtn.setOnAction(e -> executeCommand(target, "underline", null));

        Button strikeBtn = createToolbarButton("S", "Barré", "-fx-strikethrough: true;");
        strikeBtn.setOnAction(e -> executeCommand(target, "strikeThrough", null));

        // 3. Advanced Lists
        MenuButton listBtn = new MenuButton("📋 Listes");
        listBtn.setStyle("-fx-background-color: white; -fx-font-size: 11;");

        MenuItem bullet = new MenuItem("• Liste à puces");
        bullet.setOnAction(e -> executeCommand(target, "insertUnorderedList", null));

        MenuItem circle = new MenuItem("◦ Liste (Cercle)");
        circle.setOnAction(e -> executeCommand(target, "insertHTML", "<ul style='list-style-type: circle;'><li></li></ul>"));

        MenuItem square = new MenuItem("▪ Liste (Carré)");
        square.setOnAction(e -> executeCommand(target, "insertHTML", "<ul style='list-style-type: square;'><li></li></ul>"));

        MenuItem numeric = new MenuItem("1. Liste numérotée");
        numeric.setOnAction(e -> executeCommand(target, "insertOrderedList", null));

        MenuItem alpha = new MenuItem("a. Liste alphabétique");
        alpha.setOnAction(e -> executeCommand(target, "insertHTML", "<ol style='list-style-type: lower-alpha;'><li></li></ol>"));

        MenuItem roman = new MenuItem("I. Liste Romaine");
        roman.setOnAction(e -> executeCommand(target, "insertHTML", "<ol style='list-style-type: upper-roman;'><li></li></ol>"));

        MenuItem checklist = new MenuItem("✓ Checklist");
        checklist.setOnAction(e -> executeCommand(target, "insertHTML", "<div style='display:flex;align-items:center;'><input type='checkbox' style='margin-right:5px;'> </div>"));

        listBtn.getItems().addAll(bullet, circle, square, new SeparatorMenuItem(), numeric, alpha, roman, new SeparatorMenuItem(), checklist);

        // 4. Full Emoji Picker
        Button emojiBtn = createToolbarButton("😊", "Emojis", "");
        emojiBtn.setOnAction(e -> showFullEmojiPicker(target, emojiBtn));

        // 5. Calculator (Emoji Icon)
        Button calcBtn = createToolbarButton("🧮", "Calculatrice", "");
        calcBtn.setOnAction(e -> showCalculator(target));


        // 6. Complete Math Symbols
        Button mathBtn = createToolbarButton("∑", "Math", "");
        mathBtn.setOnAction(e -> showAdvancedMath(target, mathBtn));

        toolbar.getChildren().addAll(fontSize, new Separator(Orientation.VERTICAL), boldBtn, italicBtn, underlineBtn, strikeBtn,
                new Separator(Orientation.VERTICAL), listBtn, emojiBtn, calcBtn, mathBtn);
        return toolbar;
    }

    private void executeCommand(HTMLEditor editor, String command, String value) {
        WebView webView = (WebView) editor.lookup("WebView");
        if (webView != null) {
            String script;
            if (value == null) {
                script = String.format("document.execCommand('%s', false, null)", command);
            } else if (command.equals("fontSize")) {
                // Special handling for numeric font sizes in pixels
                script = String.format(
                        "var s = window.getSelection();" +
                                "if (s.rangeCount > 0) {" +
                                "  var r = s.getRangeAt(0);" +
                                "  var span = document.createElement('span');" +
                                "  span.style.fontSize = '%spx';" +
                                "  r.surroundContents(span);" +
                                "}", value);
            } else {
                // Escape single quotes and backslashes for the JS string
                String escapedValue = value.replace("\\", "\\\\").replace("'", "\\'");
                script = String.format("document.execCommand('%s', false, '%s')", command, escapedValue);
            }
            webView.getEngine().executeScript(script);
        }
    }

    private void insertHTML(HTMLEditor editor, String html) {
        executeCommand(editor, "insertHTML", html);
    }

    private Button createToolbarButton(String text, String tooltip, String extraStyle) {
        Button btn = new Button(text);
        btn.setTooltip(new Tooltip(tooltip));
        btn.setStyle("-fx-background-color: white; -fx-cursor: hand; -fx-min-width: 35; -fx-min-height: 30; -fx-font-size: 12; -fx-background-radius: 6; -fx-border-color: #f1f5f9; -fx-border-radius: 6; " + extraStyle);
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #f1f5f9; -fx-cursor: hand; -fx-min-width: 35; -fx-min-height: 30; -fx-font-size: 12; -fx-background-radius: 6; -fx-border-color: #e2e8f0; -fx-border-radius: 6; " + extraStyle));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: white; -fx-cursor: hand; -fx-min-width: 35; -fx-min-height: 30; -fx-font-size: 12; -fx-background-radius: 6; -fx-border-color: #f1f5f9; -fx-border-radius: 6; " + extraStyle));
        return btn;
    }

    private void showFullEmojiPicker(HTMLEditor target, ButtonBase owner) {
        ContextMenu picker = new ContextMenu();
        VBox container = new VBox(0);
        container.setPrefSize(320, 400);
        container.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);");

        // 1. Search Bar (Top)
        HBox searchContainer = new HBox();
        searchContainer.setPadding(new Insets(10));
        TextField search = new TextField();
        search.setPromptText("Rechercher un emoji");
        search.setPrefHeight(35);
        search.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(search, Priority.ALWAYS);
        search.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 20; -fx-padding: 0 15; -fx-font-size: 13;");
        searchContainer.getChildren().add(search);

        // 2. Category Title
        Label categoryTitle = new Label("Smileys et personnes");
        categoryTitle.setPadding(new Insets(5, 15, 5, 15));
        categoryTitle.setStyle("-fx-font-size: 13; -fx-text-fill: #64748b; -fx-font-weight: bold;");

        // 3. Emoji FlowPane in ScrollPane
        FlowPane flow = new FlowPane(5, 5);
        flow.setPadding(new Insets(10, 15, 10, 15));
        flow.setPrefWrapLength(280);

        ScrollPane scroll = new ScrollPane(flow);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(260);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: white;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        // 4. Categories Data
        String[][] categories = {
                {"Smileys", "😀", "😁", "😂", "🤣", "😃", "😄", "😅", "😆", "😉", "😊", "😋", "😎", "😍", "😘", "🥰", "😗", "😙", "😚", "☺️", "🙂", "🤗", "🤩", "🤔", "🤨", "😐", "😑", "😶", "🙄", "😏", "😣", "😥", "😮", "🤐", "😯", "😪", "😫", "🥱", "😴", "😌", "😛", "😜", "😝", "🤤", "😒", "😓", "😔", "😕", "🙃", "🤑", "😲"},
                {"Personnes", "👋", "🤚", "🖐️", "✋", "🖖", "👌", "🤌", "🤏", "✌️", "🤞", "🤟", "🤙", "👈", "👉", "👆", "👇", "☝️", "👍", "👎", "👊", "🤛", "🤜", "👏", "🙌", "👐", "🤲", "🤝", "🙏", "✍️", "💅", "🤳", "💪", "👶", "🧒", "👦", "👧", "🧑", "👨", "👩", "👨‍⚕️", "👩‍⚕️", "👨‍🎓", "👩‍🎓", "👨‍🏫", "👩‍🏫", "👨‍💻", "👩‍💻", "👨‍🔬", "👩‍🔬", "👨‍🎨", "👩‍🎨", "👨‍🍳", "👩‍🍳", "👨‍🔧", "👩‍🔧", "👨‍💼", "👩‍💼", "👮", "🕵️", "💂", "👷"},
                {"Nourriture et boisson", "🍏", "🍎", "🍐", "🍊", "🍋", "🍌", "🍉", "🍇", "🍓", "🍈", "🍒", "🍑", "🥭", "🍍", "🥥", "🥝", "🍅", "🍆", "🥑", "🥦", "🥬", "🥒", "🌶️", "🌽", "🥕", "🧄", "🧅", "🍄", "🥜", "🍞", "🥐", "🥖", "🥨", "🥯", "🥞", "🧇", "🧀", "🍖", "🍗", "🥩", "🥓", "🍔", "🍟", "🍕", "🌭", "🌮", "🌯", "🍳", "🍲", "🍿", "🍱"},
                {"Activités et sports", "⚽", "🏀", "🏈", "⚾", "🥎", "🎾", "🏐", "🏉", "🎱", "🏓", "🏸", "🏒", "🏑", "🥍", "🏏", "🥅", "⛳", "🏹", "Fishing", "🥊", "🥋", "⛸️", "🎿", "🛷", "🎯", "🎮", "🕹️", "🎰", "🎲", "🧩", "🧸", "🎨", "🧵", "🧶", "🎹", "🥁", "🎸", "🎷", "🎺", "🎻", "🎬", "🎤", "🎧", "🎟️", "🎫", "🏆", "🥇", "🥈", "🥉", "🏅"},
                {"Voyages et lieux", "🚗", "🚕", "🚙", "🚌", "🏎️", "🚓", "🚑", "🚒", "🚐", "🚚", "🚛", "🚜", "🛵", "🏍️", "🚲", "🛴", "🚨", "🚔", "🚍", "🚘", "🚖", "🚃", "🚋", "🚄", "🚅", "🚆", "🚇", "🚈", "🚉", "✈️", "🛫", "🛬", "🛸", "🚀", "🛰️", "⛵", "🛶", "🚤", "🚢", "⚓", "🌋", "🏔️", "🏕️", "🏖️", "🏗️", "🏘️", "🏚️", "🏢", "🏣", "🏤"},
                {"Objets", "⌚", "📱", "📲", "💻", "⌨️", "🖱️", "🖨️", "🕹️", "💽", "💾", "💿", "📀", "📼", "📷", "📸", "📹", "🎥", "📽️", "🎞️", "📞", "☎️", "📟", "📠", "📺", "📻", "🎙️", "🎚️", "🎛️", "🧭", "⏱️", "⏲️", "⏰", "🕰️", "⌛", "⏳", "📡", "🔋", "🔌", "💡", "🔦", "🕯️", "🪔", "🧯", "🛢️", "💸", "💵", "💴", "💶", "💷", "💰"},
                {"Symboles et cœurs", "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❣️", "💕", "💞", "💓", "💗", "💖", "💘", "💝", "💟", "🆔", "⚛️", "🉑", "☢️", "☣️", "📴", "📳", "🈶", "🈚", "🈸", "🈺", "🈷️", "✴️", "🆚", "💮", "🉐", "㊙️", "㊗️", "🈴", "🈵", "🈹", "🈲", "🅰️", "🅱️", "AB", "🆑", "🅾️", "🆘", "❌", "⭕", "🛑", "⛔", "📛", "🚫", "💯", "💢", "♨️"},
                {"Éducation", "🎓", "📚", "📖", "✍️", "📝", "✏️", "✒️", "🖋️", "🖊️", "🖌️", "🖍️", "📏", "📐", "💼", "🏫", "🎒", "🏛️", "🧠", "🧪", "🧫", "🧬", "🔭", "🔬", "💻", "🖥️", "⌨️", "🖱️", "📅", "📊", "📈", "📉", "📜", "📁", "📂", "🗂️", "📋", "📌", "📍", "📎", "🖇️", "📐", "📏", "🧮", "💡", "🔔", "📣"}
        };

        // 5. Category Buttons (Bottom)
        HBox categoryBar = new HBox(8);
        categoryBar.setAlignment(Pos.CENTER);
        categoryBar.setPadding(new Insets(8, 0, 8, 0));
        categoryBar.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1 0 0 0; -fx-background-radius: 0 0 12 12;");

        final int[] currentIndex = {0};

        for (int i = 0; i < categories.length; i++) {
            final int index = i;
            Button catBtn = new Button(categories[i][1]);
            catBtn.setStyle("-fx-background-color: transparent; -fx-font-size: 18; -fx-cursor: hand; -fx-opacity: 0.5; -fx-padding: 5;");

            catBtn.setOnAction(e -> {
                currentIndex[0] = index;
                categoryTitle.setText(categories[index][0]);
                flow.getChildren().clear();
                for (int j = 1; j < categories[index].length; j++) {
                    String emoji = categories[index][j];
                    Button b = new Button(emoji);
                    b.setStyle("-fx-background-color: transparent; -fx-font-size: 24; -fx-cursor: hand; -fx-padding: 2;");
                    b.setOnAction(ev -> {
                        insertHTML(target, emoji);
                        picker.hide();
                    });
                    flow.getChildren().add(b);
                }
                // Update active state
                categoryBar.getChildren().forEach(n -> n.setOpacity(0.5));
                catBtn.setOpacity(1.0);
            });
            categoryBar.getChildren().add(catBtn);
        }

        // Search logic (Mock filter)
        search.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                ((Button) categoryBar.getChildren().get(currentIndex[0])).fire();
                return;
            }
            // For now, simple search is not possible without metadata, 
            // but we can at least keep the UI responsive.
        });

        // Initial load
        ((Button) categoryBar.getChildren().get(0)).fire();

        container.getChildren().addAll(searchContainer, categoryTitle, scroll, categoryBar);
        CustomMenuItem item = new CustomMenuItem(container);
        item.setHideOnClick(false);
        picker.getItems().add(item);
        picker.show(owner, Side.BOTTOM, 0, 0);
    }

    private void showAdvancedMath(HTMLEditor target, ButtonBase owner) {
        ContextMenu picker = new ContextMenu();
        TabPane tabs = new TabPane();
        tabs.setPrefSize(350, 250);

        String[][] categories = {
                {"Bases", "+", "−", "×", "÷", "=", "≠", "±", "∓", "∗", "∘", "∙"},
                {"Comparaison", "<", ">", "≤", "≥", "≈", "≅", "∝", "≪", "≫", "≡", "∼"},
                {"Calcul", "∑", "∫", "∬", "∭", "∮", "∂", "∇", "√", "∛", "∜", "∞", "lim", "Δ", "∇"},
                {"Grec", "α", "β", "γ", "δ", "ε", "ζ", "η", "θ", "ι", "κ", "λ", "μ", "ν", "ξ", "ο", "π", "ρ", "σ", "τ", "υ", "φ", "χ", "ψ", "ω"},
                {"Logique", "∀", "∃", "∄", "∈", "∉", "∋", "∌", "⊂", "⊃", "⊄", "⊅", "⊆", "⊇", "∪", "∩", "∧", "∨", "¬", "⇒", "⇔"}
        };

        for (String[] cat : categories) {
            Tab tab = new Tab(cat[0]);
            tab.setClosable(false);
            FlowPane flow = new FlowPane(5, 5);
            flow.setPadding(new Insets(10));
            for (int i = 1; i < cat.length; i++) {
                String sym = cat[i];
                Button b = new Button(sym);
                b.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-font-size: 14; -fx-cursor: hand; -fx-min-width: 35;");
                b.setOnAction(e -> {
                    insertHTML(target, sym);
                    picker.hide();
                });
                flow.getChildren().add(b);
            }
            tab.setContent(new ScrollPane(flow));
            tabs.getTabs().add(tab);
        }

        picker.getItems().add(new CustomMenuItem(tabs, false));
        picker.show(owner, Side.BOTTOM, 0, 0);
    }

    private void showCalculator(HTMLEditor target) {
        Stage calcStage = new Stage();
        calcStage.setTitle("Calculatrice");

        VBox root = new VBox(0);
        root.setStyle("-fx-background-color: #1f2937; -fx-padding: 0;");

        // --- Display Area ---
        VBox displayArea = new VBox(2);
        displayArea.setPadding(new Insets(15));
        displayArea.setStyle("-fx-background-color: #374151;");

        Label historyLabel = new Label("");
        historyLabel.setStyle("-fx-text-fill: #9ca3af; -fx-font-size: 12;");
        historyLabel.setMaxWidth(Double.MAX_VALUE);
        historyLabel.setAlignment(Pos.CENTER_RIGHT);

        TextField display = new TextField("0");
        display.setEditable(false);
        display.setAlignment(Pos.CENTER_RIGHT);
        display.setStyle("-fx-background-color: #d1fae5; -fx-text-fill: #064e3b; -fx-font-family: 'Courier New'; -fx-font-size: 24; -fx-font-weight: bold; -fx-background-radius: 4;");

        displayArea.getChildren().addAll(historyLabel, display);

        // --- Buttons Area ---
        GridPane buttons = new GridPane();
        buttons.setHgap(8);
        buttons.setVgap(8);
        buttons.setPadding(new Insets(15));
        buttons.setAlignment(Pos.CENTER);

        String[] labels = {
                "sin", "cos", "tan", "sqrt", "log",
                "7", "8", "9", "/", "AC",
                "4", "5", "6", "*", "DEL",
                "1", "2", "3", "-", "^",
                "0", ".", "pi", "+", "="
        };

        final StringBuilder currentExpression = new StringBuilder();
        final boolean[] isEvaluated = {false};

        int row = 0, col = 0;
        for (String label : labels) {
            Button b = new Button(label);
            b.setPrefSize(55, 45);

            if ("0123456789.".contains(label)) {
                b.setStyle("-fx-background-color: #f9fafb; -fx-text-fill: #111827; -fx-font-weight: bold; -fx-background-radius: 5;");
            } else if ("+-*/^=".contains(label)) {
                b.setStyle("-fx-background-color: #4b5563; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
                if ("=".equals(label)) b.setStyle("-fx-background-color: #0FB5A9; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            } else if ("ACDEL".contains(label)) {
                b.setStyle("-fx-background-color: #f97316; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 5;");
            } else {
                b.setStyle("-fx-background-color: #374151; -fx-text-fill: #e5e7eb; -fx-font-size: 11; -fx-background-radius: 5;");
            }

            b.setOnAction(e -> {
                if (isEvaluated[0] && "0123456789.sctlgp".contains(label.substring(0,1))) {
                    currentExpression.setLength(0);
                    isEvaluated[0] = false;
                }

                switch (label) {
                    case "AC":
                        currentExpression.setLength(0);
                        display.setText("0");
                        historyLabel.setText("");
                        break;
                    case "DEL":
                        if (currentExpression.length() > 0) {
                            currentExpression.setLength(currentExpression.length() - 1);
                            display.setText(currentExpression.length() == 0 ? "0" : currentExpression.toString());
                        }
                        break;
                    case "=":
                        try {
                            double result = evaluateExpression(currentExpression.toString());
                            historyLabel.setText(currentExpression.toString() + " =");
                            display.setText(String.valueOf(result));
                            currentExpression.setLength(0);
                            currentExpression.append(result);
                            isEvaluated[0] = true;
                        } catch (Exception ex) {
                            display.setText("Error");
                            currentExpression.setLength(0);
                        }
                        break;
                    case "sin": case "cos": case "tan": case "sqrt": case "log":
                        currentExpression.append(label).append("(");
                        display.setText(currentExpression.toString());
                        break;
                    case "pi":
                        currentExpression.append("Math.PI");
                        display.setText(currentExpression.toString());
                        break;
                    default:
                        currentExpression.append(label);
                        display.setText(currentExpression.toString());
                        break;
                }
            });

            buttons.add(b, col++, row);
            if (col > 4) { col = 0; row++; }
        }

        Button insertBtn = new Button("Insérer résultat");
        insertBtn.setMaxWidth(Double.MAX_VALUE);
        insertBtn.setPrefHeight(40);
        insertBtn.setStyle("-fx-background-color: #111827; -fx-text-fill: #0FB5A9; -fx-font-weight: bold; -fx-border-color: #0FB5A9; -fx-border-width: 1; -fx-background-radius: 0;");
        insertBtn.setOnAction(e -> {
            insertHTML(target, display.getText());
            calcStage.close();
        });

        root.getChildren().addAll(displayArea, buttons, insertBtn);
        calcStage.setScene(new Scene(root));
        calcStage.setResizable(false);
        calcStage.show();
    }

    private double evaluateExpression(String expression) {
        try {
            // Simplified evaluator for basic and single-function scientific ops
            if (expression.startsWith("sin(")) return Math.sin(Math.toRadians(Double.parseDouble(expression.substring(4, expression.length() - (expression.endsWith(")") ? 1 : 0)))));
            if (expression.startsWith("cos(")) return Math.cos(Math.toRadians(Double.parseDouble(expression.substring(4, expression.length() - (expression.endsWith(")") ? 1 : 0)))));
            if (expression.startsWith("tan(")) return Math.tan(Math.toRadians(Double.parseDouble(expression.substring(4, expression.length() - (expression.endsWith(")") ? 1 : 0)))));
            if (expression.startsWith("sqrt(")) return Math.sqrt(Double.parseDouble(expression.substring(5, expression.length() - (expression.endsWith(")") ? 1 : 0))));
            if (expression.startsWith("log(")) return Math.log10(Double.parseDouble(expression.substring(4, expression.length() - (expression.endsWith(")") ? 1 : 0))));

            if (expression.contains("+")) {
                String[] parts = expression.split("\\+");
                return Double.parseDouble(parts[0]) + Double.parseDouble(parts[1]);
            } else if (expression.contains("-")) {
                String[] parts = expression.split("-");
                return Double.parseDouble(parts[0]) - Double.parseDouble(parts[1]);
            } else if (expression.contains("*")) {
                String[] parts = expression.split("\\*");
                return Double.parseDouble(parts[0]) * Double.parseDouble(parts[1]);
            } else if (expression.contains("/")) {
                String[] parts = expression.split("/");
                return Double.parseDouble(parts[0]) / Double.parseDouble(parts[1]);
            } else if (expression.contains("^")) {
                String[] parts = expression.split("\\^");
                return Math.pow(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
            }
            return Double.parseDouble(expression.replace("Math.PI", String.valueOf(Math.PI)));
        } catch (Exception e) {
            return 0;
        }
    }

    public void initData(Users user, Categorie categorie, Forum forum) {
        this.currentUser = user;
        this.currentCategory = categorie;
        this.currentForum = forum;
        // Créer le bouton Publier ici pour s'assurer que le conteneur est chargé
        createPublishButton();
        loadForumData();
    }

    private void createPublishButton() {
        if (publishButtonContainer != null) {
            publishButtonContainer.getChildren().clear();
            publishButtonContainer.setStyle("-fx-padding: 10 0 0 0;");

            Button publishBtn = new Button("🚀 Publier");
            publishBtn.getStyleClass().add("btn-publish-premium");
            publishBtn.setOnAction(e -> handlePublishMessage());

            publishButtonContainer.getChildren().add(publishBtn);
            System.out.println("Bouton Publier créé et ajouté au conteneur");
        } else {
            System.err.println("ERREUR: publishButtonContainer est null!");
        }
    }

    private void loadForumData() {
        if (currentForum == null) return;

        // Mettre à jour le breadcrumb
        breadcrumbCategory.setText(currentCategory != null ? currentCategory.getTitre() : "Catégorie");
        breadcrumbForum.setText(currentForum.getTitre());

        // Mettre à jour le header
        forumIcon.setText(currentCategory != null && currentCategory.getIcone() != null ? currentCategory.getIcone() : "📁");
        forumTitle.setText(currentForum.getTitre());
        forumDescription.setText(currentForum.getDescription() != null ? currentForum.getDescription() : "");

        // Changer la couleur du header en fonction de la catégorie
        if (currentCategory != null) {
            int colorIndex = (currentCategory.getId() - 1) % categoryColors.length;
            String mainColor = categoryColors[colorIndex];
            String gradientEnd = colorIndex == 0 ? "#ec4899" :
                    colorIndex == 1 ? "#10b981" :
                            colorIndex == 2 ? "#60a5fa" :
                                    colorIndex == 3 ? "#fbbf24" :
                                            colorIndex == 4 ? "#f87171" :
                                                    "#22d3ee";
            forumHeader.setStyle("-fx-background-color: linear-gradient(to right, " + mainColor + ", " + gradientEnd + "); " +
                    "-fx-background-radius: 16; -fx-padding: 30; " +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 20, 0, 0, 8);");
            forumIcon.setStyle("-fx-font-size: 28; -fx-min-width: 60; -fx-min-height: 60; -fx-alignment: center; " +
                    "-fx-background-color: rgba(255,255,255,0.25); -fx-background-radius: 30; " +
                    "-fx-text-fill: white;");
        }

        // Charger les messages
        loadMessages();
    }

    private void loadMessages() {
        if (currentForum == null) return;

        messagesContainer.getChildren().clear();
        messagesCount.setText("(Chargement...)");

        new Thread(() -> {
            try {
                List<Message> messages = messageService.getMessagesByForum(currentForum.getId());
                javafx.application.Platform.runLater(() -> {
                    messagesCount.setText("(" + messages.size() + ")");
                    if (messages.isEmpty()) {
                        Label emptyLabel = new Label("Aucun message dans ce forum. Soyez le premier à poster !");
                        emptyLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #64748b; -fx-padding: 20;");
                        messagesContainer.getChildren().add(emptyLabel);
                    } else {
                        loadMessagesStaggered(messages, 0);
                    }
                });
            } catch (Exception ex) {
                javafx.application.Platform.runLater(() -> {
                    messagesCount.setText("(Erreur)");
                    System.err.println("Erreur chargement messages : " + ex.getMessage());
                });
            }
        }).start();
    }

    private void loadMessagesStaggered(List<Message> messages, int index) {
        if (index >= messages.size()) return;
        
        try {
            VBox messageCard = createMessageCard(messages.get(index));
            messagesContainer.getChildren().add(messageCard);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Délègue la création du message suivant au prochain cycle de l'UI Thread
        // Cela évite de geler l'interface lorsqu'il y a beaucoup de WebViews
        javafx.application.Platform.runLater(() -> {
            loadMessagesStaggered(messages, index + 1);
        });
    }

    private VBox createMessageCard(Message message) {
        VBox card = new VBox(10);
        card.getStyleClass().add("message-card-premium");

        // Header avec auteur et date
        HBox header = new HBox(10);
        header.setAlignment(Pos.CENTER_LEFT);

        Label authorLabel = new Label("👤 " + (message.getCreatedBy() != null ? message.getCreatedBy() : "Anonyme"));
        authorLabel.getStyleClass().add("author-badge");

        Label dateLabel = new Label("📅 " + (message.getDatePublication() != null ? message.getDatePublication().toString() : ""));
        dateLabel.getStyleClass().add("date-label");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Badge statut
        Label statusLabel = new Label("✨ " + (message.getEtat() != null ? message.getEtat() : "actif"));
        statusLabel.getStyleClass().add("status-badge-active");

        header.getChildren().addAll(authorLabel, dateLabel, spacer, statusLabel);
        card.getChildren().add(0, header);
        System.out.println("Affichage message de : " + message.getCreatedBy());

        // Contenu du message
        String contenu = message.getContenu();
        if (contenu != null) {
            String textPart = contenu;
            String wbPart = null;
            
            int wbIndex = contenu.indexOf("[[WB_ATTACHMENT]]");
            if (wbIndex != -1) {
                textPart = contenu.substring(0, wbIndex);
                wbPart = contenu.substring(wbIndex + "[[WB_ATTACHMENT]]".length());
            }
            
            // Afficher le texte s'il n'est pas vide ou just le squelette HTML par défaut
            if (!textPart.trim().isEmpty() && !textPart.contains("<body></body>") && !textPart.equals("<html dir=\"ltr\"><head></head><body contenteditable=\"true\"></body></html>")) {
                WebView contentWebView = new WebView();
                contentWebView.setMinHeight(50);
                contentWebView.setPrefHeight(100);
                contentWebView.setMaxHeight(600);
                
                // Wrap content in HTML structure with UTF-8 charset and explicit styling to fix visibility and accents
                String htmlContent = "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">" +
                                   "<style>body { background: transparent; font-family: 'Segoe UI', system-ui, sans-serif; font-size: 14px; color: #374151; margin:0; padding:0; }</style>" +
                                   "</head><body>" + textPart + "</body></html>";
                contentWebView.getEngine().loadContent(htmlContent);
                
                contentWebView.setStyle("-fx-background-color: transparent;");
                
                // Auto-resize
                contentWebView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == javafx.concurrent.Worker.State.SUCCEEDED) {
                        try {
                            Object result = contentWebView.getEngine().executeScript("document.documentElement.scrollHeight || document.body.scrollHeight");
                            if (result instanceof Number) {
                                double height = ((Number) result).doubleValue();
                                contentWebView.setMinHeight(height + 20);
                                contentWebView.setPrefHeight(height + 20);
                            }
                        } catch (Exception e) {}
                    }
                });
                
                VBox.setVgrow(contentWebView, Priority.ALWAYS);
                card.getChildren().add(contentWebView);
            }
            
            // Afficher la pièce jointe tableau blanc si présente
            if (wbPart != null && !wbPart.isEmpty()) {
                Node attachmentNode = createWhiteboardAttachmentUI(wbPart);
                card.getChildren().add(attachmentNode);
            }
        }

        // Actions (J'aime, Je n'aime pas, Éditer, Supprimer)
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_LEFT);
        actions.setStyle("-fx-padding: 15 0 0 0; -fx-border-color: #f1f5f9; -fx-border-width: 1 0 0 0;");

        // Bouton J'aime - vert
        int likeCount = countReactionsLocally(message.getLikesUsers());
        String userMarker = currentUser != null ? "|" + currentUser.getFirstName() + " " + currentUser.getLastName() + "|" : null;
        boolean hasLiked = userMarker != null && message.getLikesUsers() != null && message.getLikesUsers().contains(userMarker);
        
        Button likeBtn = new Button((hasLiked ? "❤️" : "♥") + " J'aime (" + likeCount + ")");
        likeBtn.getStyleClass().addAll("reaction-btn", "reaction-like");
        if (hasLiked) likeBtn.setStyle("-fx-background-color: #bbf7d0; -fx-text-fill: #166534; -fx-font-weight: bold; -fx-border-color: #166534; -fx-border-width: 1;");
        likeBtn.setOnAction(e -> handleLikeMessage(message));
        updateReactionTooltip(likeBtn, message.getId(), "LIKE");

        // Bouton Je n'aime pas - rouge
        int dislikeCount = countReactionsLocally(message.getDislikesUsers());
        boolean hasDisliked = userMarker != null && message.getDislikesUsers() != null && message.getDislikesUsers().contains(userMarker);
        
        Button dislikeBtn = new Button((hasDisliked ? "👎" : "💔") + " Je n'aime pas (" + dislikeCount + ")");
        dislikeBtn.getStyleClass().addAll("reaction-btn", "reaction-dislike");
        if (hasDisliked) dislikeBtn.setStyle("-fx-background-color: #fecaca; -fx-text-fill: #991b1b; -fx-font-weight: bold; -fx-border-color: #991b1b; -fx-border-width: 1;");
        dislikeBtn.setOnAction(e -> handleDislikeMessage(message));
        updateReactionTooltip(dislikeBtn, message.getId(), "DISLIKE");

        // Bouton Éditer - bleu
        Button editBtn = new Button("✏ Éditer");
        editBtn.getStyleClass().addAll("reaction-btn", "action-btn-edit");
        editBtn.setOnAction(e -> handleEditMessage(message));

        // Bouton Supprimer - jaune/orange
        Button deleteBtn = new Button("🗑 Supprimer");
        deleteBtn.getStyleClass().addAll("reaction-btn", "action-btn-delete");
        deleteBtn.setOnAction(e -> handleDeleteMessage(message));

        actions.getChildren().addAll(likeBtn, dislikeBtn, editBtn, deleteBtn);

        card.getChildren().add(actions);

        // Hover effect handle via CSS or minimal Java for dynamic scale if wanted
        card.setOnMouseEntered(e -> card.setCursor(Cursor.HAND));

        return card;
    }


    private String extractBodyHtml(String html) {
        if (html == null) return "";
        int bodyStart = html.indexOf("<body");
        if (bodyStart != -1) {
            bodyStart = html.indexOf(">", bodyStart) + 1;
            int bodyEnd = html.indexOf("</body>");
            if (bodyEnd != -1) {
                return html.substring(bodyStart, bodyEnd).trim();
            }
        }
        return html.trim();
    }

    @FXML
    public void handlePublishMessage() {
        System.out.println("Tentative de publication...");
        try {
            String content = "";
            
            String titleBody = extractBodyHtml(messageTitleEditor.getHtmlText());
            String contentBody = extractBodyHtml(messageContentEditor.getHtmlText());
            
            // Nettoyer les contenus vides
            if (titleBody.equals("<p><br></p>") || titleBody.equals("<br>")) titleBody = "";
            if (contentBody.equals("<p><br></p>") || contentBody.equals("<br>")) contentBody = "";

            String textContent = "";
            if (!titleBody.isEmpty() && !contentBody.isEmpty()) {
                textContent = "<div style='font-weight: bold; font-size: 1.1em; color: #0FB5A9; margin-bottom: 8px;'>" + titleBody + "</div>" + 
                              "<div>" + contentBody + "</div>";
            } else if (!titleBody.isEmpty()) {
                textContent = "<div>" + titleBody + "</div>";
            } else if (!contentBody.isEmpty()) {
                textContent = "<div>" + contentBody + "</div>";
            }

            if (isWhiteboardMode) {
                if (whiteboard == null) {
                    System.err.println("Whiteboard non initialisé");
                    return;
                }

                // Demander le format d'export
                List<String> choices = Arrays.asList("PNG (.png)", "SVG (.svg)", "ZIP (.zip)");
                ChoiceDialog<String> dialog = new ChoiceDialog<>("PNG (.png)", choices);
                dialog.setTitle("Format d'export");
                dialog.setHeaderText("Choisissez le format d'exportation pour votre tableau blanc");
                dialog.setContentText("Format:");

                Optional<String> result = dialog.showAndWait();
                if (!result.isPresent()) {
                    return; // Annulé par l'utilisateur
                }

                String format = "png";
                if (result.get().contains("SVG")) format = "svg";
                else if (result.get().contains("ZIP")) format = "zip";

                WritableImage image = whiteboard.getFullSnapshot();
                String base64Image = convertImageToBase64(image);
                
                // Combiner le texte et le tableau blanc
                content = textContent + "[[WB_ATTACHMENT]]" + format + "|" + base64Image;
            } else {
                content = textContent;
            }

            if (content.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Champ requis");
                alert.setHeaderText("Le message est vide");
                alert.setContentText("Veuillez saisir du texte ou dessiner sur le tableau.");
                alert.showAndWait();
                return;
            }

            if (currentForum == null) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur de contexte");
                alert.setHeaderText("Aucun forum sélectionné.");
                alert.showAndWait();
                return;
            }

            Message newMessage = new Message();
            newMessage.setContenu(content);
            newMessage.setCreatedBy(currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "Anonyme");
            newMessage.setForum(currentForum);
            newMessage.setEtat("actif");
            newMessage.setDatePublication(new java.sql.Date(System.currentTimeMillis()));

            System.out.println("Ajout du message au service...");
            messageService.ajouter(newMessage);
            System.out.println("Message ajouté avec succès!");

            // Feedback visuel
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Succès");
            success.setHeaderText("Message publié !");
            success.setContentText("Votre message a été ajouté au forum.");
            success.show();

            // Reset editors
            messageTitleEditor.setHtmlText("");
            messageContentEditor.setHtmlText("");
            if (isWhiteboardMode && whiteboard != null) whiteboard.clearCanvas();

            loadMessages();
        } catch (Exception ex) {
            ex.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erreur");
            alert.setHeaderText("Erreur lors de la publication");
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }

    // ─── NAVIGATION HANDLERS ───────────────────────────────────────────────────

    @FXML
    public void handleDashboard(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_user_dashboard.fxml", event);
    }

    @FXML
    public void handleProfile(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_profile.fxml", event);
    }

    @FXML
    public void handleCours(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_CoursCategories.fxml", event);
    }

    @FXML
    public void handleGameList(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_GameList.fxml", event);
    }

    @FXML
    public void handleEvents(ActionEvent event) {
        navigateTo("/tn/esprit/view/frontEvent.fxml", event);
    }

    @FXML
    public void handleMeets(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_MeetList.fxml", event);
    }

    @FXML
    public void handleBackToForums(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_forum.fxml", event);
    }

    @FXML
    public void handleBackToCategory(ActionEvent event) {
        // Retourner à la page de la catégorie
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_forum_category.fxml"));
            Parent root = loader.load();

            FrontForumCategoryController controller = loader.getController();
            controller.initData(currentUser, currentCategory);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void navigateTo(String fxmlPath, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Object controller = loader.getController();
            if (controller instanceof FrontForumController && currentUser != null) {
                ((FrontForumController) controller).initUser(currentUser);
            } else if (controller instanceof FrontUserDashboardController && currentUser != null) {
                ((FrontUserDashboardController) controller).initUser(currentUser);
            } else if (controller instanceof FrontProfileController && currentUser != null) {
                ((FrontProfileController) controller).initUser(currentUser);
            } else if (controller instanceof FrontCoursCategorieController && currentUser != null) {
                ((FrontCoursCategorieController) controller).initUser(currentUser);
            } else if (controller instanceof FrontGameListController && currentUser != null) {
                ((FrontGameListController) controller).initUser(currentUser);
            } else if (controller instanceof EventFrontController && currentUser != null) {
                ((EventFrontController) controller).initUser(currentUser);
            } else if (controller instanceof FrontMeetListController && currentUser != null) {
                ((FrontMeetListController) controller).initUser(currentUser);
            }

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            System.err.println("Erreur navigation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ─── MESSAGE ACTION HANDLERS ─────────────────────────────────────────────────

    private void updateReactionTooltip(ButtonBase btn, int messageId, String type) {
        List<String> users = messageService.getReactedUsers(messageId, type);
        if (!users.isEmpty()) {
            String names = String.join(", ", users);
            Tooltip tooltip = new Tooltip(names);
            tooltip.setStyle("-fx-font-size: 11; -fx-background-color: #334155; -fx-text-fill: white;");
            btn.setTooltip(tooltip);
        } else {
            btn.setTooltip(null);
        }
    }

    private void handleLikeMessage(Message message) {
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Connexion requise");
            alert.setHeaderText("Vous devez être connecté pour liker.");
            alert.showAndWait();
            return;
        }

        String userName = currentUser.getFirstName() + " " + currentUser.getLastName();
        messageService.addOrUpdateReaction(message.getId(), userName, "LIKE");
        loadMessages(); // Refresh to update counts and tooltips
    }

    private void handleDislikeMessage(Message message) {
        if (currentUser == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Connexion requise");
            alert.setHeaderText("Vous devez être connecté pour disliker.");
            alert.showAndWait();
            return;
        }

        String userName = currentUser.getFirstName() + " " + currentUser.getLastName();
        messageService.addOrUpdateReaction(message.getId(), userName, "DISLIKE");
        loadMessages(); // Refresh to update counts and tooltips
    }

    private void handleEditMessage(Message message) {
        // Vérifier si l'utilisateur est l'auteur du message ou un admin
        String currentUserName = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "";
        boolean isAuthor = message.getCreatedBy() != null && message.getCreatedBy().equals(currentUserName);
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole());

        if (!isAuthor && !isAdmin) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Permission refusée");
            alert.setHeaderText("Vous ne pouvez pas modifier ce message");
            alert.setContentText("Seul l'auteur du message ou un administrateur peut l'éditer.");
            alert.showAndWait();
            return;
        }

        // Créer une boîte de dialogue pour l'édition
        TextInputDialog dialog = new TextInputDialog(message.getContenu());
        dialog.setTitle("Éditer le message");
        dialog.setHeaderText("Modifier le message");
        dialog.setContentText("Nouveau contenu :");

        dialog.showAndWait().ifPresent(newContent -> {
            if (newContent.trim().isEmpty()) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Erreur");
                error.setHeaderText("Le message ne peut pas être vide");
                error.showAndWait();
                return;
            }
            message.setContenu(newContent);
            messageService.modifier(message);
            loadMessages(); // Recharger pour afficher les changements
        });
    }

    private void handleDeleteMessage(Message message) {
        // Vérifier si l'utilisateur est l'auteur du message ou un admin
        String currentUserName = currentUser != null ? currentUser.getFirstName() + " " + currentUser.getLastName() : "";
        boolean isAuthor = message.getCreatedBy() != null && message.getCreatedBy().equals(currentUserName);
        boolean isAdmin = currentUser != null && "ADMIN".equals(currentUser.getRole());

        if (!isAuthor && !isAdmin) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Permission refusée");
            alert.setHeaderText("Vous ne pouvez pas supprimer ce message");
            alert.setContentText("Seul l'auteur du message ou un administrateur peut le supprimer.");
            alert.showAndWait();
            return;
        }

        // Confirmation de suppression
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le message ?");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce message ? Cette action est irréversible.");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                messageService.supprimer(message.getId());
                loadMessages(); // Recharger pour mettre à jour l'affichage
            }
        });
    }
    @FXML
    private void showTextMode() {
        isWhiteboardMode = false;

        // Restore sidebar
        sidebar.setVisible(true);
        sidebar.setManaged(true);

        // Restore text mode containers
        textModeContainer.setVisible(true);
        textModeContainer.setManaged(true);
        whiteboardModeContainer.setVisible(false);
        whiteboardModeContainer.setManaged(false);
        textModeBtn.setSelected(true);

        // Restore the white card style
        mainContentCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 40; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 20, 0, 0, 10);");
        mainContentCard.setMaxWidth(1000);
        mainContentCard.setPadding(new Insets(40));

        // Restore elements
        forumHeader.setVisible(true);
        forumHeader.setManaged(true);
        breadcrumbContainer.setVisible(true);
        breadcrumbContainer.setManaged(true);
        messagesListContainer.setVisible(true);
        messagesListContainer.setManaged(true);
        backToCategoryBtn.setVisible(true);
        backToCategoryBtn.setManaged(true);
        publishButtonContainer.setVisible(true);
        publishButtonContainer.setManaged(true);
        
        contentVBox.setPadding(new Insets(30, 0, 30, 0));
    }

    @FXML
    private void showWhiteboardMode() {
        isWhiteboardMode = true;

        // Hide normal mode elements
        textModeContainer.setVisible(false);
        textModeContainer.setManaged(false);
        publishButtonContainer.setVisible(false);
        publishButtonContainer.setManaged(false);
        
        // Hide distractions to maximize space within the container
        messagesListContainer.setVisible(false);
        messagesListContainer.setManaged(false);
        forumHeader.setVisible(false);
        forumHeader.setManaged(false);
        breadcrumbContainer.setVisible(false);
        breadcrumbContainer.setManaged(false);

        // Expand the container to be much larger/wider
        mainContentCard.setMaxWidth(Double.MAX_VALUE);
        mainContentCard.setStyle("-fx-background-color: white; -fx-background-radius: 16; -fx-padding: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);");
        contentVBox.setPadding(new Insets(10, 20, 10, 20));

        // Show whiteboard container
        whiteboardModeContainer.setVisible(true);
        whiteboardModeContainer.setManaged(true);

        if (whiteboard == null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/whiteboard.fxml"));
                whiteboardNode = loader.load();
                whiteboard = loader.getController();
                whiteboard.setOnPublish(this::handlePublishMessage);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        // Add to stack
        if (!whiteboardStack.getChildren().contains(whiteboardNode)) {
            whiteboardStack.getChildren().clear();
            whiteboardStack.getChildren().add(whiteboardNode);
        }
        
        whiteboardModeBtn.setSelected(true);
    }

    private Node createWhiteboardAttachmentUI(String data) {
        String extension = "png";
        String base64 = data;
        
        if (data.startsWith("png|") || data.startsWith("svg|") || data.startsWith("zip|")) {
            extension = data.substring(0, 3);
            base64 = data.substring(4);
        }
        
        final String finalBase64 = base64;
        final String finalExt = extension;

        HBox attachment = new HBox(15);
        attachment.setAlignment(Pos.CENTER_LEFT);
        attachment.setPadding(new Insets(12, 20, 12, 20));
        attachment.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-cursor: hand;");

        // Icon (matching the photo)
        SVGPath fileIcon = new SVGPath();
        fileIcon.setContent("M14,2H6C4.9,2 4,2.9 4,4V20C4,21.1 4.9,22 6,22H18C19.1,22 20,21.1 20,20V8L14,2M13,9V3.5L18.5,9H13Z");
        fileIcon.setFill(Color.web("#64748b"));
        fileIcon.setScaleX(1.5);
        fileIcon.setScaleY(1.5);

        VBox textInfo = new VBox(2);
        Label fileName = new Label("whiteboard_export_" + System.currentTimeMillis() % 10000 + "." + extension);
        fileName.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-font-size: 14;");
        Label fileType = new Label("Whiteboard Capture • " + extension.toUpperCase() + " File");
        fileType.setStyle("-fx-text-fill: #64748b; -fx-font-size: 11;");
        textInfo.getChildren().addAll(fileName, fileType);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label moreIcon = new Label("•••");
        moreIcon.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 16; -fx-cursor: hand; -fx-padding: 0 5 0 5;");

        ContextMenu exportMenu = new ContextMenu();
        MenuItem downloadItem = new MenuItem("Télécharger");
        downloadItem.setOnAction(e -> downloadAttachmentAs(finalBase64, finalExt));
        exportMenu.getItems().add(downloadItem);
        
        moreIcon.setOnMouseClicked(e -> {
            e.consume(); // Prevent popup
            exportMenu.show(moreIcon, Side.BOTTOM, 0, 0);
        });

        attachment.getChildren().addAll(fileIcon, textInfo, spacer, moreIcon);

        attachment.setOnMouseEntered(e -> attachment.setStyle("-fx-background-color: #f1f5f9; -fx-background-radius: 12; -fx-border-color: #cbd5e1; -fx-border-radius: 12; -fx-cursor: hand;"));
        attachment.setOnMouseExited(e -> attachment.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 12; -fx-border-color: #e2e8f0; -fx-border-radius: 12; -fx-cursor: hand;"));

        attachment.setOnMouseClicked(e -> showWhiteboardImagePopup(finalBase64));

        return attachment;
    }

    private void downloadAttachmentAs(String base64, String format) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Enregistrer l'export");
        if (format.equals("png")) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image PNG", "*.png"));
        } else if (format.equals("svg")) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Fichier SVG", "*.svg"));
        } else if (format.equals("zip")) {
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archive ZIP", "*.zip"));
        }
        
        File file = fileChooser.showSaveDialog(messagesContainer.getScene().getWindow());
        if (file != null) {
            try {
                byte[] decodedBytes = Base64.getDecoder().decode(base64);
                if (format.equals("png")) {
                    Files.write(file.toPath(), decodedBytes);
                } else if (format.equals("svg")) {
                    String svgContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<svg xmlns=\"http://www.w3.org/2000/svg\" xmlns:xlink=\"http://www.w3.org/1999/xlink\">\n" +
                        "  <image href=\"data:image/png;base64," + base64 + "\" width=\"100%\" height=\"100%\" />\n" +
                        "</svg>";
                    Files.writeString(file.toPath(), svgContent);
                } else if (format.equals("zip")) {
                    try (FileOutputStream fos = new FileOutputStream(file);
                         ZipOutputStream zos = new ZipOutputStream(fos)) {
                        ZipEntry entry = new ZipEntry("export.png");
                        zos.putNextEntry(entry);
                        zos.write(decodedBytes);
                        zos.closeEntry();
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void showWhiteboardImagePopup(String base64) {
        byte[] imageBytes = Base64.getDecoder().decode(base64);
        Image image = new Image(new java.io.ByteArrayInputStream(imageBytes));

        Stage popup = new Stage();
        popup.setTitle("Whiteboard Viewer");

        ImageView imageView = new ImageView(image);
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(1000);

        ScrollPane scrollPane = new ScrollPane(new StackPane(imageView));
        scrollPane.setStyle("-fx-background-color: #0f172a; -fx-background: #0f172a;");
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Scene scene = new Scene(scrollPane, 1050, 750);
        popup.setScene(scene);
        popup.show();
    }

    private int countReactionsLocally(String rawReactions) {
        if (rawReactions == null || rawReactions.isEmpty()) return 0;
        return (int) Arrays.stream(rawReactions.split("\\|"))
                .filter(s -> !s.isEmpty())
                .count();
    }

    private String convertImageToBase64(WritableImage writableImage) {
        try {
            java.awt.image.BufferedImage bufferedImage = javafx.embed.swing.SwingFXUtils.fromFXImage(writableImage, null);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            javax.imageio.ImageIO.write(bufferedImage, "png", outputStream);
            return Base64.getEncoder().encodeToString(outputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}
