webappdir=$tomcatdir/webapps
uidir=cornerstone-web
codedir=cornerstone
examplePrjName=cornerstone-example
version=0
secpara=$2

CATALINA_PID=/tmp/tmp.pid
export CATALINA_PID

_compileweb(){
cd $uidir
gulp clean deploy build
cd ..
rm -rf $codedir/src/main/resources/cornerstone-web/*
mv $uidir/dist/* $codedir/src/main/resources/cornerstone-web/
for f in `find $codedir/src/main/resources/cornerstone-web/maps/* -name '*.map'`
do
    echo '{}'> $f
done

for f in `find $codedir/src/main/resources/cornerstone-web/fonts/* -not -name '*.woff'`
do
    echo '{}'> $f
done

}

_jar(){
_stop
#gradle clean build
case $secpara in
notest)
	mvn clean install -Dmaven.test.skip=true
    ;;
*)
	mvn clean install
    ;;
esac
rm -rf $webappdir/$examplePrjName-$version
mv $examplePrjName/target/$examplePrjName-$version.war $webappdir
$tomcatdir/bin/startup.sh
}

_deploy(){
_compileweb
_jar
}

_stop(){

$tomcatdir/bin/shutdown.sh -force

}

_restart(){
  _stop

$tomcatdir/bin/startup.sh

}
case $1 in
stop)
    _stop
    ;;
restart)
    _restart
    ;;
jar)
    _jar
    ;;
*)
    _deploy
    ;;
esac
