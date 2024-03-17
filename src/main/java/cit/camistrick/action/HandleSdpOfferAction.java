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
        UserSession receiver = getReceiver(session);

        processVideoOffer(sender, receiver, jsonMessage);
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

    private UserSession getReceiver(WebSocketSession session) {
        return userSessionService.findSession(session.getId());
    }

    private void processVideoOffer(UserSession sender, UserSession receiver, JsonObject jsonMessage) {
        String sdpAnswer = initReceiverForVideoReception(sender, receiver, jsonMessage);
        sendSdpAnswerToReceiver(receiver, sender, sdpAnswer);
        gatherCandidates(receiver, sender);
    }

    private String initReceiverForVideoReception(UserSession sender, UserSession receiver, JsonObject jsonMessage) {
        String sdpOffer = jsonMessage.get("sdpOffer").getAsString();
        return receiver.prepareToReceiveVideoFrom(sender, sdpOffer);
    }

    private void sendSdpAnswerToReceiver(UserSession receiver, UserSession sender, String sdpAnswer) {
        JsonObject scParams = new JsonObject();
        scParams.addProperty("id", "receiveVideoAnswer");
        scParams.addProperty("name", sender.getName());
        scParams.addProperty("sdpAnswer", sdpAnswer);

        log.trace("USER {}: SdpAnswer for {} is {}", receiver.getName(), sender.getName(), sdpAnswer);
        receiver.sendMessage(scParams);
    }

    private void gatherCandidates(UserSession receiver, UserSession sender) {
        log.debug("Gathering candidates for user: {}", receiver.getName());
        receiver.gatherCandidates(sender);
    }

    @Override
    public void onError() {
        log.error("HandleSdpOfferAction : Error Occurred");
    }
}