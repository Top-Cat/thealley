FROM openjdk:16-jdk-slim

ARG BUILDVER=dev-SNAPSHOT

RUN mkdir -p /app
WORKDIR /app
COPY build/install/thealley .

ENV BUILDVER=$BUILDVER

WORKDIR /app/bin
ENTRYPOINT ["./thealley"]

EXPOSE 8080
