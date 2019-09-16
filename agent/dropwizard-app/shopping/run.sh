#!/bin/bash
java -javaagent:/app/opentracing-specialagent-1.3.6.jar \
    -Dsa.tracer=/app/wavefront-opentracing-bundle-java-SNAPSHOT.jar \
    -jar /app/shopping-1.0-SNAPSHOT.jar server /app/app.yaml
