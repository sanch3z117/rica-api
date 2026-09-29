# RICA — Red Inteligente de Investigación y Colaboración Académica

Proyecto de clase de Ingeniería de Software II (UPTC). El repositorio contiene **dos
microservicios independientes** y una **fachada** que conserva un único punto de entrada.

## Servicios

| Servicio | Puerto | Base de datos | Responsabilidad |
| --- | --- | --- | --- |
| `investigadores-service` | 8081 | PostgreSQL (`rica`) | Agregado `Investigador`, Value Object `CorreoInstitucional`, Factory y API REST |
| `publicaciones-service` | 8082 | MongoDB (`rica`) | Publicaciones, servicio de dominio del límite anual y API REST |
| `api-gateway` (nginx) | 8080 | — | Enruta `/api/investigadores` al servicio de investigadores y el resto al de publicaciones |

El frontend (`rica-frontend`) sigue hablando con `http://localhost:8080/api`: la fachada
mantiene ese contrato mientras cada servicio evoluciona y se despliega por separado.

`publicaciones-service` ya no consulta la base de datos de investigadores: le pregunta al
servicio por HTTP (`GET /api/investigadores/existe?correo=...`), a través del puerto
`ConsultaInvestigadores`.

## Cómo correr el stack completo (Docker)

```bash
docker compose up -d --build
```

- API por la fachada: http://localhost:8080/api/status
- Swagger de investigadores: http://localhost:8081/swagger-ui/index.html
- Swagger de publicaciones: http://localhost:8082/swagger-ui/index.html
- Frontend: http://localhost:5173

## Desarrollo local (servicios en tu máquina)

```bash
docker compose up -d rica-db rica-mongo
./mvnw -pl investigadores-service spring-boot:run
./mvnw -pl publicaciones-service spring-boot:run
```

Si además quieres usar el frontend contra la fachada, levanta solo el gateway apuntando a
los servicios locales:

```bash
docker compose run --rm --publish 8080:80 \
  -e INVESTIGADORES_UPSTREAM=http://host.docker.internal:8081 \
  -e PUBLICACIONES_UPSTREAM=http://host.docker.internal:8082 \
  api-gateway
```

## Pruebas

```bash
./mvnw test                                # los dos servicios
./mvnw -pl investigadores-service test     # solo uno
```

## Estructura

- `investigadores-service/` y `publicaciones-service/`: cada uno es su propio hexágono
  (`dominio`, `aplicacion`, `infraestructura`) y su propio proyecto desplegable.
- `gateway/`: configuración de nginx que conserva el contrato de entrada.
- `docs/`: modelo de dominio, ADRs y diagramas C4.
- `bruno/`: colección de peticiones contra la API (a través de la fachada, puerto 8080).
