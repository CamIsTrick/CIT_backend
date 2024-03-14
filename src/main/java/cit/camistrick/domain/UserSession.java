package cit.camistrick.domain;

import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.Continuation;
import org.kurento.client.IceCandidate;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.kurento.jsonrpc.JsonUtils;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class UserSession implements Closeable {
    private final String name;
    private final WebSocketSession session;
    private final MediaPipeline pipeline;
    private final WebRtcEndpoint outgoingMedia;
    private final ConcurrentMap<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();
    private final String roomId;

    private UserSession(final String name, String roomId, final WebSocketSession session, MediaPipeline pipeline) {
        this.pipeline = pipeline;
        this.name = name;
        this.session = session;
        this.roomId = roomId;
        this.outgoingMedia = createWebRtcEndpoint();
        addIceCandidateListener(this.outgoingMedia, getSessionId());
    }

    public static UserSession createUserSession(String name, String roomId, WebSocketSession session, MediaPipeline pipeline) {
        return new UserSession(name, roomId, session, pipeline);
    }

    private WebRtcEndpoint createWebRtcEndpoint() {
        return new WebRtcEndpoint.Builder(pipeline).build();
    }

    private void addIceCandidateListener(WebRtcEndpoint endpoint, String endpointName) {
        endpoint.addIceCandidateFoundListener(event -> sendIceCandidateMessage(endpointName, event.getCandidate()));
    }

    private void sendIceCandidateMessage(String endpointName, IceCandidate candidate) {
        JsonObject response = new JsonObject();
        response.addProperty("id", "iceCandidate");
        response.addProperty("name", endpointName);
        response.add("candidate", JsonUtils.toJsonObject(candidate));
        sendMessage(response);
    }

    public WebRtcEndpoint getOutgoingWebRtcPeer() {
        return outgoingMedia;
    }

    public String getName() {
        return name;
    }

    public WebSocketSession getSession() {
        return session;
    }

    public String getRoomId() {
        return this.roomId;
    }

    public String getSessionId() {
        return session.getId();
    }

    public String prepareToReceiveVideoFrom(UserSession sender, String sdpOffer) {
        final WebRtcEndpoint incoming = getEndpointForUser(sender);
        String sdpAnswer = incoming.processOffer(sdpOffer);
        return sdpAnswer;
    }

    public void gatherCandidates(UserSession sender) {
        final WebRtcEndpoint incoming = getEndpointForUser(sender);
        incoming.gatherCandidates();
    }

    private WebRtcEndpoint getEndpointForUser(final UserSession sender) {
        if (isLoopback(sender)) {
            return outgoingMedia;
        }

        log.debug("PARTICIPANT {}: receiving video from {}", this.name, sender.getName());

        WebRtcEndpoint incoming = getIncomingEndpoint(sender);
        if (incoming == null) {
            log.debug("PARTICIPANT {}: creating new endpoint for {}", this.name, sender.getName());
            incoming = createAndRegisterIncomingEndpoint(sender);
        }

        log.debug("PARTICIPANT {}: obtained endpoint for {}", this.name, sender.getName());
        sender.getOutgoingWebRtcPeer().connect(incoming);

        return incoming;
    }

    private boolean isLoopback(UserSession sender) {
        boolean loopback = sender.getSessionId().equals(this.getSessionId());
        if (loopback) {
            log.debug("PARTICIPANT {}: configuring loopback", this.name);
        }
        return loopback;
    }

    private WebRtcEndpoint getIncomingEndpoint(UserSession sender) {
        return incomingMedia.get(sender.getSessionId());
    }

    private WebRtcEndpoint createAndRegisterIncomingEndpoint(UserSession sender) {
        WebRtcEndpoint incoming = createWebRtcEndpoint();
        addIceCandidateListener(incoming, sender.getSessionId());
        incomingMedia.put(sender.getSessionId(), incoming);
        return incoming;
    }

    public void cancelVideoFrom(final String senderSessionId) {
        log.debug("PARTICIPANT {}: canceling video reception from {}", this.name, senderSessionId);
        final WebRtcEndpoint incoming = incomingMedia.remove(senderSessionId);

        releaseEndpoint(senderSessionId, incoming);
    }

    private void releaseEndpoint(String endpointName, WebRtcEndpoint endpoint) {
        log.debug("PARTICIPANT {}: removing endpoint for {}", this.name, endpointName);
        endpoint.release(new Continuation<>() {
            @Override
            public void onSuccess(Void result) {
                log.trace("PARTICIPANT {}: Released successfully incoming EP for {}", UserSession.this.name, endpointName);
            }

            @Override
            public void onError(Throwable cause) {
                log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.name, endpointName);
            }
        });
    }

    @Override
    public void close() {
        log.debug("PARTICIPANT {}: Releasing resources", this.name);
        releaseAllIncomingEndpoints();
        releaseEndpoint(this.getSessionId(), outgoingMedia);
    }

    private void releaseAllIncomingEndpoints() {
        incomingMedia.keySet().forEach(remoteParticipantSessionId -> {
            log.trace("PARTICIPANT {}: Released incoming EP for {}", this.name, remoteParticipantSessionId);
            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantSessionId);
            releaseEndpoint(remoteParticipantSessionId, ep);
        });
    }

    public void sendMessage(JsonObject message) {
        log.debug("USER {}: Sending message {}", name, message);

        try {
            synchronized (session) {
                session.sendMessage(new TextMessage(message.toString()));
            }
        } catch (IOException e) {
            log.debug(e.getMessage());
        }
    }

    public void addCandidate(IceCandidate candidate, String sessionId) {
        if (this.getSessionId().equals(sessionId)) {
            log.info("USER {} : outgoingMedia.addIceCandidate : {} ", this.name, sessionId);
            outgoingMedia.addIceCandidate(candidate);
            return;
        }

        WebRtcEndpoint webRtc = incomingMedia.get(sessionId);
        if (webRtc != null) {
            log.info("USER {} : incoming.addIceCandidate to {} ", this.name, sessionId);
            webRtc.addIceCandidate(candidate);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof UserSession other) {
            return session.getId().equals(other.getSessionId());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return session.getId().hashCode();
    }
}
