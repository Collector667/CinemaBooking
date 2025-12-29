package com.CinemaManager.Cinema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CinemaApplication {

	public static void main(String[] args) {
		SpringApplication.run(CinemaApplication.class, args);
		printStartupInfo();
	}

	private static void printStartupInfo() {
		System.out.println("\n=========================================");
		System.out.println("🎬 Cinema Booking System Started!");
		System.out.println("=========================================");
		System.out.println("🌐 Local: http://localhost:8080");
		System.out.println("📡 API Test: http://localhost:8080/api/test");
		System.out.println("🏥 Health: http://localhost:8080/health");
		System.out.println("🎥 Movies API: http://localhost:8080/api/movies");
		System.out.println("💾 Database: MySQL (cinema_db)");
		System.out.println("=========================================\n");
	}
}