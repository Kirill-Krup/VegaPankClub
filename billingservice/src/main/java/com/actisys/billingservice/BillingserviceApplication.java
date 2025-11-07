package com.actisys.billingservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BillingserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BillingserviceApplication.class, args);
	}

}
