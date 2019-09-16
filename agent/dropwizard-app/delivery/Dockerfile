FROM ubuntu:18.04

RUN apt-get -y update
RUN apt-get install -y openjdk-8-jdk

COPY target/delivery-1.0-SNAPSHOT.jar /app/delivery-1.0-SNAPSHOT.jar
COPY opentracing-specialagent-1.3.6.jar /app/opentracing-specialagent-1.3.6.jar
COPY wavefront-opentracing-bundle-java-0.9.0-SNAPSHOT.jar /app/wavefront-opentracing-bundle-java-SNAPSHOT.jar

COPY app.yaml /app/app.yaml
COPY tracer.properties /app/tracer.properties
COPY run.sh /app/run.sh

WORKDIR /app
ENTRYPOINT /bin/bash run.sh
