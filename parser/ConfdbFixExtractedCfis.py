#!/usr/bin/env python

import os, string, sys, posix, tokenize, array, getopt

def main(argv):
    
    curdir = '/tmp/jjhollar'
    files = os.listdir(curdir)
    
    for thefile in files:
        if(thefile.endswith("_cfi.py")):
            oldfilename = curdir + "/" + thefile
            newfilename = curdir + "/" + thefile + "_temp"
            oldfile = open(oldfilename)
            newfile = open(newfilename, 'w')
            
            lines = oldfile.readlines()
            linenum = 1

            for line in lines:
                if((linenum < 3) or (linenum > 3 and linenum < 9)):
                    print line
                else:
                    newfile.write(line)
                   
                linenum = linenum + 1

        newfile.close()
        os.system('mv ' + str(newfilename) + ' ' + str(oldfilename))
        print 'mv ' + str(newfilename) + ' ' + str(oldfilename)
        
if __name__ == "__main__":
    main(sys.argv[1:])
                
