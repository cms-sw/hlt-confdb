#!/bin/bash -ex

## IMPORTANT NOTE:
##  When the python libraries in ConfDB have to be updated based on a newer CMSSW release,
##  it is not sufficient to just update the release version in ${cmssw_rel_base},
##  because ad-hoc changes to FWCore/ParameterSet (see below)
##  are needed to make the python parser of ConfDB work.
##  When it is necessary to update these CMSSW python libraries in ConfDB,
##  please check that those ad-hoc changes still apply (if not, remove them, or update them).

cmssw_rel_base=/cvmfs/cms.cern.ch/el8_amd64_gcc12/cms/cmssw/CMSSW_14_0_0_pre1

if [ ! -d "${cmssw_rel_base}" ]; then
  printf "\n%s\n\n" ">> ERROR - target CMSSW-release area does not exist: ${cmssw_rel_base}"
  exit 1
fi

cd $(cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd)

# CMSSW packages to be copied into ConfDB
cmssw_pkgs=(
  FWCore/ParameterSet
  HeterogeneousCore/Common
  HeterogeneousCore/AlpakaCore
  HeterogeneousCore/CUDACore
  HeterogeneousCore/ROCmCore
)
for pkg in "${cmssw_pkgs[@]}"; do
  pkg_py="${cmssw_rel_base}"/src/"${pkg}"/python
  [ -d "${pkg_py}" ] || continue
  rm -rf ${pkg} \
    && cp -r "${pkg_py}" "${pkg}" \
    && touch "${pkg}"/{.,..}/__init__.py
done
unset cmssw_rel_base cmssw_pkgs pkg

# package: enum (source: https://pypi.org/project/enum34)
# note: required by HeterogeneousCore/Common/python/PlatformStatus.py
wget https://files.pythonhosted.org/packages/11/c4/2da1f4952ba476677a42f25cd32ab8aaf0e1c0d0e00b89822b835c7e654c/enum34-1.1.10.tar.gz
tar xzf enum34-1.1.10.tar.gz
rm -rf enum && mv enum34-1.1.10/enum .
rm -rf enum34-1.1.10*

# ad-hoc fix to Modules.py for ConfDB's python parser
# IMPORTANT: guaranteed to work only for CMSSW_14_0_0_pre1
[ ! -f FWCore/ParameterSet/Modules.py ] || \
sed -e "s|super()|super(SwitchProducer,self)|g" \
    -i FWCore/ParameterSet/Modules.py

# ad-hoc fix to OrderedSet.py for ConfDB's python parser
# IMPORTANT: guaranteed to work only for CMSSW_14_0_0_pre1
[ ! -f FWCore/ParameterSet/OrderedSet.py ] || \
sed -e "s|import collections.abc|import collections as collections_abc|g" \
    -e "s|collections.abc.MutableSet|collections_abc.MutableSet|g" \
    -i FWCore/ParameterSet/OrderedSet.py

# ad-hoc fix to Types.py for ConfDB's python parser
# IMPORTANT: guaranteed to work only for CMSSW_14_0_0_pre1
[ ! -f FWCore/ParameterSet/Types.py ] || \
sed -e "s|*args, default=None):|*args, **kwargs):|g" \
    -e "s|if default is not None:|default = kwargs.get('default', None)\n        if default is not None:|g" \
    -i FWCore/ParameterSet/Types.py
