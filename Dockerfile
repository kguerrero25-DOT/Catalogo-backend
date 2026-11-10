FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn -B -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:21.0.13_9-jdk-alpine-3.21 AS runtime
RUN jlink --add-modules java.base,java.compiler,java.desktop,java.instrument,java.net.http,java.prefs,java.rmi,java.scripting,java.security.jgss,java.sql.rowset,jdk.crypto.ec,jdk.jfr,jdk.management,jdk.unsupported \
    --strip-debug --no-man-pages --no-header-files --compress=2 \
    --output /opt/java-runtime

FROM alpine:3.21
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
ENV JAVA_HOME=/opt/java-runtime
COPY --from=runtime /opt/java-runtime /opt/java-runtime
COPY --from=runtime /etc/ssl/certs/ca-certificates.crt /etc/ssl/certs/ca-certificates.crt
COPY --from=build --chown=spring:spring /workspace/target/catalogo-0.0.1-SNAPSHOT.jar /app/catalogo.jar
USER spring:spring
EXPOSE 8080
ENTRYPOINT ["/opt/java-runtime/bin/java", "-XX:MaxRAMPercentage=75.0", "-jar", "/app/catalogo.jar"]
