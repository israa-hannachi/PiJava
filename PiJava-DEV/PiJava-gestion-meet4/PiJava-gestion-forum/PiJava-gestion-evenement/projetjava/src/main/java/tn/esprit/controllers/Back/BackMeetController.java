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
import tn.esprit.controllers.meet.MeetController;
import tn.esprit.controllers.meet.MeetParticipantsController;
import tn.esprit.controllers.meet.ParticipantController;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.participant;
import tn.esprit.entities.users.Users;
import tn.esprit.services.meet.MeetService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackMeetController implements Initializable {

    @FXML private TextField searchField;
    @FXML private ComboBox<String> sortCombo;
    @FXML private ComboBox<String> filterOrganisateur;
    @FXML private TableView<Meet> meetTable;
    @FXML private TableColumn<Meet, Integer> colId;
    @FXML private TableColumn<Meet, String> colTitre;
    @FXML private TableColumn<Meet, String> colDescription;
    @FXML private TableColumn<Meet, String> colDateDebut;
    @FXML private TableColumn<Meet, String> colDateFin;
    @FXML private TableColumn<Meet, String> colLien;
    @FXML private TableColumn<Meet, String> colOrganisateur;
    @FXML private TableColumn<Meet, String> colActions;
    @FXML private Label totalLabel;

    private final MeetController meetController = new MeetController();
    private final ParticipantController participantController = new ParticipantController();
    private final MeetParticipantsController mpController = new MeetParticipantsController();
    private final MeetService meetService = new MeetService();
    private ObservableList<Meet> meetList = FXCollections.observableArrayList();
    private Users currentUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setupTable();
        setupSortCombo();
        setupFilterOrganisateur();
        loadAllMeets();
    }

    public void initAdmin(Users user) {
        this.currentUser = user;
    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getId()).asObject());
        colTitre.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTitre()));
        colDescription.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDescription() != null ? c.getValue().getDescription() : "—"));
        colDateDebut.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDateDebut() != null ? c.getValue().getDateDebut().toString().substring(0, 16) : "—"));
        colDateFin.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getDateFin() != null ? c.getValue().getDateFin().toString().substring(0, 16) : "—"));
        colLien.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getLienMeet() != null ? c.getValue().getLienMeet() : "—"));
        colOrganisateur.setCellValueFactory(c -> {
            participant p = participantController.findById(c.getValue().getParticipantId());
            return new SimpleStringProperty(p != null ? p.getNom() + " " + p.getPrenom() : "ID:" + c.getValue().getParticipantId());
        });

        // Actions column
        colActions.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("✏️");
            private final Button btnDelete = new Button("🗑️");
            private final Button btnPart = new Button("👥");
            private final HBox box = new HBox(5, btnEdit, btnPart, btnDelete);

            {
                btnEdit.setStyle("-fx-background-color:#0FB5A9;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:4 8;");
                btnDelete.setStyle("-fx-background-color:#ef4444;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:4 8;");
                btnPart.setStyle("-fx-background-color:#6366f1;-fx-text-fill:white;-fx-background-radius:5;-fx-padding:4 8;");

                btnEdit.setOnAction(e -> {
                    Meet m = getTableView().getItems().get(getIndex());
                    openMeetForm(m, null);
                });
                btnDelete.setOnAction(e -> {
                    Meet m = getTableView().getItems().get(getIndex());
                    handleDelete(m);
                });
                btnPart.setOnAction(e -> {
                    Meet m = getTableView().getItems().get(getIndex());
                    showParticipantsMeet(m);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        meetTable.setItems(meetList);
    }

    private void setupSortCombo() {
        sortCombo.setItems(FXCollections.observableArrayList(
            "Date début ↑", "Date début ↓", "Titre A→Z", "Titre Z→A"
        ));
    }

    private void setupFilterOrganisateur() {
        filterOrganisateur.getItems().add("Tous");
        List<participant> parts = participantController.recupererParticipants();
        for (participant p : parts) {
            filterOrganisateur.getItems().add(p.getId() + " - " + p.getNom() + " " + p.getPrenom());
        }
        filterOrganisateur.getSelectionModel().selectFirst();
    }

    private void loadAllMeets() {
        meetList.setAll(meetController.recupererMeets());
        updateTotal();
    }

    private void updateTotal() {
        totalLabel.setText("Total : " + meetList.size() + " réunion(s)");
    }

    @FXML
    public void handleSearch() {
        String kw = searchField.getText().trim();
        if (kw.isEmpty()) {
            loadAllMeets();
        } else {
            try {
                meetList.setAll(meetService.rechercherParTitre(kw));
                updateTotal();
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Erreur lors de la recherche : " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleSort() {
        String sel = sortCombo.getValue();
        if (sel == null) return;
        try {
            switch (sel) {
                case "Date début ↑" -> meetList.setAll(meetService.trierParDateDebut(true));
                case "Date début ↓" -> meetList.setAll(meetService.trierParDateDebut(false));
                case "Titre A→Z"    -> meetList.setAll(meetService.trierParTitre(true));
                case "Titre Z→A"    -> meetList.setAll(meetService.trierParTitre(false));
            }
            updateTotal();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void handleFilter() {
        String sel = filterOrganisateur.getValue();
        if (sel == null || sel.equals("Tous")) {
            loadAllMeets();
            return;
        }
        try {
            int id = Integer.parseInt(sel.split(" - ")[0]);
            meetList.setAll(meetService.filtrerParOrganisateur(id));
            updateTotal();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", e.getMessage());
        }
    }

    @FXML
    public void handleAdd(ActionEvent event) {
        openMeetForm(null, event);
    }

    private void openMeetForm(Meet m, ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/tn/esprit/view/back_Meet_form.fxml"));
            Parent root = loader.load();
            BackMeetFormController ctrl = loader.getController();
            ctrl.initForm(m, currentUser, this);
            Stage stage = new Stage();
            stage.setTitle(m == null ? "Ajouter une réunion" : "Modifier la réunion");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Impossible d'ouvrir le formulaire : " + e.getMessage());
        }
    }

    private void handleDelete(Meet m) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer la réunion « " + m.getTitre() + " » ?");
        confirm.setContentText("Cette action supprimera aussi les participants liés.");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            meetController.supprimerMeet(m.getId());
            loadAllMeets();
            showAlert(Alert.AlertType.INFORMATION, "Succès", "Réunion supprimée avec succès.");
        }
    }

    private void showParticipantsMeet(Meet m) {
        List<tn.esprit.entities.meet.Meet_Participants> mp = mpController.getParticipantsDuMeet(m.getId());
        StringBuilder sb = new StringBuilder("Participants de « " + m.getTitre() + " » :\n\n");
        if (mp.isEmpty()) {
            sb.append("Aucun participant inscrit.");
        } else {
            for (tn.esprit.entities.meet.Meet_Participants p : mp) {
                participant part = participantController.findById(p.getParticipantId());
                sb.append("• ").append(part != null ? part.getNom() + " " + part.getPrenom() + " (" + part.getEmail() + ")" : "ID:" + p.getParticipantId()).append("\n");
            }
        }
        showAlert(Alert.AlertType.INFORMATION, "Participants", sb.toString());
    }

    public void refreshTable() {
        loadAllMeets();
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    // ─── SIDEBAR NAVIGATION ───────────────────────────────────────────────────
    @FXML public void handleDashboard(ActionEvent e) { navigate("/tn/esprit/view/back_admin.fxml", e); }
    @FXML public void handleListeComptes(ActionEvent e) { navigate("/tn/esprit/view/user_index.fxml", e); }
    @FXML public void handleCategories(ActionEvent e) { navigate("/tn/esprit/view/back_CoursCategorieList.fxml", e); }
    @FXML public void handleModules(ActionEvent e) { navigate("/tn/esprit/view/back_CoursModuleList.fxml", e); }
    @FXML public void handleCours(ActionEvent e) { navigate("/tn/esprit/view/back_CoursList.fxml", e); }
    @FXML public void handleEventsList(ActionEvent e) { navigate("/tn/esprit/view/back_Event.fxml", e); }
    @FXML public void handleParticipants(ActionEvent e) { navigate("/tn/esprit/view/back_ParticipantList.fxml", e); }
    @FXML public void handleJeux(ActionEvent e) { navigate("/tn/esprit/view/back_GameList.fxml", e); }
    @FXML public void handleForums(ActionEvent e) { navigate("/tn/esprit/view/back_forums_dashboard.fxml", e); }
    @FXML public void handleLogout(ActionEvent e) { navigate("/tn/esprit/view/front_login.fxml", e); }

    private void navigate(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException ex) {
            showAlert(Alert.AlertType.ERROR, "Navigation", "Erreur : " + ex.getMessage());
        }
    }
}
