FROM java:8-jre-alpine

ARG BUILDVER=dev-SNAPSHOT

RUN mkdir -p /app
WORKDIR /app
COPY build/libs/thealley-${BUILDVER}.jar .

ENV BUILDVER=$BUILDVER

ENTRYPOINT ["java -jar ${JAVA_ARGS} /app/thealley-${BUILDVER}.jar"]

EXPOSE 8080
