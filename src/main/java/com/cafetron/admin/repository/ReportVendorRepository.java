package com.cafetron.admin.repository;

import com.cafetron.admin.dto.VendorDeclineSummaryDTO;
import com.cafetron.vendor.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReportVendorRepository extends JpaRepository<Vendor, Long> {

    @Query("""
        SELECT new com.cafetron.admin.dto.VendorDeclineSummaryDTO(
            v.id,
            v.name,
            COUNT(vos),
            COALESCE(SUM(t.amount), 0)
        )
        FROM VendorOrderStatus vos
        JOIN vos.vendor v
        JOIN vos.orderItem oi
        JOIN oi.order o
        LEFT JOIN com.cafetron.wallet.entity.Transaction t
            ON t.orderId = o.id
            AND t.vendorId = v.id
            AND t.type = com.cafetron.wallet.entity.TransactionType.REFUND
        WHERE vos.status IN (
            com.cafetron.pickup.VendorOrderStatusType.DECLINED,
            com.cafetron.pickup.VendorOrderStatusType.TIMEOUT
        )
          AND CAST(o.createdAt AS date) = :date
        GROUP BY v.id, v.name
        ORDER BY COUNT(vos) DESC
    """)
    List<VendorDeclineSummaryDTO> getVendorDeclineSummary(@Param("date") LocalDate date);
}