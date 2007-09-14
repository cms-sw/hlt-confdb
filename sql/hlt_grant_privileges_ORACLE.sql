--
-- grant privileges to cms_hlt tablespace to users cms_hlt_writer/cms_hlt_reader
--

--
-- cms_hlt_writer
--

-- SELECT
GRANT SELECT ON SuperIdReleaseAssoc 		TO cms_hlt_writer;
GRANT SELECT ON SoftwareReleases		TO cms_hlt_writer;
GRANT SELECT ON ConfigurationPathAssoc 		TO cms_hlt_writer;
GRANT SELECT ON StreamPathAssoc 		TO cms_hlt_writer;
GRANT SELECT ON PathInPathAssoc 		TO cms_hlt_writer;
GRANT SELECT ON PathModuleAssoc 		TO cms_hlt_writer;
GRANT SELECT ON ConfigurationSequenceAssoc 	TO cms_hlt_writer;
GRANT SELECT ON PathSequenceAssoc 		TO cms_hlt_writer;
GRANT SELECT ON SequenceInSequenceAssoc 	TO cms_hlt_writer;
GRANT SELECT ON SequenceModuleAssoc 		TO cms_hlt_writer;
GRANT SELECT ON ConfigurationServiceAssoc 	TO cms_hlt_writer;
GRANT SELECT ON ConfigurationEDSourceAssoc 	TO cms_hlt_writer;
GRANT SELECT ON ConfigurationESSourceAssoc 	TO cms_hlt_writer;
GRANT SELECT ON ConfigurationESModuleAssoc 	TO cms_hlt_writer;
GRANT SELECT ON ConfigurationParamSetAssoc 	TO cms_hlt_writer;
GRANT SELECT ON Paths 				TO cms_hlt_writer;
GRANT SELECT ON Sequences 			TO cms_hlt_writer;
GRANT SELECT ON Services 			TO cms_hlt_writer;
GRANT SELECT ON ServiceTemplates 		TO cms_hlt_writer;
GRANT SELECT ON EDSources 			TO cms_hlt_writer;
GRANT SELECT ON EDSourceTemplates 		TO cms_hlt_writer;
GRANT SELECT ON ESSources 			TO cms_hlt_writer;
GRANT SELECT ON ESSourceTemplates 		TO cms_hlt_writer;
GRANT SELECT ON ESModules 			TO cms_hlt_writer;
GRANT SELECT ON ESModuleTemplates 		TO cms_hlt_writer;
GRANT SELECT ON Modules 			TO cms_hlt_writer;
GRANT SELECT ON ModuleTemplates 		TO cms_hlt_writer;
GRANT SELECT ON ModuleTypes 			TO cms_hlt_writer;
GRANT SELECT ON Configurations 			TO cms_hlt_writer;
GRANT SELECT ON LockedConfigurations  		TO cms_hlt_writer;
GRANT SELECT ON Streams		  		TO cms_hlt_writer;
GRANT SELECT ON Directories 			TO cms_hlt_writer;
GRANT SELECT ON Int32ParamValues 		TO cms_hlt_writer;
GRANT SELECT ON VInt32ParamValues 		TO cms_hlt_writer;
GRANT SELECT ON UInt32ParamValues 		TO cms_hlt_writer;
GRANT SELECT ON VUInt32ParamValues 		TO cms_hlt_writer;
GRANT SELECT ON BoolParamValues 		TO cms_hlt_writer;
GRANT SELECT ON DoubleParamValues 		TO cms_hlt_writer;
GRANT SELECT ON VDoubleParamValues 		TO cms_hlt_writer;
GRANT SELECT ON StringParamValues 		TO cms_hlt_writer;
GRANT SELECT ON VStringParamValues 		TO cms_hlt_writer;
GRANT SELECT ON InputTagParamValues 		TO cms_hlt_writer;
GRANT SELECT ON VInputTagParamValues 		TO cms_hlt_writer;
GRANT SELECT ON EventIDParamValues 		TO cms_hlt_writer;
GRANT SELECT ON VEventIDParamValues 		TO cms_hlt_writer;
GRANT SELECT ON FileInPathParamValues 		TO cms_hlt_writer;
GRANT SELECT ON SuperIdParameterAssoc 		TO cms_hlt_writer;
GRANT SELECT ON SuperIdParamSetAssoc 		TO cms_hlt_writer;
GRANT SELECT ON SuperIdVecParamSetAssoc 	TO cms_hlt_writer;
GRANT SELECT ON ParameterSets 			TO cms_hlt_writer;
GRANT SELECT ON VecParameterSets 		TO cms_hlt_writer;
GRANT SELECT ON Parameters 			TO cms_hlt_writer;
GRANT SELECT ON SuperIds 			TO cms_hlt_writer;
GRANT SELECT ON ParameterTypes 			TO cms_hlt_writer;

