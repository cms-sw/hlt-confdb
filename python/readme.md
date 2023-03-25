This directory contains python code taken from CMSSW
in order to parse cmsRun configurations into the ConfDB database.

To synchronise the content of this folder with a given CMSSW release,
the first step is to check the content of the script `update.sh` in this directory
(the target CMSSW release is specified in that script).
In addition to specifying the release, one often needs ad-hoc changes
to avoid syntax which is not compatible with `python2`
(the reason is that the python interpreter used by Java is compatible only with `python2`).
Once the script `update.sh` has been updated as intended,
it is sufficient to execute it from any directory, e.g.
```
./hlt-confdb/python/update.sh
```
The recipe which is currently implemented in `update.sh` requires
access to `/cvmfs/cms.cern.ch` to retrieve the content of a given CMSSW release.
