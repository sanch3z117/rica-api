# ADR-0001: Organizar RICA con puertos y adaptadores (arquitectura hexagonal)

- **Fecha:** 29 de septiembre de 2026
- **Estado:** aceptada
- **Contexto relacionado:** Lecciones 3 (DDD) y 4 (hexagonal)

## Contexto

Tras el taller de DDD, `rica-api` ya tenía paquetes por Bounded Context
(`investigadores`, `publicaciones`, `compartido`, `plataforma`), pero cada paquete era un
bloque plano: el controlador construía la entidad, el servicio dependía directamente de
`JpaRepository`/`MongoRepository`, y las reglas de negocio solo se podían ejercitar
levantando Spring y una base de datos.

## Decisión

Reorganizar cada contexto en tres capas —`dominio`, `aplicacion` e `infraestructura`— con:

- **Puertos primarios** (`InvestigadorUseCase`, `PublicacionUseCase`): el contrato que los
  adaptadores de entrada usan para activar los casos de uso.
- **Puertos secundarios mínimos** (`RepositorioInvestigadores`, `RepositorioPublicaciones`,
  `ConsultaInvestigadores`): solo los métodos que el núcleo realmente necesita, no los que
  regala `JpaRepository`.
- **Adaptadores** que dependen del núcleo y nunca al revés: controladores y DTOs en
  `infraestructura/entrada/web`, persistencia en `infraestructura/salida/persistencia` y la
  consulta de investigadores en `infraestructura/salida/investigadores`.

## Consecuencias

**Positivas**

- Los casos de uso y el dominio se prueban en milisegundos, sin Spring ni base de datos.
- Cambiar de motor de persistencia o de cliente HTTP toca un solo adaptador: el puerto se
  diseñó desde el caso de uso, no desde JPA.
- El controlador dejó de construir entidades: el Agregado lo arma su Factory.

**Negativas**

- Más interfaces y más indirección: cada operación atraviesa puerto, servicio y adaptador.
- Se conservan las anotaciones `@Service`/`@Component` en la capa de aplicación como fuga
  controlada de Spring (la versión estricta del patrón las evitaría).
- `Investigador` y `Publicacion` siguen siendo, a la vez, modelo de dominio y modelo de
  persistencia; separarlos del todo queda como deuda para cuando haga falta.
