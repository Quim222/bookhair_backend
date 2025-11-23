# ---- build ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

# ---- run ----
FROM eclipse-temurin:21-jre
WORKDIR /app
ENV PORT=8080
EXPOSE 8080
# copia o jar gerado (ajusta se o nome do teu jar não cair neste glob)
COPY --from=build /app/target/*.jar /app/app.jar
ENTRYPOINT ["java","-Dserver.port=${PORT}","-jar","/app/app.jar"]
