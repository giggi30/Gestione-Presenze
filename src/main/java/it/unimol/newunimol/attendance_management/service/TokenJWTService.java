package it.unimol.newunimol.attendance_management.service;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import it.unimol.newunimol.attendance_management.DTO.TokenJWTDto;

@Service
public class TokenJWTService {
    
    @Value("${jwt.private-key}")
    private String privateKeyString;

    @Value("${jwt.public-key}")
    private String publicKeyString;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private PrivateKey getPrivateKey() {
        if (this.privateKey == null) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(this.privateKeyString);
                PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.privateKey = keyFactory.generatePrivate(spec);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Chiave privata non è in formato Base64 valido", e);
            } catch (Exception e) {
                throw new RuntimeException("Errore prv_key, controlla application.properties", e);
            }
        }
        return privateKey;
    }

    private PublicKey getPublicKey() {
        if (this.publicKey == null) {
            try {
                byte[] keyBytes = Base64.getDecoder().decode(this.publicKeyString);
                X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                this.publicKey = keyFactory.generatePublic(spec);
            } catch (Exception e) {
                throw new RuntimeException("Errore pub_key, controlla application.properties", e);
            }
        }
        return publicKey;
    }

    public <T> T extractClaim (String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims (String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getPublicKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public String extractUserId(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractUsername(String token) {
        return extractClaim(token, claims -> claims.get("username", String.class));
    }
    
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    public boolean hasRole(String token, String requiredRole) {
        String role = extractRole(token);
        return requiredRole.equalsIgnoreCase(role);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public boolean isTokenValid (String token) {
        return !isTokenExpired(token);
    }

    public TokenJWTDto generateToken (String userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        
        long now = System.currentTimeMillis();
        long expiration = now + (this.jwtExpiration * 1000);
        
        String token = Jwts.builder()
            .setClaims(claims)
            .setSubject(userId)
            .setIssuedAt(new Date(now))
            .setExpiration(new Date(expiration))
            .claim("username", username)
            .claim("role", role)
            .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
            .compact();

        return new TokenJWTDto(token, userId, username, role, now / 1000, expiration / 1000);
    }

    public TokenJWTDto parseToken(String token) {
        Claims claims = extractAllClaims(token);
        return new TokenJWTDto(
            token,
            claims.getSubject(),
            claims.get("username", String.class),
            claims.get("role", String.class),
            claims.getIssuedAt().getTime() / 1000,
            claims.getExpiration().getTime() / 1000
        );
    }
}
