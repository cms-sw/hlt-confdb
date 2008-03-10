--
-- grant privileges to cms_hlt tablespace to users cms_hlt_writer/cms_hlt_reader
--



--
-- cms_hlt_w
--

-- SELECT
GRANT SELECT ON SuperIdReleaseAssoc 		TO cms_hlt_w;
GRANT SELECT ON SoftwareReleases		TO cms_hlt_w;
GRANT SELECT ON SoftwareSubsystems		TO cms_hlt_w;
GRANT SELECT ON SoftwarePackages		TO cms_hlt_w;
GRANT SELECT ON ConfigurationPathAssoc 		TO cms_hlt_w;
GRANT SELECT ON StreamPathAssoc 		TO cms_hlt_w;
GRANT SELECT ON PrimaryDatasetPathAssoc 	TO cms_hlt_w;
GRANT SELECT ON PathInPathAssoc 		TO cms_hlt_w;
GRANT SELECT ON PathModuleAssoc 		TO cms_hlt_w;
GRANT SELECT ON ConfigurationSequenceAssoc 	TO cms_hlt_w;
GRANT SELECT ON PathSequenceAssoc 		TO cms_hlt_w;
GRANT SELECT ON SequenceInSequenceAssoc 	TO cms_hlt_w;
GRANT SELECT ON SequenceModuleAssoc 		TO cms_hlt_w;
GRANT SELECT ON ConfigurationServiceAssoc 	TO cms_hlt_w;
GRANT SELECT ON ConfigurationEDSourceAssoc 	TO cms_hlt_w;
GRANT SELECT ON ConfigurationESSourceAssoc 	TO cms_hlt_w;
GRANT SELECT ON ConfigurationESModuleAssoc 	TO cms_hlt_w;
GRANT SELECT ON ConfigurationParamSetAssoc 	TO cms_hlt_w;
GRANT SELECT ON Paths 				TO cms_hlt_w;
GRANT SELECT ON Sequences 			TO cms_hlt_w;
GRANT SELECT ON Services 			TO cms_hlt_w;
GRANT SELECT ON ServiceTemplates 		TO cms_hlt_w;
GRANT SELECT ON EDSources 			TO cms_hlt_w;
GRANT SELECT ON EDSourceTemplates 		TO cms_hlt_w;
GRANT SELECT ON ESSources 			TO cms_hlt_w;
GRANT SELECT ON ESSourceTemplates 		TO cms_hlt_w;
GRANT SELECT ON ESModules 			TO cms_hlt_w;
GRANT SELECT ON ESModuleTemplates 		TO cms_hlt_w;
GRANT SELECT ON Modules 			TO cms_hlt_w;
GRANT SELECT ON ModuleTemplates 		TO cms_hlt_w;
GRANT SELECT ON ModuleTypes 			TO cms_hlt_w;
GRANT SELECT ON Configurations 			TO cms_hlt_w;
GRANT SELECT ON LockedConfigurations  		TO cms_hlt_w;
GRANT SELECT ON Streams		  		TO cms_hlt_w;
GRANT SELECT ON PrimaryDatasets	  		TO cms_hlt_w;
GRANT SELECT ON Directories 			TO cms_hlt_w;
GRANT SELECT ON Int32ParamValues 		TO cms_hlt_w;
GRANT SELECT ON VInt32ParamValues 		TO cms_hlt_w;
GRANT SELECT ON UInt32ParamValues 		TO cms_hlt_w;
GRANT SELECT ON VUInt32ParamValues 		TO cms_hlt_w;
GRANT SELECT ON BoolParamValues 		TO cms_hlt_w;
GRANT SELECT ON DoubleParamValues 		TO cms_hlt_w;
GRANT SELECT ON VDoubleParamValues 		TO cms_hlt_w;
GRANT SELECT ON StringParamValues 		TO cms_hlt_w;
GRANT SELECT ON VStringParamValues 		TO cms_hlt_w;
GRANT SELECT ON InputTagParamValues 		TO cms_hlt_w;
GRANT SELECT ON VInputTagParamValues 		TO cms_hlt_w;
GRANT SELECT ON EventIDParamValues 		TO cms_hlt_w;
GRANT SELECT ON VEventIDParamValues 		TO cms_hlt_w;
GRANT SELECT ON FileInPathParamValues 		TO cms_hlt_w;
GRANT SELECT ON SuperIdParameterAssoc 		TO cms_hlt_w;
GRANT SELECT ON SuperIdParamSetAssoc 		TO cms_hlt_w;
GRANT SELECT ON SuperIdVecParamSetAssoc 	TO cms_hlt_w;
GRANT SELECT ON ParameterSets 			TO cms_hlt_w;
GRANT SELECT ON VecParameterSets 		TO cms_hlt_w;
GRANT SELECT ON Parameters 			TO cms_hlt_w;
GRANT SELECT ON SuperIds 			TO cms_hlt_w;
GRANT SELECT ON ParameterTypes 			TO cms_hlt_w;

