#! /bin/bash -e

# set the target directory
BASE=$HOME/www

# create the target directory 
mkdir -p $BASE

# deploy the GUI and the support files
unzip -o -q lib/confdb.war confdb.version gui/* -d $BASE/
sed -i 's#$$codebase#http://confdb.web.cern.ch/confdb/gui/#' $BASE/gui/start.jnlp

# deploy all .jar files for interactive use
mkdir -p $BASE/lib
cp lib/*.jar ext/signed/*.jar $BASE/lib

echo "ConfDB GUI version `cat $BASE/confdb.version | grep confdb.version | cut -d= -f2` successfully deployed"
