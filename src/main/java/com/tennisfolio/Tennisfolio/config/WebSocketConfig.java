package com.tennisfolio.Tennisfolio.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final Map<String, Map<String, String>> sessionSubs = new ConcurrentHashMap<>();

    // 클라이언트 연결 경로
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        registry.addEndpoint("/ws") // WebScoekt 엔드포인트
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS(); // SockJS fallback 지원 (브라우저 호환성)
    }

    // STOMP 메시지 라우팅 경로 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){
        registry.enableSimpleBroker("/topic"); // 서버 -> 클라이언트 브로드캐스트 경로 prefix
        registry.setApplicationDestinationPrefixes("/app"); // 클라이언트 -> 서버 전송 prefix

    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration reg){
        reg.interceptors(inboundGuard());
    }

    @Bean
    public ChannelInterceptor inboundGuard() {

        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
                StompCommand cmd = acc.getCommand();

                if(cmd == null){
                    return message;
                }

                switch(cmd){
                    case CONNECT -> {
                        log.info("[WS] CONNECT sid={}", acc.getSessionId());
                    }

                    case SUBSCRIBE -> {
                        String sid = acc.getSessionId();
                        String dest = acc.getDestination();
                        String subId = acc.getSubscriptionId();

                        if(sid == null || dest == null){
                            return message;
                        }

                        Map<String, String> subs = sessionSubs.computeIfAbsent(sid, k -> new ConcurrentHashMap<>());

                        if(subs.containsValue(dest)){
                            log.warn("[WS] Duplicate SUBSCRIBE dropped: sid={} dest={}", sid, dest);
                            return null;
                        }

                        if (subId == null || subId.isBlank()) {
                            log.warn("[WS] SUBSCRIBE missing id; sid={} dest={}. Dropping.", sid, dest);
                            return null;
                        }

                        log.info("[WS] SUBSCRIBE ok: sid={} subId={} dest={}", sid, subId, dest);
                    }
                    case UNSUBSCRIBE -> {
                        String sid = acc.getSessionId();
                        String subId = acc.getSubscriptionId();
                        if(sid != null && subId != null){
                            Map<String, String> subs = sessionSubs.get(sid);
                            if(subs != null){
                                String removed = subs.remove(subId);
                                log.info("[WS] UNSUBSCRIBE: sid={} subId={} dest={}", sid, subId, removed);
                            }
                        }
                    }

                    case DISCONNECT -> {
                        String sid = acc.getSessionId();
                        if (sid != null){
                            sessionSubs.remove(sid);
                            log.debug("[WS] DISCONNECT: sid={} (subs cleared)", sid);
                        }
                    }
                    default -> {

                    }
                }

                return message;


            }
        };
    }

}

