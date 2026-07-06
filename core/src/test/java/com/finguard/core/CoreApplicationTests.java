package com.finguard.core;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "spring.config.import=optional:file:.env[.properties]")
class CoreApplicationTests {

	@Test
	void contextLoads() {
	}

}