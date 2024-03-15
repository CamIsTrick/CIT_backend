package cit.camistrick.config;

import cit.camistrick.action.ErrorAction;
import cit.camistrick.action.HandleSdpOfferAction;
import cit.camistrick.action.RoomFollowerAction;
import cit.camistrick.action.RoomLeaderAction;
import cit.camistrick.handler.KurentoActionResolver;
import cit.camistrick.handler.WebSocketHandler;
import cit.camistrick.repository.MemoryUserSessionRepository;
import cit.camistrick.service.RoomManager;
import cit.camistrick.service.UserSessionService;
import org.kurento.client.KurentoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

import java.util.Map;
import java.util.Objects;

@EnableWebSocket
@Configuration
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
            return KurentoClient.create(kmsUrl);
        }
        return KurentoClient.create(envKmsUrl);
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
        RoomManager roomManager = roomManager();
        UserSessionService userSessionService = userSessionService();
        return new KurentoActionResolver(Map.of(
                "createRoom", new RoomLeaderAction(roomManager, userSessionService),
                "joinRoom", new RoomFollowerAction(roomManager, userSessionService),
                "receiveVideoFrom", new HandleSdpOfferAction(userSessionService)
        ));
    }

    @Bean
    public RoomManager roomManager() {
        return new RoomManager(kurentoClient());
    }

    @Bean
    public UserSessionService userSessionService() {
        return new UserSessionService(new MemoryUserSessionRepository());
    }
}
