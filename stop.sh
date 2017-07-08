#!/bin/sh
if [ -e ~/.nxt/elastic.pid ]; then
    PID=`cat ~/.elastic/elastic.pid`
    ps -p $PID > /dev/null
    STATUS=$?
    echo "stopping"
    while [ $STATUS -eq 0 ]; do
        kill `cat ~/.elastic/elastic.pid` > /dev/null
        sleep 5
        ps -p $PID > /dev/null
        STATUS=$?
    done
    rm -f ~/.elastic/elastic.pid
    echo "elastic server stopped"
fi

