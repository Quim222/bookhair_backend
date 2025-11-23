package com.bookhair.backend.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bookhair.backend.model.User;

@Service
public class TokenService {

    private final Algorithm accessAlg;
    private final Algorithm refreshAlg;

    public TokenService(
            @Value("${api.security.access.secret}") String accessSecret,
            @Value("${api.security.refresh.secret}") String refreshSecret) {
        this.accessAlg = Algorithm.HMAC256(accessSecret);
        this.refreshAlg = Algorithm.HMAC256(refreshSecret);
    }

    // -------- ACCESS TOKEN --------
    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject(user.getEmail())
                .withClaim("uid", user.getUserId().toString())
                .withClaim("role", user.getUserRole().name())
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(15, ChronoUnit.MINUTES)))
                .sign(accessAlg);
    }

    public DecodedJWT verifyAccessToken(String token) {
        return JWT.require(accessAlg).withIssuer("auth-api").build().verify(token);
    }

    // -------- REFRESH TOKEN --------
    public String generateRefreshToken(User user) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer("auth-api")
                .withSubject(user.getEmail())
                .withClaim("typ", "refresh")
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(7, ChronoUnit.DAYS)))
                .sign(refreshAlg);
    }

    public DecodedJWT verifyRefreshToken(String token) {
        DecodedJWT jwt = JWT.require(refreshAlg).withIssuer("auth-api").build().verify(token);
        if (!"refresh".equals(jwt.getClaim("typ").asString())) {
            throw new RuntimeException("Invalid token type");
        }
        return jwt;
    }
}
