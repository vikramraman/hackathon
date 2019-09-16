#!/bin/bash

if [[ -z "${DEMO_REGISTRY}" ]]; then
    echo "Env variable DEMO_REGISTRY must be set"
    exit 1 
fi

docker build -t $DEMO_REGISTRY/loadgen-agent .
docker push $DEMO_REGISTRY/loadgen-agent
