#!/usr/bin/tcsh

# enter here your Alida installation path:
setenv ALIDA_HOME $PWD

setenv CLASSPATH ""

# append Alida jars
foreach jar ($ALIDA_HOME/*.jar)
    setenv CLASSPATH $jar":"$CLASSPATH
end
foreach jar ($ALIDA_HOME/jars/*.jar)
    setenv CLASSPATH $jar":"$CLASSPATH
end

if ( $#argv == 0 ) then
	set cmd=grappa
else
	set cmd=$1
	shift
endif

switch ( $cmd )
case grappa:
	java -Xms1024m -Xmx1024m -Xss128m -Dalida.versionprovider_class="de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar" de.unihalle.informatik.Alida.tools.ALDGrappaRunner $*
	breaksw
case guioprunner:
	java -Xms1024m -Xmx1024m -Xss128m -Dalida.versionprovider_class="de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar" de.unihalle.informatik.Alida.tools.ALDOpRunnerGUI $*
	breaksw
case oprunner:
	java -Xms1024m -Xmx1024m -Xss128m -Dalida.versionprovider_class="de.unihalle.informatik.Alida.version.ALDVersionProviderReleaseJar" de.unihalle.informatik.Alida.tools.ALDOpRunner $*
	breaksw
default:
	echo "usage runAlida.tcsh [grappa|guioprunner|oprunner] args*"
endsw
	