GRANT SELECT ON ReleaseId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON SubsysId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON PackageId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON DirId_Sequence 			TO cms_hlt_w;
GRANT SELECT ON ConfigId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON StreamId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON DatasetId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON SuperId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON PathId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON SequenceId_Sequence 		TO cms_hlt_w;
GRANT SELECT ON ParamId_Sequence 		TO cms_hlt_w;


-- INSERT
GRANT INSERT ON SuperIdReleaseAssoc 		TO cms_hlt_w;
GRANT INSERT ON ConfigurationPathAssoc 		TO cms_hlt_w;
GRANT INSERT ON StreamPathAssoc 		TO cms_hlt_w;
GRANT INSERT ON PrimaryDatasetPathAssoc		TO cms_hlt_w;
GRANT INSERT ON PathInPathAssoc 		TO cms_hlt_w;
GRANT INSERT ON PathModuleAssoc 		TO cms_hlt_w;
GRANT INSERT ON ConfigurationSequenceAssoc 	TO cms_hlt_w;
GRANT INSERT ON PathSequenceAssoc 		TO cms_hlt_w;
GRANT INSERT ON SequenceInSequenceAssoc 	TO cms_hlt_w;
GRANT INSERT ON SequenceModuleAssoc 		TO cms_hlt_w;
GRANT INSERT ON ConfigurationServiceAssoc 	TO cms_hlt_w;
GRANT INSERT ON ConfigurationEDSourceAssoc 	TO cms_hlt_w;
GRANT INSERT ON ConfigurationESSourceAssoc 	TO cms_hlt_w;
GRANT INSERT ON ConfigurationESModuleAssoc 	TO cms_hlt_w;
GRANT INSERT ON ConfigurationParamSetAssoc 	TO cms_hlt_w;
GRANT INSERT ON Paths 				TO cms_hlt_w;
GRANT INSERT ON Sequences 			TO cms_hlt_w;
GRANT INSERT ON Services 			TO cms_hlt_w;
GRANT INSERT ON EDSources 			TO cms_hlt_w;
GRANT INSERT ON ESSources 			TO cms_hlt_w;
GRANT INSERT ON ESModules 			TO cms_hlt_w;
GRANT INSERT ON Modules 			TO cms_hlt_w;
GRANT INSERT ON Configurations 			TO cms_hlt_w;
GRANT INSERT ON LockedConfigurations		TO cms_hlt_w;
GRANT INSERT ON Streams				TO cms_hlt_w;
GRANT INSERT ON PrimaryDatasets			TO cms_hlt_w;
GRANT INSERT ON Directories 			TO cms_hlt_w;
GRANT INSERT ON Int32ParamValues 		TO cms_hlt_w;
GRANT INSERT ON VInt32ParamValues 		TO cms_hlt_w;
GRANT INSERT ON UInt32ParamValues 		TO cms_hlt_w;
GRANT INSERT ON VUInt32ParamValues 		TO cms_hlt_w;
GRANT INSERT ON BoolParamValues 		TO cms_hlt_w;
GRANT INSERT ON DoubleParamValues 		TO cms_hlt_w;
GRANT INSERT ON VDoubleParamValues 		TO cms_hlt_w;
GRANT INSERT ON StringParamValues 		TO cms_hlt_w;
GRANT INSERT ON VStringParamValues 		TO cms_hlt_w;
GRANT INSERT ON InputTagParamValues 		TO cms_hlt_w;
GRANT INSERT ON VInputTagParamValues 		TO cms_hlt_w;
GRANT INSERT ON EventIDParamValues 		TO cms_hlt_w;
GRANT INSERT ON VEventIDParamValues 		TO cms_hlt_w;
GRANT INSERT ON FileInPathParamValues 		TO cms_hlt_w;
GRANT INSERT ON SuperIdParameterAssoc 		TO cms_hlt_w;
GRANT INSERT ON SuperIdParamSetAssoc 		TO cms_hlt_w;
GRANT INSERT ON SuperIdVecParamSetAssoc 	TO cms_hlt_w;
GRANT INSERT ON ParameterSets 			TO cms_hlt_w;
GRANT INSERT ON VecParameterSets 		TO cms_hlt_w;
GRANT INSERT ON Parameters 			TO cms_hlt_w;
GRANT INSERT ON SuperIds 			TO cms_hlt_w;


