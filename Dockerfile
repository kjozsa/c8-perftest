#FROM maven:3.8.6-openjdk-18-slim as builder
#WORKDIR /usr/src/c8-perftest
#COPY src/ src/
#COPY pom.xml pom.xml
#RUN mvn clean package -DskipTests -DskipChecks

FROM openjdk:17-bullseye
EXPOSE 8080
COPY ./target/*.jar app.jar
CMD java -jar app.jar
