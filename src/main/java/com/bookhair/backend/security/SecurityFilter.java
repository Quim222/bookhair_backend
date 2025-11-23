package com.bookhair.backend.security;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.User;
import com.bookhair.backend.repositories.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain) throws ServletException, IOException {

        // 1) Bypass para preflight e rotas públicas
        String path = request.getRequestURI();
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())
                || path.startsWith("/auth")) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Já autenticado? segue
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        // 3) Recupera Bearer token
        String token = recoverToken(request);
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        try {
            // 4) Valida Access Token (NÃO é refresh aqui)
            DecodedJWT jwt = tokenService.verifyAccessToken(token);
            String email = jwt.getSubject();

            User user = userRepository.findByEmailIgnoreCase(email);
            if (user == null) {
                writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "USER_NOT_FOUND", "User not found");
                return;
            }
            if (user.getStatusUser() != StatusUser.ATIVO) {
                writeJson(response, HttpServletResponse.SC_FORBIDDEN, "USER_NOT_ACTIVE", "User not active");
                return;
            }

            // 5) Autentica no contexto
            var auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(auth);

            chain.doFilter(request, response);

        } catch (TokenExpiredException e) {
            // Access token expirado → frontend deve chamar /auth/refresh
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_EXPIRED", "Access token expired");
        } catch (JWTVerificationException e) {
            // Token inválido / assinatura errada / claims inválidas
            writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "TOKEN_INVALID", "Invalid access token");
        }
    }

    private String recoverToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer "))
            return null;
        return authorization.substring(7);
    }

    private void writeJson(HttpServletResponse res, int status, String code, String message) throws IOException {
        res.setStatus(status);
        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String body = "{\"error\":\"" + code + "\",\"message\":\"" + message + "\"}";
        res.getWriter().write(body);
    }
}
