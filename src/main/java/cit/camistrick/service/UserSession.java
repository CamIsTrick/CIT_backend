package cit.camistrick.service;

import com.google.gson.JsonObject;
import org.kurento.client.Continuation;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import java.io.Closeable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class UserSession implements Closeable {

    private static final Logger log = LoggerFactory.getLogger(UserSession.class);

    private final String name;
    private final WebSocketSession session;
    private final MediaPipeline pipeline;
    private final WebRtcEndpoint outgoingMedia;
    private final ConcurrentMap<String, WebRtcEndpoint> incomingMedia = new ConcurrentHashMap<>();

    public UserSession(final String name, final WebSocketSession session, MediaPipeline pipeline) {
        this.pipeline = pipeline;
        this.name = name;
        this.session = session;
        this.outgoingMedia = createWebRtcEndpoint();
        addIceCandidateListener(this.outgoingMedia, this.name);
    }

    private WebRtcEndpoint createWebRtcEndpoint() {
        return new WebRtcEndpoint.Builder(pipeline).build();
    }

    private void addIceCandidateListener(WebRtcEndpoint endpoint, String endpointName) {
        // ICE Candidate 처리
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
        boolean loopback = sender.getName().equals(name);
        if (loopback) {
            log.debug("PARTICIPANT {}: configuring loopback", this.name);
        }
        return loopback;
    }

    private WebRtcEndpoint getIncomingEndpoint(UserSession sender) {
        return incomingMedia.get(sender.getName());
    }

    private WebRtcEndpoint createAndRegisterIncomingEndpoint(UserSession sender) {
        WebRtcEndpoint incoming = createWebRtcEndpoint();
        addIceCandidateListener(incoming, sender.getName());
        incomingMedia.put(sender.getName(), incoming);
        return incoming;
    }

    public void cancelVideoFrom(final String senderName) {
        log.debug("PARTICIPANT {}: canceling video reception from {}", this.name, senderName);
        final WebRtcEndpoint incoming = incomingMedia.remove(senderName);

        releaseEndpoint(senderName, incoming);
    }

    private void releaseEndpoint(String endpointName, WebRtcEndpoint endpoint) {
        log.debug("PARTICIPANT {}: removing endpoint for {}", this.name, endpointName);
        endpoint.release(new Continuation<>() {
            @Override
            public void onSuccess(Void result) {
                log.trace("PARTICIPANT {}: Released successfully incoming EP for {}",
                        UserSession.this.name, endpointName);
            }

            @Override
            public void onError(Throwable cause) {
                log.warn("PARTICIPANT {}: Could not release incoming EP for {}", UserSession.this.name,
                        endpointName);
            }
        });
    }

    @Override
    public void close() {
        log.debug("PARTICIPANT {}: Releasing resources", this.name);
        releaseAllIncomingEndpoints();
        releaseEndpoint(this.name, outgoingMedia);
    }

    private void releaseAllIncomingEndpoints() {
        incomingMedia.keySet().forEach(remoteParticipantName -> {
            log.trace("PARTICIPANT {}: Released incoming EP for {}", this.name, remoteParticipantName);
            final WebRtcEndpoint ep = this.incomingMedia.get(remoteParticipantName);
            releaseEndpoint(remoteParticipantName, ep);
        });
    }

    public void sendMessage(JsonObject message) {
        //WebSocket 통신
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof UserSession other) {
            return name.equals(other.name);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}