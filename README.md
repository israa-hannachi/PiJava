# 🖥️ PiJava — Plateforme Éducative Desktop (JavaFX)


---

## 📋 Table des matières

- [Description du projet](#-description-du-projet)
- [Contexte & continuité](#-contexte--continuité)
- [Modules fonctionnels](#-modules-fonctionnels)
- [Technologies utilisées](#-technologies-utilisées)
- [Prérequis](#-prérequis)
- [Installation](#-installation)
- [Structure du projet](#-structure-du-projet)
- [Stratégie d'intégration](#-stratégie-dintégration)
- [Équipe](#-équipe)

---

## 📖 Description du projet

**PiJava** est la **partie desktop** d'une plateforme éducative complète développée dans le cadre du projet intégré **PIDEV** à l'école **Esprit**. Elle est construite avec **Java** et **JavaFX**, et constitue le prolongement de l'application web réalisée avec Symfony.

L'application couvre **6 gestions métiers** réparties entre les membres du groupe.

---

## 🔗 Contexte & continuité

| Élément | Détail |
|---------|--------|
| **Projet web (partie 1)** | [PIDEV — Symfony](https://github.com/israa-hannachi/PIDEV) |
| **Projet desktop (partie 2)** | Ce dépôt — JavaFX |
| **Base de données partagée** | MySQL — mêmes tables que le projet web |
| **Charte graphique** | Identique à l'application web |
| **Logique métier** | Cohérente avec les entités et règles définies en Symfony |

---

## 🧩 Modules fonctionnels

### 👤 Gestion des utilisateurs
- Connexion classique (email/mot de passe)
- Connexion par **reconnaissance faciale** (modèle YuNet ONNX)
- Authentification **OAuth2 Google**
- **Authentification à deux facteurs (2FA)**
- Réinitialisation de mot de passe via code email à 6 chiffres
- Modification de profil (nom, email, avatar)
- Blocage / déblocage d'utilisateurs (admin)
- Tableau de bord admin avec statistiques

---

### 📚 Gestion des cours 

#### 🗂️ Catégories (`cours_categorie`)
- Affichage de la liste des catégories dans un `TableView` (ID, Nom, Description, Date création, Actif)
- Ajout d'une catégorie (nom requis, max 50 caractères, unicité vérifiée)
- Modification et suppression d'une catégorie
- Tri par date de création décroissante

#### 📦 Modules (`cours_module`)
- Affichage et filtrage des modules (par catégorie, niveau : Débutant / Intermédiaire / Avancé)
- Recherche par titre en temps réel
- CRUD complet

#### 📖 Cours (`cours`)
- Affichage avec pagination (> 20 éléments)
- Filtrage par module, statut actif/inactif
- Recherche par titre insensible à la casse
- Ajout avec :
  - Éditeur de contenu riche (**API TinyMCE**)
  - Upload de fichier **PDF** stocké sur **Cloudinary**
- Gestion de la visibilité (`visible`, `actif`, `visible_from` via DateTimePicker)
- **Drag & drop** pour réorganiser le parcours d'apprentissage (roadmap visuelle)
- **Chatbot** pour résumer le contenu d'un cours (OpenAI / GPT)
- **Email automatique** au formateur lors de la sélection d'un cours
- Statistiques : nombre de catégories / modules / cours, répartition par niveau (PieChart / BarChart)

---

### 📅 Gestion des événements
- Création, modification, suppression d'événements
- Réservation avec validation de capacité en temps réel
- Paiement en ligne des billets
- Vue **calendrier** (JavaFX)
- Affichage sur carte (API Maps)
- Notation (1-5 étoiles) et commentaires
- Gestion des sponsors
- Export de confirmation **PDF**
- **Chatbot** événements (questions en langage naturel)
- Suggestions personnalisées par IA


---

### 🎮 Gestion des quiz / jeux
- Création et gestion de quiz (enseignant)
- Questions par jeu
- Gamification : badges, scores, niveaux, chronomètre (son), max 3 tentatives
- Calculatrice intégrée
- Suggestions de quiz par **API externe**
- Statistiques de progression (enseignant)
- Export (PDF / Excel / CSV)
- Bloc-notes pour sauvegarder les suggestions

---

### 🗓️ Gestion des réunions
- Création de réunions (titre, dates, lien Meet)
- Gestion des participants (ajout / suppression, sans doublon)
- Envoi d'emails avec lien de réunion (SMTP configuré)
- Bouton **Rejoindre** activé uniquement si lien valide et réunion en cours
- Intégration **Jitsi Meet** (`Desktop.browse()`)
- Colorisation visuelle : réunions à venir (vert) vs passées (gris/rouge)
- Statistiques (nb réunions, participants moyens, PieChart)

---

### 💬 Gestion des forums
- Création / modification / suppression de forums et messages
- Réactions (Like / Dislike)
- Recherche et filtrage par catégorie
- **Chatbot IA** (LLaMA3) multilingue (FR / EN)
- Speech-to-Text (Whisper) avec bouton microphone animé
- Thème clair / sombre (Toggle Switch)
- Export WhiteBoard (PNG, SVG, ZIP)
- Statistiques dynamiques (PieChart, BarChart, KPIs)

---

## 🛠 Technologies utilisées

| Technologie | Utilisation |
|-------------|-------------|
| **Java 17+** | Langage principal |
| **JavaFX 17+** | Interface graphique desktop |
| **FXML** | Définition des vues |
| **CSS JavaFX** | Stylisation (cohérence avec le projet web) |
| **MySQL 8** | Base de données relationnelle |
| **JDBC** | Connexion à la base de données |
| **Maven** | Gestion des dépendances et build |
| **Cloudinary API** | Stockage des fichiers PDF/images |
| **TinyMCE API** | Éditeur de contenu riche (intégré via WebView) |
| **OpenAI / GPT** | Chatbot, résumé de cours |
| **LLaMA3** | Chatbot forum multilingue |
| **Whisper** | Reconnaissance vocale |
| **YuNet (ONNX)** | Reconnaissance faciale |
| **Google OAuth2** | Authentification sociale |
| **Jitsi Meet** | Visioconférence |
| **JavaMail (SMTP)** | Envoi d'emails |
| **iTextPDF / Apache POI** | Export PDF / Excel |
| **Git & GitHub** | Versioning et collaboration |

---

## ✅ Prérequis

- **Java JDK 17+**
- **JavaFX SDK 17+**
- **Maven 3.8+**
- **MySQL 8.0+**
- Accès Internet (pour les API : Cloudinary, TinyMCE, OpenAI, etc.)

---

## 🚀 Installation

```bash
# 1. Cloner le dépôt
git clone https://github.com/israa-hannachi/PiJava.git
cd PiJava

# 2. Configurer la base de données
# Importer le schéma SQL (même base que le projet web PIDEV)
# Modifier src/main/resources/config.properties :
#   db.url=jdbc:mysql://localhost:3306/pidev
#   db.user=root
#   db.password=yourpassword

# 3. Compiler et lancer avec Maven
mvn clean javafx:run
```

> ⚠️ **Note** : La base de données est partagée avec le projet web Symfony. Assurez-vous d'avoir appliqué les migrations du projet web avant de lancer l'application desktop.

---

## 🗂 Structure du projet

```
PiJava/
├── src/
│   └── main/
│       ├── java/
│       │   ├── controllers/        # Contrôleurs JavaFX (FXML)
│       │   │   ├── cours/          # Gestion des cours (Categorie, Module, Cours)
│       │   │   ├── user/           # Authentification, profil
│       │   │   ├── evenement/      # Événements, réservations
│       │   │   ├── formation/      # Formations, inscriptions
│       │   │   ├── quiz/           # Quiz, jeux, gamification
│       │   │   ├── meet/           # Réunions, participants
│       │   │   ├── forum/          # Forums, messages, chatbot
│       │   │   └── reclamation/    # Réclamations
│       │   ├── entities/           # Modèles (Cours, Categorie, Module, User...)
│       │   ├── services/           # Logique métier & accès BDD
│       │   └── utils/              # Utilitaires (connexion BDD, helpers)
│       └── resources/
│           ├── fxml/               # Fichiers FXML (vues)
│           ├── css/                # Feuilles de style JavaFX
│           ├── images/             # Ressources graphiques
│           └── config.properties   # Configuration (BDD, API keys)
└── pom.xml                         # Dépendances Maven
```

---

## 🔄 Stratégie d'intégration

Le projet est réalisé en groupe selon la stratégie suivante :

```
Membre 1 (Users)  →  push branche  →  Membre 2 (Cours)  →  push  →  Membre 3 (Events) → ...
```

1. Chaque membre développe son module dans sa propre **branche Git**
2. Une fois terminé, il **push** sa branche
3. Le membre suivant **pull** et integre son développement
4. Les branches sont mergées progressivement dans `main`

---

## 👥 Équipe

Projet réalisé dans le cadre du **PIDEV** à l'école **Esprit** — groupe de 6 membres.

| Module | Responsable |
|--------|------------|
| Gestion des utilisateurs | chahine mezni |
| Gestion des cours | israa hannachi |
| Gestion des événements | isra dabbebi |
| Gestion des forum | farah jemmali |
| Gestion des quiz/jeux | miriam kouki |
| Gestion des réunions |imen ghamlouli |

> **Partie 1 du projet** → Application Web Symfony : [PIDEV](https://github.com/israa-hannachi/PIDEV)

---
