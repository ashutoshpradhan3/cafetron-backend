package com.cafetron.orderQR.controller;

import com.cafetron.order.entity.Order;
import com.cafetron.order.repository.OrderRepository;
import com.cafetron.orderQR.dto.DecodeQRResponse;
import com.cafetron.orderQR.dto.GenQRResponse;
import com.cafetron.orderQR.entity.OrderQR;
import com.cafetron.orderQR.exception.QRDecodeException;
import com.cafetron.orderQR.repository.OrderQRRepository;
import com.cafetron.orderQR.service.OrderQRService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/order-qr")
public class OrderQRController {

    @Autowired
    OrderQRService orderQRService;

    @Autowired
    OrderQRRepository orderQRRepository;

    @Autowired
    OrderRepository orderRepository;

    @GetMapping
    public ResponseEntity<GenQRResponse> findQR(@RequestParam("orderId") Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElse(null);

        if ( order == null ) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new GenQRResponse(null, "Order not found"));
        }

        OrderQR orderQR = orderQRRepository.findByOrderId(orderId).orElse(null);

        if ( orderQR == null ) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new GenQRResponse(null, "No QR code found for this order"));
        }

        String base64String = orderQR.getQrData();

        if ( base64String == null ) {
            return ResponseEntity.internalServerError()
                    .body(new GenQRResponse(null, "Failed to generate QR code"));
        }

        return ResponseEntity.ok(new GenQRResponse(base64String, "QR code generated successfully"));

    }

    @PostMapping
    public ResponseEntity<DecodeQRResponse> decodeQR(@RequestParam("qr") MultipartFile file) {
        String token;

        try {
            token = orderQRService.decodeQRFromImage(file);
        } catch (QRDecodeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new DecodeQRResponse(false, null, e.getMessage()));
        }

        if ( token == null ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new DecodeQRResponse(false, null, "Failed to decode QR code"));
        }

        boolean doesOrderExist = orderRepository.findByToken(token).isPresent();

        if ( !doesOrderExist ) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new DecodeQRResponse(false, null, "No order found for this token"));
        }

        return ResponseEntity.ok(new DecodeQRResponse(true, token, "QR code decoded successfully"));
    }
}
