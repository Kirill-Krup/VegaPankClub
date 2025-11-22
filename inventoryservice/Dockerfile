FROM eclipse-temurin:21
WORKDIR /app
COPY target/*inventoryservice*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
