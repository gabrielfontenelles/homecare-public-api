package com.example.homecare_adm_app.api.service;


import com.example.homecare_adm_app.api.dto.AuthRequestDTO;
import com.example.homecare_adm_app.api.dto.AuthResponseDTO;
import com.example.homecare_adm_app.api.dto.RegisterRequestDTO;
import com.example.homecare_adm_app.api.dto.UserDTO;
import com.example.homecare_adm_app.api.exeption.ResourceNotFoundException;
import com.example.homecare_adm_app.api.exeption.BadRequestException;
import com.example.homecare_adm_app.api.mapper.UserMapper;
import com.example.homecare_adm_app.api.model.User;
import com.example.homecare_adm_app.api.model.UserConfig;
import com.example.homecare_adm_app.api.repository.UserRepository;
import com.example.homecare_adm_app.api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.password-reset.expiration}")
    private long passwordResetExpiration;

    @Transactional
    public AuthResponseDTO authenticate(AuthRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getSenha()
                )
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email já cadastrado");
        }

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .telefone(request.getTelefone())
                .roles(Collections.singleton("USER"))
                .isActive(true)
                .build();

        UserConfig config = UserConfig.builder()
                .user(user)
                .theme("dark")
                .notificationsEnabled(true)
                .emailNotifications(true)
                .build();

        user.setUserConfig(config);
        userRepository.save(user);

        return createAuthResponse(user);
    }

    @Transactional
    public AuthResponseDTO refreshToken(String refreshToken) {
        String userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail == null) {
            throw new BadRequestException("Token inválido");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadRequestException("Token expirado ou inválido");
        }

        return createAuthResponse(user);
    }


    @Transactional
    private AuthResponseDTO createAuthResponse(User user) {
        try {
            UserDetails userDetails = user;
            String accessToken = jwtService.generateToken(userDetails);
            String refreshToken = jwtService.generateRefreshToken(userDetails);

            // Forçar o carregamento do userConfig
            if (user.getUserConfig() != null) {
                user.getUserConfig().getTheme(); // Força o carregamento
            }

            System.out.println("User antes do mapeamento: " + user);
            UserDTO userDTO = userMapper.toDto(user);
            System.out.println("UserDTO após mapeamento: " + userDTO);

            return AuthResponseDTO.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .user(userDTO)
                    .build();
        } catch (Exception e) {
            System.err.println("Erro no createAuthResponse: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Transactional
    public void sendPasswordResetEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElse(null);

        
        if (user == null) return;

        String token = UUID.randomUUID().toString();
        user.setResetPasswordToken(token);
        user.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(30)); 
        userRepository.save(user);

        String resetLink = "http://localhost:3000/reset-password?token=" + token; 
        String message = "Clique no link para redefinir sua senha: " + resetLink;

        emailService.sendEmail(user.getEmail(), "Recuperação de Senha", message);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new BadRequestException("Token inválido ou expirado."));

        if (user.getResetPasswordExpiry() == null || user.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Token expirado.");
        }

        user.setSenha(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordExpiry(null);
        userRepository.save(user);
    }

    @Transactional
    public UserDTO getCurrentUser() {
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return userMapper.toDto(user);
    }
}