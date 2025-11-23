package com.bookhair.backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.Customizer;

import lombok.RequiredArgsConstructor;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SecurityFilter securityFilter; // o teu filtro (e.g., JWT) deve estender OncePerRequestFilter

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 1) CORS primeiro
                .cors(Customizer.withDefaults())
                // 2) CSRF off em APIs stateless
                .csrf(csrf -> csrf.disable())
                // 3) Stateless
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 4) Autorização
                .authorizeHttpRequests(auth -> auth
                        // Preflight CORS
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // Auth
                        .requestMatchers(HttpMethod.POST, "/auth/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/auth/refresh-token").permitAll()

                        // Ex.: endpoint público para a tua página da equipa
                        .requestMatchers(HttpMethod.GET, "/users/employees").permitAll()

                        // ANALYTICS
                        .requestMatchers(HttpMethod.GET, "/analytics/total-users").hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/analytics/total-bookings").hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/analytics/average-bookings-per-user")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/analytics/most-used-overall")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/analytics/most-used-overall/{clientId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/analytics/most-frequent-time/{days}")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/analytics/most-frequent-time/{days}/{clientId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/analytics/most-employee-worked")
                        .hasAnyRole("ADMIN", "FUNCIONARIO")

                        // USERS
                        .requestMatchers(HttpMethod.GET, "/users/me").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/clients").hasAnyRole("ADMIN", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/users/**").hasRole("ADMIN")

                        // SERVICES
                        .requestMatchers(HttpMethod.POST, "/service").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/service/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/service/**").hasRole("ADMIN")

                        // BOOKINGS
                        .requestMatchers(HttpMethod.PUT, "/bookings/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bookings/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/bookings/**").authenticated()

                        // resto
                        .anyRequest().permitAll())
                // 5) Handlers JSON simples
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((req, res, ex) -> {
                            res.setStatus(401);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"unauthorized\"}");
                        })
                        .accessDeniedHandler((req, res, ex) -> {
                            res.setStatus(403);
                            res.setContentType("application/json");
                            res.getWriter().write("{\"error\":\"forbidden\"}");
                        }))
                // 6) Filtro antes do UsernamePasswordAuthenticationFilter
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // CORS global (substitui o @CrossOrigin disperso pelos controllers)
    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                System.getenv("ALLOWED_ORIGIN")));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization", "Content-Type", "location")); // se precisares ler o header no
                                                                                     // browser
        cfg.setAllowCredentials(true); // se usares cookies
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
