#!/usr/bin/env python

# ConfdbLoadParamsfromConfigs.py
# Main file for parsing python configs in a CMSSW release and
# loading templates to the Conf DB.
# 
# Jonathan Hollar LLNL Nov. 3, 2008

import os, string, sys, posix, tokenize, array, getopt
import ConfdbOracleModuleLoader
import FWCore.ParameterSet.Config as cms

def main(argv):
    # Get information from the environment
    input_base_path = os.environ.get("CMSSW_RELEASE_BASE")
    input_cmsswrel = os.environ.get("CMSSW_VERSION")
    input_addfromreldir = os.environ.get("CMSSW_BASE")
    input_baserelease_path = input_base_path

    # User can provide a list of packages to ignore...
    input_usingblacklist = False
    input_blacklist = []

    # or a list of packages (and only these packages) to use
    input_usingwhitelist = False
    #    input_whitelist = []
    input_whitelist = ["RecoMuon","RecoTracker","HLTrigger"]
    input_tempwhitelise = [
        "CalibCalorimetry",
        "CalibMuon",
        "CalibTracker",
        "Calibration",
        "CondCore",
        "DQM",
        "DQMServices",
        "EventFilter",
        "FWCore",
        "Geometry",
        "GeometryReaders",
        "HLTrigger",
        "IOPool",
        "IORawData",
        "JetMETCorrections",
        "L1Trigger",
        "L1TriggerConfig",
        "MagneticField",
        "PhysicsTools",
        "RecoBTag",
        "RecoBTau",
        "RecoCaloTools",
        "RecoEcal",
        "RecoEgamma",
        "RecoJets",
        "RecoLocalCalo",
        "RecoLocalMuon",
        "RecoLocalTracker",
        "RecoLuminosity",
        "RecoMET",
        "RecoMuon",
        "RecoPixelVertexing",
        "RecoTauTag",
        "RecoTracker",
        "RecoVertex",
        "SimCalorimetry",
        "SimGeneral",
        "TrackPropagation",
        "TrackingTools"
        ]

    input_verbose = 3
    input_dbuser = "CMS_HLT_TEST"
    input_dbpwd = "hltdevtest1"
    input_host = "CMS_ORCOFF_INT2R"
    input_addtorelease = "none"
    input_comparetorelease = ""
    input_noload = False
    
    print "Using release base: " + input_base_path

    confdbjob = ConfdbLoadParamsfromConfigs(input_cmsswrel,input_baserelease_path,input_whitelist,input_blacklist,input_usingwhitelist,input_usingblacklist,input_verbose,input_dbuser,input_dbpwd,input_host,input_noload,input_addtorelease,input_comparetorelease)
    confdbjob.BeginJob()