GRANT SELECT ON ReleaseId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON DirId_Sequence 			TO cms_hlt_writer;
GRANT SELECT ON ConfigId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON StreamId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON SuperId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON PathId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON SequenceId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON ParamId_Sequence 		TO cms_hlt_writer;


-- INSERT
GRANT INSERT ON SuperIdReleaseAssoc 		TO cms_hlt_writer;
GRANT INSERT ON ConfigurationPathAssoc 		TO cms_hlt_writer;
GRANT INSERT ON StreamPathAssoc 		TO cms_hlt_writer;
GRANT INSERT ON PathInPathAssoc 		TO cms_hlt_writer;
GRANT INSERT ON PathModuleAssoc 		TO cms_hlt_writer;
GRANT INSERT ON ConfigurationSequenceAssoc 	TO cms_hlt_writer;
GRANT INSERT ON PathSequenceAssoc 		TO cms_hlt_writer;
GRANT INSERT ON SequenceInSequenceAssoc 	TO cms_hlt_writer;
GRANT INSERT ON SequenceModuleAssoc 		TO cms_hlt_writer;
GRANT INSERT ON ConfigurationServiceAssoc 	TO cms_hlt_writer;
GRANT INSERT ON ConfigurationEDSourceAssoc 	TO cms_hlt_writer;
GRANT INSERT ON ConfigurationESSourceAssoc 	TO cms_hlt_writer;
GRANT INSERT ON ConfigurationESModuleAssoc 	TO cms_hlt_writer;
GRANT INSERT ON ConfigurationParamSetAssoc 	TO cms_hlt_writer;
GRANT INSERT ON Paths 				TO cms_hlt_writer;
GRANT INSERT ON Sequences 			TO cms_hlt_writer;
GRANT INSERT ON Services 			TO cms_hlt_writer;
GRANT INSERT ON EDSources 			TO cms_hlt_writer;
GRANT INSERT ON ESSources 			TO cms_hlt_writer;
GRANT INSERT ON ESModules 			TO cms_hlt_writer;
GRANT INSERT ON Modules 			TO cms_hlt_writer;
GRANT INSERT ON Configurations 			TO cms_hlt_writer;
GRANT INSERT ON LockedConfigurations		TO cms_hlt_writer;
GRANT INSERT ON Streams				TO cms_hlt_writer;
GRANT INSERT ON Directories 			TO cms_hlt_writer;
GRANT INSERT ON Int32ParamValues 		TO cms_hlt_writer;
GRANT INSERT ON VInt32ParamValues 		TO cms_hlt_writer;
GRANT INSERT ON UInt32ParamValues 		TO cms_hlt_writer;
GRANT INSERT ON VUInt32ParamValues 		TO cms_hlt_writer;
GRANT INSERT ON BoolParamValues 		TO cms_hlt_writer;
GRANT INSERT ON DoubleParamValues 		TO cms_hlt_writer;
GRANT INSERT ON VDoubleParamValues 		TO cms_hlt_writer;
GRANT INSERT ON StringParamValues 		TO cms_hlt_writer;
GRANT INSERT ON VStringParamValues 		TO cms_hlt_writer;
GRANT INSERT ON InputTagParamValues 		TO cms_hlt_writer;
GRANT INSERT ON VInputTagParamValues 		TO cms_hlt_writer;
GRANT INSERT ON EventIDParamValues 		TO cms_hlt_writer;
GRANT INSERT ON VEventIDParamValues 		TO cms_hlt_writer;
GRANT INSERT ON FileInPathParamValues 		TO cms_hlt_writer;
GRANT INSERT ON SuperIdParameterAssoc 		TO cms_hlt_writer;
GRANT INSERT ON SuperIdParamSetAssoc 		TO cms_hlt_writer;
GRANT INSERT ON SuperIdVecParamSetAssoc 	TO cms_hlt_writer;
GRANT INSERT ON ParameterSets 			TO cms_hlt_writer;
GRANT INSERT ON VecParameterSets 		TO cms_hlt_writer;
GRANT INSERT ON Parameters 			TO cms_hlt_writer;
GRANT INSERT ON SuperIds 			TO cms_hlt_writer;


--  DELETE
GRANT DELETE ON Directories		 	TO cms_hlt_writer;
GRANT DELETE ON LockedConfigurations	 	TO cms_hlt_writer;


--
-- cms_hlt_reader
--

