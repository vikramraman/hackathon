FROM ubuntu:18.04

RUN apt-get -y update
RUN apt-get install -y openjdk-8-jdk

COPY target/shopping-1.0-SNAPSHOT.jar /app/shopping-1.0-SNAPSHOT.jar

COPY app.yaml /app/app.yaml
COPY app-tags.yml /app/app-tags.yml
COPY wf-reporting-config.yml /app/wf-reporting-config.yml
COPY run.sh /app/run.sh

WORKDIR /app
ENTRYPOINT /bin/bash run.sh
