package tn.esprit.services.meet;

import tn.esprit.entities.meet.Meet;
import tn.esprit.entities.meet.participant;
import tn.esprit.tools.MyDatabase;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.*;

/**
 * Service de planification intelligente avec IA
 * Detecte les conflits, suggere des creneaux optimaux,
 * et propose des alternatives intelligentes pour les meets
 */
public class AISchedulingService {

    private final Connection cnx;
    private final MeetService meetService;
    private final MeetParticipantsService participantsService;
    private final ParticipantService participantService;

    // Configuration de l'IA
    private static final int SUGGESTION_WINDOW_DAYS = 14; // Chercher dans les 14 prochains jours
    private static final int WORK_DAY_START_HOUR = 8;     // 8h debut journee travail
    private static final int WORK_DAY_END_HOUR = 18;      // 18h fin journee travail
    private static final int DEFAULT_MEET_DURATION_MIN = 60; // Duree par defaut 1h
    private static final int SLOT_INCREMENT_MIN = 30;     // Pas de 30min pour suggestions

    public AISchedulingService() {
        this.cnx = MyDatabase.getInstance().getCnx();
        this.meetService = new MeetService();
        this.participantsService = new MeetParticipantsService();
        this.participantService = new ParticipantService();
    }

    /**
     * Detecte les conflits pour un nouveau meet
     */
    public List<ConflictInfo> detectConflicts(Timestamp proposedStart, Timestamp proposedEnd,
                                              List<Integer> participantIds, Integer excludeMeetId) throws SQLException {
        List<ConflictInfo> conflicts = new ArrayList<>();

        if (participantIds == null || participantIds.isEmpty()) {
            return conflicts;
        }

        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT m.id, m.titre, m.date_debut, m.date_fin, p.id as pid, " +
                "p.nom, p.prenom, p.email " +
                "FROM meet m " +
                "JOIN meet_participants mp ON m.id = mp.meet_id " +
                "JOIN participant p ON mp.participant_id = p.id " +
                "WHERE mp.participant_id IN ("
        );

        // Build IN clause
        for (int i = 0; i < participantIds.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        // Exclude current meet if editing
        if (excludeMeetId != null) {
            sql.append(" AND m.id != ?");
        }

        // Time overlap conditions
        sql.append(" AND (" +
            "(m.date_debut <= ? AND m.date_fin > ?) OR " +  // Nouveau commence pendant existant
            "(m.date_debut < ? AND m.date_fin >= ?) OR " +  // Nouveau finit pendant existant
            "(m.date_debut >= ? AND m.date_fin <= ?)" +     // Nouveau englobe existant
            ")" +
            "ORDER BY m.date_debut");

        PreparedStatement ps = cnx.prepareStatement(sql.toString());
        int paramIdx = 1;

        for (Integer pid : participantIds) {
            ps.setInt(paramIdx++, pid);
        }

        if (excludeMeetId != null) {
            ps.setInt(paramIdx++, excludeMeetId);
        }

        ps.setTimestamp(paramIdx++, proposedEnd);
        ps.setTimestamp(paramIdx++, proposedStart);
        ps.setTimestamp(paramIdx++, proposedStart);
        ps.setTimestamp(paramIdx++, proposedEnd);
        ps.setTimestamp(paramIdx++, proposedStart);
        ps.setTimestamp(paramIdx++, proposedEnd);

        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            conflicts.add(new ConflictInfo(
                rs.getInt("id"),
                rs.getString("titre"),
                rs.getTimestamp("date_debut"),
                rs.getTimestamp("date_fin"),
                rs.getInt("pid"),
                rs.getString("nom") + " " + rs.getString("prenom"),
                rs.getString("email")
            ));
        }

