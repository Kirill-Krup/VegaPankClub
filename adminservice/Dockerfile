FROM eclipse-temurin:21
WORKDIR /app
COPY target/*adminservice*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
