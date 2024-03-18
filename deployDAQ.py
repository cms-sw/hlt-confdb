#!/usr/bin/env python3
import argparse
import shutil
import os
"""
this script takes the currently deployed v3 version and copies the converter
to the daq location which the DAQ experts can pick it up

once its installed in the daq, --deploy-hilton, updates the sym link and version file so it becomes availbile on the hilton by default

"""
def convert_version(in_version):   
    return ".".join([x.lstrip("0") if x.lstrip("0")!='' else "0" for x in in_version[1:].split("-")])

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='copies the main branch converter to the DAQ location')
    parser.add_argument('--update-hilton',dest="update_hilton",action='store_true',help="also updates hilton")
    parser.add_argument('--base-dir',dest="base_dir",default="/afs/cern.ch/user/c/confdb/www",help="confdb base directory")
    args = parser.parse_args()

    base_dir = args.base_dir
    main_version = "v3"
    daq_version = "daq"
    main_dir = f"{base_dir}/{main_version}"
    daq_dir = f"{base_dir}/{daq_version}"

    version = None

    converter_file = "cmssw-evf-confdb-converter"
    with open(f"{main_dir}/confdb.version") as f:
        for line in f.readlines():
            if line.startswith("confdb.version="):
                version = convert_version(line.split("=")[1].rstrip())
                break

    if version:
        out_file = f"{daq_dir}/lib/{converter_file}-{version}.jar"
        in_file = f"{main_dir}/lib/{converter_file}.jar"
        shutil.copyfile(in_file,out_file)
        print(f"deploying\n  {in_file}\nto\n  {out_file}")
        
        if args.update_hilton:
            print("updating hilton")
            if os.path.exists(f"{daq_dir}/lib/{converter_file}.jar"):
                os.remove(f"{daq_dir}/lib/{converter_file}.jar")
            cwd = os.getcwd()
            os.chdir(f"{daq_dir}/lib")
            os.symlink(f"{converter_file}-{version}.jar",
                       f"{converter_file}.jar")
            os.chdir(cwd)
            shutil.copyfile(f"{main_dir}/confdb.version",f"{daq_dir}/confdb.version")

    else:
        print("version file not found, converter was not properly deployed, aborting")
