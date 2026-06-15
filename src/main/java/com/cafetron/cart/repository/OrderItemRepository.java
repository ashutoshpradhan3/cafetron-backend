package com.cafetron.cart.repository;

import com.cafetron.cart.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {
    List<OrderItem> findByOrder_Id(Long orderId);

    @Query("SELECT oi FROM OrderItem oi LEFT JOIN FETCH oi.menuItem WHERE oi.order.id = :orderId")
    List<OrderItem> findByOrder_IdWithMenuItems(@Param("orderId") Long orderId);
}