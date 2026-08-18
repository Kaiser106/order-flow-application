package com.orderflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;


@SpringBootApplication
@EnableJpaAuditing// Entity'lerdeki @CreatedDate ve @LastModifiedDate anotasyonlarının çalışmasını sağlar.
// Bu sayede tarih atama işlemlerini manuel olarak yapmaktan (örn: entity.setCreatedAt(new Date())) kurtuluruz.
public class OrderFlowApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrderFlowApplication.class, args);
	}
}