#!/usr/bin/env bash

INSTANCENAME=ota-instance
IMAGENAME=heika-ota

COUNT=`docker ps | grep $INSTANCENAME | wc -l`
if [ $COUNT = 1 ]; then
    docker stop $INSTANCENAME
fi

COUNT=`docker ps -a | grep $INSTANCENAME | wc -l`
if [ $COUNT = 1 ]; then
    docker rm $INSTANCENAME
fi

docker run -it -d --name $INSTANCENAME -p 9100:8080 -p 9101:8443 -v /root/.dockerdata/otadata:/usr/local/tomcat/webapps/otadata $IMAGENAME

if [ $(docker images --filter "dangling=true" -q | wc -l) -gt 0 ]; then
    docker images --filter "dangling=true" -q | xargs docker rmi
fi