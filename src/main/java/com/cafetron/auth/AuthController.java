package com.cafetron.auth;

import com.cafetron.auth.dto.AuthResponse;
import com.cafetron.auth.dto.LoginRequest;
import com.cafetron.auth.dto.RegisterRequest;
import com.cafetron.security.UserPrincipal;
import com.cafetron.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    @Value("${cafetron.jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<AuthResponse> register(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody RegisterRequest request) {

        AuthResponse responseDto = authService.register(request);
        String token = responseDto.getToken(); // Extract token out

        ResponseCookie cookie = createJwtCookie(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(null, responseDto.getEmail(), responseDto.getName(), responseDto.getRole()));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<AuthResponse> login(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestBody LoginRequest request) {

        AuthResponse responseDto = authService.login(request);
        String token = responseDto.getToken(); // Extract token out

        ResponseCookie cookie = createJwtCookie(token);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(new AuthResponse(null, responseDto.getEmail(), responseDto.getName(), responseDto.getRole()));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, String>> logout() {
        // Clear the cookie by creating a replica with maxAge = 0
        ResponseCookie cookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(true) // Set to false if localhost HTTP ONLY without HTTPS
                .sameSite("None")
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    @GetMapping("/users/me")
    public ResponseEntity<Map<String, Object>> me(
            @AuthenticationPrincipal UserPrincipal principal) {

        User user = principal.getUser();

        return ResponseEntity.ok(Map.of(
                "id",         user.getId(),
                "name",       user.getName(),
                "email",      user.getEmail(),
                "role",       principal.getRole(),
                "employeeId", user.getEmployeeId() != null ? user.getEmployeeId() : "",
                "department", user.getDepartment() != null ? user.getDepartment() : ""
        ));
    }

    // Helper to generate a secure, cross-domain friendly cookie config
//    private ResponseCookie createJwtCookie(String token) {
//        return ResponseCookie.from("jwt", token)
//                .httpOnly(true)
//                .secure(true) // Mandatory for SameSite=None (production environments like Vercel/Render)
//                .sameSite("None") // Essential for cross-origin tracking between Render backend & Vercel frontend
//                .path("/")
//                .maxAge(jwtExpirationMs / 1000) // seconds
//                .build();
//    }

    private ResponseCookie createJwtCookie(String token) {
        return ResponseCookie.from("jwt", token)
                .httpOnly(true)
                .secure(false) // Set to false for local HTTP development (flip to true in prod)
                .sameSite("Lax") // Use "Lax" for localhost same-machine testing (flip to "None" in prod)
                .path("/")
                .maxAge(jwtExpirationMs / 1000)
                .build();
    }
}