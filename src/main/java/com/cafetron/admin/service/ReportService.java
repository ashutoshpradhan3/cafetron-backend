package com.cafetron.admin.service;

import com.cafetron.admin.dto.*;
import com.cafetron.admin.repository.ReportOrderItemRepository;
import com.cafetron.admin.repository.ReportOrderRepository;
import com.cafetron.admin.repository.ReportVendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final ReportOrderRepository orderRepository;
    private final ReportOrderItemRepository orderItemRepository;
    private final ReportVendorRepository vendorRepository;

    public DailySummaryDTO getDailySummary(LocalDate date) {
        DailySummaryDTO result = orderRepository.getDailySummary(date);
        if (result == null) {
            return new DailySummaryDTO(date, 0L, BigDecimal.ZERO, 0L);
        }
        result.setDate(date);
        return result;
    }

    public List<TopItemDTO> getTopItems(int limit) {
        if (limit <= 0) return List.of();
        return orderItemRepository.getTopItems(
                PageRequest.of(0, limit)   // page 0, size = limit
        );
    }

    public List<RevenuePointDTO> getRevenueRange(LocalDate from, LocalDate to) {
        return orderRepository.getRevenueRangeRaw(from, to)
                .stream()
                .map(row -> {
                    // MySQL connector 8+ may return LocalDate or java.sql.Date

                    LocalDate date;
                    if (row[0] instanceof LocalDate) {
                        date = (LocalDate) row[0];
                    } else if (row[0] instanceof java.sql.Date) {
                        date = ((java.sql.Date) row[0]).toLocalDate();
                    } else {
                        // fallback — parse from string
                        date = LocalDate.parse(row[0].toString());
                    }

                    BigDecimal revenue = row[1] != null
                            ? new BigDecimal(row[1].toString())
                            : BigDecimal.ZERO;

                    return new RevenuePointDTO(date, revenue);
                })
                .toList();
    }

    public List<StatusCountDTO> getStatusBreakdown(LocalDate date) {
        return orderRepository.getStatusBreakdown(date);
    }

    public List<VendorDeclineSummaryDTO> getVendorDeclines(LocalDate date) {
        return vendorRepository.getVendorDeclineSummary(date);
    }

    // ── CSV Exports ──────────────────────────────────────────────────

    public void exportDailySummaryCSV(LocalDate date, PrintWriter writer) {
        DailySummaryDTO d = getDailySummary(date);
        writer.println("date,orderCount,revenue,itemsSold");
        writer.printf("%s,%d,%s,%d%n",
                d.getDate(), d.getOrderCount(),
                d.getRevenue().toPlainString(), d.getItemsSold());
    }

    public void exportTopItemsCSV(int limit, PrintWriter writer) {
        writer.println("menuItemId,name,qtySold");
        getTopItems(limit).forEach(item ->
                writer.printf("%d,%s,%d%n",
                        item.getMenuItemId(),
                        escapeCsv(item.getName()),
                        item.getQtySold()));
    }

    public void exportRevenueRangeCSV(LocalDate from, LocalDate to, PrintWriter writer) {
        writer.println("date,revenue");
        getRevenueRange(from, to).forEach(p ->
                writer.printf("%s,%s%n",
                        p.getDate(),
                        p.getRevenue().toPlainString()));
    }

    public void exportVendorDeclinesCSV(LocalDate date, PrintWriter writer) {
        writer.println("vendorId,vendorName,declineCount,totalRefunded");
        getVendorDeclines(date).forEach(v ->
                writer.printf("%d,%s,%d,%s%n",
                        v.getVendorId(),
                        escapeCsv(v.getVendorName()),
                        v.getDeclineCount(),
                        v.getTotalRefunded().toPlainString()));
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        String v = value;
        // Prevent formula injection in spreadsheet apps
        if (!v.isEmpty() && "=+-@\t".indexOf(v.charAt(0)) >= 0) {
            v = "'" + v;
        }
        // Quote if contains comma, quote, or newline
        if (v.contains(",") || v.contains("\"")
                || v.contains("\n") || v.contains("\r")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}