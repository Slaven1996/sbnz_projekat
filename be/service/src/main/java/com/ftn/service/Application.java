package com.ftn.service;

import org.kie.api.KieServices;
import org.kie.api.builder.KieScanner;
import org.kie.api.runtime.KieContainer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
   
    @Bean
	public KieContainer kieContainer()
		{
			KieServices ks = KieServices.Factory.get();
			KieContainer kContainer = ks
					.newKieContainer(ks.newReleaseId("com.ftn.sbnz", "kjar", "0.0.1-SNAPSHOT"));
			KieScanner kScanner = ks.newKieScanner(kContainer);
			kScanner.start(10_000);
			return kContainer;
		}
		
	@Override
	public void run(String... args) throws Exception 
	{
		HydroMonitoringExamples.main(args);
	}

}