package com.wallet.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class RazorpayServiceTest {

    private static final String SECRET = "dummy_secret_for_local_hmac_testing_only";

    private RazorpayService razorpayService;

    @BeforeEach
    void setUp() {
        // Construct without calling @PostConstruct — verifySignature only needs the secret.
        razorpayService = new RazorpayService("rzp_test_dummy", SECRET);
    }

    @Test
    void verifySignature_valid() throws Exception {
        String orderId = "order_test123";
        String paymentId = "pay_test456";
        String signature = hmacSha256Hex(orderId + "|" + paymentId, SECRET);

        assertThat(razorpayService.verifySignature(orderId, paymentId, signature)).isTrue();
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
