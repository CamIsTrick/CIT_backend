package cit.camistrick.config;

import cit.camistrick.KurentoHandler;
import org.kurento.client.KurentoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

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

    @Bean
    public KurentoHandler kurentoHandler() {
        return new KurentoHandler();
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(kurentoHandler(), "/signal").setAllowedOrigins("*");
    }
}