        return conflicts;
    }

    /**
     * Genere des suggestions intelligentes pour un meet avec conflits
     * Cherche un creneau ou TOUS les participants sont disponibles
     */
    public SchedulingSuggestion generateSmartSuggestion(Timestamp desiredStart, Timestamp desiredEnd,
                                                        List<Integer> participantIds, int durationMinutes,
                                                        Integer excludeMeetId) throws SQLException {
        List<ConflictInfo> conflicts = detectConflicts(desiredStart, desiredEnd, participantIds, excludeMeetId);

        if (conflicts.isEmpty()) {
            return new SchedulingSuggestion(
                desiredStart, desiredEnd, 100,
                Collections.emptyList(),
                Collections.emptyList(),
                "Creneau parfait - aucun conflit detecte"
            );
        }

        // Generer des alternatives
        List<TimeSlot> alternatives = generateAlternativeSlots(desiredStart, desiredEnd, participantIds,
            durationMinutes, excludeMeetId, conflicts);

        // Trouver le premier creneau ou TOUS sont disponibles (sans conflit)
        TimeSlot allAvailableSlot = findSlotWithAllAvailable(participantIds, durationMinutes,
            excludeMeetId, desiredStart);

        // Ajouter ce creneau en priorite si trouve
        if (allAvailableSlot != null) {
            alternatives.add(0, allAvailableSlot);
        }

        TimeSlot bestSlot = alternatives.isEmpty() ? null : alternatives.get(0);
        int confidenceScore = Math.max(0, 100 - (conflicts.size() * 15));
        String reasoning = buildReasoningAllAvailable(conflicts, alternatives, bestSlot, allAvailableSlot != null);

        return new SchedulingSuggestion(
            desiredStart, desiredEnd, confidenceScore,
            conflicts, alternatives, Collections.emptyList(), reasoning
        );
    }

    /**
     * Cherche un creneau ou tous les participants sont disponibles
     */
    private TimeSlot findSlotWithAllAvailable(List<Integer> participantIds, int durationMinutes,
                                               Integer excludeMeetId, Timestamp startSearch) throws SQLException {
        LocalDateTime searchStart = startSearch.toLocalDateTime();
        LocalDateTime searchEnd = searchStart.plusDays(SUGGESTION_WINDOW_DAYS);

        for (LocalDateTime candidate = searchStart; candidate.isBefore(searchEnd);
             candidate = candidate.plusMinutes(SLOT_INCREMENT_MIN)) {

            if (candidate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                candidate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            int hour = candidate.getHour();
            if (hour < WORK_DAY_START_HOUR || hour >= WORK_DAY_END_HOUR) {
                continue;
            }

            LocalDateTime candidateEnd = candidate.plusMinutes(durationMinutes);
            Timestamp candidateStartTs = Timestamp.valueOf(candidate);
            Timestamp candidateEndTs = Timestamp.valueOf(candidateEnd);

            // Verifier si TOUS les participants sont disponibles (pas de conflit)
            List<ConflictInfo> slotConflicts = detectConflicts(candidateStartTs, candidateEndTs,
                participantIds, excludeMeetId);

            if (slotConflicts.isEmpty()) {
                return new TimeSlot(
                    candidateStartTs, candidateEndTs, 100,
                    "Creneau trouve ou tous les participants sont disponibles"
                );
            }
        }

        return null; // Aucun creneau trouve ou tous sont disponibles
    }

    /**
     * Genere des creneaux alternatifs intelligents
     */
    private List<TimeSlot> generateAlternativeSlots(Timestamp desiredStart, Timestamp desiredEnd,
                                                    List<Integer> participantIds, int durationMinutes,
                                                    Integer excludeMeetId, List<ConflictInfo> knownConflicts) throws SQLException {
        List<TimeSlot> alternatives = new ArrayList<>();
        Set<Integer> conflictingParticipantIds = new java.util.HashSet<>();
        for (ConflictInfo c : knownConflicts) {
            conflictingParticipantIds.add(c.getParticipantId());
        }

        LocalDateTime searchStart = desiredStart.toLocalDateTime();
        LocalDateTime searchEnd = searchStart.plusDays(SUGGESTION_WINDOW_DAYS);

        // Heuristiques de scoring
        Map<LocalDateTime, Integer> slotScores = new HashMap<>();

        for (LocalDateTime candidate = searchStart; candidate.isBefore(searchEnd); candidate = candidate.plusMinutes(SLOT_INCREMENT_MIN)) {
            // Ignorer weekends
            if (candidate.getDayOfWeek() == DayOfWeek.SATURDAY ||
                candidate.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }

            // Ignorer heures hors travail
            int hour = candidate.getHour();
            if (hour < WORK_DAY_START_HOUR || hour >= WORK_DAY_END_HOUR) {
                continue;
            }

            LocalDateTime candidateEnd = candidate.plusMinutes(durationMinutes);
            Timestamp candidateStartTs = Timestamp.valueOf(candidate);
            Timestamp candidateEndTs = Timestamp.valueOf(candidateEnd);

            // Verifier conflits sur ce creneau
            List<ConflictInfo> slotConflicts = detectConflicts(candidateStartTs, candidateEndTs,
                participantIds, excludeMeetId);

            int score = 100;
            int conflictCount = 0;

            for (ConflictInfo conflict : slotConflicts) {
                if (conflictingParticipantIds.contains(conflict.getParticipantId())) {
                    // Ce participant a deja un conflit au creneau desire, c'est un bon signe
                    // s'il est libre ici
                    score += 5;
                } else {
                    // Nouveau conflit - penalite
                    score -= 20;
                    conflictCount++;
                }
            }

            // Bonus pour proximite avec le creneau desire
            long hoursDiff = java.time.Duration.between(desiredStart.toLocalDateTime(), candidate).toHours();
            if (hoursDiff <= 24) score += 15;
            else if (hoursDiff <= 48) score += 10;
            else if (hoursDiff <= 72) score += 5;

            // Bonus pour heures "productives" historiques
            if (isHistoricallyProductiveHour(candidate.getHour())) {
                score += 10;
            }

            if (conflictCount == 0) {
                slotScores.put(candidate, score);
            }
        }

        // Trier par score et prendre les meilleurs
        slotScores.entrySet().stream()
            .sorted(Map.Entry.<LocalDateTime, Integer>comparingByValue().reversed())
            .limit(5)
            .forEach(entry -> {
                LocalDateTime start = entry.getKey();
                LocalDateTime end = start.plusMinutes(durationMinutes);
                alternatives.add(new TimeSlot(
                    Timestamp.valueOf(start),
                    Timestamp.valueOf(end),
                    entry.getValue(),
                    "Score: " + entry.getValue() + " - " +
                        (entry.getValue() > 90 ? "Creneau optimal" :
                            entry.getValue() > 70 ? "Bon creneau" : "Creneau acceptable")
                ));
            });

        return alternatives;
    }


    /**
     * Verifie si une heure est historiquement productive
     */
    private boolean isHistoricallyProductiveHour(int hour) throws SQLException {
        String sql = "SELECT AVG(cnt) as avg_count FROM " +
            "(SELECT HOUR(date_debut) as h, COUNT(*) as cnt FROM meet " +
            "WHERE date_debut >= DATE_SUB(NOW(), INTERVAL 30 DAY) " +
            "GROUP BY HOUR(date_debut)) as hourly_stats " +
            "WHERE h = ?";

        PreparedStatement ps = cnx.prepareStatement(sql);
        ps.setInt(1, hour);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            double avg = rs.getDouble("avg_count");
            return avg > 1.5; // Plus de 1.5 meets/heure en moyenne = productive
        }
        return false;
    }

    /**
     * Construit une explication de la suggestion avec focus sur disponibilite de tous
     */
    private String buildReasoningAllAvailable(List<ConflictInfo> conflicts, List<TimeSlot> alternatives,
                                               TimeSlot bestSlot, boolean foundAllAvailable) {
        StringBuilder sb = new StringBuilder();

        Map<Integer, List<ConflictInfo>> conflictsByMeet = new HashMap<>();
        for (ConflictInfo c : conflicts) {
            conflictsByMeet.computeIfAbsent(c.getMeetId(), k -> new ArrayList<>()).add(c);
        }

        sb.append("Conflits detectes: ").append(conflictsByMeet.size()).append(" reunion(s) en chevauchement\n");

        for (Map.Entry<Integer, List<ConflictInfo>> entry : conflictsByMeet.entrySet()) {
            ConflictInfo first = entry.getValue().get(0);
            sb.append("- \"").append(first.getMeetTitle()).append("\" (");
            sb.append(first.getMeetStart().toLocalDateTime().toLocalTime()).append("-");
            sb.append(first.getMeetEnd().toLocalDateTime().toLocalTime()).append(")");
            sb.append(" avec ").append(entry.getValue().size()).append(" participant(s) en commun\n");
        }

        if (foundAllAvailable && bestSlot != null) {
            sb.append("\nCreneau optimal trouve ou TOUS les participants sont disponibles:\n");
            sb.append(bestSlot.getStart().toLocalDateTime().toLocalDate()).append(" ");
            sb.append(bestSlot.getStart().toLocalDateTime().toLocalTime()).append("-");
            sb.append(bestSlot.getEnd().toLocalDateTime().toLocalTime());
        } else if (bestSlot != null) {
            sb.append("\nMeilleure suggestion alternative:\n");
            sb.append(bestSlot.formatRange()).append(" (").append(bestSlot.getExplanation()).append(")");
            sb.append("\n\nNote: Aucun creneau trouve ou tous les participants sont disponibles.");
        }

        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CLASSES DE DONNEES
    // ═══════════════════════════════════════════════════════════════════════

    public static class ConflictInfo {
        private final int meetId;
        private final String meetTitle;
        private final Timestamp meetStart;
        private final Timestamp meetEnd;
        private final int participantId;
        private final String participantName;
        private final String participantEmail;

        public ConflictInfo(int meetId, String meetTitle, Timestamp meetStart, Timestamp meetEnd,
                            int participantId, String participantName, String participantEmail) {
            this.meetId = meetId;
            this.meetTitle = meetTitle;
            this.meetStart = meetStart;
            this.meetEnd = meetEnd;
            this.participantId = participantId;
            this.participantName = participantName;
            this.participantEmail = participantEmail;
        }

        public int getMeetId() { return meetId; }
        public String getMeetTitle() { return meetTitle; }
        public Timestamp getMeetStart() { return meetStart; }
        public Timestamp getMeetEnd() { return meetEnd; }
        public int getParticipantId() { return participantId; }
        public String getParticipantName() { return participantName; }
        public String getParticipantEmail() { return participantEmail; }
    }

    public static class TimeSlot {
        private final Timestamp start;
        private final Timestamp end;
        private final int score;
        private final String explanation;

        public TimeSlot(Timestamp start, Timestamp end, int score, String explanation) {
            this.start = start;
            this.end = end;
            this.score = score;
            this.explanation = explanation;
        }

        public Timestamp getStart() { return start; }
        public Timestamp getEnd() { return end; }
        public int getScore() { return score; }
        public String getExplanation() { return explanation; }

        public String formatRange() {
            return start.toLocalDateTime().toLocalDate() + " " +
                start.toLocalDateTime().toLocalTime() + "-" +
                end.toLocalDateTime().toLocalTime();
        }
    }

    public static class SubgroupSuggestion {
        private final String name;
        private final List<Integer> participantIds;
        private final String explanation;
        private final int feasibilityScore;

        public SubgroupSuggestion(String name, List<Integer> participantIds,
                                  String explanation, int feasibilityScore) {
            this.name = name;
            this.participantIds = participantIds;
            this.explanation = explanation;
            this.feasibilityScore = feasibilityScore;
        }

        public String getName() { return name; }
        public List<Integer> getParticipantIds() { return participantIds; }
        public String getExplanation() { return explanation; }
        public int getFeasibilityScore() { return feasibilityScore; }
    }

    public static class SchedulingSuggestion {
        private final Timestamp originalStart;
        private final Timestamp originalEnd;
        private final int confidenceScore;
        private final List<ConflictInfo> conflicts;
        private final List<TimeSlot> alternatives;
        private final List<SubgroupSuggestion> subgroups;
        private final String reasoning;

        public SchedulingSuggestion(Timestamp originalStart, Timestamp originalEnd, int confidenceScore,
                                    List<ConflictInfo> conflicts, List<TimeSlot> alternatives,
                                    String reasoning) {
            this(originalStart, originalEnd, confidenceScore, conflicts, alternatives,
                Collections.emptyList(), reasoning);
        }

        public SchedulingSuggestion(Timestamp originalStart, Timestamp originalEnd, int confidenceScore,
                                    List<ConflictInfo> conflicts, List<TimeSlot> alternatives,
                                    List<SubgroupSuggestion> subgroups, String reasoning) {
            this.originalStart = originalStart;
            this.originalEnd = originalEnd;
            this.confidenceScore = confidenceScore;
            this.conflicts = conflicts;
            this.alternatives = alternatives;
            this.subgroups = subgroups;
            this.reasoning = reasoning;
        }

        public boolean hasConflicts() { return !conflicts.isEmpty(); }
        public boolean hasAlternatives() { return !alternatives.isEmpty(); }
        public boolean hasSubgroupSuggestions() { return !subgroups.isEmpty(); }

        public Timestamp getOriginalStart() { return originalStart; }
        public Timestamp getOriginalEnd() { return originalEnd; }
        public int getConfidenceScore() { return confidenceScore; }
        public List<ConflictInfo> getConflicts() { return conflicts; }
        public List<TimeSlot> getAlternatives() { return alternatives; }
        public List<SubgroupSuggestion> getSubgroups() { return subgroups; }
        public String getReasoning() { return reasoning; }

        public TimeSlot getBestAlternative() {
            return alternatives.isEmpty() ? null : alternatives.get(0);
        }
    }
}
