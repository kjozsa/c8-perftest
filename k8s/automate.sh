#!/bin/bash

#activeJobs: $1
#threads: $2
#sleepTime: $3
sed -i -E "11 s/[0-9]+/$1/g" configmap.yaml
sed -i -E "12 s/[0-9]+/$2/g" configmap.yaml
sed -i -E "13 s/[0-9]+/$3/g" configmap.yaml

#updates configmap, restarts deployment to apply changes
kubectl apply -f configmap.yaml
kubectl rollout restart deployment c8-perftest -n camunda-perf