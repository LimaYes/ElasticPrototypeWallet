#!/bin/sh
CP="lib/*;classes"
SP=src/

/bin/rm -f elastic.jar
/bin/rm -f elasticservice.jar
/bin/rm -rf classes
/bin/mkdir -p classes/
/bin/rm -rf addons/classes
/bin/mkdir -p addons/classes/

javac -encoding utf8 -sourcepath "${SP}" -classpath "${CP}" -d classes/ src/nxt/*.java src/nxt/*/*.java src/nxt/*/*/*.java src/nxtdesktop/*.java || exit 1

echo "nxt class files compiled successfully"

ls addons/src/*.java > /dev/null 2>&1 || exit 0
javac -encoding utf8 -sourcepath "${SP}" -classpath "${CP}" -d addons/classes addons/src/*.java || exit 1

echo "addon class files compiled successfully"
