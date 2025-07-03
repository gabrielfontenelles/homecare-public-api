package com.example.homecare_adm_app.api.controller;

import com.example.homecare_adm_app.api.dto.ChangePasswordDTO;
import com.example.homecare_adm_app.api.dto.MessageResponseDTO;
import com.example.homecare_adm_app.api.dto.UserConfigDTO;
import com.example.homecare_adm_app.api.dto.UserDTO;
import com.example.homecare_adm_app.api.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUserByEmail(authentication.getName()));
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserDTO userDTO
    ) {
        return ResponseEntity.ok(userService.updateUser(authentication.getName(), userDTO));
    }

    @PutMapping("/me/config")
    public ResponseEntity<UserConfigDTO> updateUserConfig(
            Authentication authentication,
            @Valid @RequestBody UserConfigDTO configDTO
    ) {
        return ResponseEntity.ok(userService.updateUserConfig(authentication.getName(), configDTO));
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponseDTO> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDTO request
    ) {
        userService.changePassword(authentication.getName(), request.getSenhaAtual(), request.getNovaSenha());
        return ResponseEntity.ok(new MessageResponseDTO("Senha alterada com sucesso"));
    }

    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDTO> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UserDTO userDTO
    ) {
        return ResponseEntity.ok(userService.updateUser(authentication.getName(), userDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserDTO userDTO
    ) {
        return ResponseEntity.ok(userService.updateUserById(id, userDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponseDTO> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(new MessageResponseDTO("Usuário excluído com sucesso"));
    }
}
