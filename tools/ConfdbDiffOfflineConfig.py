#!/usr/bin/env python

# Jonathan Hollar LLNL Sept. 25, 2008

import os, string, sys, posix, tokenize, array, getopt
import FWCore.ParameterSet.Config as cms
import CMSSWold_cff as oldconf

def main(argv):
    confdiff = ConfdbDiffOfflineConfig()
    confdiff.CompareConfigs()

class ConfdbDiffOfflineConfig:
    def __init__(self):
        self.newrelname = os.environ.get("CMSSW_VERSION")
        self.oldrelname = os.environ.get("THE_OLD_RELEASE_NAME")
        self.process2 = ''
        self.nchanges = 0

    def CompareConfigs(self):
        
        thecfffile = "Configuration.StandardSequences.Reconstruction_cff"
#        thecfffile = "Configuration.StandardSequences.Digi_cff"
#        thecfffile = "Configuration.StandardSequences.Simulation_cff"
#        thecfffile = "Configuration.StandardSequences.Generator_cff"
#        thecfffile = "HLTrigger.Configuration.HLT_2E30_cff")
        
        
        # Now create a process and construct the command to extend it with the py-cfi
        self.process2 = cms.Process("MyNewProcess")
        self.process2.load(thecfffile)
        
        myproducers = oldconf.process.producers_()
        myproducers2 = self.process2.producers_()
        myfilters = oldconf.process.filters_()
        myfilters2 = self.process2.filters_()
        myservices = oldconf.process.services_()
        myservices2 = self.process2.services_()
        myoutputmodules = oldconf.process.outputModules_()
        myoutputmodules2 = self.process2.outputModules_()
        myessources = oldconf.process.es_sources_()
        myessources2 = self.process2.es_sources_() 
        myesproducers = oldconf.process.es_producers_()
        myesproducers2 = self.process2.es_producers_()
        
        procname = 'self.process2'
        componenttype = ''
        rel1 = self.oldrelname
        rel2 = self.newrelname

        print '---++ diff of reconstruction_cff in ' + str(self.oldrelname) + ' vs. ' + str(self.newrelname)
        print '| *Module type* | *Module and parameter name* | *' + str(self.oldrelname) + ' value* | *' + str(self.newrelname) + ' value* |'

        for name, value in myproducers.iteritems():
            componenttype = str(value.type_())            
            self.CompareComponents(procname,name,value,componenttype,rel1,rel2,1)
                    
        for name, value in myfilters.iteritems():
            componenttype = str(value.type_())            
            self.CompareComponents(procname,name,value,componenttype,rel1,rel2,1) 

        for name, value in myservices.iteritems():
            componenttype = str(value.type_())            
            self.CompareComponents(procname,name,value,componenttype,rel1,rel2,1) 

        for name, value in myoutputmodules.iteritems():
            componenttype = str(value.type_())
            self.CompareComponents(procname,name,value,componenttype,rel1,rel2,1)
                                    
        for name, value in myessources.iteritems():
            componenttype = str(value.type_())
            self.CompareComponents(procname,name,value,componenttype,rel1,rel2,1)
                                    
        for name, value in myesproducers.iteritems():
            componenttype = str(value.type_())
            self.CompareComponents(procname,name,value,componenttype,rel1,rel2,1)
                                    

        #OK, now go backwards and look for parameters that were added
        procname = 'oldconf.process'
        rel1 = self.newrelname
        rel2 = self.oldrelname

        for name, value in myproducers2.iteritems():
            componenttype = str(value.type_())            
            self.CompareComponents(procname,name,value,componenttype,rel2,rel1,0)  

        for name, value in myfilters2.iteritems():
            componenttype = str(value.type_())            
            self.CompareComponents(procname,name,value,componenttype,rel2,rel1,0)   

        for name, value in myservices2.iteritems():
            componenttype = str(value.type_())            
            self.CompareComponents(procname,name,value,componenttype,rel2,rel1,0)   

        for name, value in myoutputmodules2.iteritems():
            componenttype = str(value.type_())
            self.CompareComponents(procname,name,value,componenttype,rel2,rel1,0)
                                    

        for name, value in myessources2.iteritems():
            componenttype = str(value.type_())
            self.CompareComponents(procname,name,value,componenttype,rel2,rel1,0)
                                    
        for name, value in myesproducers2.iteritems():
            componenttype = str(value.type_())
            self.CompareComponents(procname,name,value,componenttype,rel2,rel1,0)
                                    

