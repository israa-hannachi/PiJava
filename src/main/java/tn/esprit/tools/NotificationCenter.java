package tn.esprit.tools;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;
import tn.esprit.entities.cours.Cours;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.users.Users;
import tn.esprit.services.cours.CoursService;
import tn.esprit.services.event.EventService;
import tn.esprit.services.meet.MeetService;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class NotificationCenter {

    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter TIME_ONLY = DateTimeFormatter.ofPattern("HH:mm");

    private NotificationCenter() {
    }

    public static List<NotificationItem> collectNotifications(Users user) {
        Map<String, NotificationItem> items = new LinkedHashMap<>();
        collectCourseNotifications(items);
        collectEventNotifications(items);
        collectMeetNotifications(items);

        return items.values().stream()
                .sorted(Comparator.comparing(NotificationItem::getTimestamp).reversed())
                .toList();
    }

    public static void showPopup(Node anchor, List<NotificationItem> notifications) {
        if (anchor == null || anchor.getScene() == null) {
            return;
        }

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox root = new VBox(12);
        root.setPrefWidth(380);
        root.setStyle("-fx-background-color:white; -fx-background-radius:18; -fx-border-color:#E2E8F0; -fx-border-radius:18; -fx-effect: dropshadow(gaussian, rgba(15,181,169,0.18), 22, 0, 0, 8);");
        root.setPadding(new Insets(14));

        HBox header = new HBox(8);
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Notifications");
        title.setStyle("-fx-font-size:16px; -fx-font-weight:800; -fx-text-fill:#0f172a;");
        Label subtitle = new Label(notifications == null ? "0" : String.valueOf(notifications.size()));
        subtitle.setStyle("-fx-background-color:#0FB5A9; -fx-text-fill:white; -fx-background-radius:999; -fx-padding:2 8; -fx-font-size:11px; -fx-font-weight:800;");
        header.getChildren().addAll(title, subtitle);
        root.getChildren().add(header);

        if (notifications == null || notifications.isEmpty()) {
            Label empty = new Label("Aucune notification pour le moment.");
            empty.setStyle("-fx-text-fill:#64748b; -fx-padding:16 4 8 4;");
            root.getChildren().add(empty);
        } else {
            ScrollPane scroll = new ScrollPane();
            scroll.setFitToWidth(true);
            scroll.setPrefViewportHeight(320);
            scroll.setStyle("-fx-background-color:transparent; -fx-background:transparent; -fx-border-color:transparent;");

            VBox list = new VBox(10);
            for (NotificationItem item : notifications) {
                list.getChildren().add(createNotificationCard(item));
            }
            scroll.setContent(list);
            root.getChildren().add(scroll);
        }

        popup.getContent().add(root);

        Bounds bounds = anchor.localToScreen(anchor.getBoundsInLocal());
        if (bounds == null) {
            return;
        }
        popup.show(anchor.getScene().getWindow(), bounds.getMaxX() - 380, bounds.getMaxY() + 10);
    }

    public static void showToast(Window owner, NotificationItem item) {
        if (owner == null || item == null) {
            return;
        }

        Popup popup = new Popup();
        popup.setAutoHide(true);

        VBox card = new VBox(6);
        card.setPrefWidth(320);
        card.setStyle("-fx-background-color:#111827; -fx-background-radius:16; -fx-padding:14 16; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 18, 0, 0, 6);");

        Label title = new Label(item.getTitle());
        title.setStyle("-fx-text-fill:white; -fx-font-size:14px; -fx-font-weight:800;");

        Label message = new Label(item.getMessage());
        message.setWrapText(true);
        message.setStyle("-fx-text-fill:#E5E7EB; -fx-font-size:12px;");

        Label time = new Label(item.getDisplayTime());
        time.setStyle("-fx-text-fill:#93C5FD; -fx-font-size:11px; -fx-font-weight:700;");

        card.getChildren().addAll(title, message, time);
        popup.getContent().add(card);

        double x = owner.getX() + owner.getWidth() - 340;
        double y = owner.getY() + owner.getHeight() - 120;
        popup.show(owner, x, y);

        PauseTransition hide = new PauseTransition(Duration.seconds(4));
        hide.setOnFinished(e -> popup.hide());
        hide.play();
    }

    private static VBox createNotificationCard(NotificationItem item) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color:#F8FAFC; -fx-background-radius:14; -fx-padding:12; -fx-border-color:#E2E8F0; -fx-border-radius:14;");

        Label title = new Label(item.getTitle());
        title.setStyle("-fx-font-weight:800; -fx-text-fill:#0f172a; -fx-font-size:13px;");

        Label message = new Label(item.getMessage());
        message.setWrapText(true);
        message.setStyle("-fx-text-fill:#475569; -fx-font-size:12px;");

        Label time = new Label(item.getDisplayTime());
        time.setStyle("-fx-text-fill:#64748b; -fx-font-size:10px; -fx-font-weight:700;");

        card.getChildren().addAll(title, message, time);
        return card;
    }

    private static void collectCourseNotifications(Map<String, NotificationItem> items) {
        try {
            List<Cours> courses = new CoursService().recuperer();
            LocalDateTime threshold = LocalDateTime.now().minusDays(3);
            for (Cours course : courses) {
                Timestamp created = course.getDateCreation();
                if (created == null) {
                    continue;
                }
                LocalDateTime createdAt = created.toLocalDateTime();
                if (createdAt.isBefore(threshold)) {
                    continue;
                }
                String title = "Nouveau cours ajouté";
                String message = safeText(course.getTitre()) + " vient d'être publié.";
                items.put("course:" + course.getId(), new NotificationItem(
                        "course:" + course.getId(),
                        title,
                        message,
                        createdAt,
                        "Cours"));
            }
        } catch (SQLException ignored) {
        }
    }

    private static void collectEventNotifications(Map<String, NotificationItem> items) {
        try {
            List<Event> events = new EventService().recuperer();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime limit = now.plusDays(7);
            for (Event event : events) {
                if (event.getDateDebut() == null) {
                    continue;
                }
                LocalDateTime start = event.getDateDebut().toLocalDateTime();
                if (start.isAfter(limit)) {
                    continue;
                }
                String title;
                String message;
                if (start.isBefore(now)) {
                    long minutesPast = Math.max(0, ChronoUnit.MINUTES.between(start, now));
                    title = "Événement en cours";
                    message = safeText(event.getTitre()) + " a commencé il y a " + formatMinutes(minutesPast) + ".";
                    items.put("event:" + event.getId() + ":live", new NotificationItem(
                            "event:" + event.getId() + ":live",
                            title,
                            message,
                            start,
                            "Event"));
                } else {
                    title = "Événement à venir";
                    message = safeText(event.getTitre()) + " démarre dans " + formatTimeUntil(now, start) + ".";
                    items.put("event:" + event.getId() + ":upcoming", new NotificationItem(
                            "event:" + event.getId() + ":upcoming",
                            title,
                            message,
                            start,
                            "Event"));
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private static void collectMeetNotifications(Map<String, NotificationItem> items) {
        try {
            List<Meet> meets = new MeetService().recuperer();
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime limit = now.plusDays(7);
            for (Meet meet : meets) {
                if (meet.getDateDebut() == null) {
                    continue;
                }
                LocalDateTime start = meet.getDateDebut().toLocalDateTime();
                if (start.isAfter(limit)) {
                    continue;
                }
                String title;
                String message;
                if (start.isBefore(now)) {
                    long minutesPast = Math.max(0, ChronoUnit.MINUTES.between(start, now));
                    title = "Meet en cours";
                    message = safeText(meet.getTitre()) + " a commencé il y a " + formatMinutes(minutesPast) + ".";
                    items.put("meet:" + meet.getId() + ":live", new NotificationItem(
                            "meet:" + meet.getId() + ":live",
                            title,
                            message,
                            start,
                            "Meet"));
                } else {
                    title = "Meet à venir";
                    message = safeText(meet.getTitre()) + " démarre dans " + formatTimeUntil(now, start) + ".";
                    items.put("meet:" + meet.getId() + ":upcoming", new NotificationItem(
                            "meet:" + meet.getId() + ":upcoming",
                            title,
                            message,
                            start,
                            "Meet"));
                }
            }
        } catch (SQLException ignored) {
        }
    }

    private static String formatTimeUntil(LocalDateTime now, LocalDateTime start) {
        long days = ChronoUnit.DAYS.between(now.toLocalDate(), start.toLocalDate());
        if (days > 0) {
            return days + (days == 1 ? " jour" : " jours") + " à " + start.toLocalTime().format(TIME_ONLY);
        }
        long hours = Math.max(0, ChronoUnit.HOURS.between(now, start));
        if (hours > 0) {
            return hours + (hours == 1 ? " heure" : " heures");
        }
        long minutes = Math.max(0, ChronoUnit.MINUTES.between(now, start));
        return minutes <= 1 ? "quelques instants" : minutes + " minutes";
    }

    private static String formatMinutes(long minutesPast) {
        if (minutesPast < 60) {
            return minutesPast + (minutesPast <= 1 ? " minute" : " minutes");
        }
        long hours = minutesPast / 60;
        return hours + (hours == 1 ? " heure" : " heures");
    }

    private static String safeText(String value) {
        return value == null || value.isBlank() ? "Élément" : value.trim();
    }

    public static final class NotificationItem {
        private final String key;
        private final String title;
        private final String message;
        private final LocalDateTime timestamp;
        private final String source;

        public NotificationItem(String key, String title, String message, LocalDateTime timestamp, String source) {
            this.key = key;
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
            this.source = source;
        }

        public String getKey() { return key; }
        public String getTitle() { return title; }
        public String getMessage() { return message; }
        public LocalDateTime getTimestamp() { return timestamp; }
        public String getSource() { return source; }
        public String getDisplayTime() { return timestamp.format(DATE_TIME); }
    }
}
