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

docker run -it -d --name $INSTANCENAME -p 9008:8080 -p 9009:8443 -v /home/test/qa-app-tomcats/heika-ota-9001-9003/webapps/otadata:/usr/local/tomcat/webapps/otadata $IMAGENAME