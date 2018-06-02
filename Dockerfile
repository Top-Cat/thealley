FROM openjdk:8-jdk-alpine

ARG BUILDVER=dev-SNAPSHOT

RUN mkdir -p /app
WORKDIR /app
COPY build/libs/thealley-${BUILDVER}.jar .

ENV BUILDVER=$BUILDVER

CMD ["sh", "-c", "java -jar thealley-${BUILDVER}.jar"]

EXPOSE 8080
