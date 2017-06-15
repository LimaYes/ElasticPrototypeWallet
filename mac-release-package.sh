#!/bin/bash
VERSION=$1
if [ -x ${VERSION} ];
then
	echo VERSION not defined
	exit 1
fi
PACKAGE=elastic-client-${VERSION}
echo PACKAGE="${PACKAGE}"
CHANGELOG=elastic-client-${VERSION}.changelog.txt
OBFUSCATE=$2
MACVERSION=$3
if [ -x ${MACVERSION} ];
then
MACVERSION=${VERSION}
fi
echo MACVERSION="${MACVERSION}"


FILES="changelogs conf html lib resource contrib"
FILES="${FILES} 3RD-PARTY-LICENSES.txt AUTHORS.txt LICENSE.txt"
FILES="${FILES} DEVELOPERS-GUIDE.md OPERATORS-GUIDE.md README.md README.txt USERS-GUIDE.md"
FILES="${FILES} run.bat run.sh run-desktop.sh start.sh stop.sh compact.sh compact.bat sign.sh"
FILES="${FILES} Elastic_Wallet.url Dockerfile elastic.policy elasticdesktop.policy elastic.exe elasticservice.exe"

echo compile
./compile.sh
rm -rf html/doc/*
rm -rf nxt
rm -rf ${PACKAGE}.jar
rm -rf ${PACKAGE}.exe
rm -rf ${PACKAGE}.zip
mkdir -p nxt/
mkdir -p nxt/logs
mkdir -p nxt/addons/src

FILES="${FILES} classes src COPYING.txt"
FILES="${FILES} compile.sh javadoc.sh jar.sh package.sh"
echo javadoc
./javadoc.sh

echo copy resources
cp installer/lib/JavaExe.exe elastic.exe
cp installer/lib/JavaExe.exe elasticservice.exe
cp -a ${FILES} nxt
cp -a logs/placeholder.txt nxt/logs
echo gzip
for f in `find nxt/html -name *.gz`
do
	rm -f "$f"
done
for f in `find nxt/html -name *.html -o -name *.js -o -name *.css -o -name *.json  -o -name *.ttf -o -name *.svg -o -name *.otf`
do
	gzip -9c "$f" > "$f".gz
done
cd nxt
echo generate jar files
../jar.sh
echo package installer Jar
../installer/build-installer.sh ../${PACKAGE}
cd -
rm -rf nxt

echo bundle a dmg file	
javapackager -deploy -outdir . -outfile elastic-client -name "Elastic-Installer" -width 34 -height 43 -native dmg -srcfiles ${PACKAGE}.jar -appclass com.izforge.izpack.installer.bootstrap.Installer -v -Bmac.category=Business -Bmac.CFBundleIdentifier=org.xel.client.installer -Bmac.CFBundleName=Elastic-Installer -Bmac.CFBundleVersion=${MACVERSION} -BappVersion=${MACVERSION} -Bicon=installer/AppIcon.icns > installer/javapackager.log 2>&1
