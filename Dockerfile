FROM debian:trixie-slim

ARG BUILDVER=dev-SNAPSHOT

RUN apt update && \
    apt install -y --no-install-recommends \
        openjdk-21-jdk-headless curl && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app
WORKDIR /app
COPY build/install/thealley-jvm .

ENV BUILDVER=$BUILDVER

WORKDIR /app/bin
ENTRYPOINT ["./thealley"]

EXPOSE 8080
