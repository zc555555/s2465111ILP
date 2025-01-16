FROM openjdk:17-jdk

EXPOSE 8080

WORKDIR /app

COPY ./target/PizzaDrone-0.0.1-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]