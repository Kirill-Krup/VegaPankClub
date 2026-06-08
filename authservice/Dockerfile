FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY common-lib/pom.xml ./common-lib/
COPY common-lib/src ./common-lib/src/
RUN mvn clean install -f authservice-lib/pom.xml -DskipTests

COPY authservice/pom.xml ./authservice/
COPY authservice/src ./authservice/src/
RUN mvn clean package -f authservice/pom.xml -DskipTests

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=builder /app/authservice/target/*.jar app.jar

EXPOSE 8087
ENTRYPOINT ["java", "-jar", "app.jar"]