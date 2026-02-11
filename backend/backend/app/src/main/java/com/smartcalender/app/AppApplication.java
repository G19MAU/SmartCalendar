package com.smartcalender.app;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableAsync
@EnableScheduling
public class AppApplication {

	public static void main(String[] args) {
		// Load .env file with fallback mechanism
		Dotenv dotenv = null;

		try {
			dotenv = Dotenv.configure()
					.directory("./")
					.load();
			System.out.println("Loaded .env from current directory");
		} catch (Exception e) {
			try {
				dotenv = Dotenv.configure()
						.directory("./backend/backend/app")
						.load();
				System.out.println("Loaded .env from backend/backend/app directory");
			} catch (Exception e2) {
				dotenv = Dotenv.configure()
						.ignoreIfMissing()
						.load();
				System.out.println("No .env file found, using system environment variables");
			}
		}

		if (dotenv != null) {
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
			});
		}

		SpringApplication.run(AppApplication.class, args);
	}

}
