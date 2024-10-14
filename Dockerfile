FROM maven:3-eclipse-temurin-21 AS builder
WORKDIR /workspace
COPY pom.xml pom.xml
# Tests are run outside docker-build
RUN mvn dependency:resolve -DincludeScope=runtime
COPY src/main src/main
RUN mvn --batch-mode -Dmaven.test.skip=true package

FROM eclipse-temurin:21.0.4_7-jre-alpine
WORKDIR /app
COPY --from=builder /workspace/target/k3a-topic-terminator.jar ./
RUN apk update \
  && apk upgrade \
  && rm -rf /var/cache/apk/*

ENTRYPOINT ["java", "-jar", "k3a-topic-terminator.jar"]