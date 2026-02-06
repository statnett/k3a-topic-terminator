FROM maven:3-eclipse-temurin-25@sha256:e3d34f15aa3cb5323856d2dc9e1c0db5645dff5c038a36a12e38a82dd4e9f595 AS builder
WORKDIR /workspace
COPY pom.xml pom.xml
# Tests are run outside docker-build
RUN mvn dependency:resolve -DincludeScope=runtime
COPY src/main src/main
RUN mvn --batch-mode -Dmaven.test.skip=true package

FROM eclipse-temurin:25-jre-alpine@sha256:f10d6259d0798c1e12179b6bf3b63cea0d6843f7b09c9f9c9c422c50e44379ec
WORKDIR /app
COPY --from=builder /workspace/target/k3a-topic-terminator.jar ./
RUN apk update \
  && apk upgrade \
  && rm -rf /var/cache/apk/*

ENTRYPOINT ["java", "-jar", "k3a-topic-terminator.jar"]
