package com.example.chaea.security;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.example.chaea.entities.Usuario;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;


@Component
public class JwtUtil {

    private final String SECRET = "c40b18859af2d07e57a71bfcb7d3508325863293319c5d2f7816741fc734f696";  // Generación de la clave segura
    private final int HOURS = 1;

    // Extraer email del token
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer un claim específico del token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignInKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // Generar el token JWT
    public String generateToken(Usuario userDetails) {
        
        return createToken(userDetails);
    }

    private String createToken(Usuario userDetails) {
        return Jwts.builder()
                .claim("user", userDetails)
                .subject(userDetails.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (1000 * 60 * 60 * HOURS)))
                .signWith(getSignInKey())
                .compact();
    }
    
    private SecretKey getSignInKey() {
        byte[] bytes = Base64.getDecoder()
       .decode(SECRET.getBytes(StandardCharsets.UTF_8));
                return new SecretKeySpec(bytes, "HmacSHA256"); }

    // Validar el token JWT
    public boolean validateToken(String token, Usuario userDetails) {
        final String email = extractEmail(token);
        return (email.equals(userDetails.getEmail()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
}
