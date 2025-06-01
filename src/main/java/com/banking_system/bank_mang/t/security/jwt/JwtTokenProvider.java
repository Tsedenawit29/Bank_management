package com.banking_system.bank_mang.t.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;

// Remove unnecessary imports if not used
// import java.net.HttpRetryException;
// import java.net.MalformedURLException;

import java.security.Key;
import java.util.Date; // Keep this

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationMs; // <--- ***CHANGE THIS TO 'long'***

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    /**
     * Generate a JWT token for authenticated user
     */
    public String generateToken(Authentication authentication){
        String username= authentication.getName();
        Date currentDate= new Date();
        // This line was 'Date expireDate= new Date(currentDate.getTime() + jwtExpirationMs);'
        // If jwtExpirationMs was String, this would try string concatenation, not addition.
        // With jwtExpirationMs as long, this is now correct numerical addition.
        Date expireDate= new Date(currentDate.getTime() + jwtExpirationMs); // This was line 32 in your previous stack trace

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extracting the username from a Jwt token
     */
    public String getUsernameFromJwt(String token){
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject();
    }

    /**
     * Validating a Jwt token
     */
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return  true;
        }catch (MalformedJwtException ex){
            System.err.println("Invalid JWT token: " + ex.getMessage());
        }catch (ExpiredJwtException ex) {
            System.err.println("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            System.err.println("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            System.err.println("JWT claims string is empty: " + ex.getMessage());
        }
        return false;
    }
}