FROM eclipse-temurin:21
WORKDIR /app
COPY target/*billingservice*.jar app.jar
EXPOSE 8084
ENTRYPOINT ["java", "-jar", "app.jar"]
