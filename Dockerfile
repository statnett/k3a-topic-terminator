FROM maven:3-eclipse-temurin-25@sha256:313ecd20dc008fec427ca02430916966fe4eb34ac5775d83808ae7928be6e8aa AS builder
WORKDIR /workspace
COPY pom.xml pom.xml
# Tests are run outside docker-build
RUN mvn dependency:resolve -DincludeScope=runtime
COPY src/main src/main
RUN mvn --batch-mode -Dmaven.test.skip=true package

FROM eclipse-temurin:25.0.4_7-jre-alpine@sha256:2ca9adf44f5c29d28ecd26cf92d75cc0c66b7f32bfd839a4439e363a8b428af8
WORKDIR /app
COPY --from=builder /workspace/target/k3a-topic-terminator.jar ./
RUN apk update \
  && apk upgrade \
  && rm -rf /var/cache/apk/*

ENTRYPOINT ["java", "-jar", "k3a-topic-terminator.jar"]
