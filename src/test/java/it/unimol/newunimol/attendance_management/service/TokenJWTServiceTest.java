package it.unimol.newunimol.attendance_management.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import it.unimol.newunimol.attendance_management.DTO.TokenJWTDto;

@ExtendWith(MockitoExtension.class)
class TokenJWTServiceTest {

    @InjectMocks
    private TokenJWTService tokenJWTService;

    private String privateKeyString;
    private String publicKeyString;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        // Genera una coppia di chiavi RSA valida per i test
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();

        this.privateKeyString = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
        this.publicKeyString = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());

        // Inietta le proprietà nel service
        ReflectionTestUtils.setField(tokenJWTService, "privateKeyString", privateKeyString);
        ReflectionTestUtils.setField(tokenJWTService, "publicKeyString", publicKeyString);
        ReflectionTestUtils.setField(tokenJWTService, "jwtExpiration", 3600L);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String role = "DOCENTE";

        // Act
        TokenJWTDto tokenDto = tokenJWTService.generateToken(userId, username, role);

        // Assert
        assertNotNull(tokenDto);
        assertNotNull(tokenDto.getToken());
        assertFalse(tokenDto.getToken().isEmpty());
    }

    @Test
    void extractClaims_ShouldReturnCorrectValues() {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String role = "DOCENTE";
        TokenJWTDto tokenDto = tokenJWTService.generateToken(userId, username, role);
        String token = tokenDto.getToken();

        // Act
        String extractedUserId = tokenJWTService.extractUserId(token);
        String extractedUsername = tokenJWTService.extractUsername(token);
        String extractedRole = tokenJWTService.extractRole(token);

        // Assert
        assertEquals(userId, extractedUserId);
        assertEquals(username, extractedUsername);
        assertEquals(role, extractedRole);
    }

    @Test
    void hasRole_ShouldReturnTrueForCorrectRole() {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String role = "DOCENTE";
        TokenJWTDto tokenDto = tokenJWTService.generateToken(userId, username, role);
        String token = tokenDto.getToken();

        // Act & Assert
        assertTrue(tokenJWTService.hasRole(token, "DOCENTE"));
        assertFalse(tokenJWTService.hasRole(token, "STUDENTE"));
    }

    @Test
    void validateToken_ShouldReturnTrueForValidToken() {
        // Arrange
        String userId = "user123";
        String username = "testuser";
        String role = "DOCENTE";
        TokenJWTDto tokenDto = tokenJWTService.generateToken(userId, username, role);
        String token = tokenDto.getToken();

        // Act & Assert
        assertTrue(tokenJWTService.isTokenValid(token));
    }

    @Test
    void generateToken_WithInvalidKey_ShouldThrowException() {
        // Arrange
        ReflectionTestUtils.setField(tokenJWTService, "privateKeyString", "invalid-key");
        // Reset privateKey field to force re-initialization
        ReflectionTestUtils.setField(tokenJWTService, "privateKey", null);
        
        String userId = "user123";
        String username = "testuser";
        String role = "DOCENTE";

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            tokenJWTService.generateToken(userId, username, role);
        });
    }
}
