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
import java.util.NoSuchElementException;


@Slf4j
@RequiredArgsConstructor
public class ExitAction implements KurentoAction {

    private final RoomManager roomManager;
    private final UserSessionService userSessionService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        String id = jsonMessage.get("id").getAsString();
        String sessionId = jsonMessage.get("sessionId").getAsString();

        log.info("id : {}", id);
        
        if (sessionId == null) {
            log.error("Session Id value is null");
        } else {
            log.info("sessionId : {}", sessionId);
        }

        UserSession findUser = getUserSession(sessionId);
        leaveUser(findUser);
    }

    private void leaveUser(UserSession findUser) throws IOException{
        userSessionService.removeSession(findUser.getSessionId());
        Room leavingRoom = getRoom(findUser.getRoomId());
        leavingRoom.leave(findUser);
        closeRoom(leavingRoom);
    }

    private UserSession getUserSession(String sessionId) {
        return userSessionService.findSession(sessionId);
    }

    private Room getRoom(String roomId) {
        return roomManager.findRoom(roomId)
                .orElseThrow(NoSuchElementException::new);
    }

    private void closeRoom(Room leavingRoom) {
        if (leavingRoom.getParticipants() == null) {
            roomManager.removeRoom(leavingRoom);
        }
    }

    @Override
    public void onError() {
        log.error("ExitAction : Error Occurred");
    }
}
