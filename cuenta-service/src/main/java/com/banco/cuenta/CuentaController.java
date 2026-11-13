package com.banco.cuenta;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * API del microservicio de cuentas.
 *   GET  /cuentas/{id}/saldo
 *   POST /cuentas/{id}/depositar   {"cantidad": 100}
 *   POST /cuentas/{id}/retirar     {"cantidad": 50}
 */
@RestController
@RequestMapping("/cuentas")
public class CuentaController {

    // Almacen propio de este servicio (en memoria). Ningun otro servicio accede a el.
    private final Map<String, CuentaBancaria> cuentas = new ConcurrentHashMap<>();

    public CuentaController() {
        cuentas.put("1", new CuentaBancaria(1000.0));
        cuentas.put("2", new CuentaBancaria(500.0));
    }

    @GetMapping("/{id}/saldo")
    public ResultadoCuenta saldo(@PathVariable String id) {
        CuentaBancaria cuenta = buscar(id);
        return new ResultadoCuenta(true, "Saldo actual", cuenta.consultarSaldo());
    }

    @PostMapping("/{id}/depositar")
    public ResultadoCuenta depositar(@PathVariable String id, @RequestBody Operacion op) {
        CuentaBancaria cuenta = buscar(id);
        boolean ok = cuenta.depositar(op.cantidad());
        System.out.println("[cuenta-service] (" + Thread.currentThread().getName() + ") deposito $"
                + op.cantidad() + " en cuenta " + id + " -> " + (ok ? "OK" : "RECHAZADO")
                + ", saldo $" + cuenta.consultarSaldo());
        return new ResultadoCuenta(ok,
                ok ? "Deposito completado" : "La cantidad debe ser mayor que cero",
                cuenta.consultarSaldo());
    }

    @PostMapping("/{id}/retirar")
    public ResultadoCuenta retirar(@PathVariable String id, @RequestBody Operacion op) {
        CuentaBancaria cuenta = buscar(id);
        boolean ok = cuenta.retirar(op.cantidad());
        System.out.println("[cuenta-service] (" + Thread.currentThread().getName() + ") retiro $"
                + op.cantidad() + " en cuenta " + id + " -> " + (ok ? "OK" : "RECHAZADO")
                + ", saldo $" + cuenta.consultarSaldo());
        return new ResultadoCuenta(ok,
                ok ? "Retiro completado" : "Cantidad invalida o saldo insuficiente",
                cuenta.consultarSaldo());
    }

    private CuentaBancaria buscar(String id) {
        CuentaBancaria cuenta = cuentas.get(id);
        if (cuenta == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "La cuenta " + id + " no existe");
        }
        return cuenta;
    }
}
