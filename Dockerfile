FROM eclipse-temurin:26-jdk-alpine AS build

WORKDIR /app

COPY . .


RUN chmod +x ./gradlew && ./gradlew assemble --no-daemon

FROM eclipse-temurin:26-jre-alpine

WORKDIR /app

COPY --from=build /app/**/build/libs/*.jar app.jar

CMD ["java", "-jar", "app.jar"]