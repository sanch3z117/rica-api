# Modelo de dominio de RICA

Documento del taller de DDD (Lección 3) aplicado sobre `rica-api`: lenguaje ubicuo,
límite de los Agregados y separación entre el modelo de dominio y el modelo de persistencia.
Se complementa con el código de los paquetes `investigadores` y `publicaciones`.

> Tras la Lección 5, cada paquete vive en su propio servicio: `investigadores-service` y
> `publicaciones-service`. Las rutas de este documento se leen dentro del módulo
> correspondiente.

## 1. Lenguaje Ubicuo

| Término | Significado en RICA | Dónde vive en el código |
| --- | --- | --- |
| **Investigador** | Persona vinculada a la universidad que produce publicaciones. Su identidad no está en ningún atributo visible: la asigna el sistema. | `investigadores/dominio/Investigador` |
| **Correo institucional** | Medio de contacto del investigador. No cualquier correo sirve: debe pertenecer al dominio de la universidad. | `investigadores/dominio/CorreoInstitucional` |
| **Grupo de investigación** | Colectivo al que está vinculado el investigador. | `Investigador.grupoInvestigacion` |
| **Publicación** | Resultado académico (artículo, libro…) de un investigador, con año, tipo y detalles propios de cada tipo. | `publicaciones/dominio/Publicacion` |
| **Límite anual de publicaciones** | Regla del negocio: un investigador no puede registrar más de cinco publicaciones en un mismo año. | `publicaciones/dominio/LimitePublicacionesAnualesService` |

Los términos del negocio se quedan en español (`Investigador`, `Publicacion`,
`correoInstitucional`) porque son las palabras que usaría un coordinador de investigación;
la sintaxis, los paquetes técnicos y los nombres de infraestructura siguen la convención
habitual en inglés (`Repository`, `Controller`, `Factory`), porque esa parte del código no
pertenece al Lenguaje Ubicuo.

La auditoría de nombres (paso 1 del taller) no encontró términos traducidos ni sinónimos
sueltos: `Investigador`, `Publicacion` y `correoInstitucional` ya se llamaban como los llama
el negocio. Lo que sí faltaba era que `correoInstitucional` fuera un tipo con reglas propias
en vez de un `String` suelto (paso 2), y que el Agregado se construyera siempre por una
Factory (paso 5) en vez de repartir esa responsabilidad entre los clientes.

## 2. Límite de los Agregados

### 2.1 `Investigador` — raíz del Agregado (contexto `investigadores`)

| Pregunta | Respuesta |
| --- | --- |
| ¿Cuál es la raíz del Agregado? | `Investigador`. Es el único punto de entrada del contexto: hoy es también la única clase con `@Entity` en el paquete. |
| ¿Qué vive dentro del límite? | El `id` generado por el sistema, el nombre completo, el `CorreoInstitucional` (Value Object, inmutable) y el grupo de investigación. |
| ¿Por qué `Publicacion` (contexto `publicaciones`) NO está dentro del límite? | Porque pertenece a otro Bounded Context, con otro ciclo de vida y otro motor de base de datos. `Investigador` no necesita conocer las publicaciones para cumplir sus reglas; cuando algo las necesita juntas, la relación se resuelve por el correo institucional, nunca por el objeto completo. |
| ¿Qué pasaría si alguien agrega un campo `List<Publicacion>` dentro de `Investigador`? | Se rompería el límite: registrar una publicación obligaría a cargar y modificar el Agregado `Investigador` en la misma transacción, y los dos contextos dejarían de poder evolucionar (y desplegarse) por separado. Es exactamente el acoplamiento que impide separar `publicaciones` como servicio. |

### 2.2 `Publicacion` — raíz del Agregado (contexto `publicaciones`)

| Pregunta | Respuesta |
| --- | --- |
| ¿Cuál es la raíz del Agregado? | `Publicacion`, persistida como documento propio en MongoDB. |
| ¿Qué vive dentro del límite? | El `id`, el correo del investigador autor (referencia por identidad, no por objeto), el título, el tipo, el año y un mapa de detalles propios de cada tipo de publicación. |
| ¿Por qué `Investigador` NO está dentro del límite? | Porque vive en otro contexto; la publicación solo necesita *de quién* es, y eso se guarda como el correo institucional. Verificarlo es una colaboración entre contextos, no una composición de objetos. |
| ¿Qué pasaría si alguien agrega un campo `Investigador investigador` directo en `Publicacion`? | Un cambio en el perfil del investigador arrastraría transacciones sobre publicaciones; además, al no compartir base de datos, el objeto no podría reconstruirse completo sin una consulta extra. La relación debe quedarse en el correo. |

## 3. Modelo de dominio vs. modelo de persistencia

- `CorreoInstitucional` es un Value Object del dominio **y** se mapea con `@Embeddable`: en la
  tabla sigue siendo una sola columna (`correo_institucional`), pero en el código es un tipo
  que se protege a sí mismo — es imposible persistir un correo fuera del dominio institucional.
- `Investigador` y `Publicacion` son, hoy, a la vez modelo de dominio y modelo de persistencia
  (`@Entity` / `@Document`). Es una coincidencia cómoda, no una garantía: si en el futuro la
  forma de guardar se separa de la forma de pensar el negocio, el adaptador de persistencia es
  el único lugar que debe cambiar.
- Los repositorios se declaran **por Agregado** (`RepositorioInvestigadores`,
  `RepositorioPublicaciones`), nunca por tabla ni por entidad interna, y viven en la capa de
  aplicación como puertos secundarios: el adaptador de persistencia (JPA o Mongo) es un
  detalle intercambiable.

## 4. Qué se implementó en este taller

| Paso | Pieza | Dónde |
| --- | --- | --- |
| 1 · Lenguaje Ubicuo | Auditoría de nombres | Este documento, sección 1 |
| 2 · Value Object | `CorreoInstitucional` | `investigadores/dominio/CorreoInstitucional` (+ `CorreoInstitucionalTest`) |
| 3 · Servicio de Dominio | `LimitePublicacionesAnualesService` | `publicaciones/dominio/LimitePublicacionesAnualesService` (+ test) |
| 4 · Límite del Agregado | Mini-ADR de `Investigador` y `Publicacion` | Este documento, sección 2 |
| 5 · Factory | `InvestigadorFactory` | `investigadores/aplicacion/InvestigadorFactory` (+ test) |

## 5. Saga del registro de publicaciones (Lección 6)

Registrar una publicación es ahora una **saga orquestada** (`RegistrarPublicacionSaga`) que
combina una llamada remota protegida con resiliencia y las reglas locales del dominio:

| Paso | Acción | Compensación si algo falla después |
| --- | --- | --- |
| 1 | Crear la publicación en estado `PENDIENTE` | — (no hay ningún paso previo que deshacer) |
| 2 | Confirmar el investigador (REST protegido con Circuit Breaker + Retry) | Marcar `RECHAZADA` |
| 3 | Validar el límite anual con las publicaciones `REGISTRADA` | Marcar `RECHAZADA` |
| 4 | Confirmar la publicación como `REGISTRADA` | Marcar `RECHAZADA` |

El listado por investigador solo devuelve publicaciones `REGISTRADA`; las `RECHAZADA` quedan
en la base como evidencia de la compensación. Los tres estados viven en el dominio
(`EstadoPublicacion`) porque son parte del Lenguaje Ubicuo del flujo.
