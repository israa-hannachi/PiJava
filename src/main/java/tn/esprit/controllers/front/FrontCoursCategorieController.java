package tn.esprit.controllers.front;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursCategorieController;
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.entities.cours.Cours_Categorie;
import tn.esprit.entities.users.Users;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class FrontCoursCategorieController implements Initializable {

    @FXML private FlowPane categoriesContainer;
    @FXML private TextField searchField;
    @FXML private Label totalCatLabel;
    @FXML private Label totalModLabel;
    @FXML private Label totalCoursLabel;
    @FXML private Label countLabel;
    @FXML private Label roleBadge;
    @FXML private Button profileButton;
    @FXML private VBox emptyState;

    private final CoursCategorieController catController = new CoursCategorieController();
    private final CoursModuleController modController = new CoursModuleController();
    private final CoursController coursController = new CoursController();

    private Users currentUser;
    private List<Cours_Categorie> allCategories = new ArrayList<>();
    private boolean sortAscending = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadData();
    }

    public void initUser(Users user) {
        this.currentUser = user;
        if (user != null) {
            profileButton.setText(user.getFirstName());
            // Show role badge for prof/enseignant
            String role = user.getRole();
            if ("Enseignant".equalsIgnoreCase(role)) {
                roleBadge.setText("👨‍🏫 " + user.getFirstName() + " — Enseignant");
                roleBadge.setStyle("-fx-background-color:#fef9c3; -fx-text-fill:#92400e; -fx-font-weight:700; -fx-background-radius:8; -fx-padding:4 12; -fx-font-size:12;");
                roleBadge.setVisible(true);
                roleBadge.setManaged(true);
            }
        }
    }

    private void loadData() {
        allCategories = catController.recupererCategories()
                .stream().filter(c -> c.getActif() == 1).collect(Collectors.toList());

        int totalMod = modController.recupererModules().size();
        int totalCours = coursController.recupererCours().size();

        totalCatLabel.setText(String.valueOf(allCategories.size()));
        totalModLabel.setText(String.valueOf(totalMod));
        totalCoursLabel.setText(String.valueOf(totalCours));

        renderCards(allCategories);
    }

    private void renderCards(List<Cours_Categorie> list) {
        categoriesContainer.getChildren().clear();
        boolean empty = list.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        countLabel.setText(list.size() + " catégorie(s)");

        String[] colors = {"#dbeafe", "#dcfce7", "#fef9c3", "#fce7f3", "#f3e8ff", "#ffedd5"};
        String[] textColors = {"#1d4ed8", "#166534", "#92400e", "#9d174d", "#6b21a8", "#9a3412"};
        String[] emojis = {"💻", "🌐", "🔬", "📐", "📊", "🎨", "📚", "⚙️", "🏗️", "🧠"};

        for (int i = 0; i < list.size(); i++) {
            Cours_Categorie cat = list.get(i);
            int colorIdx = i % colors.length;
            int emojiIdx = i % emojis.length;

            // Count modules for this category
            long modCount = modController.recupererModules().stream()
                    .filter(m -> m.getCategorieId() == cat.getId() && m.getActif() == 1).count();

            VBox card = new VBox(10);
            card.setPrefWidth(280);
            card.setStyle("-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
                    "-fx-border-color:#e5e7eb; -fx-border-radius:16; -fx-cursor:hand; " +
                    "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),8,0,0,2);");

            // Emoji badge
            Label badge = new Label(emojis[emojiIdx]);
            badge.setStyle("-fx-font-size:36px;");

            // Category tag
            Label tagLabel = new Label(cat.getNom());
            tagLabel.setStyle("-fx-background-color:" + colors[colorIdx] + "; -fx-text-fill:" + textColors[colorIdx] +
                    "; -fx-background-radius:8; -fx-padding:3 10; -fx-font-weight:700; -fx-font-size:12;");

            Label nomLabel = new Label(cat.getNom());
            nomLabel.setStyle("-fx-font-size:18px; -fx-font-weight:800; -fx-text-fill:#1f2937; -fx-wrap-text:true;");
            nomLabel.setWrapText(true);

            Label descLabel = new Label(cat.getDescription() != null && !cat.getDescription().isEmpty()
                    ? cat.getDescription() : "Catégorie de cours");
            descLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-wrap-text:true;");
            descLabel.setWrapText(true);

            HBox footer = new HBox(8);
            footer.setStyle("-fx-border-color:#f1f5f9; -fx-border-width:1 0 0 0; -fx-padding:8 0 0 0;");
            Label modCountLabel = new Label(modCount + " module" + (modCount > 1 ? "s" : ""));
            modCountLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
            Region space = new Region();
            HBox.setHgrow(space, Priority.ALWAYS);
            Label arrow = new Label("→");
            arrow.setStyle("-fx-text-fill:#0FB5A9; -fx-font-weight:800;");
            footer.getChildren().addAll(modCountLabel, space, arrow);

            card.getChildren().addAll(badge, tagLabel, nomLabel, descLabel, footer);

            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle(card.getStyle().replace(
                    "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),8,0,0,2)",
                    "-fx-effect:dropshadow(three-pass-box,rgba(15,181,169,0.25),14,0,0,4)") +
                    "; -fx-border-color:#0FB5A9;"));
            card.setOnMouseExited(e -> card.setStyle(
                    "-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
                            "-fx-border-color:#e5e7eb; -fx-border-radius:16; -fx-cursor:hand; " +
                            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),8,0,0,2);"));

            // Click → navigate to modules
            Cours_Categorie finalCat = cat;
            card.setOnMouseClicked(e -> navigateToModules(finalCat));

            categoriesContainer.getChildren().add(card);
        }
    }

    @FXML
    public void handleSearch() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        List<Cours_Categorie> filtered = allCategories.stream()
                .filter(c -> q.isEmpty()
                        || c.getNom().toLowerCase().contains(q)
                        || (c.getDescription() != null && c.getDescription().toLowerCase().contains(q)))
                .collect(Collectors.toList());
        renderCards(filtered);
    }

    @FXML
    public void sortAZ() {
        sortAscending = !sortAscending;
        Comparator<Cours_Categorie> cmp = Comparator.comparing(Cours_Categorie::getNom, String.CASE_INSENSITIVE_ORDER);
        allCategories.sort(sortAscending ? cmp : cmp.reversed());
        handleSearch();
    }

    @FXML
    public void sortZA() {
        allCategories.sort(Comparator.comparing(Cours_Categorie::getNom, String.CASE_INSENSITIVE_ORDER).reversed());
        handleSearch();
    }

    private void navigateToModules(Cours_Categorie cat) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_CoursModules.fxml"));
            Parent root = loader.load();
            FrontCoursModulesController ctrl = loader.getController();
            ctrl.initData(currentUser, cat);
            Stage stage = (Stage) categoriesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── NAVIGATION ──────────────────────────────────────────────────────────────

    @FXML public void handleHome() { navigateTo("/tn/esprit/view/front_user_dashboard.fxml"); }
    @FXML public void handleProfile() { navigateTo("/tn/esprit/view/front_profile.fxml"); }
    @FXML public void handleJeux() { navigateTo("/tn/esprit/view/front_GameList.fxml"); }
    @FXML public void handleEvents() { navigateTo("/tn/esprit/view/frontEvent.fxml"); }
    @FXML public void handleForums() { navigateTo("/tn/esprit/view/front_forum.fxml"); }
    @FXML public void handleLogout() { navigateTo("/tn/esprit/view/front_login.fxml"); }

    private void navigateTo(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            
            Object controller = loader.getController();
            if (controller instanceof FrontUserDashboardController) {
                ((FrontUserDashboardController) controller).initUser(currentUser);
            } else if (controller instanceof FrontProfileController) {
                ((FrontProfileController) controller).initUser(currentUser);
            } else if (controller instanceof FrontGameListController) {
                ((FrontGameListController) controller).initUser(currentUser);
            } else if (controller instanceof EventFrontController) {
                ((EventFrontController) controller).initUser(currentUser);
            } else if (controller instanceof FrontForumController) {
                ((FrontForumController) controller).initUser(currentUser);
            }

            Stage stage = (Stage) categoriesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
