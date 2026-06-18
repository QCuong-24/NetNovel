FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    for i in 1 2 3; do \
      mvn -q -DskipTests dependency:go-offline && break; \
      if [ "$i" = "3" ]; then exit 1; fi; \
      sleep 10; \
    done

COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    for i in 1 2 3; do \
      mvn -q -DskipTests package && break; \
      if [ "$i" = "3" ]; then exit 1; fi; \
      sleep 10; \
    done

FROM mcr.microsoft.com/playwright/java:v1.52.0-noble

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
