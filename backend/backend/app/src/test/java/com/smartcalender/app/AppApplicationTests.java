package com.smartcalender.app;

import com.smartcalender.app.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@TestPropertySource(properties = {
	"spring.datasource.url=jdbc:h2:mem:testdb",
	"spring.datasource.driver-class-name=org.h2.Driver",
	"spring.jpa.hibernate.ddl-auto=create-drop",
	"spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class AppApplicationTests {

	@MockitoBean
	private UserRepository userRepository;

	@MockitoBean
	private ActivityRepository activityRepository;

	@MockitoBean
	private CategoryRepository categoryRepository;

	@MockitoBean
	private PasswordResetTokenRepository passwordResetTokenRepository;

	@MockitoBean
	private RefreshTokenRepository refreshTokenRepository;

	@MockitoBean
	private TaskRepository taskRepository;

	@MockitoBean
	private OtpRepository otpRepository;

	@Test
	void contextLoads() {
	}

}
