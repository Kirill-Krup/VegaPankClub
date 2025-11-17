FROM eclipse-temurin:21-jdk
WORKDIR /userService
COPY target/*.jar userService.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "userService.jar"]