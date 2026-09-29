# Etapa 1: construcción
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY investigadores-service/pom.xml investigadores-service/pom.xml
COPY publicaciones-service/pom.xml publicaciones-service/pom.xml
RUN chmod +x mvnw && ./mvnw -B -pl publicaciones-service -am dependency:go-offline

COPY publicaciones-service/src publicaciones-service/src
RUN ./mvnw -B -pl publicaciones-service -am package -DskipTests

# Etapa 2: imagen de ejecución
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/publicaciones-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
