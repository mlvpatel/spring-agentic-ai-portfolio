# Build context: repo root. Example:
#   docker build -f infra/docker/Dockerfile.app --build-arg JAR_FILE=apps/yagni-copilot/target/yagni-copilot-1.0.0-SNAPSHOT.jar -t portfolio/yagni-copilot:local .
ARG JAR_FILE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
ARG JAR_FILE
COPY ${JAR_FILE} /app/app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
