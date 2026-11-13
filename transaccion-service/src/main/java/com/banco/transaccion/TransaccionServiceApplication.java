package com.banco.transaccion;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TransaccionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransaccionServiceApplication.class, args);
        System.out.println("=== [transaccion-service] iniciado en el puerto 8082 ===");
    }
}
