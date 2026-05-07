package tn.esprit.services.event;

import org.apache.commons.text.similarity.CosineDistance;
import org.apache.commons.text.similarity.JaccardSimilarity;
import org.apache.commons.text.similarity.LevenshteinDistance;
import tn.esprit.entities.event.Event;
import tn.esprit.entities.event.Registration;
import tn.esprit.entities.event.Sponsor;
import tn.esprit.entities.users.Users;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class EventChatBotService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm", Locale.FRANCE);

    private final EventService eventService;
    private final SponsorService sponsorService;
    private final RegistrationService registrationService;
    private final Map<String, String> keywordResponses = new HashMap<>();

    public EventChatBotService(EventService eventService, SponsorService sponsorService, RegistrationService registrationService) {
        this.eventService = eventService;
        this.sponsorService = sponsorService;
        this.registrationService = registrationService;
        initializeKeywordResponses();
    }

    private void initializeKeywordResponses() {
        keywordResponses.put("bonjour", "Bonjour ! Je peux vous aider pour les evenements, les sponsors, les inscriptions et les recommandations.");
        keywordResponses.put("help", "Essayez: recommandation, sponsor, inscription, mes tickets, event gratuit, prochain event.");
        keywordResponses.put("recommendation", "Je peux vous proposer un evenement selon le prix, la date, vos inscriptions ou l'event selectionne.");
        keywordResponses.put("sponsor", "Je peux vous dire quels sponsors sont rattaches a un evenement et quel type de partenariat ils ont.");
        keywordResponses.put("registration", "Je peux resumer vos inscriptions et vous guider sur les tickets.");
        keywordResponses.put("ticket", "Je peux vous aider a comprendre vos tickets, vos inscriptions et les places disponibles.");
    }

    public String buildWelcomeMessage(Users currentUser, Event selectedEvent) {
        String userPart = currentUser != null ? "Bonjour " + currentUser.getFirstName() + " !" : "Bonjour !";
        String eventPart = selectedEvent != null
                ? " Vous etes sur l'evenement \"" + selectedEvent.getTitre() + "\"."
                : " Vous pouvez me demander une recommandation ou des details sur les evenements.";
        return userPart + eventPart + " Essayez par exemple: \"recommande moi un event\", \"montre les sponsors\", \"mes inscriptions\".";
    }

    public String buildResponse(String input, Users currentUser, Event selectedEvent) {
        String normalized = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        if (normalized.isBlank()) {
            return "Je suis la pour vous aider sur les evenements, les sponsors et les inscriptions.";
        }

        try {
            if (containsAny(normalized, "recommend", "recommand", "suggest", "propose")) {
                return recommendEvent(currentUser, selectedEvent);
            }
            if (containsAny(normalized, "sponsor", "partenaire")) {
                return describeSponsors(selectedEvent);
            }
            if (containsAny(normalized, "inscription", "registration", "ticket", "mes tickets")) {
                return describeRegistrations(currentUser);
            }
            if (containsAny(normalized, "gratuit", "free")) {
                return recommendFreeEvent();
            }
            if (containsAny(normalized, "prochain", "upcoming", "bientot")) {
                return nextUpcomingEvent();
            }
        } catch (SQLException e) {
            return "Je n'arrive pas a recuperer les donnees evenementielles pour le moment.";
        }

        return fuzzyKeywordResponse(normalized, currentUser, selectedEvent);
    }

    private String recommendEvent(Users currentUser, Event selectedEvent) throws SQLException {
        if (selectedEvent != null) {
            return "Je vous recommande \"" + selectedEvent.getTitre() + "\" a " + selectedEvent.getLieu()
                    + ", le " + DATE_TIME_FORMATTER.format(selectedEvent.getDateDebut().toLocalDateTime())
                    + ". Prix: " + formatPrice(selectedEvent) + ".";
        }

        List<Event> events = eventService.recuperer();
        if (events.isEmpty()) {
            return "Je ne trouve aucun evenement disponible actuellement.";
        }

        if (currentUser != null && currentUser.getEmail() != null) {
            List<Registration> registrations = registrationService.recuperer().stream()
                    .filter(r -> currentUser.getEmail().equalsIgnoreCase(r.getVisitorEmail()))
                    .collect(Collectors.toList());
            if (!registrations.isEmpty()) {
                Event latest = eventService.findById(registrations.get(0).getEvenementId());
                if (latest != null) {
                    return "Comme vous avez deja reserve \"" + latest.getTitre() + "\", je vous conseille aussi de regarder des evenements de categorie proche comme \""
                            + latest.getCategorie() + "\".";
                }
            }
        }

        Event cheapestUpcoming = events.stream()
                .sorted(Comparator.comparing(Event::getPrix).thenComparing(Event::getDateDebut))
                .findFirst()
                .orElse(events.get(0));

        return "Je vous recommande \"" + cheapestUpcoming.getTitre() + "\" a " + cheapestUpcoming.getLieu()
                + ". C'est un bon choix car il coute " + formatPrice(cheapestUpcoming)
                + " et commence le " + DATE_TIME_FORMATTER.format(cheapestUpcoming.getDateDebut().toLocalDateTime()) + ".";
    }

    private String describeSponsors(Event selectedEvent) throws SQLException {
        if (selectedEvent == null) {
            return "Selectionnez ou ouvrez un evenement et je pourrai vous montrer ses sponsors.";
        }

        List<Sponsor> sponsors = sponsorService.findByEventId(selectedEvent.getId());
        if (sponsors.isEmpty()) {
            return "Aucun sponsor n'est encore rattache a \"" + selectedEvent.getTitre() + "\".";
        }

        String sponsorSummary = sponsors.stream()
                .limit(3)
                .map(s -> s.getNom() + (s.getType() != null ? " (" + s.getType() + ")" : ""))
                .collect(Collectors.joining(", "));

        return "Pour \"" + selectedEvent.getTitre() + "\", les sponsors visibles sont: " + sponsorSummary + ".";
    }

    private String describeRegistrations(Users currentUser) throws SQLException {
        if (currentUser == null || currentUser.getEmail() == null) {
            return "Connectez-vous d'abord pour que je puisse lire vos inscriptions.";
        }

        List<Registration> registrations = registrationService.recuperer().stream()
                .filter(r -> currentUser.getEmail().equalsIgnoreCase(r.getVisitorEmail()))
                .sorted(Comparator.comparing(Registration::getDateInscription).reversed())
                .collect(Collectors.toList());

        if (registrations.isEmpty()) {
            return "Vous n'avez encore aucune inscription evenementielle.";
        }

        Registration latest = registrations.get(0);
        Event event = eventService.findById(latest.getEvenementId());
        if (event == null) {
            return "J'ai trouve vos inscriptions, mais je n'arrive pas a relier le dernier evenement.";
        }

        return "Vous avez " + registrations.size() + " inscription(s). La plus recente est pour \"" + event.getTitre()
                + "\" le " + DATE_TIME_FORMATTER.format(event.getDateDebut().toLocalDateTime())
                + " avec un montant de " + latest.getMontantPaye() + " DT.";
    }

    private String recommendFreeEvent() throws SQLException {
        return eventService.recuperer().stream()
                .filter(e -> e.getPrix() != null && e.getPrix().doubleValue() == 0.0)
                .findFirst()
                .map(e -> "Un bon evenement gratuit est \"" + e.getTitre() + "\" a " + e.getLieu() + ".")
                .orElse("Je n'ai pas trouve d'evenement gratuit pour le moment.");
    }

    private String nextUpcomingEvent() throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        return eventService.recuperer().stream()
                .filter(e -> e.getDateDebut() != null && e.getDateDebut().toLocalDateTime().isAfter(now))
                .sorted(Comparator.comparing(Event::getDateDebut))
                .findFirst()
                .map(e -> "Le prochain evenement est \"" + e.getTitre() + "\" le "
                        + DATE_TIME_FORMATTER.format(e.getDateDebut().toLocalDateTime()) + ".")
                .orElse("Aucun prochain evenement n'est programme actuellement.");
    }

    private String fuzzyKeywordResponse(String input, Users currentUser, Event selectedEvent) {
        double bestSimilarity = 0.0;
        String bestResponse = "Je peux vous aider sur les evenements, les sponsors, les inscriptions et les recommandations personnalisees.";

        for (Map.Entry<String, String> entry : keywordResponses.entrySet()) {
            double similarity = calculateSimilarity(input, entry.getKey().toLowerCase(Locale.ROOT));
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestResponse = entry.getValue();
            }
        }

        if (bestSimilarity < 0.25) {
            return buildWelcomeMessage(currentUser, selectedEvent);
        }
        return bestResponse;
    }

    private double calculateSimilarity(String input, String keyword) {
        CosineDistance cosineDistance = new CosineDistance();
        JaccardSimilarity jaccardSimilarity = new JaccardSimilarity();
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();

        double cosineSim = 1 - cosineDistance.apply(input, keyword);
        double jaccardSim = jaccardSimilarity.apply(input, keyword);
        double levenshteinSim = 1.0 / (1.0 + levenshteinDistance.apply(input, keyword));

        return (cosineSim + jaccardSim + levenshteinSim) / 3.0;
    }

    private boolean containsAny(String input, String... keywords) {
        for (String keyword : keywords) {
            if (input.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String formatPrice(Event event) {
        if (event.getPrix() == null || event.getPrix().doubleValue() == 0.0) {
            return "gratuit";
        }
        return event.getPrix().stripTrailingZeros().toPlainString() + " DT";
    }
}
