#!/usr/bin/env python
 
# ConfdbSourceParser.py
# Parse cc files in a release, and identify the modules/parameters 
# that should be loaded as templates in the Conf DB
# Jonathan Hollar LLNL July 10, 2007

import os, string, sys, posix, tokenize, array, re

class SourceParser:
    def __init__(self,verbosity,srctree):
        self.data = []

	# Store parameters(sets) and their default values for this package
        self.paramlist = []
        self.vecparamlist = []
	self.paramsetmemberlist = []
	self.paramsetlist = []
	self.vecparamsetlist = []
	self.vecparamsetmemberlist = []

	# List of parameters where we couldn't find a default value
	self.paramfailures = []

	# Hash table of parameter set variables
	self.psetdict = {}
	self.psetsequences = {}

	self.goodbaseclasses = ['EDProducer','EDFilter','ESProducer','OutputModule','EDAnalyzer','HLTProducer','HLTFilter']

	self.inheritancelevel = 0
	self.includefile = ""
        self.baseclass = ""
	self.sourcetree = srctree
	self.sequencenb = 0
	self.psetsequencenb = 0
	self.mainpset = "None"
	self.verbose = int(verbosity)

    # Parser for .cf* files. Look for default values of tracked parameters.
    # Anything specific to the .cf* file syntax nominally goes here.
    def ParseCfFile(self,thecfidir,themodule,theparam,itsparamset,theblock,thecfifile):

        foundparam = False

	if(itsparamset in self.psetsequences):
	    self.psetsequencenb = self.psetsequences[itsparamset]

        if(os.path.isdir(thecfidir)):
            cfifiles = os.listdir(thecfidir)
            for cfifile in cfifiles:
                # Get all cfi files
		if((cfifile.find('cfi') != -1) and foundparam == False):
		    if(not os.path.isfile(thecfidir+cfifile)):
			continue

		    if(thecfifile and (cfifile != thecfifile)):
			continue
		    
		    filename = open(thecfidir + cfifile)

                    if(self.verbose > 1):
                        print '\t\tChecking cf* file: ' + cfifile + ' for ' + str(theparam) + '(in (V)PSet ' + str(itsparamset) + ')'
        
                    lines = filename.readlines()
        
		    includedcfis = []
                    startedmod = False
                    startedvector = False
		    startedpset = False
		    startednestedpset = False
		    readingvector = False
		    readingpset = False
		    readingnestedpset = False
		    foundpset = False
		    startedvpset = False
		    startedvpsetentry = False
		    readingvpset = False
		    foundvpset = False
		    foundvectorend = False
		    foundpsetparams = 0
		    paramtracked = ""
		    totalvectorline = ""
		    paramtype = ""
		    paramname = ""
		    psetname = ""
		    nestedpsetname = ""
		    toppsetname = ""
		    vpsetindex = 0
 
                    for line in lines:
                        # Tokenize the line
                        vals = string.split(line)
                        
                        # Check that line isn't empty
                        if(vals):

			    # Look for using/block constructs - can appear anywhere in cfi
			    if(line.lstrip().startswith('include ') and line.rstrip().endswith('.cfi"')):
				includecfi = line.split('include')[1].lstrip().rstrip().lstrip('"').rstrip('"')
				includedcfis.append(includecfi)

                            # If we found the start of a module definition, start reading parameters
                            if(startedmod == True):
                                paramcount = 0

    				# Handle comments
				if(line.lstrip().startswith('//') or line.lstrip().startswith('#')):
				    continue

				# Handle inline comments
				if(line.find('#') != -1):
				    line = line.split('#')[0]

				# Handle inline C++ style comments. Argh.
				if(line.find('//') != -1):
				    line = line.split('//')[0]

				# Handle PSets
				if(line.find('PSet') != -1 and line.find('VPSet') == -1):
				    if(startedpset == False):
					toppsetname = (line.split('PSet')[1]).split('=')[0].rstrip().lstrip()
					psetname = toppsetname
					startedpset = True
				    else:
					startednestedpset = True
					nestedpsetname =  (line.split('PSet')[1]).split('=')[0].rstrip().lstrip()

				# Handle VPSets
				if(line.find('VPSet') != -1):
				    if(startedvpset == False):
					startedvpset = True
					vpsetindex = 0
					psetname = (line.split('VPSet')[1]).split('=')[0].rstrip().lstrip()
				if(line.find('}') != -1 and startedvpset == True and startedvpsetentry == False):
				    startedvpset = False
				    vpsetindex = 0
				if(line.find('}') != -1 and startedvpsetentry == True):
				    startedvpsetentry = False
				    vpsetindex = vpsetindex + 1
				if(line.find('{') != -1 and startedvpset == True):
				    startedvpsetentry = True

				# Handle vectors
				if(line.find('vdouble') != -1 or line.find('vint32') != -1 or line.find('vstring') != -1 or
				   line.find('vString') != -1 or line.find('vuint32') != -1 or line.find('VInputTag') != -1):
				    if(startedvector == False):
					startedvector = True

                                # This looks like a parameter declaration
                                if (startedmod == True and line.find('=') != -1 and line.find(theparam) != -1):
				    if(vals[0] == 'untracked'):
					paramtype = vals[1]

					paramname = vals[2]

					paramtracked = "false"
				    else:
					paramtype = vals[0]
                                    
					paramname = vals[1]                   				    
					
					paramtracked = "true"

				    # Sanity check
				    if(not (paramname.lstrip().rstrip() == theparam)):
					continue

				    # Clean up unusual spacing
				    if(paramname.endswith('=')):
				       paramname = paramname.rstrip('=')
				    if(paramname.find('=') != -1):
					paramname = paramname.split('=')[0]

				    if(itsparamset == ''):
					foundparam = True

				    # This is the start of the PSet we're looking for
				    if(startedpset == True):
					foundpset = True
					if(psetname == itsparamset):
					    readingpset = True					
					    foundparam = True

				    # This is the start of the VPSet we're looking for
				    if(startedvpset == True):
					if(paramtype == 'VPSet'):
					    foundvpset = True
					    readingvpset = True
					    foundparam = True

				    if(startednestedpset == True):
					foundnestedpset = True
					if(nestedpsetname == itsparamset):
					    readingnestedpset = True
					    foundparam = True

                                    # This is the start of the vector we're looking for 
                                    if(paramtype == 'vdouble' or
                                       paramtype == 'vint32' or
                                       paramtype == 'vstring' or
				       paramtype == 'vString' or
                                       paramtype == 'vuint32' or
				       paramtype == 'VInputTag'):
					readingvector = True
				    
				    # This is a normal parameter
                                    elif(paramtype != 'PSet' and paramtype != 'VPSet' and readingpset == False 
					 and readingvpset == False and itsparamset == ''):
					paramval = (line.split('=')[1]).strip('\n')

                                        if(self.verbose > 1):
                                            print '\t\t\t' + paramtype + '\t' + paramname + ' = ' + paramval
					    
					if((not paramname.lstrip().startswith('@module')) 
					   and ((self.IsNewParameter(paramname.lstrip().rstrip(),self.paramlist,'None')))):
					   self.paramlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),paramval.lstrip().rstrip(),paramtracked,self.sequencenb))
					   self.sequencenb = self.sequencenb + 1

				# Fill vector values - account for vectors spread over several lines
				if(startedvector == True):
				    if(line.find('{') != -1 and line.find('}') != -1):
					thevector = (line.split('{')[1]).split('}')[0]
					startedvector = False
					if(readingvector == True):
					    totalvectorline = totalvectorline + thevector.lstrip().rstrip()
					    readingvector = False
					    foundvectorend = True
				    elif(line.find('{') != -1 and line.find('}') == -1):
					thevector = (line.split('{')[1]).split('}')[0]
					if(readingvector == True):
					    totalvectorline = totalvectorline + thevector.lstrip().rstrip()
				    elif(line.find('{') == -1 and line.find('}') == -1):
					thevector = line
					if(readingvector == True):
					    totalvectorline = totalvectorline + thevector.lstrip().rstrip()
				    elif(line.find('{') == -1 and line.find('}') != -1):
					thevector = (line.split('}')[0])
					startedvector = False
					if(readingvector == True):
					    totalvectorline = totalvectorline + thevector.lstrip().rstrip()
					    readingvector = False
					    foundvectorend = True

				    # This is the end of a (multi-line) vector. Record the values
				    if(foundvectorend == True):
					values = totalvectorline.split(',')

					if(self.verbose > 1):
					    print '\t\t\t' + paramtype + '\t' + paramname + ' = '

					for vecval in values:
					    if(self.verbose > 1):
						print '\t\t\t\t' + vecval

					if((not paramname.lstrip().startswith('@module')) and (readingpset == False)
					   and ((self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None')))):
					    self.vecparamlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),values,"true",self.sequencenb))
					    self.sequencenb = self.sequencenb + 1
					elif((not paramname.lstrip().startswith('@module')) and (readingpset == True) and (readingnestedpset == False)
					     and ((self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,psetname)))):
					    if(self.verbose > 1):
						print '\t\tattach ' + psetname + '\t' + paramtype + '\t' + paramname + '\t' + totalvectorline + ' (' + str(self.sequencenb) + ', ' + str(self.psetsequencenb) + ')'
					    self.paramsetmemberlist.append((psetname,paramtype,paramname,totalvectorline,"true",self.sequencenb,'None',self.psetsequencenb))
					    self.sequencenb = self.sequencenb + 1
					elif((not paramname.lstrip().startswith('@module')) and (readingpset == True) and (readingnestedpset == True)
					     and ((self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,psetname)))):
					    if(self.verbose > 1):
						print '\t\tattach ' + psetname + '\t' + paramtype + '\t' + paramname + '\t' + totalvectorline + ' (' + str(self.sequencenb) + ', ' + str(self.psetsequencenb) + ')'
					    self.paramsetmemberlist.append((psetname,paramtype,paramname,totalvectorline,"true",self.sequencenb,toppsetname,self.psetsequencenb))
					    self.sequencenb = self.sequencenb + 1

					readingvector = False
					foundvectorend = False

				# Do nested PSets
				elif(startednestedpset == True):
				    if((line.lstrip()).startswith('}') or ((line.lstrip().startswith('PSet')) and line.rstrip().endswith('}'))):
					if(readingnestedpset == True):
					    readingnestedpset = False

					startednestedpset = False
					continue
				    
				    elif(line.find('=') != -1 and foundpset == True and readingnestedpset == True):
					if(line.find(paramname) != -1 and 
					   (line.rstrip().endswith('{') or line.rstrip().endswith('='))):
					    continue
					if(vals[0] == 'untracked'):
					    psetparamtype = vals[1]

					    psetparamname = vals[2]

					    paramtracked = "false"
					else:
					    psetparamtype = vals[0]
					    
					    psetparamname = vals[1]                   

					    paramtracked = "true"

					psetparamval = (line.split('=')[1]).strip('\n')
					if(self.verbose > 1):
					    print '\t\tattach ' + nestedpsetname + '\t' + psetparamtype + '\t' + psetparamname + '\t' + psetparamval + ' (' + str(self.sequencenb)  + ', ' + str(self.psetsequencenb) + ')'
					
					if((not psetparamname.lstrip().startswith('@module')) and 
					   (self.IsNewParameter(psetparamname,self.paramsetmemberlist,nestedpsetname))):
					    self.paramsetmemberlist.append((nestedpsetname,psetparamtype,psetparamname,psetparamval,paramtracked,self.sequencenb,toppsetname,self.psetsequencenb))
					    self.sequencenb = self.sequencenb + 1
					    foundpsetparams = foundpsetparams + 1

					    
				# Do PSets
				elif(startedpset == True and startednestedpset == False):
				    if((line.lstrip()).startswith('}') or ((line.lstrip().startswith('PSet')) and line.rstrip().endswith('}'))):
					if(readingpset == True):
					    readingpset = False

					startedpset = False
					foundpset = False

				    elif(line.find('=') != -1 and foundpset == True and readingpset == True):
					if(line.find(paramname) != -1 and 
					   (line.rstrip().endswith('{') or line.rstrip().endswith('='))):
					    continue
					if(vals[0] == 'untracked'):
					    psetparamtype = vals[1]

					    psetparamname = vals[2]

					    paramtracked = "false"
					else:
					    psetparamtype = vals[0]
					    
					    psetparamname = vals[1]                   

					    paramtracked = "true"

					psetparamval = (line.split('=')[1]).strip('\n')
					if(self.verbose > 1):
					    print '\t\tattach ' + psetname + '\t' + psetparamtype + '\t' + psetparamname + '\t' + psetparamval + ' (' + str(self.sequencenb) + ', ' + str(self.psetsequencenb) + ')'

					if((not psetparamname.lstrip().startswith('@module')) 
					   and (self.IsNewParameter(psetparamname,self.paramsetmemberlist,psetname))):
					    self.paramsetmemberlist.append((psetname,psetparamtype,psetparamname,psetparamval,paramtracked,self.sequencenb,'None',self.psetsequencenb))
					    self.sequencenb = self.sequencenb + 1
					    foundpsetparams = foundpsetparams + 1

				# Fill VPSets
				elif(startedvpset == True):
				    if(startedvpsetentry == True):
					if(line.find('=') != -1 and foundvpset == True and readingvpset == True):
					    if(line.find(paramname) != -1 and 
					       (line.rstrip().endswith('{') or line.rstrip().endswith('='))):
						continue

					    if(vals[0] == 'untracked'):
						vpsetparamtype = vals[1]

						vpsetparamname = vals[2]

						paramtracked = "false"
					    elif(vals[0] == '{' or vals[0] == ',{'):
						vpsetparamtype = vals[1]

						vpsetparamname = vals[2]

						paramtracked = "true"
					    else:
						vpsetparamtype = vals[0]
					    
						vpsetparamname = vals[1]                   

						paramtracked = "true"

					    vpsetparamrhs = (line.split('=')[1]).strip('\n')

					    # VPSets allow you to put two parameters on one line without 
					    # any punctuation separating them. Wow.
					    if(line.count('=') == 2):
						vpsettokens = vpsetparamrhs.split()
						vpsetparamval = vpsettokens[0]
						if(self.verbose > 1):
						    print '\t\tattach ' + psetname + '\t' + vpsetparamtype + '\t' + vpsetparamname + '\t= ' + vpsetparamval + '\t\t' + str(vpsetindex) + ' (' + str(self.sequencenb) + ', ' + str(self.psetsequencenb) + ')'
						if((not vpsetparamname.lstrip().startswith('@module')) 
						   and (self.IsNewParameter(vpsetparamname,self.vecparamsetmemberlist,psetname))):
						    self.vecparamsetmemberlist.append((psetname,vpsetparamtype,vpsetparamname,vpsetparamval,paramtracked,vpsetindex,self.sequencenb,self.psetsequencenb))
						    self.sequencenb = self.sequencenb + 1
				
						vpsetparamtypetwo = vpsettokens[1]
						vpsetparamnametwo = vpsettokens[2]
						vpsetparamvaltwo = (line.split('=')[2]).strip('\n').rstrip('}')
						if(self.verbose > 1):
						    print '\t\tattach ' + psetname + '\t' + vpsetparamtypetwo + '\t' + vpsetparamnametwo + '\t= ' + vpsetparamvaltwo + '\t\t' + str(vpsetindex) + ' (' + str(self.sequencenb) + ', ' + str(self.psetsequencenb) + ')'

						if((not vpsetparamnametwo.lstrip().startswith('@module')) 
						   and (self.IsNewParameter(vpsetparamnametwo,self.vecparamsetmemberlist,psetname))):
						    self.vecparamsetmemberlist.append((psetname,vpsetparamtypetwo,vpsetparamnametwo,vpsetparamvaltwo,"true",vpsetindex,self.sequencenb,self.psetsequencenb))
						    self.sequencenb = self.sequencenb + 1

					    else:
						vpsetparamval = vpsetparamrhs
						if(self.verbose > 1):
						    print '\t\tattach ' + psetname + '\t' + vpsetparamtype + '\t' + vpsetparamname + '\t= ' + vpsetparamval + '\t\t' + str(vpsetindex) + ' (' + str(self.sequencenb) + ', ' + str(self.psetsequencenb) + ')'

						if((not vpsetparamname.lstrip().startswith('@module')) 
						   and (self.IsNewParameter(vpsetparamname,self.vecparamsetmemberlist,psetname))):
						    self.vecparamsetmemberlist.append((psetname,vpsetparamtype,vpsetparamname,vpsetparamval,"true",vpsetindex,self.sequencenb,self.psetsequencenb))
						    self.sequencenb = self.sequencenb + 1

				# This is the end of the module definition
				elif ((line.lstrip()).startswith('}')):
				    # Clear flag when we get to the end of a module
				    startedmod = False
		    
                            # Check for start of a new module
                            if ((startedmod == False) and
                                (line.find('module') != -1) and
                                (line.find('es_module') == -1) and not
                                (line.startswith('//')) and
				(line.find('=') != -1) and 
                                line.find(themodule) != -1):
                                namedeclaration = (line.split('='))[0]

                                typedeclaration = (line.split('='))[1]

                                modname = (string.split(namedeclaration))[1]

                                modtype = (string.split(typedeclaration))[0].rstrip('{')

                                if(self.verbose > 1):
                                    print '\t\t\tFound module  = ' + modtype + ', name = ' + modname

                                # Set a flag when entering a new module definition
                                startedmod = True

			    elif((startedmod == False) and
                                (line.find('service') != -1) and
                                (line.find('es_module') == -1) and not
                                (line.startswith('//')) and
				 (line.find('=') != -1) and 
                                line.find(themodule) != -1):
                                namedeclaration = (line.split('='))[0]

                                typedeclaration = (line.split('='))[1]
				
				modname = namedeclaration

                                if(self.verbose > 1):
                                    print '\t\t\tFound service  = ' + modname

				startedmod = True

                            elif ((startedmod == False) and
                                (line.find('es_module') != -1) and not
                                (line.startswith('//')) and
				(line.find('=') != -1) and 
                                line.find(themodule) != -1):
                                namedeclaration = (line.split('='))[0]

                                typedeclaration = (line.split('='))[1]

                                modname = namedeclaration

                                if(self.verbose > 1):
                                    print '\t\t\tFound es_module  = ' + modname

                                # Set a flag when entering a new module definition
                                startedmod = True

			    elif ((startedmod == False) and 
				  line.find('block ') !=  -1 and not
				  (line.startswith('//')) and
				  (line.find('=') != -1) and 
				  (theblock) and 
				  line.find(theblock) != -1):
                                if(self.verbose > 1):
                                    print '\t\t\tFound block  = ' + theblock
				startedmod = True

		    # We didn't find the default. Before giving up, go back and look for instances of using
		    # blocks from external config files
		    if(foundparam == False and len(includedcfis) != 0):
			for line in lines:
			    # Tokenize the line
			    vals = string.split(line)
                        
			    # Check that line isn't empty
			    if(vals):
				if(line.lstrip().startswith('using ')):
				    usingblock = line.split('using ')[1].lstrip().rstrip()
				    # If we do have a using block, find the cfi file it's declared in and 
				    # get the parameters
				    for theincludedcfi in includedcfis:
					theincludedcfidatadir = theincludedcfi.split('data')[0] + 'data/'
					theincludedcfidir = thecfidir.split('src')[0] + 'src/' + theincludedcfidatadir
					theincludedcfifile = theincludedcfi.split('data/')[1]
					foundparam = self.ParseCfFile(theincludedcfidir,themodule,theparam,itsparamset,usingblock,theincludedcfifile)

                    filename.close()

	return foundparam

    # End of ParseCfFile

    # Parser for .cc files. Find constructors, tracked & untracked parameter
    # declarations
    def ParseSrcFile(self,theccfile,themodulename,thedatadir,thetdefedmodule):                            
        filename = open(theccfile)

        lines = filename.readlines()

	paraminparamset = ''

        startedmod = False
	startedconstructor = False
	endedconstructor = False
	totalconstrline = ''
	isvector = False

	thepsetname = ''
        modulename = ''
        theconstructor = ''
	externalbranch = ''
	externalbranchpset = ''

        # Bookkeeping for dealing with linebreaks
        totalline = ''
        foundlineend = False

	success = False

        for line in lines:
            # Tokenize the line
            vals = string.split(line)

            # Check that the line isn't empty and isn't commented out
            if(vals and not line.lstrip().startswith('//') and not line.lstrip().startswith('#')):
                
		# Handle inline C++ style comments. 
		if(line.find('//') != -1):
		    line = line.split('//')[0]

                # If we found a constructor, start reading the ParameterSet
                if(startedmod == True):
                    # Look for ends of parameter declarations 
                    if(line.rstrip().endswith('))') or
                       line.rstrip().endswith(')),') or
                       line.rstrip().endswith('),') or
		       line.rstrip().endswith(';') or
		       line.rstrip().endswith(');') or
		       line.rstrip().endswith(') ,') or
		       line.rstrip().endswith('{') or 
		       line.rstrip().endswith('}')):
                        foundlineend = True
			
                        totalline = totalline + line.lstrip().rstrip('\n')
                    else:

                        foundlineend = False

                        totalline = totalline + line.lstrip().rstrip('\n')

		    # Handle (i.e. ignore) PSets from "friend" branches
		    if((foundlineend == True) and (totalline.find('BranchDescription ') != -1) and (totalline.find('=') != -1)):
			externalbranch = totalline.split('BranchDescription')[1].split('=')[0].lstrip().rstrip()
			totalline = ''
		    if((foundlineend == True) and (totalline.find('getParameterSet') != -1) and externalbranch != ''):
		       if(totalline.split('getParameterSet')[1].split(')')[0].find(externalbranch) != -1):
			   externalbranchpset = totalline.split('=')[0].split('ParameterSet')[1].lstrip().rstrip()
			   totalline = ''

                    # First look at tracked parameters. No default value
                    # is specified in the .cc file                
                    if((foundlineend == True) and
                       (totalline.find('getParameter') != -1) and (totalline.find('"') != -1)):

			if(totalline.count('getParameter') > 1):
			    totalline = ''
			    continue

			# Totally confused. We can't find a parameter name in the getParameter call.
			if((totalline.split('getParameter')[1]).find('"') == -1):
			    totalline = ''
			    print "Error: getParameter used with no parameter name. Parameter will not be loaded."
			    continue

			# If this is a parameter, figure out what ParameterSet this belongs to
			belongstopset = totalline.split('.getParameter')[0].rstrip().lstrip()
			belongstopsetname = re.split('\W+',belongstopset)
			belongstovar = belongstopsetname[len(belongstopsetname)-1].lstrip().rstrip()
			if(belongstovar in self.psetdict):
			    if(self.verbose > 1):
				print '\tMember of parameter set named ' + belongstovar + ' (' + self.psetdict[belongstovar] + ')'
			    paraminparamset = self.psetdict[belongstovar]
			elif(belongstovar != '' and belongstovar == externalbranchpset):
			    if(self.verbose > 1):
				print '\tMember of external PSet ' + externalbranchpset + ' - ignoring this parameter'
			    totalline = ''
			    continue
			else:
			    paraminparamset = ''

                        paramstring = totalline.split('"')

                        # Parameter name should be the first thing in quotes after 'getParameter'
			index = 0
			for paramsubstring in paramstring:
			    if ((paramstring[index]).find('getParameter') != -1):
				paramname = paramstring[index+1]
				break
			    index = index + 1

                        # Now look for parameter <type>
			paramstring2 = totalline.split('<')
			if(paramstring2[1].find('=') != -1 and paramstring2[1].find('==') == -1 and paramstring2[1].find('!=') == -1):
			    paramstring2 = totalline.split('=')[1].split('<')

                        therest = (paramstring2[1]).split('>')

                        # It looks like our parameter type uses a namespace
                        if(therest[0].find('::') != -1):
                            namespace = therest[0].split('::')[0]
                        
                            paramtype = therest[0].split('::')[1]
                            
                        else:
                            paramtype = therest[0]

                        if(paramtype.lstrip().rstrip() == 'vector'):
			    isvector = True
			    if(totalline.find('vector<') != -1):
				vectype = (totalline.split('vector<')[1]).split('>')[0]
			    elif(totalline.find('vector <') != -1):
				vectype = (totalline.split('vector <')[1]).split('>')[0]

                            if(self.verbose > 1):
                                print '\t\t\t' + paramtype + '<' + vectype + '>' + '\t' + paramname + '\t\t(Tracked)' 
                            
			    # Strip namespace
			    if(vectype.find('::') != -1):
				vectype = (vectype.split('::'))[1]
			    
			    paramtype = vectype

			    if(paramtype == 'int' or paramtype == 'int32'):
				paramtype = 'vint32'
			    elif(paramtype == 'unsigned'):
				paramtype = 'vunsigned'
			    elif(paramtype == 'double'):
				paramtype = 'vdouble'
			    elif(paramtype == 'string'):
				paramtype = 'vstring'
			    elif(paramtype == 'InputTag'):
				paramtype = 'VInputTag'
			    elif(paramtype == 'Labels'):
				paramtype = 'vstring'
			    elif(paramtype == 'vString'):
				paramtype = 'vstring'

                        else:
			    isvector = False

			    # If this is a ParameterSet, figure out it's variable name 
			    if(paramtype == 'ParameterSet' or paramtype == 'PSet' or paramtype == 'Parameters' or paramtype == 'VPSet'):
				if(totalline.find('=') != -1):
				    thisparamset = totalline.split('=')[0].rstrip().lstrip()
				
				    if(thisparamset.find('vector<edm::ParameterSet>') != -1):
					thisparamset = thisparamset.split('>')[1].rstrip().lstrip()
				    elif(thisparamset.find('PSet ') != -1):
					thisparamset = thisparamset.split('PSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('VPSet ') != -1):
					thisparamset = thisparamset.split('VPSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('ParameterSet ') != -1):
					thisparamset = thisparamset.split('ParameterSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('ParameterSet& ') != -1):
					thisparamset = thisparamset.split('ParameterSet& ')[1].rstrip().lstrip()
				elif(totalline.split('.getParameter')[0].find('(') != -1):
				    thisparamset = totalline.split('.getParameter')[0].split('(')[0]

				if(totalline.find('=') != -1 or totalline.split('.getParameter')[0].find('(') != -1):
				    if(len(re.split('\W+',thisparamset)) == 1):
					if(self.verbose > 1):
					    print '\t\tFound a new PSet named ' + paramname + ' with variable name ' + thisparamset
					
					self.psetdict[thisparamset] = paramname
					if(paramname in self.psetsequences):
					    self.psetsequencenb = self.psetsequences[paramname]
					else:					    
					    self.psetsequencenb = self.sequencenb
					    self.sequencenb = self.sequencenb + 1					    
					    self.psetsequences[paramname] = self.psetsequencenb

                            if(self.verbose > 1):
                                print '\t\t\t' + paramtype + '\t' + paramname + '\t\t(Tracked)' 

                        # We have tracked parameters in this module. Get
                        # their default values from the corresponding .cfi
                        # file
                        if(thetdefedmodule == ""):
                            success = self.ParseCfFile(thedatadir,theconstructor,paramname,paraminparamset,None,None)
                        else:
                            success = self.ParseCfFile(thedatadir,thetdefedmodule,paramname,paraminparamset,None,None)

                        totalline = ''
                        foundlineend = False
			
			# We didn't find a default setting for this tracked parameter
			if(success == False):

			    if(self.verbose > 1):
				print '\t\tFailed to find a default value for the tracked parameter: ' + paramtype + ' ' + paramname + ' in module ' + themodulename

			    if(not paramname.lstrip().startswith('@module')):
				# Special cases for typedef'd vectors
				if(paramtype.lstrip().rstrip() == 'vtag'):
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None')):
					self.vecparamlist.append(('VInputTag',paramname.lstrip().rstrip(),'',"true",self.sequencenb))
					self.sequencenb = self.sequencenb + 1
				elif(paramtype.lstrip().rstrip() == 'Labels'):
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None')):
					self.vecparamlist.append(('vstring',paramname.lstrip().rstrip(),'',"true",self.sequencenb))
					self.sequencenb = self.sequencenb + 1
				elif(paramtype.lstrip().rstrip() == 'vString' or paramtype.lstrip().rstrip() == 'vstring'):
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None')):
					self.vecparamlist.append(('vstring',paramname.lstrip().rstrip(),'',"true",self.sequencenb))
					self.sequencenb = self.sequencenb + 1
				elif(paramtype.lstrip().rstrip() == 'VPSet' or paramtype.lstrip().rstrip() == 'Parameters'):
				    if(self.verbose > 0):
					print "Appending to vecparamsetlist with no values"
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamsetmemberlist,'None')):
					self.sequencenb = self.psetsequencenb
					self.vecparamsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"true",0,self.sequencenb,self.psetsequencenb))	
					self.sequencenb = self.sequencenb + 1
				elif(paramtype.lstrip().rstrip() == 'PSet' or 
				     paramtype.lstrip().rstrip() == 'ParameterSet'):
				    if(self.verbose > 0):
					print "Appending to paramsetlist with no values"
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,paraminparamset)):
					self.sequencenb = self.psetsequencenb
					self.paramsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"true",self.sequencenb,paraminparamset,self.psetsequencenb))
					self.sequencenb = self.sequencenb + 1
				elif(isvector == False and paraminparamset == ''):
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramlist,'None')):
					self.paramlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),None,"true",self.sequencenb))	   
					self.sequencenb = self.sequencenb + 1
				elif(isvector == False and paraminparamset != ''):
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,paraminparamset)):
					self.paramsetmemberlist.append((paraminparamset,paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),'',"true",self.sequencenb,'None',self.psetsequencenb))
					self.sequencenb = self.sequencenb + 1
				elif(isvector == True and paraminparamset == ''):
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None')):
					self.vecparamlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),'',"true",self.sequencenb))
					self.paramfailures.append((themodulename,paramtype,paramname.lstrip().rstrip(),"true",self.sequencenb))
					self.sequencenb = self.sequencenb + 1
				else:
				    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,paraminparamset)):
					self.paramsetmemberlist.append((paraminparamset,paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),'',"true",self.sequencenb,'None',self.psetsequencenb))
					self.sequencenb = self.sequencenb + 1					    
			elif(paramtype.lstrip().rstrip() == 'PSet' or 
			     paramtype.lstrip().rstrip() == 'ParameterSet'):
			    if (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,paraminparamset)):
				if(paramname in self.psetsequences):
				    self.psetsequencenb = self.psetsequences[paramname]
				else:
				    self.psetsequencenb = self.sequencenb

				self.sequencenb = self.psetsequencenb
				self.paramsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"true",self.sequencenb,paraminparamset,self.psetsequencenb))
				self.sequencenb = self.sequencenb + 1

			paraminparamset = ''	

                    # Now look at untracked parameters. Default
                    # value _may_ be specified as the second argument
                    # in the declaration.
                    if((foundlineend == True) and
                       (totalline.find('getUntrackedParameter') != -1) and 
		       (totalline.find('"') != -1)):
			defaultincc = False

                        paramstring = totalline.split('"')

			# Totally confused. We can't find a parameter name in the getUntrackedParameter call.
			if((totalline.split('getUntrackedParameter')[1]).find('"') == -1):
			    totalline = ''
			    print "Error: getUntrackedParameter used with no parameter name. Parameter will not be loaded."
			    continue

			# Figure out what ParameterSet this belongs to
			belongstopset = totalline.split('.getUntrackedParameter')[0].rstrip().lstrip()
			belongstopsetname = re.split('\W+',belongstopset)
			belongstovar = belongstopsetname[len(belongstopsetname)-1].lstrip().rstrip()
			if(belongstovar in self.psetdict):
			    if(self.verbose > 1):
				print '\tMember of parameter set named ' + belongstovar + ' (' + self.psetdict[belongstovar] + ')'
			    paraminparamset = self.psetdict[belongstovar]
			else:
			    paraminparamset = ''

                        # Parameter name should be the first thing in
                        # quotes
			index = 0
			for paramsubstring in paramstring:
			    if ((paramstring[index]).find('getUntrackedParameter') != -1):
				paramname = paramstring[index+1]
				break
			    index = index + 1

			# Vector template 
			if(totalline.find('vector<') != -1):
			    paramstring2 = totalline.split('vector<')
			    if(paramstring2[1].find('=') != -1 and paramstring2[1].find('==') == -1 and paramstring2[1].find('!=') == -1):
				paramstring2 = totalline.split('=')[1].split('<')

			    therest = (paramstring2[1]).split('>')

			    if(therest[0].find('::') != -1):
				namespace = therest[0].split('::')[0]
                                
                                paramtype = therest[0].split('::')[1]

                            else:
                                paramtype = therest[0]

			    if(paramtype == 'int' or paramtype == 'int32'):
				paramtype = 'vint32'
			    elif(paramtype == 'unsigned'):
				paramtype = 'vunsigned'
			    elif(paramtype == 'double'):
				paramtype = 'vdouble'
			    elif(paramtype == 'string'):
				paramtype = 'vstring'
			    elif(paramtype == 'InputTag'):
				paramtype = 'VInputTag'
			    elif(paramtype == 'Labels'):
				paramtype = 'vstring'
			    elif(paramtype == 'vString'):
				paramtype = 'vstring'

			    if((not paramname.lstrip().startswith('@module')) 
			       and (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None'))):
				self.vecparamlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),'',"false",self.sequencenb))
				self.sequencenb = self.sequencenb + 1
			    defaultincc = True

                        # Templated getParameter call
                        elif(totalline.find('<') != -1):
                            paramstring2 = totalline.split('<')                            
                            therest = paramstring2[1].split('>')
                            
                            # It looks like our parameter type uses a
                            # namespace
                            if(therest[0].find('::') != -1):
                                namespace = therest[0].split('::')[0]                                
                                paramtype = therest[0].split('::')[1]

                            else:
                                paramtype = therest[0]
				
			    # If this is a ParameterSet, figure out it's variable name 
			    if(paramtype == 'ParameterSet' or paramtype == 'PSet' or paramtype == 'Parameters' or paramtype == 'VPSet'):
				if(totalline.find('=') != -1):
				    thisparamset = totalline.split('=')[0].rstrip().lstrip()
				
				    if(thisparamset.find('vector<edm::ParameterSet>') != -1):
					thisparamset = thisparamset.split('>')[1].rstrip().lstrip()
				    elif(thisparamset.find('PSet ') != -1):
					thisparamset = thisparamset.split('PSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('VPSet ') != -1):
					thisparamset = thisparamset.split('VPSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('ParameterSet ') != -1):
					thisparamset = thisparamset.split('ParameterSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('ParameterSet& ') != -1):
					thisparamset = thisparamset.split('ParameterSet& ')[1].rstrip().lstrip()
				elif(totalline.split('.getUntrackedParameter')[0].find('(') != -1):
				    thisparamset = totalline.split('.getUntrackedParameter')[0].split('(')[0]

				if(totalline.find('=') != -1 or totalline.split('.getUntrackedParameter')[0].find('(') != -1):
				    if(len(re.split('\W+',thisparamset)) == 1):
					if(self.verbose > 1):
					    print 'Found a new PSet named ' + paramname + ' with variable name ' + thisparamset

					self.psetdict[thisparamset] = paramname
					if(paramname in self.psetsequences):
					    self.psetsequencenb = self.psetsequences[paramname]
					    print 'The PSet ' + paramname + ' was already found with sequencenb = ' + str(self.psetsequences[paramname])
					else:
					    self.psetsequencenb = self.sequencenb
					    self.sequencenb = self.sequencenb + 1
					    self.psetsequences[paramname] = self.psetsequencenb

                            # Check whether there are default
                            # parameter values
                            if(totalline.find(',') != -1):
                                paramvalstring = totalline.split(',')
                                
				if(paramvalstring[1].find(')') != -1):
				    paramval = (paramvalstring[1].split(')'))[0]

				    if(paramval.find('(') != -1):
					paramval = paramval.split('(')[1]
					if(not paramval):
					    paramval = 'None'

				    if(self.verbose > 1): 
					print '\t\t' + paramtype + '\t' + paramname + ' = ' + paramval + '\t\t(Untracked)'
				    if(paramtype == 'PSet' or paramtype == 'ParameterSet'):
					if((not paramname.lstrip().startswith('@module'))
					   and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,'None'))):
					    self.paramsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"false",self.sequencenb,'None',self.psetsequencenb))
					    self.sequencenb = self.sequencenb + 1
				    elif(paramtype == 'VPSet' or paramtype == 'Parameters'):
					if((not paramname.lstrip().startswith('@module'))
					   and (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamsetmemberlist,'None'))):
					    self.vecparamsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"false",0,self.sequencenb,self.psetsequencenb))
					    self.sequencenb = self.sequencenb + 1
				    elif(paramtype == 'Labels'):
					if((not paramname.lstrip().startswith('@module')) 
					    and (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None'))):
					    self.vecparamlist.append(('vstring',paramname.lstrip().rstrip(),'',"false",self.sequencenb))
					    self.sequencenb = self.sequencenb + 1
				    elif(paramtype == 'vString' or paramtype == 'vstring'):
					if((not paramname.lstrip().startswith('@module')) 
					    and (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None'))):
					    self.vecparamlist.append(('vstring',paramname.lstrip().rstrip(),'',"false",self.sequencenb))
					    self.sequencenb = self.sequencenb + 1
				    else:
					if(paraminparamset == ''):
					    if((not paramname.lstrip().startswith('@module')) 
					       and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramlist,'None'))):
						self.paramlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),paramval.lstrip().rstrip(),"false",self.sequencenb))
						self.sequencenb = self.sequencenb + 1
					else:
					    if((not paramname.lstrip().startswith('@module')) 
					       and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,paraminparamset))): 
						self.paramsetmemberlist.append((paraminparamset,paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),paramval.lstrip().rstrip(),"false",self.sequencenb,'None',self.psetsequencenb))
						self.sequencenb = self.sequencenb + 1						

				    defaultincc = True

				else:
				    if(self.verbose > 1):
					print '\t\t' + paramtype + '\t' + paramname + '\t(None)\t\t(Untracked)'

				    defaultincc = False

                            else:
                                if(self.verbose > 1):
				    print '\t\t' + paramtype + '\t' + paramname + '\t(None)\t\t(Untracked)'
                                
				defaultincc = False
      
			    # We have untracked parameters in this module. If we didn't 
			    # find their default values in the .cc file, look in 
			    # .cfi files
			    if(thetdefedmodule == "" and defaultincc == False):
				success = self.ParseCfFile(thedatadir,theconstructor,paramname,paraminparamset,None,None)

			    elif(thetdefedmodule != "" and defaultincc == False):
				success = self.ParseCfFile(thedatadir,thetdefedmodule,paramname,paraminparamset,None,None)

			    if(defaultincc == False and success == False):
				if(self.verbose > 1):
				    print '\t\tFailed to find a default value for the untracked parameter: ' + paramtype + ' ' + paramname + ' in module ' + themodulename
				self.paramfailures.append((themodulename,paramtype,paramname.lstrip().rstrip(),"false",self.sequencenb))
				if(paramtype == 'PSet' or paramtype == 'ParameterSet'):
				    if(paramname in self.psetsequences):
					self.psetsequencenb = self.psetsequences[paramname]
					self.sequencenb = self.psetsequencenb
				    else:
					self.psetsequencenb = self.sequencenb
					self.psetsequences[paramname] = self.psetsequencenb

				    self.paramsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"false",self.sequencenb,'None',self.psetsequencenb))
				    self.sequencenb = self.sequencenb + 1

				elif(paramtype == 'VPSet' or paramtype == 'Parameters'):
				    if((not paramname.lstrip().startswith('@module'))
				        and (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamsetmemberlist,'None'))):
					self.vecparamsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"false",0,self.sequencenb,self.psetsequencenb))
					self.sequencenb = self.sequencenb + 1
				else:
				    if((not paramname.lstrip().startswith('@module'))
				       and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramlist,'None'))):
				       self.paramlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),None,"false",self.sequencenb))
				       self.sequencenb = self.sequencenb + 1

			    elif(paramtype.lstrip().rstrip() == 'PSet' or 
				 paramtype.lstrip().rstrip() == 'ParameterSet'):
				if (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,'None')):
				    self.psetsequencenb = self.psetsequences[paramname]
				    self.sequencenb = self.psetsequencenb
				    self.paramsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"false",self.sequencenb,'None',self.psetsequencenb))
				    self.sequencenb = self.sequencenb + 1

			    paraminparamset = ''	



                        # Not using the templated getParameter. Look for
                        # parameter name and default value. If we're really
                        # lucky, we can find the parameter type too...
                        else:
                            paramnamestring = (totalline.split('getUntrackedParameter('))[1]
                            
			    # Figure out what ParameterSet this belongs to
			    belongstopset = totalline.split('.getUntrackedParameter')[0].rstrip().lstrip()
			    belongstopsetname = re.split('\W+',belongstopset)
			    belongstovar = belongstopsetname[len(belongstopsetname)-1].lstrip().rstrip()
			    if(belongstovar in self.psetdict):
				if(self.verbose > 1):
				    print '\tMember of parameter set named ' + belongstovar + ' (' + self.psetdict[belongstovar] + ')'
			    else:
				paraminparamset = ''

                            paramname = paramnamestring.split(',')[0]
                            
                            therest = (paramnamestring.split(',')[1]).split('))')[0]
                            
                            # More gymnastics - parameter default
                            # includes a cast(?)
                            if(therest.find('(') != -1):
                                fullparamdefault = therest.split('(')[0]
                                paramdefault = therest.split('(')[1]
                                
                                # It looks like our parameter type
                                # uses a namespace
                                if(fullparamdefault.find('::') != -1):
                                    namespace = fullparamdefault.split('::')[0]
                                    
                                    paramtype = fullparamdefault.split('::')[1]
                                    
                                else:
                                    paramtype = fullparamdefault
                                    
                            else:
                                paramtype = 'unknown'
                                paramdefault = therest

			    paramname = paramname.rstrip('"').lstrip('"')
			    paramtype = paramtype.lstrip().rstrip()

			    # If this is a ParameterSet, figure out it's variable name 
			    if(paramtype == 'ParameterSet' or paramtype == 'PSet' or paramtype == 'Parameters' or paramtype == 'VPSet'):
				if(totalline.find('=') != -1):
				    thisparamset = totalline.split('=')[0].rstrip().lstrip()
				
				    if(thisparamset.find('vector<edm::ParameterSet>') != -1):
					thisparamset = thisparamset.split('>')[1].rstrip().lstrip()
				    elif(thisparamset.find('PSet ') != -1):
					thisparamset = thisparamset.split('PSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('VPSet ') != -1):
					thisparamset = thisparamset.split('VPSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('ParameterSet ') != -1):
					thisparamset = thisparamset.split('ParameterSet ')[1].rstrip().lstrip()
				    elif(thisparamset.find('ParameterSet& ') != -1):
					thisparamset = thisparamset.split('ParameterSet& ')[1].rstrip().lstrip()
				elif(totalline.split('.getUntrackedParameter')[0].find('(') != -1):
				    thisparamset = totalline.split('.getUntrackedParameter')[0].split('(')[0]

				if(totalline.find('=') != -1 or totalline.split('.getUntrackedParameter')[0].find('(') != -1):
				    if(len(re.split('\W+',thisparamset)) == 1):
					if(self.verbose > 1):
					    print 'Found a new PSet named ' + paramname + ' with variable name ' + thisparamset

					self.psetdict[thisparamset] = paramname
					if(paramname in self.psetsequences):
					    self.psetsequencenb = self.psetsequences[paramname]
					    print 'The PSet ' + paramname + ' was already found with sequencenb = ' + str(self.psetsequences[paramname])
					else:
					    self.psetsequencenb = self.sequencenb
					    self.sequencenb = self.sequencenb + 1
					    self.psetsequences[paramname] = self.psetsequencenb

                            if(self.verbose > 1):
                                print '\t\t(Untemplated) ' + paramtype + ' ' + paramname + ' ' + paramdefault + '\t\t(Untracked)'
			    if(paramtype == 'PSet' or paramtype == 'ParameterSet'):
				if((not paramname.lstrip().startswith('@module'))
				   and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,'None'))):
				    self.sequencenb = self.psetsequencenb
				    self.paramsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"false",self.sequencenb,'None',self.psetsequencenb))
				    self.sequencenb = self.sequencenb + 1
			    elif(paramtype == 'VPSet' or paramtype == 'Parameters'):
				if((not paramname.lstrip().startswith('@module')) 
				   and (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamsetmemberlist,'None'))):
				    self.vecparamsetmemberlist.append((paramname.lstrip().rstrip(),'','','',"false",0,self.sequencenb,self.psetsequencenb))
				    self.sequencenb = self.sequencenb + 1
			    elif(paramtype == 'Labels'):
				if((not paramname.lstrip().startswith('@module'))
				   and (self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None'))):
				    self.vecparamlist.append(('vstring',paramname.lstrip().rstrip(),'',"false",self.sequencenb))
				    self.sequencenb + self.sequencenb + 1
			    else:
				if((not paramname.lstrip().startswith('@module') and (paraminparamset == '') 
				    and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramlist,'None')))):
				    self.paramlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),paramdefault,"false",self.sequencenb))
				    self.sequencenb = self.sequencenb + 1
				elif((not paramname.lstrip().startswith('@module') and (paraminparamset != '') 
				      and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,paraminparamset)))):
				    self.paramsetmemberlist.append((paraminparamset,paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),paramdefault,"false",self.sequencenb,'None',self.psetsequencenb))
				    self.sequencenb = self.sequencenb + 1

			# We're finished with this line(s)
                        totalline = ''
                        foundlineend = False

		    # This line is uninteresting
		    elif(foundlineend == True and line.find('getParameter') == -1 and line.find('getUntrackedParameter') == -1):
			foundlineend = False
			totalline = ''
			
                # Find a constructor
                if(startedmod == False and line.find('::') != -1):
                    constrline = line.split('::')

                    theclass = constrline[0].lstrip().rstrip()

                    therest = constrline[1].split('(')

                    if(therest):

                        theconstructor = therest[0].rstrip().lstrip()
 
                        if(theclass.find('<') != -1 and theclass.find('>') != -1):
                            theclass = theclass.split('<')[0]

                        # Check that this really looks like a constructor
                        if(theclass == theconstructor):   
                            # Check if this is really the module we want
                            if(theclass != themodulename): 
                                continue
			    
                            if(self.verbose > 1):
                                print '\t\tConstructor ' + theclass + '::' + theconstructor

                            startedmod = True
			    startedconstructor = True				
			    
		elif(startedmod == False and line.find(themodulename + '(') != -1):
		    theconstructor = themodulename
		    startedmod = True

		if(startedconstructor == True and not (line.rstrip().endswith('{') or line.lstrip().startswith('{'))): 	    
		    totalconstrline = totalconstrline + line

		# Look for ParameterSets passed by reference to classes that this module inherits from
		elif(startedconstructor == True and  (line.rstrip().endswith('{') or line.lstrip().startswith('{'))):
		    startedconstructor = False
		    thepsetline = ""
