package com.example.userservice.util;

import static com.example.userservice.common.constant.jwt.TokenType.ACCESS_TOKEN;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class JwtUtil {
    private final Environment env;

    public String issueAccessToken(String userId) {
        String token = env.getProperty("jwt.secret");
        return Jwts.builder()
                .subject(userId)
                .claim("token-type", ACCESS_TOKEN.getName())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN.getExpirationTime()))
                .signWith(Keys.hmacShaKeyFor(env.getProperty("jwt.secret").getBytes(StandardCharsets.UTF_8)))
                .compact();
    }
}
