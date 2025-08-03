package com.unipi.gsimos.vistaseat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VistaseatApplication {

    public static void main(String[] args) {
        SpringApplication.run(VistaseatApplication.class, args);
    }

}
