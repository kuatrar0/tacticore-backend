package com.tacticore.lambda.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", unique = true)
    private String name;
    
    @Column(name = "email", unique = true)
    private String email;
    
    @Column(name = "password")
    private String password;
    
    @Column(name = "steam_id", unique = true)
    private String steamId;
    
    @Column(name = "steam_username")
    private String steamUsername;
    
    @Column(name = "active", nullable = false)
    private boolean active = true;
    
    @Column(name = "role", nullable = false)
    private String role;
    
    @Column(name = "average_score")
    private Double averageScore;
    
    @Column(name = "total_kills")
    private Integer totalKills;
    
    @Column(name = "total_deaths")
    private Integer totalDeaths;
    
    @Column(name = "total_matches")
    private Integer totalMatches;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Constructors
    public UserEntity() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.averageScore = 0.0;
        this.totalKills = 0;
        this.totalDeaths = 0;
        this.totalMatches = 0;
    }
    
    public UserEntity(String name, String role) {
        this();
        this.name = name;
        this.role = role;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getSteamId() { return steamId; }
    public void setSteamId(String steamId) { this.steamId = steamId; }
    
    public String getSteamUsername() { return steamUsername; }
    public void setSteamUsername(String steamUsername) { this.steamUsername = steamUsername; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    
    public Double getAverageScore() { return averageScore; }
    public void setAverageScore(Double averageScore) { this.averageScore = averageScore; }
    
    public Integer getTotalKills() { return totalKills; }
    public void setTotalKills(Integer totalKills) { this.totalKills = totalKills; }
    
    public Integer getTotalDeaths() { return totalDeaths; }
    public void setTotalDeaths(Integer totalDeaths) { this.totalDeaths = totalDeaths; }
    
    public Integer getTotalMatches() { return totalMatches; }
    public void setTotalMatches(Integer totalMatches) { this.totalMatches = totalMatches; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Utility methods
    public double getKDR() {
        if (totalDeaths == null || totalDeaths == 0) {
            return totalKills != null ? totalKills : 0.0;
        }
        return totalKills != null ? (double) totalKills / totalDeaths : 0.0;
    }
    
    public void updateStats(int kills, int deaths, double score) {
        this.totalKills = (this.totalKills != null ? this.totalKills : 0) + kills;
        this.totalDeaths = (this.totalDeaths != null ? this.totalDeaths : 0) + deaths;
        this.totalMatches = (this.totalMatches != null ? this.totalMatches : 0) + 1;
        
        // Recalcular promedio de score
        if (this.averageScore == null) {
            this.averageScore = score;
        } else {
            this.averageScore = (this.averageScore * (this.totalMatches - 1) + score) / this.totalMatches;
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
