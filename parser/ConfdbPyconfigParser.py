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

    def SetThePythonVar(self,modname,psetname,paramname):
        self.themodule = modname
        self.thepset = psetname
        self.theparameter = paramname

    def FindPythonConfigDefault(self,thecomponent,thedirectory):

        # Look at what cfi_py configs are available
        thedirectory = thedirectory.split('data/')[0] + 'python/'
        if(os.path.isdir(thedirectory)):
           cfipyfiles = os.listdir(thedirectory)
           for cfipyfile in cfipyfiles:
               if(cfipyfile.endswith('_cfi.py')):
                   thecomponent = cfipyfile.split('.py')[0]

        thebasecomponent = thecomponent.split('_cfi')[0]        

        # Construct the py-cfi to import
        thesubsystempackage = thedirectory.split('src/')[1].split('/data/')[0].lstrip().rstrip()
        thesubsystem = thesubsystempackage.split('/')[0]
        thepackage = thesubsystempackage.split('/')[1]
        importcommand = "import " + thesubsystem + "." + thepackage + "." + thecomponent

        print "PSet = " + self.thepset + ", " + self.theparameter
        
        print "\tStarting the python session"

        try:
            print "\t\t" + importcommand
            exec importcommand

            # Now create a process and construct the command to extend it with the py-cfi
            process = cms.Process("MyProcess")
            theextend = "process.extend(" + thesubsystem + "." + thepackage + "." + thecomponent + ")"
            print "\t\t" + theextend
            eval(theextend)

            # Now construct the command to query the variable value
            valvar = "process."
            valvar = valvar + thebasecomponent + "."
            if(self.thepset != None and self.thepset != ''):
                valvar = valvar + self.thepset + "."
            valvar = valvar + self.theparameter + "."
            valvar = valvar + "value()"

            print "\t\t" + valvar
            
            # OK, now get the default!
            thedefault =  eval(valvar)

            print '\t\tThe default value of variable ' + self.theparameter + ' is:\n'        
            print thedefault

        except:
            print 'Could not get python-config information - no variable default found'
            
if __name__ == "__main__":
    main(sys.argv[1:])
    
