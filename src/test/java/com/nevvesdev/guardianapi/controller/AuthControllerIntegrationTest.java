package com.nevvesdev.guardianapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nevvesdev.guardianapi.dto.request.LoginRequest;
import com.nevvesdev.guardianapi.dto.request.RegisterRequest;
import com.nevvesdev.guardianapi.entity.Role;
import com.nevvesdev.guardianapi.entity.User;
import com.nevvesdev.guardianapi.repository.RoleRepository;
import com.nevvesdev.guardianapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AuthController - Testes de Integração")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    private Role userRole;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).build();

        userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(
                        Role.builder()
                                .name("USER")
                                .description("Usuário padrão")
                                .build()
                ));
    }

    @Test
    @DisplayName("POST /auth/register - deve registrar usuário com sucesso")
    void deveRegistrarUsuarioComSucesso() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("João Neves");
        request.setEmail("novo@nevvesdev.com");
        request.setPassword("senha1234");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("novo@nevvesdev.com"));
    }

    @Test
    @DisplayName("POST /auth/register - deve retornar 422 ao registrar email duplicado")
    void deveRetornar422AoRegistrarEmailDuplicado() throws Exception {
        User existingUser = User.builder()
                .email("duplicado@nevvesdev.com")
                .fullName("Usuário Existente")
                .password(passwordEncoder.encode("senha1234"))
                .isActive(true)
                .roles(new ArrayList<>())
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest();
        request.setFullName("Outro Usuário");
        request.setEmail("duplicado@nevvesdev.com");
        request.setPassword("senha1234");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value("Email já cadastrado: duplicado@nevvesdev.com"));
    }

    @Test
    @DisplayName("POST /auth/login - deve realizar login com sucesso")
    void deveRealizarLoginComSucesso() throws Exception {
        User user = User.builder()
                .email("login@nevvesdev.com")
                .fullName("João Neves")
                .password(passwordEncoder.encode("senha1234"))
                .isActive(true)
                .roles(new ArrayList<>())
                .build();
        userRepository.save(user);

        LoginRequest request = new LoginRequest();
        request.setEmail("login@nevvesdev.com");
        request.setPassword("senha1234");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.email").value("login@nevvesdev.com"));
    }

    @Test
    @DisplayName("POST /auth/login - deve retornar 401 com credenciais inválidas")
    void deveRetornar401ComCredenciaisInvalidas() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("inexistente@nevvesdev.com");
        request.setPassword("senhaErrada");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/register - deve retornar 400 com dados inválidos")
    void deveRetornar400ComDadosInvalidos() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("");
        request.setEmail("email-invalido");
        request.setPassword("123");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray());
    }
}