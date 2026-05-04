package tn.esprit.entities.meet;

public class Meet_Participants {
    private int meetId;
    private int participantId;

    public Meet_Participants() {}

    public Meet_Participants(int meetId, int participantId) {
        this.meetId = meetId;
        this.participantId = participantId;
    }

    public int getMeetId() { return meetId; }
    public void setMeetId(int meetId) { this.meetId = meetId; }

    public int getParticipantId() { return participantId; }
    public void setParticipantId(int participantId) { this.participantId = participantId; }

    @Override
    public String toString() {
        return "MeetParticipant{meetId=" + meetId + ", participantId=" + participantId + "}";
    }
}
