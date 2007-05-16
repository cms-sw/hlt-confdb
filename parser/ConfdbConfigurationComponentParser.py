#!/usr/bin/env python

# ConfdbConfigurationComponentParser.py
# Utility for determining which modules/packages
# are required by a config.
#
# Jonathan Hollar LLNL May 16, 2007


import os, string, sys, posix, tokenize, array, re

#def main(argv):
#    confdbjob = ConfigurationComponentParser(1)
#    confdbjob.FindConfigurationComponents('../uscmst1/prod/sw/cms/slc4_ia32_gcc345/cms/cmssw/CMSSW_1_5_0_pre2/src/HLTrigger/Configuration/test/HLTtable.cfg','../uscmst1/prod/sw/cms/slc4_ia32_gcc345/cms/cmssw/CMSSW_1_5_0_pre2/src/',0)

class ConfigurationComponentParser:
    def __init__(self,verbosity):
	self.verbose = verbosity
	self.foundcomps = []

    # Parser for .cfg files. Look for framework components used in this configuration.
    def FindConfigurationComponents(self,cfgfile,sourcetree,indenting):
	shortcfg = (cfgfile.split('src/')[1])

	if(not os.path.isfile(cfgfile)):
	    print 'Error: could not find specified configuration file ' + shortcfg
	    return

	filename = open(cfgfile)

	# Pretty printing
	ind = 0
	indstr = ''
	while ind < indenting:
	    indstr = indstr + '  '
	    ind = ind + 1

	if(ind == 0):
	    print 'Generating list of framework components needed by the configuration ' + shortcfg      
	    print '------------------------------------------------------------------'
#	else:
#	    print '\n'
#	    print indstr + '------------------------------------------------------------------'
#	    print indstr + (cfgfile.split('src/')[1])
#	    print indstr + '------------------------------------------------------------------'

	lines = filename.readlines()
	startedccomment = False

	for line in lines:
	    # Tokenize the line
	    vals = string.split(line)

	    # Check that line isn't empty
	    if(vals):
		if(line.lstrip().startswith('/*')):
		   startedccomment = True
		if(line.endswith('*/')):
		    startedccomment = False

		if(line.lstrip().startswith('//') or line.lstrip().startswith('#') or startedccomment == True):
		    continue

		if(line.find('//') != -1):
		    line = line.split('//')[0]
		
		if(line.find('module ') != -1 and line.find('=') != -1):
		    if(line.find(' from ') != -1):
			self.ParseFrom(line,sourcetree)
			continue

		    moduleline = line.split('=')[1]
		    if(moduleline.find('{') != -1):
			themodule = moduleline.split('{')[0].lstrip().rstrip()
			if(not themodule in self.foundcomps):
			    if(self.verbose > 0):
				print indstr + ' module ' + themodule + ' (' + shortcfg + ')'     
			    self.foundcomps.append(themodule)
		    else:
			themodule = moduleline.lstrip().rstrip()
			if(not themodule in self.foundcomps):
			    if(self.verbose > 0):
				print indstr + ' module ' + themodule + ' (' + shortcfg + ')'
			    self.foundcomps.append(themodule)

		elif(line.find('source ') != -1 and line.find('=') != -1):    
		    sourceline = line.split('=')[1]
		    if(sourceline.find('{') != -1):
			thesource = sourceline.split('{')[0].lstrip().rstrip()			    
			if(not thesource in self.foundcomps):
			    if(self.verbose > 0):
				print indstr + ' source ' + thesource + ' (' + shortcfg + ')'
			    self.foundcomps.append(thesource)
		    else:
			thesource = sourceline.lstrip().rstrip()
			if(not thesource in self.foundcomps):
			    if(self.verbose > 0):
				print indstr + ' source ' + thesource + ' (' + shortcfg + ')'
			    self.foundcomps.append(thesource)

		elif(line.find('service ') != -1 and line.find('=') != -1):
		    serviceline = line.split('=')[1]
		    if(serviceline.find('{') != -1):
			theservice = serviceline.split('{')[0].lstrip().rstrip()
			if(not theservice in self.foundcomps):
			    if(self.verbose > 0):
				print indstr + ' service ' + theservice + ' (' + shortcfg + ')'
			    self.foundcomps.append(theservice)
		    else:
			theservice = serviceline.lstrip().rstrip()
			if(not theservice in self.foundcomps):
			    if(self.verbose > 0):
				print indstr + ' service ' + theservice + ' (' + shortcfg + ')'
			    self.foundcomps.append(theservice)

		elif(line.lstrip().startswith('include ')):
		    newfile = (line.split('include ')[1]).rstrip().lstrip().strip('"').strip("'")
		    # Recursively search included config files
		    self.FindConfigurationComponents(sourcetree+newfile,sourcetree,indenting+1)

	return self.foundcomps

    def ParseFrom(self,thefromline,fromsourcetree):
	themodinstance = thefromline.split('module ')[1].split(' from ')[0]
	themodfile = thefromline.split(' from ')[1].rstrip().lstrip().strip('"').strip("'")

	if(not os.path.isfile(fromsourcetree+themodfile)):
	    print 'Error: using _from_ a configuration file that could not be found: ' + themodfile
	    return

	fromfilename = open(fromsourcetree+themodfile)	

	fromlines = fromfilename.readlines()
	startedccomment = False

	for fromline in fromlines:
	    # Tokenize the line
	    vals = string.split(fromline)

	    # Check that line isn't empty
	    if(vals):
		if(fromline.lstrip().startswith('/*')):
		   startedccomment = True
		if(fromline.endswith('*/')):
		    startedccomment = False

		if(fromline.lstrip().startswith('//') or fromline.lstrip().startswith('#') or startedccomment == True):
		    continue

		if(fromline.find('module') != -1 and fromline.find(themodinstance) != -1 and fromline.find('=') != -1):
		    moduleline = line.split('=')[1]
		    if(moduleline.find('{') != -1):
			themodule = moduleline.split('{')[0].lstrip().rstrip()
			if(not themodule in self.foundcomps):
			    if(self.verbose > 0):
				print indstr + ' module ' + themodule + ' (' + shortcfg + ')'     
			    self.foundcomps.append(themodule)
		    else:
			themodule = moduleline.lstrip().rstrip()
			if(not themodule in self.foundcomps):
			    if(self.verbose > 0):
				print indstr + ' module ' + themodule + ' (' + shortcfg + ')'
			    self.foundcomps.append(themodule)	

#if __name__ == "__main__":
#   main(sys.argv[1:])