--  SELECT
GRANT SELECT ON SuperIdReleaseAssoc 		TO cms_hlt_reader;
GRANT SELECT ON SoftwareReleases		TO cms_hlt_reader;
GRANT SELECT ON ConfigurationPathAssoc 		TO cms_hlt_reader;
GRANT SELECT ON StreamPathAssoc 		TO cms_hlt_reader;
GRANT SELECT ON PathInPathAssoc 		TO cms_hlt_reader;
GRANT SELECT ON PathModuleAssoc 		TO cms_hlt_reader;
GRANT SELECT ON ConfigurationSequenceAssoc 	TO cms_hlt_reader;
GRANT SELECT ON PathSequenceAssoc 		TO cms_hlt_reader;
GRANT SELECT ON SequenceInSequenceAssoc 	TO cms_hlt_reader;
GRANT SELECT ON SequenceModuleAssoc 		TO cms_hlt_reader;
GRANT SELECT ON ConfigurationServiceAssoc 	TO cms_hlt_reader;
GRANT SELECT ON ConfigurationEDSourceAssoc 	TO cms_hlt_reader;
GRANT SELECT ON ConfigurationESSourceAssoc 	TO cms_hlt_reader;
GRANT SELECT ON ConfigurationESModuleAssoc 	TO cms_hlt_reader;
GRANT SELECT ON ConfigurationParamSetAssoc 	TO cms_hlt_reader;
GRANT SELECT ON Paths 				TO cms_hlt_reader;
GRANT SELECT ON Sequences 			TO cms_hlt_reader;
GRANT SELECT ON Services 			TO cms_hlt_reader;
GRANT SELECT ON ServiceTemplates 		TO cms_hlt_reader;
GRANT SELECT ON EDSources 			TO cms_hlt_reader;
GRANT SELECT ON EDSourceTemplates 		TO cms_hlt_reader;
GRANT SELECT ON ESSources 			TO cms_hlt_reader;
GRANT SELECT ON ESSourceTemplates 		TO cms_hlt_reader;
GRANT SELECT ON ESModules 			TO cms_hlt_reader;
GRANT SELECT ON ESModuleTemplates 		TO cms_hlt_reader;
GRANT SELECT ON Modules 			TO cms_hlt_reader;
GRANT SELECT ON ModuleTemplates 		TO cms_hlt_reader;
GRANT SELECT ON ModuleTypes 			TO cms_hlt_reader;
GRANT SELECT ON Configurations 			TO cms_hlt_reader;
GRANT SELECT ON LockedConfigurations		TO cms_hlt_reader;
GRANT SELECT ON Streams 			TO cms_hlt_reader;
GRANT SELECT ON Directories 			TO cms_hlt_reader;
GRANT SELECT ON Int32ParamValues 		TO cms_hlt_reader;
GRANT SELECT ON VInt32ParamValues 		TO cms_hlt_reader;
GRANT SELECT ON UInt32ParamValues 		TO cms_hlt_reader;
GRANT SELECT ON VUInt32ParamValues 		TO cms_hlt_reader;
GRANT SELECT ON BoolParamValues 		TO cms_hlt_reader;
GRANT SELECT ON DoubleParamValues 		TO cms_hlt_reader;
GRANT SELECT ON VDoubleParamValues 		TO cms_hlt_reader;
GRANT SELECT ON StringParamValues 		TO cms_hlt_reader;
GRANT SELECT ON VStringParamValues 		TO cms_hlt_reader;
GRANT SELECT ON InputTagParamValues 		TO cms_hlt_reader;
GRANT SELECT ON VInputTagParamValues 		TO cms_hlt_reader;
GRANT SELECT ON EventIDParamValues 		TO cms_hlt_reader;
GRANT SELECT ON VEventIDParamValues 		TO cms_hlt_reader;
GRANT SELECT ON FileInPathParamValues 		TO cms_hlt_reader;
GRANT SELECT ON SuperIdParameterAssoc 		TO cms_hlt_reader;
GRANT SELECT ON SuperIdParamSetAssoc 		TO cms_hlt_reader;
GRANT SELECT ON SuperIdVecParamSetAssoc 	TO cms_hlt_reader;
GRANT SELECT ON ParameterSets 			TO cms_hlt_reader;
GRANT SELECT ON VecParameterSets 		TO cms_hlt_reader;
GRANT SELECT ON Parameters 			TO cms_hlt_reader;
GRANT SELECT ON SuperIds 			TO cms_hlt_reader;
GRANT SELECT ON ParameterTypes 			TO cms_hlt_reader;

GRANT SELECT ON ReleaseId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON DirId_Sequence 			TO cms_hlt_reader;
GRANT SELECT ON ConfigId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON StreamId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON SuperId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON PathId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON SequenceId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON ParamId_Sequence 		TO cms_hlt_reader;

-- INSERT

-- DELETE

