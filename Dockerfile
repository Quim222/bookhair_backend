# ---- build (Gradle) ----
FROM gradle:8.7-jdk21 AS build
WORKDIR /home/gradle/app

# Copia wrapper e configs primeiro para cache
COPY gradle gradle
COPY gradlew build.gradle settings.gradle ./
RUN chmod +x gradlew

# Copia o código
COPY src ./src

# Gera o fat jar (Spring Boot)
RUN ./gradlew --no-daemon clean bootJar

# ---- run (JRE) ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV PORT=8080
EXPOSE 8080

# Copia o jar gerado (qualquer nome)
# 1ª linha tenta jar com sufixo comum; a 2ª é fallback
COPY --from=build /home/gradle/app/build/libs/*-SNAPSHOT*.jar /app/app.jar 2>/dev/null || true
COPY --from=build /home/gradle/app/build/libs/*.jar            /app/app.jar

ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","/app/app.jar"]
