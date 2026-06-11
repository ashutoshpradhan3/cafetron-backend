package com.cafetron.orderQR;

import com.cafetron.order.Order;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Service
public class OrderQRServiceImpl implements OrderQRService{

    @Autowired
    private OrderQRRepository orderQRRepository;


    @Override
    public String generateAndStoreQR(Order order) {

        try {
            String token = UUID.randomUUID().toString();
            String qrData = encodeTokenToQR(token);

            OrderQR orderQR = new OrderQR();

            orderQR.setOrder(order);
            orderQR.setQrData(qrData);
            orderQR.setCreatedAt(LocalDateTime.now());

            orderQRRepository.save(orderQR);

            log.info("QR generated successfully for the given Order[id: {}]", order.getId());

            return token;

        } catch (Exception e) {

            log.error("Failed to generate QR Code for the given order! Exception: {}", e.getMessage());

            return null;
        }
    }

     private String encodeTokenToQR( String token ) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(token, BarcodeFormat.QR_CODE, 250, 250);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(matrix, "PNG", stream);

        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }

}
