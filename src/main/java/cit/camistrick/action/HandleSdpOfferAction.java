package cit.camistrick.action;

import cit.camistrick.domain.UserSession;
import cit.camistrick.service.UserSessionService;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@RequiredArgsConstructor
public class HandleSdpOfferAction implements KurentoAction {

    private final UserSessionService userSessionService;

    @Override
    public void process(WebSocketSession session, JsonObject jsonMessage) {
        if (!isValidMessage(jsonMessage)) {
            return;
        }

        UserSession sender = getSender(jsonMessage);
        UserSession recipient = getRecipient(session);

        processVideoOffer(sender, recipient, jsonMessage);
    }

    private boolean isValidMessage(JsonObject jsonMessage) {
        if (!jsonMessage.has("sender") || !jsonMessage.has("sdpOffer")) {
            log.error("JSON message is missing required fields: 'sender' or 'sdpOffer'");
            return false;
        }
        return true;
    }

    private UserSession getSender(JsonObject jsonMessage) {
        String senderSessionId = jsonMessage.get("sender").getAsString();
        return userSessionService.findSession(senderSessionId);
    }

    private UserSession getRecipient(WebSocketSession session) {
        return userSessionService.findSession(session.getId());
    }

    private void processVideoOffer(UserSession sender, UserSession recipient, JsonObject jsonMessage) {
        // 구현 예정
    }

    @Override
    public void onError() {
        log.error("HandleSdpOfferAction : Error Occurred");
    }
}