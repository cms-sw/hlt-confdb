#!/usr/bin/env python

 # ConfdbPyconfigParser.py
 # Parse cc files in a release, and identify the modules/parameters
 # that should be loaded as templates in the Conf DB
 # Jonathan Hollar LLNL Nov. 7, 2007

import os, string, sys, posix, tokenize, array, getopt
import FWCore.ParameterSet.Config as cms

def main(argv):
    pyparser = ConfdbPyconfigParser()
    pyparser.FindPythonConfigDefault() 

class ConfdbPyconfigParser:
    def __init__(self):
        self.value = ''

    def FindPythonConfigDefault(self):

        #        thecfifile = "RecoMuon.L3MuonProducer.L3Muons_cfi"
        #        __import__(str(thecfifile))
        #        import RecoMuon.L3MuonProducer.L3Muons_cfi
        exec "import " + "RecoMuon.L3MuonProducer.L3Muons_cfi"
        
        process = cms.Process("MyProcess")

        theextend = "process.extend(" + "RecoMuon.L3MuonProducer.L3Muons_cfi" + ")"
        eval(theextend)

        print '\nJJH: the default value of variable is:\n'

        jjhvar = "process."
        jjhvar = jjhvar + "L3Muons" + "."
        jjhvar = jjhvar + "L3TrajBuilderParameters" + "."
        jjhvar = jjhvar + "SeedGeneratorParameters" + "."
        jjhvar = jjhvar + "ComponentName" + "."
        jjhvar = jjhvar + "value()"
        
        thedefault =  eval(jjhvar)
        print thedefault

if __name__ == "__main__":
    main(sys.argv[1:])
    
