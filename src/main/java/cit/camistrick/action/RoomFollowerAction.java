package cit.camistrick.action;

import cit.camistrick.domain.Room;
import cit.camistrick.domain.UserSession;
import cit.camistrick.service.RoomService;
import cit.camistrick.service.UserSessionService;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.NoSuchElementException;

@Slf4j
@RequiredArgsConstructor
public class RoomFollowerAction implements KurentoAction {

    private final RoomService roomService;
    private final UserSessionService userSessionService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        String id = jsonMessage.get("id").getAsString();
        String username = jsonMessage.get("name").getAsString();
        String roomId = jsonMessage.get("roomId").getAsString();

        log.info("id : {}", id);
        log.info("name : {}", username);
        log.info("roomId : {}", roomId);
        
        joinRoom(session, username, roomId);
    }

    private void joinRoom(WebSocketSession session, String username, String roomId) {
        Room findRoom = roomService.findRoom(roomId)
                .orElseThrow(NoSuchElementException::new);
        UserSession participant = findRoom.join(username, roomId, session);
        userSessionService.register(participant);
        findRoom.notifyParticipantsOfNewUser(participant);
        findRoom.notifyParticipantsOfExisting(participant);
    }

    @Override
    public void onError() {
        log.error("RoomFollowerAction : Error Occurred");
    }
}
