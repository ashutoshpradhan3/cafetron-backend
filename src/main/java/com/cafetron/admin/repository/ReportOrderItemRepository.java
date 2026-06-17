package com.cafetron.admin.repository;

import com.cafetron.admin.dto.TopItemDTO;
import com.cafetron.cart.entity.OrderItem;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportOrderItemRepository extends JpaRepository<OrderItem, Long> {

    // ─────────────────────────────────────────────────────────────────
    // Top items by total quantity sold.
    // OrderItem.menuItem is a real @ManyToOne MenuItem.
    // MenuItem.itemName is the display name.
    // Joins through order to filter out cancelled orders.
    // ─────────────────────────────────────────────────────────────────
    @Query("""
        SELECT new com.cafetron.admin.dto.TopItemDTO(
            mi.id,
            mi.itemName,
            COALESCE(SUM(oi.quantity), 0)
        )
        FROM OrderItem oi
        JOIN oi.menuItem mi
        JOIN oi.order o
        WHERE o.overallStatus NOT IN ('CANCELLED', 'VENDOR_DECLINED', 'TIMEOUT')
          AND o.paymentStatus <> 'REFUNDED'
        GROUP BY mi.id, mi.itemName
        ORDER BY SUM(oi.quantity) DESC
    """)
    List<TopItemDTO> getTopItems(Pageable pageable);
}
