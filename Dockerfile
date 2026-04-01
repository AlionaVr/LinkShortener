FROM gradle:8.7-jdk17 AS builder
WORKDIR /app

COPY build.gradle settings.gradle gradlew ./
COPY gradle ./gradle
RUN ./gradlew dependencies --no-daemon || true

COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app

COPY --from=builder /app/build/libs/*.jar app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]