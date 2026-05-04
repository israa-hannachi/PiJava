// src/main/java/tn/esprit/controllers/Back/BackParticipantController.java
package tn.esprit.controllers.Back;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.controllers.meet.ParticipantController;
import tn.esprit.entities.meet.participant;
import tn.esprit.entities.users.Users;
import tn.esprit.services.meet.ParticipantService;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackParticipantController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterRoleCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private TableView<participant> participantTable;
    @FXML private TableColumn<participant, String> colNom;
    @FXML private TableColumn<participant, String> colPrenom;
    @FXML private TableColumn<participant, String> colEmail;
    @FXML private TableColumn<participant, String> colRole;
    @FXML private TableColumn<participant, String> colDate;
    @FXML private TableColumn<participant, String> colActions;
    @FXML private Label totalLabel;

    private final ParticipantController participantController = new ParticipantController();
    private final ParticipantService participantService = new ParticipantService();
    private ObservableList<participant> participantList = FXCollections.observableArrayList();
    private Users currentUser;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        setupCombos();
        loadAll();
    }

    public void initAdmin(Users user) { this.currentUser = user; }

    private void setupTable() {
        colNom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNom()));
        colPrenom.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPrenom()));
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colRole.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRole()));
        colDate.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getCreatedAt() != null ? c.getValue().getCreatedAt().toString().substring(0, 10) : "—"));

        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit   = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final HBox box = new HBox(5, btnEdit, btnDelete);
            {
                btnEdit.setStyle("-fx-background-color:#0FB5A9;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:4 8;");
                btnDelete.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:4 8;");
                btnEdit.setOnAction(e -> openForm(getTableView().getItems().get(getIndex()), null));
                btnDelete.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(String i, boolean empty) {
                super.updateItem(i, empty); setGraphic(empty ? null : box);
            }
        });
        participantTable.setItems(participantList);
    }

    private void setupCombos() {
        filterRoleCombo.setItems(FXCollections.observableArrayList("Tous", "enseignant", "etudiant"));
        filterRoleCombo.getSelectionModel().selectFirst();
        sortCombo.setItems(FXCollections.observableArrayList("Nom A→Z", "Nom Z→A"));
    }

    private void loadAll() {
        participantList.setAll(participantController.recupererParticipants());
        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText("Total : " + participantList.size() + " participant(s)");
    }

    @FXML public void handleSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) { loadAll(); return; }
        try {
            participantList.setAll(participantService.rechercherParNom(kw));
            updateTotal();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    @FXML public void handleFilter() {
        String sel = filterRoleCombo.getValue();
        if (sel == null || sel.equals("Tous")) { loadAll(); return; }
        try {
            participantList.setAll(participantService.filtrerParRole(sel));
            updateTotal();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    @FXML public void handleSort() {
        String sel = sortCombo.getValue();
        if (sel == null) return;
        try {
            participantList.setAll(participantService.trierParNom(sel.contains("A→Z")));
            updateTotal();
        } catch (Exception e) { showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage()); }
    }

    @FXML public void handleAdd(ActionEvent event) { openForm(null, event); }

    private void openForm(participant p, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_Participant_form.fxml"));
            Parent root = loader.load();
            BackParticipantFormController ctrl = loader.getController();
            ctrl.initForm(p, currentUser, this);
            Stage stage = new Stage();
            stage.setTitle(p == null ? "Ajouter un participant" : "Modifier le participant");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    private void handleDelete(participant p) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le participant « " + p.getNom() + " " + p.getPrenom() + " » ?");
        confirm.setContentText("Cela supprimera aussi ses participations aux réunions.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            participantController.supprimerParticipant(p.getId());
            loadAll();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Participant supprimé avec succès.");
        }
    }

    public void refreshTable() { loadAll(); }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type); a.setTitle(title); a.setHeaderText(null);
        a.setContentText(msg); a.showAndWait();
    }

    // ─── SIDEBAR ───────────────────────────────────────────────────────────────
    @FXML public void handleDashboard(ActionEvent e)   { navigate("/tn/esprit/view/back_admin.fxml", e); }
    @FXML public void handleListeComptes(ActionEvent e){ navigate("/tn/esprit/view/user_index.fxml", e); }
    @FXML public void handleCategories(ActionEvent e)  { navigate("/tn/esprit/view/back_CoursCategorieList.fxml", e); }
    @FXML public void handleModules(ActionEvent e)     { navigate("/tn/esprit/view/back_CoursModuleList.fxml", e); }
    @FXML public void handleCours(ActionEvent e)       { navigate("/tn/esprit/view/back_CoursList.fxml", e); }
    @FXML public void handleEventsList(ActionEvent e)  { navigate("/tn/esprit/view/back_Event.fxml", e); }
    @FXML public void handleMeetList(ActionEvent e)    { navigate("/tn/esprit/view/back_MeetList.fxml", e); }
    @FXML public void handleMeetDashboard(ActionEvent e) { navigate("/tn/esprit/view/back_MeetDashboard.fxml", e); }
    @FXML public void handleJeux(ActionEvent e)        { navigate("/tn/esprit/view/back_GameList.fxml", e); }
    @FXML public void handleForums(ActionEvent e)      { navigate("/tn/esprit/view/back_forums_dashboard.fxml", e); }
    @FXML public void handleLogout(ActionEvent e)      { navigate("/tn/esprit/view/front_login.fxml", e); }

    private void navigate(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage)((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root)); stage.show();
        } catch (IOException ex) { showAlert(Alert.AlertType.ERROR, "Navigation", ex.getMessage()); }
    }
}
