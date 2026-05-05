FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /build
COPY RoomAppoint.springboot/pom.xml ./pom.xml
COPY RoomAppoint.springboot/src ./src
ENV MAVEN_OPTS="-Xmx256m -Xms128m"
RUN mvn -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /build/target/web-0.0.1-SNAPSHOT-exec.jar /app/app.jar
EXPOSE 7245
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
