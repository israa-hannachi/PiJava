package tn.esprit.services.meet;

import tn.esprit.entities.meet.Meet_Participants;
import java.sql.SQLException;
import java.util.List;

public interface IMeetParticipantsService {
    void ajouter(Meet_Participants mp) throws SQLException;
    void supprimer(int meetId, int participantId) throws SQLException;
    List<Meet_Participants> findByMeetId(int meetId) throws SQLException;
    List<Meet_Participants> findByParticipantId(int participantId) throws SQLException;
    boolean exists(int meetId, int participantId) throws SQLException;
}
