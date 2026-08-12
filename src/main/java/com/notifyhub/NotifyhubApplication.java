package com.notifyhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "currentUserAuditorAware")
@EnableScheduling
public class NotifyhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotifyhubApplication.class, args);
	}

}
