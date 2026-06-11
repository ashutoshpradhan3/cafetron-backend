package com.cafetron.orderQR;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderQRRepository extends JpaRepository<OrderQR, Long> {
}
