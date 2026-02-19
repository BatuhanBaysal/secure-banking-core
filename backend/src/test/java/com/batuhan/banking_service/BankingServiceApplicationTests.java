package com.batuhan.banking_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.ApplicationContext;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test for verifying the Spring Application Context.
 * This class ensures that all beans, configurations, and dependencies
 * are correctly wired and that the application can start without any failures.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Application Context - Smoke Test")
class BankingServiceApplicationTests {

	@Test
	@DisplayName("Success: Application context should load without any configuration errors")
	void contextLoads(ApplicationContext context) {
		assertThat(context).isNotNull();
	}
}