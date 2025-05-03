package com.tennisfolio.Tennisfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class TennisfolioApplication {

	public static void main(String[] args) {
		SpringApplication.run(TennisfolioApplication.class, args);
	}

}
