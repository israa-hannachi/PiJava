package tn.esprit.controllers.Back;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import tn.esprit.controllers.game.GameQuestionController;
import tn.esprit.entities.game.Game;
import tn.esprit.entities.game.Game_Question;
import tn.esprit.services.game.GameService;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.sql.SQLException;
import java.util.List;

public class BackGameListController {
    // Table des Jeux
    @FXML private TableView<Game> gamesTable;
    @FXML private TableColumn<Game, Integer> idColumn;
    @FXML private TableColumn<Game, String> titreColumn;
    @FXML private TableColumn<Game, String> typeColumn;
    @FXML private TableColumn<Game, String> niveauColumn;
    @FXML private TableColumn<Game, Integer> attemptColumn;
    @FXML private TableColumn<Game, Void> actionsColumn;
    @FXML private TextField searchField;

    // Stats
    @FXML private Label totalGamesLabel;
    @FXML private Label qcmCountLabel;
    @FXML private Label vfCountLabel;
    @FXML private Label libreCountLabel;

    // Table des Questions liées
    @FXML private TableView<Game_Question> questionsTable;
    @FXML private TableColumn<Game_Question, Integer> questionIdColumn;
    @FXML private TableColumn<Game_Question, String> questionTextColumn;
    @FXML private TableColumn<Game_Question, String> correctAnswerColumn;
    @FXML private TableColumn<Game_Question, Void> questionActionsColumn;

    private GameService gameService = new GameService();

