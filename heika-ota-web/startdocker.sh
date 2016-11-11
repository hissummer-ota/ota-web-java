#!/usr/bin/env bash
cd `dirname $0`
docker build -t ota -f Dockerfile
docker run -it -v $(pwd)/target/heika-ota-web*:/usr/local/tomcat/webapps/otadata  ota /bin/bash
