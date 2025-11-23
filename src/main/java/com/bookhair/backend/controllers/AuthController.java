package com.bookhair.backend.controllers;

import java.time.Duration;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.User;
import com.bookhair.backend.security.TokenService;
import com.bookhair.backend.services.UserService;
import com.bookhair.dto.AuthenticationDto;
import com.bookhair.dto.UserDto.UserCreateDto;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final TokenService tokenService;

    // Podes mover estes TTLs para application.properties se preferires
    private static final Duration REFRESH_TTL = Duration.ofDays(7);

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid AuthenticationDto data,
            HttpServletResponse response) {
        var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(data.email(), data.password()));

        var user = (User) authentication.getPrincipal();

        String access = tokenService.generateAccessToken(user); // 15m no TokenService ou usa ACCESS_TTL
        String refresh = tokenService.generateRefreshToken(user); // 7d no TokenService

        // Define cookie HttpOnly com o refresh token
        boolean prod = false;
        ResponseCookie cookie = ResponseCookie.from("refreshToken", refresh)
                .httpOnly(true)
                .secure(prod) // EM PRODUÇÃO: true (HTTPS)
                .sameSite(prod ? "None" : "Lax") // se front em domínio diferente: "None" + secure(true)
                .path("/auth") // só enviado para /auth/*
                .maxAge(REFRESH_TTL)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        // devolve o access token no body (string simples ou JSON, como preferires)
        return ResponseEntity.ok(access);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(401).body("NO_REFRESH");
        }

        DecodedJWT jwt;
        try {
            jwt = tokenService.verifyRefreshToken(refreshToken);
        } catch (Exception e) {
            return ResponseEntity.status(401).body("INVALID_REFRESH");
        }

        String email = jwt.getSubject();
        User user = userService.findByEmailIgnoreCase(email); // garante que tens este método no serviço/repos
        if (user == null) {
            return ResponseEntity.status(401).body("USER_NOT_FOUND");
        }
        if (user.getStatusUser() != StatusUser.ATIVO) {
            return ResponseEntity.status(403).body("USER_NOT_ACTIVE");
        }
        // Emite novo access
        String newAccess = tokenService.generateAccessToken(user);

        // (Opcional mas recomendado) Rotação do refresh: emitir novo e substituir
        // cookie
        String newRefresh = tokenService.generateRefreshToken(user);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", newRefresh)
                .httpOnly(true)
                .secure(false) // PRODUÇÃO: true
                .sameSite("Lax")
                .path("/auth")
                .maxAge(REFRESH_TTL)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());

        return ResponseEntity.ok(newAccess);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // apaga o cookie de refresh
        ResponseCookie cookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(false) // PRODUÇÃO: true
                .sameSite("Lax")
                .path("/auth")
                .maxAge(0)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody @Valid UserCreateDto registerRequest) {
        userService.createUser(registerRequest);
        return ResponseEntity.ok().build();
    }
}
