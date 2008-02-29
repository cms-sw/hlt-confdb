--
-- grant privileges to cms_hlt tablespace to users cms_hlt_writer/cms_hlt_reader
--


--
-- cms_hlt_admin
--

-- SELECT
GRANT SELECT ON SuperIdReleaseAssoc 		TO cms_hlt_admin;
GRANT SELECT ON SoftwareReleases		TO cms_hlt_admin;
GRANT SELECT ON SoftwareSubsystems		TO cms_hlt_admin;
GRANT SELECT ON SoftwarePackages		TO cms_hlt_admin;
GRANT SELECT ON ServiceTemplates 		TO cms_hlt_admin;
GRANT SELECT ON EDSourceTemplates 		TO cms_hlt_admin;
GRANT SELECT ON ESSourceTemplates 		TO cms_hlt_admin;
GRANT SELECT ON ESModuleTemplates 		TO cms_hlt_admin;
GRANT SELECT ON ModuleTemplates 		TO cms_hlt_admin;
GRANT SELECT ON ModuleTypes 			TO cms_hlt_admin;
GRANT SELECT ON Configurations 			TO cms_hlt_admin;
GRANT SELECT ON Directories 			TO cms_hlt_admin;
GRANT SELECT ON Int32ParamValues 		TO cms_hlt_admin;
GRANT SELECT ON VInt32ParamValues 		TO cms_hlt_admin;
GRANT SELECT ON UInt32ParamValues 		TO cms_hlt_admin;
GRANT SELECT ON VUInt32ParamValues 		TO cms_hlt_admin;
GRANT SELECT ON BoolParamValues 		TO cms_hlt_admin;
GRANT SELECT ON DoubleParamValues 		TO cms_hlt_admin;
GRANT SELECT ON VDoubleParamValues 		TO cms_hlt_admin;
GRANT SELECT ON StringParamValues 		TO cms_hlt_admin;
GRANT SELECT ON VStringParamValues 		TO cms_hlt_admin;
GRANT SELECT ON InputTagParamValues 		TO cms_hlt_admin;
GRANT SELECT ON VInputTagParamValues 		TO cms_hlt_admin;
GRANT SELECT ON EventIDParamValues 		TO cms_hlt_admin;
GRANT SELECT ON VEventIDParamValues 		TO cms_hlt_admin;
GRANT SELECT ON FileInPathParamValues 		TO cms_hlt_admin;
GRANT SELECT ON SuperIdParameterAssoc 		TO cms_hlt_admin;
GRANT SELECT ON SuperIdParamSetAssoc 		TO cms_hlt_admin;
GRANT SELECT ON SuperIdVecParamSetAssoc 	TO cms_hlt_admin;
GRANT SELECT ON ParameterSets 			TO cms_hlt_admin;
GRANT SELECT ON VecParameterSets 		TO cms_hlt_admin;
GRANT SELECT ON Parameters 			TO cms_hlt_admin;
GRANT SELECT ON SuperIds 			TO cms_hlt_admin;
GRANT SELECT ON ParameterTypes 			TO cms_hlt_admin;

GRANT SELECT ON ReleaseId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON SubsysId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON PackageId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON DirId_Sequence 			TO cms_hlt_admin;
GRANT SELECT ON ConfigId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON StreamId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON DatasetId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON SuperId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON PathId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON SequenceId_Sequence 		TO cms_hlt_admin;
GRANT SELECT ON ParamId_Sequence 		TO cms_hlt_admin;


-- INSERT
GRANT INSERT ON SuperIdReleaseAssoc 		TO cms_hlt_admin;
GRANT INSERT ON ServiceTemplates		TO cms_hlt_admin;
GRANT INSERT ON EDSourceTemplates		TO cms_hlt_admin;
GRANT INSERT ON ESSourceTemplates		TO cms_hlt_admin;
GRANT INSERT ON ESModuleTemplates		TO cms_hlt_admin;
GRANT INSERT ON ModuleTemplates			TO cms_hlt_admin;
GRANT INSERT ON Int32ParamValues 		TO cms_hlt_admin;
GRANT INSERT ON VInt32ParamValues 		TO cms_hlt_admin;
GRANT INSERT ON UInt32ParamValues 		TO cms_hlt_admin;
GRANT INSERT ON VUInt32ParamValues 		TO cms_hlt_admin;
GRANT INSERT ON BoolParamValues 		TO cms_hlt_admin;
GRANT INSERT ON DoubleParamValues 		TO cms_hlt_admin;
GRANT INSERT ON VDoubleParamValues 		TO cms_hlt_admin;
GRANT INSERT ON StringParamValues 		TO cms_hlt_admin;
GRANT INSERT ON VStringParamValues 		TO cms_hlt_admin;
GRANT INSERT ON InputTagParamValues 		TO cms_hlt_admin;
GRANT INSERT ON VInputTagParamValues 		TO cms_hlt_admin;
GRANT INSERT ON EventIDParamValues 		TO cms_hlt_admin;
GRANT INSERT ON VEventIDParamValues 		TO cms_hlt_admin;
GRANT INSERT ON FileInPathParamValues 		TO cms_hlt_admin;
GRANT INSERT ON SuperIdParameterAssoc 		TO cms_hlt_admin;
GRANT INSERT ON SuperIdParamSetAssoc 		TO cms_hlt_admin;
GRANT INSERT ON SuperIdVecParamSetAssoc 	TO cms_hlt_admin;
GRANT INSERT ON ParameterSets 			TO cms_hlt_admin;
GRANT INSERT ON VecParameterSets 		TO cms_hlt_admin;
GRANT INSERT ON Parameters 			TO cms_hlt_admin;
GRANT INSERT ON SuperIds 			TO cms_hlt_admin;



