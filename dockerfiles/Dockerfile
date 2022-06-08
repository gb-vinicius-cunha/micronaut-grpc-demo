FROM adoptopenjdk/openjdk11-openj9:jdk-11.28-alpine-slim

WORKDIR /app

COPY build/libs/demo-grpc-*-all.jar demo-grpc.jar

RUN GRPC_HEALTH_PROBE_VERSION=v0.4.11 && \
    wget -qO/bin/grpc_health_probe https://github.com/grpc-ecosystem/grpc-health-probe/releases/download/${GRPC_HEALTH_PROBE_VERSION}/grpc_health_probe-linux-amd64 && \
    chmod +x /bin/grpc_health_probe

EXPOSE 8080

CMD ["java", "-jar", "demo-grpc.jar"]