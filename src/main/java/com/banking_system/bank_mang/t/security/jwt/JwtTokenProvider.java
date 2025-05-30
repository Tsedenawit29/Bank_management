package com.banking_system.bank_mang.t.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Value;

import java.net.HttpRetryException;
import java.net.MalformedURLException;
import java.security.Key;
import java.util.Date;

import io.jsonwebtoken.security.Keys;

public class JwtTokenProvider {
@Value("$(app.jwt-secret}")
    private String jwtSecret;
    @Value("$(app.jwt-expiration-milliseconds}")
    private String jwtExpirationMs;
    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
    /**
     * Generate a JWT token for authenticated user
     */
    public String generateToken(Authentication authentication){
        String username= authentication.getName();
        Date currentDate= new Date();
        Date expireDate= new Date(currentDate.getTime() + jwtExpirationMs);
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
            System.err.println("Invalide JWT token: " + ex.getMessage());
        }catch (ExpiredJwtException ex) {
            // Log this: Expired JWT token
            System.err.println("Expired JWT token: " + ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            // Log this: Unsupported JWT token
            System.err.println("Unsupported JWT token: " + ex.getMessage());
        } catch (IllegalArgumentException ex) {
            // Log this: JWT claims string is empty
            System.err.println("JWT claims string is empty: " + ex.getMessage());
        }
        return false;
    }

}
