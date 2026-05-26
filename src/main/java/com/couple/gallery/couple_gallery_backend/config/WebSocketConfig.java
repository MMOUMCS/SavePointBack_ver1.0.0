package com.couple.gallery.couple_gallery_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final com.couple.gallery.couple_gallery_backend.config.GameStatusWebSocketHandler gameStatusWebSocketHandler;

    public WebSocketConfig(com.couple.gallery.couple_gallery_backend.config.GameStatusWebSocketHandler gameStatusWebSocketHandler) {
        this.gameStatusWebSocketHandler = gameStatusWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry
                .addHandler(gameStatusWebSocketHandler, "/ws/game-status")
                .setAllowedOrigins("*");
    }
}