package tn.esprit.services;

import tn.esprit.entities.meet.Meet;

public class JitsiMeetService {

    private static final String DEFAULT_JITSI_DOMAIN = "8x8.vc"; // Plus stable et moderne que meet.jit.si
    private String jitsiDomain;

    public JitsiMeetService() {
        this.jitsiDomain = "jitsi.belnet.be"; // Serveur éducatif sans restrictions de login
    }

    public JitsiMeetService(String jitsiDomain) {
        this.jitsiDomain = jitsiDomain != null && !jitsiDomain.isEmpty() ? jitsiDomain : DEFAULT_JITSI_DOMAIN;
    }

    public String getJitsiDomain() {
        return jitsiDomain;
    }

    public void setJitsiDomain(String jitsiDomain) {
        this.jitsiDomain = jitsiDomain != null && !jitsiDomain.isEmpty() ? jitsiDomain : DEFAULT_JITSI_DOMAIN;
    }

    public String generateRoomName(Meet meet) {
        if (meet == null) {
            return "naja7ni-meet";
        }

        String rawTitle = meet.getTitre() != null ? meet.getTitre() : "meet";
        String safeTitle = rawTitle.toLowerCase()
                .replaceAll("[^a-zA-Z0-9]+", "-")
                .replaceAll("^-+|--+", "-")
                .replaceAll("-+$", "");

        safeTitle = safeTitle.length() > 30 ? safeTitle.substring(0, 30) : safeTitle;

        return "naja7ni-" + meet.getId() + (safeTitle.isEmpty() ? "" : "-" + safeTitle);
    }

    public String generateRoomUrl(Meet meet) {
        String roomName = generateRoomName(meet);
        return "https://" + jitsiDomain + "/" + roomName;
    }

    public String generateExternalApiUrl() {
        return "https://" + jitsiDomain + "/external_api.js";
    }
}
