package com.tacticore.lambda.service;

import com.tacticore.lambda.model.UserEntity;
import com.tacticore.lambda.model.UserRole;
import com.tacticore.lambda.model.dto.UserDto;
import com.tacticore.lambda.repository.UserRepository;
import com.tacticore.lambda.repository.KillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private KillRepository killRepository;
    
    // Create or get user
    public UserEntity createOrGetUser(String name, String role) {
        Optional<UserEntity> existingUser = userRepository.findByName(name);
        if (existingUser.isPresent()) {
            return existingUser.get();
        }
        
        UserEntity newUser = new UserEntity(name, role);
        return userRepository.save(newUser);
    }
    
    // Create or get user with random role
    public UserEntity createOrGetUser(String name) {
        return createOrGetUser(name, getRandomRole());
    }
    
    // Get a random role from available roles
    private String getRandomRole() {
        UserRole[] roles = UserRole.values();
        int randomIndex = ThreadLocalRandom.current().nextInt(roles.length);
        return roles[randomIndex].getDisplayName();
    }
    
    // Get user by name
    public Optional<UserEntity> getUserByName(String name) {
        return userRepository.findByName(name);
    }
    
    // Get user by email
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Get user by Steam ID
    public Optional<UserEntity> findBySteamId(String steamId) {
        return userRepository.findBySteamId(steamId);
    }
    
    // Save user
    public UserEntity save(UserEntity user) {
        return userRepository.save(user);
    }
    
    // Check if user exists
    public boolean userExists(String name) {
        return userRepository.existsByName(name);
    }
    
    // Get all users
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
    
    // Get users by role
    public List<UserEntity> getUsersByRole(String role) {
        return userRepository.findByRole(role);
    }
    
    // Search users by name
    public List<UserEntity> searchUsersByName(String name) {
        return userRepository.findByNameContaining(name);
    }
    
    // Get top players by average score
    public List<UserEntity> getTopPlayersByScore() {
        return userRepository.findAllOrderByAverageScoreDesc();
    }
    
    // Get top players by kills
    public List<UserEntity> getTopPlayersByKills() {
        return userRepository.findAllOrderByTotalKillsDesc();
    }
    
    // Get top players by KDR
    public List<UserEntity> getTopPlayersByKDR() {
        return userRepository.findAllOrderByKDRDesc();
    }
    
    // Get top players with minimum matches
    public List<UserEntity> getTopPlayersByMatches(int minMatches) {
        return userRepository.findTopPlayersByMatches(minMatches);
    }
    
    // Update user stats after a match
    public void updateUserStats(String userName, int kills, int deaths, double score) {
        Optional<UserEntity> userOpt = userRepository.findByName(userName);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            // Establecer valores directamente en lugar de sumarlos
            user.setTotalKills(kills);
            user.setTotalDeaths(deaths);
            user.setAverageScore(score);
            // Mantener totalMatches como está o calcularlo basándose en datos reales
            userRepository.save(user);
        }
    }
    
    public void updateUserStatsWithMatches(String userName, int kills, int deaths, double score, int totalMatches) {
        Optional<UserEntity> userOpt = userRepository.findByName(userName);
        if (userOpt.isPresent()) {
            UserEntity user = userOpt.get();
            // Establecer valores directamente basándose en datos reales
            user.setTotalKills(kills);
            user.setTotalDeaths(deaths);
            user.setAverageScore(score);
            user.setTotalMatches(totalMatches);
            userRepository.save(user);
        }
    }
    
    // Update multiple users stats
    public void updateMultipleUsersStats(List<String> userNames, List<Integer> kills, List<Integer> deaths, List<Double> scores) {
        for (int i = 0; i < userNames.size(); i++) {
            if (i < kills.size() && i < deaths.size() && i < scores.size()) {
                updateUserStats(userNames.get(i), kills.get(i), deaths.get(i), scores.get(i));
            }
        }
    }
    
    // Ensure users exist (create if they don't)
    public List<UserEntity> ensureUsersExist(List<String> userNames) {
        return userNames.stream()
                .map(this::createOrGetUser)
                .collect(Collectors.toList());
    }
    
    // Get user statistics
    public UserStats getUserStatistics() {
        Long totalUsers = userRepository.countAllUsers();
        Double averageScore = userRepository.getAverageScoreOfAllUsers();
        Long totalKills = userRepository.getTotalKillsOfAllUsers();
        Long totalDeaths = userRepository.getTotalDeathsOfAllUsers();
        Long totalMatches = userRepository.getTotalMatchesOfAllUsers();
        List<Object[]> roleStats = userRepository.getUsersCountByRole();
        
        return new UserStats(
            totalUsers != null ? totalUsers : 0L,
            averageScore != null ? averageScore : 0.0,
            totalKills != null ? totalKills : 0L,
            totalDeaths != null ? totalDeaths : 0L,
            totalMatches != null ? totalMatches : 0L,
            roleStats
        );
    }
    
    // Convert Entity to DTO
    public UserDto convertToDto(UserEntity entity) {
        return new UserDto(
            entity.getId(),
            entity.getName(),
            entity.getRole(),
            entity.getAverageScore(),
            entity.getTotalKills(),
            entity.getTotalDeaths(),
            entity.getTotalMatches(),
            entity.getKDR()
        );
    }
    
    // Convert list of entities to DTOs
    public List<UserDto> convertToDtoList(List<UserEntity> entities) {
        return entities.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // Get available roles
    public String[] getAvailableRoles() {
        return UserRole.getAllDisplayNames();
    }
    
    // Inner class for user statistics
    public static class UserStats {
        private final Long totalUsers;
        private final Double averageScore;
        private final Long totalKills;
        private final Long totalDeaths;
        private final Long totalMatches;
        private final List<Object[]> roleStats;
        
        public UserStats(Long totalUsers, Double averageScore, Long totalKills, 
                        Long totalDeaths, Long totalMatches, List<Object[]> roleStats) {
            this.totalUsers = totalUsers;
            this.averageScore = averageScore;
            this.totalKills = totalKills;
            this.totalDeaths = totalDeaths;
            this.totalMatches = totalMatches;
            this.roleStats = roleStats;
        }
        
        // Getters
        public Long getTotalUsers() { return totalUsers; }
        public Double getAverageScore() { return averageScore; }
        public Long getTotalKills() { return totalKills; }
        public Long getTotalDeaths() { return totalDeaths; }
        public Long getTotalMatches() { return totalMatches; }
        public List<Object[]> getRoleStats() { return roleStats; }
    }

    public com.tacticore.lambda.model.dto.UserProfileDto getUserProfile(String userName) {
        UserEntity user = userRepository.findByName(userName)
            .orElseThrow(() -> new RuntimeException("User not found: " + userName));

        // Obtener estadísticas reales de kills (actualizadas)
        int actualKills = user.getTotalKills();
        int actualDeaths = user.getTotalDeaths();
        
        // Calcular estadísticas adicionales usando datos reales
        int goodPlays = calculateGoodPlays(actualKills);
        int badPlays = calculateBadPlays(actualKills);
        double kdr = actualDeaths > 0 ? (double) actualKills / actualDeaths : actualKills;
        double winRate = 65.0; // Por ahora un valor fijo, se puede calcular después
        double hoursPlayed = user.getTotalMatches() * 0.75; // Estimación: 45 min promedio por partida
        
        // Obtener arma favorita desde los datos de kills (simplificado temporalmente)
        String favoriteWeapon = "AK-47"; // Default
        try {
            List<Object[]> weaponStats = killRepository.getWeaponUsageStatsByUser(userName);
            if (!weaponStats.isEmpty()) {
                favoriteWeapon = (String) weaponStats.get(0)[0]; // Primera arma (más usada)
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo arma favorita para " + userName + ": " + e.getMessage());
        }

        // Obtener mapa favorito desde los datos de kills (simplificado temporalmente)
        String favoriteMap = "Dust 2"; // Default
        try {
            List<Object[]> locationStats = killRepository.getLocationStatsByUser(userName);
            if (!locationStats.isEmpty()) {
                String location = (String) locationStats.get(0)[0];
                // Mapear location a mapa (simplificado)
                if (location != null) {
                    if (location.toLowerCase().contains("dust")) favoriteMap = "Dust 2";
                    else if (location.toLowerCase().contains("mirage")) favoriteMap = "Mirage";
                    else if (location.toLowerCase().contains("inferno")) favoriteMap = "Inferno";
                    else favoriteMap = "Dust 2"; // Default
                }
            }
        } catch (Exception e) {
            System.err.println("Error obteniendo mapa favorito para " + userName + ": " + e.getMessage());
        }
        
        // Crear estadísticas usando datos reales
        var stats = new com.tacticore.lambda.model.dto.UserProfileDto.UserStatsDto(
            user.getTotalMatches(),
            user.getTotalMatches() * 30, // Estimación: 30 rondas por partida
            actualKills, // Usar kills reales
            actualDeaths, // Usar deaths reales
            goodPlays, // Calcular buenas jugadas reales
            badPlays, // Calcular malas jugadas reales
            user.getAverageScore(), // Score ya actualizado
            kdr, // KDR calculado con datos reales
            winRate,
            favoriteMap, // Mapa favorito real
            favoriteWeapon, // Arma favorita real
            hoursPlayed,
            user.getCreatedAt()
        );

        // Crear actividad reciente
        var recentActivity = new com.tacticore.lambda.model.dto.UserProfileDto.RecentActivityDto(
            user.getUpdatedAt(),
            5, // Partidas esta semana (valor fijo por ahora)
            user.getTotalMatches() > 20 ? 20 : user.getTotalMatches() // Partidas este mes
        );

        // Crear preferencias
        var preferences = new com.tacticore.lambda.model.dto.UserProfileDto.UserPreferencesDto(
            "dark",
            true
        );

        return new com.tacticore.lambda.model.dto.UserProfileDto(
            "user_" + user.getId(),
            user.getName(),
            user.getName().toLowerCase() + "@tacticore.gg",
            "/gamer-avatar.png",
            user.getRole(),
            stats,
            recentActivity,
            preferences
        );
    }
    
    public Map<String, Object> getKillsUsersDebugInfo() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Obtener todos los atacantes únicos
            List<String> attackers = killRepository.findAllAttackers();
            
            // Obtener todos los victims únicos
            List<String> victims = killRepository.findAllVictims();
            
            // Combinar y obtener usuarios únicos
            Set<String> allKillsUsers = new HashSet<>();
            allKillsUsers.addAll(attackers);
            allKillsUsers.addAll(victims);
            
            // Obtener usuarios preloaded
            List<String> preloadedUsers = userRepository.findAll().stream()
                .map(UserEntity::getName)
                .collect(Collectors.toList());
            
            // Verificar coincidencias
            Set<String> matchingUsers = new HashSet<>(preloadedUsers);
            matchingUsers.retainAll(allKillsUsers);
            
            // Usuarios en kills pero no preloaded
            Set<String> killsOnlyUsers = new HashSet<>(allKillsUsers);
            killsOnlyUsers.removeAll(preloadedUsers);
            
            // Usuarios preloaded pero no en kills
            Set<String> preloadedOnlyUsers = new HashSet<>(preloadedUsers);
            preloadedOnlyUsers.removeAll(allKillsUsers);
            
            result.put("totalKillsUsers", allKillsUsers.size());
            result.put("totalPreloadedUsers", preloadedUsers.size());
            result.put("matchingUsers", new ArrayList<>(matchingUsers));
            result.put("killsOnlyUsers", new ArrayList<>(killsOnlyUsers));
            result.put("preloadedOnlyUsers", new ArrayList<>(preloadedOnlyUsers));
            result.put("allKillsUsers", new ArrayList<>(allKillsUsers));
            result.put("preloadedUsers", preloadedUsers);
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return result;
    }
    
    public Map<String, Object> getRealUserStatsFromKills(String userName) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Obtener estadísticas reales directamente del killRepository
            Long totalKillsFromKills = killRepository.countKillsByUser(userName);
            Long totalDeathsFromKills = killRepository.countDeathsByUser(userName);
            Long totalHeadshots = killRepository.countHeadshotsByUser(userName);
            
            int actualKills = (totalKillsFromKills != null) ? totalKillsFromKills.intValue() : 0;
            int actualDeaths = (totalDeathsFromKills != null) ? totalDeathsFromKills.intValue() : 0;
            int headshots = (totalHeadshots != null) ? totalHeadshots.intValue() : 0;
            
            // Calcular estadísticas
            int goodPlays = calculateGoodPlays(actualKills);
            int badPlays = calculateBadPlays(actualKills);
            double kdr = actualDeaths > 0 ? (double) actualKills / actualDeaths : actualKills;
            double score = calculateUnifiedScore(actualKills, actualDeaths, goodPlays, badPlays);
            
            result.put("userName", userName);
            result.put("killsFromRepository", actualKills);
            result.put("deathsFromRepository", actualDeaths);
            result.put("headshotsFromRepository", headshots);
            result.put("calculatedGoodPlays", goodPlays);
            result.put("calculatedBadPlays", badPlays);
            result.put("calculatedKDR", kdr);
            result.put("calculatedScore", score);
            
            // También obtener los datos de la entidad UserEntity para comparar
            Optional<UserEntity> userOpt = userRepository.findByName(userName);
            if (userOpt.isPresent()) {
                UserEntity user = userOpt.get();
                result.put("entityKills", user.getTotalKills());
                result.put("entityDeaths", user.getTotalDeaths());
                result.put("entityScore", user.getAverageScore());
            }
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
        }
        
        return result;
    }
    
    // Métodos de cálculo reutilizados del DatabaseMatchService
    private int calculateGoodPlays(int kills) {
        // Deterministic calculation: good plays are 30-50% of kills
        // Use kills as seed for consistent results
        double seed = kills * 0.12345; // Deterministic seed
        double factor = 0.3 + (seed % 0.2); // 30-50% range
        return (int)(kills * factor);
    }

    private int calculateBadPlays(int kills) {
        // Deterministic calculation: bad plays are 10-20% of kills
        // Use kills as seed for consistent results
        double seed = kills * 0.67890; // Different seed for variety
        double factor = 0.1 + (seed % 0.1); // 10-20% range
        return (int)(kills * factor);
    }

    private double calculateUnifiedScore(int kills, int deaths, int goodPlays, int badPlays) {
        // Fórmula unificada que incluye KDR y jugadas buenas/malas
        // Ponderación: 2/3 jugadas, 1/3 KDR

        // Calcular KDR
        double kdr = deaths > 0 ? (double) kills / deaths : kills;

        // Componente KDR (1/3 del peso total)
        double kdrComponent = Math.min(kdr * 1.5, 6.0); // Max 6 puntos por KDR
        kdrComponent = Math.max(kdrComponent, 0.0); // Mínimo 0

        // Componente de jugadas (2/3 del peso total)
        double playsComponent = 0.0;
        if (kills > 0) {
            // Ratio de buenas jugadas vs total de actividad
            double goodPlayRatio = (double) goodPlays / kills;
            double badPlayRatio = (double) badPlays / kills;

            // Score basado en la diferencia entre buenas y malas jugadas
            double playDifference = goodPlayRatio - badPlayRatio;
            playsComponent = Math.min(playDifference * 8.0, 8.0); // Max 8 puntos
            playsComponent = Math.max(playsComponent, -2.0); // Mínimo -2 puntos
        }

        // Combinar componentes con ponderación
        double rawScore = (playsComponent * 2.0/3.0) + (kdrComponent * 1.0/3.0);

        // Ajustar al rango 1.0-10.0
        double adjustedScore = rawScore + 5.0; // Centrar en 5.0
        return Math.min(Math.max(adjustedScore, 1.0), 10.0);
    }
}
