package com.cafetron.pickup.repository;

import com.cafetron.pickup.VendorOrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VendorOrderStatusRepository extends JpaRepository<VendorOrderStatus, Long> {
    List<VendorOrderStatus> findByOrderItem_Order_Id(Long orderId);

    @Query("SELECT vos FROM VendorOrderStatus vos LEFT JOIN FETCH vos.orderItem WHERE vos.orderItem.order.id = :orderId")
    List<VendorOrderStatus> findByOrderItem_Order_IdWithOrderItem(@Param("orderId") Long orderId);
}

