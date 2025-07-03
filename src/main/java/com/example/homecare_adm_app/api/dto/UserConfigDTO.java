package com.example.homecare_adm_app.api.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConfigDTO {

    private Long id;
    private Long userId;
    private String theme;
    private boolean notificationsEnabled; 
    private boolean emailNotifications; 

    
    private UserDTO user;
}