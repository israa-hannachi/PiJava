package tn.esprit.mains;

import tn.esprit.controllers.game.GameController;
import tn.esprit.controllers.game.GameQuestionController;
import tn.esprit.entities.game.Game;
import tn.esprit.entities.game.Game_Question;
import tn.esprit.controllers.event.EventController;
import tn.esprit.controllers.event.SponsorController;
import tn.esprit.controllers.event.RegistrationController;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Sponsor;
import tn.esprit.entities.event.Registration;
import tn.esprit.controllers.cours.CoursCategorieController;
import tn.esprit.controllers.cours.CoursModuleController;
import tn.esprit.controllers.cours.CoursController;
import tn.esprit.entities.cours.cours_categorie;
import tn.esprit.entities.cours.cours_module;
import tn.esprit.entities.cours.cours;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int choice = -1;

        while (choice != 0) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║       MENU PRINCIPAL         ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Gestion des Jeux         ║");
            System.out.println("║  2. Gestion des Événements   ║");
            System.out.println("║  3. Gestion des Cours        ║");
            System.out.println("║  0. Quitter                  ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("Votre choix : ");

            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
                System.out.println("❌ Veuillez entrer un nombre valide.");
                continue;
            }

            switch (choice) {
                case 1:
                    manageGames(scanner);
                    break;
                case 2:
                    manageEvents(scanner);
                    break;
                case 3:
                    manageCours(scanner);
                    break;
                case 0:
                    System.out.println("👋 Au revoir !");
                    break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
        scanner.close();
    }

    // ===================== GESTION DES COURS =====================
    private static void manageCours(Scanner scanner) {
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║       GESTION DES COURS          ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. Gérer les Catégories         ║");
            System.out.println("║  2. Gérer les Modules            ║");
            System.out.println("║  3. Gérer les Cours              ║");
            System.out.println("║  0. Retour au Menu Principal     ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Votre choix : ");
            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
                System.out.println("❌ Choix invalide.");
                continue;
            }
            switch (choix) {
                case 1: manageCategories(scanner); break;
                case 2: manageModules(scanner); break;
                case 3: manageCoursList(scanner); break;
                case 0: System.out.println("↩️ Retour..."); break;
                default: System.out.println("❌ Choix invalide.");
            }
        }
    }

    private static void manageCategories(Scanner scanner) {
        CoursCategorieController cc = new CoursCategorieController();
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║     GESTION DES CATÉGORIES       ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. Afficher toutes              ║");
            System.out.println("║  2. Ajouter une catégorie        ║");
            System.out.println("║  3. Modifier une catégorie       ║");
            System.out.println("║  4. Supprimer une catégorie      ║");
            System.out.println("║  0. Retour                       ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Votre choix : ");
            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
                continue;
            }
            switch (choix) {
                case 1:
                    List<cours_categorie> cats = cc.recupererCategories();
                    if (cats.isEmpty()) {
                        System.out.println("❌ Aucune catégorie trouvée.");
                    } else {
                        System.out.println("\n-----------------------------------------------");
                        System.out.printf("%-5s %-20s %-30s %-6s%n", "ID", "Nom", "Description", "Actif");
                        System.out.println("-----------------------------------------------");
                        for (cours_categorie c : cats) {
                            System.out.printf("%-5d %-20s %-30s %-6d%n",
                                    c.getId(), c.getNom(),
                                    c.getDescription() != null ? c.getDescription() : "-",
                                    c.getActif());
                        }
                    }
                    break;
                case 2:
                    System.out.println("\n--- AJOUTER UNE CATÉGORIE ---");
                    System.out.print("Nom : "); String nom = scanner.nextLine();
                    System.out.print("Description : "); String desc = scanner.nextLine();
                    System.out.print("Actif (1=oui, 0=non) : "); int actif = scanner.nextInt(); scanner.nextLine();
                    cc.ajouterCategorie(new cours_categorie(nom, desc.isEmpty() ? null : desc,
                            new Timestamp(System.currentTimeMillis()), actif));
                    break;
                case 3:
                    System.out.print("ID de la catégorie à modifier : ");
                    int idMod = scanner.nextInt(); scanner.nextLine();
                    cours_categorie catMod = cc.findById(idMod);
                    if (catMod == null) {
                        System.out.println("❌ Catégorie introuvable !");
                    } else {
                        System.out.print("Nouveau nom [" + catMod.getNom() + "] : ");
                        String newNom = scanner.nextLine();
                        if (!newNom.isEmpty()) catMod.setNom(newNom);
                        System.out.print("Nouvelle description [" + catMod.getDescription() + "] : ");
                        String newDesc = scanner.nextLine();
                        if (!newDesc.isEmpty()) catMod.setDescription(newDesc);
                        System.out.print("Actif (1/0) [" + catMod.getActif() + "] : ");
                        String newActif = scanner.nextLine();
                        if (!newActif.isEmpty()) catMod.setActif(Integer.parseInt(newActif));
                        cc.modifierCategorie(catMod);
                    }
                    break;
                case 4:
                    System.out.print("ID de la catégorie à supprimer : ");
                    int idSuppr = scanner.nextInt(); scanner.nextLine();
                    cc.supprimerCategorie(idSuppr);
                    break;
                case 0:
                    System.out.println("↩️ Retour..."); break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }

    private static void manageModules(Scanner scanner) {
        CoursModuleController mc = new CoursModuleController();
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║       GESTION DES MODULES        ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. Afficher tous les modules    ║");
            System.out.println("║  2. Ajouter un module            ║");
            System.out.println("║  3. Modifier un module           ║");
            System.out.println("║  4. Supprimer un module          ║");
            System.out.println("║  0. Retour                       ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Votre choix : ");
            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
                continue;
            }
            switch (choix) {
                case 1:
                    List<cours_module> modules = mc.recupererModules();
                    if (modules.isEmpty()) {
                        System.out.println("❌ Aucun module trouvé.");
                    } else {
                        System.out.println("\n--------------------------------------------------------------");
                        System.out.printf("%-5s %-20s %-12s %-10s %-8s %-5s%n", "ID", "Titre", "Niveau", "Durée(h)", "CatID", "Actif");
                        System.out.println("--------------------------------------------------------------");
                        for (cours_module m : modules) {
                            System.out.printf("%-5d %-20s %-12s %-10d %-8d %-5d%n",
                                    m.getId(), m.getTitre(), m.getNiveau(), m.getDuree(), m.getCategorieId(), m.getActif());
                        }
                    }
                    break;
                case 2:
                    System.out.println("\n--- AJOUTER UN MODULE ---");
                    System.out.print("Titre : "); String titre = scanner.nextLine();
                    System.out.print("Description : "); String desc = scanner.nextLine();
                    System.out.print("Durée (en heures) : "); int duree = scanner.nextInt(); scanner.nextLine();
                    System.out.print("Niveau (Débutant / Intermédiaire / Avancé) : "); String niveau = scanner.nextLine();
                    System.out.print("ID Catégorie : "); int catId = scanner.nextInt(); scanner.nextLine();
                    System.out.print("Actif (1=oui, 0=non) : "); int actif = scanner.nextInt(); scanner.nextLine();
                    mc.ajouterModule(new cours_module(titre, desc.isEmpty() ? null : desc, duree, niveau,
                            new Timestamp(System.currentTimeMillis()), actif, catId, 0));
                    break;
                case 3:
                    System.out.print("ID du module à modifier : ");
                    int idMod = scanner.nextInt(); scanner.nextLine();
                    cours_module modMod = mc.findById(idMod);
                    if (modMod == null) {
                        System.out.println("❌ Module introuvable !");
                    } else {
                        System.out.print("Nouveau titre [" + modMod.getTitre() + "] : ");
                        String newTitre = scanner.nextLine();
                        if (!newTitre.isEmpty()) modMod.setTitre(newTitre);
                        System.out.print("Nouvelle description [" + modMod.getDescription() + "] : ");
                        String newDesc = scanner.nextLine();
                        if (!newDesc.isEmpty()) modMod.setDescription(newDesc);
                        System.out.print("Nouvelle durée [" + modMod.getDuree() + "] : ");
                        String newDuree = scanner.nextLine();
                        if (!newDuree.isEmpty()) modMod.setDuree(Integer.parseInt(newDuree));
                        System.out.print("Nouveau niveau [" + modMod.getNiveau() + "] : ");
                        String newNiveau = scanner.nextLine();
                        if (!newNiveau.isEmpty()) modMod.setNiveau(newNiveau);
                        System.out.print("Actif (1/0) [" + modMod.getActif() + "] : ");
                        String newActif = scanner.nextLine();
                        if (!newActif.isEmpty()) modMod.setActif(Integer.parseInt(newActif));
                        mc.modifierModule(modMod);
                    }
                    break;
                case 4:
                    System.out.print("ID du module à supprimer : ");
                    int idSuppr = scanner.nextInt(); scanner.nextLine();
                    mc.supprimerModule(idSuppr);
                    break;
                case 0:
                    System.out.println("↩️ Retour..."); break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }

    private static void manageCoursList(Scanner scanner) {
        CoursController coursCtrl = new CoursController();
        int choix = -1;
        while (choix != 0) {
            System.out.println("\n╔══════════════════════════════════╗");
            System.out.println("║         GESTION DES COURS        ║");
            System.out.println("╠══════════════════════════════════╣");
            System.out.println("║  1. Afficher tous les cours      ║");
            System.out.println("║  2. Ajouter un cours             ║");
            System.out.println("║  3. Modifier un cours            ║");
            System.out.println("║  4. Supprimer un cours           ║");
            System.out.println("║  5. Cours par module             ║");
            System.out.println("║  0. Retour                       ║");
            System.out.println("╚══════════════════════════════════╝");
            System.out.print("Votre choix : ");
            if (scanner.hasNextInt()) {
                choix = scanner.nextInt();
                scanner.nextLine();
            } else {
                scanner.nextLine();
                continue;
            }
            switch (choix) {
                case 1:
                    List<cours> coursList = coursCtrl.recupererCours();
                    if (coursList.isEmpty()) {
                        System.out.println("❌ Aucun cours trouvé.");
                    } else {
                        System.out.println("\n--------------------------------------------------------------");
                        System.out.printf("%-5s %-25s %-8s %-6s %-8s %-6s%n", "ID", "Titre", "Durée", "Ordre", "ModuleID", "Actif");
                        System.out.println("--------------------------------------------------------------");
                        for (cours c : coursList) {
                            System.out.printf("%-5d %-25s %-8d %-6d %-8d %-6d%n",
                                    c.getId(), c.getTitre(), c.getDuree(), c.getOrdre(), c.getModuleId(), c.getActif());
                        }
                    }
                    break;
                case 2:
                    System.out.println("\n--- AJOUTER UN COURS ---");
                    System.out.print("Titre : "); String titre = scanner.nextLine();
                    System.out.print("Description : "); String desc = scanner.nextLine();
                    System.out.print("Contenu : "); String contenu = scanner.nextLine();
                    System.out.print("Durée (en minutes) : "); int duree = scanner.nextInt();
                    System.out.print("Ordre : "); int ordre = scanner.nextInt(); scanner.nextLine();
                    System.out.print("ID Module : "); int modId = scanner.nextInt(); scanner.nextLine();
                    System.out.print("Actif (1=oui, 0=non) : "); int actif = scanner.nextInt(); scanner.nextLine();
                    System.out.print("Visible (1=oui, 0=non) : "); int visible = scanner.nextInt(); scanner.nextLine();
                    coursCtrl.ajouterCours(new cours(titre, desc.isEmpty() ? null : desc,
                            contenu.isEmpty() ? null : contenu, duree, ordre,
                            new Timestamp(System.currentTimeMillis()), actif, modId, null, 0, visible));
                    break;
                case 3:
                    System.out.print("ID du cours à modifier : ");
                    int idMod = scanner.nextInt(); scanner.nextLine();
                    cours coursMod = coursCtrl.findById(idMod);
                    if (coursMod == null) {
                        System.out.println("❌ Cours introuvable !");
                    } else {
                        System.out.print("Nouveau titre [" + coursMod.getTitre() + "] : ");
                        String newTitre = scanner.nextLine();
                        if (!newTitre.isEmpty()) coursMod.setTitre(newTitre);
                        System.out.print("Nouvelle description [" + coursMod.getDescription() + "] : ");
                        String newDesc = scanner.nextLine();
                        if (!newDesc.isEmpty()) coursMod.setDescription(newDesc);
                        System.out.print("Nouveau contenu [" + coursMod.getContenu() + "] : ");
                        String newContenu = scanner.nextLine();
                        if (!newContenu.isEmpty()) coursMod.setContenu(newContenu);
                        System.out.print("Nouvelle durée [" + coursMod.getDuree() + "] : ");
                        String newDuree = scanner.nextLine();
                        if (!newDuree.isEmpty()) coursMod.setDuree(Integer.parseInt(newDuree));
                        System.out.print("Nouvel ordre [" + coursMod.getOrdre() + "] : ");
                        String newOrdre = scanner.nextLine();
                        if (!newOrdre.isEmpty()) coursMod.setOrdre(Integer.parseInt(newOrdre));
                        System.out.print("Actif (1/0) [" + coursMod.getActif() + "] : ");
                        String newActif = scanner.nextLine();
                        if (!newActif.isEmpty()) coursMod.setActif(Integer.parseInt(newActif));
                        System.out.print("Visible (1/0) [" + coursMod.getVisible() + "] : ");
                        String newVisible = scanner.nextLine();
                        if (!newVisible.isEmpty()) coursMod.setVisible(Integer.parseInt(newVisible));
                        coursCtrl.modifierCours(coursMod);
                    }
                    break;
                case 4:
                    System.out.print("ID du cours à supprimer : ");
                    int idSuppr = scanner.nextInt(); scanner.nextLine();
                    coursCtrl.supprimerCours(idSuppr);
                    break;
                case 5:
                    System.out.print("ID du module : ");
                    int modIdF = scanner.nextInt(); scanner.nextLine();
                    List<cours> byModule = coursCtrl.findByModuleId(modIdF);
                    if (byModule.isEmpty()) {
                        System.out.println("❌ Aucun cours trouvé pour ce module.");
                    } else {
                        for (cours c : byModule) System.out.println(c);
                    }
                    break;
                case 0:
                    System.out.println("↩️ Retour..."); break;
                default:
                    System.out.println("❌ Choix invalide.");
            }
        }
    }

    // ===================== GESTION DES JEUX =====================
    private static void manageGames(Scanner scanner) {
        GameController gc = new GameController();
        GameQuestionController qc = new GameQuestionController();
        int choix = -1;

        while (choix != 0) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║        GESTION DES JEUX      ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Afficher tous les jeux   ║");
            System.out.println("║  2. Ajouter un jeu           ║");
            System.out.println("║  3. Modifier un jeu          ║");
            System.out.println("║  4. Supprimer un jeu         ║");
            System.out.println("║  5. Rechercher par titre     ║");
            System.out.println("║  6. Gérer les questions      ║");
            System.out.println("║  0. Retour au Menu Principal ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("Votre choix : ");
            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1:
                    System.out.println("\n---------------------------------------------------------------");
                    System.out.printf("%-5s %-20s %-10s %-12s %-10s %-10s%n", "ID", "Titre", "Type", "Niveau", "ScoreMax", "Durée");
                    System.out.println("---------------------------------------------------------------");
                    for (Game g : gc.recupererGames()) {
                        System.out.printf("%-5d %-20s %-10s %-12s %-10d %-10d%n", g.getId(), g.getTitre(), g.getType(), g.getNiveau(), g.getScoreMax(), g.getDuration());
                        List<Game_Question> questions = qc.recupererParGame(g.getId());
                        if (questions.isEmpty()) {
                            System.out.println("   ❌ Aucune question pour ce jeu.");
                        } else {
                            for (Game_Question q : questions) {
                                System.out.println("   📌 Q" + q.getId() + ": " + q.getQuestionText());
                                System.out.println("      ✅ Réponse : " + q.getCorrectAnswer());
                            }
                        }
                        System.out.println("---------------------------------------------------------------");
                    }
                    break;
                case 2:
                    System.out.println("\n--- AJOUTER UN JEU ---");
                    System.out.print("Titre : "); String titre = scanner.nextLine();
                    System.out.print("Type (qcm / vraie ou faux / libre) : "); String type = scanner.nextLine();
                    System.out.print("Niveau (débutant / intermediare / avancé) : "); String niveau = scanner.nextLine();
                    System.out.print("Score maximum : "); int scoreMax = scanner.nextInt();
                    System.out.print("Durée (en secondes) : "); int duration = scanner.nextInt();
                    System.out.print("Nombre de tentatives (0-3) : "); int attempts = scanner.nextInt();
                    scanner.nextLine();
                    Game newGame = new Game(titre, type, niveau, scoreMax, 0, 0.0, duration, attempts, new Timestamp(System.currentTimeMillis()), 0);
                    gc.ajouterGame(newGame);
                    System.out.print("Combien de questions voulez-vous ajouter ? ");
                    int nbQuestions = scanner.nextInt(); scanner.nextLine();
                    for (int i = 1; i <= nbQuestions; i++) {
                        System.out.println("\n--- Question " + i + " ---");
                        System.out.print("Texte : "); String qText = scanner.nextLine();
                        System.out.print("Option 1 : "); String op1 = scanner.nextLine();
                        System.out.print("Option 2 : "); String op2 = scanner.nextLine();
                        System.out.print("Option 3 (Entrée pour ignorer) : "); String op3 = scanner.nextLine();
                        System.out.print("Option 4 (Entrée pour ignorer) : "); String op4 = scanner.nextLine();
                        System.out.print("Réponse correcte : "); String correct = scanner.nextLine();
                        qc.ajouterQuestion(new Game_Question(qText, op1, op2, op3.isEmpty() ? null : op3, op4.isEmpty() ? null : op4, correct, newGame.getId()));
                    }
                    break;
                case 3:
                    System.out.print("Entrez l'ID du jeu à modifier : ");
                    int idModif = scanner.nextInt(); scanner.nextLine();
                    Game gameAModifier = gc.findById(idModif);
                    if (gameAModifier == null) System.out.println("❌ Aucun jeu trouvé !");
                    else {
                        System.out.print("Nouveau titre : "); gameAModifier.setTitre(scanner.nextLine());
                        System.out.print("Nouveau type : "); gameAModifier.setType(scanner.nextLine());
                        System.out.print("Nouveau niveau : "); gameAModifier.setNiveau(scanner.nextLine());
                        System.out.print("Nouveau score max : "); gameAModifier.setScoreMax(scanner.nextInt());
                        System.out.print("Nouvelle durée : "); gameAModifier.setDuration(scanner.nextInt()); scanner.nextLine();
                        gc.modifierGame(gameAModifier);
                    }
                    break;
                case 4:
                    System.out.print("ID du jeu à supprimer : ");
                    int idSuppr = scanner.nextInt(); scanner.nextLine();
                    gc.supprimerGame(idSuppr);
                    break;
                case 5:
                    System.out.print("Titre à rechercher : ");
                    String titreRech = scanner.nextLine();
                    List<Game> results = gc.rechercherParTitre(titreRech);
                    if (results.isEmpty()) System.out.println("❌ Aucun jeu trouvé !");
                    else for (Game g : results) System.out.println(g);
                    break;
                case 6:
                    manageQuestions(scanner, qc);
                    break;
                case 0:
                    System.out.println("↩️ Retour au Menu Principal...");
                    break;
            }
        }
    }

    private static void manageQuestions(Scanner scanner, GameQuestionController qc) {
        int choixQ = -1;
        while (choixQ != 0) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║     GESTION DES QUESTIONS    ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Afficher par jeu         ║");
            System.out.println("║  2. Ajouter une question     ║");
            System.out.println("║  3. Modifier une question    ║");
            System.out.println("║  4. Supprimer une question   ║");
            System.out.println("║  0. Retour                   ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("Votre choix : ");
            choixQ = scanner.nextInt(); scanner.nextLine();
            switch (choixQ) {
                case 1:
                    System.out.print("ID du jeu : ");
                    int gId = scanner.nextInt(); scanner.nextLine();
                    for (Game_Question q : qc.recupererParGame(gId)) System.out.println(q);
                    break;
                case 2:
                    System.out.print("ID du jeu : "); int gameId = scanner.nextInt(); scanner.nextLine();
                    System.out.print("Texte : "); String text = scanner.nextLine();
                    System.out.print("Correct : "); String correct = scanner.nextLine();
                    qc.ajouterQuestion(new Game_Question(text, "opt1", "opt2", null, null, correct, gameId));
                    break;
                case 3:
                    System.out.print("ID Question : "); int qId = scanner.nextInt(); scanner.nextLine();
                    Game_Question qm = qc.findById(qId);
                    if (qm != null) {
                        System.out.print("Nouveau texte : "); qm.setQuestionText(scanner.nextLine());
                        qc.modifierQuestion(qm);
                    }
                    break;
                case 4:
                    System.out.print("ID Question : "); int qdId = scanner.nextInt(); scanner.nextLine();
                    qc.supprimerQuestion(qdId);
                    break;
            }
        }
    }

    // ===================== GESTION DES ÉVÉNEMENTS =====================
    private static void manageEvents(Scanner scanner) {
        EventController ec = new EventController();
        SponsorController sc = new SponsorController();
        RegistrationController rc = new RegistrationController();
        int choix = -1;

        while (choix != 0) {
            System.out.println("\n╔══════════════════════════════╗");
            System.out.println("║      GESTION DES EVENEMENTS  ║");
            System.out.println("╠══════════════════════════════╣");
            System.out.println("║  1. Afficher tous les events ║");
            System.out.println("║  2. Ajouter un event         ║");
            System.out.println("║  3. Supprimer un event       ║");
            System.out.println("║  4. Gérer les Sponsors       ║");
            System.out.println("║  5. Gérer les Registrations  ║");
            System.out.println("║  0. Retour au Menu Principal ║");
            System.out.println("╚══════════════════════════════╝");
            System.out.print("Votre choix : ");
            choix = scanner.nextInt();
            scanner.nextLine();

            switch (choix) {
                case 1:
                    System.out.println("\n--- LISTE DES EVENEMENTS ---");
                    for (Event e : ec.recupererEvents()) System.out.println(e);
                    break;
                case 2:
                    System.out.println("\n--- AJOUTER UN EVENEMENT ---");
                    System.out.print("Titre : "); String titre = scanner.nextLine();
                    System.out.print("Description : "); String desc = scanner.nextLine();
                    System.out.print("Lieu : "); String lieu = scanner.nextLine();
                    System.out.print("Prix : "); BigDecimal prix = scanner.nextBigDecimal();
                    System.out.print("Capacité : "); int cap = scanner.nextInt(); scanner.nextLine();
                    System.out.print("Catégorie : "); String cat = scanner.nextLine();
                    System.out.print("Statut : "); String statut = scanner.nextLine();
                    Event newEvent = new Event(0, titre, desc, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis() + 3600000), cap, null, cat, prix, lieu, statut);
                    ec.ajouterEvent(newEvent);
                    break;
                case 3:
                    System.out.print("ID de l'event à supprimer : ");
                    int idSuppr = scanner.nextInt();
                    ec.supprimerEvent(idSuppr);
                    break;
                case 4:
                    System.out.println("\n--- GESTION DES SPONSORS ---");
                    System.out.println("1. Afficher | 2. Ajouter");
                    int choixS = scanner.nextInt(); scanner.nextLine();
                    if (choixS == 1) for (Sponsor s : sc.recupererSponsors()) System.out.println(s);
                    else if (choixS == 2) {
                        System.out.print("Event ID : "); int eId = scanner.nextInt(); scanner.nextLine();
                        System.out.print("Nom : "); String sNom = scanner.nextLine();
                        sc.ajouterSponsor(new Sponsor(0, eId, sNom, "TYPE", BigDecimal.ZERO, new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()+86400000), "ACTIF"));
                    }
                    break;
                case 5:
                    System.out.println("\n--- GESTION DES REGISTRATIONS ---");
                    System.out.println("1. Afficher | 2. Ajouter");
                    int choixR = scanner.nextInt(); scanner.nextLine();
                    if (choixR == 1) for (Registration r : rc.recupererRegistrations()) System.out.println(r);
                    else if (choixR == 2) {
                        System.out.print("Event ID : "); int evId = scanner.nextInt(); scanner.nextLine();
                        System.out.print("Visitor Name : "); String vName = scanner.nextLine();
                        System.out.print("Visitor Email : "); String vEmail = scanner.nextLine();
                        rc.ajouterRegistration(new Registration(0, evId, vName, vEmail, "CONFIRME", "PAYPAL", BigDecimal.ZERO, "PAYE"));
                    }
                    break;
                case 0:
                    System.out.println("↩️ Retour au Menu Principal...");
                    break;
            }
        }
    }
}