#        print '\n============================================================================='
#        print 'Found ' + str(self.nchanges) + ' parameters that were changed, added, or removed'
#        print '============================================================================='

    def CompareComponents(self,processname,name,value,componenttype,rel1,rel2,recorddiffs):
        params = value.parameters_()
        for paramname, paramval in params.iteritems():
            vpsetval = ''
            if(paramval.configTypeName() == "VPSet"):
                vpsetval = paramval.value()
            elif(paramval.configTypeName() == "PSet"):
                subquery = name + "." + paramname
                self.CompareComponents(processname,subquery,paramval,componenttype,rel1,rel2,recorddiffs)
            
            newquery = str(processname) + "." + name + "." + paramname
            newparamval = ''
            formatparamval = ''
            formatnewparamval = ''
            formatvpsetval = ''
            formatnewvpsetval = ''

            try:
                newparamval = eval(newquery)
                if(paramval.configTypeName() == "VPSet"):
                    newvpsetval = newparamval.value()
                    if(str(vpsetval) != str(newvpsetval)):
                        if(str(vpsetval).find("\n") != -1):
                            formatvpsetval = str(vpsetval).replace("\n"," ")
                            formatvpsetval = str(formatvpsetval).replace("     "," ")
                        else:
                            formatvpsetval = str(vpsetval)
                        if(str(newvpsetval).find("\n") != -1):
                            formatnewvpsetval = str(newvpsetval).replace("\n"," ")
                            formatnewvpsetval = str(formatnewvpsetval).replace("     "," ")
                        else:
                            formatnewvpsetval = str(newvpsetval)
                                                                                                                                                                                                                
                        if(recorddiffs == 1):
                            print "| (!" + componenttype + ") | " + str(name) + "." + str(paramname) + " | " + str(formatvpsetval)  + " | " + str(formatnewvpsetval) + " | "
                            self.nchanges = self.nchanges + 1
                        
                elif(paramval.configTypeName() != "PSet"):
                    if(str(paramval) != str(newparamval)):
                        if(str(paramval).find("\n") != -1):
                            formatparamval = str(paramval).replace("\n"," ")
                            formatparamval = str(formatparamval).replace("     "," ")
                        else:
                            formatparamval = str(paramval)
                        if(str(newparamval).find("\n") != -1):
                            formatnewparamval = str(newparamval).replace("\n"," ")
                            formatnewparamval = str(formatnewparamval).replace("     "," ")
                        else:
                            formatnewparamval = str(newparamval)
                                                                                               
                        if(recorddiffs == 1):
                            print "| (!" + componenttype + ") | " + str(name) + "." + str(paramname) + " | " + str(formatparamval)  + " | " + str(formatnewparamval) + " | "
                            self.nchanges = self.nchanges + 1
                                                                                                                         
            except AttributeError:
                if(paramval.configTypeName() == "VPSet"):
                    #                    if(str(vpsetval) != str(newvpsetval)):
                    if(str(vpsetval).find("\n") != -1):
                        formatvpsetval = str(vpsetval).replace("\n"," ")
                        formatvpsetval = str(formatvpsetval).replace("     "," ")
                    else:
                        formatvpsetval = str(vpsetval)
                        #                        if(str(newvpsetval).find("\n") != -1):
                        #                                formatnewvpsetval = str(newvpsetval).replace("\n"," ")
                        #                                formatnewvpsetval = str(formatnewvpsetval).replace("     "," ")
                        #                            else:
                        #                                formatnewvpsetval = str(newvpsetval)
                                
                    if(recorddiffs == 1):
                        print "| (!" + componenttype + ") | " + str(name) + "." + str(paramname) + " | " + str(formatvpsetval)  + " | " + str(formatnewvpsetval) + " | "
                    else:
                        print "| (!" + componenttype + ") | " + str(name) + "." + str(paramname) + " | " + str(formatnewvpsetval)  + " | " + str(formatvpsetval) + " | "
                        self.nchanges = self.nchanges + 1

                elif(paramval.configTypeName() != "PSet"):
                    if(str(paramval).find("\n") != -1):
                        formatparamval = str(paramval).replace("\n"," ")
                        formatparamval = str(formatparamval).replace("     "," ")
                    else:
                        formatparamval = str(paramval)
                        #                    if(str(newparamval).find("\n") != -1):
                        #                        formatnewparamval = str(newparamval).replace("\n"," ")
                        #                        formatnewparamval = str(formatnewparamval).replace("     "," ")
                        #                    else:
                        #                        formatnewparamval = str(newparamval)
                    if(recorddiffs == 1):
                        print "| (!" + componenttype + ") | " + str(name) + "." + str(paramname) + " | " + str(formatparamval)  + " | " + str(formatnewparamval) + " | "
                    else:
                        print "| (!" + componenttype + ") | " + str(name) + "." + str(paramname) + " | " + str(formatnewparamval)  + " | " + str(formatparamval) + " | "
                    self.nchanges = self.nchanges + 1
                                            
if __name__ == "__main__":
	main(sys.argv[1:])
	
                            
