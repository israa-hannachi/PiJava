package tn.esprit.services.meet;

import tn.esprit.entities.meet.meet_participants;
import java.sql.SQLException;
import java.util.List;

public interface IMeetParticipantsService {
    void ajouter(meet_participants mp) throws SQLException;
    void supprimer(int meetId, int participantId) throws SQLException;
    List<meet_participants> findByMeetId(int meetId) throws SQLException;
    List<meet_participants> findByParticipantId(int participantId) throws SQLException;
    boolean exists(int meetId, int participantId) throws SQLException;
}
