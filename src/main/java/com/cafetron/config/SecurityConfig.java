package com.cafetron.config;

import com.cafetron.security.CustomAuthEntryPoint;
import com.cafetron.security.JwtFilter;
import com.cafetron.security.JwtUtil;
import com.cafetron.security.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
@EnableMethodSecurity
public class SecurityConfig {

    private final CorsConfigurationSource corsConfigurationSource;
    private final UserDetailsServiceImpl userDetailsService;
    private final CustomAuthEntryPoint customAuthEntryPoint;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CorsConfigurationSource corsConfigurationSource,
                          UserDetailsServiceImpl userDetailsService,
                          CustomAuthEntryPoint customAuthEntryPoint,
                          JwtUtil jwtUtil) {
        this.corsConfigurationSource = corsConfigurationSource;
        this.userDetailsService = userDetailsService;
        this.customAuthEntryPoint = customAuthEntryPoint;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public JwtFilter jwtFilter() {
        return new JwtFilter(jwtUtil, userDetailsService);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthEntryPoint))
                .authorizeHttpRequests(auth -> auth

                                //Temporarily commented for ease of development.

//                        // Public routes
//                        .requestMatchers(
//                                "/api/auth/register",
//                                "/api/auth/login"
//                        ).permitAll()
//
//                        // Admin only
//                        .requestMatchers("/api/admin/**")
//                        .hasRole("ADMIN")
//
//                        // Counter and Admin
//                        .requestMatchers("/api/vendors/**")
//                        .hasAnyRole("COUNTER", "ADMIN")
//
//                        // Pickup — Counter and Admin
//                        .requestMatchers("/api/pickup/**")
//                        .hasAnyRole("COUNTER", "ADMIN")
//
//                        // Everything else needs a valid JWT
//                        .anyRequest().authenticated()

                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .requestMatchers(
                                        "/api/auth/**",
                                        "/auth/**"
                                ).permitAll()
                                .requestMatchers("/api/admin/**", "/api/dev/**").hasRole("ADMIN")
                                .requestMatchers("/api/vendor/orders/**").hasAnyRole("VENDOR", "ADMIN")
                                .requestMatchers("/api/vendors/**", "/vendors/**").authenticated()
                                .requestMatchers("/api/menu/**", "/menu/**").authenticated()
                                .requestMatchers("/api/orders/**").authenticated()
                                .requestMatchers("/api/order-qr/**").authenticated()
                                .requestMatchers("/api/wallet/**").authenticated()
                                .anyRequest().authenticated()
                )
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtFilter(),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
