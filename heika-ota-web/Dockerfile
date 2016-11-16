FROM tomcat:7-jre8

WORKDIR $CATALINA_HOME
RUN rm -rf $CATALINA_HOME/conf/server.xml && rm -rf $CATALINA_HOME/webapps/* 
ADD config/ota.keystore $CATALINA_HOME/conf/
ADD config/server.xml $CATALINA_HOME/conf/
ADD webapps/ $CATALINA_HOME/webapps/ROOT/ 

VOLUME $CATALINA_HOME/webapps/otadata

# set timezone
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

EXPOSE 8443 
EXPOSE 8080

CMD ["catalina.sh", "run"]