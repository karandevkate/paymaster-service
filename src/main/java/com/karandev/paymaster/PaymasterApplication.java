package com.karandev.paymaster;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PaymasterApplication {

	public static void main(String[] args) {
		SpringApplication.run(PaymasterApplication.class, args);
	}

}
