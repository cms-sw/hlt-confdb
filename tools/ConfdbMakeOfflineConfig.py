#!/usr/bin/env python

# Jonathan Hollar LLNL Sept. 25, 2008

import os, string, sys, posix, tokenize, array, getopt
import FWCore.ParameterSet.Config as cms

def main(argv):

    thecfffile = "Configuration.StandardSequences.Reconstruction_cff"
#    thecfffile = "Configuration.StandardSequences.Digi_cff"
#    thecfffile = "Configuration.StandardSequences.Simulation_cff"
#    thecfffile = "Configuration.StandardSequences.Generator_cff"
#    thecfffile = "HLTrigger.Configuration.HLT_2E30_cff") 
        

    # Now create a process and construct the command to extend it with the py-cfi
    process = cms.Process("MyProcess")
    process.load(thecfffile)
            
    print process.dumpPython()
    
if __name__ == "__main__":
	main(sys.argv[1:])
	
                            
