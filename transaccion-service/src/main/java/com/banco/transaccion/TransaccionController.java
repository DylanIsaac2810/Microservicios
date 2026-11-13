package com.banco.transaccion;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API del microservicio de transacciones.
 *   POST /transacciones                 {"cuentaId":"1","tipo":"DEPOSITO","cantidad":100}
 *   GET  /transacciones[?cuentaId=1]
 *
 * Codigos HTTP del POST: 201 exitosa, 422 rechazada, 503 cuenta-service no disponible.
 */
@RestController
@RequestMapping("/transacciones")
public class TransaccionController {

    private final TransaccionServicio servicio;

    public TransaccionController(TransaccionServicio servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<Transaccion> crear(@RequestBody SolicitudTransaccion solicitud) {
        Transaccion t = servicio.procesar(solicitud);
        HttpStatus estado = switch (t.estado()) {
            case "EXITOSA" -> HttpStatus.CREATED;
            case "RECHAZADA" -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> HttpStatus.SERVICE_UNAVAILABLE;
        };
        return ResponseEntity.status(estado).body(t);
    }

    @GetMapping
    public List<Transaccion> listar(@RequestParam(required = false) String cuentaId) {
        return servicio.historial(cuentaId);
    }
}
