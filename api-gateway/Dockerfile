# Etapa 1: construcción
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY eureka-server/pom.xml eureka-server/pom.xml
COPY investigadores-service/pom.xml investigadores-service/pom.xml
COPY publicaciones-service/pom.xml publicaciones-service/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
RUN chmod +x mvnw && ./mvnw -B -pl api-gateway -am dependency:go-offline

COPY api-gateway/src api-gateway/src
RUN ./mvnw -B -pl api-gateway -am package -DskipTests

# Etapa 2: imagen de ejecución
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/api-gateway/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