--  DELETE
GRANT DELETE ON Directories		 	TO cms_hlt_w;
GRANT DELETE ON LockedConfigurations	 	TO cms_hlt_w;


-- EXECUTE
GRANT EXECUTE ON load_parameter_value           TO cms_hlt_w;
GRANT EXECUTE ON load_parameters                TO cms_hlt_w;
GRANT EXECUTE ON load_template                  TO cms_hlt_w;
GRANT EXECUTE ON load_templates                 TO cms_hlt_w;
GRANT EXECUTE ON load_templates_for_config      TO cms_hlt_w;
GRANT EXECUTE ON load_configuration             TO cms_hlt_w;

GRANT SELECT  ON tmp_template_table             TO cms_hlt_w;
GRANT SELECT  ON tmp_instance_table             TO cms_hlt_w;
GRANT SELECT  ON tmp_parameter_table            TO cms_hlt_w;
GRANT SELECT  ON tmp_boolean_table              TO cms_hlt_w;
GRANT SELECT  ON tmp_int_table                  TO cms_hlt_w;
GRANT SELECT  ON tmp_real_table                 TO cms_hlt_w;
GRANT SELECT  ON tmp_string_table               TO cms_hlt_w;
GRANT SELECT  ON tmp_path_entries               TO cms_hlt_w;
GRANT SELECT  ON tmp_sequence_entries           TO cms_hlt_w;



--
-- cms_hlt_r
--

