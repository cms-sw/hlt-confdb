#! /bin/bash -e

# set the target directory
BASE=$HOME/www/v2
URL=http://confdb.web.cern.ch/confdb/v2/gui/

# buld everything from scratch
ant clean
ant all

# backup an existing directory
if [ -e "$BASE" ]; then
  BACKUP=$BASE.`date -u -r $BASE +%Y%m%d.%H%M%S`
  mv $BASE $BACKUP
  echo "ConfDB GUI version `cat $BACKUP/confdb.version | grep confdb.version | cut -d= -f2` backed up at ${BACKUP}"
fi

# create the target directory
mkdir -p $BASE
mkdir -p $BASE/lib
mkdir -p $BASE/gui

# deploy all .jar files
cp -a lib/*.jar ext/signed/*.jar $BASE/lib/
cp -a lib/*.jar ext/signed/*.jar $BASE/gui/

# deploy the .jnlp and support files
cp -a src/conf/confdb.version $BASE/
cp -a javaws/WebContent/index.html javaws/WebContent/start.jnlp $BASE/gui/

sed -i "s#\$\$codebase#$URL#" $BASE/gui/start.jnlp

echo "ConfDB GUI version `cat $BASE/confdb.version | grep confdb.version | cut -d= -f2` successfully deployed at ${URL}start.jnlp"
