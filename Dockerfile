FROM eclipse-temurin:22-jdk-alpine AS builder

WORKDIR /build
COPY .mvn .mvn
COPY mvnw pom.xml ./

RUN chmod +x mvnw

RUN ./mvnw dependency:go-offline

COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:22-jre-alpine

WORKDIR /app

RUN addgroup -S accounting && adduser -S accounting -G accounting

COPY --from=builder /build/target/*.jar app.jar

RUN chown accounting:accounting app.jar

USER accounting:accounting

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "app.jar"]