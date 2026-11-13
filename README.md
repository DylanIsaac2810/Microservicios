# Práctica 8 — Microservicios (Billetera Digital)

Evolución de la P7: la billetera deja de ser un solo programa y se divide en tres
servicios independientes que se comunican por HTTP/JSON.

```
                 ┌──────────────────────┐
 Cliente ───────▶│  api-gateway  :8080  │   punto único de entrada + interfaz PWA
                 └─────────┬────────────┘
            ┌──────────────┴───────────────┐
            ▼                              ▼
 ┌──────────────────────┐  HTTP  ┌──────────────────────┐
 │ transaccion-service  │───────▶│   cuenta-service     │
 │        :8082         │        │        :8081         │
 │  historial propio    │        │   saldo propio       │
 └──────────────────────┘        └──────────────────────┘
```

| Servicio | Puerto | Responsabilidad | Datos propios |
|---|---|---|---|
| `api-gateway` | 8080 | Enruta `/api/cuentas/**` y `/api/transacciones/**`; sirve la interfaz | Ninguno |
| `cuenta-service` | 8081 | Saldo, depósitos y retiros (`synchronized`) | Saldos |
| `transaccion-service` | 8082 | Registra transacciones llamando a `cuenta-service` | Historial |

Requisitos: Java 17+ y Maven (opción A) o Docker (opción B).

## Opción A: sin contenedores (tres terminales)

```bash
cd cuenta-service       && mvn spring-boot:run     # terminal 1
cd transaccion-service  && mvn spring-boot:run     # terminal 2
cd api-gateway          && mvn spring-boot:run     # terminal 3
```

## Opción B: con Docker Compose

```bash
docker compose up --build        # o: docker-compose up --build
```

Abrir `http://localhost:8080` (interfaz). Detener con `Ctrl+C` y `docker compose down`.

## API (todo por el gateway, puerto 8080)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/cuentas/{id}/saldo` | Saldo de la cuenta (cuentas `1` y `2`) |
| POST | `/api/transacciones` | `{"cuentaId":"1","tipo":"DEPOSITO","cantidad":100}` |
| GET | `/api/transacciones` | Historial (opcional `?cuentaId=1`) |

Códigos del POST: `201` exitosa, `422` rechazada, `503` cuenta-service no disponible.
Cada servicio también responde directo en su puerto (`/cuentas/...` en 8081,
`/transacciones` en 8082).

## Pruebas para la demostración

```bash
# 1. Saldo
curl localhost:8080/api/cuentas/1/saldo

# 2. Depósito
curl -X POST localhost:8080/api/transacciones -H "Content-Type: application/json" \
  -d '{"cuentaId":"1","tipo":"DEPOSITO","cantidad":500}'

# 3. Retiro mayor al saldo (rechazada, 422)
curl -i -X POST localhost:8080/api/transacciones -H "Content-Type: application/json" \
  -d '{"cuentaId":"1","tipo":"RETIRO","cantidad":99999}'

# 4. Historial
curl localhost:8080/api/transacciones
```

**Aislamiento de fallos:** detén `cuenta-service` (Ctrl+C en su terminal, o
`docker compose stop cuenta-service`) y repite el paso 2. El POST responde `503` con
estado `FALLIDA`, el historial sigue funcionando y en la interfaz `cuenta-service` pasa a
"caído". Al volver a iniciarlo, todo se recupera solo.

## Subir a Git

```bash
git init
git add -A
git commit -m "Práctica 8: microservicios (cuenta, transacción y API gateway)"
git branch -M main
git remote add origin https://github.com/DylanIsaac2810/<tu-repo>.git
git push -u origin main
```
