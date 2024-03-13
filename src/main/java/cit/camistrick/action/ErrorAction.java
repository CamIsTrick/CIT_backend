package cit.camistrick.action;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Slf4j
public class ErrorAction implements KurentoAction {
    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", "error");
        jsonObject.addProperty("message", "Invalid ID");
        session.sendMessage(new TextMessage(jsonObject.toString()));
    }

    @Override
    public void onError() {
        log.error("RoomFollowerAction : Error Occurred");
    }
}
