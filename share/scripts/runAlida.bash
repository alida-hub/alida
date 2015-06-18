#!/bin/bash

# add here your MiToBo installation path
export ALIDA_HOME=$PWD

# init class path for JVM
export CLASSPATH=""

for jar in $ALIDA_HOME/*.jar
do
    echo $jar
	export CLASSPATH="${jar}:$CLASSPATH"
done
for jar in $ALIDA_HOME/jars/*.jar
do
    echo $jar
	export CLASSPATH="${jar}:$CLASSPATH"
done

echo CLASSPATH = "$CLASSPATH"

if [ $# -eq 0 ]
then
	cmd=grappa
else
	cmd=$1
	shift
fi


case $cmd in
grappa)
	java -Xms1024m -Xmx1024m -Xss128m -Dalida.versionprovider_class="de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar" de.unihalle.informatik.Alida.tools.ALDGrappaRunner $*
   ;;
guioprunner)
	java -Xms1024m -Xmx1024m -Xss128m -Dalida.versionprovider_class="de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar" de.unihalle.informatik.Alida.tools.ALDOpRunnerGUI $*
   ;;
oprunner)
	java -Xms1024m -Xmx1024m -Xss128m -Dalida.versionprovider_class="de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar" de.unihalle.informatik.Alida.tools.ALDOpRunner $*
   ;;
*)
   echo "usage runAlida.tcsh [grappa|guioprunner|oprunner] args*"
   ;;
esac
