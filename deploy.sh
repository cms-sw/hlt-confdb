#! /bin/bash -e

#we get the branch from 
BRANCH=`git rev-parse --abbrev-ref HEAD`
VERSION=`git rev-parse --abbrev-ref HEAD | sed s/confdb//g`
GIT_HASH=`git rev-parse HEAD`
VALID_VERSIONS=("v2" "v3" "v3-beta" "v3-test")
PROD_VERSIONS=("v2" "v3")

if [[ ! " ${VALID_VERSIONS[*]} " =~ " ${VERSION} " ]]; then
    echo "branch $BRANCH gives version $VERSION which is not a valid version, which are:"
    echo "   ${VALID_VERSIONS[@]}"
    echo "exiting"
    exit
fi

if [[ ! " ${PROD_VERSIONS[*]} " =~ " ${VERSION} " ]]; then
    echo "non production version $VERSION detected, appending git hash to version number"
    sed -i -E 's/(^confdb.version=V)([0-9\-]+)/\1\2-'$GIT_HASH'/g' src/conf/confdb.version 
fi

BASE=$HOME/www/$VERSION
URL=https://confdb.web.cern.ch/confdb/$VERSION/gui/


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
echo "confdb.githash=$GIT_HASH" >> $BASE/confdb.version
cp -a javaws/WebContent/index.html javaws/WebContent/start.jnlp $BASE/gui/

sed -i "s#\$\$codebase#$URL#" $BASE/gui/start.jnlp

echo "ConfDB GUI version `cat $BASE/confdb.version | grep confdb.version | cut -d= -f2` successfully deployed at ${URL}start.jnlp"
if [[ ! " ${PROD_VERSIONS[*]} " =~ " ${VERSION} " ]]; then
    echo "restoring branch confdb.version"
    git checkout src/conf/confdb.version 
fi
