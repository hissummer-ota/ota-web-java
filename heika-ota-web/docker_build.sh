#!/usr/bin/env bash

IMAGENAME=heika-ota

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

docker build -t $IMAGENAME .


if [ $(docker images --filter "dangling=true" -q | wc -l) -gt 0 ]; then
    docker images --filter "dangling=true" -q | xargs docker rmi
fi