class ConfdbLoadParamsfromConfigs:
    def __init__(self,clirel,clibasereleasepath,cliwhitelist,cliblacklist,cliusingwhitelist,cliusingblacklist,cliverbose,clidbuser,clidbpwd,clihost,clinoload,cliaddtorelease,clicomparetorelease):

        self.dbname = ''
        self.dbuser = clidbuser
        self.verbose = int(cliverbose)
        self.dbpwd = clidbpwd
        self.dbhost = clihost
        self.verbose = cliverbose
        self.noload = clinoload
        self.addtorelease = cliaddtorelease
        self.comparetorelease = clicomparetorelease
        self.componenttable = ''
        self.softpackageid = ''

        # Track CVS tags
        self.cvstag = ''
        self.packageid = ''
        self.tagtuple = []
        self.basereltagtuple = []

	# Get a Conf DB connection here. Only need to do this once at the 
	# beginning of a job.
        self.dbloader = ConfdbOracleModuleLoader.ConfdbOracleModuleLoader(self.verbose,self.addtorelease,self.comparetorelease)
        self.dbcursor = self.dbloader.ConfdbOracleConnect(self.dbname,self.dbuser,self.dbpwd,self.dbhost)

        self.modtypedict = {}
        self.paramtypedict = {}
        self.paramtabledict = {}
        
	# Deal with package tags for this release.
	self.cmsswrel = clirel
        self.cmsswrelid = 0
	self.baserelease_path = clibasereleasepath
        self.base_path = clibasereleasepath
        self.whitelist = cliwhitelist

        # Global bookkeeping
        self.localseq = 0
        self.immediatesuperid = 0

    def BeginJob(self):
	# List of all available modules
	sealcomponenttuple = []

	# Track all modules which will be modified
	modifiedmodules = []

	source_tree = self.base_path + "//src/"

	print "The  source tree is: " + source_tree

        if(not (os.path.isdir(self.base_path))):
            print 'Fatal error: release source tree not found. Exiting now'
            return

        # Find this release in the DB
        self.dbcursor.execute("SELECT SoftwareReleases.releaseId FROM SoftwareReleases WHERE (releaseTag = '" + self.cmsswrel + "')")
        self.cmsswrelid = self.dbcursor.fetchone()[0]

        # Find CVS tags
        self.GetCVSTags()

        # Do some one-time operations - get dictionaries of parameter, module,
        # and service type mappings so we don't have to do this every time
        self.dbcursor.execute("SELECT ParameterTypes.paramType, ParameterTypes.paramTypeId FROM ParameterTypes")
        temptuple = self.dbcursor.fetchall()
        for temptype, tempname in temptuple:
            self.paramtypedict[temptype] = tempname

        self.dbcursor.execute("SELECT ModuleTypes.type, ModuleTypes.typeId FROM ModuleTypes")
        temptuple = self.dbcursor.fetchall()
        for temptype, tempname in temptuple:
            self.modtypedict[temptype] = tempname

        self.paramtabledict = {"int32":"Int32ParamValues",
                               "vint32":"VInt32ParamValues",
                               "uint32":"UInt32ParamValues",
                               "vuint32":"VUInt32ParamValues",
                               "int64":"Int64ParamValues",
                               "vint64":"VInt64ParamValues",
                               "uint64":"UInt64ParamValues",
                               "vuint64":"VUInt64ParamValues",
                               "bool":"BoolParamValues",
                               "double":"DoubleParamValues",
                               "vdouble":"VDoubleParamValues",
                               "string":"StringParamValues",
                               "vstring":"VStringParamValues",
                               "InputTag":"InputTagParamValues",
                               "VInputTag":"VInputTagParamValues",
                               "EventID":"EventIDParamValues",
                               "VEventID":"VEventIDParamValues",
                               "FileInPath":"FileInPathParamValues"}
                               
	# Get package list for this release
	packagelist = os.listdir(source_tree)

	# Start decending into the source tree
	for package in packagelist:

            if(not (os.path.isdir(self.base_path))):
                print 'Fatal error: release source tree not found. Exiting now'
                return

            if(not package in self.whitelist):
                continue

	    # Check if this is really a directory
	    if(os.path.isdir(source_tree + package)):

		subdirlist = os.listdir(source_tree + package)

		for subdir in subdirlist:
                    if(subdir.startswith(".")):
                       continue
		    # Check if the user really wants to use this package

		    packagedir = source_tree + package + "/" + subdir
		
                    self.VerbosePrint("Scanning package: " + package + "/" + subdir, 0)
                
		    srcdir = packagedir + "/src/"
		    testdir = packagedir + "/test/"
		    pluginsdir = packagedir + "/plugins/"
                    pydir = packagedir + "/python/"

                    if(not os.path.isdir(pydir)):
                        continue

                    # Retrieve the CVS tag
                    packagename = package+"/"+subdir
                    for modtag, cvstag in self.tagtuple:
                        if(modtag.lstrip().rstrip() == packagename.lstrip().rstrip()):
                            self.cvstag = cvstag
                            self.VerbosePrint("\tCVS tag: " + cvstag,0)

                    self.GetPackageID(package,subdir)
                            
                    pyfiles = os.listdir(pydir)

                    for pyfile in pyfiles:
                        if(pyfile.endswith("_cfi.py")):
                            #                            print "the pyfile = " + str(pyfile)
                            thecomponent = pyfile.split('.py')[0]
                            thebasecomponent = thecomponent.split('_cfi')[0]

                            # Construct the py-cfi to import
                            thesubsystempackage = pydir.split('src/')[1].split('/data/')[0].lstrip().rstrip()
                            thesubsystem = thesubsystempackage.split('/')[0]
                            thepackage = thesubsystempackage.split('/')[1]
                            importcommand = "import " + thesubsystem + "." + thepackage + "." + thecomponent
                            #                            print importcommand
                            process = cms.Process("MyProcess")

                            try:
                                exec importcommand
                                # Now create a process and construct the command to extend it with the py-cfi
                                theextend = "process.extend(" + thesubsystem + "." + thepackage + "." + thecomponent + ")"
                                eval(theextend)

                                myproducers = process.producers_()
                                self.componenttable = "ModuleTemplates"
                                self.FindParamsFromPython(thesubsystem, thepackage, myproducers,"EDProducer")

                                myfilters = process.filters_()
                                self.componenttable = "ModuleTemplates"
                                self.FindParamsFromPython(thesubsystem, thepackage, myfilters,"EDFilter") 

                                myservices = process.services_()
                                self.componenttable = "ServiceTemplates"                                
                                self.FindParamsFromPython(thesubsystem, thepackage, myservices,"Service") 

                                #                                myanalyzers = process.analyzers_()
                                #                                self.FindParamsFromPython(thesubsystem, thepackage, myanalyzers,"EDAnalyzer") 
                                
                                myoutputmodules = process.outputModules_()
                                self.componenttable = "ModuleTemplates"
                                self.FindParamsFromPython(thesubsystem, thepackage, myoutputmodules,"OutputModule")

                                myessources = process.es_sources_()
                                self.componenttable = "ESSourceTemplates"                                
                                self.FindParamsFromPython(thesubsystem, thepackage, myessources,"ESSource") 
                                            
                                myesproducers = process.es_producers_()
                                self.componenttable = "ESModuleTemplates"                                
                                self.FindParamsFromPython(thesubsystem, thepackage, myesproducers,"ESModule") 

