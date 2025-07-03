package com.example.homecare_adm_app.api.service;


import com.example.homecare_adm_app.api.dto.UserConfigDTO;
import com.example.homecare_adm_app.api.dto.UserDTO;
import com.example.homecare_adm_app.api.exeption.BadRequestException;
import com.example.homecare_adm_app.api.exeption.ResourceNotFoundException;
import com.example.homecare_adm_app.api.mapper.UserConfigMapper;
import com.example.homecare_adm_app.api.mapper.UserMapper;
import com.example.homecare_adm_app.api.model.User;
import com.example.homecare_adm_app.api.model.UserConfig;
import com.example.homecare_adm_app.api.repository.UserConfigRepository;
import com.example.homecare_adm_app.api.repository.UserRepository;
import com.example.homecare_adm_app.api.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserConfigRepository userConfigRepository;
    private final UserMapper userMapper;
    private final UserConfigMapper userConfigMapper;
    private final PasswordEncoder passwordEncoder;

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return userMapper.toDto(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return userMapper.toDto(user);
    }

    @Transactional
    public UserDTO updateUser(String email, UserDTO userDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email já cadastrado");
        }

        user.setNome(userDTO.getNome() != null ? userDTO.getNome() : user.getNome());
        user.setEmail(userDTO.getEmail() != null ? userDTO.getEmail() : user.getEmail());
        user.setTelefone(userDTO.getTelefone() != null ? userDTO.getTelefone() : user.getTelefone());

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public UserDTO updateUserById(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (userDTO.getEmail() != null && !userDTO.getEmail().equals(user.getEmail()) &&
                userRepository.existsByEmail(userDTO.getEmail())) {
            throw new BadRequestException("Email já cadastrado");
        }

        user.setNome(userDTO.getNome() != null ? userDTO.getNome() : user.getNome());
        user.setEmail(userDTO.getEmail() != null ? userDTO.getEmail() : user.getEmail());
        user.setTelefone(userDTO.getTelefone() != null ? userDTO.getTelefone() : user.getTelefone());
        user.setIsActive(userDTO.isActive());

        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            user.setRoles(userDTO.getRoles());
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        User userToDelete = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        String currentEmail = SecurityUtils.getCurrentUserLogin().orElse(null);
        if (currentEmail == null) {
            throw new BadRequestException("Usuário autenticado não encontrado");
        }
        User currentUser = userRepository.findByEmail(currentEmail)
            .orElseThrow(() -> new ResourceNotFoundException("Usuário autenticado não encontrado"));
        Set<String> currentRoles = currentUser.getRoles();
        Set<String> targetRoles = userToDelete.getRoles();

        boolean isCurrentAdmin = currentRoles.contains("ADMIN");
        boolean isCurrentCoordenador = currentRoles.contains("COORDENADOR");
        boolean isTargetAdmin = targetRoles.contains("ADMIN");
        boolean isTargetCoordenador = targetRoles.contains("COORDENADOR");

        if (isCurrentAdmin) {
            userRepository.deleteById(id);
            return;
        }
        if (isCurrentCoordenador) {
            if (isTargetAdmin) {
                throw new BadRequestException("Coordenador não pode excluir um Admin");
            }
            userRepository.deleteById(id);
            return;
        }
        throw new BadRequestException("Você não tem permissão para excluir usuários");
    }

    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getSenha())) {
            throw new BadRequestException("Senha atual incorreta");
        }

        user.setSenha(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public UserConfigDTO updateUserConfig(String email, UserConfigDTO configDTO) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        UserConfig config = user.getUserConfig();
        if (config == null) {
            config = UserConfig.builder()
                    .user(user)
                    .build();
        }

        config.setTheme(configDTO.getTheme() != null ? configDTO.getTheme() : config.getTheme());
        config.setNotificationsEnabled(configDTO.isNotificationsEnabled());
        config.setEmailNotifications(configDTO.isEmailNotifications());

        UserConfig savedConfig = userConfigRepository.save(config);
        return userConfigMapper.toDto(savedConfig);
    }

    public UserDTO getByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        return userMapper.toDto(user);
    }
}