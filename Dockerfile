# ---- build (Gradle) ----
FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/app

# Copiar wrapper e configs primeiro para cache
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

# Copiar o código
COPY src ./src

# Construir o fat jar (Spring Boot)
RUN ./gradlew --no-daemon clean bootJar

# Renomear o jar gerado para um nome fixo
RUN ls -l build/libs && cp build/libs/*.jar app.jar

# ---- run (JRE) ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV PORT=8080
EXPOSE 8080

# Copiar o jar fixo
COPY --from=build /home/gradle/app/app.jar /app/app.jar

ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","/app/app.jar"]
