package it.unimol.newunimol.attendance_management.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import it.unimol.newunimol.attendance_management.DTO.TokenJWTDto;
import it.unimol.newunimol.attendance_management.service.TokenJWTService;

@RestController
@RequestMapping("api/token")
public class TokenJWTTestController {
    private final TokenJWTService tokenJWTService;

    public TokenJWTTestController(TokenJWTService tokenJWTService) {
        this.tokenJWTService = tokenJWTService;
    }

    /**
     * Genera un token JWT contenente i parametri passati nel body JSON della richiesta:
     * - userId: identificativo (matricola) dell'utente
     * - username: nome utente
     * - role: ruolo dell'utente nel sistema
     * @param request Parametri per l'avvaloramento del token (userId e role)
     * @return Token JWT generato con informazioni associate incapsulate in TokenJWTDto
     */
    @PostMapping("/generate")
    public ResponseEntity<TokenJWTDto> generateToken(@RequestBody TokenRequest request) {
        TokenJWTDto token = tokenJWTService.generateToken(request.getUserId(), request.getUsername(), request.getRole());
        return ResponseEntity.ok(token);
    }

    /**
     * Valida un token JWT fornito nel body della richiesta e restituisce informazioni dettagliate.
     * Se il token è valido, estrae anche userId, role, issuedAt ed expiresIn.
     * @param request Oggetto contenente il token JWT da validare
     * @return Mappa con stato di validità, messaggio descrittivo e dettagli del token se valido.
     *         In caso di errore restituisce anche il messaggio di eccezione.
     */
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestBody TokenValidationRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = tokenJWTService.isTokenValid(request.getToken());
            response.put("valid", isValid);
            response.put("message", isValid ? "Token valido" : "Token scaduto");
            
            if (isValid) {
                TokenJWTDto tokenInfo = tokenJWTService.parseToken(request.getToken());
                response.put("userId", tokenInfo.getUserId());
                response.put("role", tokenInfo.getRole());
                response.put("issuedAt", tokenInfo.getIssuedAt());
                response.put("expiresIn", tokenInfo.getExpiresIn());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Token non valido o malformato");
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Valida un token JWT estratto dall'header Authorization con formato "Bearer <token>".
     * Funzionalità identica al metodo POST validate ma utilizza l'header HTTP invece del body.
     * @param authHeader Header Authorization contenente il token nel formato "Bearer <token>"
     * @return Mappa con stato di validità, messaggio descrittivo e dettagli del token se valido.
     *         Restituisce errore se l'header è mancante o ha formato non valido.
     */
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateTokenFromHeader(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        
        Map<String, Object> response = new HashMap<>();
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.put("valid", false);
            response.put("message", "Header Authorization mancante o formato non valido");
            return ResponseEntity.badRequest().body(response);
        }
        
        String token = authHeader.substring(7);
        
        try {
            boolean isValid = tokenJWTService.isTokenValid(token);
            response.put("valid", isValid);
            response.put("message", isValid ? "Token valido" : "Token scaduto");
            
            if (isValid) {
                TokenJWTDto tokenInfo = tokenJWTService.parseToken(token);
                response.put("userId", tokenInfo.getUserId());
                response.put("username", tokenInfo.getUsername());
                response.put("role", tokenInfo.getRole());
                response.put("issuedAt", tokenInfo.getIssuedAt());
                response.put("expiresIn", tokenInfo.getExpiresIn());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("valid", false);
            response.put("message", "Token non valido o malformato");
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Analizza un token JWT e restituisce le informazioni contenute senza validarne la scadenza.
     * Utile per estrarre dati da un token anche se potenzialmente scaduto.
     * @param request Oggetto contenente il token JWT da analizzare
     * @return TokenJWTDto con le informazioni estratte dal token, oppure BAD_REQUEST se il parsing fallisce
     */
    @PostMapping("/parse")
    public ResponseEntity<TokenJWTDto> parseToken(@RequestBody TokenValidationRequest request) {
        try {
            TokenJWTDto tokenInfo = tokenJWTService.parseToken(request.getToken());
            return ResponseEntity.ok(tokenInfo);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }


    /**
     * Estrae l'ID utente da un token JWT fornito come parametro query.
     * @param token Token JWT come parametro query string
     * @return Mappa contenente il campo "userId" con l'ID estratto, oppure "error" se l'estrazione fallisce
     */
    @GetMapping("/user")
    public ResponseEntity<Map<String, String>> extractUserId(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String userId = tokenJWTService.extractUserId(token);
            response.put("userId", userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Impossibile estrarre userId dal token");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Estrae l'username utente da un token JWT fornito come parametro query.
     * @param token Token JWT come parametro query string
     * @return Mappa contenente il campo "username" con il nome utente estratto, oppure "error" se l'estrazione fallisce
     */
    @GetMapping("/username")
    public ResponseEntity<Map<String, String>> extractUsername(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String username = tokenJWTService.extractUsername(token);
            response.put("username", username);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Impossibile estrarre username dal token");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Estrae il ruolo da un token JWT fornito come parametro query.
     * @param token Token JWT come parametro query string
     * @return Mappa contenente il campo "role" con il ruolo estratto, oppure "error" se l'estrazione fallisce
     */
    @GetMapping("/role")
    public ResponseEntity<Map<String, String>> extractRole(@RequestParam String token) {
        Map<String, String> response = new HashMap<>();
        
        try {
            String role = tokenJWTService.extractRole(token);
            response.put("role", role);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Impossibile estrarre ruolo dal token");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Controlla lo stato di scadenza di un token JWT e calcola il tempo rimanente.
     * Fornisce informazioni dettagliate sui tempi di scadenza in secondi e minuti.
     * @param request Oggetto contenente il token JWT da controllare
     * @return Mappa contenente:
     *         - expired: boolean che indica se il token è scaduto
     *         - expiresAt: timestamp di scadenza del token
     *         - timeRemainingSeconds: secondi rimanenti alla scadenza (0 se già scaduto)
     *         - timeRemainingMinutes: minuti rimanenti alla scadenza (0 se già scaduto)
     *         Oppure campo "error" se il controllo fallisce
     */
    @PostMapping("/expiration")
    public ResponseEntity<Map<String, Object>> checkExpiration(@RequestBody TokenValidationRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean isValid = tokenJWTService.isTokenValid(request.getToken());
            TokenJWTDto tokenInfo = tokenJWTService.parseToken(request.getToken());
            
            long currentTime = System.currentTimeMillis() / 1000;
            long expirationTime = tokenInfo.getExpiresIn();
            long timeRemaining = expirationTime - currentTime;
            
            response.put("expired", !isValid);
            response.put("expiresAt", expirationTime);
            response.put("timeRemainingSeconds", Math.max(0, timeRemaining));
            response.put("timeRemainingMinutes", Math.max(0, timeRemaining / 60));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Errore nel controllo scadenza token");
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Classe di supporto per incapsulare i parametri di richiesta per la generazione del token.
     * Contiene userId e role necessari per creare un nuovo token JWT.
     */
    public static class TokenRequest {
        private String userId;
        private String username;
        private String role;

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }

    /**
     * Classe di supporto per incapsulare il token JWT nelle richieste di validazione e parsing.
     * Utilizzata come wrapper per i metodi che richiedono solo il token come input.
     */
    public static class TokenValidationRequest {
        private String token;

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }
    }
}