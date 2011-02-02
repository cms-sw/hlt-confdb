#!/usr/bin/env python

# ConfdbOracleModuleLoader.py
# Interface for loading module templates to the Conf DB
# (Oracle version). All Oracle specific code belongs here.
# Jonathan Hollar LLNL Jan. 12, 2008

import os, string, sys, posix, tokenize, array

#sys.path.append(os.environ.get("CMS_PATH") + "/sw/slc4_ia32_gcc345/external/py2-cx-oracle/4.2/lib/python2.4/site-packages/")

sys.path.append("./")

import cx_Oracle

class ConfdbOracleModuleLoader:

    def __init__(self, verbosity, addtorelease, comparetorelease):
	self.data = []
	self.changes = []
        self.paramtypedict = {}
        self.modtypedict = {}
	self.releasekey = -1
	self.verbose = int(verbosity)
	self.addtorel = addtorelease
	self.comparetorel = comparetorelease
	self.comparetorelid = 0
	self.connection = None
	self.fwknew = 0
	self.fwkunchanged = 0
	self.fwkchanged = 0
	self.globalseqcount = 0

    # Connect to the Confdb db
    def ConfdbOracleConnect(self,dbname,username,userpwd,userhost):
#       self.connection = cx_Oracle.connect(host=userhost, 
#                                    user=username, passwd=userpwd,
#                                     db=dbname )
        self.connection = cx_Oracle.connect(username+"/"+userpwd+"@"+userhost)
        
        cursor = self.connection.cursor() 

        # Do some one-time operations - get dictionaries of parameter, module,
        # and service type mappings so we don't have to do this every time
        cursor.execute("SELECT ParameterTypes.paramType, ParameterTypes.paramTypeId FROM ParameterTypes")
        temptuple = cursor.fetchall()
	for temptype, tempname in temptuple:
	    self.paramtypedict[temptype] = tempname

	cursor.execute("SELECT ModuleTypes.type, ModuleTypes.typeId FROM ModuleTypes")
        temptuple = cursor.fetchall()
	for temptype, tempname in temptuple:
	    self.modtypedict[temptype] = tempname

        if(self.comparetorel != ""):
            print self.comparetorel
            cursor.execute("SELECT SoftwareReleases.releaseId FROM SoftwareReleases WHERE (releaseTag = '" + self.comparetorel + "')")
            tempid = cursor.fetchone()
            self.comparetorelid = tempid[0]
 
	return cursor

    
    # Add this CMSSW release to the table after a sanity check 
    # to make sure it doesn't already exist.
    def ConfdbAddNewRelease(self,thecursor,therelease):
	thecursor.execute("SELECT SoftwareReleases.releaseId FROM SoftwareReleases WHERE (releaseTag = '" + therelease + "')")
	therelnum =  thecursor.fetchone()

	if(therelnum):
	    print "\tThis release already exists in the DB!"
	    return -1

	thecursor.execute("INSERT INTO SoftwareReleases (releaseTag) VALUES ('" + therelease + "')")

#	thecursor.execute("SELECT LAST_INSERT_ID()")

        thecursor.execute("SELECT ReleaseId_Sequence.currval from dual")
#	thecursor.execute("SELECT releaseId FROM SoftwareReleases ORDER BY releaseId DESC")
	therelnum = (thecursor.fetchone())[0]
	print "New releasekey = " + str(therelnum)

	self.releasekey = therelnum

	return therelnum

    # Given a tag of a module, check if its template exists in the DB
    def ConfdbCheckModuleExistence(self,thecursor,modtype,modname,modtag):
	thecursor.execute("SELECT * FROM SuperIds")

        # Get the module type (base class) ID
	modtypestr = str(self.modtypedict[modtype])

        # See if a module of this type, name, and CVS tag already exists
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT ModuleTemplates.superId, ModuleTemplates.name FROM ModuleTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ModuleTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ") AND (ModuleTemplates.name = '" + modname + "') AND (ModuleTemplates.typeId = " + modtypestr + ")")
	else:
	    if(self.verbose > 2):
		print "SELECT ModuleTemplates.superId FROM ModuleTemplates WHERE (ModuleTemplates.name = '" + modname + "') AND (ModuleTemplates.typeId = " + modtypestr + ")"
	    thecursor.execute("SELECT ModuleTemplates.superId FROM ModuleTemplates WHERE (ModuleTemplates.name = '" + modname + "') AND (ModuleTemplates.typeId = '" + modtypestr + "')")

	modsuperid = thecursor.fetchone()

        if(modsuperid):
            return modsuperid
        else:
            return 0
        
        return modsuperid

    # Given a tag of a service, check if its template exists in the DB
    def ConfdbCheckServiceExistence(self,thecursor,servname,servtag):
	thecursor.execute("SELECT * FROM SuperIds")

        # See if a service of this name and CVS tag already exists
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT ServiceTemplates.superId, ServiceTemplates.name FROM ServiceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ServiceTemplates.superId) WHERE (ServiceTemplates.name = '" + servname + "') AND (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ")")
	else:
	    if(self.verbose > 2):
		print "SELECT ServiceTemplates.superId FROM ServiceTemplates WHERE (ServiceTemplates.name = '" + servname + "')"
	    thecursor.execute("SELECT ServiceTemplates.superId FROM ServiceTemplates WHERE (ServiceTemplates.name = '" + servname + "')")

	servsuperid = thecursor.fetchone()

        if(servsuperid):
            return servsuperid
        else:
            return 0
        
        return servsuperid

    # Given a tag of an es_source, check if its template exists in the DB
    def ConfdbCheckESSourceExistence(self,thecursor,srcname,srctag):
	thecursor.execute("SELECT * FROM SuperIds")

        # See if a service of this name and CVS tag already exists
        # See if a service of this name and CVS tag already exists
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT ESSourceTemplates.superId, ESSourceTemplates.name FROM ESSourceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ESSourceTemplates.superId) WHERE (ESSourceTemplates.name = '" + srcname + "') AND (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ")")
	else:
	    if(self.verbose > 2):
		print "SELECT ESSourceTemplates.superId FROM ESSourceTemplates WHERE (ESSourceTemplates.name = '" + srcname + "')"
	    thecursor.execute("SELECT ESSourceTemplates.superId FROM ESSourceTemplates WHERE (ESSourceTemplates.name = '" + srcname + "')")

	srcsuperid = thecursor.fetchone()

        if(srcsuperid):
            return srcsuperid
        else:
            return 0
        
        return srcsuperid

    # Given a tag of an ed_source, check if its template exists in the DB
    def ConfdbCheckEDSourceExistence(self,thecursor,srcname,srctag):
	thecursor.execute("SELECT * FROM SuperIds")

        # See if a service of this name and CVS tag already exists
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT EDSourceTemplates.superId, EDSourceTemplates.name FROM EDSourceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = EDSourceTemplates.superId) WHERE (EDSourceTemplates.name = '" + srcname + "') AND (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ")")
	else:
	    if(self.verbose > 2):
		print "SELECT EDSourceTemplates.superId FROM EDSourceTemplates WHERE (EDSourceTemplates.name = '" + srcname + "')"
	    thecursor.execute("SELECT EDSourceTemplates.superId FROM EDSourceTemplates WHERE (EDSourceTemplates.name = '" + srcname + "')")

	srcsuperid = thecursor.fetchone()

        if(srcsuperid):
            return srcsuperid
        else:
            return 0
        
        return srcsuperid

    # Given a tag of an es_module, check if its template exists in the DB
    def ConfdbCheckESModuleExistence(self,thecursor,srcname,srctag):
	thecursor.execute("SELECT * FROM SuperIds")

        # See if a service of this name and CVS tag already exists
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT ESModuleTemplates.superId, ESModuleTemplates.name FROM ESModuleTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ESModuleTemplates.superId) WHERE (ESModuleTemplates.name = '" + srcname + "') AND (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ")")
	else:
	    if(self.verbose > 2):
		print "SELECT ESModuleTemplates.superId FROM ESModuleTemplates WHERE (ESModuleTemplates.name = '" + srcname + "')"
	    thecursor.execute("SELECT ESModuleTemplates.superId FROM ESModuleTemplates WHERE (ESModuleTemplates.name = '" + srcname + "')")

	esmodsuperid = thecursor.fetchone()

        if(esmodsuperid):
            return esmodsuperid
        else:
            return 0
        
        return esmodsuperid
               
    # Create a  new module template in the DB
    def ConfdbLoadNewModuleTemplate(self,thecursor,modclassname,modbaseclass,modcvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):
	
	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUES('')")

        thecursor.execute("SELECT SuperId_Sequence.currval from dual") 
	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Get the module type (base class)
	modbaseclassid = self.modtypedict[modbaseclass]

        if(self.addtorel != "none"):
            print 'This module appears in the base release with a different tag. Will ADD from the specified test release'

	# Now create a new module
	if(self.verbose > 2):
	    print "INSERT INTO ModuleTemplates (superId, typeId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + modclassname + "', '" + modcvstag + "', '" + str(softpackageid) + "')"
            
	thecursor.execute("INSERT INTO ModuleTemplates (superId, typeId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + modclassname + "', '" + modcvstag + "', '" + str(softpackageid) + "')")

	
	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbLoadNewModuleTemplate
	
    # Create a new service template in the DB
    def ConfdbLoadNewServiceTemplate(self,thecursor,servclassname,servcvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):

	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUES('')")

#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")
	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

        if(self.addtorel != "none"):
            print 'This service appears in the base release with a different tag. Will ADD from the specified test release'

	# Now create a new service
	thecursor.execute("INSERT INTO ServiceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + servclassname + "', '" + servcvstag + "', '" + str(softpackageid) + "')")
	if(self.verbose > 2):
	    print "INSERT INTO ServiceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + servclassname + "', '" + servcvstag + "', '" + str(softpackageid) + "')"
	
	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbLoadNewServiceTemplate

    # Create a new es_source template in the DB
    def ConfdbLoadNewESSourceTemplate(self,thecursor,srcclassname,srccvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):
	
	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUES('')")

#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

        if(self.addtorel != "none"):
            print 'This es_source appears in the base release with a different tag. Will ADD from the specified test release'

	# Now create a new es_source
	thecursor.execute("INSERT INTO ESSourceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + srcclassname + "', '" + srccvstag + "', '" + str(softpackageid) + "')")
	if(self.verbose > 2):
	    print "INSERT INTO ESSourceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + srcclassname + "', '" + srccvstag + "', '" + str(softpackageid) + "')"
	
        # appendToDataLabel
        doAppend = False
        for ptype, pname,pval,ptracked,pseq in parameters:
            if pval == 'appendToDataLabel':
                doAppend = True
        if(doAppend == False):
            print "\tMessage: appending string appendToDataLabel to this ESSource"
            parameters.append(("string","appendToDataLabel","''","true",999))

	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)


	self.globalseqcount = 0
    # End ConfdbLoadNewESSourceTemplate

    # Create a new ed_source template in the DB
    def ConfdbLoadNewEDSourceTemplate(self,thecursor,srcclassname,srccvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):
	
	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUES('')")

#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

        if(self.addtorel != "none"):
            print 'This ed_source appears in the base release with a different tag. Will ADD from the specified test release'

	# Now create a new es_source
	thecursor.execute("INSERT INTO EDSourceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + srcclassname + "', '" + srccvstag + "', '" + str(softpackageid) + "')")
	if(self.verbose > 2):
	    print "INSERT INTO EDSourceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + srcclassname + "', '" + srccvstag + "', '" + str(softpackageid) + "')"
	
	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbLoadNewEDSourceTemplate

    # Create a new es_module template in the DB
    def ConfdbLoadNewESModuleTemplate(self,thecursor,modclassname,modcvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):
	
	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUES('')")

#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC");
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

        if(self.addtorel != "none"):
            print 'This es_module appears in the base release with a different tag. Will ADD from the specified test release'

	# Now create a new module
        if(self.verbose > 2):
            print "INSERT INTO ESModuleTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", " + modclassname + "', '" + modcvstag + "', '" + str(softpackageid) + "')"
	thecursor.execute("INSERT INTO ESModuleTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + modclassname + "', '" + modcvstag + "', '" + str(softpackageid) + "')")
	
        # appendToDataLabel
        doAppend = False
        for ptype, pname,pval,ptracked,pseq in parameters:
            if pval == 'appendToDataLabel':
                doAppend = True
        if(doAppend == False):
            print "\tMessage: appending string appendToDataLabel to this ESSource"
            parameters.append(("string","appendToDataLabel","''","true",999))

	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbLoadNewESModuleTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateModuleTemplate(self,thecursor,modclassname,modbaseclass,modcvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):

	# Get the SuperId of the previous version of this template
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT ModuleTemplates.superId, ModuleTemplates.cvstag FROM ModuleTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ModuleTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ") AND (ModuleTemplates.name = '" + modclassname + "')")
            print "SELECT ModuleTemplates.superId, ModuleTemplates.cvstag FROM ModuleTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ModuleTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ") AND (ModuleTemplates.name = '" + modclassname + "')"
	else:
	    thecursor.execute("SELECT ModuleTemplates.superId, ModuleTemplates.cvstag FROM ModuleTemplates WHERE (ModuleTemplates.name = '" + modclassname + "') ORDER BY ModuleTemplates.superId DESC")

	oldmodule = thecursor.fetchone()
	oldsuperid = oldmodule[0]
	oldtag = oldmodule[1]
	print '\tOld module had tag' + ' ' + oldtag + ' with superId = ' + str(oldsuperid) + ', new module has tag ' + modcvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if((oldtag == modcvstag) and (self.addtorel == "none")):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this module is unchanged - attach old template to new release'
	    if(self.verbose > 0):
		print 'New releaseId = ' + str(self.releasekey)
                print "INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")"
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

#        if(self.addtorel != "none"):
#            print 'This module already exists in the release. Will REPLACE it with the one from the specified test release'
#            if(self.verbose > 2):
#                print "DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid)
#            thecursor.execute("DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid))
                                                                    
	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUES('')")
#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Get the module type (base class)
	modbaseclassid = self.modtypedict[modbaseclass]

	# Now create a new module
	thecursor.execute("INSERT INTO ModuleTemplates (superId, typeId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + modclassname + "', '" + modcvstag + "', '" + str(softpackageid) + "')")
	if(self.verbose > 2):
	    print "INSERT INTO ModuleTemplates (superId, typeId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + modclassname + "', '" + modcvstag + "', '" + str(softpackageid) + "')"
	
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateModuleTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateServiceTemplate(self,thecursor,servclassname,servcvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):

	# Get the SuperId of the previous version of this template
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT ServiceTemplates.superId, ServiceTemplates.cvstag FROM ServiceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ServiceTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ") AND (ServiceTemplates.name = '" + servclassname + "')") 
	else:
	    thecursor.execute("SELECT ServiceTemplates.superId, ServiceTemplates.cvstag FROM ServiceTemplates WHERE (ServiceTemplates.name = '" + servclassname + "') ORDER BY ServiceTemplates.superId DESC")

	oldservice = thecursor.fetchone()
	oldsuperid = oldservice[0]
	oldtag = oldservice[1]
	print 'Old service had ' + ' ' + oldtag + ', new service has tag ' + servcvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if((oldtag == servcvstag) and (self.addtorel == "none")):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this service is unchanged - attach old template to new release'
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

#        if(self.addtorel != "none"):
#            print 'This service already exists in the release. Will REPLACE it with the one from the specified test release'
#            if(self.verbose > 2):
#                print "DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid)
#            thecursor.execute("DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid))
                                                                    
	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUES('')")
#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	print 'New service has ' + str(newsuperid) + ' ' + servcvstag

	# Now create a new service
	thecursor.execute("INSERT INTO ServiceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '"  + servclassname + "', '" + servcvstag + "', '" + str(softpackageid) + "')")
	
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateServiceTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateESSourceTemplate(self,thecursor,sourceclassname,sourcecvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):

	# Get the SuperId of the previous version of this template
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT ESSourceTemplates.superId, ESSourceTemplates.cvstag FROM ESSourceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ESSourceTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ") AND (ESSourceTemplates.name = '" + sourceclassname + "')") 
	else:
	    thecursor.execute("SELECT ESSourceTemplates.superId, ESSourceTemplates.cvstag FROM ESSourceTemplates WHERE (ESSourceTemplates.name = '" + sourceclassname + "') ORDER BY ESSourceTemplates.superId DESC")

	oldsource = thecursor.fetchone()
	oldsuperid = oldsource[0]
	oldtag = oldsource[1]
	print 'Old source had tag ' + ' ' + oldtag + ', new source has tag ' + sourcecvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if((oldtag == sourcecvstag) and (self.addtorel == "none")):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this source is unchanged - attach old template to new release'
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

