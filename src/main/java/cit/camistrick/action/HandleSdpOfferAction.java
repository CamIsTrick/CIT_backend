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
        String sdpAnswer = setupRecipientForVideoReception(sender, recipient, jsonMessage);
        sendSdpAnswerToRecipient(recipient, sender, sdpAnswer);
    }

    private String setupRecipientForVideoReception(UserSession sender, UserSession recipient, JsonObject jsonMessage) {
        String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        return recipient.prepareToReceiveVideoFrom(sender, sdpOffer);
    }

    private void sendSdpAnswerToRecipient(UserSession recipient, UserSession sender, String sdpAnswer) {
        JsonObject scParams = new JsonObject();
        scParams.addProperty("id", "receiveVideoAnswer");
        scParams.addProperty("name", sender.getName());
        scParams.addProperty("sdpAnswer", sdpAnswer);

        log.trace("USER {}: SdpAnswer for {} is {}", recipient.getName(), sender.getName(), sdpAnswer);
        recipient.sendMessage(scParams);
    }

    @Override
    public void onError() {
        log.error("HandleSdpOfferAction : Error Occurred");
    }
}