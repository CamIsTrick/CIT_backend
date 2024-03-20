package cit.camistrick.action;

import cit.camistrick.domain.Room;
import cit.camistrick.domain.UserSession;
import cit.camistrick.service.RoomManager;
import cit.camistrick.service.UserSessionService;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RoomLeaderAction implements KurentoAction {

    private final RoomManager roomManager;
    private final UserSessionService userSessionService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {

        String id = jsonMessage.get("id").getAsString();
        String username = jsonMessage.get("name").getAsString();

        if (id == null) {
            log.error("ID value is null");
        } else {
            log.info("id : {}", id);
        }
        if (username == null) {
            log.error("Name value is null");
        } else {
            log.info("name : {}", username);
        }

        createRoomAndSession(session, username);
    }

    private void createRoomAndSession(WebSocketSession session, String username) {
        Room room = roomManager.createRoom();
        UserSession participant = room.join(username, room.getRoomId(), session);
        userSessionService.register(participant);
    }

    @Override
    public void onError() {
        log.error("RoomLeaderAction : Error Occurred");
    }
}
