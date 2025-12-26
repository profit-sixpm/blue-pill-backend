package com.sixpm;

import com.sixpm.config.TestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
@EnableAutoConfiguration(exclude = {
	io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration.class,
	io.awspring.cloud.autoconfigure.core.CredentialsProviderAutoConfiguration.class,
	io.awspring.cloud.autoconfigure.core.RegionProviderAutoConfiguration.class
})
class ApplicationTests {

	@Test
	void contextLoads() {
	}

}

