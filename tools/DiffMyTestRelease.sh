#!/bin/sh 
# 

CUR_DIR=`pwd`
cd $CMSSW_BASE
MY_TEST_REL="$CMSSW_VERSION-YOUR_TEST_RELEASE"
echo '###Building your test release configurations from ' $CMSSW_BASE
eval `scramv1 b python`
cd $CUR_DIR
$CUR_DIR/ConfdbMakeOfflineConfig.py >& CMSSWold_cff.py
cd $CMSSW_BASE
echo '###Un-building your test release configurations from ' $CMSSW_BASE
eval `scramv1 b clean`
cd $CUR_DIR
echo '###Comparing the two configurations '
eval `scramv1 runtime -sh`
export THE_OLD_RELEASE_NAME=$MY_TEST_REL
$CUR_DIR/ConfdbDiffOfflineConfig.py >& thediff.twikiformat.txt
rm CMSSWold_cff.py
rm CMSSWold_cff.pyc
exit
