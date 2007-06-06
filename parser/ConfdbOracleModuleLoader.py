#!/usr/bin/env python

# ConfdbOracleModuleLoader.py
# Interface for loading module templates to the Conf DB
# (Oracle version). All Oracle specific code belongs here.
# Jonathan Hollar LLNL May. 11, 2007

import os, string, sys, posix, tokenize, array

sys.path.append(os.environ.get("CMS_PATH") + "/sw/slc4_ia32_gcc345/external/py2-cx-oracle/4.2/lib/python2.4/site-packages/")

import cx_Oracle

class ConfdbOracleModuleLoader:

    def __init__(self, verbosity):
	self.data = []
	self.changes = []
        self.paramtypedict = {}
        self.modtypedict = {}
	self.releasekey = -1
	self.verbose = int(verbosity)
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

	thecursor.execute("SELECT LAST_INSERT_ID()")

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
	if(self.verbose > 2):
	    print "SELECT EDSourceTemplates.superId FROM EDSourceTemplates WHERE (EDSourceTemplates.name = '" + srcname + "')"
	thecursor.execute("SELECT EDSourceTemplates.superId FROM EDSourceTemplates WHERE (EDSourceTemplates.name = '" + srcname + "')")

	srcsuperid = thecursor.fetchone()

        if(srcsuperid):
            return srcsuperid
        else:
            return 0
        
        return srcsuperid

               
    # Create a new module template in the DB
    def ConfdbLoadNewModuleTemplate(self,thecursor,modclassname,modbaseclass,modcvstag,parameters,vecparameters,paramsets,vecparamsets):
	
	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUE('');")

	thecursor.execute("SELECT LAST_INSERT_ID()")

	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Get the module type (base class)
	modbaseclassid = self.modtypedict[modbaseclass]

	# Now create a new module
	thecursor.execute("INSERT INTO ModuleTemplates (superId, typeId, name, cvstag) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + modclassname + "', '" + modcvstag + "')")
	if(self.verbose > 2):
	    print "INSERT INTO ModuleTemplates (superId, typeId, name, cvstag) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + modclassname + "', '" + modcvstag + "')"
	
	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbLoadNewModuleTemplate
	
    # Create a new service template in the DB
    def ConfdbLoadNewServiceTemplate(self,thecursor,servclassname,servcvstag,parameters,vecparameters,paramsets,vecparamsets):

	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUE('');")

	thecursor.execute("SELECT LAST_INSERT_ID()")

	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Now create a new service
	thecursor.execute("INSERT INTO ServiceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '" + servclassname + "', '" + servcvstag + "')")
	if(self.verbose > 2):
	    print "INSERT INTO ServiceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '" + servclassname + "', '" + servcvstag + "')"
	
	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbLoadNewServiceTemplate

    # Create a new es_source template in the DB
    def ConfdbLoadNewESSourceTemplate(self,thecursor,srcclassname,srccvstag,parameters,vecparameters,paramsets,vecparamsets):
	
	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUE('');")

	thecursor.execute("SELECT LAST_INSERT_ID()")

	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Now create a new es_source
	thecursor.execute("INSERT INTO ESSourceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '" + srcclassname + "', '" + srccvstag + "')")
	if(self.verbose > 2):
	    print "INSERT INTO ESSourceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '" + srcclassname + "', '" + srccvstag + "')"
	
	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbLoadNewESSourceTemplate

    # Create a new ed_source template in the DB
    def ConfdbLoadNewEDSourceTemplate(self,thecursor,srcclassname,srccvstag,parameters,vecparameters,paramsets,vecparamsets):
	
	self.fwknew = self.fwknew + 1

	# Allocate a new SuperId
	newsuperid = -1
	thecursor.execute("INSERT INTO SuperIds VALUE('');")

	thecursor.execute("SELECT LAST_INSERT_ID()")

	newsuperid = (thecursor.fetchall()[0])[0]

	# Attach this template to the currect release
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Now create a new es_source
	thecursor.execute("INSERT INTO EDSourceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '" + srcclassname + "', '" + srccvstag + "')")
	if(self.verbose > 2):
	    print "INSERT INTO EDSourceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '" + srcclassname + "', '" + srccvstag + "')"
	
	# Now deal with parameters
	self.ConfdbAttachParameters(thecursor,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbLoadNewEDSourceTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateModuleTemplate(self,thecursor,modclassname,modbaseclass,modcvstag,parameters,vecparameters,paramsets,vecparamsets):

	# Get the SuperId of the previous version of this template
	thecursor.execute("SELECT ModuleTemplates.superId, ModuleTemplates.cvstag FROM ModuleTemplates WHERE (ModuleTemplates.name = '" + modclassname + "') ORDER BY ModuleTemplates.superId DESC")
	oldmodule = thecursor.fetchone()
	oldsuperid = oldmodule[0]
	oldtag = oldmodule[1]
	print '\tOld module had tag' + ' ' + oldtag + ', new module has tag ' + modcvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if(oldtag == modcvstag):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this module is unchanged - attach old template to new release'
	    if(self.verbose > 0):
		print 'New releaseId = ' + str(self.releasekey)
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUE('');")
	thecursor.execute("SELECT LAST_INSERT_ID()")
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Get the module type (base class)
	modbaseclassid = self.modtypedict[modbaseclass]

	# Now create a new module
	thecursor.execute("INSERT INTO ModuleTemplates (superId, typeId, name, cvstag) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + modclassname + "', '" + modcvstag + "')")
	if(self.verbose > 2):
	    print "INSERT INTO ModuleTemplates (superId, typeId, name, cvstag) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + modclassname + "', '" + modcvstag + "')"
	
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateModuleTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateServiceTemplate(self,thecursor,servclassname,servcvstag,parameters,vecparameters,paramsets,vecparamsets):

	# Get the SuperId of the previous version of this template
	thecursor.execute("SELECT ServiceTemplates.superId, ServiceTemplates.cvstag FROM ServiceTemplates WHERE (ServiceTemplates.name = '" + servclassname + "') ORDER BY ServiceTemplates.superId DESC")
	oldservice = thecursor.fetchone()
	oldsuperid = oldservice[0]
	oldtag = oldservice[1]
	print 'Old service had ' + ' ' + oldtag + ', new service has tag ' + servcvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if(oldtag == servcvstag):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this service is unchanged - attach old template to new release'
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUE('');")
	thecursor.execute("SELECT LAST_INSERT_ID()")
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	print 'New service has ' + str(newsuperid) + ' ' + servcvstag

	# Now create a new service
	thecursor.execute("INSERT INTO ServiceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '"  + servclassname + "', '" + servcvstag + "')")
	
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateServiceTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateESSourceTemplate(self,thecursor,sourceclassname,sourcecvstag,parameters,vecparameters,paramsets,vecparamsets):

	# Get the SuperId of the previous version of this template
	thecursor.execute("SELECT ESSourceTemplates.superId, ESSourceTemplates.cvstag FROM ESSourceTemplates WHERE (ESSourceTemplates.name = '" + sourceclassname + "') ORDER BY ESSourceTemplates.superId DESC")
	oldsource = thecursor.fetchone()
	oldsuperid = oldsource[0]
	oldtag = oldsource[1]
	print 'Old source had tag ' + ' ' + oldtag + ', new source has tag ' + sourcecvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if(oldtag == sourcecvstag):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this source is unchanged - attach old template to new release'
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUE('');")
	thecursor.execute("SELECT LAST_INSERT_ID()")
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Now create a new source
	thecursor.execute("INSERT INTO ESSourceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '" + sourceclassname + "', '" + sourcecvstag + "')")
	
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateESSourceTemplate

    # Given a component, update parameters that have changed from the 
    # templated version
    def ConfdbUpdateEDSourceTemplate(self,thecursor,sourceclassname,sourcecvstag,parameters,vecparameters,paramsets,vecparamsets):

	# Get the SuperId of the previous version of this template
	thecursor.execute("SELECT EDSourceTemplates.superId, EDSourceTemplates.cvstag FROM EDSourceTemplates WHERE (EDSourceTemplates.name = '" + sourceclassname + "') ORDER BY EDSourceTemplates.superId DESC")
	oldsource = thecursor.fetchone()
	oldsuperid = oldsource[0]
	oldtag = oldsource[1]
	print 'Old source had ' + ' ' + oldtag + ', new source has tag ' + sourcecvstag

	# If the template hasn't been updated (with a new CVS tag), 
	# just attach the old template to the new release and exit
	if(oldtag == sourcecvstag):
	    self.fwkunchanged = self.fwkunchanged + 1
	    print 'The CVS tag for this source is unchanged - attach old template to new release'
	    thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(oldsuperid) + ", " + str(self.releasekey) + ")")
	    return

	self.fwkchanged = self.fwkchanged + 1

	# Otherwise allocate a new SuperId for this template and attach 
	# it to the release
	thecursor.execute("INSERT INTO SuperIds VALUE('');")
	thecursor.execute("SELECT LAST_INSERT_ID()")
	newsuperid = (thecursor.fetchall()[0])[0]
	thecursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.releasekey) + ")")

	# Now create a new source
	thecursor.execute("INSERT INTO EDSourceTemplates (superId, name, cvstag) VALUES (" + str(newsuperid) + ", '" + sourceclassname + "', '" + sourcecvstag + "')")
	
	# Now deal with parameters
	self.ConfdbUpdateParameters(thecursor,oldsuperid,newsuperid,parameters,vecparameters)
	self.ConfdbAttachParameterSets(thecursor,newsuperid,paramsets,vecparamsets)

	self.globalseqcount = 0
    # End ConfdbUpdateEDSourceTemplate

    # Associate a list of parameters with a component template (via superId)
    def ConfdbAttachParameters(self,thecursor,newsuperid,parameters,vecparameters):

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
		    thecursor.execute("INSERT INTO Int32ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")

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
		    thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")

	    # bool
	    elif(paramtype == "bool"):
		type = self.paramtypedict['bool']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)   

		# Fill ParameterValues table
		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
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
	    elif(paramtype == "string" or paramtype == "FileInPath"):
		type = self.paramtypedict['string']

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
			thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
		    elif(paramval.find('"') != -1):
			thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")
		    else:
			print "\tWarning: Attempted to load a non-string value to string table:"
			print "\t\tstring " + str(paramname) + " = " + str(paramval)
			print "\t\tLoading parameter with no default value"

	    # InputTag
	    elif(paramtype == "InputTag"):
		type = self.paramtypedict['InputTag']

		# Fill Parameters table
		newparamid = self.AddNewParam(thecursor,newsuperid,paramname,type,paramistracked,paramseq)

		# Fill ParameterValues table
		if(paramval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
		    if(paramval.find("'") != -1):
			thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
		    else:
			thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")

	    else:
		print '\tError: Unknown param type ' + paramtype + ' ' + paramname + ' - do nothing'
	    
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
			thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
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
			thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
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
	    elif(vecptype == "VInputTag" or vecptype == "InputTag"):
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
	
	# First the non-vectors
	for paramtype, paramname, paramval, paramistracked, paramseq in parameters:
	    
	    paramseq = self.globalseqcount
	    self.globalseqcount = self.globalseqcount + 1

	    neednewparam = False

	    oldparamval = None

	    # int32
	    if(paramtype == "int32" or paramtype == "int" or paramtype == "int32_t"):
		type = self.paramtypedict['int32']

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
		    thecursor.execute("SELECT Int32ParamValues.value FROM Int32ParamValues WHERE (Int32ParamValues.paramId = " + str(oldparamid) + ")")

		    oldparamval = thecursor.fetchone()

		    if(oldparamval):
			oldparamval = oldparamval[0]

		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or 
		       (oldparamval == None and paramval == None)):
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
			if(self.verbose > 0):
			    print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

			neednewparam = False

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
			thecursor.execute("INSERT INTO Int32ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + str(paramval) + ")")

	    # uint32
	    if(paramtype == "uint32" or paramtype == "unsigned int" or paramtype == "uint32_t" or paramtype == "uint"):
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
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
			if(self.verbose > 0):
			    print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"
			
			neednewparam = False

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
			thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + str(paramval) + ")")

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

			if(oldparamval == 1):
			    oldparamval = "true"
			if(oldparamval == 0):
			    oldparamval = "false"
		    
		    # No changes. Attach parameter to new template.
		    if((oldparamval == paramval) or 
		       (oldparamval == None and paramval == None)):
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
			if(self.verbose > 0):
			    print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"
			
			neednewparam = False

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
			thecursor.execute("INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")

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
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
			if(self.verbose > 0):
			    print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"
			
			neednewparam = False

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

	    # string
	    if(paramtype == "string" or paramtype == "FileInPath"):
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
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
			if(self.verbose > 0):
			    print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"

			neednewparam = False

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
			    print "\tWarning: Attempted to load a non-string value to string table:"
			    print "\t\tstring " + str(paramname) + " = " + str(paramval)
			    print "\t\tLoading parameter with no default value"

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
		       (oldparamval == None and paramval == None)):
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(paramseq) + ")")
			
			neednewparam = False

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
			if(paramval.find("'") != -1):
			    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", " + paramval + ")")
			else:
			    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newparamid) + ", '" + paramval + "')")

	# Now deal with any vectors
	for vecptype, vecpname, vecpvals, vecpistracked, vecpseq in vecparameters:

	    vecpseq = self.globalseqcount
	    self.globalseqcount = self.globalseqcount + 1

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
			if(self.verbose > 0):
			    print "Parameter is unchanged (" + str(oldparamval) + ", " + str(paramval) + ")"
			
			neednewparam = False

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
			    thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
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
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
			
			neednewparam = False

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
			    thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(sequencer) + ", " + vecpval + ")")   
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
			
			print "vdouble is unchanged"
			neednewparam = False

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
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
			
			neednewparam = False

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
	    elif(vecptype == "VInputTag" or vecptype == "InputTag"):		
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
			thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(oldparamid) + ", " + str(vecpseq) + ")")
			
			neednewparam = False

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

	for pset, psettype, psetname, psetval, psettracked, psetseq, psetnesting, psetpsetseq in paramsets:
	    # If this is the first entry in this PSet for this component, add it to the ParameterSets table
	    if(not pset in psetcache):
		psetcache.append(pset)

		psetpsetseq = self.globalseqcount
		self.globalseqcount = self.globalseqcount + 1
		localseqcount = 0

		thecursor.execute("INSERT INTO SuperIds VALUE('')")
		thecursor.execute("SELECT LAST_INSERT_ID()")
		newparamsetid = thecursor.fetchone()[0]	

		# Add a new PSet
		if(self.verbose > 2):
		    print "INSERT INTO ParameterSets (superId, name, tracked) VALUES (" + str(newparamsetid) + ", '" + pset + "', " + psettracked + ")"
		thecursor.execute("INSERT INTO ParameterSets (superId, name, tracked) VALUES (" + str(newparamsetid) + ", '" + pset + "', " + psettracked + ")")

		# Each new top level PSet points to the framework component
		if(psetnesting == 'None' or psetnesting == ''):
		    # Attach the PSet to a Fwk component via their superIds
		    if(self.verbose > 2):
			print "INSERT INTO SuperIdParamSetAssoc (superId, paramSetId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(newparamsetid) + ", " + str(psetpsetseq) + ")"
		    thecursor.execute("INSERT INTO SuperIdParamSetAssoc (superId, paramSetId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(newparamsetid) + ", " + str(psetpsetseq) + ")")

		# Nested PSets point to the relevant top level PSet 
		else:
		    # Attach the PSet to another PSet component via their superIds
		    if(self.verbose > 2):
			print "SELECT ParameterSets.superId FROM ParameterSets WHERE (name = '" + psetnesting + "') ORDER BY ParameterSets.superId DESC"
		    thecursor.execute("SELECT ParameterSets.superId FROM ParameterSets WHERE (name = '" + psetnesting + "') ORDER BY ParameterSets.superId DESC")

		    toplevelid = thecursor.fetchone()[0]

		    psetpsetseq = lastpsetseqdict[psetnesting]		    
		    lastpsetseqdict[psetnesting] = psetpsetseq + 1
		    lastpsetseqdict[pset] = localseqcount

		    if(self.verbose > 2):
			print "INSERT INTO SuperIdParamSetAssoc (superId, paramSetId, sequenceNb) VALUES (" + str(toplevelid) + ", " + str(newparamsetid) + ", " + str(psetpsetseq) + ")"
		    thecursor.execute("INSERT INTO SuperIdParamSetAssoc (superId, paramSetId, sequenceNb) VALUES (" + str(toplevelid) + ", " + str(newparamsetid) + ", " + str(psetpsetseq) + ")")   
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
	    if(psettype == "uint32_t" or psettype == "unsigned int" or psettype == "uint"):
		psettype = "uint32"
	    if(psettype == "FileInPath"):
		psettype = "string"
	    if(psettype == "vunsigned"):
		psettype = "vuint32"

	    if(not (psettype in self.paramtypedict)):
		continue

	    type = self.paramtypedict[psettype]

	    psetseq = localseqcount
	    localseqcount = localseqcount + 1
	    lastpsetseqdict[pset] = localseqcount

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
		    thecursor.execute("INSERT INTO Int32ParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
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
		    thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")

	    elif(psettype == "bool"):
		thecursor.execute("INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
	    elif(psettype == "double"):
		if(psetval):
		    if(psetval.find('.') == -1 and (not psetval.isdigit())):
			psetval = None

		if(psetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
		    thecursor.execute("INSERT INTO DoubleParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
	    elif(psettype == "string" or psettype == "FileInPath"):
		if(psetval.find("'") != -1):
		    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", " + psetval + ")")
		elif(psetval.find('"') != -1):
		    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newparammemberid) + ", '" + psetval + "')")
		else:
		    print "\tWarning: Attempted to load a non-string value to string table:"
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
		    thecursor.execute("INSERT INTO VInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ")")   
		    sequencer = sequencer + 1	
	    elif(psettype == "vunsigned" or psettype == "vuint32"):
		sequencer = 0
		entries = psetval.lstrip().rstrip().lstrip('{').rstrip('}').split(',')
		for entry in entries:
		    thecursor.execute("INSERT INTO VUInt32ParamValues (paramId, sequenceNb, value) VALUES (" + str(newparammemberid) + ", " + str(sequencer) + ", " + entry.lstrip().rstrip() + ")")   
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
	    elif(psettype == "VInputTag"):
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

		# Each new VPSet gets a new SuperId
		thecursor.execute("INSERT INTO SuperIds VALUE('')")
		thecursor.execute("SELECT LAST_INSERT_ID()")
		newvparamsetid = thecursor.fetchone()[0]	

		# Add a new VPSet
		if(self.verbose > 2):
		    print "INSERT INTO VecParameterSets (superId, name, tracked) VALUES (" + str(newvparamsetid) + ", '" + vpset + "', " + vpsettracked + ")"
		thecursor.execute("INSERT INTO VecParameterSets (superId, name, tracked) VALUES (" + str(newvparamsetid) + ", '" + vpset + "', " + vpsettracked + ")")

		# Attach the PSet to a Fwk component via their superIds
		if(self.verbose > 2):
		    print "INSERT INTO SuperIdVecParamSetAssoc (superId, vecParamSetId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(newvparamsetid) + ", " + str(vpsetpsetseq) + ")"
		thecursor.execute("INSERT INTO SuperIdVecParamSetAssoc (superId, vecParamSetId, sequenceNb) VALUES (" + str(newsuperid) + ", " + str(newvparamsetid) + ", " + str(vpsetpsetseq) + ")")

	    lastvpsetname = vpset

	    # Now make new entries for each parameter in this VPSet if they exist
	    if(vpsettype == '' or vpsetname == ''):
		continue

	    if(vpsettype == "int" or vpsettype == "int32_t"):
		vpsettype = "int32"
	    if(vpsettype == "uint32_t" or vpsettype == "unsigned int" or vpsettype == "uint"):
		vpsettype = "uint32"
	    if(vpsettype == "FileInPath"):
		vpsettype = "string"
	    if(vpsettype == "vunsigned"):
		vpsettype = "vuint32"

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
		    thecursor.execute("INSERT INTO Int32ParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
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
		    thecursor.execute("INSERT INTO UInt32ParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
	    elif(vpsettype == "bool"):
		thecursor.execute("INSERT INTO BoolParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
	    elif(vpsettype == "double"):
		if(vpsetval):
		    if(vpsetval.find('.') == -1 and (not paramval.isdigit())):
			vpsetval = None

		if(vpsetval == None):
		    if(self.verbose > 2):
			print "No default parameter value found"
		else:
		    thecursor.execute("INSERT INTO DoubleParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
	    elif(vpsettype == "string" or vpsettype == "FileInPath"):
		if(vpsetval.find("'") != -1):
		    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
		elif(vpsetval.find('"') != -1):
		    thecursor.execute("INSERT INTO StringParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", '" + vpsetval + "')")
		else:
		    print "\tWarning: Attempted to load a non-string value to string table:"
		    print "\t\tstring " + str(vpsetname) + " = " + str(vpsetval)
		    print "\t\tLoading parameter with no default value" 
	    elif(vpsettype == "InputTag"):
		if(vpsetval.find("'") != -1):
		    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", " + vpsetval + ")")
		else:
		    thecursor.execute("INSERT INTO InputTagParamValues (paramId, value) VALUES (" + str(newvparammemberid) + ", '" + vpsetval + "')")

    # End ConfdbAttachParameterSets

#    # Update a ParameterSet/VParameterSet if necessary
#    def ConfdbUpdateParameterSets(self,thecursor,oldsuperid,newsuperid,paramsets,vecparamsets):
#	print "TBD"

    # End ConfdbUpdateParameterSets

    # Utility function for adding a new parameter 
    def AddNewParam(self,thecursor,sid,pname,ptype,ptracked,pseq):
	if(self.verbose > 2):
	    print "INSERT INTO Parameters (paramTypeId, name, tracked) VALUES (" + str(ptype) + ", '" + pname + "', " + ptracked + ")"

	thecursor.execute("INSERT INTO Parameters (paramTypeId, name, tracked) VALUES ('" + str(ptype) + "', '" + pname + "', " + ptracked + ")")
	
	thecursor.execute("SELECT LAST_INSERT_ID()")
	newparamid = thecursor.fetchone()[0]

	# Fill Parameter <-> Super ID table
	if(self.verbose > 2):
	    print "INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(pseq) + ")"
	thecursor.execute("INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(pseq) + ")")

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