#                            except NameError:
#                                print "Name Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
#                                continue

#                            except TypeError:
#                                print "Type Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
#                                continue

                            except ImportError:
                                print "Import Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue

                            except SyntaxError:
                                print "Syntax Error exception in " + thesubsystem + "." + thepackage + "." + thecomponent
                                continue

        # Commit and disconnect to be compatible with either INNODB or MyISAM
        self.dbloader.ConfdbExitGracefully()
                
    def DoPsetRecursion(self,psetval,psetname,psetsid):

        params = psetval.parameters_()
        subobjectsuperid = -1

        for paramname, paramval in params.iteritems():
            if(paramval.configTypeName() == "PSet" or paramval.configTypeName() == "untracked PSet"):
                subobjectsuperid = self.LoadUpdatePSet(paramname,psetname,paramval,psetsid)
                self.DoPsetRecursion(paramval,psetname+"."+paramname,subobjectsuperid)

            elif(paramval.configTypeName().find("VPSet") == -1):
                self.VerbosePrint("\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval),1)
                self.LoadUpdateParam(paramname,psetname,paramval,psetsid)
                
            else:
                self.VerbosePrint("\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval.configTypeName()) + "[" + str(len(paramval)) + "]",1)
                i = 1
                for vpsetentry in paramval:
                    subobjectsuperid = self.LoadUpdatePSet(paramname,psetname,paramval,psetsid)
                    self.DoPsetRecursion(vpsetentry,psetname+'['+str(i)+']',subobjectsuperid)
                    i = i + 1
    
    def FindParamsFromPython(self, thesubsystem, thepackage, mycomponents, componenttype):

        for name, value in mycomponents.iteritems():
            psetname = "TopLevel"
            self.VerbosePrint("(" + str(componenttype) + " " + value.type_() + ") " + thesubsystem + "." + thepackage + "." + name, 1)
            componentsuperid = self.LoadUpdateComponent(value.type_(),componenttype)
            objectsuperid = -1
            vobjectsuperid = -1
            self.localseq = 1

            params = value.parameters_()

            for paramname, paramval in params.iteritems():
                if(paramval.configTypeName() == "PSet" or paramval.configTypeName() == "untracked PSet"):
                    psetname = paramname
                    objectsuperid = self.LoadUpdatePSet(paramname,psetname,paramval,componentsuperid)
                    self.DoPsetRecursion(paramval,paramname,objectsuperid)

                elif(paramval.configTypeName().find("VPSet") == -1):
                    self.VerbosePrint("\t\t" + str(psetname) + "." + str(paramname) + "\t" + str(paramval), 1)
                    self.LoadUpdateParam(paramname,psetname,paramval,componentsuperid)

                else:
                    vobjectsuperid = self.LoadUpdateVPSet(paramname,psetname,paramval,componentsuperid)
                    self.VerbosePrint("\t\t" + str(paramname) + "\t" + str(paramval.configTypeName()) + "[" + str(len(paramval)) + "]", 1)
                    psetname = str(paramval.configTypeName()) + "[" + str(len(paramval)) + "]"
                    i = 1
                    for vpsetentry in paramval:
                        vobjectmembersuperid = self.LoadUpdatePSet(paramname,psetname,paramval,vobjectsuperid) 
                        self.DoPsetRecursion(vpsetentry,paramname+'['+str(i)+']',vobjectmembersuperid)
                        i = i + 1


    def FindObjectSuperId(self):
        print "Not yet"
        
    def LoadUpdateComponent(self,componentname,componenttype):

        componenttable = self.componenttable
        modtypestr = ''
            
        selectstring = "SELECT " + componenttable + ".superId, " + componenttable + ".name, " + componenttable + ".cvstag, " + componenttable + ".packageId FROM " + componenttable + " JOIN SuperIdReleaseAssoc ON (SuperIdReleaseAssoc.superId = " + componenttable + ".superId) WHERE (SuperIdReleaseAssoc.releaseId = " + str(self.cmsswrelid) + ") AND (" + componenttable + ".name = '" + componentname + "')"
        self.VerbosePrint(selectstring, 2)

        self.dbcursor.execute(selectstring)

        componentsuperid = self.dbcursor.fetchone()

        self.VerbosePrint("ConfDB says: " + str(componentsuperid), 2)

        if(componentsuperid):
            # The module already exists! Return its superID
            returnid = componentsuperid[0]
        else:
            # The module doesn't exist! Let's add it.
            newsuperid = -1
            self.dbcursor.execute("INSERT INTO SuperIds VALUES('')")

            self.dbcursor.execute("SELECT SuperId_Sequence.currval from dual")
            newsuperid = (self.dbcursor.fetchall()[0])[0]

            # Attach this template to the currect release
            self.dbcursor.execute("INSERT INTO SuperIdReleaseAssoc (superId, releaseId) VALUES (" + str(newsuperid) + ", " + str(self.cmsswrelid) + ")")
            
            # Now create a new module
            insertstring = ''
            if(self.componenttable == "ModuleTemplates"):
                modbaseclassid = self.modtypedict[componenttype]
                insertstring = "INSERT INTO " + self.componenttable + "(superId, typeId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", " + str(modbaseclassid) + ", '" + componentname + "', '" + self.cvstag  + "', '" + str(self.softpackageid) + "')"
            else:
                insertstring = "INSERT INTO " + self.componenttable + "(superId, name, cvstag, packageId) VALUES (" + str(newsuperid) + ", '" + componentname + "', '" + self.cvstag  + "', '" + str(self.softpackageid) + "')"

            self.VerbosePrint(insertstring, 3)
            self.dbcursor.execute(insertstring)

            returnid = newsuperid

            print 'JH: We added ' + componentname + ' with superId = ' + str(returnid)

        return returnid

    def LoadUpdateParam(self,parametername,psetname,pval,sid):

        returnid = 0
        paramistracked = 0

        parametervalue = pval.value()
        parametertype = pval.configTypeName()
        parametertracked = pval.isTracked()

        if(parametertype.find("untracked") != -1):
            parametertype = parametertype.split("untracked")[1].lstrip().rstrip()

        # Reformat representations of Booleans for python -> Oracle

        # Tracked true/false -> 1/0
        if(parametertracked == True):
            paramistracked =  1
        else:
            paramistracked = 0

        # Boolean parameter true/false -> 1/0
        if(parametertype == "bool"):
            if(parametervalue == True):
                parametervalue = str(1)
            if(parametervalue == False):
                parametervalue = str(0)

        selectstr = "SELECT SuperIdParameterAssoc.paramId FROM SuperIdParameterAssoc JOIN Parameters ON (Parameters.name = '" + parametername + "') WHERE (SuperIdParameterAssoc.superId = " + str(sid) + ") AND (SuperIdParameterAssoc.paramId = Parameters.paramId)"

        self.VerbosePrint(selectstr, 3)
        self.dbcursor.execute(selectstr)

        paramid = self.dbcursor.fetchone()

        self.VerbosePrint("ConfDB says: " + str(paramid), 3)

        if(paramid):
            returnid = paramid[0]
        else:
                intparametertype = self.paramtypedict[parametertype]
                paramtable = self.paramtabledict[parametertype]
                
                insertstr1 = "INSERT INTO Parameters (paramTypeId, name, tracked) VALUES (" + str(intparametertype) + ", '" + parametername + "', " + str(paramistracked) + ")"
                self.VerbosePrint(insertstr1, 3)
                self.dbcursor.execute(insertstr1)
                
                self.dbcursor.execute("SELECT ParamId_Sequence.currval from dual")
                newparamid = self.dbcursor.fetchone()[0] 

                self.dbcursor.execute("SELECT sequenceNb FROM SuperIdParameterAssoc WHERE superId = " + str(sid) + " ORDER BY sequenceNb DESC")
                nextseqid = self.dbcursor.fetchone()
                if(nextseqid):
                    self.localseq = nextseqid[0] + 1
                    
                insertstr2 = "INSERT INTO SuperIdParameterAssoc (superId, paramId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(self.localseq) + ")"
                self.VerbosePrint(insertstr2, 3)
                self.dbcursor.execute(insertstr2)                                                

                returnid = newparamid

                print 'JH: We added ' + str(parametername) + ' with paramId = ' + str(newparamid)                            

                self.localseq = self.localseq + 1
                
        
                if((not parametertype.startswith('v')) and (not parametertype.startswith('V'))):
                    insertstr3 = "INSERT INTO " + str(paramtable) + " (paramId, value) VALUES (" + str(newparamid) + ", '" + str(parametervalue) + "')"
                    self.VerbosePrint(insertstr3, 3)
                    self.dbcursor.execute(insertstr3)

                else:
                    paramindex = 1
                    for parametervectorvalue in parametervalue:
                        insertstr3 = "INSERT INTO " + str(paramtable) + " (paramId, sequenceNb, value) VALUES (" + str(newparamid) + ", " + str(paramindex) + ", '" + str(parametervectorvalue) + "')"
                        self.VerbosePrint(insertstr3, 3)
                        self.dbcursor.execute(insertstr3)
                        paramindex = paramindex + 1

        return returnid

    def LoadUpdatePSet(self,parametername,psetname,pval,sid):

        returnid = 0
        paramistracked = 0

        parametervalue = pval.value()
        parametertype = pval.configTypeName()
        parametertracked = pval.isTracked()

        if(psetname.find("[") != -1 and psetname.find("]") != -1):
            parametername = ''

        # Reformat representations of Booleans for python -> Oracle

        # Tracked true/false -> 1/0
        if(parametertracked == True):
            paramistracked =  1
        else:
            paramistracked = 0
                                                 
        selectstr = "SELECT SuperIdParamSetAssoc.psetId FROM SuperIdParamSetAssoc JOIN ParameterSets ON (ParameterSets.name = '" + parametername + "') WHERE (SuperIdParamSetAssoc.superId = " + str(sid) + ") AND (SuperIdParamSetAssoc.psetId = ParameterSets.superId)"

        self.VerbosePrint(selectstr, 3)
        self.dbcursor.execute(selectstr)

        paramid = self.dbcursor.fetchone()
        
        self.VerbosePrint("ConfDB says PSet has: " + str(paramid), 2)

        if(paramid):
            returnid = paramid[0]
        else:
            self.dbcursor.execute("INSERT INTO SuperIds VALUES('')")

            self.dbcursor.execute("SELECT SuperId_Sequence.currval from dual")
            newparamid = self.dbcursor.fetchone()[0]

            insertstr1 = "INSERT INTO ParameterSets (superId, name, tracked) VALUES (" + str(newparamid) + ", '" + parametername + "', " + str(paramistracked) + ")"
            self.VerbosePrint(insertstr1, 3)
            self.dbcursor.execute(insertstr1)
                                                                                                 
            self.dbcursor.execute("SELECT sequenceNb FROM SuperIdParamSetAssoc WHERE superId = " + str(sid) + " ORDER BY sequenceNb DESC")
            nextseqid = self.dbcursor.fetchone()
            if(nextseqid):
                self.localseq = nextseqid[0] + 1
                
            insertstr2 = "INSERT INTO SuperIdParamSetAssoc (superId, psetId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(self.localseq) + ")"
            self.VerbosePrint(insertstr2, 3)
            self.dbcursor.execute(insertstr2)

            self.localseq = self.localseq + 1
            
            print 'JH: We added PSet ' + str(parametername) + ' with superId = ' + str(newparamid)
            
            returnid = newparamid

        return returnid

    def LoadUpdateVPSet(self,parametername,psetname,pval,sid):

        returnid = 0
        paramistracked = 0

        parametervalue = pval.value()
        parametertype = pval.configTypeName()
        parametertracked = pval.isTracked()

        # Reformat representations of Booleans for python -> Oracle

        # Tracked true/false -> 1/0
        if(parametertracked == True):
            paramistracked =  1
        else:
            paramistracked = 0
                                                 
        selectstr = "SELECT SuperIdVecParamSetAssoc.vpsetId FROM SuperIdVecParamSetAssoc JOIN VecParameterSets ON (VecParameterSets.name = '" + parametername + "') WHERE (SuperIdVecParamSetAssoc.superId = " + str(sid) + ") AND (SuperIdVecParamSetAssoc.vpsetId = VecParameterSets.superId)"

        self.VerbosePrint(selectstr, 3)
        self.dbcursor.execute(selectstr)

        paramid = self.dbcursor.fetchone()
        
        self.VerbosePrint("ConfDB says VPSet has: " + str(paramid), 2)

        if(paramid):
            returnid = paramid[0]
        else:
            self.dbcursor.execute("INSERT INTO SuperIds VALUES('')")

            self.dbcursor.execute("SELECT SuperId_Sequence.currval from dual")
            newparamid = self.dbcursor.fetchone()[0]

            insertstr1 = "INSERT INTO VecParameterSets (superId, name, tracked) VALUES (" + str(newparamid) + ", '" + parametername + "', " + str(paramistracked) + ")"
            self.VerbosePrint(insertstr1, 3)
            self.dbcursor.execute(insertstr1)
                                                                                                 
            self.dbcursor.execute("SELECT sequenceNb FROM SuperIdVecParamSetAssoc WHERE superId = " + str(sid) + " ORDER BY sequenceNb DESC")
            nextseqid = self.dbcursor.fetchone()
            if(nextseqid):
                self.localseq = nextseqid[0] + 1
                
            insertstr2 = "INSERT INTO SuperIdVecParamSetAssoc (superId, vpsetId, sequenceNb) VALUES (" + str(sid) + ", " + str(newparamid) + ", " + str(self.localseq) + ")"
            self.VerbosePrint(insertstr2, 3)
            self.dbcursor.execute(insertstr2)

            self.localseq = self.localseq + 1
            
            print 'JH: We added VPSet ' + str(parametername) + ' with superId = ' + str(newparamid)
            
            returnid = newparamid

        return returnid

    def GetCVSTags(self):
        if(self.addtorelease != "none"):
            # If we're creating an intermediate release, get the list of tags from the *base* release
            if(os.path.isfile(self.baserelease_path + "//src/PackageList.cmssw")):
                tagfile = open(self.baserelease_path + "//src/PackageList.cmssw")
                taglines = tagfile.readlines()
                for tagline in taglines:
                    self.tagtuple.append(((tagline.split())[0], (tagline.split())[1]))
        else:
            # Otherwise, get the list of tags from *this* release
            if(os.path.isfile(self.base_path + "//src/PackageList.cmssw")):
                tagfile = open(self.base_path + "//src/PackageList.cmssw")
                taglines = tagfile.readlines()
                for tagline in taglines:
                    self.tagtuple.append(((tagline.split())[0], (tagline.split())[1]))

    def GetPackageID(self,subsystem,package):

        packageid = self.dbloader.ConfdbInsertPackageSubsystem(self.dbcursor,subsystem,package)

        self.softpackageid = packageid

        self.VerbosePrint("Package ID = " + str(self.softpackageid), 2)

    def VerbosePrint(self,message,severity):
        if(self.verbose >= severity):
            print message
        
if __name__ == "__main__":
    main(sys.argv[1:])
