package com.tacticore.lambda.controller;

import com.tacticore.lambda.model.UserEntity;
import com.tacticore.lambda.model.dto.UserDto;
import com.tacticore.lambda.service.UserService;
import com.tacticore.lambda.service.SteamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SteamService steamService;
    
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        String steamId = request.get("steamId");
        String steamUsername = request.get("steamUsername");
        
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Email y contraseña son requeridos");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Validar formato de email
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Formato de email inválido");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        // Validar longitud de contraseña
        if (password.length() < 6) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "La contraseña debe tener al menos 6 caracteres");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // Verificar si el usuario ya existe
            Optional<UserEntity> existingUser = userService.findByEmail(email);
            if (existingUser.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "El usuario ya existe");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Crear nuevo usuario
            UserEntity newUser = new UserEntity();
            newUser.setEmail(email);
            newUser.setPassword(passwordEncoder.encode(password));
            newUser.setRole("USER");
            newUser.setActive(true);
            
            // Si hay datos de Steam, agregarlos
            if (steamId != null && !steamId.isEmpty()) {
                newUser.setSteamId(steamId);
            }
            if (steamUsername != null && !steamUsername.isEmpty()) {
                newUser.setSteamUsername(steamUsername);
            }
            
            UserEntity savedUser = userService.save(newUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario registrado exitosamente");
            response.put("user", new UserDto(savedUser));
            
            return ResponseEntity.status(201).body(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al registrar usuario: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        String password = request.get("password");
        
        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Email y contraseña son requeridos");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        try {
            // Buscar usuario por email
            Optional<UserEntity> userOpt = userService.findByEmail(email);
            if (!userOpt.isPresent()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Credenciales inválidas");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            UserEntity user = userOpt.get();
            
            // Verificar contraseña
            if (!passwordEncoder.matches(password, user.getPassword())) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Credenciales inválidas");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Verificar si el usuario está activo
            if (!user.isActive()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Cuenta desactivada");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            // Generar token JWT (por ahora simulamos con un token simple)
            String token = generateSimpleToken(user.getId(), user.getEmail());
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login exitoso");
            response.put("token", token);
            response.put("user", new UserDto(user));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al iniciar sesión: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // GET /api/auth/steam/login - Inicia el flujo de Steam OpenID
    @GetMapping("/steam/login")
    public ResponseEntity<Map<String, Object>> steamLogin() {
        try {
            String steamLoginUrl = steamService.getSteamLoginUrl();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("steamLoginUrl", steamLoginUrl);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error generando URL de Steam: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // POST /api/auth/steam/callback - Procesa la respuesta de Steam OpenID
    @PostMapping("/steam/callback")
    public ResponseEntity<Map<String, Object>> steamCallback(@RequestBody Map<String, String> request) {
        System.out.println("Steam callback recibido: " + request);
        System.out.println("SteamService inyectado: " + (steamService != null ? "SÍ" : "NO"));
        
        String openIdResponse = request.get("openIdResponse");
        String steamId = request.get("steamId");

        if (openIdResponse == null || openIdResponse.isEmpty()) {
            System.out.println("Error: openIdResponse es null o vacío");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Respuesta de Steam OpenID requerida");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            System.out.println("Procesando Steam callback...");
            System.out.println("openIdResponse: " + openIdResponse);
            System.out.println("steamId recibido: " + steamId);
            
            // Simular respuesta exitosa por ahora
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Steam callback procesado correctamente");
            response.put("steamId", steamId);
            response.put("steamUsername", "SteamUser_" + (steamId != null ? steamId.substring(steamId.length() - 4) : "Unknown"));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error en Steam callback: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error procesando respuesta de Steam: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    // GET /api/auth/debug/users - Endpoint para debug de usuarios
    @GetMapping("/debug/users")
    public ResponseEntity<Map<String, Object>> getUsersDebug() {
        try {
            System.out.println("Debug endpoint llamado");
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Backend funcionando correctamente");
            response.put("timestamp", System.currentTimeMillis());
            response.put("databaseStatus", "H2 in-memory database configured");
            response.put("steamApiConfigured", "Yes");
            
            System.out.println("Debug endpoint completado exitosamente");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.out.println("Error en debug endpoint: " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error en debug endpoint: " + e.getMessage());
            errorResponse.put("error", e.getClass().getSimpleName());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // GET /api/auth/me
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(@RequestHeader("Authorization") String token) {
        try {
            // Validar token (implementación simple)
            if (token == null || !token.startsWith("Bearer ")) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "Token inválido");
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
            String actualToken = token.substring(7); // Remover "Bearer "
            // Aquí deberías validar el token JWT real
            // Por ahora simulamos la validación
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Usuario autenticado");
            // Aquí deberías devolver los datos del usuario desde el token
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Error al validar token: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
    
    private String generateSimpleToken(Long userId, String email) {
        // Implementación simple de token (en producción usar JWT)
        return "tacticore_" + userId + "_" + System.currentTimeMillis() + "_" + email.hashCode();
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }
}
