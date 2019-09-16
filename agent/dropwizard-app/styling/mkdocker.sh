#!/bin/bash

echo ${DEMO_REGISTRY}

mvn clean install
docker build -t $DEMO_REGISTRY/styling-agent .
docker push $DEMO_REGISTRY/styling-agent
