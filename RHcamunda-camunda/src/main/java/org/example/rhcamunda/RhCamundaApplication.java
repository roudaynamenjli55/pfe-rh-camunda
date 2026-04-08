package org.example.rhcamunda;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableProcessApplication  // ✅ ✅ ✅ HADHI L LIGNE LI TACTIVI CAMUNDA ✅ ✅ ✅
public class RhCamundaApplication {

    public static void main(String[] args) {
        SpringApplication.run(RhCamundaApplication.class, args);
    }
}