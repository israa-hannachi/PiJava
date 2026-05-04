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
import tn.esprit.entities.forum.Forum;
import tn.esprit.entities.forum.Message;
import tn.esprit.services.forum.ServiceForum;
import tn.esprit.services.forum.ServiceMessage;

import java.io.IOException;
import java.net.URL;
import java.sql.Date;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackForumMessagesController implements Initializable {

    @FXML private TableView<Message> messageTable;

    @FXML private TableColumn<Message, String> colContenu;
    @FXML private TableColumn<Message, String> colForum;
    @FXML private TableColumn<Message, String> colEtat;
    @FXML private TableColumn<Message, String> colCreatedBy;
    @FXML private TableColumn<Message, String> colDate;
    @FXML private TableColumn<Message, String> colLikes;
    @FXML private TableColumn<Message, String> colDislikes;
    @FXML private TableColumn<Message, Void> colActions;

    @FXML private TextField searchField;
    @FXML private ComboBox<String> filterEtat;
    @FXML private ComboBox<Forum> forumCombo;
    
    @FXML private TextArea contenuField;
    @FXML private TextField createdByField;
    @FXML private ComboBox<String> etatCombo;
    @FXML private ComboBox<Forum> forumFormCombo;
    
    @FXML private Label errorLabel;
    @FXML private Label resultCountLabel;
    @FXML private Label totalMessagesLabel;
    @FXML private Label publieLabel;
    @FXML private Label modereLabel;
    @FXML private Label topContributorLabel;

    private final ServiceMessage serviceMessage = new ServiceMessage();
    private final ServiceForum serviceForum = new ServiceForum();
    private ObservableList<Message> masterData;
    private FilteredList<Message> filteredData;
    private Message selectedMessage = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupColumns();
        setupComboBoxes();
        loadData();
        errorLabel.setVisible(false);
    }

    private void setupComboBoxes() {
        filterEtat.setItems(FXCollections.observableArrayList("Tous", "Publié", "Modéré", "Supprimé"));
        filterEtat.getSelectionModel().selectFirst();
        
        etatCombo.setItems(FXCollections.observableArrayList("publié", "modéré", "supprimé"));
        etatCombo.getSelectionModel().selectFirst();
        
        List<Forum> forums = serviceForum.afficher();
        forumCombo.setItems(FXCollections.observableArrayList(forums));
        forumFormCombo.setItems(FXCollections.observableArrayList(forums));
    }

    private void setupColumns() {

        colContenu.setCellValueFactory(c -> {
            String contenu = c.getValue().getContenu();
            if (contenu == null) return new SimpleStringProperty("—");
            // Truncate long content
            if (contenu.length() > 50) contenu = contenu.substring(0, 47) + "...";
            return new SimpleStringProperty(contenu);
        });
        colForum.setCellValueFactory(c -> {
            Forum forum = c.getValue().getForum();
            return new SimpleStringProperty(forum != null ? forum.getTitre() : "—");
        });
        colEtat.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getEtat() != null ? c.getValue().getEtat() : "—"));
        colCreatedBy.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getCreatedBy() != null ? c.getValue().getCreatedBy() : "—"));
        colDate.setCellValueFactory(c -> {
            Date date = c.getValue().getDatePublication();
            return new SimpleStringProperty(date != null ? date.toString() : "—");
        });

        // Setup Likes column with count and tooltip
        colLikes.setCellValueFactory(c -> {
            String raw = c.getValue().getLikesUsers();
            if (raw == null || raw.isEmpty()) return new SimpleStringProperty("0");
            long count = java.util.Arrays.stream(raw.split("\\|")).filter(s -> !s.isEmpty()).count();
            return new SimpleStringProperty(String.valueOf(count));
        });
        colLikes.setCellFactory(tc -> new TableCell<Message, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    Message msg = getTableView().getItems().get(getIndex());
                    String names = msg.getLikesUsers().replace("|", ", ").replaceAll("^, |, $", "");
                    if (!names.isEmpty()) {
                        Tooltip tt = new Tooltip(names);
                        tt.setStyle("-fx-font-size: 11;");
                        setTooltip(tt);
                    } else {
                        setTooltip(null);
                    }
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #16a34a; -fx-font-weight: bold;");
                }
            }
        });

        // Setup Dislikes column with count and tooltip
        colDislikes.setCellValueFactory(c -> {
            String raw = c.getValue().getDislikesUsers();
            if (raw == null || raw.isEmpty()) return new SimpleStringProperty("0");
            long count = java.util.Arrays.stream(raw.split("\\|")).filter(s -> !s.isEmpty()).count();
            return new SimpleStringProperty(String.valueOf(count));
        });
        colDislikes.setCellFactory(tc -> new TableCell<Message, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);
                } else {
                    setText(item);
                    Message msg = getTableView().getItems().get(getIndex());
                    String names = msg.getDislikesUsers().replace("|", ", ").replaceAll("^, |, $", "");
                    if (!names.isEmpty()) {
                        Tooltip tt = new Tooltip(names);
                        tt.setStyle("-fx-font-size: 11;");
                        setTooltip(tt);
                    } else {
                        setTooltip(null);
                    }
                    setStyle("-fx-alignment: CENTER; -fx-text-fill: #dc2626; -fx-font-weight: bold;");
                }
            }
        });

        colActions.setCellFactory(tc -> new TableCell<>() {
            private final Button editBtn = new Button("✏️");
            private final Button delBtn = new Button("🗑️");
            private final HBox box = new HBox(6, editBtn, delBtn);

            {
                editBtn.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:4 10;");
                delBtn.setStyle("-fx-background-color:#dc2626; -fx-text-fill:white; -fx-background-radius:6; -fx-padding:4 10;");
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

    private void loadData() {
        List<Message> list = serviceMessage.afficher();
        masterData = FXCollections.observableArrayList(list);
        filteredData = new FilteredList<>(masterData, p -> true);
        SortedList<Message> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(messageTable.comparatorProperty());
        messageTable.setItems(sortedData);
        updateStats(list);
        updateResultCount();
    }

    private void updateStats(List<Message> list) {
        long total = list.size();
        long publie = list.stream().filter(m -> "publié".equalsIgnoreCase(m.getEtat())).count();
        long modere = list.stream().filter(m -> "modéré".equalsIgnoreCase(m.getEtat())).count();
        
        totalMessagesLabel.setText(String.valueOf(total));
        publieLabel.setText(String.valueOf(publie));
        modereLabel.setText(String.valueOf(modere));
        
        // Find top contributor
        String topContributor = list.stream()
            .collect(java.util.stream.Collectors.groupingBy(Message::getCreatedBy, java.util.stream.Collectors.counting()))
            .entrySet().stream()
            .max(java.util.Map.Entry.comparingByValue())
            .map(java.util.Map.Entry::getKey)
            .orElse("—");
        topContributorLabel.setText(topContributor);
    }

    private void updateResultCount() {
        resultCountLabel.setText(filteredData.size() + " message(s)");
    }

    @FXML
    public void handleSearch() {
        applyFilters();
    }

    @FXML
    public void handleFilterEtat() {
        applyFilters();
    }

    @FXML
    public void handleFilterForum() {
        applyFilters();
    }

    private void applyFilters() {
        String search = searchField.getText() == null ? "" : searchField.getText().toLowerCase().trim();
        String etat = filterEtat.getValue();
        Forum forum = forumCombo.getValue();

        filteredData.setPredicate(msg -> {
            boolean matchSearch = search.isEmpty()
                    || (msg.getContenu() != null && msg.getContenu().toLowerCase().contains(search))
                    || (msg.getCreatedBy() != null && msg.getCreatedBy().toLowerCase().contains(search));

            boolean matchEtat = "Tous".equals(etat) || etat == null
                    || ("Publié".equals(etat) && "publié".equalsIgnoreCase(msg.getEtat()))
                    || ("Modéré".equals(etat) && "modéré".equalsIgnoreCase(msg.getEtat()))
                    || ("Supprimé".equals(etat) && "supprimé".equalsIgnoreCase(msg.getEtat()));

            boolean matchForum = forum == null || 
                    (msg.getForum() != null && msg.getForum().getId() == forum.getId());

            return matchSearch && matchEtat && matchForum;
        });
        updateResultCount();
    }



    @FXML
    public void sortByDate() {
        masterData.sort(Comparator.comparing(Message::getDatePublication, 
            Comparator.nullsLast(Comparator.naturalOrder())));
        messageTable.refresh();
    }

    @FXML
    public void sortByEtat() {
        masterData.sort(Comparator.comparing(Message::getEtat, 
            Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER)));
        messageTable.refresh();
    }

    @FXML
    public void handleAdd() {
        errorLabel.setVisible(false);
        
        String contenu = contenuField.getText();
        String createdBy = createdByField.getText();
        String etat = etatCombo.getValue();
        Forum forum = forumFormCombo.getValue();

        // Contrôle de saisie
        if (contenu == null || contenu.trim().isEmpty()) {
            showError("Le contenu est obligatoire");
            return;
        }
        if (contenu.length() > 5000) {
            showError("Le contenu ne doit pas dépasser 5000 caractères");
            return;
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            showError("L'auteur est obligatoire");
            return;
        }
        if (createdBy.length() > 255) {
            showError("Le nom de l'auteur ne doit pas dépasser 255 caractères");
            return;
        }
        if (forum == null) {
            showError("Veuillez sélectionner un forum");
            return;
        }

        Message msg = new Message(
            contenu.trim(), 
            new Date(System.currentTimeMillis()), 
            etat, 
            createdBy.trim(), 
            forum
        );
        serviceMessage.ajouter(msg);
        
        clearFields();
        loadData();
        showSuccess("Message ajouté avec succès");
    }

    @FXML
    public void handleUpdate() {
        if (selectedMessage == null) {
            showError("Veuillez sélectionner un message à modifier");
            return;
        }

        String contenu = contenuField.getText();
        String etat = etatCombo.getValue();

        // Contrôle de saisie
        if (contenu == null || contenu.trim().isEmpty()) {
            showError("Le contenu est obligatoire");
            return;
        }
        if (contenu.length() > 5000) {
            showError("Le contenu ne doit pas dépasser 5000 caractères");
            return;
        }

        selectedMessage.setContenu(contenu.trim());
        selectedMessage.setEtat(etat);
        
        serviceMessage.modifier(selectedMessage);
        
        clearFields();
        selectedMessage = null;
        loadData();
        showSuccess("Message modifié avec succès");
    }

    @FXML
    public void handleClear() {
        clearFields();
        selectedMessage = null;
        errorLabel.setVisible(false);
    }

    private void handleEdit(Message msg) {
        selectedMessage = msg;
        contenuField.setText(msg.getContenu());
        createdByField.setText(msg.getCreatedBy());
        createdByField.setDisable(true); // Cannot change author
        etatCombo.setValue(msg.getEtat());
        forumFormCombo.setValue(msg.getForum());
        forumFormCombo.setDisable(true); // Cannot change forum
    }

    private void handleDelete(Message msg) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmation");
        confirm.setHeaderText("Supprimer le message");
        confirm.setContentText("Êtes-vous sûr de vouloir supprimer ce message ?");
        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            serviceMessage.supprimer(msg.getId());
            loadData();
            showSuccess("Message supprimé avec succès");
        }
    }

    private void clearFields() {
        contenuField.clear();
        createdByField.clear();
        createdByField.setDisable(false);
        etatCombo.getSelectionModel().selectFirst();
        forumFormCombo.setValue(null);
        forumFormCombo.setDisable(false);
    }

    private void showError(String msg) {
        errorLabel.setText("❌ " + msg);
        errorLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
    }

    private void showSuccess(String msg) {
        errorLabel.setText("✅ " + msg);
        errorLabel.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
        errorLabel.setVisible(true);
    }

    @FXML
    public void handleBack(ActionEvent event) {
        navigateTo("/tn/esprit/view/back_forum_dashboard.fxml", event);
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
