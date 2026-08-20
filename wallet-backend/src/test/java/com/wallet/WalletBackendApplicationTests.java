package com.wallet;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"DB_PASSWORD=root",
		"JWT_SECRET=test-secret-must-be-at-least-32-characters-long"
})
class WalletBackendApplicationTests {

	@Test
	void contextLoads() {
	}

}
