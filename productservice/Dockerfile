FROM eclipse-temurin:21
WORKDIR /app
COPY target/*productservice*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
