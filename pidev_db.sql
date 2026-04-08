-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Hôte : 127.0.0.1
-- Généré le : ven. 03 avr. 2026 à 10:56
-- Version du serveur : 10.4.32-MariaDB
-- Version de PHP : 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Base de données : `pidev_db`
--

-- --------------------------------------------------------

--
-- Structure de la table `aimodel_adjustment`
--

CREATE TABLE `aimodel_adjustment` (
  `id` int(11) NOT NULL,
  `factor_type` varchar(255) NOT NULL,
  `factor_value` varchar(255) NOT NULL,
  `adjustment_multiplier` double NOT NULL,
  `sample_size` int(11) NOT NULL,
  `last_updated` datetime NOT NULL,
  `is_active` tinyint(4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `aiprediction`
--

CREATE TABLE `aiprediction` (
  `id` int(11) NOT NULL,
  `event_id` int(11) DEFAULT NULL,
  `prediction_type` varchar(255) NOT NULL,
  `predicted_value` double NOT NULL,
  `actual_value` double DEFAULT NULL,
  `confidence` double NOT NULL,
  `factors` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`factors`)),
  `prediction_date` datetime NOT NULL,
  `accuracy_percentage` double DEFAULT NULL,
  `evaluated` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `categorie`
--

CREATE TABLE `categorie` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `icone` varchar(10) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `categorie`
--

INSERT INTO `categorie` (`id`, `titre`, `description`, `icone`) VALUES
(2, 'Analyse numérique', 'Forum pour discuter des méthodes numériques, résoudre des exercices et échanger des astuces sur les techniques de calcul approché.', NULL),
(3, 'Infrastructure des réseaux IP', 'Forum dédié aux échanges et discussions autour des concepts fondamentaux des réseaux IP, incluant l\'adressage, le routage et les techniques de segmentation des réseaux.', NULL),
(4, 'Théorie des langages', 'Forum dédié aux échanges et discussions autour des concepts fondamentaux de la théorie des langages formels, des automates et des grammaires.', NULL),
(5, 'Environnement & Stratégies de l\'entreprise', 'Forum dédié aux échanges et discussions autour de l\'environnement de l\'entreprise, de ses objectifs et des outils d\'analyse stratégique.', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `cours`
--

CREATE TABLE `cours` (
  `id` int(11) NOT NULL,
  `titre` varchar(150) NOT NULL,
  `description` longtext DEFAULT NULL,
  `contenu` longtext DEFAULT NULL,
  `duree` int(11) NOT NULL,
  `ordre` int(11) NOT NULL,
  `date_creation` datetime NOT NULL,
  `date_modification` datetime DEFAULT NULL,
  `actif` tinyint(4) NOT NULL,
  `module_id` int(11) NOT NULL,
  `fichier_contenu` longtext DEFAULT NULL,
  `cree_par_admin` tinyint(1) NOT NULL DEFAULT 0,
  `visible` tinyint(1) NOT NULL DEFAULT 1,
  `visible_from` datetime DEFAULT NULL,
  `resume_ai` longtext DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `cours`
--

INSERT INTO `cours` (`id`, `titre`, `description`, `contenu`, `duree`, `ordre`, `date_creation`, `date_modification`, `actif`, `module_id`, `fichier_contenu`, `cree_par_admin`, `visible`, `visible_from`, `resume_ai`) VALUES
(3, 'israa', 'hannachi', '<p class=\"MsoNormal\" style=\"text-align: center;\" align=\"center\"><strong style=\"mso-bidi-font-weight: normal;\"><span lang=\"EN-GB\" style=\"font-size: 27.0pt; line-height: 115%; color: #f1c232;\">Understand The Problem</span></strong></p>\n<p class=\"MsoNormal\"><span lang=\"EN-GB\" style=\"font-size: 14.0pt; line-height: 115%;\">&nbsp;</span></p>\n<p class=\"MsoNormal\"><span lang=\"EN-GB\" style=\"font-size: 14.0pt; line-height: 115%;\">&nbsp;</span></p>\n<p class=\"MsoNormal\"><span lang=\"EN-GB\" style=\"font-size: 14.0pt; line-height: 115%;\">&nbsp;</span></p>\n<p class=\"MsoListParagraph\" style=\"text-indent: -18.0pt; mso-list: l0 level1 lfo1;\"><!-- [if !supportLists]--><span lang=\"EN-GB\" style=\"font-size: 14.0pt; line-height: 115%; color: #073763;\"><span style=\"mso-list: Ignore;\">1-<span style=\"font: 7.0pt \'Times New Roman\';\">&nbsp;&nbsp; </span></span></span><!--[endif]--><span lang=\"EN-GB\" style=\"font-size: 14.0pt; line-height: 115%; color: #073763;\">Choose 3 problems justifying the question <strong style=\"mso-bidi-font-weight: normal;\">&ldquo; Why is it important? &ldquo;.</strong></span></p>\n<p class=\"MsoNormal\"><span lang=\"EN-GB\" style=\"font-size: 14.0pt; line-height: 115%; mso-bidi-font-weight: bold;\">Due to the spread of viral diseases resulting from the lack of clean drinking water among students in the state of Kairouan (Hajeb El Ayoun) by 57%. Which led to a decline in the educational level of students.</span></p>\n<p class=\"MsoNormal\"><strong style=\"mso-bidi-font-weight: normal;\"><span lang=\"EN-GB\" style=\"font-size: 14.0pt; line-height: 115%; color: #073763;\">&nbsp;</span></strong></p>\n<p class=\"MsoNormal\"><span lang=\"EN-GB\" style=\"font-size: 13.0pt; line-height: 115%; color: #073763;\">2- Why do you believe they are significant causes ?</span></p>\n<p class=\"MsoNormal\"><strong style=\"mso-bidi-font-weight: normal;\"><span lang=\"EN-GB\" style=\"font-size: 14.0pt; line-height: 115%;\">&nbsp;</span></strong></p>\n<p class=\"MsoNormal\"><span lang=\"EN-GB\" style=\"color: #0b5394;\">&nbsp;</span></p>', 60, 3, '2026-02-05 22:19:48', '2026-02-06 17:15:09', 1, 2, NULL, 0, 1, NULL, NULL),
(5, 'cause et consequences', 'dd', 'hello', 30, 3, '2026-02-06 15:58:53', '2026-02-20 17:48:03', 1, 5, NULL, 0, 1, NULL, NULL),
(7, 'coloration', NULL, 'aa', 2, 1, '2026-02-06 16:05:40', NULL, 1, 7, NULL, 0, 1, NULL, NULL),
(8, 'soisou', 'hana', '<h1>sdqjusucsf</h1>\n<ol>\n<li style=\"text-align: center;\"><strong><a title=\"choufli hal\" href=\"https://youtu.be/r2hBrZjrlnU?si=qj3Msm8E4HIkzjbe\"><img src=\"https://pennyappeal.ca/wp-content/uploads/2021/05/palestineemergency.jpg\" alt=\"palestine flag\" width=\"77\" height=\"52\"></a>&nbsp;</strong>\n<table style=\"border-collapse: collapse; width: 32.2722%; height: 133px;\" border=\"1\"><colgroup><col style=\"width: 8.3613%;\"><col style=\"width: 8.3613%;\"><col style=\"width: 16.6346%;\"><col style=\"width: 16.6346%;\"><col style=\"width: 16.6346%;\"><col style=\"width: 16.6346%;\"><col style=\"width: 16.6346%;\"></colgroup>\n<tbody>\n<tr>\n<td>&nbsp;</td>\n<td>bdffbddf</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n</tr>\n<tr>\n<td>&nbsp;</td>\n<td>fdvdfvf</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n</tr>\n<tr>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n</tr>\n<tr>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n</tr>\n<tr>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n</tr>\n</tbody>\n</table>\n</li>\n<li style=\"text-align: center;\"><a title=\"choufli hal\" href=\"https://youtu.be/r2hBrZjrlnU?si=qj3Msm8E4HIkzjbe\">https://youtu.be/r2hBrZjrlnU?si=qj3Msm8E4HIkzjbe</a></li>\n<li style=\"text-align: center;\">&nbsp;</li>\n</ol>', 45, 2, '2026-02-06 16:56:08', '2026-02-06 17:11:45', 1, 2, NULL, 0, 1, NULL, NULL),
(10, 'piles et files', NULL, NULL, 32, 0, '2026-02-06 19:34:00', NULL, 1, 6, 'workshop-integration-templates-6986341924054.pdf', 0, 1, NULL, NULL),
(11, 'gestion des disques', NULL, '<p>greshrhtythththtydhy</p>\n<h1>fdbfxbfngn</h1>\n<p><strong>bfdbfbfbf<em>fdffbfbfbfddf<span style=\"text-decoration: underline;\">fdfbfbgfbnfgg</span></em></strong></p>\n<p style=\"text-align: center;\"><strong><em><span style=\"text-decoration: underline;\">bonjour&nbsp;<a title=\"choufli hal\" href=\"https://www.youtube.com/watch?v=NKRiODF5jzg\" target=\"_blank\" rel=\"noopener\">https://www.youtube.com/watch?v=NKRiODF5jzg</a></span></em></strong></p>\n<p>&nbsp;</p>\n<p style=\"text-align: center;\">&nbsp;</p>\n<p style=\"text-align: center;\">&nbsp;</p>\n<p style=\"text-align: center;\"><strong><em><span style=\"text-decoration: underline;\"><img src=\"https://cdn.imgai.ai/imageai-page/photo-magic-after-01.webp\" alt=\"mona\" width=\"150\" height=\"150\"></span></em></strong></p>\n<p style=\"text-align: center;\">&nbsp;</p>\n<p style=\"text-align: center;\"><strong><em><span style=\"text-decoration: underline;\">bthrfhfhnhhtrgegereggetrg</span></em></strong></p>', 60, 3, '2026-02-09 12:48:14', '2026-02-09 13:05:59', 1, 9, NULL, 0, 1, NULL, NULL),
(12, 'friendship', 'important', '<p style=\"text-align: center;\">bonjour mes eleves&nbsp;</p>\n<p style=\"text-align: center;\">voici votre cours</p>\n<p style=\"text-align: center;\">merci de regarder cette video&nbsp;</p>\n<p style=\"text-align: center;\"><a title=\"friendship\" href=\"https://www.youtube.com/watch?v=0b1sPJq1BaA\" target=\"_blank\" rel=\"noopener\">https://www.youtube.com/watch?v=0b1sPJq1BaA</a></p>\n<p><img style=\"display: block; margin-left: auto; margin-right: auto;\" src=\"https://watermark.lovepik.com/photo/50085/3715.jpg_wh1200.jpg\" alt=\"friendship\" width=\"437\" height=\"291\"></p>\n<table style=\"border-collapse: collapse; width: 99.983%;\" border=\"1\"><colgroup><col style=\"width: 33.305%;\"><col style=\"width: 33.305%;\"><col style=\"width: 33.305%;\"></colgroup>\n<tbody>\n<tr>\n<td>avis&nbsp;</td>\n<td>verbs</td>\n<td>description</td>\n</tr>\n<tr>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n<td>&nbsp;</td>\n</tr>\n</tbody>\n</table>', 45, 1, '2026-02-09 22:11:14', '2026-02-14 15:16:12', 1, 10, NULL, 0, 1, NULL, NULL),
(14, 'les tableaux', NULL, '<p>s</p>', 9, 1, '2026-02-14 18:55:50', NULL, 1, 6, NULL, 0, 1, NULL, NULL),
(16, 'calque', NULL, NULL, 2, 1, '2026-02-14 20:53:09', NULL, 1, 8, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771098793/gestion-cours/cours/1-6990d2a63a5b3.pdf', 0, 1, NULL, NULL),
(19, 'chaine de caractere', NULL, NULL, 8, 4, '2026-02-14 22:46:52', NULL, 1, 6, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771105615/gestion-cours/cours/Workshop-lesformulaire-CS-25-26-1-6990ed4ce22ce.pdf', 1, 1, NULL, NULL),
(20, 'gestion des utilisateurs', NULL, NULL, 2, 0, '2026-02-20 14:37:30', NULL, 1, 9, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771594655/gestion-cours/cours/CH3-LID-6998639b2add2.pdf', 0, 1, NULL, NULL),
(21, 'ubuntu', 'foody', NULL, 5, 2, '2026-02-20 15:40:46', NULL, 1, 9, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771598453/gestion-cours/cours/CH3-LID-6998639b2add2-699872717cd64.pdf', 0, 1, NULL, NULL),
(22, 'le controle de saisie', NULL, NULL, 6, 2, '2026-02-20 17:34:49', NULL, 1, 6, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771605301/gestion-cours/cours/cours-9-69988d2a2d22c.pdf', 0, 1, '2026-02-20 17:35:00', NULL),
(24, 'base de donnÃ©es', NULL, NULL, 4, 0, '2026-02-20 23:55:02', NULL, 1, 9, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771628106/gestion-cours/cours/CH3-LID-6998e647165c0.pdf', 0, 1, '2026-02-20 23:52:00', NULL),
(25, 'bundle', NULL, NULL, 90, 1, '2026-02-24 09:57:24', NULL, 1, 14, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771923452/gestion-cours/cours/Chapitre3-Controleur-et-routage-699d67f663a25.pdf', 0, 1, '2026-02-24 09:58:00', NULL),
(26, 'API', NULL, NULL, 12, 1, '2026-02-24 10:41:09', NULL, 1, 14, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771926115/gestion-cours/cours/Chapitre7-Translation-d-adresses-699d7236b5008.pptx', 1, 1, '2026-02-24 10:25:00', NULL),
(27, 'VLAN', NULL, NULL, 63, 2, '2026-02-24 11:08:18', '2026-02-24 11:09:50', 1, 13, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771927702/gestion-cours/cours/Chapitre12-Reseaux-VLAN-699d78933e120.pdf', 1, 1, '2026-02-24 11:09:00', NULL),
(28, 'validation', NULL, NULL, 13, 3, '2026-02-24 15:06:25', NULL, 1, 13, 'https://res.cloudinary.com/dbkehfwxu/raw/upload/v1771941995/gestion-cours/cours/Chapitre12-Reseaux-VLAN-699db0641dc8e.pptx', 0, 1, NULL, NULL),
(29, 'validation2', NULL, NULL, 77, 1, '2026-03-03 08:56:19', NULL, 1, 14, 'CH5-ML-2425-S2-4-1-69a6a233a5258.pdf', 0, 1, '2026-03-03 09:56:00', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `cours_categorie`
--

CREATE TABLE `cours_categorie` (
  `id` int(11) NOT NULL,
  `nom` varchar(50) NOT NULL,
  `description` longtext DEFAULT NULL,
  `date_creation` datetime NOT NULL,
  `actif` tinyint(4) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `cours_categorie`
--

INSERT INTO `cours_categorie` (`id`, `nom`, `description`, `date_creation`, `actif`) VALUES
(1, 'informatique', '5eme', '2026-02-05 19:35:00', 1),
(3, 'cloud', '4 twin', '2026-02-05 19:39:00', 1),
(4, 'genie civil', 'qwerty', '2026-02-05 22:16:50', 0),
(7, 'infographie', 'design', '2026-02-06 16:03:11', 1),
(8, 'math', 'z', '2026-02-06 16:03:29', 1),
(10, 'nids', NULL, '2026-02-06 19:23:47', 1),
(11, 'anglais', 'communication', '2026-02-09 22:04:44', 1),
(12, 'validation', NULL, '2026-02-10 11:08:53', 1),
(13, 'test', NULL, '2026-02-24 09:36:30', 1);

-- --------------------------------------------------------

--
-- Structure de la table `cours_module`
--

CREATE TABLE `cours_module` (
  `id` int(11) NOT NULL,
  `titre` varchar(100) NOT NULL,
  `description` longtext DEFAULT NULL,
  `duree` int(11) NOT NULL,
  `niveau` varchar(20) NOT NULL,
  `date_creation` datetime NOT NULL,
  `actif` tinyint(4) NOT NULL,
  `categorie_id` int(11) NOT NULL,
  `cree_par_admin` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `cours_module`
--

INSERT INTO `cours_module` (`id`, `titre`, `description`, `duree`, `niveau`, `date_creation`, `actif`, `categorie_id`, `cree_par_admin`) VALUES
(2, 'hello', 'cc', 24, 'IntermÃ©diaire', '2026-02-05 22:17:29', 1, 4, 0),
(5, 'francais', 'communication', 3, 'AvancÃ©', '2026-02-06 15:55:32', 1, 3, 0),
(6, 'algo', 'rr', 4, 'DÃ©butant', '2026-02-06 16:01:16', 1, 1, 0),
(7, 'photoshop', 'adobe', 2, 'DÃ©butant', '2026-02-06 16:04:12', 1, 7, 0),
(8, 'illustrator', 'adobe', 3, 'AvancÃ©', '2026-02-06 16:04:39', 1, 7, 0),
(9, 'unix', NULL, 64, 'DÃ©butant', '2026-02-06 19:22:54', 1, 1, 0),
(10, 'CCCA2', '2 eme annÃ©e', 48, 'AvancÃ©', '2026-02-09 22:05:59', 1, 11, 0),
(13, 'symfony', NULL, 45, 'DÃ©butant', '2026-02-10 11:09:27', 1, 12, 0),
(14, 'projet', NULL, 38, 'AvancÃ©', '2026-02-24 09:36:59', 1, 13, 1);

-- --------------------------------------------------------

--
-- Structure de la table `doctrine_migration_versions`
--

CREATE TABLE `doctrine_migration_versions` (
  `version` varchar(191) NOT NULL,
  `executed_at` datetime DEFAULT NULL,
  `execution_time` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;

--
-- Déchargement des données de la table `doctrine_migration_versions`
--

INSERT INTO `doctrine_migration_versions` (`version`, `executed_at`, `execution_time`) VALUES
('DoctrineMigrations\\Version20260218181142', '2026-02-18 18:11:53', 1509);

-- --------------------------------------------------------

--
-- Structure de la table `email_queue`
--

CREATE TABLE `email_queue` (
  `id` int(11) NOT NULL,
  `recipient_email` varchar(255) NOT NULL,
  `recipient_name` varchar(255) DEFAULT NULL,
  `subject` varchar(255) NOT NULL,
  `body` longtext NOT NULL,
  `status` varchar(20) NOT NULL,
  `variables` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`variables`)),
  `created_at` datetime NOT NULL,
  `sent_at` datetime DEFAULT NULL,
  `error_message` longtext DEFAULT NULL,
  `attempts` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Structure de la table `email_templates`
--

CREATE TABLE `email_templates` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `template_path` varchar(255) NOT NULL,
  `description` longtext DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `email_templates`
--

INSERT INTO `email_templates` (`id`, `name`, `subject`, `template_path`, `description`, `created_at`, `updated_at`) VALUES
(1, 'password_reset', 'Reset Your Password - Code: {{ code }}', 'emails/password_reset.html.twig', 'Template for password reset emails with 6-digit code', '2026-02-20 13:46:58', '2026-02-20 13:46:58'),
(2, 'welcome', 'Welcome to Naja7ni, {{ name }}!', 'emails/welcome.html.twig', 'Welcome email for new users', '2026-02-20 13:46:59', '2026-02-20 13:46:59');

-- --------------------------------------------------------

--
-- Structure de la table `events`
--

CREATE TABLE `events` (
  `id` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` longtext NOT NULL,
  `date_creation` datetime NOT NULL DEFAULT current_timestamp(),
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `capacite` int(11) NOT NULL DEFAULT 50,
  `inscrits` int(11) NOT NULL DEFAULT 0,
  `image` varchar(300) DEFAULT NULL,
  `categorie` varchar(50) NOT NULL,
  `prix` decimal(8,2) NOT NULL DEFAULT 0.00,
  `lieu` varchar(250) NOT NULL,
  `latitude` decimal(10,6) DEFAULT NULL,
  `longitude` decimal(10,6) DEFAULT NULL,
  `statut` varchar(50) NOT NULL,
  `time_zone` varchar(50) NOT NULL DEFAULT 'UTC',
  `is_recurring` tinyint(1) NOT NULL DEFAULT 0,
  `recurrence_frequency` varchar(50) DEFAULT NULL,
  `recurrence_count` int(11) DEFAULT NULL,
  `attendees_emails` longtext DEFAULT NULL,
  `organizer_email` varchar(255) DEFAULT NULL,
  `target_audience` varchar(255) DEFAULT NULL,
  `required_level` varchar(255) DEFAULT NULL,
  `tags` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`tags`)),
  `notes_interne` longtext DEFAULT NULL,
  `ical_id` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `events`
--

INSERT INTO `events` (`id`, `titre`, `description`, `date_creation`, `date_debut`, `date_fin`, `capacite`, `inscrits`, `image`, `categorie`, `prix`, `lieu`, `latitude`, `longitude`, `statut`, `time_zone`, `is_recurring`, `recurrence_frequency`, `recurrence_count`, `attendees_emails`, `organizer_email`, `target_audience`, `required_level`, `tags`, `notes_interne`, `ical_id`) VALUES
(1, 'Test Event 1', 'This is a test event', '2026-02-18 19:20:20', '2026-02-19 19:20:20', '2026-02-19 19:20:20', 50, 1, NULL, 'Programming', 0.00, 'Room A', NULL, NULL, 'planifié', 'UTC', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(2, 'Web Development with Symfony', 'Master modern web development using Symfony framework', '2026-02-18 19:20:28', '2026-02-20 19:20:28', '2026-02-20 19:20:28', 30, 2, NULL, 'Web Development', 49.00, 'Room B', NULL, NULL, 'planifié', 'UTC', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(3, 'Advanced React Patterns', 'Deep dive into React best practices', '2026-02-18 19:20:36', '2026-02-21 19:20:36', '2026-02-21 19:20:36', 25, 0, NULL, 'Frontend', 39.00, 'Room C', NULL, NULL, 'planifié', 'UTC', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(4, 'Machine Learning Basics', 'Introduction to ML with Python', '2026-02-18 19:20:36', '2026-02-22 19:20:36', '2026-02-22 19:20:36', 35, 2, NULL, 'AI & ML', 59.00, 'Room D', NULL, NULL, 'planifié', 'UTC', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(5, 'Database Design Fundamentals', 'Learn efficient database design', '2026-02-18 19:20:36', '2026-02-23 19:20:36', '2026-02-23 19:20:36', 40, 2, NULL, 'Database', 29.00, 'Room E', NULL, NULL, 'planifié', 'UTC', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(6, 'tyguhjio', ':w:\r\nslf\r\nslflfs\r\nfl', '2026-02-18 19:50:21', '2026-02-26 20:49:00', '2026-02-27 20:49:00', 50, 1, 'assets/uploads/events/R-699617fd00960.jpg', 'workshop', 0.00, 'morneg', NULL, NULL, 'planifié', 'UTC', 1, 'Daily', 3, 'kmlksmfksfm@kkd.com', 'isra.debbabi@esprit.tn', NULL, NULL, NULL, NULL, NULL),
(7, 'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa', 'scscddddddddddddddddd', '2026-02-18 20:33:13', '2026-02-19 21:32:00', '2026-02-20 21:32:00', 50, 1, NULL, 'workshop', 0.00, 'morneg', 36.739513, 10.302946, 'planifié', 'Asia/Tokyo', 1, 'Daily', 5, 'xcxccccccccccccccc', 'isra.debbabi@esprit.tn', NULL, NULL, NULL, NULL, NULL),
(8, 'ccccccccccccccccccccccc', 'sfsffwwwwwwwwwwwwwwwwww', '2026-02-18 20:34:16', '2026-02-27 21:33:00', '2026-03-13 21:33:00', 50, 0, NULL, 'workshop', 0.00, 'morneg', 36.739513, 10.302946, 'planifié', 'Europe/London', 0, 'Weekly', 1, 'dkzkf@.f', 'isra.debbabi@esprit.tn', NULL, NULL, NULL, NULL, NULL),
(9, 'cccccccccccccccccccccccaaaaaaaaaaaaaaaa', ',,,,,,,,,,,,,,,ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff', '2026-02-18 20:34:57', '2026-02-18 21:34:00', '2026-02-26 21:34:00', 50, 2, NULL, 'workshop', 0.00, 'morneg', 36.739513, 10.302946, 'planifié', 'UTC', 0, 'Weekly', 1, ',kj@kjf.ml', 'isra.debbabi@esprit.tn', NULL, NULL, NULL, NULL, NULL),
(10, 'sssssssssssssssssssssssssssssssss', 'xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx', '2026-02-18 20:35:35', '2026-02-21 21:35:00', '2026-03-05 21:35:00', 50, 0, NULL, 'workshop', 0.00, 'morneg', 36.739513, 10.302946, 'planifié', 'UTC', 0, 'Daily', 1, 'kwlk@,lkc,s.com', 'isra.debbabi@esprit.tn', NULL, NULL, NULL, NULL, NULL),
(12, 'Test AI Form', 'Description for AI magic and submit test', '2026-02-27 18:20:44', '2026-03-10 10:00:00', '2026-03-10 12:00:00', 120, 0, NULL, 'Technologie', 35.00, 'Tunis', 36.806500, 10.181500, 'planifié', 'UTC', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL),
(13, 'Formation à Tunis', 'formation python workshop machine learning', '2026-02-27 21:17:01', '2026-02-27 18:00:00', '2026-02-27 22:00:00', 50, 1, 'assets/uploads/events/ChatGPT-Image-8-fevr-2026-10-00-17-removebg-preview-69a1fbbd460af.png', 'Formation', 0.00, 'Tunis, Tunisie', 33.843941, 9.400138, 'planifié', 'UTC', 0, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL, NULL);

-- --------------------------------------------------------

--
-- Structure de la table `event_chats`
--

CREATE TABLE `event_chats` (
  `id` int(11) NOT NULL,
  `event_id` int(11) DEFAULT NULL,
  `user_id` int(11) DEFAULT NULL,
  `sender` varchar(50) NOT NULL,
  `message` longtext NOT NULL,
  `created_at` datetime NOT NULL,
  `updated_at` datetime DEFAULT NULL,
  `visibility` varchar(50) NOT NULL DEFAULT 'public',
  `likes` int(11) DEFAULT NULL,
  `metadata` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`metadata`)),
  `is_ai_generated` tinyint(1) NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `event_posters`
--

CREATE TABLE `event_posters` (
  `id` int(11) NOT NULL,
  `event_id` int(11) DEFAULT NULL,
  `image_url` varchar(500) NOT NULL,
  `prompt` longtext NOT NULL,
  `style` varchar(50) NOT NULL,
  `generated_by` varchar(50) NOT NULL,
  `generated_at` datetime NOT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT 1,
  `download_count` int(11) DEFAULT NULL,
  `metadata` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`metadata`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `forum`
--

CREATE TABLE `forum` (
  `id` int(11) NOT NULL,
  `titre` varchar(200) NOT NULL,
  `description` longtext NOT NULL,
  `date_creation` date NOT NULL,
  `etat` varchar(50) NOT NULL,
  `created_by` varchar(200) NOT NULL,
  `categorie_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `forum`
--

INSERT INTO `forum` (`id`, `titre`, `description`, `date_creation`, `etat`, `created_by`, `categorie_id`) VALUES
(2, 'Chapitre 1: Résolution numérique de systèmes d\'équations linéaires', 'Forum dédié aux échanges et discussions autour des méthodes numériques de résolution des systèmes d\'équations linéaires (Gauss, LU, Jacobi, etc.).', '2026-02-12', 'actif', 'Anouer Tlil', 2),
(3, 'Chapitre 2 : Interpolation polynomiale', 'Forum dédié aux échanges et discussions sur les méthodes d\'interpolation polynomiale (Lagrange, Newton, différences divisées, etc.).', '2026-02-12', 'actif', 'Anouer Tlil', 2),
(4, 'Chapitre 3 : Résolutions numériques des équations non linéaires', 'Forum consacré aux échanges sur les méthodes numériques de résolution des équations non linéaires (dichotomie, Newton-Raphson, point fixe, etc.).', '2026-02-12', 'inactif', 'Anouer Tlil', 2),
(5, 'Chapitre 1: Protocole IPv4', 'Forum consacré à l\'étude du protocole IPv4, son fonctionnement, sa structure et son rôle dans les réseaux informatiques.', '2026-02-13', 'inactif', 'Adel Agrebi', 3),
(6, 'Chapitre 2: Segmentation à masque fixe', 'Forum dédié à la compréhension et à l\'application de la segmentation des réseaux IP à masque de sous-réseau fixe.', '2026-02-13', 'actif', 'Adel Agrebi', 3),
(7, 'Chapitre 3: Segmentation à masque variable', 'Forum consacré à l\'étude et à la mise en pratique de la segmentation des réseaux IP à masque variable (VLSM) pour optimiser l\'utilisation des adresses.', '2026-02-13', 'actif', 'Adel Agrebi', 3),
(8, 'Chapitre 1: Automates finis et expressions régulières', 'Forum consacré à l\'étude des automates finis déterministes et non déterministes ainsi qu\'à leur lien avec les expressions régulières.', '2026-02-14', 'actif', 'Naouel Boughattas', 4),
(9, 'Chapitre 2 : Les automates à pile', 'Forum dédié à l\'étude des automates à pile et à leur rôle dans la reconnaissance des langages non réguliers.', '2026-02-14', 'inactif', 'Naouel Boughattas', 4),
(10, 'Chapitre 3: Les grammaires', 'Forum consacré à l\'étude des grammaires formelles, de leurs types et de leur utilisation dans la définition des langages.', '2026-02-14', 'actif', 'Naouel Boughattas', 4),
(11, 'Chapitre 1: Finalités de l\'entreprise', 'Forum consacré à l\'étude des finalités économiques, sociales et sociétales de l\'entreprise.', '2026-02-15', 'inactif', 'Insaf Tekaya', 5),
(12, 'Chapitre 2: Fonctions de l\'entreprise', 'Forum dédié à l\'étude des différentes fonctions de l\'entreprise et de leur rôle dans son organisation.', '2026-02-15', 'inactif', 'Insaf Tekaya', 5);

-- --------------------------------------------------------

--
-- Structure de la table `game`
--

CREATE TABLE `game` (
  `id` int(11) NOT NULL,
  `titre` varchar(255) NOT NULL,
  `type` varchar(255) NOT NULL,
  `niveau` varchar(255) NOT NULL,
  `score_max` int(11) NOT NULL,
  `last_score` int(11) DEFAULT NULL,
  `avg_score` double DEFAULT NULL,
  `duration` int(11) DEFAULT NULL,
  `attempt_number` int(11) NOT NULL,
  `created_at` datetime NOT NULL,
  `course_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `game`
--

INSERT INTO `game` (`id`, `titre`, `type`, `niveau`, `score_max`, `last_score`, `avg_score`, `duration`, `attempt_number`, `created_at`, `course_id`) VALUES
(14, 'analyse financi?re : ratio de liquidit?', 'qcm', 'd?butant', 5, 20, 13.75, 120, 3, '2026-02-07 11:36:09', 0),
(15, 'communication et culture : droits de l homme', 'vraie ou faux', 'd?butant', 10, 10, 10, 240, 3, '2026-02-07 11:39:29', 0),
(16, 'strategie de l entreprise : d?veloppement durable', 'qcm', 'intermediare', 10, NULL, NULL, 240, 3, '2026-02-07 11:41:01', 0),
(17, 'G?nie logiciel / uml', 'qcm', 'd?butant', 20, 10, 8.875, 360, 3, '2026-02-07 11:46:20', 0),
(18, 'Programmation mobile:Android', 'vraie', 'd?butant', 10, 0, 0, 240, 3, '2026-02-07 11:49:07', 0),
(19, 'projet java : Servlets', 'qcm', 'intermediare', 10, 1, 1, 360, 3, '2026-02-07 11:50:47', 0),
(20, 'base donn?es :SQL', 'qcm', 'intermediare', 30, 10, 5.25, 320, 3, '2026-02-07 11:53:20', 0),
(21, 'estimation pour l ing?nieur: Marge d erreur', 'libre', 'intermediare', 30, 0, 1.3502807617188, 320, 3, '2026-02-07 11:56:33', 0),
(22, 'mathematique : equation du premier degr?', 'qcm', 'intermediare', 30, 10, 5, 320, 3, '2026-02-07 12:01:36', 0),
(23, 'informatique de base : systeme d exploitation', 'qcm', 'intermediare', 30, 0, 0, 120, 3, '2026-02-07 12:03:29', 0),
(24, 'introduction a l economie', 'vraie ou faux', 'd?butant', 50, 1, 1, 120, 3, '2026-02-07 12:05:37', 0),
(25, 'francais', 'qcm', 'd?butant', 5, 10, 5, 120, 2, '2026-02-07 12:07:19', 0),
(26, 'Unix: gestion disque', 'qcm', 'intermediare', 10, 10, 10, 10, 3, '2026-02-09 12:54:49', 0),
(27, 'validation', 'qcm', 'd?butant', 1, 20, 20, 20, 3, '2026-02-10 11:50:29', 0);

-- --------------------------------------------------------

--
-- Structure de la table `game_question`
--

CREATE TABLE `game_question` (
  `id` int(11) NOT NULL,
  `question_text` longtext NOT NULL,
  `option1` varchar(255) DEFAULT NULL,
  `option2` varchar(255) DEFAULT NULL,
  `option3` varchar(255) DEFAULT NULL,
  `option4` varchar(255) DEFAULT NULL,
  `correct_answer` varchar(255) NOT NULL,
  `game_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `game_question`
--

INSERT INTO `game_question` (`id`, `question_text`, `option1`, `option2`, `option3`, `option4`, `correct_answer`, `game_id`) VALUES
(18, 'la ratio de liquidit?? mesure', 'la rentabilit??', 'la capacit?? a rembourser les dettes ?? court terme', 'le niveau d\'endettement', 'la croissance des ventes', 'la capacit?? a rembourser les dettes ?? court terme', 14),
(19, 'la d??claration universelle  des droits de l\'homme a ete adopt??e en 1948', 'vraie', 'faux', NULL, NULL, 'vraie', 15),
(20, 'le concept de \"triple botttom line\" repose sur', 'profit ,plan??te , Personnes', 'Production ,Prix ,Publicit??', 'Croissance,Innovation ,Technologie', 'Capital,Travail,Ressources', 'profit ,plan??te , Personnes', 16),
(21, 'UML est un langage de ....?', 'Programmation', 'Mod??lisation', 'Compilation', 'D??bogage', 'Mod??lisation', 17),
(22, 'android utilise java comme langage principal ?', 'vraie', 'faux', NULL, NULL, 'vraie', 18),
(23, 'une Servlet java est ex??cut??e  ?', 'cote client', 'cote serveur', 'dans la base de donn??es', 'dans le navigateur', 'cote serveur', 19),
(24, 'SQl signifie ?', 'structured Query Language', 'simple query logic', 'System Query list', 'none', 'structured Query Language', 20),
(25, 'expliquer en une phrase ce que signifie \"marge d\'erreur\" en statistiques', NULL, NULL, NULL, NULL, 'intervalle qui exprime l\'incertitude d\'une estimation', 21),
(26, 'La solution de l?????quation  2 ???? + 4 = 10  est??? ?', '2', '3', '4', '5', '2', 22),
(27, 'un syst??me d\'exploitation linux est ??', 'un logiciel proprietaire', 'un logiciel libre', 'un langage de programmation', 'un navigateur web', 'un logicel proprietaire', 23),
(28, 'demande et offre : selon la loi de l\'offre et de la demande si le prix augmente , la demande diminue ?', 'vraie', 'faux', NULL, NULL, 'vraie', 24),
(29, 'quelle est la bonne orthographe', 'developpement', 'devellopement', 'd??veloppemnt', 'd??veloppement', 'd??veloppement', 25),
(30, 'qu\'elle est la commande utiliser pour afficher les disques ?', 'gdisk', 'ldisk', 'rdisk', 'fdisk', 'fdisk', 26),
(31, 'Combien font 10*10?', '10', '50', '0', '100', '100', 27),
(32, 'Combien font 50*50?', '2500', '50', '0', '100', '2500', 27);

-- --------------------------------------------------------

--
-- Structure de la table `meet`
--

CREATE TABLE `meet` (
  `id` int(11) NOT NULL,
  `titre` varchar(150) NOT NULL,
  `description` longtext DEFAULT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `lien_meet` varchar(255) DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `participant_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `meet`
--

INSERT INTO `meet` (`id`, `titre`, `description`, `date_debut`, `date_fin`, `lien_meet`, `created_at`, `participant_id`) VALUES
(11, 'cour_diag', 'aaa', '2026-02-08 23:08:00', '2026-02-09 23:08:00', NULL, '2026-02-08 23:09:13', 3),
(21, 'aaaaaaaaaa', NULL, '2026-02-09 17:14:00', '2026-02-10 17:14:00', NULL, '2026-02-09 17:14:39', 3),
(24, '555555', NULL, '2026-02-09 17:28:00', '2026-02-10 17:28:00', NULL, '2026-02-09 17:28:28', 3),
(25, '999', NULL, '2026-02-18 17:30:00', '2026-02-27 17:30:00', NULL, '2026-02-09 17:30:42', 3),
(27, 'bbbbbbbbbb', NULL, '2026-02-09 17:38:00', '2026-02-09 20:38:00', NULL, '2026-02-09 17:39:10', 3),
(28, 'cours_music', NULL, '2026-02-10 18:30:00', '2026-02-16 18:30:00', NULL, '2026-02-09 18:30:27', 7),
(29, 'cours_uml', NULL, '2026-02-10 19:51:00', '2026-02-11 19:51:00', NULL, '2026-02-09 19:51:46', 7),
(31, 'cours_ds', NULL, '2026-02-10 20:59:00', '2026-02-10 21:59:00', NULL, '2026-02-09 20:59:43', 3),
(32, 'bbbbbbbbbbb', NULL, '2026-02-09 21:13:00', '2026-02-10 21:13:00', NULL, '2026-02-09 21:13:47', 3),
(33, 'cours_unix', 'aaaaaaaaaaaa', '2026-02-09 21:45:00', '2026-02-10 21:45:00', NULL, '2026-02-09 21:46:08', 3),
(34, 'cours-phy', NULL, '2026-02-11 22:37:00', '2026-02-13 22:37:00', NULL, '2026-02-09 22:38:00', 7),
(35, 'cours_histo', 'maintenant', '2026-02-09 22:54:00', '2026-02-09 23:02:00', NULL, '2026-02-09 22:56:04', 3),
(37, 'cours-java', 'urgent urgent', '2026-02-10 11:18:00', '2026-02-10 12:18:00', NULL, '2026-02-10 11:19:07', 3),
(38, 'cours_geo', 'il aura un ds', '2026-02-16 20:50:00', '2026-02-17 20:50:00', NULL, '2026-02-16 20:50:52', 3),
(42, 'cours_reci1', NULL, '2026-02-19 21:52:00', '2026-02-21 21:52:00', NULL, '2026-02-19 21:52:33', 3),
(45, 'test', 'aa', '2026-02-21 14:55:00', '2026-02-22 14:55:00', NULL, '2026-02-21 14:55:51', 10),
(46, 'test1', NULL, '2026-02-21 14:59:00', '2026-02-22 14:59:00', NULL, '2026-02-21 14:59:42', 10),
(47, 'test2', NULL, '2026-02-21 15:10:00', '2026-02-24 15:10:00', NULL, '2026-02-21 15:11:34', 11),
(48, 'cours_tla', NULL, '2026-02-24 11:22:00', '2026-02-25 11:22:00', NULL, '2026-02-24 11:22:28', 12),
(49, 'cours_tla', NULL, '2026-02-24 11:24:00', '2026-02-26 11:24:00', NULL, '2026-02-24 11:24:27', 10),
(50, 'cours_java', NULL, '2026-02-24 11:26:00', '2026-02-25 11:26:00', NULL, '2026-02-24 11:26:44', 12),
(51, 'cours_math', NULL, '2026-02-24 11:31:00', '2026-02-25 11:32:00', NULL, '2026-02-24 11:32:14', 12),
(52, 'cour_fr', NULL, '2026-02-24 11:39:00', '2026-02-26 11:39:00', NULL, '2026-02-24 11:39:26', 10),
(53, 'cour_fr', NULL, '2026-02-24 11:41:00', '2026-02-25 11:41:00', NULL, '2026-02-24 11:42:04', 14),
(54, 'cours_math', NULL, '2026-02-24 11:44:00', '2026-02-25 11:44:00', NULL, '2026-02-24 11:45:08', 11),
(55, 'cour_analyse', NULL, '2026-02-24 12:30:00', '2026-02-26 12:30:00', NULL, '2026-02-24 12:31:23', 15),
(56, 'cour_analyse', NULL, '2026-02-24 12:34:00', '2026-02-25 12:34:00', NULL, '2026-02-24 12:35:39', 11),
(57, 'cour_analyse', NULL, '2026-02-24 12:34:00', '2026-02-25 12:34:00', NULL, '2026-02-24 12:38:47', 11),
(58, 'cour_analyse', NULL, '2026-02-24 12:34:00', '2026-02-25 12:34:00', NULL, '2026-02-24 12:40:43', 11),
(59, 'cour_analyse', NULL, '2026-02-24 12:34:00', '2026-02-25 12:34:00', NULL, '2026-02-24 12:41:12', 11),
(60, 'cour_analyse', NULL, '2026-02-24 12:34:00', '2026-02-25 12:34:00', NULL, '2026-02-24 12:42:06', 11),
(61, 'cour_analyse', NULL, '2026-02-24 12:34:00', '2026-02-25 12:34:00', NULL, '2026-02-24 12:42:58', 11),
(62, 'test2', NULL, '2026-02-24 12:43:00', '2026-02-25 12:43:00', NULL, '2026-02-24 12:43:57', 11),
(63, 'test2', NULL, '2026-02-24 12:43:00', '2026-02-25 12:43:00', NULL, '2026-02-24 12:45:27', 11),
(64, 'cours_math', NULL, '2026-02-24 12:50:00', '2026-02-25 12:50:00', NULL, '2026-02-24 12:50:44', 11),
(65, 'cours_math', NULL, '2026-02-25 12:54:00', '2026-02-26 12:54:00', NULL, '2026-02-24 12:54:39', 11),
(66, 'cours_java', NULL, '2026-02-24 15:03:00', '2026-02-25 15:03:00', NULL, '2026-02-24 15:03:22', 11),
(67, 'integ', 'hsgfdh', '2026-03-03 02:28:00', '2026-03-03 07:28:00', NULL, '2026-03-03 02:28:17', 12),
(68, 'integ', 'hsgfdh', '2026-03-03 02:28:00', '2026-03-03 07:28:00', NULL, '2026-03-03 02:29:28', 12),
(69, 'integhh', 'gqsdg', '2026-03-03 02:35:00', '2026-03-03 07:35:00', NULL, '2026-03-03 02:35:42', 14),
(70, 'integhhjj', 'gfdg', '2026-03-03 02:48:00', '2026-03-03 07:48:00', NULL, '2026-03-03 02:48:21', 3),
(71, 'integhhjj', 'lkhjl', '2026-03-03 02:49:00', '2026-03-03 07:49:00', NULL, '2026-03-03 02:49:53', 11),
(72, 'integhhjjv26', 'bgdf', '2026-03-03 02:51:00', '2026-03-05 02:51:00', NULL, '2026-03-03 02:51:56', 11),
(73, 'cours_math', NULL, '2026-03-03 10:01:00', '2026-03-03 12:01:00', NULL, '2026-03-03 09:01:35', 11);

-- --------------------------------------------------------

--
-- Structure de la table `meet_participants`
--

CREATE TABLE `meet_participants` (
  `meet_id` int(11) NOT NULL,
  `participant_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `meet_participants`
--

INSERT INTO `meet_participants` (`meet_id`, `participant_id`) VALUES
(11, 4),
(21, 6),
(24, 6),
(25, 6),
(27, 6),
(28, 6),
(29, 4),
(29, 6),
(31, 4),
(31, 6),
(32, 6),
(33, 4),
(33, 6),
(35, 4),
(35, 6),
(37, 4),
(37, 6),
(38, 4),
(38, 6),
(42, 4),
(42, 6),
(45, 4),
(45, 6),
(46, 6),
(47, 4),
(47, 6),
(48, 4),
(49, 4),
(49, 6),
(50, 4),
(50, 6),
(51, 4),
(51, 6),
(52, 4),
(55, 4),
(56, 4),
(56, 6),
(57, 4),
(57, 6),
(58, 4),
(58, 6),
(59, 4),
(59, 6),
(60, 4),
(60, 6),
(61, 4),
(61, 6),
(62, 4),
(63, 4),
(64, 4),
(64, 6),
(65, 4),
(66, 4),
(67, 4),
(67, 6),
(67, 16),
(68, 4),
(68, 6),
(68, 16),
(69, 4),
(69, 6),
(69, 16),
(70, 4),
(70, 6),
(70, 16),
(71, 4),
(71, 6),
(71, 16),
(72, 4),
(72, 6),
(72, 16),
(73, 4),
(73, 6),
(73, 16);

-- --------------------------------------------------------

--
-- Structure de la table `message`
--

CREATE TABLE `message` (
  `id` int(11) NOT NULL,
  `contenu` longtext NOT NULL,
  `date_publication` date NOT NULL,
  `etat` varchar(200) NOT NULL,
  `created_by` varchar(200) NOT NULL,
  `forum_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `message`
--

INSERT INTO `message` (`id`, `contenu`, `date_publication`, `etat`, `created_by`, `forum_id`) VALUES
(1, 'Bonjour monsieur, je n\'ai pas bien compris la m??thode de Gauss. Est-ce qu\'on doit toujours transformer la matrice en forme triangulaire sup??rieure avant de faire la substitution arri??re ?', '2026-02-28', 'Actif', '??tudiante-Israa Hannechi', 2),
(2, 'Bonne question Israa. Les deux m??thodes permettent de r??soudre un syst??me lin??aire, mais Gauss transforme la matrice en forme triangulaire, tandis que Gauss-Jordan va jusqu\'?? la forme ??chelonn??e r??duite. Le r??sultat est le m??me si le syst??me a une solution unique.', '2026-03-03', 'Actif', 'Monsieur-Anouer Tlili', 2),
(3, 'Monsieur, comment savoir si un syst??me d\'??quations lin??aires admet une solution unique avant de faire tous les calculs ?', '2026-02-22', 'Actif', '??tudiante -Imen Ghamlouli', 2),
(4, 'Imene, on peut v??rifier si le d??terminant de la matrice des coefficients est diff??rent de z??ro. Si c\'est le cas, le syst??me admet une solution unique.', '2026-02-22', 'Actif', 'Monsieur-Anouer Tlili', 2),
(5, 'Pour la m??thode de jacobi, est-ce qu\'il y a une condition pour garantir la convergence?', '2026-02-22', 'Actif', '??tudiant-Chahine Mezni', 2),
(6, 'Oui Chahine, la m??thode de Jacobi converge gén??ralement si la matrice est strictement diagonale dominante. Pensez toujours ?? v??rifier cette condition avant d\'appliquer la m??thode.', '2026-02-28', 'Actif', 'Monsieur-Anouer Tlili', 2),
(7, 'Bonjour monsieur, je ne comprends pas bien la diff??rence entre l\'interpolation de Lagrange et celle de Newton. Est-ce qu\'elles donnent toujours le m??me polyn??me ?', '2026-02-22', 'Actif', '??tudiante-Israa Dabbebi', 3),
(8, 'Oui Israa, elles donnent le m??me polyn??me d\'interpolation, mais la forme de Newton est plus pratique lorsqu\'on ajoute un nouveau point, car on n\'a pas besoin de tout recalculer.', '2026-02-22', 'Actif', 'Monsieur-Anouer Tlili', 3),
(9, 'Est-ce que l\'erreur d\'interpolation d??pend uniquement du degr?? du polyn??me?', '2026-02-22', 'Actif', '??tudiante-Miriam Kouki', 3),
(10, 'Non, elle d??pend aussi de la fonction ??tudi??e et de la r??partition des points. Une mauvaise distribution des points peut augmenter l\'erreur m??me avec un degr?? adapt??.', '2026-02-22', 'Actif', 'Monsieur-Anouer Tlili', 3),
(11, 'Merci monsieur.', '2026-02-22', 'Actif', '??tudiante-Israa Dabbebi', 3),
(12, 'Merci beaucoup monsieur.', '2026-02-22', 'Actif', '??tudiante-Miriam Kouki', 3),
(13, 'Bonjour monsieur, je ne comprends pas quand utiliser la m??thode de dichotomie au lieu de la m??thode de Newton.', '2026-02-22', 'Actif', '??tudiante-Imen Ghamlouli', 4),
(14, 'La m??thode de dichotomie est plus simple et garantit la convergence si la fonction change de signe sur l\'intervalle. La m??thode de Newton est plus rapide, mais elle n??cessite la d??riv??e et un bon choix de valeur initiale.', '2026-02-22', 'Actif', 'Monsieur-Anouer Tlil', 4),
(15, 'Monsieur, que se passe-t-il si la d??riv??e ets proche de z??ro dans la m??thode de Newton?', '2026-02-22', 'Actif', '??tudiante-Farah Jemmali', 4),
(16, 'Dans ce cas, la m??thode peut diverger ou donner des r??sultats incorrects. Il faut v??rifier les conditions avant de l\'appliquer.', '2026-02-22', 'Actif', 'Monsieur- Anouer Tlil', 4),
(17, 'Est-ce que la m??thode du point fixe converge toujours?', '2026-02-22', 'Actif', '??tudiant-Chahine Mezni', 4),
(18, 'Non, elle converge seulement si la fonction v??rifie certaines conditions, notamment si la valeur absolue de la d??riv??e est inf??rieure ?? 1 dans l\'intervalle ??tudi??.', '2026-02-22', 'Actif', 'Monsieur- Anouer Tlil', 4),
(20, 'Bonjour monsieur, je ne comprends pas bien la diff??rence entre une adresse IP publique et une adresse IP priv??e en IPv4.', '2026-02-22', 'Actif', '??tudiant-Chahine Mezni', 5),
(21, 'Bonne question. Une adresse IP publique est routable sur Internet, tandis qu\'une adresse priv??e est utilis??e dans un r??seau local et n\'est pas directement accessible depuis Internet.', '2026-02-22', 'Actif', 'Monsieur Adel Agrebi', 5),
(22, 'Monsieur, est-ce que le protocole IPv4 permet d\'identifier ?? la fois le r??seau et l\'h??te?', '2026-02-22', 'Actif', '??tudiante-Miriam Kouki', 5),
(23, 'Oui ,une adresse IPv4 est compos??e d\'une partie r??seau et d\'une partie h??te, d??termin??es par le masque de sous-r??seau.', '2026-02-22', 'Actif', 'Monsieur Adel Agrebi', 5),
(24, 'Merci monsieur.', '2026-02-22', 'Actif', '??tudiant-Chahine Mezni', 5),
(25, 'Merci pour votre aide Monsieur.', '2026-02-22', 'Actif', '??tudiante-Miriam Kouki', 5),
(26, 'Monsieur, je ne sais pas comment d??terminer le nombre de sous-r??seaux possibles avec un masque fixe.', '2026-02-22', 'Actif', '??tudiante-Imen Ghamlouli', 6),
(27, 'Pour cela, il faut regarder le nombre de bits emprunt??s ?? la partie h??te. Le nombre de sous-r??seaux est ??gal ?? 2 puissance le nombre de bits emprunt??s.', '2026-02-22', 'Actif', 'Monsieur Adel Agrebi', 6),
(28, 'Monsieur,est-ce que tous les sous-r??seaux obtenus avec FLSM ont le m??me nombre d\'h??tes ?', '2026-02-22', 'Actif', '??tudiante-Israa Hannechi', 6),
(29, 'Oui,chaque sous-r??seau poss??de le m??me masque et donc le m??me nombre d\'adresses disponibles.', '2026-02-22', 'Actif', 'Monsieur Adel Agrebi', 6),
(30, 'Bonjour, je trouve le VLSM un peu compliqu??. Par quoi faut-il commencer pour bien r??partir les sous-r??seaux ?', '2026-02-22', 'Actif', '??tudiante-Farah Jemmali', 7),
(31, 'Il faut d\'abord classer les sous-r??seaux par ordre d??croissant selon le nombre d\'h??tes n??cessaires, puis attribuer les masques les plus grands en premier.', '2026-02-22', 'Actif', 'Monsieur Adel Agrebi', 7),
(32, 'Monsieur, est-ce que le VLSM permet d\'??viter le gaspillage d\'adresses IP ?', '2026-02-22', 'Actif', '??tudiant-Chahine Mezni', 7),
(33, 'Oui, c\'est son principal avantage. Le VLSM adapte le masque selon les besoins r??els de chaque sous-r??seau.', '2026-02-22', 'Actif', 'Monsieur Adel Agrebi', 7),
(34, 'Bonjour madame, je ne comprends pas bien la diff??rence entre un automate fini d??terministe (AFD) et un automate fini non d??terministe (AFN).Pouvez-vous m\'expliquer?', '2026-02-22', 'Actif', '??tudiante-Israa Dabbebi', 8),
(35, 'Bonne question. Dans un AFD, pour chaque ??tat et chaque symbole, il existe une seule transition possible. Dans un AFN, il peut y avoir plusieurs transitions ou aucune pour un m??me symbole.', '2026-02-22', 'Actif', 'Madame Naouel Boughattas', 8),
(36, 'Quel est le lien avec les automates finis et les expressions r??guli??res ?', '2026-02-22', 'Actif', '??tudiante-Miriam Kouki', 8),
(37, 'Les automates finis et les expressions r??guli??res d??crivent exactement les m??mes langages, appel??s langages r??guliers.', '2026-02-22', 'Actif', 'Madame Naouel Boughattas', 8),
(39, 'Bonjour, pourquoi un automate fini ne peut pas reconna??tre le langage des parenth??ses bien ??quilibr??es ?', '2026-02-22', 'Actif', '??tudiante-Israa Hannechi', 9),
(40, 'Parce qu\'un automate fini n\'a pas de m??moire suffisante. Un automate ?? pile, gr??ce ?? sa pile, peut m??moriser les symboles ouverts et v??rifier leur correspondance.', '2026-02-22', 'Actif', 'Madame Naouel Boughattas', 9),
(41, 'Madame, est-ce que tous les langages reconnus par un automate ?? pile sont contextuels ?', '2026-02-22', 'Actif', '??tudiante-Miriam Kouki', 9),
(42, 'Les automates ?? pile reconnaissent exactement les langages hors-contexte.', '2026-02-22', 'Actif', 'Madame Naouel Boughattas', 9),
(43, 'Bonjour madame , je ne comprends pas bien la diff??rence entre une grammaire r??guli??re et une grammaire hors-contexte.', '2026-02-22', 'Actif', '??tudiante-Farah Jemmali', 10),
(44, 'Une grammaire r??guli??re g??n??re des langages r??guliers et correspond aux automates finis. Une grammaire hors-contexte est plus puissante et correspond aux automates ?? pile.', '2026-02-22', 'Actif', 'Madame Naouel Boughattas', 10),
(45, 'Madame, ?? quoi sert la forme normale de Chomsky ?', '2026-02-22', 'Actif', '??tudiante-Imen Ghamlouli', 10),
(46, 'Elle permet de simplifier l\'analyse des grammaires et est souvent utilis??e dans les algorithmes d\'analyse syntaxique.', '2026-02-22', 'Actif', 'Madame Naouel Boughattas', 10),
(47, 'Es-ce que toutes les grammaires peuvent ??tre transform??es en forme normale ?', '2026-02-22', 'Actif', '??tudiante-Israa Dabbebi', 10),
(48, 'Madame, est-ce que la finalit?? d\'une entreprise est uniquement de r??aliser un profit ?', '2026-02-22', 'Actif', '??tudiante-Israa Hannechi', 11),
(49, 'Non, le profit est une finalit?? ??conomique importante, mais l\'entreprise a aussi des finalit??s sociales (emploi, conditions de travail) et soci??tales (respect de l\'environnement, responsabilit?? sociale).', '2026-02-22', 'Actif', 'Madame Insaf Tekaya', 11),
(50, 'Madame, est-ce que les finalit??s changent selon le type d\'entreprise, par exemple publique ou priv??e ?', '2026-02-22', 'Actif', '??tudiante-Imen Ghamlouli', 11),
(51, 'Oui, une entreprise publique peut accorder plus d\'importance ?? l\'int??r??t g??n??ral, tandis qu\'une entreprise priv??e met souvent l\'accent sur la rentabilit??.', '2026-02-22', 'Actif', 'Madame Insaf Tekaya', 11),
(52, 'Bonjour madame, quelles sont les principales fonctions dans une entreprise ?', '2026-02-22', 'Actif', '??tudiante-Israa Hannechi', 12),
(53, 'Les principales fonctions sont g??n??ralement la production, la fonction commerciale, la fonction financi??re, la fonction ressources humaines et la direction.', '2026-02-22', 'Actif', 'Madame Insaf Tekaya', 12),
(54, 'Madame, quel est exactement le r??le de la fonction de direction ?', '2026-02-22', 'Actif', '??tudiant-Chahine Mezni', 12),
(55, 'La direction fixe les objectifs, prend les d??cisions strat??giques et coordonne les diff??rentes fonctions de l\'entreprise.', '2026-02-22', 'Actif', 'Madame Insaf Tekaya', 12),
(57, 'aaa', '2026-03-03', 'Actif', 'admin admin', 2);

-- --------------------------------------------------------

--
-- Structure de la table `messenger_messages`
--

CREATE TABLE `messenger_messages` (
  `id` bigint(20) NOT NULL,
  `body` longtext NOT NULL,
  `headers` longtext NOT NULL,
  `queue_name` varchar(190) NOT NULL,
  `created_at` datetime NOT NULL,
  `available_at` datetime NOT NULL,
  `delivered_at` datetime DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `notification`
--

CREATE TABLE `notification` (
  `id` int(11) NOT NULL,
  `title` varchar(255) NOT NULL,
  `message` longtext NOT NULL,
  `type` varchar(20) NOT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `notification`
--

INSERT INTO `notification` (`id`, `title`, `message`, `type`, `created_at`) VALUES
(1, 'Compte modifi?', 'Le compte tasnimmezni123@gmail.com a ?t? mis ? jour.', 'info', '2026-02-08 12:50:18'),
(2, 'Compte modifi?', 'Le compte majdomri@gmail.com a ?t? mis ? jour.', 'info', '2026-02-09 16:21:28'),
(3, 'Nouveau compte', 'Le compte isradabebi@gmail.com a ?t? cr?? avec succ?s.', 'success', '2026-02-09 16:22:50'),
(4, 'Compte modifi?', 'Le compte isradabebi@gmail.com a ?t? mis ? jour.', 'info', '2026-02-10 11:16:00'),
(5, 'Compte modifi?', 'Le compte isradabebi@gmail.com a ?t? mis ? jour.', 'info', '2026-02-10 11:16:15'),
(6, 'Compte modifi?', 'Le compte isradabebi@gmail.com a ?t? mis ? jour.', 'info', '2026-02-10 11:16:29'),
(7, 'Compte modifi?', 'Le compte isradabebi@gmail.com a ?t? mis ? jour.', 'info', '2026-02-10 11:16:29'),
(8, 'Nouveau compte', 'Le compte PI@gmail.com a ?t? cr?? avec succ?s.', 'success', '2026-02-10 12:15:04'),
(9, 'Compte modifi?', 'Le compte PI@gmail.com a ?t? mis ? jour.', 'info', '2026-02-10 12:16:10'),
(10, 'Compte modifi?', 'Le compte chahinemezni77@gmail.com a ?t? mis ? jour.', 'info', '2026-02-10 12:17:04'),
(11, 'Compte modifi?', 'Le compte 3wichka@gmail.com a ?t? mis ? jour.', 'info', '2026-02-10 14:56:09'),
(12, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 10:29:52'),
(13, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 10:31:17'),
(14, 'Compte modifi?', 'Le compte chahinemezni77@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 10:31:31'),
(15, 'Compte modifi?', 'Le compte chahinemezni77@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 11:06:01'),
(16, 'Compte modifi?', 'Le compte chahinemezni77@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 11:07:04'),
(17, 'Compte modifi?', 'Le compte chahinemezni77@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 11:09:22'),
(18, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 11:13:02'),
(19, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 11:14:05'),
(20, 'Compte modifi?', 'Le compte chahinemezni77@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 11:14:40'),
(21, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 11:25:48'),
(22, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 11:29:54'),
(23, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 11:58:16'),
(24, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 12:02:07'),
(25, 'Compte modifi?', 'Le compte chahine.mezni@esprit.tn a ?t? mis ? jour.', 'info', '2026-02-19 12:04:30'),
(26, 'Nouveau compte', 'Le compte mriglinentreprise@gmail.com a ?t? cr?? avec succ?s.', 'success', '2026-02-19 12:43:44'),
(27, 'Compte modifi?', 'Le compte mriglinentreprise@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 12:44:33'),
(28, 'Compte modifi?', 'Le compte mriglinentreprise@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 12:45:32'),
(29, 'Compte modifi?', 'Le compte test_1770383627@example.com a ?t? mis ? jour.', 'info', '2026-02-19 20:52:16'),
(30, 'Compte modifi?', 'Le compte test_1770383627@example.com a ?t? mis ? jour.', 'info', '2026-02-19 20:52:49'),
(31, 'Compte modifi?', 'Le compte test_1770383627@example.com a ?t? mis ? jour.', 'info', '2026-02-19 22:11:37'),
(32, 'Compte modifi?', 'Le compte test_1770383627@example.com a ?t? mis ? jour.', 'info', '2026-02-19 22:17:38'),
(33, 'Compte modifi?', 'Le compte tasnimmezni123@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 22:31:57'),
(34, 'Compte modifi?', 'Le compte tasnimmezni123@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 22:33:54'),
(35, 'Compte modifi?', 'Le compte tasnimmezni123@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 22:34:31'),
(36, 'Nouveau compte', 'Le compte tasnimmezni123@gmail.com a ?t? cr?? avec succ?s.', 'success', '2026-02-19 22:36:43'),
(37, 'Compte modifi?', 'Le compte tasnimmezni123@gmail.com a ?t? mis ? jour.', 'info', '2026-02-19 22:37:33'),
(38, 'Compte modifi?', 'Le compte test_1770383627@example.com a ?t? mis ? jour.', 'info', '2026-02-20 14:55:50'),
(39, 'Compte modifi?', 'Le compte test_1770383627@example.com a ?t? mis ? jour.', 'info', '2026-02-20 15:02:01'),
(40, 'Compte modifi?', 'Le compte test_1770383627@example.com a ?t? mis ? jour.', 'info', '2026-02-20 16:16:57'),
(41, 'Nouveau compte', 'Le compte sihamdi@gmail.com a été créé avec succès.', 'success', '2026-03-01 02:20:51'),
(42, 'Compte modifié', 'Le compte sihamdi@gmail.com a été mis à jour.', 'info', '2026-03-01 02:21:18'),
(43, 'Compte modifié', 'Le compte 3wichka@gmail.com a été mis à jour.', 'info', '2026-03-03 02:57:32'),
(44, 'Compte modifié', 'Le compte 3wichka@gmail.com a été mis à jour.', 'info', '2026-03-03 03:01:23'),
(45, 'Compte modifié', 'Le compte 3wichka@gmail.com a été mis à jour.', 'info', '2026-03-03 03:02:49'),
(46, 'Compte modifié', 'Le compte 3wichka@gmail.com a été mis à jour.', 'info', '2026-03-03 03:05:42'),
(47, 'Compte modifié', 'Le compte 3wichka@gmail.com a été mis à jour.', 'info', '2026-03-03 03:30:42'),
(48, 'Compte modifié', 'Le compte 3wichka@gmail.com a été mis à jour.', 'info', '2026-03-03 03:31:28'),
(49, 'Nouveau compte', 'Le compte ensignant@gmail.com a été créé avec succès.', 'success', '2026-03-03 08:42:10'),
(50, 'Nouveau compte', 'Le compte ETUDIANT@esprit.tn a été créé avec succès.', 'success', '2026-03-03 08:43:23'),
(51, 'Compte modifié', 'Le compte ETUDIANT@esprit.tn a été mis à jour.', 'info', '2026-03-03 08:44:09'),
(52, 'Compte modifié', 'Le compte ETUDIANT@esprit.tn a été mis à jour.', 'info', '2026-03-03 08:44:17'),
(53, 'Compte modifié', 'Le compte ETUDIANT@esprit.tn a été mis à jour.', 'info', '2026-03-03 08:46:08');

-- --------------------------------------------------------

--
-- Structure de la table `participant`
--

CREATE TABLE `participant` (
  `id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `prenom` varchar(100) NOT NULL,
  `email` varchar(255) NOT NULL,
  `role` varchar(100) NOT NULL,
  `created_at` datetime NOT NULL,
  `smtp_email` varchar(255) DEFAULT NULL,
  `smtp_app_password` varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `participant`
--

INSERT INTO `participant` (`id`, `nom`, `prenom`, `email`, `role`, `created_at`, `smtp_email`, `smtp_app_password`) VALUES
(3, 'israa', 'hannachi', 'israahannachi35@gmail.com', 'enseignant', '2026-02-06 19:30:05', NULL, NULL),
(4, 'alfa', 'alla', 'israahannachi35@gmail.com', 'etudiant', '2026-02-08 13:59:04', NULL, NULL),
(6, 'farah', 'alla', 'imenghamlouli1234@gmail.com', 'etudiant', '2026-02-09 16:21:56', NULL, NULL),
(7, 'salim', 'hmili', 'imenghamlouli1234@gmail.com', 'enseignant', '2026-02-09 18:29:38', NULL, NULL),
(10, 'ghamlouli', 'imen', 'imenghamlouli0000@gmail.com', 'enseignant', '2026-02-21 14:32:25', 'imenghamlouli0000@gmail.com', 'mjrz jvub ecuf brcd'),
(11, 'sawssen', 'ali', 'Ghamlouli.Imen@esprit.tn', 'enseignant', '2026-02-21 15:10:27', 'Ghamlouli.Imen@esprit.tn', 'dhaa agij dbsq brld'),
(12, 'Imen', 'Ghamlouli', 'imenghamlouli0000@gmail.com', 'enseignant', '2026-02-24 11:21:09', 'israahannachi35@gmail.com', 'mjrz jvub ecuf brcd'),
(13, 'Imen', 'Ghamlouli', 'imenghamlouli0000@gmail.com', 'enseignant', '2026-02-24 11:30:38', 'israahannachi35@gmail.com', 'mjrz jvub ecuf brcd'),
(14, 'imen', 'Ghamlouli', 'Ghamlouli.Imen@esprit.tn', 'enseignant', '2026-02-24 11:38:44', 'Ghamlouli.Imen@esprit.tn', 'dhaa agij dbsq brld'),
(15, 'imen', 'ghamlouli', 'imenghamlouli0000@gmail.com', 'enseignant', '2026-02-24 12:30:05', 'imenghamlouli0000@gmail.com', 'mjrz jvub ecuf brcd'),
(16, 'chahine', 'nawfel', 'test_1770383627@example.com', 'etudiant', '2026-03-03 02:26:54', 'chahine.mezni@esprit.tn', 'admin123');

-- --------------------------------------------------------

--
-- Structure de la table `rating`
--

CREATE TABLE `rating` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `stars` int(11) NOT NULL,
  `comment` longtext DEFAULT NULL,
  `created_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `rating`
--

INSERT INTO `rating` (`id`, `event_id`, `stars`, `comment`, `created_at`) VALUES
(1, 1, 4, 'sfs', '2026-02-18 18:21:21'),
(2, 13, 4, 'wlhi behi event 3jbni\r\n', '2026-02-27 23:10:54');

-- --------------------------------------------------------

--
-- Structure de la table `reclamation_cours`
--

CREATE TABLE `reclamation_cours` (
  `id` int(11) NOT NULL,
  `message` longtext NOT NULL,
  `created_at` datetime NOT NULL,
  `resolved` tinyint(4) NOT NULL,
  `resolved_at` datetime DEFAULT NULL,
  `cours_id` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `reclamation_cours`
--

INSERT INTO `reclamation_cours` (`id`, `message`, `created_at`, `resolved`, `resolved_at`, `cours_id`) VALUES
(1, 'j\'ai pas compris le cours', '2026-02-24 14:26:20', 0, NULL, 26),
(2, 'cccc', '2026-02-24 15:08:41', 1, '2026-02-24 15:09:16', 28);

-- --------------------------------------------------------

--
-- Structure de la table `recommendation_cache`
--

CREATE TABLE `recommendation_cache` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `match_score` double NOT NULL,
  `factor_scores` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`factor_scores`)),
  `explanations` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`explanations`)),
  `computed_at` datetime NOT NULL,
  `is_valid` tinyint(1) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `registrations`
--

CREATE TABLE `registrations` (
  `id` int(11) NOT NULL,
  `evenement_id` int(11) NOT NULL,
  `visitor_name` varchar(100) NOT NULL,
  `visitor_email` varchar(180) NOT NULL,
  `date_inscription` datetime NOT NULL DEFAULT current_timestamp(),
  `statut` varchar(50) NOT NULL,
  `presence` tinyint(1) NOT NULL DEFAULT 0,
  `mode_paiement` varchar(50) DEFAULT NULL,
  `notes` longtext DEFAULT NULL,
  `payment_token` varchar(255) DEFAULT NULL,
  `montant_paye` decimal(8,2) NOT NULL DEFAULT 0.00,
  `paiement_statut` varchar(50) NOT NULL DEFAULT 'en_attente'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Déchargement des données de la table `registrations`
--

INSERT INTO `registrations` (`id`, `evenement_id`, `visitor_name`, `visitor_email`, `date_inscription`, `statut`, `presence`, `mode_paiement`, `notes`, `payment_token`, `montant_paye`, `paiement_statut`) VALUES
(1, 1, 'mmglmglùm', 'sir809218@gmail.com', '2026-02-18 18:21:40', 'en_attente', 0, 'gratuit', 'mskfkfms', NULL, 0.00, 'en_attente'),
(2, 2, 'mmglmglùm', 'sir809218@gmail.com', '2026-02-18 18:31:57', 'confirmé', 0, 'gratuit', 'lmmmlk', NULL, 49.00, 'en_attente'),
(3, 9, 'ksfùmk', 'isra@isra.com', '2026-02-20 16:31:28', 'en_attente', 0, 'gratuit', NULL, NULL, 0.00, 'en_attente'),
(4, 5, 'isra', 'isra.debbabi@esprit.tn', '2026-02-21 10:59:57', 'confirmé', 0, 'carte', NULL, NULL, 29.00, 'en_attente'),
(11, 6, 'admin admin', 'test_1770383627@example.com', '2026-02-27 02:43:18', 'en_attente', 0, NULL, NULL, NULL, 0.00, 'en_attente'),
(12, 13, 'admin admin', 'test_1770383627@example.com', '2026-02-27 23:11:12', 'en_attente', 0, 'espece', NULL, NULL, 0.00, 'en_attente'),
(14, 7, 'admin admin', 'test_1770383627@example.com', '2026-02-27 23:13:37', 'en_attente', 0, 'paymee', NULL, NULL, 0.00, 'en_attente'),
(15, 4, 'admin admin', 'test_1770383627@example.com', '2026-02-27 23:46:18', 'confirmé', 0, 'espece', NULL, NULL, 0.00, 'en_attente'),
(16, 9, 'ensignant validation', 'ensignant@gmail.com', '2026-03-03 09:21:34', 'en_attente', 0, 'espece', NULL, NULL, 0.00, 'en_attente');

-- --------------------------------------------------------

--
-- Structure de la table `sponsors`
--

CREATE TABLE `sponsors` (
  `id` int(11) NOT NULL,
  `event_id` int(11) NOT NULL,
  `nom` varchar(100) NOT NULL,
  `description` longtext DEFAULT NULL,
  `logo` varchar(255) DEFAULT NULL,
  `site_web` varchar(255) DEFAULT NULL,
  `type` varchar(50) NOT NULL,
  `montant` decimal(10,2) NOT NULL,
  `date_debut` datetime NOT NULL,
  `date_fin` datetime NOT NULL,
  `statut` varchar(50) NOT NULL,
  `contact_personne` varchar(150) DEFAULT NULL,
  `contact_email` varchar(180) DEFAULT NULL,
  `contact_telephone` varchar(20) DEFAULT NULL,
  `date_creation` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `email` varchar(150) NOT NULL,
  `password` varchar(255) NOT NULL,
  `first_name` varchar(100) NOT NULL,
  `last_name` varchar(100) NOT NULL,
  `profile_picture` varchar(255) DEFAULT NULL,
  `profession` varchar(100) DEFAULT NULL,
  `experience_level` varchar(50) DEFAULT NULL,
  `role` varchar(20) NOT NULL,
  `statut` varchar(20) NOT NULL DEFAULT 'ACTIF',
  `date_creation` datetime NOT NULL DEFAULT current_timestamp(),
  `cover_picture` varchar(255) DEFAULT NULL,
  `reset_token` varchar(10) DEFAULT NULL,
  `reset_token_expires_at` datetime DEFAULT NULL,
  `google_authenticator_secret` varchar(255) DEFAULT NULL,
  `biometric_descriptor` longtext DEFAULT NULL,
  `user_preference_profile` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`user_preference_profile`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Déchargement des données de la table `users`
--

INSERT INTO `users` (`id`, `email`, `password`, `first_name`, `last_name`, `profile_picture`, `profession`, `experience_level`, `role`, `statut`, `date_creation`, `cover_picture`, `reset_token`, `reset_token_expires_at`, `google_authenticator_secret`, `biometric_descriptor`, `user_preference_profile`) VALUES
(1, 'test_1770383627@example.com', '$2y$13$uikXTrlC4PcecRC.HtahDO1OA3yy4YWZaebxgq3U4aLFmLjySvGEe', 'admin', 'admin', NULL, NULL, NULL, 'ADMIN', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, '46ZVWAH3TOMJ5BXVEKJINVU3PTPV3D2K4YFTCGNVH2NIWUGGTEJA', NULL, NULL),
(3, 'chahine.mezni@esprit.tn', '$2y$13$jsjafaFUvI65oDgSosBvpu6I2s.90OIv35gbjcNu4uCIHI7Sm9ej6', 'chahine ', 'mezni', NULL, NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, '992744', '2026-02-21 01:04:52', NULL, NULL, NULL),
(5, 'aziz@gmail.com', '$2y$13$rj0xcndSacXN0/CKSqoPRue6IHZfUYos7kZBGA2Nir3DSf8wEc602', 'aziz', 'abdalllah', NULL, NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, NULL, NULL),
(6, 'majdomri@gmail.com', '$2y$13$xIDL.5qSUulrkpnHp47vj.J1CoNnx/bX3TPyhZtz2ODldxwRRwWrO', 'majd', 'OMRI', 'ChatGPT-Image-6-fevr-2026-21-58-17-6989fb78ca3da.png', NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, NULL, NULL),
(7, '3wichka@gmail.com', '$2y$13$AzhAcTd2CXuUURb/HiQIFukM8rwGiFe/LYF8C9ZkHSm9YepbDLURa', 'si', '3wichka', 'chbrouch-6998f43fbe271.jpg', NULL, NULL, 'Enseignant', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, '[0.008038132451474667,0.035112783312797546,0.11899705231189728,0.025449855253100395,-0.06484661251306534,-0.041075803339481354,0.005752589553594589,-0.09433592110872269,0.14999449253082275,-0.14964675903320312,0.18133552372455597,-0.030171513557434082,-0.22473761439323425,-0.022781677544116974,-0.056045711040496826,0.13681428134441376,-0.10689987987279892,-0.12579041719436646,-0.031100334599614143,-0.09158680588006973,0.07527181506156921,0.02037428878247738,0.02089550904929638,0.056203532963991165,-0.12914349138736725,-0.34320610761642456,-0.08262460678815842,-0.10854124277830124,0.056320495903491974,-0.08628308027982712,-0.1093587651848793,0.07173870503902435,-0.13522310554981232,-0.00710049644112587,-0.03321601822972298,0.04061955213546753,-0.019535992294549942,-0.00879360269755125,0.14490395784378052,-0.006226266734302044,-0.20711910724639893,0.02833082526922226,0.046792276203632355,0.2230580896139145,0.18532313406467438,0.12821847200393677,0.05031922087073326,-0.0820891410112381,0.08988907188177109,-0.22271282970905304,0.10455898940563202,0.07297166436910629,0.14556649327278137,0.11096782982349396,0.020516404882073402,-0.25001493096351624,0.009202180430293083,-0.023664917796850204,-0.18430988490581512,0.1107100322842598,0.01859947293996811,-0.07741768658161163,0.04042932391166687,0.06774918735027313,0.25310564041137695,0.06811979413032532,-0.0917513445019722,-0.07166161388158798,0.184129998087883,-0.25840404629707336,-0.021070728078484535,0.054323069751262665,-0.08374418318271637,-0.14926084876060486,-0.24784909188747406,0.004811488091945648,0.4744771122932434,0.14783474802970886,-0.16479691863059998,0.056324586272239685,-0.08891171962022781,-0.014929194003343582,0.11931214481592178,0.10754646360874176,-0.08004772663116455,0.03506224602460861,-0.12126044929027557,0.03071768209338188,0.19843362271785736,-0.049556031823158264,-0.06683545559644699,0.2300378531217575,0.01928338222205639,0.11414817720651627,0.048191823065280914,0.06802596151828766,-0.11161565780639648,0.04853430762887001,-0.0802900642156601,-0.024153999984264374,0.009635872207581997,-0.06274129450321198,0.00810093991458416,0.06266210973262787,-0.19256733357906342,0.23250310122966766,-0.018494540825486183,0.020633861422538757,0.007611846551299095,0.0952388346195221,-0.10776036232709885,-0.04069285839796066,0.13341879844665527,-0.2652207016944885,0.1836269348859787,0.1844545304775238,0.05105949193239212,0.1908152848482132,0.03370223566889763,0.08458220213651657,0.03492310643196106,-0.09526108205318451,-0.17777316272258759,-0.0355863943696022,0.06491805613040924,-0.0990736111998558,0.09717366099357605,0.10232549160718918]', NULL),
(10, 'FARAHJEMMALI@GMAIL.COM', '$2y$13$KOFe27u3L/sVSOOSjXXVuuBe/DVReRT8Vm6k4M6lM9CcfFWKPTrxO', 'FARAH', 'JEMMALI', 'portrait-teenage-beautiful-girl-high-260nw-2656282593-removebg-preview-69890ed474ae4.png', NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, NULL, NULL),
(11, 'chahinemezni77@gmail.com', '$2y$13$.L9sUjCkuEYUyqcd6MrRL.7huvdW1fNR97MO95KhGrJUbWijR1Veu', 'Chahine mezni', 'User', 'Capture-decran-2024-07-07-175804-6998f300e81b5.png', NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, NULL, NULL),
(12, 'isradabebi@gmail.com', '$2y$13$xz9lgkz4Eprs.2pFd6x79uWj/40hKgPMLyvTW/9uYAe5SJhVhZjUq', 'israa', 'dabebi', 'telecharge-1-698b056f493ca.jpg', NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, NULL, NULL),
(13, 'PI@gmail.com', '$2y$13$4XVq7IYp7/H.vNvMSdAzgOJAmlgT6.vuxQu3.ssPuuBFbXrlhqZ7u', 'VALID??', 'PI', NULL, NULL, NULL, 'ETUDIANT', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, NULL, NULL),
(14, 'mriglinentreprise@gmail.com', '$2y$13$GZ9Gs12CKog2Ee0IzB1C7OdtcdxpjtSozDJGYCRwtrzYGPKRpvwaq', 'naja7ni', 'User', NULL, NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, NULL, NULL),
(15, 'tasnimmezni123@gmail.com', '$2y$13$fynPYfPRePVy6HVTa2LGTOlKBuVbxy6wKOZzcck1xXBNW9176RojG', 'tasnim', 'MEZNI', NULL, NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, NULL, NULL, NULL, '[-0.10286315530538559,-0.004723262973129749,0.03736884519457817,-0.02682521939277649,-0.10068270564079285,-0.028789140284061432,-0.03467469662427902,-0.09841185063123703,0.11352241784334183,-0.12115956842899323,0.17469973862171173,-0.0634806677699089,-0.24760793149471283,0.029206279665231705,-0.08882290124893188,0.19270437955856323,-0.07059603929519653,-0.20521850883960724,-0.10012982040643692,-0.002517521381378174,0.009225605055689812,-0.013789650984108448,0.024120943620800972,0.13847675919532776,-0.16777801513671875,-0.27722907066345215,-0.12303417176008224,-0.14271500706672668,-0.0915473997592926,-0.008566899225115776,-0.07776765525341034,-0.004999314434826374,-0.1408996731042862,-0.014250051230192184,0.0821094736456871,0.07640830427408218,-0.0003315345384180546,-0.046940721571445465,0.19356761872768402,0.018213240429759026,-0.29823505878448486,0.11318328231573105,0.06373754888772964,0.24281002581119537,0.19048327207565308,0.07839208841323853,0.019725259393453598,-0.1286347359418869,0.059976670891046524,-0.2200949341058731,0.06405292451381683,0.14000587165355682,0.12364695221185684,0.06873252242803574,0.016537681221961975,-0.12143506854772568,0.04833576828241348,0.12278124690055847,-0.1956700086593628,0.022752003744244576,0.03946515917778015,-0.0021420810371637344,0.01857021264731884,-0.09045577049255371,0.21043233573436737,0.12311271578073502,-0.1785481721162796,-0.14508453011512756,0.15805043280124664,-0.2573779821395874,-0.07579819858074188,0.0984441339969635,-0.14922009408473969,-0.2364983707666397,-0.2763448655605316,0.019481122493743896,0.4333426058292389,0.22694601118564606,-0.1802442967891693,0.060897160321474075,-0.045077309012413025,0.034114159643650055,0.022774066776037216,0.11865490674972534,-0.03899948298931122,0.04138714820146561,-0.030483465641736984,-0.03675948083400726,0.22012090682983398,-0.017170781269669533,-0.020190944895148277,0.23534852266311646,0.008930032141506672,0.020822931081056595,0.05227312818169594,0.07663273811340332,-0.05442790687084198,-0.028104808181524277,-0.11606902629137039,0.0052548665553331375,-0.07163587212562561,0.004643052816390991,0.05145846679806709,0.05448654294013977,-0.1889072209596634,0.13254892826080322,-0.082975372672081,-0.034079935401678085,-0.055150002241134644,0.12991102039813995,-0.08283903449773788,-0.04739851877093315,0.14054584503173828,-0.28107041120529175,0.15649725496768951,0.1969107985496521,0.058729179203510284,0.07661938667297363,0.10726348310709,0.05139688029885292,-0.01474712323397398,0.00015490129590034485,-0.19506895542144775,-0.07996329665184021,0.10476946085691452,-0.08878722041845322,0.14656974375247955,0.04696575552225113]', NULL),
(16, 'adem.dragon5@gmail.com', '$2y$13$6GstrXApj7wSv1CzVoLrSuG1wL8ggTYyne/oLEpONXdzkTTVOaRYq', 'raidh', 'nawfel', NULL, NULL, NULL, 'Student', 'ACTIF', '2026-02-27 02:12:07', NULL, '396516', '2026-02-21 01:11:28', NULL, NULL, NULL),
(17, 'sihamdi@gmail.com', '$2y$13$76LpY982KmYn02poRAJaIeHYD.IH9PZICA.hN8c6v459roU5v0oii', 'si', 'hamdi', 'Kang-English-Teacher-02-GALLERY-EVAN-0028-removebg-preview-69a3948e592c5.png', NULL, NULL, 'Enseignant', 'ACTIF', '2026-03-01 02:20:50', NULL, NULL, NULL, NULL, NULL, NULL),
(18, 'ensignant@gmail.com', '$2y$13$gx4giF8nw2Ltmlh4LfrRcO85NikcHCn1KZtLMOYk.eOpf5Tp4lxtO', 'ensignant', 'validation', NULL, NULL, NULL, 'Enseignant', 'ACTIF', '2026-03-03 08:42:09', NULL, NULL, NULL, NULL, NULL, NULL),
(19, 'ETUDIANT@esprit.tn', '$2y$13$BbXA7ovmenEroU1DTdANBu75mqJEQb9qykLi1QwibrrABOJmU4H4S', 'ETUDIAJNT', 'VALIDATION', 'images-1-69a69f595577a.png', NULL, NULL, 'Student', 'ACTIF', '2026-03-03 08:43:23', NULL, NULL, NULL, 'FRME4FZAMWDC4OTEJM5PQ2V2QLDUJJ7IYS5GUAMX3WIFFDAHDKLA', '[-0.026978764683008194,0.05495193228125572,0.1253519356250763,-0.01514617633074522,-0.08547165989875793,-0.05264494940638542,-0.08734564483165741,-0.14632323384284973,0.16090407967567444,-0.17723941802978516,0.19618889689445496,-0.0016741659492254257,-0.16254158318042755,-0.0036178003065288067,-0.058635130524635315,0.13333886861801147,-0.216193288564682,-0.1500050276517868,0.024943655356764793,-0.11485357582569122,0.04792868345975876,0.021213706582784653,-0.029881464317440987,0.030808312818408012,-0.15495729446411133,-0.3029024302959442,-0.1721799522638321,-0.1172867864370346,0.13429875671863556,-0.04830656573176384,-0.06290512531995773,0.04808183014392853,-0.10613448172807693,-0.013759700581431389,0.035571638494729996,0.03423401713371277,-0.0456421934068203,-0.036115407943725586,0.18452346324920654,0.0374060720205307,-0.11649372428655624,0.058142535388469696,-0.002213800325989723,0.2765217423439026,0.16028353571891785,0.11577428132295609,0.045782677829265594,-0.0852031260728836,0.13176655769348145,-0.22385258972644806,0.06575578451156616,0.15280748903751373,0.16064336895942688,0.1438264548778534,0.044495295733213425,-0.1842970848083496,0.07794389128684998,0.005716485902667046,-0.21725083887577057,0.11959294974803925,0.06253188848495483,-0.09338687360286713,0.043419741094112396,0.07300872355699539,0.2575429379940033,0.07437803596258163,-0.11959271132946014,-0.06168943643569946,0.18576687574386597,-0.1942201554775238,-0.05122632905840874,0.01372254267334938,-0.1036464124917984,-0.1476651430130005,-0.23326197266578674,0.009984888136386871,0.5160267949104309,0.17601311206817627,-0.19068607687950134,0.06038424000144005,-0.050379879772663116,-0.03220098838210106,0.13764026761054993,0.12482532113790512,-0.12270909547805786,-0.006436409428715706,-0.06487059593200684,0.02061770111322403,0.17478695511817932,-0.033344678580760956,-0.040703099220991135,0.23884908854961395,0.047638773918151855,0.15728724002838135,0.07459980994462967,0.03796993941068649,-0.07642509043216705,-0.029340825974941254,-0.05367007851600647,-0.006341318599879742,-0.013530070893466473,-0.021665211766958237,0.012933729216456413,0.0919274240732193,-0.1378903090953827,0.25560858845710754,0.009455563500523567,-0.008582652546465397,-0.05293407291173935,0.09395705908536911,-0.10857588797807693,-0.04898446798324585,0.13844606280326843,-0.29494452476501465,0.16112473607063293,0.21127523481845856,0.02068731188774109,0.18408676981925964,0.032212480902671814,0.051683537662029266,-0.007086051627993584,-0.08021516352891922,-0.124077707529068,-0.07965598255395889,0.0281665101647377,-0.08669682592153549,0.050823748111724854,0.04963742941617966]', NULL);

-- --------------------------------------------------------

--
-- Structure de la table `user_event_interaction`
--

CREATE TABLE `user_event_interaction` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `event_id` int(11) DEFAULT NULL,
  `interaction_type` varchar(255) NOT NULL,
  `timestamp` datetime NOT NULL,
  `duration` int(11) DEFAULT NULL,
  `metadata` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL CHECK (json_valid(`metadata`))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- --------------------------------------------------------

--
-- Structure de la table `user_preference_profile`
--

CREATE TABLE `user_preference_profile` (
  `id` int(11) NOT NULL,
  `user_id` int(11) DEFAULT NULL,
  `preferred_categories` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`preferred_categories`)),
  `preferred_topics` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`preferred_topics`)),
  `preferred_difficulty` varchar(255) DEFAULT NULL,
  `preferred_days` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL CHECK (json_valid(`preferred_days`)),
  `activity_score` double NOT NULL,
  `profile_completeness` double NOT NULL,
  `last_computed_at` datetime NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Index pour les tables déchargées
--

--
-- Index pour la table `aimodel_adjustment`
--
ALTER TABLE `aimodel_adjustment`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `aiprediction`
--
ALTER TABLE `aiprediction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_3608490B71F7E88B` (`event_id`);

--
-- Index pour la table `categorie`
--
ALTER TABLE `categorie`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `cours`
--
ALTER TABLE `cours`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_FDCA8C9CAFC2B591` (`module_id`);

--
-- Index pour la table `cours_categorie`
--
ALTER TABLE `cours_categorie`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `cours_module`
--
ALTER TABLE `cours_module`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_C242628BCF5E72D` (`categorie_id`);

--
-- Index pour la table `doctrine_migration_versions`
--
ALTER TABLE `doctrine_migration_versions`
  ADD PRIMARY KEY (`version`);

--
-- Index pour la table `email_queue`
--
ALTER TABLE `email_queue`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `email_templates`
--
ALTER TABLE `email_templates`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_6023E2A55E237E06` (`name`);

--
-- Index pour la table `events`
--
ALTER TABLE `events`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_5387574AFF7747B4` (`titre`),
  ADD KEY `idx_events_date_debut` (`date_debut`),
  ADD KEY `idx_events_statut` (`statut`),
  ADD KEY `idx_events_categorie` (`categorie`),
  ADD KEY `idx_events_date_creation` (`date_creation`),
  ADD KEY `idx_events_lieu` (`lieu`(100));

--
-- Index pour la table `event_chats`
--
ALTER TABLE `event_chats`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_6C73A92371F7E88B` (`event_id`),
  ADD KEY `IDX_6C73A923A76ED395` (`user_id`);

--
-- Index pour la table `event_posters`
--
ALTER TABLE `event_posters`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_CDC7849871F7E88B` (`event_id`);

--
-- Index pour la table `forum`
--
ALTER TABLE `forum`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_852BBECDBCF5E72D` (`categorie_id`),
  ADD KEY `idx_forum_etat` (`etat`);

--
-- Index pour la table `game`
--
ALTER TABLE `game`
  ADD PRIMARY KEY (`id`),
  ADD KEY `idx_game_type` (`type`(50)),
  ADD KEY `idx_game_niveau` (`niveau`(50));

--
-- Index pour la table `game_question`
--
ALTER TABLE `game_question`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_1DB3B668E48FD905` (`game_id`);

--
-- Index pour la table `meet`
--
ALTER TABLE `meet`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_E9F6D3CE9D1C3019` (`participant_id`),
  ADD KEY `idx_meet_date_debut` (`date_debut`),
  ADD KEY `idx_meet_date_fin` (`date_fin`);

--
-- Index pour la table `meet_participants`
--
ALTER TABLE `meet_participants`
  ADD PRIMARY KEY (`meet_id`,`participant_id`),
  ADD KEY `IDX_4D90C6BF3BBBF66` (`meet_id`),
  ADD KEY `IDX_4D90C6BF9D1C3019` (`participant_id`);

--
-- Index pour la table `message`
--
ALTER TABLE `message`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_B6BD307F29CCBAD0` (`forum_id`),
  ADD KEY `idx_message_date_publication` (`date_publication`);

--
-- Index pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_75EA56E0FB7336F0E3BD61CE16BA31DBBF396750` (`queue_name`,`available_at`,`delivered_at`,`id`);

--
-- Index pour la table `notification`
--
ALTER TABLE `notification`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `participant`
--
ALTER TABLE `participant`
  ADD PRIMARY KEY (`id`);

--
-- Index pour la table `rating`
--
ALTER TABLE `rating`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_D889262271F7E88B` (`event_id`);

--
-- Index pour la table `reclamation_cours`
--
ALTER TABLE `reclamation_cours`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_20C6BFD27ECF78B0` (`cours_id`);

--
-- Index pour la table `recommendation_cache`
--
ALTER TABLE `recommendation_cache`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_8836B028A76ED395` (`user_id`),
  ADD KEY `IDX_8836B02871F7E88B` (`event_id`);

--
-- Index pour la table `registrations`
--
ALTER TABLE `registrations`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_53DE51E7FD02F13` (`evenement_id`),
  ADD KEY `idx_registrations_statut` (`statut`),
  ADD KEY `idx_registrations_visitor_email` (`visitor_email`(100)),
  ADD KEY `idx_registrations_date_inscription` (`date_inscription`);

--
-- Index pour la table `sponsors`
--
ALTER TABLE `sponsors`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_9A31550F71F7E88B` (`event_id`),
  ADD KEY `idx_sponsors_statut` (`statut`),
  ADD KEY `idx_sponsors_type` (`type`);

--
-- Index pour la table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_1483A5E9E7927C74` (`email`),
  ADD KEY `idx_users_statut` (`statut`),
  ADD KEY `idx_users_role` (`role`),
  ADD KEY `idx_users_date_creation` (`date_creation`);

--
-- Index pour la table `user_event_interaction`
--
ALTER TABLE `user_event_interaction`
  ADD PRIMARY KEY (`id`),
  ADD KEY `IDX_641B950CA76ED395` (`user_id`),
  ADD KEY `IDX_641B950C71F7E88B` (`event_id`);

--
-- Index pour la table `user_preference_profile`
--
ALTER TABLE `user_preference_profile`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `UNIQ_4DFF43E4A76ED395` (`user_id`);

--
-- AUTO_INCREMENT pour les tables déchargées
--

--
-- AUTO_INCREMENT pour la table `aimodel_adjustment`
--
ALTER TABLE `aimodel_adjustment`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `aiprediction`
--
ALTER TABLE `aiprediction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `categorie`
--
ALTER TABLE `categorie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT pour la table `cours`
--
ALTER TABLE `cours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=30;

--
-- AUTO_INCREMENT pour la table `cours_categorie`
--
ALTER TABLE `cours_categorie`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `cours_module`
--
ALTER TABLE `cours_module`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT pour la table `email_queue`
--
ALTER TABLE `email_queue`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `email_templates`
--
ALTER TABLE `email_templates`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `events`
--
ALTER TABLE `events`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `event_chats`
--
ALTER TABLE `event_chats`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `event_posters`
--
ALTER TABLE `event_posters`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `forum`
--
ALTER TABLE `forum`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=14;

--
-- AUTO_INCREMENT pour la table `game`
--
ALTER TABLE `game`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=28;

--
-- AUTO_INCREMENT pour la table `game_question`
--
ALTER TABLE `game_question`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=33;

--
-- AUTO_INCREMENT pour la table `meet`
--
ALTER TABLE `meet`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=74;

--
-- AUTO_INCREMENT pour la table `message`
--
ALTER TABLE `message`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=58;

--
-- AUTO_INCREMENT pour la table `messenger_messages`
--
ALTER TABLE `messenger_messages`
  MODIFY `id` bigint(20) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `notification`
--
ALTER TABLE `notification`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=54;

--
-- AUTO_INCREMENT pour la table `participant`
--
ALTER TABLE `participant`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT pour la table `rating`
--
ALTER TABLE `rating`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `reclamation_cours`
--
ALTER TABLE `reclamation_cours`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT pour la table `recommendation_cache`
--
ALTER TABLE `recommendation_cache`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `registrations`
--
ALTER TABLE `registrations`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT pour la table `sponsors`
--
ALTER TABLE `sponsors`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=20;

--
-- AUTO_INCREMENT pour la table `user_event_interaction`
--
ALTER TABLE `user_event_interaction`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT pour la table `user_preference_profile`
--
ALTER TABLE `user_preference_profile`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Contraintes pour les tables déchargées
--

--
-- Contraintes pour la table `aiprediction`
--
ALTER TABLE `aiprediction`
  ADD CONSTRAINT `FK_3608490B71F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`);

--
-- Contraintes pour la table `cours`
--
ALTER TABLE `cours`
  ADD CONSTRAINT `FK_FDCA8C9CAFC2B591` FOREIGN KEY (`module_id`) REFERENCES `cours_module` (`id`);

--
-- Contraintes pour la table `cours_module`
--
ALTER TABLE `cours_module`
  ADD CONSTRAINT `FK_C242628BCF5E72D` FOREIGN KEY (`categorie_id`) REFERENCES `cours_categorie` (`id`);

--
-- Contraintes pour la table `event_chats`
--
ALTER TABLE `event_chats`
  ADD CONSTRAINT `FK_6C73A92371F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`),
  ADD CONSTRAINT `FK_6C73A923A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `event_posters`
--
ALTER TABLE `event_posters`
  ADD CONSTRAINT `FK_CDC7849871F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`);

--
-- Contraintes pour la table `forum`
--
ALTER TABLE `forum`
  ADD CONSTRAINT `FK_852BBECDBCF5E72D` FOREIGN KEY (`categorie_id`) REFERENCES `categorie` (`id`);

--
-- Contraintes pour la table `game_question`
--
ALTER TABLE `game_question`
  ADD CONSTRAINT `FK_1DB3B668E48FD905` FOREIGN KEY (`game_id`) REFERENCES `game` (`id`);

--
-- Contraintes pour la table `meet`
--
ALTER TABLE `meet`
  ADD CONSTRAINT `FK_E9F6D3CE9D1C3019` FOREIGN KEY (`participant_id`) REFERENCES `participant` (`id`);

--
-- Contraintes pour la table `meet_participants`
--
ALTER TABLE `meet_participants`
  ADD CONSTRAINT `FK_MEET_PARTICIPANTS_MEET` FOREIGN KEY (`meet_id`) REFERENCES `meet` (`id`) ON DELETE CASCADE,
  ADD CONSTRAINT `FK_MEET_PARTICIPANTS_PARTICIPANT` FOREIGN KEY (`participant_id`) REFERENCES `participant` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `message`
--
ALTER TABLE `message`
  ADD CONSTRAINT `FK_B6BD307F29CCBAD0` FOREIGN KEY (`forum_id`) REFERENCES `forum` (`id`);

--
-- Contraintes pour la table `rating`
--
ALTER TABLE `rating`
  ADD CONSTRAINT `FK_D889262271F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`);

--
-- Contraintes pour la table `reclamation_cours`
--
ALTER TABLE `reclamation_cours`
  ADD CONSTRAINT `FK_20C6BFD27ECF78B0` FOREIGN KEY (`cours_id`) REFERENCES `cours` (`id`) ON DELETE CASCADE;

--
-- Contraintes pour la table `recommendation_cache`
--
ALTER TABLE `recommendation_cache`
  ADD CONSTRAINT `FK_8836B02871F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`),
  ADD CONSTRAINT `FK_8836B028A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `registrations`
--
ALTER TABLE `registrations`
  ADD CONSTRAINT `FK_53DE51E7FD02F13` FOREIGN KEY (`evenement_id`) REFERENCES `events` (`id`);

--
-- Contraintes pour la table `sponsors`
--
ALTER TABLE `sponsors`
  ADD CONSTRAINT `FK_9A31550F71F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`);

--
-- Contraintes pour la table `user_event_interaction`
--
ALTER TABLE `user_event_interaction`
  ADD CONSTRAINT `FK_641B950C71F7E88B` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`),
  ADD CONSTRAINT `FK_641B950CA76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);

--
-- Contraintes pour la table `user_preference_profile`
--
ALTER TABLE `user_preference_profile`
  ADD CONSTRAINT `FK_4DFF43E4A76ED395` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
