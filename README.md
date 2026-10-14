# RICA — Red Inteligente de Investigación y Colaboración Académica

Proyecto de clase de Ingeniería de Software II (UPTC). El repositorio contiene **dos
microservicios independientes**, un **registro de servicios**, un **API Gateway** y la
infraestructura de mensajería que los conecta.

## Componentes

| Componente | Puerto | Dependencias | Responsabilidad |
| --- | --- | --- | --- |
| `api-gateway` | 8080 | Eureka, Redis | Punto de entrada único: rutas, filtros y límite de tasa (Spring Cloud Gateway) |
| `eureka-server` | 8761 | — | Registro de servicios: las instancias se auto-registran y el gateway las descubre |
| `investigadores-service` | 8081 | PostgreSQL, Kafka, Eureka | Agregado `Investigador`, Value Object `CorreoInstitucional`, Factory, API REST y Outbox del evento `InvestigadorRegistrado` |
| `publicaciones-service` | 8082 | MongoDB, Kafka, Eureka | Publicaciones con Saga del límite anual, consumidor idempotente de `InvestigadorRegistrado` y cliente HTTP resiliente hacia investigadores |

El frontend (`rica-frontend`) sigue hablando con `http://localhost:8080/api`; el gateway
mantiene ese contrato mientras cada servicio evoluciona y se despliega por separado.

## Comunicación entre servicios (Lección 6)

- **REST síncrono con resiliencia** — `publicaciones-service` confirma al investigador con
  `GET /api/investigadores/existe?correo=...` a través del puerto `ConsultaInvestigadores`,
  protegido con Resilience4j (Circuit Breaker + Retry) y timeouts explícitos en el cliente.
- **Mensajería asíncrona** — al registrar un investigador, `investigadores-service` guarda
  `InvestigadorRegistrado` en su tabla outbox dentro de la misma transacción (Transactional
  Outbox) y un publicador programado lo envía al topic `investigadores-eventos` (Kafka).
  `publicaciones-service` lo consume de forma idempotente (colección `eventos_procesados`).
- **Saga con compensación** — registrar una publicación crea el documento en estado
  `PENDIENTE`, confirma investigador y límite anual, y lo pasa a `REGISTRADA`; si algo falla,
  lo compensa marcándolo `RECHAZADA`.
- **API Gateway + descubrimiento** — el gateway enruta `lb://investigadores-service` y
  `lb://publicaciones-service` contra Eureka, con límite de tasa de 20 req/s (ráfaga 40)
  sobre publicaciones. Rutas activas en `/actuator/gateway/routes`.

## Cómo correr el stack completo (Docker)

```bash
docker compose up -d --build
```

- API por el gateway: http://localhost:8080/api/status
- Rutas del gateway: http://localhost:8080/actuator/gateway/routes
- Registro de Eureka: http://localhost:8761
- Swagger de investigadores: http://localhost:8081/swagger-ui/index.html
- Swagger de publicaciones: http://localhost:8082/swagger-ui/index.html
- Frontend: http://localhost:5173

## Desarrollo local (servicios en tu máquina)

```bash
docker compose up -d rica-db rica-mongo kafka redis eureka-server
./mvnw -pl investigadores-service spring-boot:run
./mvnw -pl publicaciones-service spring-boot:run
./mvnw -pl api-gateway spring-boot:run
```

## Pruebas

```bash
./mvnw test                                # todos los módulos
./mvnw -pl publicaciones-service test      # solo uno
```

## Estructura

- `investigadores-service/` y `publicaciones-service/`: cada uno es su propio hexágono
  (`dominio`, `aplicacion`, `infraestructura`) y su propio proyecto desplegable.
- `api-gateway/`: Spring Cloud Gateway con rutas, límite de tasa y descubrimiento por Eureka.
- `eureka-server/`: registro de servicios.
- `docs/`: modelo de dominio, ADRs y diagramas C4.
- `bruno/`: colección de peticiones contra la API (a través del gateway, puerto 8080).
