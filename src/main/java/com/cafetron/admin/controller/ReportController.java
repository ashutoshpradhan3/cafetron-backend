package com.cafetron.admin.controller;

import com.cafetron.admin.dto.*;
import com.cafetron.admin.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
//@PreAuthorize("hasAuthority('admin')")
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/daily")
    public ResponseEntity<DailySummaryDTO> getDailySummary(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                reportService.getDailySummary(date != null ? date : LocalDate.now()));
    }

    @GetMapping("/top-items")
    public ResponseEntity<List<TopItemDTO>> getTopItems(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(reportService.getTopItems(limit));
    }

    @GetMapping("/range")
    public ResponseEntity<List<RevenuePointDTO>> getRevenueRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(reportService.getRevenueRange(from, to));
    }

    @GetMapping("/status-breakdown")
    public ResponseEntity<List<StatusCountDTO>> getStatusBreakdown(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                reportService.getStatusBreakdown(date != null ? date : LocalDate.now()));
    }

    // ── New endpoint — vendor decline analytics ───────────────────────
    @GetMapping("/vendor-declines")
    public ResponseEntity<List<VendorDeclineSummaryDTO>> getVendorDeclines(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(
                reportService.getVendorDeclines(date != null ? date : LocalDate.now()));
    }

    // ── CSV Exports ───────────────────────────────────────────────────

//    @GetMapping("/daily/export")
//    public void exportDailyCsv(
//            @RequestParam(required = false)
//            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
//            HttpServletResponse response) throws IOException {
//        LocalDate target = date != null ? date : LocalDate.now();
//        response.setContentType("text/csv");
//        response.setHeader("Content-Disposition",
//                "attachment; filename=daily-report-" + target + ".csv");
//        reportService.exportDailySummaryCSV(target, response.getWriter());
//    }
    @GetMapping("/daily/export")
    public void exportDailyCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletResponse response) throws IOException {
        LocalDate target = date != null ? date : LocalDate.now();
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=daily-report-" + target + ".csv");
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Expires", "0");
        reportService.exportDailySummaryCSV(target, response.getWriter());
        response.getWriter().flush();
        response.flushBuffer();
    }

    @GetMapping("/top-items/export")
    public void exportTopItemsCsv(
            @RequestParam(defaultValue = "10") int limit,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=top-items.csv");
        reportService.exportTopItemsCSV(limit, response.getWriter());
    }

    @GetMapping("/range/export")
    public void exportRangeCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=revenue-" + from + "-to-" + to + ".csv");
        reportService.exportRevenueRangeCSV(from, to, response.getWriter());
    }

    @GetMapping("/vendor-declines/export")
    public void exportVendorDeclinesCsv(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            HttpServletResponse response) throws IOException {
        LocalDate target = date != null ? date : LocalDate.now();
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition",
                "attachment; filename=vendor-declines-" + target + ".csv");
        reportService.exportVendorDeclinesCSV(target, response.getWriter());
    }
}