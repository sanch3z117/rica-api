# NOTAS — Auditoría de la descomposición (Taller 5)

## Dominio y criterio de corte

RICA se dividió **por subdominio**, no por capa técnica. De los Bounded Contexts
identificados con DDD, `investigadores` y `publicaciones` quedaron como dos servicios
desplegables independientes. `eureka-server` y `api-gateway` son infraestructura de
comunicación, no servicios de negocio.

## Servicios y responsabilidades

- **investigadores-service (8081):** Agregado `Investigador`, Value Object `CorreoInstitucional`
  y `InvestigadorFactory`; persiste en PostgreSQL y publica `InvestigadorRegistrado` con
  Transactional Outbox.
- **publicaciones-service (8082):** `Publicacion` y el servicio de límite anual; persiste en
  MongoDB, aplica la Saga con compensación y consume eventos de forma idempotente.
- **eureka-server (8761):** registro y descubrimiento de instancias.
- **api-gateway (8080):** punto de entrada único; enruta con Spring Cloud Gateway y aplica
  límite de tasa con Redis.

## De llamada en memoria a llamada de red

Antes, `PublicacionService` resolvía "¿existe este investigador?" con una llamada de método
al `InvestigadorRepository` dentro del mismo proceso. Ahora esa pregunta cruza la red: el
`ConsultaInvestigadoresHttpAdapter` invoca `GET /api/investigadores/existe`, protegido con
Resilience4j (circuit breaker + retry) y timeouts explícitos. Es el cambio conceptual central
del taller: un límite de dominio se convirtió en un límite de proceso.

## Datos por servicio

Cada servicio es dueño de su base (Database per Service): PostgreSQL para investigadores y
MongoDB para publicaciones. No existe un JOIN entre ambos; la consistencia se resuelve con
eventos y con la consulta HTTP, nunca con una transacción compartida.

## Compensaciones

Se ganó despliegue independiente y aislamiento de fallos a cambio de latencia de red,
consistencia eventual y más piezas de infraestructura que operar (registro, gateway, bróker y
tolerancia a fallos). La granularidad se mantuvo en dos servicios de negocio para no caer en
nanoservicios ni en un monolito distribuido.

## Migración

La separación se hizo sin reescribir reglas de negocio: cada contexto se movió a su propio
proyecto Spring Boot y la llamada de método se reemplazó por una llamada HTTP. Es el espíritu
de **Strangler Fig** aplicado sobre un monolito modular ya bien delimitado.

## Decisión

Los microservicios aquí se justifican por límites de dominio claros y necesidad de despliegue
independiente. No se separó por entidad ni se compartió base de datos entre servicios.
