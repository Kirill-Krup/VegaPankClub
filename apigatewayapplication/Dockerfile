FROM eclipse-temurin:21
WORKDIR /app
COPY target/*apigateway*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
