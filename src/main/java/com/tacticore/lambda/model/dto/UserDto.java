package com.tacticore.lambda.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tacticore.lambda.model.UserEntity;

public class UserDto {
    
    @JsonProperty("id")
    private Long id;
    
    @JsonProperty("name")
    private String name;
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("averageScore")
    private Double averageScore;
    
    @JsonProperty("totalKills")
    private Integer totalKills;
    
    @JsonProperty("totalDeaths")
    private Integer totalDeaths;
    
    @JsonProperty("totalMatches")
    private Integer totalMatches;
    
    @JsonProperty("kdr")
    private Double kdr;
    
    @JsonProperty("email")
    private String email;
    
    @JsonProperty("steamId")
    private String steamId;
    
    @JsonProperty("steamUsername")
    private String steamUsername;
    
    @JsonProperty("active")
    private Boolean active;
    
    // Constructors
    public UserDto() {}
    
    public UserDto(Long id, String name, String role, Double averageScore, 
                   Integer totalKills, Integer totalDeaths, Integer totalMatches, Double kdr) {
        this.id = id;
        this.name = name;
        this.role = role;
        this.averageScore = averageScore;
        this.totalKills = totalKills;
        this.totalDeaths = totalDeaths;
        this.totalMatches = totalMatches;
        this.kdr = kdr;
    }
    
    // Constructor from UserEntity
    public UserDto(UserEntity entity) {
        this.id = entity.getId();
        this.name = entity.getName();
        this.email = entity.getEmail();
        this.role = entity.getRole();
        this.averageScore = entity.getAverageScore();
        this.totalKills = entity.getTotalKills();
        this.totalDeaths = entity.getTotalDeaths();
        this.totalMatches = entity.getTotalMatches();
        this.kdr = entity.getKDR();
        this.steamId = entity.getSteamId();
        this.steamUsername = entity.getSteamUsername();
        this.active = entity.isActive();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
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
    
    public Double getKdr() { return kdr; }
    public void setKdr(Double kdr) { this.kdr = kdr; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getSteamId() { return steamId; }
    public void setSteamId(String steamId) { this.steamId = steamId; }
    
    public String getSteamUsername() { return steamUsername; }
    public void setSteamUsername(String steamUsername) { this.steamUsername = steamUsername; }
    
    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
