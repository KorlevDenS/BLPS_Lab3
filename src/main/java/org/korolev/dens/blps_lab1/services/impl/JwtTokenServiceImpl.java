package org.korolev.dens.blps_lab1.services.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.korolev.dens.blps_lab1.exceptions.InvalidTokenException;
import org.korolev.dens.blps_lab1.services.JwtTokenService;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Properties;

@Service
public class JwtTokenServiceImpl implements JwtTokenService {

    public String generateToken(Integer subjectId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 86400000); // устанавливаем срок действия токена (1 день)


        return Jwts.builder()
                .claim("role", role)
                .setSubject(String.valueOf(subjectId))
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }


    public Integer verifyToken(String token) throws InvalidTokenException {

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey()).build()
                    .parseClaimsJws(token).getBody();
            Date now = new Date();
            if (claims.getExpiration().after(now)) {
                return Integer.valueOf(claims.getSubject());
            }
            throw new InvalidTokenException("Token is rotten");
        } catch (SignatureException | MalformedJwtException e) {
            throw new InvalidTokenException(e.getMessage());
        }

    }

    private Key getSigningKey() {
        byte[] keyBytes = getSecretProperty().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String getSecretProperty() {
        Properties properties = new Properties();
        String jwtSecret;
        try {
            FileInputStream inputStream = new FileInputStream("src/main/resources/client.properties");
            properties.load(inputStream);
            jwtSecret = properties.getProperty("jwt.secret");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return jwtSecret;
    }

}
