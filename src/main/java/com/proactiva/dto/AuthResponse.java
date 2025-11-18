package com.proactiva.dto;

import com.proactiva.model.User;

/**
 * DTO para resposta de autenticação.
 */
public class AuthResponse {

    private Long userId;
    private String username;
    private String email;
    private String message;
    private String token; // NOVO CAMPO

    // Construtores
    public AuthResponse() {
    }

    // CONSTRUTOR ATUALIZADO
    public AuthResponse(User user, String message, String token) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.message = message;
        this.token = token;
    }

    // Getters e Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    // NOVO GETTER/SETTER
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
