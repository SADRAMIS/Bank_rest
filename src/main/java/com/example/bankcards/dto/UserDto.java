package com.example.bankcards.dto;

import com.example.bankcards.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserDto {
    private Long id;
    
    @NotBlank(message = "Имя пользователя обязательно")
    @Size(max = 50, message = "Имя пользователя не может быть длиннее 50 символов")
    private String username;
    
    @NotBlank(message = "Email обязателен")
    @Email(message = "Неверный формат email")
    @Size(max = 100, message = "Email не может быть длиннее 100 символов")
    private String email;
    
    private Role role;
    private boolean enabled;

    public UserDto() {}

    public UserDto(Long id, String username, String email, Role role, boolean enabled) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
