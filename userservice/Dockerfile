FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY common-lib/pom.xml ./common-lib/
COPY common-lib/src ./common-lib/src/
RUN mvn clean install -f common-lib/pom.xml -DskipTests

COPY userservice/pom.xml ./userservice/
COPY userservice/src ./userservice/src/
RUN mvn clean package -f userservice/pom.xml -DskipTests

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=builder /app/userservice/target/*.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]