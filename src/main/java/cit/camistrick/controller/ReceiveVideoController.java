package cit.camistrick.controller;

import java.io.IOException;

import cit.camistrick.domain.UserSession;
import org.kurento.client.*;
import org.springframework.web.socket.TextMessage;

import com.google.gson.JsonObject;

import static org.kurento.jsonrpc.client.JsonRpcClient.log;

public class ReceiveVideoController {

    public void sendMessage(UserSession sender, JsonObject message) throws IOException {
        log.debug("USER {}: Sending message {}", sender.getName(), message);
        synchronized (sender.getSession()) {
            sender.getSession().sendMessage(new TextMessage(message.toString()));
        }
    }

    public void sdpAnswer(UserSession sender, UserSession receiver, String sdpOffer) throws IOException {
        log.info("USER {}: SdpOffer for {} receiveVideoFrom", receiver.getName(), sender.getName());

        final JsonObject receiveVideoResponse = new JsonObject();
        receiveVideoResponse.addProperty("id", "receiveVideoAnswer");
        receiveVideoResponse.addProperty("name", sender.getName());
        receiveVideoResponse.addProperty("sdpAnswer", sdpOffer);

        log.info("USER {}: SdpAnswer for {} receiveVideoAnswer", receiver.getName(), sender.getName());
        this.sendMessage(sender, receiveVideoResponse);
    }

    public void receiveVideoFrom(UserSession sender, UserSession receiver, String sdpOffer) throws IOException {
        log.info("USER {}: connecting with {} in room {}", receiver.getName(), sender.getName(), receiver.getRoomName());

        log.trace("USER {}: SdpOffer for {} is {}", receiver.getName(), sender.getName(), sdpOffer);

        final String ipSdpAnswer = sender.getEndpointForUser(sender).processOffer(sdpOffer);
        final JsonObject scParams = new JsonObject();
        scParams.addProperty("id", "receiveVideoAnswer");
        scParams.addProperty("name", sender.getName());
        scParams.addProperty("sdpAnswer", ipSdpAnswer);

        log.trace("USER {}: SdpAnswer for {} is {}", receiver.getName(), sender.getName(), ipSdpAnswer);
        this.sendMessage(sender, scParams);
        log.debug("gather candidates");
        sender.getEndpointForUser(sender).gatherCandidates();
    }
}