#!/bin/sh
java -cp classes nxt.tools.ManifestGenerator
/bin/rm -f elastic.jar
jar cfm elastic.jar resource/elastic.manifest.mf -C classes . || exit 1
/bin/rm -f elasticservice.jar
jar cfm elasticservice.jar resource/elasticservice.manifest.mf -C classes . || exit 1

echo "jar files generated successfully"