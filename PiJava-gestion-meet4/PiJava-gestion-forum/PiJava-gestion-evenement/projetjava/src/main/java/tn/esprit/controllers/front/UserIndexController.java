package tn.esprit.controllers.front;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import tn.esprit.controllers.users.UsersController;
import tn.esprit.entities.users.Users;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;


public class UserIndexController {

    // ── Table ──────────────────────────────────────────────────────────────────
    @FXML private TableView<Users>        usersTable;
    @FXML private TableColumn<Users, String> idColumn;
    @FXML private TableColumn<Users, String> nomColumn;
    @FXML private TableColumn<Users, String> prenomColumn;
    @FXML private TableColumn<Users, String> emailColumn;
    @FXML private TableColumn<Users, String> roleColumn;
    @FXML private TableColumn<Users, String> statutColumn;
    @FXML private TableColumn<Users, String> dateCreationColumn;
    @FXML private TableColumn<Users, Void>   actionsColumn;

    // ── Stats labels ───────────────────────────────────────────────────────────
    @FXML private Label totalLabel;
    @FXML private Label adminCountLabel;
    @FXML private Label studentCountLabel;
    @FXML private Label teacherCountLabel;
    @FXML private TextField searchField;

    private Users currentAdmin;
    private ObservableList<Users> allUsers = FXCollections.observableArrayList();

    // ── Called by AdminDashboardController after loading ───────────────────────
    public void initAdmin(Users admin) {
        this.currentAdmin = admin;
        loadUsers();
    }

    private void loadUsers() {
        UsersController uc = new UsersController();
        List<Users> users = uc.recupererUsers();
        allUsers.setAll(users);
        usersTable.setItems(allUsers);
        refreshStats(users);
    }

    private void refreshStats(List<Users> users) {
        totalLabel.setText(String.valueOf(users.size()));
        adminCountLabel.setText(String.valueOf(
                users.stream().filter(u -> "ADMIN".equals(u.getRole())).count()));
        studentCountLabel.setText(String.valueOf(
                users.stream().filter(u -> "etudiant".equalsIgnoreCase(u.getRole())).count()));
        teacherCountLabel.setText(String.valueOf(
                users.stream().filter(u -> "enseignant".equalsIgnoreCase(u.getRole())).count()));
    }

