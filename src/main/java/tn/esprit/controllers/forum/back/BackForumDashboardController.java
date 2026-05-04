package tn.esprit.controllers.forum.back;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import tn.esprit.entities.forum.Categorie;
import tn.esprit.entities.forum.Forum;
import tn.esprit.services.forum.ServiceCategorie;
import tn.esprit.services.forum.ServiceForum;
import tn.esprit.services.forum.ServiceMessage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackForumDashboardController implements Initializable {

    // Table
    @FXML private TableView<Forum> forumsTable;
    @FXML private TableColumn<Forum, Integer> colId;
    @FXML private TableColumn<Forum, String> colTitre;
    @FXML private TableColumn<Forum, String> colCreatedBy;
    @FXML private TableColumn<Forum, Date> colDate;
    @FXML private TableColumn<Forum, String> colEtat;
    @FXML private TableColumn<Forum, Void> colActions;

    // Filtres
    @FXML private TextField searchEtatField;
    @FXML private TextField searchCreatorField;
    @FXML private ComboBox<String> sortByDateCombo;
    @FXML private Label resultCountLabel;

    private final ServiceForum serviceForum = new ServiceForum();
    private final ServiceCategorie serviceCategorie = new ServiceCategorie();
    private ObservableList<Forum> masterData;
    private FilteredList<Forum> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupComboBoxes();
        loadData();
        setupSearchListeners();
    }

    private void setupColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTitre.setCellValueFactory(new PropertyValueFactory<>("titre"));
        colCreatedBy.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCreatedBy() != null ? c.getValue().getCreatedBy() : "—"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("dateCreation"));

        colDate.setCellFactory(tc -> new TableCell<Forum, Date>() {
            @Override
            protected void updateItem(Date date, boolean empty) {
                super.updateItem(date, empty);
                if (empty || date == null) {
                    setText("—");
                } else {
                    setText(date.toString());
                }
            }
        });

        // État column with colored badges
        colEtat.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEtat() != null ? c.getValue().getEtat() : "—"));
        colEtat.setCellFactory(tc -> new TableCell<Forum, String>() {
            @Override
            protected void updateItem(String etat, boolean empty) {
                super.updateItem(etat, empty);
                if (empty || etat == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label badge = new Label(etat.toLowerCase());
                    badge.setStyle(getEtatBadgeStyle(etat));
                    setGraphic(badge);
                    setText(null);
                }
            }
        });

        // Actions column with edit/delete buttons
        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button delBtn = new Button("🗑");
            private final HBox box = new HBox(5, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #3b82f6; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 3 6; -fx-cursor: hand; -fx-font-size: 12;");
                delBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-border-width: 1; -fx-border-radius: 4; -fx-padding: 3 6; -fx-cursor: hand; -fx-font-size: 12;");
                editBtn.setOnAction(e -> handleEdit(getTableView().getItems().get(getIndex())));
                delBtn.setOnAction(e -> handleDelete(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private String getEtatBadgeStyle(String etat) {
        switch (etat.toLowerCase()) {
            case "actif":
                return "-fx-background-color: #dcfce7; -fx-text-fill: #166534; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;";
            case "inactif":
                return "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;";
            case "fermé":
                return "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: bold;";
            default:
                return "-fx-background-color: #e5e7eb; -fx-text-fill: #374151; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11;";
        }
    }

    private void setupComboBoxes() {
        sortByDateCombo.setItems(FXCollections.observableArrayList("Trier par date", "Plus récent", "Plus ancien"));
        sortByDateCombo.getSelectionModel().selectFirst();
    }

    private void setupSearchListeners() {
        searchEtatField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
        searchCreatorField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());
    }

    private void loadData() {
        List<Forum> list = serviceForum.afficher();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Forum> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(forumsTable.comparatorProperty());
        forumsTable.setItems(sortedData);
        updateResultCount();
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " forum(s)");
    }

    private void applyFilters() {
        String etatSearch = searchEtatField.getText() == null ? "" : searchEtatField.getText().toLowerCase().trim();
        String creatorSearch = searchCreatorField.getText() == null ? "" : searchCreatorField.getText().toLowerCase().trim();

        filteredData.setPredicate(forum -> {
            boolean matchEtat = etatSearch.isEmpty()
                    || (forum.getEtat() != null && forum.getEtat().toLowerCase().contains(etatSearch));

            boolean matchCreator = creatorSearch.isEmpty()
                    || (forum.getCreatedBy() != null && forum.getCreatedBy().toLowerCase().contains(creatorSearch));

            return matchEtat && matchCreator;
        });
        updateResultCount();
    }

    @FXML
    public void handleManageCategories(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forum_categories.fxml", event);
    }

    @FXML
    public void handleManageForums(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forum_forums.fxml", event);
    }

    @FXML
    public void handleManageMessages(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forum_messages.fxml", event);
    }

    @FXML
    public void handleDashboard(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_admin.fxml", event);
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        navigateTo("/tn/esprit/view/front_login.fxml", event);
    }

    private void handleEdit(Forum forum) {
        // Navigate to edit page or show edit dialog
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Éditer");
        info.setContentText("Éditer le forum: " + forum.getTitre());
        info.showAndWait();
    }

    private void handleDelete(Forum forum) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le forum");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer \"" + forum.getTitre() + "\" ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceForum.supprimer(forum.getId());
            loadData();
        }
    }

    private void navigateTo(String fxml, ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxml));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
