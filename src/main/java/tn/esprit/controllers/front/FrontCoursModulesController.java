package tn.esprit.controllers.front;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.entities.cours.cours_categorie;
import tn.esprit.entities.cours.cours_module;
import tn.esprit.entities.users.Users;

import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class FrontCoursModulesController implements Initializable {

    @FXML private FlowPane modulesContainer;
    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterNiveau;
    @FXML private Label categorieNomLabel;
    @FXML private Label categorieDescLabel;
    @FXML private Label breadcrumbLabel;
    @FXML private Label totalModLabel;
    @FXML private Label debutantLabel;
    @FXML private Label interLabel;
    @FXML private Label avanceLabel;
    @FXML private Label countLabel;
    @FXML private Button profileButton;
    @FXML private VBox emptyState;

    private final CoursModuleController modController = new CoursModuleController();
    private final CoursController coursController = new CoursController();

    private Users currentUser;
    private cours_categorie currentCategorie;
    private List<cours_module> allModules = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        filterNiveau.setItems(FXCollections.observableArrayList("Tous", "Débutant", "Intermédiaire", "Avancé"));
        filterNiveau.getSelectionModel().selectFirst();
    }

    public void initData(Users user, cours_categorie cat) {
        this.currentUser = user;
        this.currentCategorie = cat;

        if (user != null) profileButton.setText(user.getFirstName());

        categorieNomLabel.setText("📦 " + cat.getNom());
        breadcrumbLabel.setText(cat.getNom());
        categorieDescLabel.setText(cat.getDescription() != null ? cat.getDescription() : "");

        allModules = modController.recupererModules().stream()
                .filter(m -> m.getCategorieId() == cat.getId() && m.getActif() == 1)
                .collect(Collectors.toList());

        updateStats(allModules);
        renderCards(allModules);
    }

    private void updateStats(List<cours_module> list) {
        totalModLabel.setText(String.valueOf(list.size()));
        debutantLabel.setText(String.valueOf(list.stream().filter(m -> "Débutant".equalsIgnoreCase(m.getNiveau())).count()));
        interLabel.setText(String.valueOf(list.stream().filter(m -> "Intermédiaire".equalsIgnoreCase(m.getNiveau())).count()));
        avanceLabel.setText(String.valueOf(list.stream().filter(m -> "Avancé".equalsIgnoreCase(m.getNiveau())).count()));
    }

    private void renderCards(List<cours_module> list) {
        modulesContainer.getChildren().clear();
        boolean empty = list.isEmpty();
        emptyState.setVisible(empty);
        emptyState.setManaged(empty);
        countLabel.setText(list.size() + " module(s)");

        // Color by niveau
        for (cours_module mod : list) {
            String niveau = mod.getNiveau() != null ? mod.getNiveau() : "—";
            String bgColor, textColor, niveauBg, niveauText;
            String emoji;

            switch (niveau.toLowerCase()) {
                case "débutant" -> { bgColor = "#f0fdf4"; textColor = "#166534"; niveauBg = "#dcfce7"; niveauText = "#166534"; emoji = "🟢"; }
                case "intermédiaire" -> { bgColor = "#eff6ff"; textColor = "#1d4ed8"; niveauBg = "#dbeafe"; niveauText = "#1d4ed8"; emoji = "🔵"; }
                case "avancé" -> { bgColor = "#fef2f2"; textColor = "#991b1b"; niveauBg = "#fee2e2"; niveauText = "#991b1b"; emoji = "🔴"; }
                default -> { bgColor = "#f8fafc"; textColor = "#334155"; niveauBg = "#e2e8f0"; niveauText = "#334155"; emoji = "⚪"; }
            }

            long coursCount = coursController.findByModuleId(mod.getId()).stream()
                    .filter(c -> c.getActif() == 1).count();

            VBox card = new VBox(10);
            card.setPrefWidth(300);
            card.setStyle("-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
                    "-fx-border-color:#e5e7eb; -fx-border-radius:16; -fx-cursor:hand; " +
                    "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),8,0,0,2);");

            // Top row
            HBox topRow = new HBox(8);
            topRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label niveauBadge = new Label(emoji + " " + niveau);
            niveauBadge.setStyle("-fx-background-color:" + niveauBg + "; -fx-text-fill:" + niveauText +
                    "; -fx-background-radius:8; -fx-padding:3 10; -fx-font-weight:700; -fx-font-size:11;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label dureeLabel = new Label("⏱ " + mod.getDuree() + "h");
            dureeLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
            topRow.getChildren().addAll(niveauBadge, spacer, dureeLabel);

            Label titreLabel = new Label(mod.getTitre());
            titreLabel.setStyle("-fx-font-size:18px; -fx-font-weight:800; -fx-text-fill:#1f2937; -fx-wrap-text:true;");
            titreLabel.setWrapText(true);

            Label descLabel = new Label(mod.getDescription() != null && !mod.getDescription().isEmpty()
                    ? mod.getDescription() : "Module de formation");
            descLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12; -fx-wrap-text:true;");
            descLabel.setWrapText(true);

            // Footer
            HBox footer = new HBox(8);
            footer.setStyle("-fx-border-color:#f1f5f9; -fx-border-width:1 0 0 0; -fx-padding:8 0 0 0;");
            footer.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            Label coursCountLabel = new Label(coursCount + " cours");
            coursCountLabel.setStyle("-fx-text-fill:#64748b; -fx-font-size:12;");
            Region spacer2 = new Region();
            HBox.setHgrow(spacer2, Priority.ALWAYS);
            Button voirBtn = new Button("Voir les cours →");
            voirBtn.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-font-weight:700; " +
                    "-fx-background-radius:8; -fx-padding:5 12; -fx-font-size:12; -fx-cursor:hand;");
            voirBtn.setOnAction(e -> navigateToCours(mod));
            footer.getChildren().addAll(coursCountLabel, spacer2, voirBtn);

            card.getChildren().addAll(topRow, titreLabel, descLabel, footer);

            card.setOnMouseEntered(e -> card.setStyle(
                    "-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
                            "-fx-border-color:#0FB5A9; -fx-border-radius:16; -fx-cursor:hand; " +
                            "-fx-effect:dropshadow(three-pass-box,rgba(15,181,169,0.25),14,0,0,4);"));
            card.setOnMouseExited(e -> card.setStyle(
                    "-fx-background-color:white; -fx-background-radius:16; -fx-padding:20; " +
                            "-fx-border-color:#e5e7eb; -fx-border-radius:16; -fx-cursor:hand; " +
                            "-fx-effect:dropshadow(three-pass-box,rgba(0,0,0,0.06),8,0,0,2);"));
            card.setOnMouseClicked(e -> navigateToCours(mod));

            modulesContainer.getChildren().add(card);
        }
    }

    @FXML
    public void handleSearch() { applyFilters(); }

    @FXML
    public void handleFilterNiveau() { applyFilters(); }

    private void applyFilters() {
        String q = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String niveau = filterNiveau.getValue();

        List<cours_module> filtered = allModules.stream().filter(m -> {
            boolean matchQ = q.isEmpty()
                    || m.getTitre().toLowerCase().contains(q)
                    || (m.getDescription() != null && m.getDescription().toLowerCase().contains(q));
            boolean matchNiveau = "Tous".equals(niveau) || niveau == null
                    || m.getNiveau().equalsIgnoreCase(niveau);
            return matchQ && matchNiveau;
        }).collect(Collectors.toList());

        renderCards(filtered);
    }

    @FXML public void sortAZ() {
        allModules.sort(Comparator.comparing(cours_module::getTitre, String.CASE_INSENSITIVE_ORDER));
        applyFilters();
    }
    @FXML public void sortByDuree() {
        allModules.sort(Comparator.comparingInt(cours_module::getDuree));
        applyFilters();
    }
    @FXML public void sortByNiveau() {
        List<String> order = List.of("Débutant", "Intermédiaire", "Avancé");
        allModules.sort(Comparator.comparingInt(m -> {
            int idx = order.indexOf(m.getNiveau());
            return idx == -1 ? 99 : idx;
        }));
        applyFilters();
    }

    private void navigateToCours(cours_module mod) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/front_CoursList.fxml"));
            Parent root = loader.load();
            FrontCoursListController ctrl = loader.getController();
            ctrl.initData(currentUser, currentCategorie, mod);
            Stage stage = (Stage) modulesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }

    // ─── NAVIGATION ──────────────────────────────────────────────────────────────

    @FXML public void handleCoursCategories() { navigateTo("/tn/esprit/view/front_CoursCategories.fxml"); }
    @FXML public void handleHome() { navigateTo("/tn/esprit/view/front_user_dashboard.fxml"); }
    @FXML public void handleJeux() { navigateTo("/tn/esprit/view/front_GameList.fxml"); }
    @FXML public void handleLogout() { navigateTo("/tn/esprit/view/front_login.fxml"); }
    @FXML public void handleProfile() { navigateTo("/tn/esprit/view/front_profile.fxml"); }

    private void navigateTo(String fxml) {
        try {
            if ("/tn/esprit/view/front_CoursCategories.fxml".equals(fxml)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();
                FrontCoursCategorieController ctrl = loader.getController();
                ctrl.initUser(currentUser);
                Stage stage = (Stage) modulesContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
                return;
            }
            if ("/tn/esprit/view/front_user_dashboard.fxml".equals(fxml) && currentUser != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();
                FrontUserDashboardController ctrl = loader.getController();
                ctrl.initUser(currentUser);
                Stage stage = (Stage) modulesContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
                return;
            }
            if ("/tn/esprit/view/front_profile.fxml".equals(fxml) && currentUser != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
                Parent root = loader.load();
                FrontProfileController ctrl = loader.getController();
                ctrl.initUser(currentUser);
                Stage stage = (Stage) modulesContainer.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
                return;
            }
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) modulesContainer.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
