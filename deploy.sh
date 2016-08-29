#! /bin/bash -e

# set the target directory
BASE=$HOME/www/v1
URL=http://confdb.web.cern.ch/confdb/v1/gui/

# create the target directory 
mkdir -p $BASE

# deploy the GUI and the support files
unzip -o -q lib/confdb.war confdb.version gui/* -d $BASE/
sed -i "s#\$\$codebase#$URL#" $BASE/gui/start.jnlp

# deploy all .jar files for interactive use
mkdir -p $BASE/lib
cp lib/*.jar ext/signed/*.jar $BASE/lib

echo "ConfDB GUI version `cat $BASE/confdb.version | grep confdb.version | cut -d= -f2` successfully deployed at $URL"
