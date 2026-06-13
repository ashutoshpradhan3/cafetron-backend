package com.cafetron.config;

import com.cafetron.security.CustomAuthEntryPoint;
import com.cafetron.security.JwtFilter;
import com.cafetron.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final JwtFilter jwtFilter;
    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthEntryPoint customAuthEntryPoint;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource,
                          @Lazy JwtFilter jwtFilter,
                          @Lazy UserDetailsServiceImpl userDetailsService,
                          CustomAuthEntryPoint customAuthEntryPoint) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.jwtFilter = jwtFilter;
        this.userDetailsService = userDetailsService;
        this.customAuthEntryPoint = customAuthEntryPoint;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint)
                )
                .authorizeHttpRequests(auth -> auth

                        // Public routes — no JWT needed
                        .requestMatchers(
                                "/api/auth/register",
                                "/api/auth/login"
                        ).permitAll()

                        // Admin only
                        .requestMatchers("/api/admin/**")
                        .hasRole("ADMIN")

                        // Counter and Admin
                        .requestMatchers("/api/vendors/**")
                        .hasAnyRole("COUNTER", "ADMIN")

                        // Pickup — Counter and Admin
                        .requestMatchers("/api/pickup/**")
                        .hasAnyRole("COUNTER", "ADMIN")

                        // Everything else needs a valid JWT
                        .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter,
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        // Spring Security 6 — CORRECT
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);  // ← pass in constructor
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}