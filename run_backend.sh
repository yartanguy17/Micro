#!/bin/sh
MAVEN_OPTS="-Xms512M -Xmx2048M -Xss2M -XX:MaxMetaspaceSize=2048M" mvn clean install -D checkstyle.skip=true -am -T 1C -DskipTests -DskipITs
target="bk-application/target/bk-application-`mvn help:evaluate -Dexpression=project.version -q -DforceStdout`.jar"
current_folder=`pwd`
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=GMT  -Dfile.encoding=UTF8 -Dspring.cloud.config.import-check.enabled=false  -Dconfig.server.properties.location=$current_folder/bk-config/.butterknife/conf.yaml -Dspring.profiles.active=butterknife,application -Duser.home=$current_folder -Djava.net.preferIPv4Stack=true -Xms1024M -Xmx1024M"
java $JAVA_OPTS -jar $target