--  SELECT
GRANT SELECT ON SuperIdReleaseAssoc 		TO cms_hlt_r;
GRANT SELECT ON SoftwareReleases		TO cms_hlt_r;
GRANT SELECT ON SoftwareSubsystems		TO cms_hlt_r;
GRANT SELECT ON SoftwarePackages		TO cms_hlt_r;
GRANT SELECT ON ConfigurationPathAssoc 		TO cms_hlt_r;
GRANT SELECT ON StreamPathAssoc 		TO cms_hlt_r;
GRANT SELECT ON PrimaryDatasetPathAssoc		TO cms_hlt_r;
GRANT SELECT ON PathInPathAssoc 		TO cms_hlt_r;
GRANT SELECT ON PathModuleAssoc 		TO cms_hlt_r;
GRANT SELECT ON ConfigurationSequenceAssoc 	TO cms_hlt_r;
GRANT SELECT ON PathSequenceAssoc 		TO cms_hlt_r;
GRANT SELECT ON SequenceInSequenceAssoc 	TO cms_hlt_r;
GRANT SELECT ON SequenceModuleAssoc 		TO cms_hlt_r;
GRANT SELECT ON ConfigurationServiceAssoc 	TO cms_hlt_r;
GRANT SELECT ON ConfigurationEDSourceAssoc 	TO cms_hlt_r;
GRANT SELECT ON ConfigurationESSourceAssoc 	TO cms_hlt_r;
GRANT SELECT ON ConfigurationESModuleAssoc 	TO cms_hlt_r;
GRANT SELECT ON ConfigurationParamSetAssoc 	TO cms_hlt_r;
GRANT SELECT ON Paths 				TO cms_hlt_r;
GRANT SELECT ON Sequences 			TO cms_hlt_r;
GRANT SELECT ON Services 			TO cms_hlt_r;
GRANT SELECT ON ServiceTemplates 		TO cms_hlt_r;
GRANT SELECT ON EDSources 			TO cms_hlt_r;
GRANT SELECT ON EDSourceTemplates 		TO cms_hlt_r;
GRANT SELECT ON ESSources 			TO cms_hlt_r;
GRANT SELECT ON ESSourceTemplates 		TO cms_hlt_r;
GRANT SELECT ON ESModules 			TO cms_hlt_r;
GRANT SELECT ON ESModuleTemplates 		TO cms_hlt_r;
GRANT SELECT ON Modules 			TO cms_hlt_r;
GRANT SELECT ON ModuleTemplates 		TO cms_hlt_r;
GRANT SELECT ON ModuleTypes 			TO cms_hlt_r;
GRANT SELECT ON Configurations 			TO cms_hlt_r;
GRANT SELECT ON LockedConfigurations		TO cms_hlt_r;
GRANT SELECT ON Streams 			TO cms_hlt_r;
GRANT SELECT ON PrimaryDatasets			TO cms_hlt_r;
GRANT SELECT ON Directories 			TO cms_hlt_r;
GRANT SELECT ON Int32ParamValues 		TO cms_hlt_r;
GRANT SELECT ON VInt32ParamValues 		TO cms_hlt_r;
GRANT SELECT ON UInt32ParamValues 		TO cms_hlt_r;
GRANT SELECT ON VUInt32ParamValues 		TO cms_hlt_r;
GRANT SELECT ON BoolParamValues 		TO cms_hlt_r;
GRANT SELECT ON DoubleParamValues 		TO cms_hlt_r;
GRANT SELECT ON VDoubleParamValues 		TO cms_hlt_r;
GRANT SELECT ON StringParamValues 		TO cms_hlt_r;
GRANT SELECT ON VStringParamValues 		TO cms_hlt_r;
GRANT SELECT ON InputTagParamValues 		TO cms_hlt_r;
GRANT SELECT ON VInputTagParamValues 		TO cms_hlt_r;
GRANT SELECT ON EventIDParamValues 		TO cms_hlt_r;
GRANT SELECT ON VEventIDParamValues 		TO cms_hlt_r;
GRANT SELECT ON FileInPathParamValues 		TO cms_hlt_r;
GRANT SELECT ON SuperIdParameterAssoc 		TO cms_hlt_r;
GRANT SELECT ON SuperIdParamSetAssoc 		TO cms_hlt_r;
GRANT SELECT ON SuperIdVecParamSetAssoc 	TO cms_hlt_r;
GRANT SELECT ON ParameterSets 			TO cms_hlt_r;
GRANT SELECT ON VecParameterSets 		TO cms_hlt_r;
GRANT SELECT ON Parameters 			TO cms_hlt_r;
GRANT SELECT ON SuperIds 			TO cms_hlt_r;
GRANT SELECT ON ParameterTypes 			TO cms_hlt_r;

GRANT SELECT ON ReleaseId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON SubsysId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON PackageId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON DirId_Sequence 			TO cms_hlt_r;
GRANT SELECT ON ConfigId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON StreamId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON DatasetId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON SuperId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON PathId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON SequenceId_Sequence 		TO cms_hlt_r;
GRANT SELECT ON ParamId_Sequence 		TO cms_hlt_r;

-- INSERT

-- DELETE

-- EXECUTE
GRANT EXECUTE ON load_parameter_value           TO cms_hlt_r;
GRANT EXECUTE ON load_parameters                TO cms_hlt_r;
GRANT EXECUTE ON load_template                  TO cms_hlt_r;
GRANT EXECUTE ON load_templates                 TO cms_hlt_r;
GRANT EXECUTE ON load_templates_for_config      TO cms_hlt_r;
GRANT EXECUTE ON load_configuration             TO cms_hlt_r;

GRANT SELECT  ON tmp_template_table             TO cms_hlt_r;
GRANT SELECT  ON tmp_instance_table             TO cms_hlt_r;
GRANT SELECT  ON tmp_parameter_table            TO cms_hlt_r;
GRANT SELECT  ON tmp_boolean_table              TO cms_hlt_r;
GRANT SELECT  ON tmp_int_table                  TO cms_hlt_r;
GRANT SELECT  ON tmp_real_table                 TO cms_hlt_r;
GRANT SELECT  ON tmp_string_table               TO cms_hlt_r;
GRANT SELECT  ON tmp_path_entries               TO cms_hlt_r;
GRANT SELECT  ON tmp_sequence_entries           TO cms_hlt_r;
