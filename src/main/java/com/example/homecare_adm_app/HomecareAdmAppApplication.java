package com.example.homecare_adm_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HomecareAdmAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(HomecareAdmAppApplication.class, args);
	}
}


