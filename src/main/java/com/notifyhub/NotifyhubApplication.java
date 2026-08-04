package com.notifyhub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "currentUserAuditorAware")
public class NotifyhubApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotifyhubApplication.class, args);
	}

}
