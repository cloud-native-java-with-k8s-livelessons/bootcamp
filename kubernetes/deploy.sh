#!/usr/bin/env bash

echo "deploying the application to Kubernetes"
cd $(dirname $0 ) 
HERE=$( pwd )

NS=cnj
APP_NAME=customers
MANIFESTS_DIR=$HERE/k8s/manifests
IMAGE_NAME=gcr.io/bootiful/cnj-basics:latest 
## ^ notice that we tag the image as 'latest', which Kubernetes will automatically redeploy everytime

## docker 
./mvnw -DskipTests=true clean package spring-boot:build-image -Dspring-boot.build-image.imageName=${IMAGE_NAME}
# docker run -e SPRING_PROFILES_ACTIVE=cloud -e SERVER_PORT=8082 -p 8082:8082 $IMAGE_NAME
docker push $IMAGE_NAME

## kubernetes
mkdir -p $MANIFESTS_DIR 
kubectl create ns $NS  -o yaml > $MANIFESTS_DIR/namespace.yaml  
kubectl -n $NS create deployment  --image=$IMAGE_NAME $APP_NAME -o yaml > $MANIFESTS_DIR/deployment.yaml
kubectl -n $NS expose deployment $APP_NAME --port=8080 -o yaml > $MANIFESTS_DIR/service.yaml
sleep 5
kubectl -n $NS port-forward deployment/$APP_NAME 8080:8080  &

curl -s localhost:8080/actuator/health/liveness | jq

kubectl logs -n cnj deployments/$APP_NAME

# 1. test readiness / liveness actuators

#     ```bash
#     curl -s localhost:8080/actuator/health | jq
#     curl -s localhost:8080/actuator/health/readiness | jq
#     curl -s localhost:8080/actuator/health/liveness | jq
#     ```

# 1. configure readiness / liveness probes (edit k8s/manifests/deployment.yaml)

#     ```yaml
#     spec:
#       terminationGracePeriodSeconds: 30
#     containers:
#     ...
#         env:
#           - name: MANAGEMENT_SERVER_PORT
#             value: "9001"
#         livenessProbe:
#           httpGet:
#             path: /actuator/health/liveness
#             port: 9001
#         readinessProbe:
#           httpGet:
#             path: /actuator/health/readiness
#             port: 9001
#     ```

# 1. apply the new changes

#     ```bash
#     kubectl -n booternetes apply -f k8s/manifests/deployment.yaml
#     ```


# 1. Deploy a ingress ... WOW MAGIC DNS / TLS

#     ```bash
#      kubectl -n booternetes create ingress customer --class=default \
#       --rule="crm.s1t.k8s.camp/*=customer:8080,tls=crm-secret" \
#       --annotation="cert-manager.io/cluster-issuer=letsencrypt-prod" \
#       -o yaml > k8s/manifests/ingress.yaml
#     ```

# ### Deploy with a database

# 1. Configure to use a database by reading the cloud properties into a secret:

#     ```bash
#     kubectl -n booternetes create secret generic customers --from-file ./src/main/resources/application-cloud.properties
#     ```

# 1. update kube deployment to mount secret as files...

#     ```bash
#         spec:
#           containers:
#           ...
#             env:
#               - name: SPRING_PROFILES_ACTIVE
#                 value: cloud
#             volumeMounts:
#               - mountPath: "/config"
#                 name: config
#                 readOnly: true
#           volumes:
#             - name: config
#               secret:
#                 secretName: customers
#     ```


# ## Scripts

# I've put `run.sh` and `build.sh` in the `customers` module. Invoke `build.sh` and then `run.sh`.