#!/bin/sh
CP=conf/:classes/:lib/*:testlib/*
SP=src/:test/

if [ $# -eq 0 ]; then
TESTS="nxt.crypto.Curve25519Test nxt.crypto.ReedSolomonTest nxt.peer.HallmarkTest nxt.TokenTest nxt.RedeemTest nxt.ManualForgingTest nxt.BigDecimalTest nxt.GenesisAmountsTest nxt.BitcoinJTest nxt.SpongycastleTest"
else
TESTS=$@
fi

/bin/rm -f elastic.jar
/bin/rm -rf classes
/bin/mkdir -p classes/

javac -encoding utf8 -sourcepath ${SP} -classpath ${CP} -d classes/ src/nxt/*.java src/nxt/*/*.java test/nxt/*.java test/nxt/*/*.java || exit 1

for TEST in ${TESTS} ; do
java -classpath ${CP} org.junit.runner.JUnitCore ${TEST} ;
done



