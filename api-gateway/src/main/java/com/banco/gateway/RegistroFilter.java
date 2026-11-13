package com.banco.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Filtro global: deja en consola que peticion paso por el gateway y con que codigo
 * respondio el servicio de destino. Para no llenar el log, omite los GET que van bien
 * (la interfaz los repite cada pocos segundos).
 */
@Component
public class RegistroFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String metodo = exchange.getRequest().getMethod().name();
        String ruta = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            HttpStatusCode codigo = exchange.getResponse().getStatusCode();
            boolean error = codigo != null && codigo.isError();
            if (ruta.startsWith("/api/") && (!metodo.equals("GET") || error)) {
                System.out.println("[api-gateway] " + metodo + " " + ruta + " -> HTTP " + codigo);
            }
        }));
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
