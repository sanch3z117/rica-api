# ADR-0002: Descomponer RICA en dos microservicios

- **Fecha:** 29 de septiembre de 2026
- **Estado:** aceptada
- **Contexto relacionado:** Lecciones 3 (DDD), 4 (hexagonal) y 5 (microservicios)

## Contexto

Con el trabajo de DDD y de arquitectura hexagonal ya hecho, `rica-api` era un monolito
modular con dos Bounded Contexts separados en el código (`investigadores` y
`publicaciones`), cada uno con su propio hexágono, y con dos motores de base de datos
distintos desde los tutoriales de persistencia: PostgreSQL para investigadores y MongoDB
para publicaciones. Esa separación ya impedía cualquier `JOIN`: la verificación "¿existe
este investigador?" se resolvía con una llamada en memoria al puerto
`RepositorioInvestigadores`.

Lo único que el monolito modular no daba era **despliegue independiente**: cualquier
cambio en publicaciones obligaba a redesplegar también investigadores, aunque no hubiera
cambiado una línea.

## Decisión

Convertir el repositorio en un proyecto Maven multimódulo con **dos servicios desplegables
de forma independiente**:

- `investigadores-service` (puerto 8081) con su base de datos PostgreSQL.
- `publicaciones-service` (puerto 8082) con su base de datos MongoDB.

La verificación de la existencia del investigador —que antes era una llamada de método— pasa
a ser una **llamada de red** al endpoint interno `GET /api/investigadores/existe`, detrás
del mismo puerto `ConsultaInvestigadores` (solo cambia el adaptador).

Para no romper el contrato del frontend (que consume un único `http://localhost:8080/api`),
se agrega una **fachada** (`api-gateway`, nginx) que enruta `/api/investigadores` al servicio
de investigadores y el resto de rutas al de publicaciones.

## Consecuencias

**Positivas**

- Cada servicio se construye, despliega y escala por separado (`./mvnw -pl publicaciones-service package`).
- Los límites de dominio ya no dependen de la disciplina del equipo: son procesos distintos,
  con bases de datos privadas.
- El fallo del servicio de investigadores no se confunde con un error de negocio: el
  adaptador HTTP lo traduce en `503 Service Unavailable`.

**Negativas**

- La llamada local se volvió llamada de red: hay latencia, puede fallar y hay que mantener
  el contrato HTTP interno.
- El repositorio ahora tiene tres artefactos desplegables (dos servicios y la fachada) más
  las dos bases de datos.
- Quedan pendientes, para la Lección 6: descubrimiento de servicios, circuit breaker,
  trazabilidad distribuida y saga. La fachada nginx cumple hoy el papel de *API Gateway*
  mínimo, sin balanceo ni políticas de resiliencia.
