package com.banco.cuenta;

/** Respuesta JSON del microservicio de cuentas. */
public record ResultadoCuenta(boolean ok, String mensaje, double saldo) {
}
