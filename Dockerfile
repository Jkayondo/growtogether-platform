FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src src
RUN ./mvnw -B -ntp clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S gt && adduser -S gt -G gt
WORKDIR /app
COPY --from=build /workspace/target/gt-platform-*.jar app.jar
USER gt:gt
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]
