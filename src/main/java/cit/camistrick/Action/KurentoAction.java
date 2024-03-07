package cit.camistrick.Action;

import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public interface KurentoAction {
    void process(WebSocketSession session, JsonObject jsonMessage) throws IOException;

    void onError();
}
