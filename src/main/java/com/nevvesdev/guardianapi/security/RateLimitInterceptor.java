package com.nevvesdev.guardianapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevvesdev.guardianapi.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    @Value("${rate-limit.max-requests:100}")
    private int maxRequests;

    @Value("${rate-limit.window-seconds:60}")
    private int windowSeconds;

    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String identifier = getIdentifier(request);
        String key = RATE_LIMIT_PREFIX + identifier;

        Long currentCount = redisTemplate.opsForValue().increment(key);

        if (currentCount == 1) {
            redisTemplate.expire(key, windowSeconds, TimeUnit.SECONDS);
        }

        Long ttl = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        response.setHeader("X-RateLimit-Limit", String.valueOf(maxRequests));
        response.setHeader("X-RateLimit-Remaining", String.valueOf(Math.max(0, maxRequests - currentCount)));
        response.setHeader("X-RateLimit-Reset", String.valueOf(ttl));

        if (currentCount > maxRequests) {
            log.warn("Rate limit excedido para: {}", identifier);

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            ErrorResponse errorResponse = ErrorResponse.builder()
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .error("Too Many Requests")
                    .message("Limite de requisições excedido. Tente novamente em " + ttl + " segundos.")
                    .path(request.getRequestURI())
                    .timestamp(LocalDateTime.now())
                    .build();

            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
            return false;
        }

        return true;
    }

    private String getIdentifier(HttpServletRequest request) {
        String userEmail = null;

        if (request.getUserPrincipal() != null) {
            userEmail = request.getUserPrincipal().getName();
        }

        if (userEmail != null) {
            return "user:" + userEmail;
        }

        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return "ip:" + xForwardedFor.split(",")[0].trim();
        }

        return "ip:" + request.getRemoteAddr();
    }
}