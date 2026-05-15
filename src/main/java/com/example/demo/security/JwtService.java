package com.example.demo.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtService {
    private static final String SECRET= "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 24 * 60 * 60 * 1000L;

    public String generateToken(UserDetails userDetails){
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
                .signWith(getSigninKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserName(String token){
        return extractClaim(token, Claims::getSubject);

    }
    // now check the validity of token
    private boolean isTokenExpired(String token){
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    public boolean isTokenValid(String token, UserDetails userDetails){
        String username = extractUserName(token);
        boolean userEquals = username.equals(userDetails.getUsername());
        boolean isTokenExpired = isTokenExpired(token);
        return userEquals && !isTokenExpired;
    }


    public <T> T extractClaim (String token, Function<Claims, T> resolver){
        JwtParser parser = Jwts.parserBuilder()
                .setSigningKey(getSigninKey())
                .build();
        Claims body = parser.parseClaimsJwt(token).getBody();
        return resolver.apply(body);
    }


    private Key getSigninKey() {
        byte[] bytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(bytes);
    }

}
