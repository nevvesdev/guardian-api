package com.nevvesdev.guardianapi.service;

import com.nevvesdev.guardianapi.dto.request.LoginRequest;
import com.nevvesdev.guardianapi.dto.request.RegisterRequest;
import com.nevvesdev.guardianapi.dto.response.AuthResponse;
import com.nevvesdev.guardianapi.entity.Role;
import com.nevvesdev.guardianapi.entity.User;
import com.nevvesdev.guardianapi.repository.RoleRepository;
import com.nevvesdev.guardianapi.repository.UserRepository;
import com.nevvesdev.guardianapi.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String accessToken = jwtTokenProvider.generateToken(authentication);
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

        log.info("Usuário autenticado: {}", request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime(accessToken))
                .email(request.getEmail())
                .build();
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + request.getEmail());
        }

        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> {
                    Role newRole = Role.builder()
                            .name("USER")
                            .description("Usuário padrão")
                            .build();
                    return roleRepository.save(newRole);
                });

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .build();

        userRepository.save(user);

        log.info("Novo usuário registrado: {}", request.getEmail());

        String accessToken = jwtTokenProvider.generateTokenFromEmail(request.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(request.getEmail());

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getExpirationTime(accessToken))
                .email(request.getEmail())
                .build();
    }
}