--
-- cms_hlt_writer
--

-- SELECT
GRANT SELECT ON SuperIdReleaseAssoc 		TO cms_hlt_writer;
GRANT SELECT ON SoftwareReleases		TO cms_hlt_writer;
GRANT SELECT ON SoftwareSubsystems		TO cms_hlt_writer;
GRANT SELECT ON SoftwarePackages		TO cms_hlt_writer;
GRANT SELECT ON ConfigurationPathAssoc 		TO cms_hlt_writer;
GRANT SELECT ON StreamPathAssoc 		TO cms_hlt_writer;
GRANT SELECT ON PrimaryDatasetPathAssoc 	TO cms_hlt_writer;
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
GRANT SELECT ON PrimaryDatasets	  		TO cms_hlt_writer;
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
GRANT SELECT ON SubsysId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON PackageId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON DirId_Sequence 			TO cms_hlt_writer;
GRANT SELECT ON ConfigId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON StreamId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON DatasetId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON SuperId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON PathId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON SequenceId_Sequence 		TO cms_hlt_writer;
GRANT SELECT ON ParamId_Sequence 		TO cms_hlt_writer;


-- INSERT
GRANT INSERT ON SuperIdReleaseAssoc 		TO cms_hlt_writer;
GRANT INSERT ON ConfigurationPathAssoc 		TO cms_hlt_writer;
GRANT INSERT ON StreamPathAssoc 		TO cms_hlt_writer;
GRANT INSERT ON PrimaryDatasetPathAssoc		TO cms_hlt_writer;
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
GRANT INSERT ON PrimaryDatasets			TO cms_hlt_writer;
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


-- EXECUTE
GRANT EXECUTE ON load_parameter_value           TO cms_hlt_writer;
GRANT EXECUTE ON load_parameters                TO cms_hlt_writer;
GRANT EXECUTE ON load_template                  TO cms_hlt_writer;
GRANT EXECUTE ON load_templates                 TO cms_hlt_writer;
GRANT EXECUTE ON load_templates_for_config      TO cms_hlt_writer;
GRANT EXECUTE ON load_configuration             TO cms_hlt_writer;

GRANT SELECT  ON tmp_template_table             TO cms_hlt_writer;
GRANT SELECT  ON tmp_instance_table             TO cms_hlt_writer;
GRANT SELECT  ON tmp_parameter_table            TO cms_hlt_writer;
GRANT SELECT  ON tmp_boolean_table              TO cms_hlt_writer;
GRANT SELECT  ON tmp_int_table                  TO cms_hlt_writer;
GRANT SELECT  ON tmp_real_table                 TO cms_hlt_writer;
GRANT SELECT  ON tmp_string_table               TO cms_hlt_writer;
GRANT SELECT  ON tmp_path_entries               TO cms_hlt_writer;
GRANT SELECT  ON tmp_sequence_entries           TO cms_hlt_writer;



--
-- cms_hlt_reader
--

--  SELECT
GRANT SELECT ON SuperIdReleaseAssoc 		TO cms_hlt_reader;
GRANT SELECT ON SoftwareReleases		TO cms_hlt_reader;
GRANT SELECT ON SoftwareSubsystems		TO cms_hlt_reader;
GRANT SELECT ON SoftwarePackages		TO cms_hlt_reader;
GRANT SELECT ON ConfigurationPathAssoc 		TO cms_hlt_reader;
GRANT SELECT ON StreamPathAssoc 		TO cms_hlt_reader;
GRANT SELECT ON PrimaryDatasetPathAssoc		TO cms_hlt_reader;
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
GRANT SELECT ON PrimaryDatasets			TO cms_hlt_reader;
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
GRANT SELECT ON SubsysId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON PackageId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON DirId_Sequence 			TO cms_hlt_reader;
GRANT SELECT ON ConfigId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON StreamId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON DatasetId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON SuperId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON PathId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON SequenceId_Sequence 		TO cms_hlt_reader;
GRANT SELECT ON ParamId_Sequence 		TO cms_hlt_reader;

-- INSERT

-- DELETE

-- EXECUTE
GRANT EXECUTE ON load_parameter_value           TO cms_hlt_reader;
GRANT EXECUTE ON load_parameters                TO cms_hlt_reader;
GRANT EXECUTE ON load_template                  TO cms_hlt_reader;
GRANT EXECUTE ON load_templates                 TO cms_hlt_reader;
GRANT EXECUTE ON load_templates_for_config      TO cms_hlt_reader;
GRANT EXECUTE ON load_configuration             TO cms_hlt_reader;

GRANT SELECT  ON tmp_template_table             TO cms_hlt_reader;
GRANT SELECT  ON tmp_instance_table             TO cms_hlt_reader;
GRANT SELECT  ON tmp_parameter_table            TO cms_hlt_reader;
GRANT SELECT  ON tmp_boolean_table              TO cms_hlt_reader;
GRANT SELECT  ON tmp_int_table                  TO cms_hlt_reader;
GRANT SELECT  ON tmp_real_table                 TO cms_hlt_reader;
GRANT SELECT  ON tmp_string_table               TO cms_hlt_reader;
GRANT SELECT  ON tmp_path_entries               TO cms_hlt_reader;
GRANT SELECT  ON tmp_sequence_entries           TO cms_hlt_reader;
