package com.smartcalender.app;

import com.smartcalender.app.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
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
