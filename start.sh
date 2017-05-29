#!/bin/sh
if [ -e ~/.elastic/elastic.pid ]; then
    PID=`cat ~/.elastic/elastic.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    if [ $STATUS -eq 0 ]; then
        echo "Elastic server already running"
        exit 1
    fi
fi
mkdir -p ~/.elastic/
DIR=`dirname "$0"`
cd "${DIR}"
if [ -x jre/bin/java ]; then
    JAVA=./jre/bin/java
else
    JAVA=java
fi
echo "About to start elastic in the background, call stop.sh to stop it again"
nohup ${JAVA} -cp classes:lib/*:conf:addons/classes:addons/lib/* -Dnxt.runtime.mode=desktop nxt.Nxt > /dev/null 2>&1 &

echo $! > ~/.elastic/elastic.pid
cd - > /dev/null
