package cit.camistrick.domain;

import com.google.gson.JsonObject;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import javax.annotation.PreDestroy;
import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Room implements Closeable {
    private final Logger log = LoggerFactory.getLogger(Room.class);

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

    public UserSession join(String userName, WebSocketSession session) throws IOException {
        log.info("ROOM [{}]: adding participant [{}]", roomId, userName);
        final UserSession participant = new UserSession(userName, roomId, session, pipeline);
        addUserSession(participant);
        return participant;
    }

    public void leave(UserSession user) throws IOException {
        log.debug("PARTICIPANT {}: Leaving room {}", user.getName(), this.roomId);
        removeUserSession(user);
    }

    public void addUserSession(UserSession userSession) throws IOException {
        participants.put(userSession.getSessionId(), userSession);
        notifyParticipantsOfNewUser(userSession);
    }

    public void removeUserSession(UserSession userSession) throws IOException {
        participants.remove(userSession.getSessionId());
        notifyParticipantsOfUserLeaving(userSession);
        userSession.close();
    }

    private void notifyParticipantsOfNewUser(UserSession newParticipant) throws IOException {
        final JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.addProperty("name", newParticipant.getName());
        broadcastMessage(newParticipantMsg);
    }

    private void notifyParticipantsOfUserLeaving(UserSession leavingUser) throws IOException {
        final JsonObject participantLeftMsg = new JsonObject();
        participantLeftMsg.addProperty("id", "participantLeft");
        participantLeftMsg.addProperty("name", leavingUser.getName());
        notifyParticipantsOfCancelVideoFrom(leavingUser.getName());
        broadcastMessage(participantLeftMsg);
    }

    private void broadcastMessage(JsonObject message) {
        for (UserSession participant : participants.values()) {
            participant.sendMessage(message);
        }
    }

    private void notifyParticipantsOfCancelVideoFrom(String leavingUserName) {
        for (UserSession participant : participants.values()) {
            participant.cancelVideoFrom(leavingUserName);
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