    @FXML
    public void initialize() {
        // ── Column bindings ────────────────────────────────────────────────────
        idColumn.setCellValueFactory(d ->
                new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        nomColumn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getLastName()));
        prenomColumn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getFirstName()));
        emailColumn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getEmail()));
        roleColumn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getRole()));
        statutColumn.setCellValueFactory(d ->
                new SimpleStringProperty(d.getValue().getStatut()));
        dateCreationColumn.setCellValueFactory(d -> {
            var ts = d.getValue().getDateCreation();
            return new SimpleStringProperty(ts != null ? ts.toLocalDateTime()
                    .toLocalDate().toString() : "—");
        });

        // ── Role badge colouring ───────────────────────────────────────────────
        roleColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String role, boolean empty) {
                super.updateItem(role, empty);
                if (empty || role == null) { setText(null); setStyle(""); return; }
                setText(role.toUpperCase());
                String color = switch (role.toUpperCase()) {
                    case "ADMIN"      -> "-fx-text-fill:#dc2626; -fx-font-weight:bold;";
                    case "ENSEIGNANT" -> "-fx-text-fill:#0FB5A9; -fx-font-weight:bold;";
                    default           -> "-fx-text-fill:#2563eb; -fx-font-weight:bold;";
                };
                setStyle(color);
            }
        });

        // ── Statut badge colouring ─────────────────────────────────────────────
        statutColumn.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String statut, boolean empty) {
                super.updateItem(statut, empty);
                if (empty || statut == null) { setText(null); setStyle(""); return; }
                setText(statut);
                boolean active = "ACTIF".equalsIgnoreCase(statut);
                setStyle(active
                        ? "-fx-text-fill:#16a34a; -fx-font-weight:bold;"
                        : "-fx-text-fill:#dc2626; -fx-font-weight:bold;");
            }
        });

        // ── Actions column: Inspect + Modify buttons ───────────────────────────
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button inspectBtn  = new Button("🔍 Voir");
            private final Button modifyBtn   = new Button("✏ Modifier");
            private final Button deleteBtn   = new Button("🗑 Supprimer");
            private final HBox   box         = new HBox(6, inspectBtn, modifyBtn, deleteBtn);

            {
                inspectBtn.setStyle(
                        "-fx-background-color:#e0f2fe; -fx-text-fill:#0284c7;" +
                        "-fx-background-radius:6; -fx-font-size:11; -fx-padding:4 8;");
                modifyBtn.setStyle(
                        "-fx-background-color:#dcfce7; -fx-text-fill:#16a34a;" +
                        "-fx-background-radius:6; -fx-font-size:11; -fx-padding:4 8;");
                deleteBtn.setStyle(
                        "-fx-background-color:#fee2e2; -fx-text-fill:#dc2626;" +
                        "-fx-background-radius:6; -fx-font-size:11; -fx-padding:4 8;");

                inspectBtn.setOnAction(e -> {
                    Users u = getTableView().getItems().get(getIndex());
                    showInspectDialog(u);
                });
                modifyBtn.setOnAction(e -> {
                    Users u = getTableView().getItems().get(getIndex());
                    showModifyDialog(u);
                });
                deleteBtn.setOnAction(e -> {
                    Users u = getTableView().getItems().get(getIndex());
                    showDeleteConfirm(u);
                });
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    // ── Search ─────────────────────────────────────────────────────────────────
    @FXML
    public void handleSearch(ActionEvent event) {
        String q = searchField.getText().trim().toLowerCase();
        if (q.isEmpty()) {
            usersTable.setItems(allUsers);
            return;
        }
        List<Users> filtered = allUsers.stream().filter(u ->
                u.getFirstName().toLowerCase().contains(q) ||
                u.getLastName().toLowerCase().contains(q)  ||
                u.getEmail().toLowerCase().contains(q)     ||
                (u.getRole() != null && u.getRole().toLowerCase().contains(q))
        ).collect(Collectors.toList());
        usersTable.setItems(FXCollections.observableArrayList(filtered));
    }

    // ── Inspect Dialog ─────────────────────────────────────────────────────────
    private void showInspectDialog(Users u) {
        String info = String.format(
                "ID          : %d%n" +
                "Nom         : %s%n" +
                "Prénom      : %s%n" +
                "Email       : %s%n" +
                "Rôle        : %s%n" +
                "Statut      : %s%n" +
                "Profession  : %s%n" +
                "Exp. Level  : %s%n" +
                "Créé le     : %s",
                u.getId(), u.getLastName(), u.getFirstName(),
                u.getEmail(), u.getRole(), u.getStatut(),
                u.getProfession(), u.getExperienceLevel(),
                u.getDateCreation() != null ? u.getDateCreation().toLocalDateTime().toLocalDate() : "—"
        );
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails Utilisateur");
        alert.setHeaderText("👤 " + u.getFirstName() + " " + u.getLastName());
        alert.setContentText(info);
        alert.getDialogPane().setPrefWidth(420);
        alert.showAndWait();
    }

    // ── Delete Confirm Dialog ──────────────────────────────────────────────────
    private void showDeleteConfirm(Users u) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmer la Suppression");
        confirm.setHeaderText("⚠️ Supprimer l'utilisateur ?");
        confirm.setContentText(String.format(
                "Vous allez supprimer définitivement :%n%n" +
                "  👤 %s %s%n" +
                "  ✉  %s%n%n" +
                "Cette action est irréversible !",
                u.getFirstName(), u.getLastName(), u.getEmail()));

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                UsersController uc = new UsersController();
                uc.supprimerUser(u.getId());
                loadUsers(); // Refresh table + stats
                showAlert(Alert.AlertType.INFORMATION, "Supprimé",
                        "✅ L'utilisateur a été supprimé avec succès.");
            }
        });
    }

    // ── Modify Dialog ──────────────────────────────────────────────────────────
    private void showModifyDialog(Users u) {
        Dialog<Users> dialog = new Dialog<>();
        dialog.setTitle("Modifier l'Utilisateur");
        dialog.setHeaderText("✏ Modifier : " + u.getFirstName() + " " + u.getLastName());

        ButtonType saveType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveType, ButtonType.CANCEL);

        // Form fields
        TextField firstField  = new TextField(u.getFirstName());
        TextField lastField   = new TextField(u.getLastName());
        TextField emailField  = new TextField(u.getEmail());
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("ADMIN", "etudiant", "enseignant");
        roleBox.setValue(u.getRole());
        ComboBox<String> statutBox = new ComboBox<>();
        statutBox.getItems().addAll("ACTIF", "INACTIF");
        statutBox.setValue(u.getStatut());

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(12); grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(16));
        grid.add(new Label("Prénom :"),  0, 0); grid.add(firstField,  1, 0);
        grid.add(new Label("Nom :"),     0, 1); grid.add(lastField,   1, 1);
        grid.add(new Label("Email :"),   0, 2); grid.add(emailField,  1, 2);
        grid.add(new Label("Rôle :"),    0, 3); grid.add(roleBox,     1, 3);
        grid.add(new Label("Statut :"),  0, 4); grid.add(statutBox,   1, 4);
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(btn -> {
            if (btn == saveType) {
                u.setFirstName(firstField.getText().trim());
                u.setLastName(lastField.getText().trim());
                u.setEmail(emailField.getText().trim());
                u.setRole(roleBox.getValue());
                u.setStatut(statutBox.getValue());
                return u;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(updated -> {
            UsersController uc = new UsersController();
            String error = uc.modifierUser(updated);
            if (error == null) {
                loadUsers();  // Refresh table
                showAlert(Alert.AlertType.INFORMATION, "Succès",
                        "✅ L'utilisateur a été mis à jour avec succès !");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erreur", error);
            }
        });
    }

    // ── Navigation ─────────────────────────────────────────────────────────────
    @FXML
    public void handleBackAdmin(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/tn/esprit/view/back_admin.fxml"));
            javafx.scene.Parent root = loader.load();
            AdminDashboardController ctrl = loader.getController();
            ctrl.initAdmin(currentAdmin);
            showScene(root, event);
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/tn/esprit/view/front_login.fxml"));
            showScene(root, event);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void showScene(javafx.scene.Parent root, ActionEvent event) {
        javafx.scene.Scene scene = new javafx.scene.Scene(root);
        javafx.stage.Stage stage = (javafx.stage.Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.show();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title); a.setHeaderText(null); a.setContentText(msg);
        a.showAndWait();
    }
    @FXML
    public void handleGameList(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/tn/esprit/view/front_GameList.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void handleCours(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/tn/esprit/view/front_CoursCategories.fxml"));
            Parent root = loader.load();
            FrontCoursCategorieController ctrl = loader.getController();
            ctrl.initUser(currentAdmin);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
