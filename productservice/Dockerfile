FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY common-lib/pom.xml ./common-lib/
COPY common-lib/src ./common-lib/src/
RUN mvn clean install -f common-lib/pom.xml -DskipTests

COPY productservice/pom.xml ./productservice/
COPY productservice/src ./productservice/src/
RUN mvn clean package -f productservice/pom.xml -DskipTests

FROM eclipse-temurin:21
WORKDIR /app
COPY --from=builder /app/productservice/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]