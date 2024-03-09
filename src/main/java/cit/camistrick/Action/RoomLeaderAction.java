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

@Slf4j
@RequiredArgsConstructor
public class RoomLeaderAction implements KurentoAction {
    private final RoomManager roomManager;
    private final MediaPipeline mediaPipeline;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {

        String userId = jsonMessage.get("id").getAsString();
        String username = jsonMessage.get("name").getAsString();

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
        initUserSession(session, username);
    }

    private void initUserSession(WebSocketSession session, String username) {
        Room room = roomManager.createRoom();
        UserSession user = new UserSession(username, room.getRoomId(), session, mediaPipeline);

        room.addUserSession(user);
    }

    @Override
    public void onError() {
        log.error("RoomLeaderAction : Error Occurred");
    }
}
