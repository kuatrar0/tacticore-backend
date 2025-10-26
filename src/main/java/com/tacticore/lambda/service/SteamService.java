package com.tacticore.lambda.service;

import com.tacticore.lambda.model.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SteamService {

    @Autowired
    private UserService userService;

    @Value("${steam.api.key:YOUR_STEAM_API_KEY_HERE}")
    private String steamApiKey;

    private final WebClient webClient = WebClient.builder().build();
    private final String STEAM_OPENID_URL = "https://steamcommunity.com/openid/login";
    private final String STEAM_OPENID_RETURN_URL = "http://localhost:3000/auth/steam/callback";

    /**
     * Genera la URL de autenticación de Steam OpenID
     */
    public String getSteamLoginUrl() {
        try {
            String returnUrl = URLEncoder.encode(STEAM_OPENID_RETURN_URL, StandardCharsets.UTF_8.toString());
            return STEAM_OPENID_URL + "?openid.ns=http://specs.openid.net/auth/2.0" +
                   "&openid.mode=checkid_setup" +
                   "&openid.return_to=" + returnUrl +
                   "&openid.realm=" + URLEncoder.encode("http://localhost:3000", StandardCharsets.UTF_8.toString()) +
                   "&openid.identity=http://specs.openid.net/auth/2.0/identifier_select" +
                   "&openid.claimed_id=http://specs.openid.net/auth/2.0/identifier_select";
        } catch (Exception e) {
            throw new RuntimeException("Error generando URL de Steam", e);
        }
    }

    /**
     * Procesa la respuesta de Steam OpenID y extrae el Steam ID
     */
    public String extractSteamIdFromOpenIdResponse(String responseUrl) {
        try {
            // Steam devuelve una URL como: https://steamcommunity.com/openid/id/76561198000000000
            if (responseUrl.contains("/openid/id/")) {
                return responseUrl.substring(responseUrl.lastIndexOf("/") + 1);
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtiene información del usuario de Steam usando su Steam ID
     */
    public Mono<Map<String, Object>> getSteamUserInfo(String steamId) {
        // Si no hay API key configurada, devolver datos simulados
        if (steamApiKey == null || steamApiKey.equals("YOUR_STEAM_API_KEY_HERE") || steamApiKey.isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("steamId", steamId);
            result.put("steamUsername", "SteamUser_" + steamId.substring(steamId.length() - 4));
            return Mono.just(result);
        }

        String url = String.format(
            "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v0002/?key=%s&steamids=%s",
            steamApiKey, steamId
        );

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    Map<String, Object> result = new HashMap<>();
                    if (response.containsKey("response")) {
                        Map<String, Object> responseData = (Map<String, Object>) response.get("response");
                        if (responseData.containsKey("players")) {
                            java.util.List<Map<String, Object>> players = (java.util.List<Map<String, Object>>) responseData.get("players");
                            if (!players.isEmpty()) {
                                Map<String, Object> player = players.get(0);
                                result.put("steamId", player.get("steamid"));
                                result.put("steamUsername", player.get("personaname"));
                                result.put("profileUrl", player.get("profileurl"));
                                result.put("avatarUrl", player.get("avatarmedium"));
                                result.put("success", true);
                            } else {
                                result.put("success", false);
                                result.put("message", "Usuario de Steam no encontrado");
                            }
                        } else {
                            result.put("success", false);
                            result.put("message", "No se encontraron datos del usuario");
                        }
                    } else {
                        result.put("success", false);
                        result.put("message", "Error en la respuesta de Steam API");
                    }
                    return result;
                })
                .onErrorReturn(createErrorResponse("Error al conectar con Steam API"));
    }

    /**
     * Valida el Steam ID y obtiene información del usuario
     */
    public Mono<Map<String, Object>> validateSteamUser(String steamId, String steamUsername) {
        return getSteamUserInfo(steamId)
                .map(steamInfo -> {
                    if ((Boolean) steamInfo.get("success")) {
                        String apiUsername = (String) steamInfo.get("steamUsername");
                        if (apiUsername != null && apiUsername.equals(steamUsername)) {
                            steamInfo.put("validated", true);
                        } else {
                            steamInfo.put("validated", false);
                            steamInfo.put("message", "El nombre de usuario no coincide con el Steam ID");
                        }
                    }
                    return steamInfo;
                });
    }

    /**
     * Busca o crea un usuario basado en Steam ID
     */
    public UserEntity findOrCreateSteamUser(String steamId, String steamUsername) {
        Optional<UserEntity> existingUser = userService.findBySteamId(steamId);
        
        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            // Crear nuevo usuario de Steam
            UserEntity newUser = new UserEntity();
            newUser.setSteamId(steamId);
            newUser.setSteamUsername(steamUsername);
            newUser.setEmail(steamId + "@steam.local"); // Email temporal
            newUser.setRole("USER");
            newUser.setActive(true);
            return userService.save(newUser);
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }
}
