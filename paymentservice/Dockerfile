FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY common-lib/pom.xml ./common-lib/
COPY common-lib/src ./common-lib/src/
RUN mvn clean install -f common-lib/pom.xml -DskipTests

COPY paymentservice/pom.xml ./paymentservice/
COPY paymentservice/src ./paymentservice/src/
RUN mvn clean package -f paymentservice/pom.xml -DskipTests

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=builder /app/paymentservice/target/*.jar app.jar

EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]