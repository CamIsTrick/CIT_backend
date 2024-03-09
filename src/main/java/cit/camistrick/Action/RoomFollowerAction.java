package cit.camistrick.action;

import cit.camistrick.domain.Room;
import cit.camistrick.domain.UserSession;
import cit.camistrick.service.RoomManager;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.MediaPipeline;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
public class RoomFollowerAction implements KurentoAction {

    private final RoomManager roomManager;
    private final MediaPipeline mediaPipeline;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        String userId = jsonMessage.get("id").getAsString();
        String username = jsonMessage.get("name").getAsString();
        String roomId = jsonMessage.get("roomId").getAsString();

        if (userId == null) {
            log.error("User ID value is null");
        } else {
            log.info("id : {}", userId);
        }

        if (username == null) {
            log.error("Name value is null");
        } else {
            log.info("name : {}", username);
        }

        if (roomId == null) {
            log.error("Room ID value is null");
        } else {
            log.info("roomId : {}", roomId);
        }

        joinRoom(session, username, roomId);
    }

    private void joinRoom(WebSocketSession session, String username, String roomId) {
        Room findRoom = roomManager.findRoom(roomId)
                .orElseThrow(NoSuchElementException::new);

        UserSession user = new UserSession(username, findRoom.getRoomId(), session, mediaPipeline);

        findRoom.addUserSession(user);
        findRoom.notifyParticipantsOfNewUser(user);

        JsonObject newParticipantMsg = new JsonObject();
        newParticipantMsg.addProperty("id", "newParticipantArrived");
        newParticipantMsg.addProperty("name", user.getName());

        findRoom.broadcastMessage(newParticipantMsg);
    }

    @Override
    public void onError() {
        log.error("RoomFollowerAction : Error Occurred");
    }
}