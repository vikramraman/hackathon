#!/bin/bash

echo ${DEMO_REGISTRY}

mvn clean install
docker build -t $DEMO_REGISTRY/shopping .
docker push $DEMO_REGISTRY/shopping
