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
}