    @FXML
    public void initialize() {
        // Colonnes Jeux
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        titreColumn.setCellValueFactory(new PropertyValueFactory<>("titre"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        niveauColumn.setCellValueFactory(new PropertyValueFactory<>("niveau"));
        attemptColumn.setCellValueFactory(new PropertyValueFactory<>("attemptNumber"));

        // Colonnes Questions
        questionIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        questionTextColumn.setCellValueFactory(new PropertyValueFactory<>("questionText"));
        correctAnswerColumn.setCellValueFactory(new PropertyValueFactory<>("correctAnswer"));
        addQuestionActionsToTable();

        // Configurer les actions pour les jeux
        addActionsToTable();

        // Charger la liste des jeux au démarrage
        loadGames();

        // ⚡ Listener : quand on sélectionne un jeu, charger ses questions
        gamesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                loadQuestions(newSel.getId());
            } else {
                questionsTable.getItems().clear(); // si aucun jeu sélectionné, vider la table des questions
            }
        });
    }

    // Charger les jeux
    private void loadGames() {
        try {
            List<Game> games = gameService.recuperer();
            ObservableList<Game> data = FXCollections.observableArrayList(games);
            gamesTable.setItems(data);

            // Mettre à jour les stats
            totalGamesLabel.setText(String.valueOf(games.size()));
            long qcmCount = games.stream().filter(g -> "qcm".equalsIgnoreCase(g.getType())).count();
            long vfCount = games.stream().filter(g -> "vraie ou faux".equalsIgnoreCase(g.getType())).count();
            long libreCount = games.stream().filter(g -> "libre".equalsIgnoreCase(g.getType())).count();

            qcmCountLabel.setText(String.valueOf(qcmCount));
            vfCountLabel.setText(String.valueOf(vfCount));
            libreCountLabel.setText(String.valueOf(libreCount));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Charger les questions liées à un jeu
    private void loadQuestions(int gameId) {
        List<Game_Question> questions = new GameQuestionController().recupererParGame(gameId);
        questionsTable.setItems(FXCollections.observableArrayList(questions));
    }

    // Recherche
    @FXML
    private void handleSearch() {
        String filter = searchField.getText();
        try {
            List<Game> games = filter.isEmpty() ? gameService.recuperer() : gameService.rechercherParTitre(filter);
            gamesTable.setItems(FXCollections.observableArrayList(games));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddGame() {
        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un jeu");
        dialog.setHeaderText("Remplissez les informations du nouveau jeu");

        // Boutons
        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, cancelButtonType);

        // Champs du formulaire
        TextField titreField = new TextField();
        titreField.setPromptText("Titre du jeu");
        titreField.setPrefWidth(300);

        TextField typeField = new TextField();
        typeField.setPromptText("Type (QCM, Vrai/Faux, Libre)");
        typeField.setPrefWidth(300);

        TextField niveauField = new TextField();
        niveauField.setPromptText("Niveau (Débutant, Intermédiaire, Avancé)");
        niveauField.setPrefWidth(300);

        TextField attemptField = new TextField();
        attemptField.setPromptText("Nombre de tentatives");
        attemptField.setPrefWidth(300);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titreField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeField, 1, 1);
        grid.add(new Label("Niveau:"), 0, 2);
        grid.add(niveauField, 1, 2);
        grid.add(new Label("Tentatives:"), 0, 3);
        grid.add(attemptField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // 🎨 Style des boutons
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:8;");

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(cancelButtonType);
        cancelButton.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-font-weight:bold; -fx-background-radius:8;");

        // Conversion du résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                if (titreField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Titre manquant");
                    alert.setHeaderText("Veuillez entrer un titre pour le jeu !");
                    alert.showAndWait();
                    return null; // annuler la création
                }

                Game newGame = new Game();
                newGame.setTitre(titreField.getText().trim());
                newGame.setType(typeField.getText());
                newGame.setNiveau(niveauField.getText());
                try {
                    newGame.setAttemptNumber(Integer.parseInt(attemptField.getText()));
                } catch (NumberFormatException e) {
                    newGame.setAttemptNumber(0); // valeur par défaut si vide ou invalide
                }
                newGame.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

                return newGame;
            }
            return null;
        });


        dialog.showAndWait().ifPresent(newGame -> {
            try {
                gameService.ajouter(newGame); // méthode à implémenter dans GameService
                loadGames(); // rafraîchir la table des jeux
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Impossible d'ajouter le jeu");
                errorAlert.setContentText(e.getMessage());
                errorAlert.showAndWait();
            }
        });
    }


    // Configurer la colonne Actions des jeux
    private void addActionsToTable() {
        actionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("✏ Modifier");
            private final Button deleteButton = new Button("🗑 Supprimer");
            private final Button addQuestionButton = new Button("➕ Ajouter Question");
            private final HBox container = new HBox(8, editButton, deleteButton, addQuestionButton);

            {
                // 🎨 Styles
                editButton.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:8;");
                deleteButton.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-background-radius:8;");
                addQuestionButton.setStyle("-fx-background-color:#2563eb; -fx-text-fill:white; -fx-background-radius:8;");

                // Actions
                editButton.setOnAction(e -> {
                    Game game = getTableView().getItems().get(getIndex());
                    modifier(game);
                });

                deleteButton.setOnAction(e -> {
                    Game game = getTableView().getItems().get(getIndex());
                    supprimer(game);
                });

                addQuestionButton.setOnAction(e -> {
                    Game game = getTableView().getItems().get(getIndex());
                    ajouterQuestion(game); // 👉 on va créer cette fonction ensuite
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }


    // Supprimer un jeu
    private void supprimer(Game g) {
        GameQuestionController questionController = new GameQuestionController();
        List<Game_Question> questions = questionController.recupererParGame(g.getId());

        if (!questions.isEmpty()) {
            // ⚠️ Il existe des questions liées
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Suppression impossible");
            alert.setHeaderText("Jeu lié à des questions");
            alert.setContentText("Veuillez supprimer les questions liées à ce jeu avant de le supprimer.");
            alert.showAndWait();
        } else {
            // ✅ Pas de questions liées → demander confirmation
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Voulez-vous vraiment supprimer le jeu : " + g.getTitre() + " ?",
                    ButtonType.YES, ButtonType.NO);
            alert.setTitle("Confirmation suppression");
            alert.setHeaderText("Supprimer le jeu");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        gameService.supprimer(g.getId()); // peut lancer SQLException
                        loadGames(); // rafraîchir la table
                        questionsTable.getItems().clear(); // vider les questions si le jeu est supprimé
                    } catch (SQLException e) {
                        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                        errorAlert.setTitle("Erreur SQL");
                        errorAlert.setHeaderText("Impossible de supprimer le jeu");
                        errorAlert.setContentText("Une erreur est survenue : " + e.getMessage());
                        errorAlert.showAndWait();
                    }
                }
            });
        }
    }



    // Modifier un jeu
    private void modifier(Game g) {
        Dialog<Game> dialog = new Dialog<>();
        dialog.setTitle("Modifier le jeu");
        dialog.setHeaderText("Modifier les informations du jeu : " + g.getTitre());

        // Boutons OK / Annuler
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Champs de formulaire
        TextField titreField = new TextField(g.getTitre());
        TextField typeField = new TextField(g.getType());
        TextField niveauField = new TextField(g.getNiveau());
        TextField attemptField = new TextField(String.valueOf(g.getAttemptNumber()));

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Titre:"), 0, 0);
        grid.add(titreField, 1, 0);
        grid.add(new Label("Type:"), 0, 1);
        grid.add(typeField, 1, 1);
        grid.add(new Label("Niveau:"), 0, 2);
        grid.add(niveauField, 1, 2);
        grid.add(new Label("Tentatives:"), 0, 3);
        grid.add(attemptField, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Conversion du résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                if (titreField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Titre manquant");
                    alert.setHeaderText("Veuillez entrer un titre pour le jeu !");
                    alert.showAndWait();
                    return null; // annuler la création
                }

                Game newGame = new Game();
                newGame.setTitre(titreField.getText().trim());
                newGame.setType(typeField.getText());
                newGame.setNiveau(niveauField.getText());
                try {
                    newGame.setAttemptNumber(Integer.parseInt(attemptField.getText()));
                } catch (NumberFormatException e) {
                    newGame.setAttemptNumber(0); // valeur par défaut si vide ou invalide
                }
                newGame.setCreatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

                return newGame;
            }
            return null;
        });


        dialog.showAndWait().ifPresent(updatedGame -> {
            try {
                gameService.modifier(updatedGame); // méthode à implémenter dans GameService
                loadGames(); // rafraîchir la table
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Impossible de modifier le jeu");
                errorAlert.setContentText(e.getMessage());
                errorAlert.showAndWait();
            }
        });
    }


    // Configurer la colonne Actions des questions
    private void addQuestionActionsToTable() {
        questionActionsColumn.setCellFactory(col -> new TableCell<>() {
            private final Button editButton = new Button("✏ Modifier");
            private final Button deleteButton = new Button("🗑 Supprimer");
            private final HBox container = new HBox(8, editButton, deleteButton);


            {      editButton.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:8;");
                deleteButton.setStyle("-fx-background-color:#ef4444; -fx-text-fill:white; -fx-background-radius:8;");

                editButton.setOnAction(e -> {
                    Game_Question q = getTableView().getItems().get(getIndex());
                    modifierQuestion(q);
                });
                deleteButton.setOnAction(e -> {
                    Game_Question q = getTableView().getItems().get(getIndex());
                    supprimerQuestion(q);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        });
    }

    // Supprimer une question
    private void supprimerQuestion(Game_Question q) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                "Supprimer la question : " + q.getQuestionText() + " ?", ButtonType.YES, ButtonType.NO);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                new GameQuestionController().supprimerQuestion(q.getId());
                loadQuestions(q.getGameId());
            }
        });
    }

    // Modifier une question
    private void modifierQuestion(Game_Question q) {
        Dialog<Game_Question> dialog = new Dialog<>();
        dialog.setTitle("Modifier la question");
        dialog.setHeaderText("Modifier la question ID : " + q.getId());

        // Boutons OK / Annuler
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        // Champs de formulaire
        TextField questionTextField = new TextField(q.getQuestionText());
        TextField option1Field = new TextField(q.getOption1());
        TextField option2Field = new TextField(q.getOption2());
        TextField option3Field = new TextField(q.getOption3());
        TextField option4Field = new TextField(q.getOption4());
        TextField correctAnswerField = new TextField(q.getCorrectAnswer());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Question:"), 0, 0);
        grid.add(questionTextField, 1, 0);
        grid.add(new Label("Option 1:"), 0, 1);
        grid.add(option1Field, 1, 1);
        grid.add(new Label("Option 2:"), 0, 2);
        grid.add(option2Field, 1, 2);
        grid.add(new Label("Option 3:"), 0, 3);
        grid.add(option3Field, 1, 3);
        grid.add(new Label("Option 4:"), 0, 4);
        grid.add(option4Field, 1, 4);
        grid.add(new Label("Réponse correcte:"), 0, 5);
        grid.add(correctAnswerField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Conversion du résultat
        // Conversion du résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                // ✅ Contrôles de saisie
                if (questionTextField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Question manquante");
                    alert.setHeaderText("Veuillez entrer le texte de la question !");
                    alert.showAndWait();
                    return null;
                }

                if (option1Field.getText().trim().isEmpty() ||
                        option2Field.getText().trim().isEmpty() ||
                        option3Field.getText().trim().isEmpty() ||
                        option4Field.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Options manquantes");
                    alert.setHeaderText("Veuillez remplir toutes les options (1 à 4) !");
                    alert.showAndWait();
                    return null;
                }

                if (correctAnswerField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Réponse correcte manquante");
                    alert.setHeaderText("Veuillez entrer la réponse correcte !");
                    alert.showAndWait();
                    return null;
                }

                // ✅ Vérifier que la réponse correcte correspond à une des options
                String correct = correctAnswerField.getText().trim();
                if (!(correct.equals(option1Field.getText().trim()) ||
                        correct.equals(option2Field.getText().trim()) ||
                        correct.equals(option3Field.getText().trim()) ||
                        correct.equals(option4Field.getText().trim()))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Réponse incorrecte");
                    alert.setHeaderText("La réponse correcte doit correspondre à l'une des options !");
                    alert.showAndWait();
                    return null;
                }

                // ✅ Mise à jour de l'objet Question
                q.setQuestionText(questionTextField.getText().trim());
                q.setOption1(option1Field.getText().trim());
                q.setOption2(option2Field.getText().trim());
                q.setOption3(option3Field.getText().trim());
                q.setOption4(option4Field.getText().trim());
                q.setCorrectAnswer(correct);

                return q;
            }
            return null;
        });


        dialog.showAndWait().ifPresent(updatedQuestion -> {
            try {
                new GameQuestionController().modifierQuestion(updatedQuestion); // méthode à implémenter dans GameQuestionController
                loadQuestions(updatedQuestion.getGameId()); // rafraîchir la table des questions
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Impossible de modifier la question");
                errorAlert.setContentText(e.getMessage());
                errorAlert.showAndWait();
            }
        });
    }
    // Ajouter une question liée à un jeu
    private void ajouterQuestion(Game g) {
        Dialog<Game_Question> dialog = new Dialog<>();
        dialog.setTitle("Ajouter une question");
        dialog.setHeaderText("Nouvelle question pour le jeu : " + g.getTitre());

        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType("Annuler", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, cancelButtonType);

        // Champs du formulaire
        TextField questionTextField = new TextField();
        questionTextField.setPromptText("Texte de la question");
        questionTextField.setPrefWidth(300);

        TextField option1Field = new TextField();
        option1Field.setPromptText("Option 1");
        option1Field.setPrefWidth(300);

        TextField option2Field = new TextField();
        option2Field.setPromptText("Option 2");
        option2Field.setPrefWidth(300);

        TextField option3Field = new TextField();
        option3Field.setPromptText("Option 3");
        option3Field.setPrefWidth(300);

        TextField option4Field = new TextField();
        option4Field.setPromptText("Option 4");
        option4Field.setPrefWidth(300);

        TextField correctAnswerField = new TextField();
        correctAnswerField.setPromptText("Réponse correcte");
        correctAnswerField.setPrefWidth(300);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Question:"), 0, 0);
        grid.add(questionTextField, 1, 0);
        grid.add(new Label("Option 1:"), 0, 1);
        grid.add(option1Field, 1, 1);
        grid.add(new Label("Option 2:"), 0, 2);
        grid.add(option2Field, 1, 2);
        grid.add(new Label("Option 3:"), 0, 3);
        grid.add(option3Field, 1, 3);
        grid.add(new Label("Option 4:"), 0, 4);
        grid.add(option4Field, 1, 4);
        grid.add(new Label("Réponse correcte:"), 0, 5);
        grid.add(correctAnswerField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        // Conversion du résultat
        // Conversion du résultat
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                // ✅ Contrôles de saisie
                if (questionTextField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Question manquante");
                    alert.setHeaderText("Veuillez entrer le texte de la question !");
                    alert.showAndWait();
                    return null;
                }

                if (option1Field.getText().trim().isEmpty() ||
                        option2Field.getText().trim().isEmpty() ||
                        option3Field.getText().trim().isEmpty() ||
                        option4Field.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Options manquantes");
                    alert.setHeaderText("Veuillez remplir toutes les options (1 à 4) !");
                    alert.showAndWait();
                    return null;
                }

                if (correctAnswerField.getText().trim().isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("Réponse correcte manquante");
                    alert.setHeaderText("Veuillez entrer la réponse correcte !");
                    alert.showAndWait();
                    return null;
                }

                // ✅ Vérifier que la réponse correcte correspond à une des options
                String correct = correctAnswerField.getText().trim();
                if (!(correct.equals(option1Field.getText().trim()) ||
                        correct.equals(option2Field.getText().trim()) ||
                        correct.equals(option3Field.getText().trim()) ||
                        correct.equals(option4Field.getText().trim()))) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Réponse incorrecte");
                    alert.setHeaderText("La réponse correcte doit correspondre à l'une des options !");
                    alert.showAndWait();
                    return null;
                }

                // ✅ Création de l'objet Question
                Game_Question newQuestion = new Game_Question();
                newQuestion.setGameId(g.getId()); // lien avec le jeu
                newQuestion.setQuestionText(questionTextField.getText().trim());
                newQuestion.setOption1(option1Field.getText().trim());
                newQuestion.setOption2(option2Field.getText().trim());
                newQuestion.setOption3(option3Field.getText().trim());
                newQuestion.setOption4(option4Field.getText().trim());
                newQuestion.setCorrectAnswer(correct);

                return newQuestion;
            }
            return null;
        });


        dialog.showAndWait().ifPresent(newQuestion -> {
            try {
                new GameQuestionController().ajouterQuestion(newQuestion); // méthode à implémenter
                loadQuestions(g.getId()); // rafraîchir la table des questions
            } catch (Exception e) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("Erreur");
                errorAlert.setHeaderText("Impossible d'ajouter la question");
                errorAlert.setContentText(e.getMessage());
                errorAlert.showAndWait();
            }
        });
    }


}
