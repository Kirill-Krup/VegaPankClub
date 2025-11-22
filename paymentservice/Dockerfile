FROM eclipse-temurin:21
WORKDIR /app
COPY target/*paymentservice*.jar app.jar
EXPOSE 8086
ENTRYPOINT ["java", "-jar", "app.jar"]
