package com.nevvesdev.guardianapi.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenProvider - Testes Unitários")
class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String SECRET = "your-super-secret-key-that-is-at-least-32-characters-long-for-hs256";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpirationMs", 86400000);
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenExpirationMs", 604800000);
    }

    @Test
    @DisplayName("Deve gerar token a partir de Authentication")
    void deveGerarTokenAPartirDeAuthentication() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "joao@nevvesdev.com", "senha1234"
        );

        String token = jwtTokenProvider.generateToken(authentication);

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Deve gerar token a partir de email")
    void deveGerarTokenAPartirDeEmail() {
        String token = jwtTokenProvider.generateTokenFromEmail("joao@nevvesdev.com");

        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Deve extrair email do token")
    void deveExtrairEmailDoToken() {
        String token = jwtTokenProvider.generateTokenFromEmail("joao@nevvesdev.com");
        String email = jwtTokenProvider.getUserEmailFromToken(token);

        assertThat(email).isEqualTo("joao@nevvesdev.com");
    }

    @Test
    @DisplayName("Deve validar token válido")
    void deveValidarTokenValido() {
        String token = jwtTokenProvider.generateTokenFromEmail("joao@nevvesdev.com");

        boolean isValid = jwtTokenProvider.validateToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar token malformado")
    void deveInvalidarTokenMalformado() {
        boolean isValid = jwtTokenProvider.validateToken("token-invalido");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve gerar refresh token")
    void deveGerarRefreshToken() {
        String refreshToken = jwtTokenProvider.generateRefreshToken("joao@nevvesdev.com");

        assertThat(refreshToken).isNotNull().isNotEmpty();
        assertThat(jwtTokenProvider.validateToken(refreshToken)).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se token não está expirado")
    void deveVerificarSeTokenNaoEstaExpirado() {
        String token = jwtTokenProvider.generateTokenFromEmail("joao@nevvesdev.com");

        boolean isExpired = jwtTokenProvider.isTokenExpired(token);

        assertThat(isExpired).isFalse();
    }
}