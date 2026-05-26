package com.couple.gallery.couple_gallery_backend.config;

import com.couple.gallery.couple_gallery_backend.service.GameSessionService;
import com.couple.gallery.couple_gallery_backend.service.UserService; // 유저 정보 조회를 위해 필요!
import com.couple.gallery.couple_gallery_backend.config.security.JwtTokenProvider;
import com.couple.gallery.couple_gallery_backend.domain.User; // 언니의 User 엔티티 경로
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
@RequiredArgsConstructor
public class GameStatusWebSocketHandler extends TextWebSocketHandler {

    private final GameSessionService gameSessionService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService; // 유저/커플 정보를 가져오기 위해 주입!
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final CopyOnWriteArrayList<WebSocketSession> reactClients = new CopyOnWriteArrayList<>();
    private WebSocketSession electronSession = null;
    private String lastStatusPayload = null;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        Map<String, String> params = parseQueryParams(session.getUri().getQuery());
        String token = params.get("token");
        String clientType = params.get("client");

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 1. 토큰에서 이메일 추출
            String email = jwtTokenProvider.getAuthentication(token).getName();

            try {
                // 2. [수정된 부분] findUserByEmail 호출!
                User user = userService.findUserByEmail(email);

                if (user != null) {
                    // 세션 이름표(Attributes)에 저장
                    session.getAttributes().put("userId", user.getId());

                    // 만약 user 엔티티에 getCoupleId()가 있다면 바로 사용
                    // 커플이 연결 안 된 상태면 null일 수 있으니 0L 등으로 기본값 처리
                    Long coupleId = (user.getCoupleId() != null) ? user.getCoupleId() : 0L;
                    session.getAttributes().put("coupleId", coupleId);

                    if ("electron".equals(clientType)) {
                        electronSession = session;
                        log.info("[WS] Electron 연결 성공! 유저ID: {}", user.getId());
                    } else {
                        reactClients.add(session);
                        log.info("[WS] React 앱 연결 성공! 유저ID: {}", user.getId());
                        if (lastStatusPayload != null) {
                            session.sendMessage(new TextMessage(lastStatusPayload));
                        }
                    }
                }
            } catch (Exception e) {
                log.error("[WS] 유저 정보를 찾을 수 없습니다: {}", email);
                session.close(CloseStatus.BAD_DATA);
            }
        } else {
            session.close(CloseStatus.BAD_DATA);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        if (!isElectron(session)) return;

        Long userId = (Long) session.getAttributes().get("userId");
        Long coupleId = (Long) session.getAttributes().get("coupleId");

        String payload = message.getPayload();
        var node = objectMapper.readTree(payload);

        // Electron이 쏘는 메시지의 "type"이 "status"일 때만 처리
        if (node.has("type") && "status".equals(node.get("type").asText())) {
            boolean isRunning = node.get("running").asBoolean();
            String process = node.get("process").asText();

            // ─── DB 세션 기록 ───
            if (isRunning) {
                gameSessionService.startSession(userId, coupleId, process);
            } else {
                gameSessionService.endSession(userId);
            }

            lastStatusPayload = payload;
            broadcastToReact(payload);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        if (isElectron(session)) {
            Long userId = (Long) session.getAttributes().get("userId");
            if (userId != null) gameSessionService.endSession(userId);
            electronSession = null;
        } else {
            reactClients.remove(session);
        }
    }

    private void broadcastToReact(String payload) throws Exception {
        for (WebSocketSession client : reactClients) {
            if (client.isOpen()) client.sendMessage(new TextMessage(payload));
        }
    }

    private boolean isElectron(WebSocketSession session) {
        String query = session.getUri().getQuery();
        return query != null && query.contains("client=electron");
    }

    private Map<String, String> parseQueryParams(String query) {
        if (query == null) return Map.of();
        return Map.ofEntries(java.util.Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .filter(a -> a.length > 0)
                .map(a -> java.util.Map.entry(a[0], a.length > 1 ? a[1] : ""))
                .toArray(java.util.Map.Entry[]::new));
    }
}