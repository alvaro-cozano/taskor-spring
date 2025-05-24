FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace/app

COPY pom.xml .
COPY .mvn/ .mvn/
COPY mvnw .
COPY src/ src/

RUN ./mvnw package -DskipTests

FROM openjdk:17-jdk-slim
WORKDIR /app

COPY --from=build /workspace/app/target/project-management-*.jar app.jar

EXPOSE 8080

ARG JAVA_OPTS
ENV JAVA_OPTS=$JAVA_OPTS

CMD ["sh", "-c", "exec java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app/app.jar"]
