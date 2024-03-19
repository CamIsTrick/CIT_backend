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
        UserSession findUser = getUserSession(jsonMessage);
        leaveUser(findUser);
    }

    private void leaveUser(UserSession findUser) throws IOException{
        userSessionService.removeSession(findUser.getSessionId());
        Room leavingRoom = getRoom(findUser.getRoomId());
        leavingRoom.leave(findUser);
        closeRoom(leavingRoom);
    }

    private UserSession getUserSession(JsonObject jsonMessage) {
        return userSessionService.findSession(jsonMessage.get("id").getAsString());
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
