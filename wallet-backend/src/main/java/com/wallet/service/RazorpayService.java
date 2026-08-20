package com.wallet.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import com.wallet.exception.ApiException;
import jakarta.annotation.PostConstruct;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class RazorpayService {

    private final String keyId;
    private final String keySecret;
    private RazorpayClient client;

    public RazorpayService(
            @Value("${razorpay.key-id}") String keyId,
            @Value("${razorpay.key-secret}") String keySecret
    ) {
        this.keyId = keyId;
        this.keySecret = keySecret;
    }

    @PostConstruct
    void init() throws RazorpayException {
        if (keyId == null || keyId.isBlank() || keySecret == null || keySecret.isBlank()) {
            throw new IllegalStateException("RAZORPAY_KEY_ID and RAZORPAY_KEY_SECRET must be set");
        }
        this.client = new RazorpayClient(keyId, keySecret);
    }

    public String getKeyId() {
        return keyId;
    }

    /**
     * Creates a Razorpay order. Amount is in INR rupees; Razorpay expects paise.
     */
    public Order createOrder(BigDecimal amountInRupees, String receipt, Long userId) {
        try {
            int amountPaise = toPaise(amountInRupees);

            JSONObject request = new JSONObject();
            request.put("amount", amountPaise);
            request.put("currency", "INR");
            request.put("receipt", receipt);
            request.put("payment_capture", 1);

            JSONObject notes = new JSONObject();
            notes.put("userId", String.valueOf(userId));
            notes.put("amountRupees", amountInRupees.setScale(2, RoundingMode.HALF_UP).toPlainString());
            request.put("notes", notes);

            return client.orders.create(request);
        } catch (RazorpayException ex) {
            throw new ApiException("Failed to create Razorpay order: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    public Order fetchOrder(String orderId) {
        try {
            return client.orders.fetch(orderId);
        } catch (RazorpayException ex) {
            throw new ApiException("Failed to fetch Razorpay order: " + ex.getMessage(), HttpStatus.BAD_GATEWAY);
        }
    }

    public boolean verifySignature(String orderId, String paymentId, String signature) {
        try {
            JSONObject options = new JSONObject();
            options.put("razorpay_order_id", orderId);
            options.put("razorpay_payment_id", paymentId);
            options.put("razorpay_signature", signature);
            return Utils.verifyPaymentSignature(options, keySecret);
        } catch (RazorpayException ex) {
            return false;
        }
    }

    public BigDecimal amountRupeesFromOrder(Order order) {
        int amountPaise = order.get("amount");
        return BigDecimal.valueOf(amountPaise, 2);
    }

    private int toPaise(BigDecimal amountInRupees) {
        BigDecimal scaled = amountInRupees.setScale(2, RoundingMode.HALF_UP);
        if (scaled.compareTo(BigDecimal.ONE) < 0) {
            throw new ApiException("Amount must be at least ₹1.00", HttpStatus.BAD_REQUEST);
        }
        return scaled.multiply(BigDecimal.valueOf(100)).intValueExact();
    }


}
