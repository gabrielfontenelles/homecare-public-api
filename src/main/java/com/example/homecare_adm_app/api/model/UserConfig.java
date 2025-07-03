package com.example.homecare_adm_app.api.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_configs")
public class UserConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude // <-- ESSENCIAL!
    private User user;


    @Column(name = "theme")
    private String theme = "dark"; 

    @Column(name = "notifications_enabled")
    private boolean notificationsEnabled = true;

    @Column(name = "email_notifications")
    private boolean emailNotifications = true;

    
}