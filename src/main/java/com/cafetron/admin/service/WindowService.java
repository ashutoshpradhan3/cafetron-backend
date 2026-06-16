package com.cafetron.admin.service;

import com.cafetron.admin.dto.OpsStatusDTO;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Holds ordering window state in memory.
 * No DB row needed — state resets to defaults on every restart
 * and at midnight via @Scheduled.
 *
 * CONTRACT for Module 3:
 *   windowService.isOpen()         → false blocks new orders
 *   windowService.isBeforeCutoff() → false blocks new orders
 */
@Slf4j
@Service
public class WindowService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Value("${cafetron.order.daily-cutoff:15:00}")
    private String defaultCutoffTime;

    // ── In-memory state ──────────────────────────────────────────────
    private volatile boolean windowOpen = true;
    private volatile LocalTime cutoffTime;

    @PostConstruct
    public void init() {
        this.cutoffTime = LocalTime.parse(defaultCutoffTime, FMT);
        log.info("WindowService initialised — cutoff: {}, window: open", defaultCutoffTime);
    }

    /**
     * Resets to defaults every day at midnight.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailyConfig() {
        this.windowOpen = true;
        this.cutoffTime = LocalTime.parse(defaultCutoffTime, FMT);
        log.info("Daily reset at midnight — window opened, cutoff reset to {}", defaultCutoffTime);
    }

    // ── Contract methods (called by Module 3) ────────────────────────

    public boolean isOpen() {
        return windowOpen;
    }

    public boolean isBeforeCutoff() {
        return LocalTime.now().isBefore(cutoffTime);
    }

    public boolean isOrderingAllowed() {
        return isOpen() && isBeforeCutoff();
    }

    // ── Mutation methods (called by AdminController) ─────────────────

    public OpsStatusDTO toggleWindow() {
        this.windowOpen = !this.windowOpen;
        log.info("Ordering window toggled to: {}", this.windowOpen);
        return getStatus();
    }

    public OpsStatusDTO updateCutoff(String timeStr) {
        try {
            this.cutoffTime = LocalTime.parse(timeStr, FMT);
            log.info("Cutoff time updated to: {}", timeStr);
            return getStatus();
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException(
                    "Invalid cutoff time (expected HH:mm): " + timeStr, ex);
        }
    }

    public OpsStatusDTO getStatus() {
        return new OpsStatusDTO(
                windowOpen,
                cutoffTime.format(FMT),
                !isBeforeCutoff()
        );
    }
}