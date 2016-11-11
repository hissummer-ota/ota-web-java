#!/usr/bin/env bash
cd `dirname $0`

WORKDIR=$(pwd)
WEBPATH=`cd $(pwd)/target/heika-ota-web*;pwd`

if [ -e $WORKDIR/webapps ]; then
    rm -rf $WORKDIR/webapps/*
else
    mkdir $WORKDIR/webapps
fi
cp -rf $WEBPATH/* $WORKDIR/webapps
sed -i 's#^TOMCAT_APP_BASE_DIR=.*#TOMCAT_APP_BASE_DIR=/usr/local/tomcat/webapps#' $WORKDIR/webapps/WEB-INF/classes/config-ota.properties

cd `dirname $0`

COUNT=`docker ps | grep ota-instance | wc -l`
if [ $COUNT = 1 ]; then
    docker stop ota-instance
fi

COUNT=`docker ps -a | grep ota-instance | wc -l`
if [ $COUNT = 1 ]; then
    docker rm ota-instance
fi


docker build -t ota .
docker run -it -d --name ota-instance -p 9008:8080 -p 9009:8443 -v /home/test/qa-app-tomcats/heika-ota-9001-9003/webapps/otadata:/usr/local/tomcat/webapps/otadata ota