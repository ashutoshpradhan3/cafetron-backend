package com.cafetron.orderQR;

import com.cafetron.order.Order;

public interface OrderQRService {

    String generateAndStoreQR(Order order);

}
