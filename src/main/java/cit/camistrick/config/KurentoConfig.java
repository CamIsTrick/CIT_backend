package cit.camistrick.config;

import cit.camistrick.action.RoomFollowerAction;
import cit.camistrick.action.RoomLeaderAction;
import cit.camistrick.handler.KurentoActionResolver;
import cit.camistrick.handler.WebSocketHandler;
import cit.camistrick.service.RoomManager;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
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
        KurentoActionResolver kurentoActionResolver = configKurentoHandler();
        return new WebSocketHandler(kurentoActionResolver);
    }

    @Bean
    public KurentoActionResolver configKurentoHandler() {
        return new KurentoActionResolver(Map.of(
                "createRoom", new RoomLeaderAction(roomManager(), createMediaPipeLine()),
                "joinRoom", new RoomFollowerAction(roomManager(), createMediaPipeLine())
        ));
    }

    @Bean
    public RoomManager roomManager() {
        return new RoomManager();
    }

    private MediaPipeline createMediaPipeLine() {
        return kurentoClient().createMediaPipeline();
    }
}
