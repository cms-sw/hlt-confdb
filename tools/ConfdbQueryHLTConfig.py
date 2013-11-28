#!/usr/bin/env python

import os, string, sys, posix, tokenize, array, getopt

sys.path.append(os.environ.get("CMS_PATH") + "/sw/slc4_ia32_gcc345/external/py2-cx-oracle/4.2/lib/python2.4/site-packages/")

import cx_Oracle


def main(argv):

    hltclasses = []

    username = "*****"
    userpwd = "*****"
    userhost = "*****"
    #    relname = "CMSSW_2_1_10"
    relname = self.oldrelname = os.environ.get("THE_OLD_RELEASE_NAME") 

    constring = username+"/"+userpwd+"@"+userhost
    connection = cx_Oracle.connect(constring)
    
    cursor = connection.cursor()

    query = "SELECT SoftwareReleases.releaseId FROM SoftwareReleases WHERE (SoftwareReleases.releaseTag = '" + str(relname) + "')"
    cursor.execute(query)
    relsuperid = (cursor.fetchall())[0][0]    

    #Modules
    query = "SELECT ModuleTemplates.name FROM ModuleTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ModuleTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(relsuperid) + ")"
    cursor.execute(query)
    complist = cursor.fetchall()
    for comp in complist:
        hltclasses.append(comp)

    #Services
    query = "SELECT ServiceTemplates.name FROM ServiceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ServiceTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(relsuperid) + ")"
    cursor.execute(query)
    complist = cursor.fetchall()
    for comp in complist:
        hltclasses.append(comp)
        
    #EDSources
    query = "SELECT EDSourceTemplates.name FROM EDSourceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = EDSourceTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(relsuperid) + ")"
    cursor.execute(query)
    servlist = cursor.fetchall()
    for comp in complist:
        hltclasses.append(comp)
        
    #ESSources
    query = "SELECT ESSourceTemplates.name FROM ESSourceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ESSourceTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(relsuperid) + ")"
    cursor.execute(query)
    servlist = cursor.fetchall()
    for comp in complist:
        hltclasses.append(comp)

    #ESModules
    query = "SELECT ESModuleTemplates.name FROM ESModuleTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ESModuleTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(relsuperid) + ")"
    cursor.execute(query)
    servlist = cursor.fetchall()
    for comp in complist:
        hltclasses.append(comp)
        
        
    for hltclass in hltclasses:
        print str(hltclass[0])
    
if __name__ == "__main__":
    main(sys.argv[1:])
    
