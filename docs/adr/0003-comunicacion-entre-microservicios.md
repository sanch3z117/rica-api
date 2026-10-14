# ADR-0003: Comunicación entre microservicios (Lección 6)

- **Fecha:** 13 de octubre de 2026
- **Estado:** aceptada
- **Contexto relacionado:** Lecciones 5 (microservicios) y 6 (comunicación), y su mini-curso complementario de gRPC y mensajería

## Contexto

Tras ADR-0002, `investigadores-service` y `publicaciones-service` ya eran dos procesos
independientes con bases de datos propias, pero la única comunicación entre ellos era una
llamada REST síncrona sin ninguna protección (sin timeouts, sin reintentos, sin cortocircuito),
y la fachada nginx enrutaba sin balanceo ni políticas de tráfico. La Lección 6 pide, para este
sistema: un API Gateway, descubrimiento de servicios, resiliencia en la llamada entre
servicios, un Domain Event publicado de forma confiable y una saga con compensación para el
flujo del límite anual.

## Decisión

- **API Gateway con Spring Cloud Gateway** (`api-gateway`, variante WebFlux) que reemplaza la
  fachada nginx de ADR-0002. Rutas: `/api/investigadores/**` → `investigadores-service`,
  `/api/publicaciones/**` y `/api/status` → `publicaciones-service`, con un filtro
  `RequestRateLimiter` (20 req/s, ráfaga 40, respaldado en Redis) sobre la ruta de
  publicaciones. Se usa el namespace de configuración de Gateway 5.x
  (`spring.cloud.gateway.server.webflux.routes`), que cambió respecto a los ejemplos
  anteriores de la librería.
- **Descubrimiento de servicios con Eureka** (`eureka-server`, puerto 8761). Los dos servicios
  y el gateway se auto-registran al arrancar; el gateway resuelve `lb://` contra el registro.
- **Resiliencia con Resilience4j** en el adaptador HTTP `ConsultaInvestigadoresHttpAdapter`:
  Circuit Breaker (ventana de 10, umbral de fallo 50 %, 10 s en estado abierto) + Retry
  (3 intentos, 300 ms) + timeouts explícitos en el `RestClient` (conexión 2 s, lectura 3 s).
  Un circuito abierto se sigue traduciendo a `503 Service Unavailable`.
- **Mensajería asíncrona con Kafka**: al registrar un investigador,
  `investigadores-service` guarda el evento `InvestigadorRegistrado` en su tabla outbox dentro
  de la **misma transacción** que el investigador (Transactional Outbox) y un publicador
  programado (cada 2 s) lo envía al topic `investigadores-eventos`.
  `publicaciones-service` lo consume de forma **idempotente**: cada `eventoId` se reclama una
  única vez en la colección `eventos_procesados` antes de procesar el mensaje.
- **Saga orquestada** en `publicaciones-service` para registrar una publicación: crea el
  documento en estado `PENDIENTE`, confirma el investigador (llamada remota protegida), valida
  el límite anual y lo pasa a `REGISTRADA`; si cualquier paso falla, **compensa marcándolo
  como `RECHAZADA`**. El listado por investigador solo muestra las `REGISTRADA`.
- **No se adopta gRPC** para `rica-api`: la guía indica que REST sigue siendo la opción
  correcta por defecto a este tamaño, y gRPC queda cubierto por el mini-curso complementario
  (proyecto aparte). Tampoco service mesh: la guía lo descarta explícitamente para dos o tres
  servicios.

## Consecuencias

**Positivas**

- El punto de entrada vuelve a ser código Spring: rutas, filtros y límite de tasa viven en el
  repositorio y se pueden verificar con `/actuator/gateway/routes`.
- Cuando investigadores se degrada, la llamada falla rápido (circuito abierto) y los fallos
  transitorios se reintentan, sin agotar los hilos de publicaciones.
- El registro de un investigador se notifica sin acoplamiento directo, y la entrega es
  confiable aunque el proceso se caiga entre el guardado y la publicación (outbox).
- El flujo del límite anual queda íntegro sin transacciones distribuidas: pasos locales con
  compensación explícita.

**Negativas**

- Cuatro piezas de infraestructura más que operar (Eureka, Kafka, Redis y el gateway), además
  de los dos servicios y las dos bases de datos.
- Kafka introduce consistencia eventual en la notificación: el evento puede tardar hasta ~2 s
  en publicarse, y el contrato del payload hay que mantenerlo entre los dos servicios.
- Las anotaciones de Spring (`@Service`, `@Component`, `@Transactional`) se conservan como
  fuga controlada en la capa de aplicación, la misma tolerancia documentada en ADR-0001.
