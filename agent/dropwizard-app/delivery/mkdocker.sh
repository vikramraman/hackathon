#!/bin/bash

echo ${DEMO_REGISTRY}

mvn clean install
docker build -t $DEMO_REGISTRY/delivery-agent:latest .
docker push $DEMO_REGISTRY/delivery-agent:latest
