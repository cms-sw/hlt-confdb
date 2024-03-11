## starting confdb

The prefered way is to is to use the web start

https://confdb.web.cern.ch/confdb/v3/gui/

the alternative way is to use the ./start script
```bash
git clone git@github.com:cms-sw/hlt-confdb.git
cd hlt-confdb
./start
```

## branches

The following branches are defined

  * `confdbv1`: v1 converter used in Run1
  * `confdbv2`: v2 converter used in Run2
  * `confdbv3`: v3 converter (currently in use
  * `confdbv3-beta`: beta version of the v3 converter to be at the bleeding edge
  * `confdbv3-test`: test version of the v3 converter, will break frequently

Note the `confdbv3-beta` and `confdbv3-test` do not have their history preserved and will be frequently
force synced to `confdbv3`.

## versioning policy

The `confdbv3` will be frequently released and pushed to the web server for users.

The version format is V\<converter-version\>-\<major-version\>-\<minor-version\>, e.g. `V03-00-01`.

The converter version corresponds to v3, v2, v1 and implies a major database scheme change
The major version changes whenever the python output of the menu changes, ie the same menu will now have a differnt python output. It is also permissable to increase the version number when a major feature is added, in fact this is recommended, it is just mandidatory when the python output changes. It is policy to only install major versions on the DAQ. 
The minor version is for all other changes.

## deployment instructions

  1. first the [ChangeLog](ChangeLog) describing the changes should be updated with the new version number
  1. then change the version number in [src/conf/confdb.version](src/conf/confdb.version)
  1. make the PR with this changes and merge it (and any other changes you wish to make for this release)
  1. create a [release](https://github.com/Sam-Harper/hlt-confdb/releases/new) with title of the version (eg V03-01-00). Tag it with this version as well, using "create new tag: VXX-YY-ZZ on publish"
  1. log into `lxplus8` (on `lxplus9` and above the right version of `java` is not available) as `confdb` (ping trigger management for password)
  1. go to directory with this repo cloned there (currently `~/private/hlt-confdb`)
  1. move to the branch you wish to deploy and ensure you have the latest version
  1. deploy by executing `./deploy.sh`. this will automatically deploy to the correct location using the branch name, stripping `confdb` from the start of the name to get the name to deploy to

After deployment, make sure that changes in the `confdbv3` branch are also propagated to the `confdbv3-beta` and `confdbv3-test` branches
(unless there are reasons not to, e.g. developments in progress).
The two development branches should always be a superset of the stable branch.
To deploy new versions of the non-production branches, update them appropriately
and re-run the deployment script `./deploy.sh` from the relevant branch (so, once per branch).
