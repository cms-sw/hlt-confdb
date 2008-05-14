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

    def SetThePythonVar(self,modname,psetname,nestedpsetname,paramname):
        self.themodule = modname
        self.thepset = psetname
        self.thenestedpset = nestedpsetname
        self.theparameter = paramname

    def FindPythonConfigDefault(self,thesubsystem,thepackage,thecomponent):

        exec "import " + thesubsystem + "." + thepackage + "." + thecomponent + "_cfi"
        
        process = cms.Process("MyProcess")

        theextend = "process.extend(" + thesubsystem + "." + thepackage + "." + thecomponent + "_cfi)"
        eval(theextend)

        print '\nJJH: the default value of variable ' + self.theparameter + ' is:\n'

        valvar = "process."
        valvar = valvar + self.themodule + "."
        valvar = valvar + self.thepset + "."
        valvar = valvar + self.thenestedpset + "."
        valvar = valvar + self.theparameter + "."
        valvar = valvar + "value()"

        thedefault =  eval(valvar)
        print thedefault

if __name__ == "__main__":
    main(sys.argv[1:])
    
