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
		Dotenv dotenv = null;

		String[] possiblePaths = {
			"./backend/backend/app",
			"./SmartCalendar/backend/backend/app",
			"./",
		};

		for (String path : possiblePaths) {
			try {
				dotenv = Dotenv.configure()
						.directory(path)
						.load();
				System.out.println("Loaded .env from: " + path);
				break;
			} catch (Exception e) {
				System.out.println("Failed to load .env from: " + path);
				System.out.println("Trying next path...");
			}
		}

		if (dotenv == null) {
			dotenv = Dotenv.configure()
					.ignoreIfMissing()
					.load();
			System.out.println("⚠ No .env file found, using system environment variables");
		}

		if (dotenv != null) {
			dotenv.entries().forEach(entry -> {
				System.setProperty(entry.getKey(), entry.getValue());
			});
		}

		SpringApplication.run(AppApplication.class, args);
	}

}
