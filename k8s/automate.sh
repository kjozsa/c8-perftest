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

#waits for the deployment to restart
sleep 50s

#starts forwarding port
kubectl port-forward deployment/c8-perftest 8080:8080 -n camunda-perf &

sleep 5s


#calculates limit and begins to start instances $4: base, $5: count, $6: difference
#url="localhost:8080/start/"
#limit=$(($4 + $5 * $6))

#for ((i=$4;i<$limit;i+=$6)) 
#do
#    updatedUrl=${url}$i
#    until $(curl ${updatedUrl}); do
#        sleep 10
#    done
#done


# part for static testing: starts the fourth command line amount of instances 3 times with a 10 ms difference
url="localhost:8080/start/"

for ((i=0;i<3;i++)) 
do
    updatedUrl=${url}$4
    until $(curl ${updatedUrl}); do
        sleep 10
    done
    sleep 10
done

pkill kubectl