#        if(self.addtorel != "none"):
#            print 'This source already exists in the release. Will REPLACE it with the one from the specified test release'
#            if(self.verbose > 2):
#                print "DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid)
#            thecursor.execute("DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid))
                                                                    
	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUES('')")
#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Now create a new source
	thecursor.execute("INSERT INTO ESSourceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + sourceclassname + "', '" + sourcecvstag + "', '" + str(softpackageid) + "')")


        # appendToDataLabel
        doAppend = False
        for ptype, pname,pval,ptracked,pseq in parameters:
            if pval == 'appendToDataLabel':
                doAppend = True
        if(doAppend == False):
            print "\tMessage: appending string appendToDataLabel to this ESSource"
            parameters.append(("string","appendToDataLabel","''","true",999))
                                                                                                 
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateESSourceTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateEDSourceTemplate(self,thecursor,sourceclassname,sourcecvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):

	# Get the SuperId of the previous version of this template
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT EDSourceTemplates.superId, EDSourceTemplates.cvstag FROM EDSourceTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = EDSourceTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ") AND (EDSourceTemplates.name = '" + sourceclassname + "')") 
	else:
	    thecursor.execute("SELECT EDSourceTemplates.superId, EDSourceTemplates.cvstag FROM EDSourceTemplates WHERE (EDSourceTemplates.name = '" + sourceclassname + "') ORDER BY EDSourceTemplates.superId DESC")

	oldsource = thecursor.fetchone()
	oldsuperid = oldsource[0]
	oldtag = oldsource[1]
	print 'Old source had ' + ' ' + oldtag + ', new source has tag ' + sourcecvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if((oldtag == sourcecvstag) and (self.addtorel == "none")):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this source is unchanged - attach old template to new release'
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

#        if(self.addtorel != "none"):
#            print 'This source already exists in the release. Will REPLACE it with the one from the specified test release'
#            if(self.verbose > 2):
#                print "DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid)
#            thecursor.execute("DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid))
                                                                    
	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUES('')")
#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Now create a new source
	thecursor.execute("INSERT INTO EDSourceTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + sourceclassname + "', '" + sourcecvstag + "', '" + str(softpackageid) + "')")
	
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateEDSourceTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateESModuleTemplate(self,thecursor,sourceclassname,sourcecvstag,parameters,vecparameters,paramsets,vecparamsets,softpackageid):

	# Get the SuperId of the previous version of this template
	if(self.comparetorelid != ""):
	    thecursor.execute("SELECT ESModuleTemplates.superId, ESModuleTemplates.cvstag FROM ESModuleTemplates JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = ESModuleTemplates.superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.comparetorelid) + ") AND (ESModuleTemplates.name = '" + sourceclassname + "')") 
	else:
	    thecursor.execute("SELECT ESModuleTemplates.superId, ESModuleTemplates.cvstag FROM ESModuleTemplates WHERE (ESModuleTemplates.name = '" + sourceclassname + "') ORDER BY ESModuleTemplates.superId DESC")
            
	oldsource = thecursor.fetchone()
	oldsuperid = oldsource[0]
	oldtag = oldsource[1]
	print 'Old esmodule had ' + ' ' + oldtag + ' and superId = ' + str(oldsuperid) + ', new esmodule has tag ' + sourcecvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if((oldtag == sourcecvstag) and (self.addtorel == "none")):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this esmodule is unchanged - attach old template to new release'
            print "INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")"
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

