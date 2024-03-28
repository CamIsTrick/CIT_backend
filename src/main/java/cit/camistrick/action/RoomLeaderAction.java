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

@Slf4j
@RequiredArgsConstructor
public class RoomLeaderAction implements KurentoAction {

    private final RoomService roomService;
    private final UserSessionService userSessionService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {

        String id = jsonMessage.get("id").getAsString();
        String username = jsonMessage.get("name").getAsString();

        log.info("id : {}", id);
        log.info("name : {}", username);

        createRoomProcess(session, username);
    }

    private void createRoomProcess(WebSocketSession session, String username) {
        Room room = createRoom();
        UserSession participant = joinRoom(session, username, room);
        sendRoomInfo(room, participant);
    }

    private Room createRoom() {
        return roomService.createRoom();
    }

    private UserSession joinRoom(WebSocketSession session, String username, Room room) {
        UserSession participant = room.join(username, room.getRoomId(), session);
        userSessionService.register(participant);
        return participant;
    }

    private void sendRoomInfo(Room room, UserSession receiver) {
        String roomURL = roomService.getRoomURL(room.getEntryCode());

        JsonObject scParams = new JsonObject();
        scParams.addProperty("id", "createRoomResponse");
        scParams.addProperty("roomId", room.getRoomId());
        scParams.addProperty("entryCode", room.getEntryCode());
        scParams.addProperty("roomURL", roomURL);

        log.trace("USER SessionID {}: createRoomResponse is {}", receiver.getSessionId(), room.getRoomId());
        receiver.sendMessage(scParams);
    }

    @Override
    public void onError() {
        log.error("RoomLeaderAction : Error Occurred");
    }
}
