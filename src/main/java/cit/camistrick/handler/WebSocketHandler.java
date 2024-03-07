package cit.camistrick.handler;

import cit.camistrick.Action.KurentoAction;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketHandler extends TextWebSocketHandler {
    private static final Gson gson = new GsonBuilder().create();
    private final KurentoActionResolver kurentoActionResolver;

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
        String id = Optional.ofNullable(jsonMessage.get("id"))
                            .map(JsonElement::getAsString)
                             .orElse("error");
        log.info("Receive ID [{}] from {} ", id, session.getId());

        KurentoAction findAction = kurentoActionResolver.findAction(id);
        processByAction(session, jsonMessage, findAction);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        KurentoAction controller = kurentoActionResolver.getCloseAction();
        processByAction(session, null, controller);
    }

    private void processByAction(WebSocketSession session, JsonObject jsonMessage, KurentoAction action) {
        try {
            action.process(session, jsonMessage);
        } catch (IOException e) {
            action.onError();
            log.warn("Error Occurred on {}", action.getClass());
        }
    }
}