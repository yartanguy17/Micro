#!/bin/sh
target="bk-config-server/target/bk-config-server-`mvn help:evaluate -Dexpression=project.version -q -DforceStdout`.jar"
current_folder=`pwd`
JAVA_OPTS="$JAVA_OPTS -Duser.timezone=GMT -Dfile.encoding=UTF8 -Dspring.config.location=$current_folder/bk-config/.butterknife/config-server.yaml -Duser.home=$current_folder/bk-config -Djava.net.preferIPv4Stack=true -Xms1024M -Xmx1024M"
java $JAVA_OPTS -jar $target