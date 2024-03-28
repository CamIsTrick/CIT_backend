package cit.camistrick.config;


import cit.camistrick.action.ErrorAction;
import cit.camistrick.action.ExitAction;
import cit.camistrick.action.HandleSdpOfferAction;
import cit.camistrick.action.IceCandidateAction;
import cit.camistrick.action.RoomFollowerAction;
import cit.camistrick.action.RoomLeaderAction;
import cit.camistrick.handler.KurentoActionResolver;
import cit.camistrick.handler.WebSocketHandler;
import cit.camistrick.repository.MemoryRoomRepository;
import cit.camistrick.repository.MemoryUserSessionRepository;
import cit.camistrick.service.RoomService;
import cit.camistrick.service.UserSessionService;
import java.util.Map;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.kurento.client.KurentoClient;
import org.kurento.client.KurentoClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

@EnableWebSocket
@Configuration
@Slf4j
public class KurentoConfig implements WebSocketConfigurer {

    @Value("${kms.url}")
    private String kmsUrl;

    @Bean
    public ServletServerContainerFactoryBean createServletServerContainerFactoryBean() {
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
        container.setMaxTextMessageBufferSize(32768);
        return container;
    }

    @Bean
    public KurentoClient kurentoClient() {
        String envKmsUrl = System.getenv("KMS_URL");
        if (Objects.isNull(envKmsUrl) || envKmsUrl.isEmpty()) {
            return createKurentoClient(kmsUrl);
        }
        return createKurentoClient(envKmsUrl);
    }

    private KurentoClient createKurentoClient(String kmsUrl) {
        return new KurentoClientBuilder()
            .setKmsWsUri(kmsUrl)
            .setConnectionTimeout(1800000L) // TimeOut 30 Minutes
            .onConnectionFailed(
                () -> log.error("Kurento connection status: connection attempt failed"))
            .onConnected(
                () -> log.info("Kurento connection status: connected"))
            .onDisconnected(
                () -> log.error("Kurento connection status: disconnected"))
            .onReconnecting(
                () -> log.info("Kurento connection status: reconnecting..."))
            .onReconnected(sameServer -> {
                log.info("Kurento connection status: reconnected");
                log.info("Reconnected to same server: {}", sameServer);
            })
            .connect();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(kurentoHandler(), "/signal").setAllowedOrigins("*");
    }

    @Bean
    public WebSocketHandler kurentoHandler() {
        return new WebSocketHandler(kurentoActionResolver());
    }

    @Bean
    public KurentoActionResolver kurentoActionResolver() {
        RoomService roomService = roomService();
        UserSessionService userSessionService = userSessionService();
        return new KurentoActionResolver(Map.of(
            "createRoom", new RoomLeaderAction(roomService, userSessionService),
            "joinRoom", new RoomFollowerAction(roomService, userSessionService),
            "exitRoom", new ExitAction(roomService, userSessionService),
            "receiveVideoFrom", new HandleSdpOfferAction(userSessionService),
            "onIceCandidate", new IceCandidateAction(userSessionService),
            "error", new ErrorAction()
        ));
    }

    @Bean
    public RoomService roomService() {
        return new RoomService(kurentoClient(), new MemoryRoomRepository());
    }

    @Bean
    public UserSessionService userSessionService() {
        return new UserSessionService(new MemoryUserSessionRepository());
    }
}