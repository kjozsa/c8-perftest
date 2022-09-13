#!/bin/bash

activeJobs=$1
threads=$2
sleepTime=$3
sed -i "11 s/[0-9]+/$1/g" configmap.yaml
sed -i "12 s/[0-9]+/$2/g" configmap.yaml
sed -i "13 s/[0-9]+/$3/g" configmap.yaml