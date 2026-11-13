package com.banco.transaccion;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Cliente HTTP hacia cuenta-service. Es la UNICA forma en que este servicio
 * toca el saldo: llamando a su API, nunca a su memoria.
 */
@Component
public class CuentaClient {

    private final RestClient http;

    public CuentaClient(@Value("${cuenta.url}") String urlCuentas) {
        SimpleClientHttpRequestFactory fabrica = new SimpleClientHttpRequestFactory();
        fabrica.setConnectTimeout(2000);   // si cuenta-service no esta, fallar rapido
        fabrica.setReadTimeout(3000);
        this.http = RestClient.builder()
                .baseUrl(urlCuentas)
                .requestFactory(fabrica)
                .build();
    }

    /** tipo: DEPOSITO | RETIRO */
    public ResultadoCuenta aplicar(String cuentaId, String tipo, double cantidad) {
        String accion = tipo.equals("DEPOSITO") ? "depositar" : "retirar";
        return http.post()
                .uri("/cuentas/{id}/{accion}", cuentaId, accion)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new Operacion(cantidad))
                .retrieve()
                .body(ResultadoCuenta.class);
    }
}
