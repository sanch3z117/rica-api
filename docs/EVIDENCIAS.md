# Evidencias de talleres (40 puntos manuales)

Los 60 puntos automáticos los calcula `VerificarTalleres.java`. Los **40 puntos
restantes (MT1–MT4)** se suben a Moodle como evidencia. Esta guía produce esa
evidencia sobre el commit de `main`.

## Prerrequisitos

- Docker y Java 25.
- Desde la raíz del repositorio (`rica-api`).

## Arranque del stack

```powershell
docker compose up -d --build
./mvnw -pl consultas-service spring-boot:run
```

Servicios y puertos:

| Servicio | URL |
| --- | --- |
| API Gateway | http://localhost:8080 |
| Eureka | http://localhost:8761 |
| investigadores-service | http://localhost:8081 |
| publicaciones-service | http://localhost:8082 |
| consultas-service | http://localhost:8083 |
| gRPC investigadores | localhost:9090 |

---

## MT1 — Evidencias de talleres 5 y 6 (10 pts)

Captura: **los servicios corriendo** y **los logs de la comunicación**.

1. Registra un investigador (dispara el evento de dominio):

   ```powershell
   curl.exe -X POST http://localhost:8081/api/investigadores `
     -H "Content-Type: application/json" -H "X-Correlation-Id: prueba-mt1" `
     -d '{\"nombreCompleto\":\"Ana Torres\",\"correoInstitucional\":\"ana.torres@uptc.edu.co\",\"grupoInvestigacion\":\"GIT-UPTC\"}'
   ```

2. Comunicación REST síncrona (taller 6):

   ```powershell
   curl.exe "http://localhost:8081/api/investigadores/existe?correo=ana.torres@uptc.edu.co"
   ```

3. Comunicación gRPC (taller 6):

   ```powershell
   docker run --rm fullstorydev/grpcurl -plaintext host.docker.internal:9090 list
   docker run --rm -d '{"id":1}' fullstorydev/grpcurl -plaintext -d '@' host.docker.internal:9090 InvestigadorGrpc/ExisteInvestigador
   ```

4. Mensajería con Kafka (taller 6): en el log de `publicaciones-service` debe
   aparecer `InvestigadorRegistrado: ... [correlationId=prueba-mt1]`.

Capturas sugeridas: consola de los 3 servicios arrancados, respuesta del GET
`/existe`, salida de `grpcurl list`, y el log del listener.

---

## MT2 — Evidencias de talleres 8 y 9 (10 pts)

1. **JSON de la ficha** (taller 8) y **log de la proyección**:

   ```powershell
   curl.exe http://localhost:8083/api/consultas/fichas
   ```

   En el log de `consultas-service`: `Ficha proyectada para el investigador 1`.

2. **`/estadisticas` antes y después**:

   ```powershell
   curl.exe http://localhost:8083/api/consultas/estadisticas   # antes
   # registra otra publicación/investigador
   curl.exe http://localhost:8083/api/consultas/estadisticas   # después
   ```

3. **Salida de la Dead Letter Topic** (taller 9): provoca un payload inválido
   para que el proyector falle y, tras los reintentos, el mensaje vaya a
   `investigadores-eventos.DLT`:

   ```powershell
   docker exec -i kafka /opt/kafka/bin/kafka-console-producer.sh `
     --bootstrap-server localhost:19092 --topic investigadores-eventos
   # escribe una línea con JSON roto, por ejemplo:  {esto-no-es-json}
   ```

   ```powershell
   docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh `
     --bootstrap-server localhost:19092 --topic investigadores-eventos.DLT --from-beginning
   ```

Capturas: JSON de `/fichas`, log de proyección, `/estadisticas` antes/después y
la salida del consumidor de la DLT.

---

## MT3 — Respuestas escritas propias (10 pts)

### Explicación del taller 8 (CQRS)

> En el taller 8 separé el modelo de escritura del de lectura. `investigadores-service`
> y `publicaciones-service` siguen siendo la fuente de verdad y escriben en
> PostgreSQL y MongoDB. Un nuevo `consultas-service` consume el tópico
> `investigadores-eventos` y proyecta una `FichaInvestigador` desnormalizada en
> MongoDB. Así la consulta de la ficha no depende de llamar a dos servicios en
> cada petición. La proyección es idempotente: `findById` + `save` deja la ficha
> igual si el evento llega dos veces, algo necesario porque Kafka garantiza
> “al menos una vez”. El costo es consistencia eventual: la ficha puede tardar
> unos segundos en reflejar el registro.

### Discusión del taller 9 (EDA)

> En el taller 9 reforcé el consumidor de `consultas-service` para que tolere
> fallos. Configuré un `DefaultErrorHandler` con `FixedBackOff(1000, 2)` y un
> `DeadLetterPublishingRecoverer` que envía a `investigadores-eventos.DLT` los
> mensajes que agotan los reintentos; un `@DltHandler` registra el mensaje
> muerto. La idempotencia evita duplicar fichas. La decisión de negocio es que
> un evento inválido no bloquee la partición: después de pocos reintentos sale
> del flujo principal y queda en la DLT para revisión manual, en vez de
> reintentarse indefinidamente.

> Ajusta estas respuestas con los valores reales de tu ejecución (ids, conteos,
> timestamps) antes de subirlas.

---

## MT4 — Historial de Git (10 pts)

```powershell
git log --oneline --author="sanch3z117"
```

Debe verse al menos un commit por taller, con mensaje que nombre el paso y en
orden cronológico. Los commits de este repositorio ya siguen ese criterio
(`feat(publicaciones)`, `feat(consultas): ...`, `feat(eda): ...`, `ci: ...`).

---

## Entrega en Moodle

- `entrega-talleres.txt` con repositorio, hash del corte y ramas por taller.
- Informe `.md` de autoevaluación con su **huella** y puntaje automático.
