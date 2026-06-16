package com.cafetron.admin.controller;

import com.cafetron.admin.dto.OpsStatusDTO;
import com.cafetron.admin.service.WindowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Operational controls — window toggle and cutoff management.
 * @PreAuthorize("hasRole('ADMIN')") will be added in Module 1 integration.
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
//@PreAuthorize("hasAuthority('admin')")
public class AdminController {

    private final WindowService windowService;

    // ─────────────────────────────────────────────────────────────────
    // GET /api/admin/config
    // Returns current window state and cutoff time.
    // ─────────────────────────────────────────────────────────────────
    @GetMapping("/config")
    public ResponseEntity<OpsStatusDTO> getConfig() {
        return ResponseEntity.ok(windowService.getStatus());
    }

    // ─────────────────────────────────────────────────────────────────
    // POST /api/admin/window/toggle
    // Flips windowOpen true→false or false→true.
    // No request body needed.
    // ─────────────────────────────────────────────────────────────────
    @PostMapping("/window/toggle")
    public ResponseEntity<OpsStatusDTO> toggleWindow() {
        return ResponseEntity.ok(windowService.toggleWindow());
    }

    // ─────────────────────────────────────────────────────────────────
    // PUT /api/admin/cutoff
    // Body: { "time": "11:30" }
    // Updates the daily cutoff time.
    // ─────────────────────────────────────────────────────────────────
    @PutMapping("/cutoff")
    public ResponseEntity<OpsStatusDTO> updateCutoff(
            @RequestBody Map<String, String> body) {
        String time = body.get("time");
        if (time == null || !time.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(windowService.updateCutoff(time));
    }
}