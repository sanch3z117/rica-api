# Diagramas C4 — RICA

Diagramas de **Contexto** (nivel 1) y **Contenedores** (nivel 2) del proyecto, en el
estado que quedó tras la Lección 5. Se dibujan con Mermaid para que vivan en el mismo
repositorio que el código.

## Contexto (nivel 1)

```mermaid
flowchart LR
    coordinador["Coordinador de investigación<br/>[persona]"]
    admin["Facultad / administración<br/>[persona]"]

    rica["RICA<br/>Red Inteligente de Investigación<br/>y Colaboración Académica<br/>[sistema]"]
    minciencias["Sistema de reportes<br/>de MinCiencias<br/>[sistema externo]"]

    coordinador -->|"registra investigadores<br/>y consulta publicaciones<br/>[HTTPS]"| rica
    admin -->|"consulta ocupación<br/>y reportes [HTTPS]"| rica
    rica -->|"reporta producción académica<br/>[archivo / HTTPS]"| minciencias
```

## Contenedores (nivel 2) — estado tras la Lección 4

Un solo proceso desplegable (monolito modular hexagonal): los dos Bounded Contexts viven en
el mismo artefacto y se llaman en memoria.

```mermaid
flowchart TB
    usuario["Usuario<br/>[persona]"]

    subgraph proceso["rica-api [proceso único]"]
        web["Adaptadores web<br/>[Spring MVC]"]
        nucleo["Núcleo<br/>investigadores · publicaciones<br/>[Java]"]
        jpa["Adaptador JPA"]
        mongo["Adaptador Mongo"]
    end

    pg[("PostgreSQL<br/>[base de datos]")]
    mo[("MongoDB<br/>[base de datos]")]

    usuario -->|"consume /api/**<br/>[HTTPS/JSON]"| web
    web --> nucleo
    nucleo -->|"puerto secundario<br/>RepositorioInvestigadores"| jpa
    nucleo -->|"puerto secundario<br/>RepositorioPublicaciones"| mongo
    jpa --> pg
    mongo --> mo
```

## Contenedores (nivel 2) — estado tras la Lección 5

Dos procesos independientes, cada uno con su base de datos, y una fachada que conserva el
punto de entrada.

```mermaid
flowchart TB
    usuario["Usuario<br/>[persona]"]
    frontend["rica-frontend<br/>[SPA React + nginx]"]

    gateway["api-gateway<br/>[nginx]"]

    subgraph inv["investigadores-service [Spring Boot]"]
        invWeb["Adaptador web"]
        invNucleo["Núcleo<br/>Investigador · CorreoInstitucional<br/>InvestigadorFactory"]
        invJpa["Adaptador JPA"]
    end

    subgraph pub["publicaciones-service [Spring Boot]"]
        pubWeb["Adaptador web"]
        pubNucleo["Núcleo<br/>Publicacion · LimitePublicacionesAnualesService"]
        pubMongo["Adaptador Mongo"]
        pubHttp["Adaptador HTTP<br/>ConsultaInvestigadores"]
    end

    pg[("PostgreSQL<br/>[investigadores]")]
    mo[("MongoDB<br/>[publicaciones]")]

    usuario --> frontend
    frontend -->|"/api/** [HTTPS/JSON]"| gateway
    gateway -->|"/api/investigadores/**"| invWeb
    gateway -->|"resto de /api/**"| pubWeb
    invWeb --> invNucleo
    invNucleo --> invJpa --> pg
    pubWeb --> pubNucleo
    pubNucleo --> pubMongo --> mo
    pubNucleo -->|"puerto ConsultaInvestigadores<br/>[HTTP/JSON]"| pubHttp
    pubHttp -->|"/api/investigadores/existe?correo=...<br/>[HTTP]"| invWeb
```
