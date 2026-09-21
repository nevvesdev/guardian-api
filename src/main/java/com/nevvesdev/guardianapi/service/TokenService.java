package com.nevvesdev.guardianapi.service;

import com.nevvesdev.guardianapi.dto.request.RefreshTokenRequest;
import com.nevvesdev.guardianapi.dto.response.AuthResponse;
import com.nevvesdev.guardianapi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String BLACKLIST_PREFIX = "blacklist:";

    public AuthResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (isTokenBlacklisted(refreshToken)) {
            throw new RuntimeException("Refresh token inválido ou expirado");
        }

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new RuntimeException("Refresh token inválido");
        }

        String email = jwtTokenProvider.getUserEmailFromToken(refreshToken);

        String newAccessToken = jwtTokenProvider.generateTokenFromEmail(email);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(email);

        blacklistToken(refreshToken, jwtTokenProvider.getExpirationTime(refreshToken));

        log.info("Token renovado com sucesso para: {}", email);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(86400000L)
                .email(email)
                .build();
    }

    public void revokeToken(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            long expirationTime = jwtTokenProvider.getExpirationTime(token);
            blacklistToken(token, expirationTime);
            log.info("Token revogado com sucesso");
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(BLACKLIST_PREFIX + token)
        );
    }

    private void blacklistToken(String token, long expirationTimeMs) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token,
                "revoked",
                expirationTimeMs,
                TimeUnit.MILLISECONDS
        );
    }
}