package com.cafetron.orderQR.repository;

import com.cafetron.orderQR.entity.OrderQR;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderQRRepository extends JpaRepository<OrderQR, Long> {

    Optional<OrderQR> findByOrderId(Long orderId);
}
