package com.cafetron.admin.repository;

import com.cafetron.admin.dto.DailySummaryDTO;
import com.cafetron.admin.dto.StatusCountDTO;
import com.cafetron.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportOrderRepository extends JpaRepository<Order, Long> {

    @Query("""
        SELECT new com.cafetron.admin.dto.DailySummaryDTO(
            COUNT(o.id),
            COALESCE(SUM(o.totalAmount), 0),
            COALESCE((
                SELECT SUM(oi.quantity) 
                FROM OrderItem oi 
                WHERE CAST(oi.order.createdAt AS date) = :date
                  AND oi.order.overallStatus NOT IN ('CANCELLED', 'VENDOR_DECLINED', 'TIMEOUT')
                  AND oi.order.paymentStatus <> 'REFUNDED'
            ), 0)
        )
        FROM Order o
        WHERE CAST(o.createdAt AS date) = :date
          AND o.overallStatus NOT IN ('CANCELLED', 'VENDOR_DECLINED', 'TIMEOUT')
          AND o.paymentStatus <> 'REFUNDED'
    """)
    DailySummaryDTO getDailySummary(@Param("date") LocalDate date);

    // ─────────────────────────────────────────────────────────────────
    // Native query — avoids Hibernate translating CAST in a way
    // MySQL only_full_group_by rejects.
    // Returns Object[] rows: [0]=date string, [1]=revenue BigDecimal
    // Mapped to RevenuePointDTO in ReportService.
    // ─────────────────────────────────────────────────────────────────
    @Query(value = """
        SELECT DATE(created_at) as date, COALESCE(SUM(total_amount), 0) as revenue
        FROM `Order`
        WHERE DATE(created_at) BETWEEN :from AND :to
          AND overall_status NOT IN ('CANCELLED', 'VENDOR_DECLINED', 'TIMEOUT')
          AND payment_status <> 'REFUNDED'
        GROUP BY DATE(created_at)
        ORDER BY DATE(created_at) ASC
    """, nativeQuery = true)
    List<Object[]> getRevenueRangeRaw(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);

    @Query("""
        SELECT new com.cafetron.admin.dto.StatusCountDTO(
            o.overallStatus,
            COUNT(o)
        )
        FROM Order o
        WHERE CAST(o.createdAt AS date) = :date
        GROUP BY o.overallStatus
        ORDER BY COUNT(o) DESC
    """)
    List<StatusCountDTO> getStatusBreakdown(@Param("date") LocalDate date);
}
