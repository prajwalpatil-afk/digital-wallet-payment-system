package com.wallet.service;

import com.razorpay.Utils;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RazorpaySignatureTest {

    @Test
    void verifyPaymentSignature_acceptsValidHmac() throws Exception {
        String secret = "dummy_secret_for_local_hmac_testing_only";
        String orderId = "order_test123";
        String paymentId = "pay_test456";
        String payload = orderId + "|" + paymentId;
        String signature = hmacSha256Hex(payload, secret);

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", orderId);
        options.put("razorpay_payment_id", paymentId);
        options.put("razorpay_signature", signature);

        assertTrue(Utils.verifyPaymentSignature(options, secret));
    }

    @Test
    void verifyPaymentSignature_rejectsTamperedSignature() throws Exception {
        String secret = "dummy_secret_for_local_hmac_testing_only";

        JSONObject options = new JSONObject();
        options.put("razorpay_order_id", "order_test123");
        options.put("razorpay_payment_id", "pay_test456");
        options.put("razorpay_signature", "deadbeef");

        assertFalse(Utils.verifyPaymentSignature(options, secret));
    }

    private static String hmacSha256Hex(String data, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
