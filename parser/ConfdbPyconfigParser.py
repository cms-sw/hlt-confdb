#!/usr/bin/env python

 # ConfdbPyconfigParser.py
 # Query python-configs in a release for the default
 # values of parameters
 # Jonathan Hollar LLNL May. 14, 2008

import os, string, sys, posix, tokenize, array, getopt
import FWCore.ParameterSet.Config as cms

def main(argv):
    pyparser = ConfdbPyconfigParser()
    pyparser.SetThePythonVar("L3Muons","L3TrajBuilderParameters","SeedGeneratorParameters","ComponentName")
    pyparser.FindPythonConfigDefault("RecoMuon","L3MuonProducer","L3Muons") 

class ConfdbPyconfigParser:
    def __init__(self):
        self.value = ''
        self.themodule = ''
        self.thepset = ''
        self.thenestedpset = ''
        self.theparameter = ''
        self.theparamdefault = ''
        self.founddefault = False
        self.foundcomponent = False
        self.verbose = 0

    def SetThePythonVar(self,modname,psetname,nestedpsetname,paramname):
        self.themodule = modname
        self.thepset = psetname
        self.thenestedpset = nestedpsetname
        self.theparameter = paramname

    def FindPythonConfigDefault(self,thecomponent,thedirectory):

        self.theparamdefault = ''

        self.founddefault = False
        self.foundcomponent = False

        # Look at what cfi_py configs are available
        thedirectory = thedirectory.split('data/')[0] + 'python/'
        if(os.path.isdir(thedirectory)):
            cfipyfiles = os.listdir(thedirectory)
            for cfipyfile in cfipyfiles:
                if(cfipyfile.endswith('_cfi.py')):
                    thefilecomponent = cfipyfile.split('.py')[0]
                    
                    thebasefilecomponent = thefilecomponent.split('_cfi')[0]        
                    
                    # Construct the py-cfi to import
                    thesubsystempackage = thedirectory.split('src/')[1].split('/data/')[0].lstrip().rstrip()
                    thesubsystem = thesubsystempackage.split('/')[0]
                    thepackage = thesubsystempackage.split('/')[1]
                    importcommand = "import " + thesubsystem + "." + thepackage + "." + thefilecomponent
                    
                    if(self.verbose > 2):
                        print "PSet = " + self.thenestedpset + ", " + self.thepset + ", " + self.theparameter
                        
                    if(self.verbose > 2):
                        print 'Starting python session'
        
                    try:
                        if(self.verbose > 2):
                            print "\t\t" + importcommand

                        exec importcommand

                        # Now create a process and construct the command to extend it with the py-cfi
                        process = cms.Process("MyProcess")
                        theextend = "process.extend(" + thesubsystem + "." + thepackage + "." + thefilecomponent + ")"
                        if(self.verbose > 2):
                            print "\t\t" + theextend
                        eval(theextend)

                        myproducers = process.producers_() 
                        myfilters = process.filters_()
                        myservices = process.services_()
                        myoutputmodules = process.outputModules_()
                        myessources = process.es_sources_()
                        myesproducers = process.es_producers_()
                        myanalyzers = process.analyzers_()

                        # More complete than before - look at all components by baseclass instead of instance name.
                        for name, value in myproducers.iteritems():
                            if(value.type_() == thecomponent):
                                thecomponent = str(name)
                        for name, value in myfilters.iteritems():
                            if(value.type_() == thecomponent):
                                thecomponent = str(name)
                        for name, value in myservices.iteritems():
                            if(value.type_() == thecomponent):
                                thecomponent = str(name)                                
                        for name, value in myoutputmodules.iteritems():
                            if(value.type_() == thecomponent):
                                thecomponent = str(name)                                
                        for name, value in myessources.iteritems():
                            if(value.type_() == thecomponent):
                                thecomponent = str(name)                                
                        for name, value in myesproducers.iteritems():
                            if(value.type_() == thecomponent):
                                thecomponent = str(name)
                        for name, value in myanalyzers.iteritems():
                            if(value.type_() == thecomponent):
                                thecomponent = str(name)

                        # Now construct the command to query the variable value
                        valvar = "process."
                        valvar = valvar + thecomponent + "."
                        valcomp = "process." + thecomponent
                        if(self.thenestedpset != None and self.thenestedpset != ''):
                            valvar = valvar + self.thenestedpset + "."
                        if(self.thepset != None and self.thepset != ''):
                            valvar = valvar + self.thepset + "."
                        valvar = valvar + self.theparameter + "."
                        valvar = valvar + "value()"

                        if(self.verbose > 2):
                            print "\t\t" + valcomp
                        eval(valcomp)

                        self.foundcomponent = True

                        if(self.verbose > 2):
                            print "\t\t" + valvar

                        # OK, now get the default!
                        thedefault =  eval(valvar)

                        if(self.verbose > 2):
                            print 'The default value of variable ' + self.theparameter + ' is:\n'        
                            print thedefault

                        self.theparamdefault = thedefault

                        self.founddefault = True

                        return
                       
                    except:
                        if(self.verbose > 2):
                            print 'Could not get python-config information - no variable default found'
                            

    def RetrievePythonConfigFoundComponent(self):
        return self.foundcomponent

    def RetrievePythonConfigSuccess(self):
        return self.founddefault
                           
    def RetrievePythonConfigDefault(self):
        return self.theparamdefault
    
if __name__ == "__main__":
    main(sys.argv[1:])
    
