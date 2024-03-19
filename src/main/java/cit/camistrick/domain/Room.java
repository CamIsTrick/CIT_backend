package cit.camistrick.domain;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class Room implements Closeable {
    private final ConcurrentMap<String, UserSession> participants = new ConcurrentHashMap<>();
    private final String roomId;
    private final MediaPipeline pipeline;

    public Room(String roomId, MediaPipeline pipeline) {
        this.roomId = roomId;
        this.pipeline = pipeline;
        log.info("ROOM [{}] has been created", roomId);
    }

    public String getRoomId() {
        return roomId;
    }

    public List<UserSession> getAllUserSessions() {
        return new ArrayList<>(participants.values());
    }

    @PreDestroy
    private void shutdown() {
        this.close();
    }

    public UserSession join(String userName, String roomId, WebSocketSession session) {
        log.info("ROOM [{}]: adding participant [{}]", roomId, userName);
        final UserSession participant = UserSession.createUserSession(userName, roomId, session, pipeline);
        addUserSession(participant);
        return participant;
    }

    public void leave(UserSession user) throws IOException {
        log.debug("PARTICIPANT {}: Leaving room {}", user.getName(), this.roomId);
        removeUserSession(user);
    }

    public void addUserSession(UserSession userSession) {
        participants.put(userSession.getSessionId(), userSession);
    }

    public void removeUserSession(UserSession userSession) throws IOException {
        participants.remove(userSession.getSessionId());
        notifyParticipantsOfUserLeaving(userSession);
        userSession.close();
    }

    public void notifyParticipantsOfNewUser(UserSession newParticipant) {
        final JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.addProperty("name", newParticipant.getName());
        broadcastMessage(newParticipant, newParticipantMsg);
    }

    public ConcurrentMap<String, UserSession> notifyParticipantsOfExisting(UserSession newParticipant) {
        for (UserSession existingParticipant : participants.values()) {
            if (!newParticipant.equals(existingParticipant)) {
                final JsonObject existingParticipantMsg = new JsonObject();
                existingParticipantMsg.addProperty("id", "existingParticipant");
                existingParticipantMsg.addProperty("name", existingParticipant.getName());
                newParticipant.sendMessage(existingParticipantMsg);
            }
        }
        return participants;
    }

    private void notifyParticipantsOfUserLeaving(UserSession leavingUser) throws IOException {
        final JsonObject participantLeftMsg = new JsonObject();
        participantLeftMsg.addProperty("id", "participantLeft");
        participantLeftMsg.addProperty("name", leavingUser.getName());
        notifyParticipantsOfCancelVideoFrom(leavingUser.getSessionId());
        broadcastMessage(leavingUser, participantLeftMsg);
    }

    private void broadcastMessage(UserSession sender, JsonObject message) {
        for (UserSession participant : participants.values()) {
            if (!participant.equals(sender)) {
                participant.sendMessage(message);
            }
        }
    }

    private void notifyParticipantsOfCancelVideoFrom(String leavingUserSessionId) {
        for (UserSession participant : participants.values()) {
            participant.cancelVideoFrom(leavingUserSessionId);
        }
    }

    public Collection<UserSession> getParticipants() {
        return participants.values();
    }

    public UserSession getUserSession(String userSessionId) {
        return participants.get(userSessionId);
    }

    @Override
    public void close() {
        closeAllSessions();
        releasePipeline();
        log.info("Room [{}] closed", roomId);
    }

    private void closeAllSessions() {
        for (final UserSession user : participants.values()) {
            user.close();
        }

        participants.clear();
    }

    private void releasePipeline() {
        pipeline.release(new Continuation<Void>() {
            @Override
            public void onSuccess(Void result) throws Exception {
                log.info("ROOM [{}]: Released Pipeline", roomId);
            }

            @Override
            public void onError(Throwable cause) throws Exception {
                log.warn("ROOM [{}]: Could not release Pipeline", roomId);
            }
        });
    }

}