#        if(self.addtorel != "none"):
#            print 'This esmodule already exists in the release. Will REPLACE it with the one from the specified test release'
#            if(self.verbose > 2):
#                print "DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid)
#            thecursor.execute("DELETE FROM SuperIdReleaseAssoc WHERE SuperIdReleaseAssoc.superId = " + str(oldsuperid))
                                                                    
	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUES('')")
#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
        thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Now create a new module
	thecursor.execute("INSERT INTO ESModuleTemplates (superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + sourceclassname + "', '" + sourcecvstag + "', '" + str(softpackageid) + "')")

        # appendToDataLabel
        doAppend = False
        for ptype, pname,pval,ptracked,pseq in parameters:
            if pval == 'appendToDataLabel':
                doAppend = True
        if(doAppend == False):
            print "\tMessage: appending string appendToDataLabel to this ESSource"
            parameters.append(("string","appendToDataLabel","''","true",999))
	
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateESModuleTemplate


    # Associate a list of parameters with a component template (via superId)
    def ConfdbAttachParameters(self,thecursor,newsuperid,parameters,vecparameters):

	self.globalseqcount = 0

	# First the non-vectors
	for paramtype, paramname, paramval, paramistracked, paramseq in parameters:

	    paramseq = self.globalseqcount
	    self.globalseqcount = self.globalseqcount + 1

	    # int32
	    if(paramtype == "int32" or paramtype == "int" or paramtype == "int32_t"):
		type = self.paramtypedict['int32']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		if(paramval):
		    if(paramval.find('.') != -1):
			paramval = str(int(float(paramval)))
		    elif(not paramval.isdigit()):
			paramval = None

		# Fill ParameterValues table
		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(paramval).find("X") == -1 and str(paramval).find("x") == -1):
                        thecursor.execute("INSERT INTO Int32ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
                    else:
                        paramval = str(int(str(paramval), 16))
                        thecursor.execute("INSERT INTO Int32ParamValues (paramId, value, hex) VALUES (" + str(newparamid) + ", " + paramval + ", 1)") 

	    # uint32
	    elif(paramtype == "uint32" or paramtype == "unsigned int" or paramtype == "uint32_t" or paramtype == "unsigned" or paramtype == "uint"):
		type = self.paramtypedict['uint32']

		if(paramval):
		    if(str(paramval).endswith("U")):
			paramval = (str(paramval).rstrip("U"))

		    if(paramval.find('.') != -1):
			paramval = str(int(float(paramval)))
		    elif(not paramval.isdigit()):
			paramval = None

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)    

		# Fill ParameterValues table
		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(paramval).find("X") == -1 and str(paramval).find("x") == -1): 
                        thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
                    else:
                        paramval = str(int(str(paramval), 16))                        
                        thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value, hex) VALUES (" + str(newparamid) + ", " + paramval + ", 1)") 


	    # int64
	    elif(paramtype == "int64" or paramtype == "long" or paramtype == "int64_t"):
		type = self.paramtypedict['int64']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		if(paramval):
		    if(paramval.find('.') != -1):
			paramval = str(int(float(paramval)))
		    elif(not paramval.isdigit()):
			paramval = None

		# Fill ParameterValues table
		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(paramval).find("X") == -1 and str(paramval).find("x") == -1):
                        thecursor.execute("INSERT INTO Int64ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
                    else:
                        paramval = str(int(str(paramval), 16))
                        thecursor.execute("INSERT INTO Int64ParamValues (paramId, value, hex) VALUES (" + str(newparamid) + ", " + paramval + ", 1)") 

	    # uint64
	    elif(paramtype == "uint64" or paramtype == "unsigned long" or paramtype == "uint64_t"):
		type = self.paramtypedict['uint64']

		if(paramval):
		    if(str(paramval).endswith("U")):
			paramval = (str(paramval).rstrip("U"))

		    if(paramval.find('.') != -1):
			paramval = str(int(float(paramval)))
		    elif(not paramval.isdigit()):
			paramval = None

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)    

		# Fill ParameterValues table
		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(paramval).find("X") == -1 and str(paramval).find("x") == -1): 
                        thecursor.execute("INSERT INTO UInt64ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
                    else:
                        paramval = str(int(str(paramval), 16))                        
                        thecursor.execute("INSERT INTO UInt64ParamValues (paramId, value, hex) VALUES (" + str(newparamid) + ", " + paramval + ", 1)") 


	    # bool
	    elif(paramtype == "bool"):
		type = self.paramtypedict['bool']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)   

                boolval = str(paramval).strip('"').strip()
                if(boolval == "true"):
                    paramval = str(1)
                if(boolval == "false"):
                    paramval = str(0)

                if(paramval != "0" and paramval != "1"):
                    paramval = None
                
		# Fill ParameterValues table
		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(self.verbose > 2):
                        print "INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")"
		    thecursor.execute("INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")


	    # double
	    elif(paramtype == "double"):
		type = self.paramtypedict['double']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		if(paramval):
		    if(paramval.find('.') == -1 and (not paramval.isdigit())):
			paramval = None

		# Fill ParameterValues table
		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
		    thecursor.execute("INSERT INTO DoubleParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")

	    #string
	    elif(paramtype == "string"):
		type = self.paramtypedict['string']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"

		else:
                    if(len(paramval) > 1024):
                        paramval = ""
                                        
		    # Stupid special case for string variables defined in 
		    # single quotes in .cf* files
		    if(paramval.find("'") != -1):
			# Fill ParameterValues table
			thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
		    elif(paramval.find('"') != -1):
			thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")
		    else:
                        paramval = "'" + str(paramval) + "'"

                        thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")") 
                        #			print "\tWarning: Attempted to load a non-string value to string table:"
                        #			print "\t\tstring " + str(paramname) + " = " + str(paramval)
                        #			print "\t\tLoading parameter with no default value"

	    #FileInPath
	    elif(paramtype == "FileInPath"):
		type = self.paramtypedict['FileInPath']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
		    # Stupid special case for string variables defined in 
		    # single quotes in .cf* files
		    if(paramval.find("'") != -1):
			# Fill ParameterValues table
			thecursor.execute("INSERT INTO FileInPathParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
		    elif(paramval.find('"') != -1):
			thecursor.execute("INSERT INTO FileInPathParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")
		    else:
			print "\tWarning: Attempted to load a non-string value to FileInPath table:"
			print "\t\tstring " + str(paramname) + " = " + str(paramval)
			print "\t\tLoading parameter with no default value"

	    # InputTag
	    elif(paramtype == "InputTag"):
		type = self.paramtypedict['InputTag']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		# Fill ParameterValues table
		if(paramval == None or paramval == ''):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
		    if(paramval.find("'") != -1):
			if(self.verbose > 2):
			    print "INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")"
			thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
		    else:
			if(self.verbose > 2):
			    print "INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')"
			thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")

	    else:
		print '\tError: Unknown param type ' + paramtype + ' ' + paramname + ' - do nothing'
                self.globalseqcount = self.globalseqcount - 1
                
	# Now deal with any vectors
	for vecptype, vecpname, vecpvals, vecpistracked, vecpseq in vecparameters:

	    vecpseq = self.globalseqcount
	    self.globalseqcount = self.globalseqcount + 1

	    # vector<int32>
	    if(vecptype == "vint32" or vecptype == "int32" or vecptype == "int" or vecptype == "int32_t"):
		type = self.paramtypedict['vint32']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		sequencer = 0

		for vecpval in vecpvals:
		    if(vecpval):
			# Fill ParameterValues table
			if(self.verbose > 2):
			    print "INSERT INTO VInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")"
                        if(str(vecpval).find("X") == -1 and str(vecpval).find("x") == -1):
                            thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
                        else:
                            vecpval = str(int(str(vecpval), 16))                            
                            thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ", 1)")
			sequencer = sequencer + 1

	    # vector<uint32>
	    elif(vecptype == "vunsigned" or vecptype == "uint32" or vecptype == "unsigned int" or vecptype == "uint32_t" or vecptype == "unsigned" or vecptype == "uint32" or vecptype == "uint" or vecptype == "vuint32"):
		type = self.paramtypedict['vuint32']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		sequencer = 0

		for vecpval in vecpvals:
		    if(vecpval):
			# Fill ParameterValues table
			if(self.verbose > 2):
			    print "INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")"
                        if(str(vecpval).find("X") == -1 and str(vecpval).find("x") == -1):
                            thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
                        else:
                            vecpval = str(int(str(vecpval), 16))                            
                            thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ", 1)")
			sequencer = sequencer + 1
                        

	    # vector<int64>
	    elif(vecptype == "vint64" or vecptype == "int64" or vecptype == "vint64_t"):
		type = self.paramtypedict['vint64']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		sequencer = 0

		for vecpval in vecpvals:
		    if(vecpval):
			# Fill ParameterValues table
			if(self.verbose > 2):
			    print "INSERT INTO VInt64ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")"
                        if(str(vecpval).find("X") == -1 and str(vecpval).find("x") == -1):
                            thecursor.execute("INSERT INTO VInt64ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
                        else:
                            vecpval = str(int(str(vecpval), 16))                            
                            thecursor.execute("INSERT INTO VInt64ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ", 1)")
			sequencer = sequencer + 1

	    # vector<uint64>
	    elif(vecptype == "uint64" or vecptype == "unsigned int64" or vecptype == "uint64_t" or vecptype == "unsigned long" or vecptype == "vuint64"):
		type = self.paramtypedict['vuint64']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		sequencer = 0

		for vecpval in vecpvals:
		    if(vecpval):
			# Fill ParameterValues table
			if(self.verbose > 2):
			    print "INSERT INTO VUInt64ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")"
                        if(str(vecpval).find("X") == -1 and str(vecpval).find("x") == -1):
                            thecursor.execute("INSERT INTO VUInt64ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
                        else:
                            vecpval = str(int(str(vecpval), 16))                            
                            thecursor.execute("INSERT INTO VUInt64ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ", 1)")
			sequencer = sequencer + 1

	    #vector<double>
	    elif(vecptype == "vdouble" or vecptype == "double"):
		type = self.paramtypedict['vdouble']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		sequencer = 0

		for vecpval in vecpvals:
		    if(vecpval):
			# Fill ParameterValues table
			if(self.verbose > 2):
			    print "INSERT INTO VDoubleParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")"
			thecursor.execute("INSERT INTO VDoubleParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
			sequencer = sequencer + 1

	    # vector<InputTag>
	    elif(vecptype == "VInputTag" or vecptype == "InputTag" or vecptype == "vtag"):
		type = self.paramtypedict['VInputTag']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		sequencer = 0

		for vecpval in vecpvals:
		    if(vecpval):
			# Fill ParameterValues table
			if(self.verbose > 2):
			    print "INSERT INTO VInputTagParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", '" + vecpval + "')"
			thecursor.execute("INSERT INTO VInputTagParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", '" + vecpval + "')")   
			sequencer = sequencer + 1

	    # vector<string>
	    elif(vecptype == "vstring" or vecptype == "vString" or vecptype == "string"):
		type = self.paramtypedict['vstring']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		sequencer = 0

		for vecpval in vecpvals:
		    if(vecpval):
			# Handle signle quoted strings
			if(vecpval.find("'") != -1):
			    # Fill ParameterValues table
			    if(self.verbose > 2):
				print "INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) +", " + vecpval + ")"
			    thecursor.execute("INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
			else:
			    # Fill ParameterValues table
			    if(self.verbose > 2):
				print "INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", '" + vecpval + "')"
			    thecursor.execute("INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", '" + vecpval + "')")   

			sequencer = sequencer + 1

	    else:
		if(self.verbose > 0):
		    print 'Unknown vector param type ' + vecptype + ' ' + vecpname + ' - do nothing'

    # End ConfdbAttachParameters

    # Update a list of parameters if necessary
    def ConfdbUpdateParameters(self,thecursor,oldsuperid,newsuperid,parameters,vecparameters):

	self.globalseqcount = 0
	
	# First the non-vectors
	for paramtype, paramname, paramval, paramistracked, paramseq in parameters:
	    
	    paramseq = self.globalseqcount
	    self.globalseqcount = self.globalseqcount + 1

            if(paramval == 'true'):
                paramval = str(1)
            elif(paramval == 'false'):
                paramval = str(0)


	    neednewparam = False

	    oldparamval = None

	    # int32
	    if(paramtype == "int32" or paramtype == "int" or paramtype == "int32_t"):
		type = self.paramtypedict['int32']

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)

		# Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.
		if(paramval):
		    if(paramval.find('.') != -1 and paramval.find('get') == -1):
			paramval = str(int(float(paramval)))
		    elif(not paramval.isdigit()):
			paramval = None
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):
		    thecursor.execute("SELECT Int32ParamValues.value FROM Int32ParamValues WHERE (Int32ParamValues.paramId = " + str(oldparamid) + ")")

		    oldparamval = thecursor.fetchone()

		    if(oldparamval):
			oldparamval = oldparamval[0]

		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or 
		       (oldparamval == None and paramval == None)):
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		    # Fill ParameterValues table
		    if(paramval == None):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
                        if(str(paramval).find("X") == -1 and str(paramval).find("x") == -1):  
                            thecursor.execute("INSERT INTO Int32ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + str(paramval) + ")")
                        else:
                            paramval = str(int(str(paramval), 16))                            
                            thecursor.execute("INSERT INTO Int32ParamValues (paramId, value, hex) VALUES (" + str(newparamid) + ", " + str(paramval) + ", 1)") 
		else:                    
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
		    if(self.verbose > 0):
			print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

	    # uint32
	    if(paramtype == "uint32" or paramtype == "unsigned int" or paramtype == "uint32_t" or paramtype == "uint" or paramtype == "unsigned"):
		type = self.paramtypedict['uint32']

		if(paramval):
		    if(str(paramval).endswith("U")):
			paramval = (str(paramval).rstrip("U"))

		    if(paramval.find('.') != -1):
			paramval = str(int(float(paramval)))
		    elif(not paramval.isdigit()):
			paramval = None

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT UInt32ParamValues.value FROM UInt32ParamValues WHERE (UInt32ParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchone()
		    if(oldparamval):
			oldparamval = oldparamval[0]

		    if(paramval):
			paramval = int(paramval)		    

		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or 
		       (oldparamval == None and paramval == None)):			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)
		    
		    # Fill ParameterValues table
		    if(paramval == None):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
                        if(str(paramval).find("X") == -1 and str(paramval).find("x") == -1):
                            thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + str(paramval) + ")")
                        else:
                            paramval = str(int(str(paramval), 16))                            
                            thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value, hex) VALUES (" + str(newparamid) + ", " + str(paramval) + ", 1)")
		else:
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
                    if(self.verbose > 0):
                        print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

	    elif(paramtype == "int64" or paramtype == "long" or paramtype == "int64_t"):
		type = self.paramtypedict['int64']

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)

		# Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.
		if(paramval):
		    if(paramval.find('.') != -1):
			paramval = str(int(float(paramval)))
		    elif(not paramval.isdigit()):
			paramval = None
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):
		    thecursor.execute("SELECT Int64ParamValues.value FROM Int64ParamValues WHERE (Int64ParamValues.paramId = " + str(oldparamid) + ")")

		    oldparamval = thecursor.fetchone()

		    if(oldparamval):
			oldparamval = oldparamval[0]

		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or 
		       (oldparamval == None and paramval == None)):
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		    # Fill ParameterValues table
		    if(paramval == None):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
                        if(str(paramval).find("X") == -1 and str(paramval).find("x") == -1):  
                            thecursor.execute("INSERT INTO Int64ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + str(paramval) + ")")
                        else:
                            paramval = str(int(str(paramval), 16))                            
                            thecursor.execute("INSERT INTO Int64ParamValues (paramId, value, hex) VALUES (" + str(newparamid) + ", " + str(paramval) + ", 1)") 
		else:                    
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
		    if(self.verbose > 0):
			print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

	    # uint64
	    elif(paramtype == "uint64" or paramtype == "unsigned long" or paramtype == "uint64_t" or paramtype == "ulong"):
		type = self.paramtypedict['uint64']

		if(paramval):
		    if(str(paramval).endswith("U")):
			paramval = (str(paramval).rstrip("U"))

		    if(paramval.find('.') != -1):
			paramval = str(int(float(paramval)))
		    elif(not paramval.isdigit()):
			paramval = None

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT UInt64ParamValues.value FROM UInt64ParamValues WHERE (UInt64ParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchone()
		    if(oldparamval):
			oldparamval = oldparamval[0]

		    if(paramval):
			paramval = int(paramval)		    

		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or 
		       (oldparamval == None and paramval == None)):			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)
		    
		    # Fill ParameterValues table
		    if(paramval == None):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
                        if(str(paramval).find("X") == -1 and str(paramval).find("x") == -1):
                            thecursor.execute("INSERT INTO UInt64ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + str(paramval) + ")")
                        else:
                            paramval = str(int(str(paramval), 16))                            
                            thecursor.execute("INSERT INTO UInt64ParamValues (paramId, value, hex) VALUES (" + str(newparamid) + ", " + str(paramval) + ", 1)")
		else:
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
                    if(self.verbose > 0):
                        print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

	    # bool
	    if(paramtype == "bool"):
		type = self.paramtypedict['bool']

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT BoolParamValues.value FROM BoolParamValues WHERE (BoolParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchone()

		    if(oldparamval):
			oldparamval = oldparamval[0]

                        #Oracle uses NUMBER(1) to represent Booleans!
#			if(oldparamval == 1):
#			    oldparamval = "true"
#			if(oldparamval == 0):
#			    oldparamval = "false"
		    
		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or 
		       (oldparamval == None and paramval == None)):			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)
		    
		    # Fill ParameterValues table
		    if(paramval == None):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
                        if(paramval == '"true"'):
                            paramval = str(1)
                        if(paramval == '"false"'):
                            paramval = str(0)

                        if(paramval != "0" and paramval != "1"):
                            paramval = None

                        else:
                            thecursor.execute("INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
		else:
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
		    if(self.verbose > 0):
			print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

	    # double
	    if(paramtype == "double"):
		type = self.paramtypedict['double']

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)
		
		if(paramval):
		    if(paramval.find('.') == -1 and (not paramval.isdigit())):
			paramval = None

		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT DoubleParamValues.value FROM DoubleParamValues WHERE (DoubleParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchone()
		    if(oldparamval):
			oldparamval = oldparamval[0]

		    if(paramval):
			paramval = float(paramval)	
		    
		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or 
		       (oldparamval == None and paramval == None)):
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)
		    
		    # Fill ParameterValues table
		    if(paramval == None):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
			thecursor.execute("INSERT INTO DoubleParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + str(paramval) + ")")
		else:
		    print "INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")"
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
		    if(self.verbose > 0):
			print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

	    # string
	    if(paramtype == "string"):
		type = self.paramtypedict['string']

		if(paramval):
		    if(paramval.find("'") != -1):
			paramval = paramval.lstrip("'").rstrip("'")

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):
		    thecursor.execute("SELECT StringParamValues.value FROM StringParamValues WHERE (StringParamValues.paramId = " + str(oldparamid) + ")")

		    oldparamval = thecursor.fetchone()

		    if(oldparamval):
			oldparamval = oldparamval[0]
		    
		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or
		       (oldparamval == None and paramval == None)):
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):		    
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)
		    
		    if(paramval == None):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
			# Special case for string variables defined in 
			# single quotes in .cf* files
			if(paramval.find("'") != -1):
			    # Fill ParameterValues table
			    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
			elif(paramval.find('"') != -1):
			    # Fill ParameterValues table
			    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")
			else:
                            paramval = "'" + str(paramval) + "'"
                            thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")") 
                            #			    print "\tWarning: Attempted to load a non-string value to string table:"
                            #			    print "\t\tstring " + str(paramname) + " = " + str(paramval)
                            #			    print "\t\tLoading parameter with no default value"
		else:
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
		    if(self.verbose > 0):
			print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"
                        if(self.verbose > 2):
                            print "INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")"
	    # FileInPath
	    if(paramtype == "FileInPath"):
		type = self.paramtypedict['FileInPath']

		if(paramval):
		    if(paramval.find("'") != -1):
			paramval = paramval.lstrip("'").rstrip("'")

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):
		    thecursor.execute("SELECT FileInPathParamValues.value FROM FileInPathParamValues WHERE (FileInPathParamValues.paramId = " + str(oldparamid) + ")")

		    oldparamval = thecursor.fetchone()

		    if(oldparamval):
			oldparamval = oldparamval[0]
		    
		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or
		       (oldparamval == None and paramval == None)):
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):		    
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)
		    
		    if(paramval == None):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
			# Special case for string variables defined in 
			# single quotes in .cf* files
			if(paramval.find("'") != -1):
			    # Fill ParameterValues table
			    thecursor.execute("INSERT INTO FileInPathParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
			elif(paramval.find('"') != -1):
			    # Fill ParameterValues table
			    thecursor.execute("INSERT INTO FileInPathParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")
			else:
			    print "\tWarning: Attempted to load a non-string value to FileInPath table:"
			    print "\t\tstring " + str(paramname) + " = " + str(paramval)
			    print "\t\tLoading parameter with no default value"
		else:
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
                    if(self.verbose > 0):
			print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

	    # InputTag
	    if(paramtype == "InputTag"):
		type = self.paramtypedict['InputTag']

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,paramname,oldsuperid)

		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT InputTagParamValues.value FROM InputTagParamValues WHERE (InputTagParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchone()
		    if(oldparamval):
			oldparamval = oldparamval[0]
		    
		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or
		       (oldparamval == None and paramval == None) or
                       (oldparamval == None and paramval == '')):
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != paramistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(paramistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
                else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    if(self.verbose > 0):
			print "Parameter is changed (" + str(oldparamval) + ", " + str(paramval) + ")"

		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)
		    
		    # Fill ParameterValues table
		    if(paramval == None or paramval == ''):
			if(self.verbose > 2):
			    print "No default parameter value found"
		    else:
			if(paramval.find("'") != -1):
                            print "INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")"
			    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
			else:
                            print "INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")"
			    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")
		else:
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=paramseq)
                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")

	# Now deal with any vectors
	for vecptype, vecpname, vecpvals, vecpistracked, vecpseq in vecparameters:

	    vecpseq = self.globalseqcount
	    self.globalseqcount = self.globalseqcount + 1

            if(vecpistracked == 'true'):
                vecpistracked = str(1)
            elif(vecpistracked == 'false'):
                vecpistracked = str(0)

	    # vector<int32>
	    if(vecptype == "vint32" or vecptype == "int32" or vecptype == "int" or vecptype == "int32_t"):
		type = self.paramtypedict['vint32']

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,vecpname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT VInt32ParamValues.value FROM VInt32ParamValues WHERE (VInt32ParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchall()
		    
		    valssame = self.CompareVectors(oldparamval,vecpvals)

		    # No changes. Attach parameter to new template.
		    if(valssame):
                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
                        #                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=vecpseq)
			if(self.verbose > 0):
			    print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"
			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != vecpistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(vecpistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
	    
		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		    sequencer = 0

		    for vecpval in vecpvals:
			if(vecpval):
			    # Fill ParameterValues table
                            if(str(vecpval).find("X") == -1 and str(vecpval).find("x") == -1):
                                thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
                            else:
                                vecpval = str(int(str(vecpval), 16))                                
                                thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ", 1)")
			    sequencer = sequencer + 1

	    # vector<uint32>
	    elif(vecptype == "vunsigned" or vecptype == "uint32" or vecptype == "unsigned int" or vecptype == "uint32_t" or vecptype == "unsigned" or vecptype == "uint" or vecptype == "vuint32"):
		type = self.paramtypedict['vuint32']
		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,vecpname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT VUInt32ParamValues.value FROM VUInt32ParamValues WHERE (VUInt32ParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchall()
		    
		    valssame = self.CompareVectors(oldparamval,vecpvals)

		    # No changes. Attach parameter to new template.
		    if(valssame):
                        #                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=vecpseq)
                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != vecpistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(vecpistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		    sequencer = 0

		    for vecpval in vecpvals:
			if(vecpval):
			    # Fill ParameterValues table
                            if(str(vecpval).find("X") == -1 and str(vecpval).find("x") == -1):
                                thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
                            else:
                                vecpval = str(int(str(vecpval), 16))                                
                                thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ", 1)")
			    sequencer = sequencer + 1

	    # vector<int64>
	    elif(vecptype == "vint64" or vecptype == "int64" or vecptype == "long" or vecptype == "int64_t"):
		type = self.paramtypedict['vint64']

		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,vecpname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT VInt64ParamValues.value FROM VInt64ParamValues WHERE (VInt64ParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchall()
		    
		    valssame = self.CompareVectors(oldparamval,vecpvals)

		    # No changes. Attach parameter to new template.
		    if(valssame):
                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
                        #                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=vecpseq)
			if(self.verbose > 0):
			    print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"
			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != vecpistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(vecpistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
	    
		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		    sequencer = 0

		    for vecpval in vecpvals:
			if(vecpval):
			    # Fill ParameterValues table
                            if(str(vecpval).find("X") == -1 and str(vecpval).find("x") == -1):
                                thecursor.execute("INSERT INTO VInt64ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
                            else:
                                vecpval = str(int(str(vecpval), 16))                                
                                thecursor.execute("INSERT INTO VInt64ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ", 1)")
			    sequencer = sequencer + 1

	    # vector<uint64>
	    elif(vecptype == "uint64" or vecptype == "unsigned int64" or vecptype == "unsigned long" or vecptype == "uint64_t" or vecptype == "vuint64"):
		type = self.paramtypedict['vuint64']
		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,vecpname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT VUInt64ParamValues.value FROM VUInt64ParamValues WHERE (VUInt64ParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchall()
		    
		    valssame = self.CompareVectors(oldparamval,vecpvals)

		    # No changes. Attach parameter to new template.
		    if(valssame):
                        #                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=vecpseq)
                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != vecpistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(vecpistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		    sequencer = 0

		    for vecpval in vecpvals:
			if(vecpval):
			    # Fill ParameterValues table
                            if(str(vecpval).find("X") == -1 and str(vecpval).find("x") == -1):
                                thecursor.execute("INSERT INTO VUInt64ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
                            else:
                                vecpval = str(int(str(vecpval), 16))                                
                                thecursor.execute("INSERT INTO VUInt64ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ", 1)")
			    sequencer = sequencer + 1

	    # vector<double>
	    elif(vecptype == "vdouble" or vecptype == "double"):
		type = self.paramtypedict['vdouble']
		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,vecpname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):
		    print "Found old vdouble param id " + str(oldparamid)
		    thecursor.execute("SELECT VDoubleParamValues.value FROM VDoubleParamValues WHERE (VDoubleParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchall()
		    
		    valssame = self.CompareVectors(oldparamval,vecpvals)

		    # No changes. Attach parameter to new template.
		    if(valssame):
                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
                        #                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=vecpseq)
                   
			print "vdouble is unchanged"
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != vecpistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(vecpistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		    sequencer = 0

		    for vecpval in vecpvals:
			if(vecpval):
			    # Fill ParameterValues table
			    thecursor.execute("INSERT INTO VDoubleParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
			    if(self.verbose > 2):
				print "INSERT INTO VDoubleParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")"
			    sequencer = sequencer + 1

	    # vector<string>
	    elif(vecptype == "vstring" or vecptype == "vString" or vecptype == "string"):
		type = self.paramtypedict['vstring']
		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,vecpname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT VStringParamValues.value FROM VStringParamValues WHERE (VStringParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchall()
		    
		    valssame = self.CompareVectors(oldparamval,vecpvals)

		    # No changes. Attach parameter to new template.
		    if(valssame):
                        #                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=vecpseq)
                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != vecpistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(vecpistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		    sequencer = 0

		    for vecpval in vecpvals:
			if(vecpval):
			    # Handle signle quoted strings
			    if(vecpval.find("'") != -1):
				# Fill ParameterValues table
				if(self.verbose > 2):
				    print "INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) +", " + vecpval + ")"
				thecursor.execute("INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
			    else:
				# Fill ParameterValues table
				if(self.verbose > 2):
				    print "INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", '" + vecpval + "')"
				thecursor.execute("INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", '" + vecpval + "')")   

			    sequencer = sequencer + 1

	    # vector<InputTag>
	    elif(vecptype == "VInputTag" or vecptype == "InputTag" or vecptype == "vtag"):		
		type = self.paramtypedict['VInputTag']
		# Get the old value of this parameter
		oldparamid = self.RetrieveParamId(thecursor,vecpname,oldsuperid)
		
		# A previous version of this parameter exists. See if its 
		# value has changed.
		if(oldparamid):

		    thecursor.execute("SELECT VInputTagParamValues.value FROM VInputTagParamValues WHERE (VInputTagParamValues.paramId = " + str(oldparamid) + ")")
		    oldparamval = thecursor.fetchall()
		    
		    valssame = self.CompareVectors(oldparamval,vecpvals)

		    # No changes. Attach parameter to new template.
		    if(valssame):
                        #                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=oldparamid,bindvar3=vecpseq)
                        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
			
			neednewparam = False

			# Now check if the tracked/untracked status has changed
			thecursor.execute("SELECT tracked FROM Parameters WHERE paramId = " + str(oldparamid))
			oldparamstatus = thecursor.fetchone()[0]
			if(str(bool(oldparamstatus)).lower() != vecpistracked):
			    if(self.verbose > 0):
				print "Parameter status has changed from " + str(bool(oldparamstatus)).lower() + " to " + str(vecpistracked)
			    neednewparam = True

		    # The parameter value has changed. Create a new parameter 
		    # entry and attach it to the new template.
		    else:
			neednewparam = True
		else:
		    neednewparam = True

		# We need a new entry for this parameter, either because its 
		# value changed, or there is no previous version.
		if(neednewparam == True):
		    # Fill Parameters table
		    newparamid = self.AddNewParam(thecursor,newsuperid,vecpname,type,vecpistracked,vecpseq)

		    sequencer = 0

		    for vecpval in vecpvals:
			if(vecpval):
			    # Fill ParameterValues table
			    thecursor.execute("INSERT INTO VInputTagParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", '" + vecpval + "')")   
			    sequencer = sequencer + 1

    # End ConfdbUpdateParameters

    # Associate a ParameterSet/VParameterSet with a component template 
    def ConfdbAttachParameterSets(self,thecursor,newsuperid,paramsets,vecparamsets):

	lastpsetname = ''
	psetcache = []
	lastpsetseqdict = {}
	localseqcount = 0
        psetsuperiddict = {}

	for pset, psettype, psetname, psetval, psettracked, psetseq, psetnesting, psetpsetseq in paramsets:

	    # If this is the first entry in this PSet for this component, add it to the ParameterSets table
	    if(not pset in psetcache):
		psetcache.append(pset)

		psetpsetseq = self.globalseqcount
		self.globalseqcount = self.globalseqcount + 1
		localseqcount = 0


                if(psettracked == 'true'):
                    psettracked = str(1)
                elif(psettracked == 'false'):
                    psettracked = str(0)

		thecursor.execute("INSERT INTO SuperIds VALUES('')")
#		thecursor.execute("SELECT LAST_INSERT_ID()")

#		thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
                thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
		newparamsetid = thecursor.fetchone()[0]	

		# Add a new PSet
		if(self.verbose > 2):
		    print "INSERT INTO ParameterSets (superId, name, tracked) VALUES (" + str(newparamsetid) + ", '" + pset + "', " + psettracked + ")"
		thecursor.execute("INSERT INTO ParameterSets (superId, name, tracked) VALUES (" + str(newparamsetid) + ", '" + pset + "', " + psettracked + ")")

                psetsuperiddict[pset+str(newsuperid)] = newparamsetid

		# Each new top level PSet points to the framework component
		if(psetnesting == 'None' or psetnesting == ''):
		    # Attach the PSet to a Fwk component via their superIds
		    if(self.verbose > 2):
			print "INSERT INTO SuperIdParamSetAssoc (superId, psetId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(newparamsetid) + ", " + str(psetpsetseq) + ")"
                    thecursor.execute("INSERT INTO SuperIdParamSetAssoc (superId, psetId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(newparamsetid) + ", " + str(psetpsetseq) + ")")
                    #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=newsuperid,bindvar2=newparamsetid,bindvar3=psetpsetseq)
                   
		# Nested PSets point to the relevant top level PSet 
		else:
		    # Attach the PSet to another PSet component via their superIds
                    # We can't rely on Oracle to keep the ordering on 2-node machines, so get the parent association
                    # from an internal dictionary instead...
                    #		    if(self.verbose > 2):
                    #			print "SELECT ParameterSets.superId FROM ParameterSets WHERE (name = '" + psetnesting + "') ORDER BY ParameterSets.superId DESC"
                    #		    thecursor.execute("SELECT ParameterSets.superId FROM ParameterSets WHERE (name = '" + psetnesting + "') ORDER BY ParameterSets.superId DESC")
                    #
                    #		    testtoplevelid = thecursor.fetchone()[0]

                    toplevelid = psetsuperiddict[psetnesting + str(newsuperid)]

                    if(psetnesting in lastpsetseqdict):
                        psetpsetseq = lastpsetseqdict[psetnesting]
                    else:
                        psetpsetseq = 0

		    lastpsetseqdict[psetnesting] = psetpsetseq + 1
		    lastpsetseqdict[pset] = localseqcount

		    if(self.verbose > 2):
			print "INSERT INTO SuperIdParamSetAssoc (superId, psetId, sequenceNb) VALUES (" + str(toplevelid) + ", " + str(newparamsetid) + ", " + str(psetpsetseq) + ")"
                    thecursor.execute("INSERT INTO SuperIdParamSetAssoc (superId, psetId, sequenceNb) VALUES (" + str(toplevelid) + ", " + str(newparamsetid) + ", " + str(psetpsetseq) + ")")   
                        #                    thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=toplevelid,bindvar2=newparamsetid,bindvar3=psetpsetseq)
                   
	    else:
		if(pset in lastpsetseqdict):
		    localseqcount = lastpsetseqdict[pset]
		else:
		    localseqcount = 0		    
		    lastpsetseqdict[pset] = localseqcount

	    # Now make new entries for each parameter in this PSet if they exist
	    if(psettype == '' or psetname == ''):
		continue

	    if(psettype == "int" or psettype == "int32_t"):
		psettype = "int32"
            if(psettype == "long" or psettype == "int64_t"):
                psettype = "int64"
	    if(psettype == "uint32_t" or psettype == "unsigned int" or psettype == "uint"):
		psettype = "uint32"
            if(psettype == "uint64_t" or psettype == "unsigned long" or psettype == "ulong"):
                psettype = "uint64"
#	    if(psettype == "FileInPath"):
#		psettype = "string"
	    if(psettype == "vunsigned"):
		psettype = "vuint32"
            if(psettype == "vlong"):
                psettype = "vint64"

	    if(not (psettype in self.paramtypedict)):
		continue

	    type = self.paramtypedict[psettype]

	    psetseq = localseqcount
	    localseqcount = localseqcount + 1
	    lastpsetseqdict[pset] = localseqcount

            newparamsetid = psetsuperiddict[pset+str(newsuperid)]

	    # Fill Parameters table
	    newparammemberid = self.AddNewParam(thecursor,newparamsetid,psetname,type,psettracked,psetseq)	    
	    if(psetval == ''):
		continue

	    if(psettype == "int32" or psettype == "int" or psettype == "int32_t"):
		# Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.
		if(psetval):
		    if(psetval.find('.') != -1):
			psetval = str(int(float(psetval)))
		    elif(not psetval.isdigit()):
			psetval = None

		if(psetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(psetval).find("X") == -1 and str(psetval).find("x") == -1):                        
                        thecursor.execute("INSERT INTO Int32ParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
                    else:
                        psetval = str(int(str(psetval), 16))                       
                        thecursor.execute("INSERT INTO Int32ParamValues (paramId, value, hex) VALUES (" + str(newparammemberid) + ", " + psetval + ", 1)") 
	    elif(psettype == "uint32" or psettype == "unsigned int" or psettype == "uint32_t" or psettype == "uint"):
		if(str(psetval).endswith("U")):
		    psetval = (str(psetval).rstrip("U"))

		# Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.
		if(psetval):
		    if(psetval.find('.') != -1):
			psetval = str(int(float(psetval)))
		    elif(not psetval.isdigit()):
			psetval = None

		if(psetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(psetval).find("X") == -1 and str(psetval).find("x") == -1):
                        thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
                    else:
                        psetval = str(int(str(psetval), 16))                       
                        thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value, hex) VALUES (" + str(newparammemberid) + ", " + psetval + ", 1)")

	    elif(psettype == "int64" or psettype == "long" or psettype == "int64_t"):
		# Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.
		if(psetval):
		    if(psetval.find('.') != -1):
			psetval = str(int(float(psetval)))
		    elif(not psetval.isdigit()):
			psetval = None

		if(psetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(psetval).find("X") == -1 and str(psetval).find("x") == -1):                        
                        thecursor.execute("INSERT INTO Int64ParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
                    else:
                        psetval = str(int(str(psetval), 16))                       
                        thecursor.execute("INSERT INTO Int64ParamValues (paramId, value, hex) VALUES (" + str(newparammemberid) + ", " + psetval + ", 1)") 
	    elif(psettype == "uint64" or psettype == "unsigned long" or psettype == "uint64_t" or psettype == "ulong"):
		if(str(psetval).endswith("U")):
		    psetval = (str(psetval).rstrip("U"))

		# Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.
		if(psetval):
		    if(psetval.find('.') != -1):
			psetval = str(int(float(psetval)))
		    elif(not psetval.isdigit()):
			psetval = None

		if(psetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(psetval).find("X") == -1 and str(psetval).find("x") == -1):
                        thecursor.execute("INSERT INTO UInt64ParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
                    else:
                        psetval = str(int(str(psetval), 16))                       
                        thecursor.execute("INSERT INTO UInt64ParamValues (paramId, value, hex) VALUES (" + str(newparammemberid) + ", " + psetval + ", 1)")

	    elif(psettype == "bool"):
                print "psetval = " + str(psetval)
                boolval = str(psetval).strip('"').strip()
                if(boolval == "true"):
                    psetval = str(1)
                if(boolval == "false"):
                    psetval = str(0)

                if(psetval != "0" and psetval != "1"):
                    psetval = None                                                    

                else:
                    if(self.verbose > 2):
                        print "INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")"

                    thecursor.execute("INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")

	    elif(psettype == "double"):
		if(psetval):
		    if(psetval.find('.') == -1 and (not psetval.isdigit())):
			psetval = None

		if(psetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(self.verbose > 2):
                        print "INSERT INTO DoubleParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")" 
                    if(isinstance(psetval, (int, long, float, complex))):
                        thecursor.execute("INSERT INTO DoubleParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
	    elif(psettype == "string"):
		if(psetval.find("'") != -1):
		    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
		elif(psetval.find('"') != -1):
		    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", '" + psetval + "')")
		else:
                    psetval = "'" + str(psetval) + "'"
                    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
                    #		    print "\tWarning: Attempted to load a non-string value to string table:"
                    #		    print "\t\tstring " + str(psetname) + " = " + str(psetval)
                    #		    print "\t\tLoading parameter with no default value"
	    elif(psettype == "FileInPath"):
		if(psetval.find("'") != -1):
		    thecursor.execute("INSERT INTO FileInPathParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
		elif(psetval.find('"') != -1):
		    thecursor.execute("INSERT INTO FileInPathParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", '" + psetval + "')")
		else:
		    print "\tWarning: Attempted to load a non-string value to FileInPath table:"
		    print "\t\tstring " + str(psetname) + " = " + str(psetval)
		    print "\t\tLoading parameter with no default value"
	    elif(psettype == "InputTag"):
		if(psetval.find("'") != -1):
		    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
		else:
		    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", '" + psetval + "')")
	    elif(psettype == "vint32"):
		sequencer = 0
		entries = psetval.lstrip().rstrip().lstrip('{').rstrip('}').split(',')
		for entry in entries:
                    if(entry.lstrip().rstrip().find("X") == -1 and entry.lstrip().rstrip().find("x") == -1): 
                        thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ")")
                    else:
                        entry = str(int(entry.lstrip().rstrip(), 16))                        
                        thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ", 1)")
		    sequencer = sequencer + 1	
	    elif(psettype == "vunsigned" or psettype == "vuint32"):
		sequencer = 0
		entries = psetval.lstrip().rstrip().lstrip('{').rstrip('}').split(',')
		for entry in entries:
                    if(entry.lstrip().rstrip().find("X") == -1 and entry.lstrip().rstrip().find("x") == -1):  
                        thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ")")   
                    else:
                        entry = str(int(entry.lstrip().rstrip(), 16))                        
                        thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ", 1)")
		    sequencer = sequencer + 1
	    elif(psettype == "vint64" or psettype == "vlong"):
		sequencer = 0
		entries = psetval.lstrip().rstrip().lstrip('{').rstrip('}').split(',')
		for entry in entries:
                    if(entry.lstrip().rstrip().find("X") == -1 and entry.lstrip().rstrip().find("x") == -1): 
                        thecursor.execute("INSERT INTO VInt64ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ")")
                    else:
                        entry = str(int(entry.lstrip().rstrip(), 16))                        
                        thecursor.execute("INSERT INTO VInt64ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ", 1)")
		    sequencer = sequencer + 1	
	    elif(psettype == "vuint64"):
		sequencer = 0
		entries = psetval.lstrip().rstrip().lstrip('{').rstrip('}').split(',')
		for entry in entries:
                    if(entry.lstrip().rstrip().find("X") == -1 and entry.lstrip().rstrip().find("x") == -1):  
                        thecursor.execute("INSERT INTO VUInt64ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ")")   
                    else:
                        entry = str(int(entry.lstrip().rstrip(), 16))                        
                        thecursor.execute("INSERT INTO VUInt64ParamValues (paramId, sequenceNb, value, hex) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ", 1)")
		    sequencer = sequencer + 1	                    
	    elif(psettype == "vdouble"):
		sequencer = 0
		entries = psetval.lstrip().rstrip().lstrip('{').rstrip('}').split(',')
		for entry in entries:
		    thecursor.execute("INSERT INTO VDoubleParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ")")   
		    sequencer = sequencer + 1
	    elif(psettype == "vstring" or psettype == "vString"):
		sequencer = 0
		entries = psetval.lstrip().rstrip().lstrip('{').rstrip('}').split(',')
		for entry in entries:
		    thecursor.execute("INSERT INTO VStringParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", '" + entry.lstrip().rstrip() + "')")   
		    sequencer = sequencer + 1		
	    elif(psettype == "VInputTag" or psettype == "vtag"):
		sequencer = 0
		entries = psetval.lstrip().rstrip().lstrip('{').rstrip('}').split(',')
		for entry in entries:
		    thecursor.execute("INSERT INTO VInputTagParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ")")   
		    sequencer = sequencer + 1	


	# Now VPSets
	vpsetcache = []

	for vpset, vpsettype, vpsetname, vpsetval, vpsettracked, vpsetindex, vpsetseq, vpsetpsetseq in vecparamsets:
	    # If this is the first entry in this VPSet for this component, add it to the ParameterSets table
	    if(not vpset in vpsetcache):
		vpsetcache.append(vpset)

		vpsetpsetseq = self.globalseqcount
		self.globalseqcount = self.globalseqcount + 1
		localseqcount = 0

                if(vpsettracked == 'true'):
                    vpsettracked = str(1)
                elif(vpsettracked == 'false'):
                    vpsettracked = str(0)

		# Each new VPSet gets a new SuperId
		thecursor.execute("INSERT INTO SuperIds VALUES('')")
#		thecursor.execute("SELECT LAST_INSERT_ID()")

#		thecursor.execute("SELECT superId FROM SuperIds ORDER BY superId DESC")
                thecursor.execute("SELECT SuperId_Sequence.currval from dual")        
		newvparamsetid = thecursor.fetchone()[0]	

		# Add a new VPSet
		if(self.verbose > 2):
		    print "INSERT INTO VecParameterSets (superId, name, tracked) VALUES (" + str(newvparamsetid) + ", '" + vpset + "', " + vpsettracked + ")"
		thecursor.execute("INSERT INTO VecParameterSets (superId, name, tracked) VALUES (" + str(newvparamsetid) + ", '" + vpset + "', " + vpsettracked + ")")

		# Attach the PSet to a Fwk component via their superIds
		if(self.verbose > 2):
		    print "INSERT INTO SuperIdVecParamSetAssoc (superId, vpsetId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(newvparamsetid) + ", " + str(vpsetpsetseq) + ")"
		thecursor.execute("INSERT INTO SuperIdVecParamSetAssoc (superId, vpsetId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(newvparamsetid) + ", " + str(vpsetpsetseq) + ")")

	    lastvpsetname = vpset

	    # Now make new entries for each parameter in this VPSet if they exist
	    if(vpsettype == '' or vpsetname == ''):
		continue

	    if(vpsettype == "int" or vpsettype == "int32_t"):
		vpsettype = "int32"
	    if(vpsettype == "uint32_t" or vpsettype == "unsigned int" or vpsettype == "uint"):
		vpsettype = "uint32"
#	    if(vpsettype == "FileInPath"):
#		vpsettype = "string"
	    if(vpsettype == "vunsigned"):
		vpsettype = "vuint32"
            if(vpsettype == "int64_t" or vpsettype == "long"):
                vpsettype = "int64"
            if(vpsettype == "uint64_t" or vpsettype == "unsigned long"):
                vpsettype = "uint64"
            if(vpsettype == "vlong"):
                vpsettype = "vint64"

	    type = self.paramtypedict[vpsettype]

	    vpsetseq = localseqcount
	    localseqcount = localseqcount + 1
	    lastpsetseqdict[vpset] = localseqcount

	    # Fill Parameters table
	    newvparammemberid = self.AddNewParam(thecursor,newvparamsetid,vpsetname,type,vpsettracked,vpsetseq)	    

	    if(vpsettype == "int32" or vpsettype == "int" or vpsettype == "int32_t"):
		# Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.
		if(vpsetval):
		    if(vpsetval.find('.') != -1):
			vpsetval = str(int(float(vpsetval)))
		    elif(not vpsetval.isdigit()):
			vpsetval = None

		if(vpsetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(vpsetval).find("X") == -1 and str(vpsetval).find("x") == -1):
                        thecursor.execute("INSERT INTO Int32ParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
                    else:
                        vpsetval = str(int(vpsetval, 16))                        
                        thecursor.execute("INSERT INTO Int32ParamValues (paramId, value, hex) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ", 1)")
	    elif(vpsettype == "uint32" or vpsettype == "unsigned int" or vpsettype == "uint32_t" or vpsettype == "uint"):
		if(vpsetval):
		    if(str(vpsetval).endswith("U")):
			vpsetval = (str(vpsetval).rstrip("U"))

		    # Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.		
		    if(vpsetval.find('.') != -1):
			vpsetval = str(int(float(vpsetval)))
		    elif(not vpsetval.isdigit()):
			vpsetval = None

		if(vpsetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(vpsetval).find("X") == -1 and str(vpsetval).find("x") == -1): 
                        thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
                    else:
                        vpsetval = str(int(vpsetval, 16))                        
                        thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value, hex) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ", 1)")

	    elif(vpsettype == "int64" or vpsettype == "long" or vpsettype == "int64_t"):
		# Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.
		if(vpsetval):
		    if(vpsetval.find('.') != -1):
			vpsetval = str(int(float(vpsetval)))
		    elif(not vpsetval.isdigit()):
			vpsetval = None

		if(vpsetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(vpsetval).find("X") == -1 and str(vpsetval).find("x") == -1):
                        thecursor.execute("INSERT INTO Int64ParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
                    else:
                        vpsetval = str(int(vpsetval, 16))                        
                        thecursor.execute("INSERT INTO Int64ParamValues (paramId, value, hex) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ", 1)")
	    elif(vpsettype == "uint64" or vpsettype == "unsigned long" or vpsettype == "uint64_t" or vpsettype == "ulong"):
		if(vpsetval):
		    if(str(vpsetval).endswith("U")):
			vpsetval = (str(vpsetval).rstrip("U"))

		    # Protect against loading non-integer values. Also deal with implicit fp->int conversions and hex.		
		    if(vpsetval.find('.') != -1):
			vpsetval = str(int(float(vpsetval)))
		    elif(not vpsetval.isdigit()):
			vpsetval = None

		if(vpsetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
                    if(str(vpsetval).find("X") == -1 and str(vpsetval).find("x") == -1): 
                        thecursor.execute("INSERT INTO UInt64ParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
                    else:
                        vpsetval = str(int(vpsetval, 16))                        
                        thecursor.execute("INSERT INTO UInt64ParamValues (paramId, value, hex) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ", 1)") 
                        
	    elif(vpsettype == "bool"):
                boolval = str(vpsetval).strip('"').strip()
                if(boolval == "true"):
                    vpsetval = str(1)
                if(boolval == "false"):
                    vpsetval = str(0)

                if(vpsetval != "0" and vpsetval != "1"):
                    vpsetval = None

                else:
                    thecursor.execute("INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")

	    elif(vpsettype == "double"):
		if(vpsetval):
		    if(vpsetval.find('.') == -1 and (not vpsetval.isdigit())):
			vpsetval = None

		if(vpsetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
		    thecursor.execute("INSERT INTO DoubleParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
	    elif(vpsettype == "string"):
		if(vpsetval.find("'") != -1):
		    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
		elif(vpsetval.find('"') != -1):
		    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", '" + vpsetval + "')")
		else:
                    vpsetval = "'" + str(vpsetval) + "'"
                    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
                    #		    print "\tWarning: Attempted to load a non-string value to string table:"
                    #		    print "\t\tstring " + str(vpsetname) + " = " + str(vpsetval)
                    #		    print "\t\tLoading parameter with no default value" 
	    elif(vpsettype == "FileInPath"):
		if(vpsetval.find("'") != -1):
		    thecursor.execute("INSERT INTO FileInPathParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
		elif(vpsetval.find('"') != -1):
		    thecursor.execute("INSERT INTO FileInPathParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", '" + vpsetval + "')")
		else:
		    print "\tWarning: Attempted to load a non-string value to FileInPath table:"
		    print "\t\tstring " + str(vpsetname) + " = " + str(vpsetval)
		    print "\t\tLoading parameter with no default value" 

	    elif(vpsettype == "InputTag"):
		if(vpsetval.find("'") != -1):
		    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
		else:
		    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", '" + vpsetval + "')")

    # End ConfdbAttachParameterSets

    # Add package and subsystem information
    def ConfdbInsertPackageSubsystem(self,thecursor,thesubsystem,thepackage):
        if(self.verbose > 2):
            print "SELECT SoftwareSubsystems.subsysId FROM SoftwareSubsystems WHERE (SoftwareSubsystems.name = '" + thesubsystem + "')"

        # Insert the subsystem if it doesn't yet exist
        thecursor.execute("SELECT SoftwareSubsystems.subsysId FROM SoftwareSubsystems WHERE (SoftwareSubsystems.name = '" + thesubsystem + "')")
        subsys = thecursor.fetchone()
        if(subsys):
            subsys = subsys[0]

        if(subsys == None):
            if(self.verbose > 2):
                print "INSERT INTO SoftwareSubsystems (name) VALUES ('" + str(thesubsystem) + "')"

            thecursor.execute("INSERT INTO SoftwareSubsystems (name) VALUES ('" + str(thesubsystem) + "')")
#            thecursor.execute("SELECT LAST_INSERT_ID()")
#	    thecursor.execute("SELECT subsysId FROM SoftwareSubsystems ORDER BY subsysId DESC")
            thecursor.execute("SELECT SubsysId_Sequence.currval from dual")
                
            subsys = thecursor.fetchone()
            if(subsys):
                subsys = subsys[0]

        if(self.verbose > 2):
            print "SELECT SoftwarePackages.packageId FROM SoftwarePackages JOIN SoftwareSubsystems ON (SoftwarePackages.subsysId = " + str(subsys) + ") WHERE (SoftwarePackages.name = '" + str(thepackage) + "')"
        thecursor.execute("SELECT SoftwarePackages.packageId FROM SoftwarePackages JOIN SoftwareSubsystems ON (SoftwarePackages.subsysId = " + str(subsys) + ") WHERE (SoftwarePackages.name = '" + str(thepackage) + "')")
                               
        pack = thecursor.fetchone()

        if(pack):
            pack = pack[0]

        if(pack == None):
            if(self.verbose > 2):
                print "INSERT INTO SoftwarePackages (name,subsysId) VALUES ('" + str(thepackage) + "', " + str(subsys) + ")"

            thecursor.execute("INSERT INTO SoftwarePackages (name,subsysId) VALUES ('" + str(thepackage) + "', " + str(subsys) + ")")
#	    thecursor.execute("SELECT packageId FROM SoftwarePackages ORDER BY packageId DESC")
            thecursor.execute("SELECT PackageId_Sequence.currval from dual")            
	    pack = thecursor.fetchone()
	    if(pack):
	        pack = pack[0]

	return pack

    # Now just attach all non-updated old templates to new release
    def ConfdbReassociateTemplates(self,thecursor,oldrelease,newrelease,modifiedtemplates):
	thecursor.execute("SELECT SoftwareReleases.releaseId FROM SoftwareReleases WHERE (SoftwareReleases.releaseTag = '" + oldrelease + "')")
        oldrelid = (thecursor.fetchone())[0]
	newrelid = self.releasekey
	thecursor.execute("SELECT SuperIds.superId FROM SuperIds JOIN SuperIdReleaseAssoc ON (SuperIds.superId = SuperIdReleaseAssoc.superId) WHERE (SuperIdReleaseAssoc.releaseId = '" + str(oldrelid) + "')")
	superidtuple = thecursor.fetchall()
	print "Remapping existing templates from release " + oldrelease + " to new intermediate release called " + newrelease
	for superidentry in superidtuple:	    
	    superid = superidentry[0]

	    matches = ''
	    thecursor.execute("SELECT ModuleTemplates.name FROM ModuleTemplates WHERE (ModuleTemplates.superId = '" + str(superid) + "')")
	    tempname = thecursor.fetchone()
	    if(tempname):
		matches = tempname[0]

	    thecursor.execute("SELECT ServiceTemplates.name FROM ServiceTemplates WHERE (ServiceTemplates.superId = '" + str(superid) + "')")
	    tempname = thecursor.fetchone()
	    if(tempname):
		matches = tempname[0]

	    thecursor.execute("SELECT ESSourceTemplates.name FROM ESSourceTemplates WHERE (ESSourceTemplates.superId = '" + str(superid) + "')")
	    tempname = thecursor.fetchone()
	    if(tempname):
		matches = tempname[0]

	    thecursor.execute("SELECT EDSourceTemplates.name FROM EDSourceTemplates WHERE (EDSourceTemplates.superId = '" + str(superid) + "')")
	    tempname = thecursor.fetchone()
	    if(tempname):
		matches = tempname[0]

	    thecursor.execute("SELECT ESModuleTemplates.name FROM ESModuleTemplates WHERE (ESModuleTemplates.superId = '" + str(superid) + "')")
	    tempname = thecursor.fetchone()
	    if(tempname):
		matches = tempname[0]

	    if(not (matches in modifiedtemplates)):
		thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(superid) + ", " + str(newrelid) + ")")
	    print ".",
	print "\n"

    # Utility function for adding a new parameter 
    def AddNewParam(self,thecursor,sid,pname,ptype,ptracked,pseq):
        if(ptracked == 'true'):
            ptracked = str(1)
        elif(ptracked == 'false'):
            ptracked = str(0)

        if(pname == '' or pname == "" or pname == None):
            pname = "None"

	if(self.verbose > 2):
	    print "INSERT INTO Parameters (paramTypeId, name, tracked) VALUES (" + str(ptype) + ", '" + pname + "', " + ptracked + ")"

	thecursor.execute("INSERT INTO Parameters (paramTypeId, name, tracked) VALUES (" + str(ptype) + ", '" + pname + "', " + ptracked + ")")
	
#	thecursor.execute("SELECT LAST_INSERT_ID()")

#	thecursor.execute("SELECT paramId FROM Parameters ORDER BY paramId DESC")
        thecursor.execute("SELECT ParamId_Sequence.currval from dual")            
	newparamid = thecursor.fetchone()[0]

	# Fill Parameter <-> Super ID table
	if(self.verbose > 2):
	    print "INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(pseq) + ")"
            thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(pseq) + ")")
            #        thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (:bindvar1, :bindvar2, :bindvar3)", bindvar1=sid,bindvar2=newparamid,bindvar3=pseq)
                   
	return newparamid

    # Utility function for returning the paramId of a parameter
    def RetrieveParamId(self,thecursor,pname,sid):
	thecursor.execute("SELECT SuperIdParameterAssoc.paramId FROM SuperIdParameterAssoc JOIN Parameters ON (Parameters.name = '" + pname + "') WHERE (SuperIdParameterAssoc.superId = " + str(sid) + ") AND (SuperIdParameterAssoc.paramId = Parameters.paramId)")
	
	oldparamid = thecursor.fetchone()

	if(self.verbose > 2):
	    print "SELECT SuperIdParameterAssoc.paramId FROM SuperIdParameterAssoc JOIN Parameters ON (Parameters.name = '" + pname + "') WHERE (SuperIdParameterAssoc.superId = " + str(sid) + ") AND (SuperIdParameterAssoc.paramId = Parameters.paramId)"
	    if(oldparamid):	    
		print "Old param id was " + str(oldparamid[0])

	if(oldparamid):
	    return oldparamid[0]
	else:
	    return oldparamid

    # Utility function for comparing two lists ("vectors").
    def CompareVectors(self,vec1,vec2):
	# If the old & new parameter vectors have different #'s of elements 
	# it's easy - they don't match
	if(len(vec1) != len(vec2)):
	    if(self.verbose > 2):
		print "Vectors are of different lengths"
	    return False

	else:
	    if(vec1 != vec2):
		if(self.verbose > 2):
		    print "Vectors have changed"
		    print vec1
		    print vec2
		return False

	if(self.verbose > 2):
	    print "Vectors are unchanged"
	    print vec1
	    print vec2
	return True

    # Set the verbosity
    def SetVerbosity(self, verbosity):
	self.verbose = verbosity

    def PrintStats(self):
	print "\tAdded " + str(self.fwknew) + " new framework components to the DB" 
	print "\t" + str(self.fwkchanged) + " framework components were updated"
	print "\t" + str(self.fwkunchanged)  + " framework components were unchanged from the previous release"

    # All done. Clean up and commit changes (necessary for INNODB engine)
    def ConfdbExitGracefully(self):
	self.connection.commit()
	self.connection.close()
