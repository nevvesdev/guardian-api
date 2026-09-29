package com.nevvesdev.guardianapi.service;

import com.nevvesdev.guardianapi.dto.request.RefreshTokenRequest;
import com.nevvesdev.guardianapi.dto.response.AuthResponse;
import com.nevvesdev.guardianapi.exception.UnauthorizedException;
import com.nevvesdev.guardianapi.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TokenService - Testes Unitários")
class TokenServiceTest {

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private TokenService tokenService;

    private RefreshTokenRequest refreshTokenRequest;

    @BeforeEach
    void setUp() {
        refreshTokenRequest = new RefreshTokenRequest();
        refreshTokenRequest.setRefreshToken("valid-refresh-token");
    }

    @Test
    @DisplayName("Deve renovar token com sucesso")
    void deveRenovarTokenComSucesso() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(true);
        when(jwtTokenProvider.getUserEmailFromToken("valid-refresh-token"))
                .thenReturn("joao@nevvesdev.com");
        when(jwtTokenProvider.generateTokenFromEmail("joao@nevvesdev.com"))
                .thenReturn("new-access-token");
        when(jwtTokenProvider.generateRefreshToken("joao@nevvesdev.com"))
                .thenReturn("new-refresh-token");
        when(jwtTokenProvider.getExpirationTime("valid-refresh-token")).thenReturn(3600000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        AuthResponse response = tokenService.refreshToken(refreshTokenRequest);

        assertThat(response).isNotNull();
        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        assertThat(response.getRefreshToken()).isEqualTo("new-refresh-token");
        assertThat(response.getEmail()).isEqualTo("joao@nevvesdev.com");
    }

    @Test
    @DisplayName("Deve lançar exceção quando token está na blacklist")
    void deveLancarExcecaoQuandoTokenEstaNaBlacklist() {
        when(redisTemplate.hasKey(anyString())).thenReturn(true);

        assertThatThrownBy(() -> tokenService.refreshToken(refreshTokenRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Refresh token inválido ou expirado");

        verify(jwtTokenProvider, never()).validateToken(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando token é inválido")
    void deveLancarExcecaoQuandoTokenEhInvalido() {
        when(redisTemplate.hasKey(anyString())).thenReturn(false);
        when(jwtTokenProvider.validateToken("valid-refresh-token")).thenReturn(false);

        assertThatThrownBy(() -> tokenService.refreshToken(refreshTokenRequest))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Refresh token inválido");
    }

    @Test
    @DisplayName("Deve revogar token com sucesso")
    void deveRevogarTokenComSucesso() {
        when(jwtTokenProvider.validateToken("valid-token")).thenReturn(true);
        when(jwtTokenProvider.getExpirationTime("valid-token")).thenReturn(3600000L);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        tokenService.revokeToken("valid-token");

        verify(valueOperations).set(anyString(), eq("revoked"), anyLong(), any());
    }
}