#		    thepsetname = ""
		    lookupclass = ""
		    lookupclass2 = ""
		    multint = False 

		    if(totalconstrline == ''):
			totalconstrline = totalconstrline + line

		    if(totalconstrline.find('ParameterSet') != -1 and totalconstrline.find('&') != -1):
			if(totalconstrline.find('ParameterSet&') != -1):
			    thepsetline = totalconstrline.split('ParameterSet&')[1]
			    thepsetname = totalconstrline.split('ParameterSet&')[1].split(')')[0].lstrip().rstrip()
			elif(totalconstrline.find('ParameterSet const&') != -1):
			    thepsetline = totalconstrline.split('ParameterSet const&')[1]
			    thepsetname = totalconstrline.split('ParameterSet const&')[1].split(')')[0].lstrip().rstrip()
			elif(totalconstrline.find('ParameterSet &') != -1):
			    thepsetline = totalconstrline.split('ParameterSet &')[1]
			    thepsetname = totalconstrline.split('ParameterSet &')[1].split(')')[0].lstrip().rstrip()

			if(thepsetname):
			    if(thepsetname.find(',') != -1):
				thepsetname = thepsetname.split(',')[0].lstrip().rstrip()

			    self.mainpset = thepsetname

			    if(thepsetline.find(': ') != -1 or thepsetline.find(' :') != -1):
				if(thepsetline.find('(' + thepsetname + ')') != -1):
				    lookupclass = thepsetline.split(':')[1].split('('+thepsetname+')')[0].lstrip().rstrip()
				elif(thepsetline.find('(' + thepsetname + ',') != -1):
				    lookupclass = thepsetline.split(':')[1].split('('+thepsetname+',')[0].lstrip().rstrip()

				if((thepsetline.find('(' + thepsetname + ')') != -1) and 
				   (thepsetline.find('(' + thepsetname + ',') != -1)):
				    lookupclass = thepsetline.split(':')[1].split('('+thepsetname+',')[0].lstrip().rstrip()
				    lookupclass2 = thepsetline.split(':')[1].split('),')[1].split('('+thepsetname+')')[0].lstrip().rstrip()

				if(lookupclass):
				    # deal with multiple levels of inheritance
				    self.inheritancelevel = self.inheritancelevel + 1
				    if(self.inheritancelevel > 1):
					thehfile = theccfile.replace('/src/','/interface/').replace('.cc','.h').replace('//interface','//src')
				    else:
					thehfile = self.includefile
				
				    self.FindInheritedParameters(lookupclass,thedatadir,thehfile)

				    # Deal with multiple inheritance...
				    if(len(thepsetline.split(':')[1].split('('+thepsetname+')')) > 2):	
					lookupclass = thepsetline.split(':')[1].split('('+thepsetname+')')[1].split(',')[1].lstrip().rstrip()
					multint = True
				    elif(len(thepsetline.split(':')[1].split('('+thepsetname+',')) > 2):
					lookupclass = thepsetline.split(':')[1].split('('+thepsetname+',')[1].split(',')[1].lstrip().rstrip()
					multint = True

				    if(multint == True):
					self.FindInheritedParameters(lookupclass,thedatadir,thehfile)			

				    if(lookupclass2):
					self.FindInheritedParameters(lookupclass2,thedatadir,thehfile)
	
		# Look for ParameterSets passed to objects instantiated within this module. This won't pick up PSets 
		# passed to methods of the new object
		elif(startedconstructor == False and  (line.find('new ') != -1) and (line.find('(') != -1)):
		    theobjectclass = ''
		    theobjectargument = ''

		    if(len(line.split('new ')) == 1):
			theobjectclass = line.split('new ')[0].split('(')[0].lstrip().rstrip()
		    elif(len(line.split('new ')) == 2):
			theobjectclass = line.split('new ')[1].split('(')[0].lstrip().rstrip()
		    elif(len(line.split('new ')) == 3):
			theobjectclass = line.split('new ')[2].split('(')[0].lstrip().rstrip()

		    if(theobjectclass):
			if(len(line.split(theobjectclass)) == 2):
			    theobjectargument = line.split(theobjectclass)[1].lstrip('(')
			elif(len(line.split(theobjectclass)) == 3):
			    theobjectargument = line.split(theobjectclass)[2].lstrip('(')
    
			if(line.find(',') != -1):
			    thepassedpset = theobjectargument.split(',')[0].lstrip().rstrip()
			else:
			    thepassedpset = theobjectargument.split(')')[0].lstrip('(').rstrip(');').lstrip().rstrip()

			if((thepassedpset in self.psetdict)):
			    psettype = self.psetdict[thepassedpset] 
			    if(self.verbose > 1):
				print 'Found pset of type ' + psettype + ' passed to object of type ' + theobjectclass

			    self.ParsePassedParameterSet(psettype, theccfile, theobjectclass, 'None',thedatadir,themodulename)
			elif(thepassedpset == self.mainpset):
			    if(self.verbose > 1):
				print 'Found top-level pset passed to object of type ' + theobjectclass

			    self.ParsePassedParameterSet('None', theccfile, theobjectclass, 'None',thedatadir,themodulename)

		elif(startedconstructor == False and  (line.find('new ') == -1) and (line.find('(') != -1)):
		    theobjectclass = ''
		    theobjectargument = ''

		    if(len(line.split('(')[0].split()) == 2):
			theobjectclass = line.split('(')[0].split()[0]

		    if(theobjectclass):
			if(len(line.split(theobjectclass)) == 2):
			    theobjectargument = line.split('(')[1]
    
			if(line.find(',') != -1):
			    thepassedpset = theobjectargument.split(',')[0].lstrip().rstrip()
			else:
			    thepassedpset = theobjectargument.split(')')[0].lstrip('(').rstrip(');').lstrip().rstrip()

			if((thepassedpset in self.psetdict)):
			    psettype = self.psetdict[thepassedpset] 
			    if(self.verbose > 1):
				print 'Found pset of type ' + psettype + ' passed to object of type ' + theobjectclass

			    self.ParsePassedParameterSet(psettype, theccfile, theobjectclass, 'None',thedatadir,themodulename)
			elif(thepassedpset == self.mainpset):
			    if(self.verbose > 1):
				print 'Found top-level pset passed to object of type ' + theobjectclass

			    self.ParsePassedParameterSet('None', theccfile, theobjectclass, 'None',thedatadir,themodulename)

    # End of ParseSrcFile

    # Parse .h files. Find class definitions and base classes
    def ParseInterfaceFile(self, thehfile, themodname):
        if os.path.isfile(thehfile):
            intfile = open(thehfile)
            
            # Bookkeeping for dealing w. line breaks
            lines = intfile.readlines()
            totalline = ''
            foundlineend = False
            startedclass = False

            for line in lines:
                if((line.lstrip()).startswith('class')):
                    classname = (line.lstrip().split(':')[0]).lstrip('class').lstrip().rstrip()

                    if(classname != themodname):
                        continue
                    else:
			startedclass = True

		elif((line.lstrip()).startswith('template') and line.find('class') != -1 and line.find('public') != -1):
		    classname = line.split(':')[0].split('>')[1].split('class')[1].lstrip().rstrip()
		    print classname
                    if(classname != themodname):
                        continue
                    else:
			startedclass = True
			
		if(startedclass == True):
                    if(line.rstrip().endswith('{') or line.rstrip().endswith(';')):
                        totalline = totalline + line.lstrip().rstrip()

                        foundlineend = True

			startedclass = False

                    else:
                        totalline = totalline + line.lstrip().rstrip('\n')

                        foundlineend = False

                    if(foundlineend == True and totalline.find('public') != -1):
                        basepos = totalline.split('public')[1]

                        baseclass = ((basepos.split())[0]).rstrip('{')
			if(baseclass.find('::') != -1):
			    # Remove namespace information - do we want this?
			    baseclass = (baseclass.split('::'))[1]

			baseclass = baseclass.rstrip(',').lstrip(',')

			self.baseclass = baseclass

			self.includefile = thehfile

			if(baseclass in self.goodbaseclasses):
			    if(self.verbose > 1):
				print '\t\tBase class is ' + baseclass
                        
			    foundlineend = False

			    totalline = ''

			# Multiple inheritance...
			elif(len(totalline.split(', public')) > 1):
			    basepos = totalline.split(', public')[1]

			    baseclass = ((basepos.split())[0]).rstrip('{')

			    if(baseclass.find('::') != -1):
				# Remove namespace information - do we want this?
				baseclass = (baseclass.split('::'))[1]

			    baseclass = baseclass.rstrip(',').lstrip(',')

			    if(self.verbose > 1):
				print '\t\tBase class is ' + baseclass

			    self.baseclass = baseclass

			    self.includefile = thehfile
                        
			    foundlineend = False

			    totalline = ''			     

    # End of ParseInterfaceFile

    # Special case for handling module declarations of the form
    # typedef BaseClass<> ModuleName;
    def HandleTypedefs(self,theccfile,themodulename,thesrcdir,theinterfacedir,thedatadir, sourcetree):
        filename = open(theccfile)

        lines = filename.readlines()
        totalline = ''
	foundlineend = False
	startedtypedef = False
	foundatypedef = False

        for line in lines:
	    if(line.lstrip().startswith('typedef ')):
		startedtypedef = True

	    if(startedtypedef):
		totalline = totalline + line

		if (totalline.find('//') != -1):
		    totalline = (totalline.split('//'))[0]
		if(totalline.rstrip().endswith(';')):
		    foundlineend = True
		    startedtypedef = False
		else:
		    foundlineend = False

		if(foundlineend == True and line.find(themodulename) != -1):
		    if((line.find('>') != -1) and (line.split('>')[1].find(themodulename) != -1)):
			foundatypedef = True
			if(self.verbose > 1):
			    print 'found a typedef module declaration in ' + theccfile
			    print '\n' + totalline

		    theclass =  (totalline.split('<')[0]).lstrip().lstrip('typedef').lstrip().rstrip()
		    if(theclass.find('::') != -1):
			theclass = theclass.split('::')[1]

		    if(os.path.isdir(theinterfacedir)):
			hfiles = os.listdir(theinterfacedir)

			for hfile in hfiles:
			    if(hfile.endswith(".h")):
				# Check the interface file for the real base class
				self.ParseInterfaceFile(theinterfacedir + hfile, theclass)

				# And check if the class constructor is in the .h file
				if(self.verbose > 1):
				    print "\t\tChecking include file  " + theinterfacedir + hfile + " for " + theclass 
				self.ParseSrcFile(theinterfacedir + hfile, theclass, thedatadir, themodulename)

		    ccfiles = os.listdir(thesrcdir)

		    for ccfile in ccfiles:
			if(ccfile.endswith(".cc")):
			    if(self.verbose > 1):
				print "\t\tChecking sourcefile " + thesrcdir + ccfile + " for " + theclass
			    self.ParseSrcFile(thesrcdir + ccfile, theclass, thedatadir, themodulename)
		
		    foundlineend = False
		    totalline = ''

	# We found a typedef/templated module declaration, but couldn't find the template class 
	# in this package. As a last resort, look for it in any included files. 
	if((foundatypedef == True) and (self.baseclass == '')):
	    self.includefile = theccfile
	    thebaseclass = self.FindOriginalBaseClass(theclass, sourcetree, thedatadir)

	    if(thebaseclass):
		self.baseclass = thebaseclass

    # Find the base class for modules that have 2 levels of inheritance. This can be expensive, so
    # try to be smart and look at what files are being included.		    
    def FindOriginalBaseClass(self, classname, sourcetree, thedatadir):
	basebaseclass = ""

	if(self.verbose > 1):
	    print "\tNeed to look for " + classname + " from " + self.includefile

	if(os.path.isfile(self.includefile)):
	    classfile = open(self.includefile)

	    includelines = classfile.readlines()

	    for includeline in includelines:
		if(includeline.lstrip().startswith('#include')):

		    includedlib = includeline.split('#include')[1]

		    if(includedlib.find('.h') != -1 and includedlib.find('"') != -1):
			includedlib = (includedlib.split('"')[1]).lstrip().rstrip()

			if(self.verbose > 1):
			    print "\tNeed to check the file " + sourcetree + includedlib 

			if(os.path.isfile(sourcetree + includedlib)):
			    baseincludefile = open(sourcetree + includedlib)
			    
			    baseincludelines = baseincludefile.readlines()

			    # Bookkeeping for dealing w. line breaks
			    totalline = ''
			    foundlineend = False
			    startedbaseclass = False		
	    
			    for baseincludeline in baseincludelines:
				if((baseincludeline.lstrip()).startswith('class')):
				    baseclassname = (baseincludeline.lstrip().split(':')[0]).lstrip('class').lstrip().rstrip()

				    if(baseclassname != classname):
					continue
				    else:
					startedbaseclass = True

				if(startedbaseclass == True):
				    if(baseincludeline.rstrip().endswith('{') or baseincludeline.rstrip().endswith(';')):
					totalline = totalline + baseincludeline.lstrip().rstrip('\n')

					foundlineend = True

					startedbaseclass = False

				    else:
					totalline = totalline + baseincludeline.lstrip().rstrip('\n')
					
					foundlineend = False

				if(foundlineend == True and totalline.find('public') != -1):
				    basepos = totalline.split('public')[1]
				    
				    basebaseclass = ((basepos.split())[0]).rstrip('{')
				    if(basebaseclass.find('<') != -1):
					basebaseclass = basebaseclass.split('<')[0].lstrip().rstrip()

				    if(basebaseclass.find('::') != -1):
					basebaseclass = (basebaseclass.split('::'))[1]

					basebaseclass = basebaseclass.rstrip(',').lstrip(',')

				    if(self.verbose > 1):
					print '\t\tBase base class is ' + basebaseclass

				    # Now see if the actual class implementation is also in the same .h file
				    self.ParseSrcFile(sourcetree + includedlib, classname, thedatadir, classname)

				    foundlineend = False

	return basebaseclass

    # Look for parameters that this component may have inherited
    def FindInheritedParameters(self,thebaseobject,thederiveddatadir,theincfile):
	objectinstantiated = False
	includeclass = ""

	if(self.verbose > 1):
	    print '\tIncludefile is ' + theincfile
	    print '\tLook for parameters from the base object ' + thebaseobject

	if(os.path.isfile(theincfile)):

	    classfile = open(theincfile)

	    includelines = classfile.readlines()

	    # Look for the declaration of this object
	    for includeline in includelines:

		# It's explicitly instantiated in the include file. Get the class name, stop, and go look for the #include
		if (includeline.find(thebaseobject) != -1 and not (includeline.lstrip().startswith('#')) and 
		    includeline.find('public') == -1 and not (includeline.lstrip().startswith('$')) and not 
		    (includeline.lstrip().startswith('//')) and not (includeline.lstrip().startswith('/*')) and 
		    (includeline.split(thebaseobject)[1].find('}') == -1)):
		    includeclass = includeline.split(thebaseobject)[0].lstrip().rstrip()
		    if(self.verbose > 1):
			print '\tThe base class is ' + includeclass

		    # More C++ fun. Is this an assignment or a usage of the PSet?
		    if(includeclass.find('ParameterSet') != -1):
			# Assignment:
			self.mainpset = thebaseobject
			if(self.verbose > 1):
			    print '\tFound a copy of the main PSet. Setting main PSet to ' + thebaseobject
			objectinstantiated = False			
		    else:
			# Usage:
			objectinstantiated = True

		    break

		# It's not explicitly instantiated. Trace it back using the #include file
		elif (includeline.find(thebaseobject) != -1 and (includeline.lstrip().startswith('#include')) and not
		      (includeline.lstrip().startswith('$'))):
		    objectinstantiated = False
		    baseobjectincludefile = includeline.lstrip('#include').lstrip().rstrip().lstrip('"').rstrip('"')
		    baseobjectsrcfile = baseobjectincludefile.replace('interface','src').rstrip('.h') + '.cc'
		    if(self.verbose > 1):
			print '\tLook for it in the sourcefile ' + self.sourcetree + baseobjectsrcfile
		    
		    if(os.path.isfile(self.sourcetree + baseobjectsrcfile)):
			baseobjectdatadir = baseobjectincludefile.replace('interface','data').rstrip('.h').rstrip(thebaseobject)
			if(self.verbose > 1):
			    print '\tAnd the data dir ' + thederiveddatadir

			self.ParseSrcFile(self.sourcetree+baseobjectsrcfile,thebaseobject,thederiveddatadir,"")

	    # Go look for the #include 
	    if(objectinstantiated == True):
		# Hack for massive redirection of PoolSource
		if(includeclass == 'InputFileCatalog'):
		    includeclass = 'FileCatalog'

		if(self.verbose > 1):
		    print '\tInstantiated object. Search for include file for ' + includeclass
		for includeline in includelines:
		    if (includeline.find(includeclass) != -1 and (includeline.lstrip().startswith('#include'))):
			baseobjectincludefile = includeline.lstrip('#include').lstrip().rstrip().lstrip('"').rstrip('"')
			baseobjectsrcfile = baseobjectincludefile.replace('interface','src').rstrip('.h') + '.cc'
			if(self.verbose > 1):
			    print '\tLook for it in the package/library ' + self.sourcetree + baseobjectsrcfile

			if(os.path.isfile(self.sourcetree + baseobjectsrcfile)):
			    if(self.verbose > 1):
				print '\tParse the file ' + self.sourcetree + baseobjectsrcfile
			    baseobjectdatadir = baseobjectincludefile.replace('interface','data').rstrip('.h').rstrip(thebaseobject)
			    if(self.verbose > 1):
				print '\tAnd the data dir ' + thederiveddatadir
			    self.ParseSrcFile(self.sourcetree+baseobjectsrcfile,includeclass,thederiveddatadir,"")			
			    objectinstantiated = False

    # Handle the case of a ParameterSet being passed to an object that's been "new'd" in the original 
    # module.
    def ParsePassedParameterSet(self, thepsetname, thesrcfile, theobjectclass, thenestedpsetname, thedatadir, themodulename):

	if(self.verbose > 1):
	    print 'Parsing passed parameter set ' + thepsetname + ' passed from file ' + thesrcfile + ' to object of class ' + theobjectclass

	totalline = ''
	foundlineend = False

	srcfilehandle = open(thesrcfile)

        srcfilelines = srcfilehandle.readlines()

	for srcline in srcfilelines:
	    if(srcline.lstrip().startswith('#include') and 
	       (srcline.find(theobjectclass + '.h') != -1)):
		theincfile = srcline.lstrip('#include').lstrip().rstrip().lstrip('"').rstrip('"').lstrip('<').rstrip('>').replace('.h','.cc').replace('interface','src')

		if(self.verbose > 1):
		    print 'Look in file ' + self.sourcetree + theincfile

		newsrcfilehandle = open(self.sourcetree + theincfile)

		newsrcfilelines = newsrcfilehandle.readlines()

		startedcomment = False

		for srcline in newsrcfilelines:

		    # C-style comments. Seriously. Why?
		    if(srcline.lstrip().startswith('/*')):
			startedcomment = True
		    if(startedcomment == True and (srcline.rstrip().endswith('*/'))):
			startedcomment = False
			continue
		    if(startedcomment == True and not (srcline.rstrip().endswith('*/'))):
			continue

		    # C++-style comments
		    if(srcline.lstrip().startswith('//')):
			continue

                    if(srcline.rstrip().endswith(';')): 
                        foundlineend = True
			
                        totalline = totalline + srcline.lstrip().rstrip('\n')
                    else:
                        foundlineend = False

                        totalline = totalline + srcline.lstrip().rstrip('\n')

		    if((foundlineend == True) and totalline.find('getParameter') != -1):
			paramname = totalline.split('getParameter')[1].split('"')[1]

                        paramstring = totalline.split('"')

                        # Parameter name should be the first thing in quotes after 'getParameter'
			index = 0
			for paramsubstring in paramstring:
			    if ((paramstring[index]).find('getParameter') != -1):
				paramname = paramstring[index+1]
				break
			    index = index + 1
			paramstring2 = totalline.split('<')
			if(paramstring2[1].find('=') != -1 and paramstring2[1].find('==') == -1 and paramstring2[1].find('!=') == -1):
			    paramstring2 = totalline.split('=')[1].split('<')
                        therest = (paramstring2[1]).split('>')

                        # It looks like our parameter type uses a namespace
                        if(therest[0].find('::') != -1):
                            namespace = therest[0].split('::')[0]
                        
                            paramtype = therest[0].split('::')[1]
                            
                        else:
                            paramtype = therest[0]

			if(paramtype.lstrip().rstrip() == 'vector'):
			    isvector = True
			    if(totalline.find('vector<') != -1):
				vectype = (totalline.split('vector<')[1]).split('>')[0]
			    elif(totalline.find('vector <') != -1):
				vectype = (totalline.split('vector <')[1]).split('>')[0]

                            if(self.verbose > 1):
                                print '\tPassed parameter ' + paramtype + '<' + vectype + '>' + '\t' + paramname + '\t\t(Tracked)' 
                            
			    # Strip namespace
			    if(vectype.find('::') != -1):
				vectype = (vectype.split('::'))[1]
			    
			    paramtype = vectype

			    if(paramtype == 'int' or paramtype == 'int32'):
				paramtype = 'vint32'
			    elif(paramtype == 'unsigned'):
				paramtype = 'vunsigned'
			    elif(paramtype == 'double'):
				paramtype = 'vdouble'
			    elif(paramtype == 'string'):
				paramtype = 'vstring'
			    elif(paramtype == 'InputTag'):
				paramtype = 'VInputTag'
			    elif(paramtype == 'Labels'):
				paramtype = 'vstring'
			    elif(paramtype == 'vString'):
				paramtype = 'vstring'

                        else:
			    isvector = False

			    if(self.verbose > 1):
				print '\tPassed parameter ' + paramtype + '\t' + paramname + ' (tracked)'

			if((paramtype != 'ParameterSet') and (paramtype != 'PSet')):
			    success = self.ParseCfFile(thedatadir,themodulename,paramname,thepsetname,None,None)			

			    if(success == False):
				if(thepsetname != "None"):
				    if(success == False and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,thenestedpsetname))):
					self.paramsetmemberlist.append((thepsetname,paramtype,paramname,'',"true",self.sequencenb,thenestedpsetname,self.psetsequences[thepsetname]))
					self.sequencenb = self.sequencenb + 1			    
				elif(isvector == True):
				    if(self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None')):
					self.vecparamlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),'',"true",self.sequencenb))
					self.sequencenb = self.sequencenb + 1
				elif(isvector == False):
				    if(self.IsNewParameter(paramname.lstrip().rstrip(),self.paramlist,'None')):
					self.paramlist.append((paramtype,paramname,None,"true",self.sequencenb))
					self.sequencenb = self.sequencenb + 1
				    
			else:
			    if(totalline.find('=') != -1):
				thisparamset = totalline.split('=')[0].rstrip().lstrip()
				
				if(thisparamset.find('vector<edm::ParameterSet>') != -1):
				    thisparamset = thisparamset.split('>')[1].rstrip().lstrip()
				elif(thisparamset.find('PSet ') != -1):
				    thisparamset = thisparamset.split('PSet ')[1].rstrip().lstrip()
				elif(thisparamset.find('ParameterSet ') != -1):
				    thisparamset = thisparamset.split('ParameterSet ')[1].rstrip().lstrip()
				elif(thisparamset.find('ParameterSet& ') != -1):
				    thisparamset = thisparamset.split('ParameterSet& ')[1].rstrip().lstrip()

				if(len(re.split('\W+',thisparamset)) == 1):
				    self.psetdict[thisparamset] = paramname
				    if(paramname in self.psetsequences):
					self.psetsequencenb = self.psetsequences[paramname]
				    else:
					self.psetsequencenb = self.sequencenb
					self.psetsequences[paramname] = self.psetsequencenb

					if(thepsetname != "None"):
					    self.paramsetmemberlist.append((paramname,paramtype,'','',"true",self.sequencenb,thepsetname,self.psetsequences[paramname]))
					    self.sequencenb = self.sequencenb + 1
					elif(isvector == True):
					    self.vecparamlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),'',"true",self.sequencenb))
					    self.sequencenb = self.sequencenb + 1
					elif(isvector == False):
					    self.paramlist.append((paramtype,paramname,None,"true",self.sequencenb))
					    self.sequencenb = self.sequencenb + 1

				if(self.verbose > 0):
				    print '\tnew PSet in this object = ' + paramname

		    if((foundlineend == True) and totalline.find('getUntrackedParameter') != -1):
			paramname = totalline.split('getUntrackedParameter')[1].split('"')[1]

                        paramstring = totalline.split('"')

                        # Parameter name should be the first thing in quotes after 'getUntrackedParameter'
			index = 0
			for paramsubstring in paramstring:
			    if ((paramstring[index]).find('getParameter') != -1):
				paramname = paramstring[index+1]
				break
			    index = index + 1
			paramstring2 = totalline.split('<')

			if(paramstring2[1].find('=') != -1 and paramstring2[1].find('==') == -1 and paramstring2[1].find('!=') == -1):
			    if(totalline.split('=')[1].find('>') != -1):
				paramstring2 = totalline.split('=')[1].split('<')

                        therest = (paramstring2[1]).split('>')

                        # It looks like our parameter type uses a namespace
                        if(therest[0].find('::') != -1):
                            namespace = therest[0].split('::')[0]
                        
                            paramtype = therest[0].split('::')[1]
                            
                        else:
                            paramtype = therest[0]

			if(paramtype.lstrip().rstrip() == 'vector'):
			    isvector = True
			    if(totalline.find('vector<') != -1):
				vectype = (totalline.split('vector<')[1]).split('>')[0]
			    elif(totalline.find('vector <') != -1):
				vectype = (totalline.split('vector <')[1]).split('>')[0]

                            if(self.verbose > 1):
                                print '\tPassed parameter ' + paramtype + '<' + vectype + '>' + '\t' + paramname + '\t\t(Tracked)' 
                            
			    # Strip namespace
			    if(vectype.find('::') != -1):
				vectype = (vectype.split('::'))[1]
			    
			    paramtype = vectype

			    if(paramtype == 'int' or paramtype == 'int32'):
				paramtype = 'vint32'
			    elif(paramtype == 'unsigned'):
				paramtype = 'vunsigned'
			    elif(paramtype == 'double'):
				paramtype = 'vdouble'
			    elif(paramtype == 'string'):
				paramtype = 'vstring'
			    elif(paramtype == 'InputTag'):
				paramtype = 'VInputTag'
			    elif(paramtype == 'Labels'):
				paramtype = 'vstring'
			    elif(paramtype == 'vString'):
				paramtype = 'vstring'

                        else:
			    isvector = False

			    if(self.verbose > 1):
				print '\tPassed parameter ' + paramtype + '\t' + paramname + ' (untracked)'

			if((paramtype != 'ParameterSet') and (paramtype != 'PSet')):
			    success = self.ParseCfFile(thedatadir,themodulename,paramname,thepsetname,None,None)			

			    if(success == False):

				if(thepsetname != "None"):
				    if(success == False and (self.IsNewParameter(paramname.lstrip().rstrip(),self.paramsetmemberlist,thenestedpsetname))):
					self.paramsetmemberlist.append((thepsetname,paramtype,paramname,'',"false",self.sequencenb,thenestedpsetname,self.psetsequences[thepsetname]))
					self.sequencenb = self.sequencenb + 1			    
				elif(isvector == True):
				    if(self.IsNewParameter(paramname.lstrip().rstrip(),self.vecparamlist,'None')):
					self.vecparamlist.append((paramtype.lstrip().rstrip(),paramname.lstrip().rstrip(),'',"false",self.sequencenb))
					self.sequencenb = self.sequencenb + 1
				elif(isvector == False):
				    if(self.IsNewParameter(paramname.lstrip().rstrip(),self.paramlist,'None')):
					self.paramlist.append((paramtype,paramname,None,"false",self.sequencenb))
					self.sequencenb = self.sequencenb + 1


		    # Look for ParameterSets passed to objects instantiated within this module. This won't pick up PSets 
		    # passed to methods of the new object - are there any cases of this?
		    if((foundlineend == True) and (totalline.find('new ') != -1) and (totalline.find('(') != -1)):
			newtheobjectclass = ''
			newtheobjectargument = ''

			if(len(totalline.split('new ')) == 1):
			    newtheobjectclass = totalline.split('new ')[0].split('(')[0].lstrip().rstrip()
			elif(len(totalline.split('new ')) == 2):
			    newtheobjectclass = totalline.split('new ')[1].split('(')[0].lstrip().rstrip()
			elif(len(totalline.split('new ')) == 3):
			    newtheobjectclass = totalline.split('new ')[2].split('(')[0].lstrip().rstrip()

			if(newtheobjectclass):
			    if(len(totalline.split(newtheobjectclass)) == 2):
				newtheobjectargument = totalline.split(newtheobjectclass)[1].lstrip('(')
			    elif(len(totalline.split(newtheobjectclass)) == 3):
				newtheobjectargument = totalline.split(newtheobjectclass)[2].lstrip('(')
    
			    if(totalline.find(',') != -1):
				newthepassedpset = newtheobjectargument.split(',')[0].lstrip().rstrip()
			    else:
				newthepassedpset = newtheobjectargument.split(')')[0].lstrip('(').rstrip(');').lstrip().rstrip()

			    if(newthepassedpset in self.psetdict):
				newpsettype = self.psetdict[newthepassedpset] 
				if(self.verbose > 1):
				    print 'Found pset of type ' + newpsettype + ', nested in the PSet ' + thepsetname + ', passed to object of type ' + newtheobjectclass
				self.ParsePassedParameterSet(newpsettype, self.sourcetree + theincfile, newtheobjectclass, thepsetname, thedatadir, themodulename)

		    # This line is uninteresting
		    if(foundlineend == True and srcline.find('getParameter') == -1 and srcline.find('getUntrackedParameter') == -1):
			foundlineend = False
			totalline = ''


    # Check whether a variable has already been parsed
    def IsNewParameter(self, parametername, parameterlist, parameterset):

	for listings in parameterlist:
	    if(parameterset == 'None'):
		if(parametername in listings):
		    return False
	    else:
		if((listings[2] == parametername) and (listings[0] == parameterset)):
		    return False
	    
	return True

    # Check if a PSet has already been parsed
    def IsNewParameterSet(self, parameterlist, parameterset):

	for listings in parameterlist:
	    if(parameterset in listings):
		return False
	    
	return True

    # Return the parameters and default values associated with a module
    def GetParams(self ,modname):
	if(self.verbose > 0):
	    print "\tDumping parameters for module/service " + modname + "(" + self.baseclass + ")"
	    
	    for ptype, pname, pval, ptracked, pseq in self.paramlist:
		print "\t\t" + ptype + "\t" + pname + "\t" + str(pval) + "\t(tracked = " + str(ptracked) + ")" + " sequenceNb = " + str(pseq)

        return self.paramlist

    # Return the vector parameters and default values associated with a module
    def GetVectorParams(self, modname):
	if(self.verbose > 0):
	    print "\tDumping vector parameters for module " + modname + "(" + self.baseclass + ")"

	    for vecptype, vecpname, vecpvals, vectracked, vecpseq in self.vecparamlist:
		print "\t\t" + vecptype + "\t" + vecpname + "\t(tracked = " + str(vectracked) + ")" + " sequenceNb = " + str(vecpseq)

		for vecpval in vecpvals:
		    print "\t\t\t" + vecpval
        
    
        return self.vecparamlist

    # Return the PSets associated with this module
    def GetParamSets(self, modname):
	if(self.verbose > 0):
	    print "\tDumping parameter sets for module " + modname + "(" + self.baseclass + ")"

	    for pset, psettype, psetname, psetval, psettracked, psetseq, psetnesting, psetpsetseq in self.paramsetmemberlist:
		print "\t\t" + pset + "\t" + psettype + "\t" + psetname + "\t" + psetval + "\t(tracked = " + str(psettracked) + ")" +  " param sequenceNb = " + str(psetseq) + " pset sequenceNb = " + str(psetpsetseq) + " nested in (" + psetnesting + ")"

	# Now reorder the list so that top-level PSets will be loaded to the DB first.
	tempparamsetmemberlist = []
		
	for pset, psettype, psetname, psetval, psettracked, psetseq, psetnesting, psetpsetseq in self.paramsetmemberlist:
	    if(psetnesting == 'None'):
		tempparamsetmemberlist.append((pset, psettype, psetname, psetval, psettracked, psetseq, psetnesting, psetpsetseq))
	for pset, psettype, psetname, psetval, psettracked, psetseq, psetnesting, psetpsetseq in self.paramsetmemberlist:
	    if(psetnesting != 'None'):
		tempparamsetmemberlist.append((pset, psettype, psetname, psetval, psettracked, psetseq, psetnesting, psetpsetseq))	

	return tempparamsetmemberlist
    
    # Return the VPSets associated with this module
    def GetVecParamSets(self, modname):
	if(self.verbose > 0):
	    print "\tDumping <vector>parameter sets for module " + modname + "(" + self.baseclass + ")"

	    for vpset, vpsettype, vpsetname, vpsetval, vpsettracked, vpsetindex, vpsetseq, vpsetpsetseq in self.vecparamsetmemberlist:
		print "\t\t" + vpset + "\t" + vpsettype + "\t" + vpsetname + "\t" + vpsetval + "\t(tracked = " + str(vpsettracked) + ") [" + str(vpsetindex) + "]" +  " param sequenceNb = " + str(vpsetseq) + "vpset sequenceNb = " + str(vpsetpsetseq)

    
        return self.vecparamsetmemberlist	    

    # Return the module base class
    def GetBaseClass(self):
        return self.baseclass
        
    # Return parameters where we failed to find a default value
    def ShowParamFailures(self):
	if(len(self.paramfailures) > 1):
	    print "\tMessage: Could not find defaults for (component, (type)parameter):"
	    for mod, paramtype, param, istracked, paramseq in self.paramfailures:
		print "\t\t" + mod + "\t\t(" + paramtype + ")" + param + "\t\t(Tracked = " + istracked + ")" + str(paramseq)


    # Set the verbosity
    def SetVerbosity(self, verbosity):
	self.verbose = verbosity

    # Erase stored parameters for the next module
    def ResetParams(self):
        self.paramlist = []

        self.vecparamlist = []

	self.paramsetlist = []

	self.paramsetmemberlist = []

	self.vecparamsetmemberlist = []

	self.psetdict = {}
	
	self.psetsequences = {}

	self.sequencenb = 1

	self.inheritancelevel = 0

        self.baseclass = ""

	self.includefile = ""

	self.mainpset = ""
