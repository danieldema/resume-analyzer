FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app
COPY apps/api/.mvn/ .mvn/
COPY apps/api/mvnw apps/api/pom.xml ./
RUN ./mvnw dependency:go-offline
COPY apps/api/src/ ./src/
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
RUN apt-get update && \
    apt-get install -y --no-install-recommends libreoffice-writer && \
    apt-get clean && rm -rf /var/lib/apt/lists/*
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
