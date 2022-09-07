FROM maven:3.8.6-openjdk-18-slim as builder
WORKDIR /usr/src/c8-perftest
COPY src/ src/
COPY pom.xml pom.xml
RUN mvn clean package -DskipTests -DskipChecks

FROM openjdk:18-jdk-slim
COPY --from=builder usr/src/c8-perftest/target/*.jar app.jar
CMD java $JAVA_OPTIONS -jar app.jar
EXPOSE 8080
