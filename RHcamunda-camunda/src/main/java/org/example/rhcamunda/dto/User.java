package org.example.rhcamunda.dto;

public class User {
    private Long id;
    private String username;
    private String email;
    private String password;  // ← AJOUTÉ: Ce champ manquait! ✅
    private String role;

    // Constructor vide (nécessaire pour Jackson)
    public User() {}

    // Constructor complet (avec password maintenant)
    public User(Long id, String username, String email, String password, String role) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;  // ← AJOUTÉ
        this.role = role;
    }

    // Getters et Setters
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

    // ⚠️ AJOUTÉ: Getter et Setter pour password
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}