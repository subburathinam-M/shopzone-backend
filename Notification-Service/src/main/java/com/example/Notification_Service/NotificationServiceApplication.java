package com.example.Notification_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NotificationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NotificationServiceApplication.class, args);
		System.out.println("📧 Notification Service started on port 8086");
        System.out.println("📨 Listening to Kafka topics: user-events, order-events, payment-events");
	}

}
