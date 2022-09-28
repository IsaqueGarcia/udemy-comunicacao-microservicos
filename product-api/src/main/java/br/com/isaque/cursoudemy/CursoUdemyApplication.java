package br.com.isaque.cursoudemy;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableRabbit
@EnableFeignClients
@SpringBootApplication
public class CursoUdemyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CursoUdemyApplication.class, args);
    }

}
