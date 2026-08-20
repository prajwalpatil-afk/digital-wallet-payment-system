package com.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"DB_PASSWORD=root",
		"JWT_SECRET=test-secret-must-be-at-least-32-characters-long",
		"RAZORPAY_KEY_ID=rzp_test_dummy",
		"RAZORPAY_KEY_SECRET=dummy_secret_for_local_tests_only"
})
class WalletBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
