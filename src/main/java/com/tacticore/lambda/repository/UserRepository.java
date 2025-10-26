package com.tacticore.lambda.repository;

import com.tacticore.lambda.model.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    
    Optional<UserEntity> findByName(String name);
    
    Optional<UserEntity> findByEmail(String email);
    
    Optional<UserEntity> findBySteamId(String steamId);
    
    boolean existsByName(String name);
    
    boolean existsByEmail(String email);
    
    boolean existsBySteamId(String steamId);
    
    List<UserEntity> findByRole(String role);
    
    @Query("SELECT u FROM UserEntity u ORDER BY u.averageScore DESC")
    List<UserEntity> findAllOrderByAverageScoreDesc();
    
    @Query("SELECT u FROM UserEntity u ORDER BY u.totalKills DESC")
    List<UserEntity> findAllOrderByTotalKillsDesc();
    
    @Query("SELECT u FROM UserEntity u WHERE u.totalKills > 0 ORDER BY (u.totalKills * 1.0 / NULLIF(u.totalDeaths, 0)) DESC")
    List<UserEntity> findAllOrderByKDRDesc();
    
    @Query("SELECT u FROM UserEntity u WHERE u.name LIKE %:name%")
    List<UserEntity> findByNameContaining(@Param("name") String name);
    
    @Query("SELECT COUNT(u) FROM UserEntity u")
    Long countAllUsers();
    
    @Query("SELECT AVG(u.averageScore) FROM UserEntity u WHERE u.averageScore > 0")
    Double getAverageScoreOfAllUsers();
    
    @Query("SELECT SUM(u.totalKills) FROM UserEntity u")
    Long getTotalKillsOfAllUsers();
    
    @Query("SELECT SUM(u.totalDeaths) FROM UserEntity u")
    Long getTotalDeathsOfAllUsers();
    
    @Query("SELECT SUM(u.totalMatches) FROM UserEntity u")
    Long getTotalMatchesOfAllUsers();
    
    @Query("SELECT u.role, COUNT(u) FROM UserEntity u GROUP BY u.role ORDER BY COUNT(u) DESC")
    List<Object[]> getUsersCountByRole();
    
    @Query("SELECT u FROM UserEntity u WHERE u.totalMatches >= :minMatches ORDER BY u.averageScore DESC")
    List<UserEntity> findTopPlayersByMatches(@Param("minMatches") Integer minMatches);
    
    @Query("SELECT u FROM UserEntity u WHERE u.name IN :names")
    List<UserEntity> findByNames(@Param("names") List<String> names);
}
