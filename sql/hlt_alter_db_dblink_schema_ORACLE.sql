--
-- CMS High Level Trigger Configuration Database Schema: ORACLE
-- ------------------------------------------------------------
-- 
-- CREATED:
-- 07/03/2012 Raul Jimenez Estupinan  <raul.jimenez.estupinan@cern.ch>
-- 
-- NOTE: This script prepares a copy of a CMS_HLT schema for being accessed
-- as a read-only schema using database links over an active dataguard connection.
--

-- ALTERING THE USER PASSWORD:
-- alter user CMS_HLT_GUI   identified by <password> REPLACE temp5678;
-- alter user CMS_HLT_GUI_W identified by <password> REPLACE temp5678;
-- alter user CMS_HLT_GUI_R identified by <password> REPLACE temp5678;

-- Create a database link to the active dataguard copy: cms_orcon_adg.
CREATE DATABASE LINK cmsonr CONNECT TO CMS_HLT_R IDENTIFIED BY "ConvertMe!" USING 'cms_orcon_adg';

-- Check the existing views:
SELECT * FROM user_objects WHERE OBJECT_TYPE = 'VIEW';

--
-- TABLE 'FileInPathParamValues'
--
DROP TABLE FileInPathParamValues;
CREATE VIEW FileInPathParamValues AS SELECT * FROM FileInPathParamValues@cmsonr;

--
-- TABLE 'VEventIDParamValues'
--
DROP TABLE VEventIDParamValues;
CREATE VIEW VEventIDParamValues AS SELECT * FROM VEventIDParamValues@cmsonr;

--
-- TABLE 'EventIDParamValues'
--
DROP TABLE EventIDParamValues;
CREATE VIEW EventIDParamValues AS SELECT * FROM EventIDParamValues@cmsonr;


--
-- TABLE 'VInputTagParamValues'
--
DROP TABLE VInputTagParamValues;
CREATE VIEW VInputTagParamValues AS SELECT * FROM VInputTagParamValues@cmsonr;

--
-- TABLE 'InputTagParamValues'
--
DROP TABLE InputTagParamValues;
CREATE VIEW InputTagParamValues AS SELECT * FROM InputTagParamValues@cmsonr;

--
-- TABLE 'VStringParamValues'
--
DROP TABLE VStringParamValues;
CREATE VIEW VStringParamValues AS SELECT * FROM VStringParamValues@cmsonr;

--
-- TABLE 'StringParamValues'
--
DROP TABLE StringParamValues;
CREATE VIEW StringParamValues AS SELECT * FROM StringParamValues@cmsonr;

--
-- TABLE 'VDoubleParamValues'
--
DROP TABLE VDoubleParamValues;
CREATE VIEW VDoubleParamValues AS SELECT * FROM VDoubleParamValues@cmsonr;

--
-- TABLE 'DoubleParamValues'
--
DROP TABLE DoubleParamValues;
CREATE VIEW DoubleParamValues AS SELECT * FROM DoubleParamValues@cmsonr;

--
-- TABLE 'BoolParamValues'
--
DROP TABLE BoolParamValues;
CREATE VIEW BoolParamValues AS SELECT * FROM BoolParamValues@cmsonr;

--
-- TABLE 'VUInt64ParamValues'
--
DROP TABLE VUInt64ParamValues;
CREATE VIEW VUInt64ParamValues AS SELECT * FROM VUInt64ParamValues@cmsonr;

--
-- TABLE 'UInt64ParamValues'
--
DROP TABLE UInt64ParamValues;
CREATE VIEW UInt64ParamValues AS SELECT * FROM UInt64ParamValues@cmsonr;

--
-- TABLE 'VInt64ParamValues'
--
DROP TABLE VInt64ParamValues;
CREATE VIEW VInt64ParamValues AS SELECT * FROM VInt64ParamValues@cmsonr;

--
-- TABLE 'Int64ParamValues'
--
DROP TABLE Int64ParamValues;
CREATE VIEW Int64ParamValues AS SELECT * FROM Int64ParamValues@cmsonr;

--
-- TABLE 'VUInt32ParamValues'
--
DROP TABLE VUInt32ParamValues;
CREATE VIEW VUInt32ParamValues AS SELECT * FROM VUInt32ParamValues@cmsonr;

--
-- TABLE 'UInt32ParamValues'
--
DROP TABLE UInt32ParamValues;
CREATE VIEW UInt32ParamValues AS SELECT * FROM UInt32ParamValues@cmsonr;

--
-- TABLE 'VInt32ParamValues'
--
DROP TABLE VInt32ParamValues;
CREATE VIEW VInt32ParamValues AS SELECT * FROM VInt32ParamValues@cmsonr;

--
-- TABLE 'Int32ParamValues'
--
DROP TABLE Int32ParamValues;
CREATE VIEW Int32ParamValues AS SELECT * FROM Int32ParamValues@cmsonr;

--
-- TABLE 'SuperIdParameterAssoc'
--
DROP TABLE SuperIdParameterAssoc;
CREATE VIEW SuperIdParameterAssoc AS SELECT * FROM SuperIdParameterAssoc@cmsonr;

--
-- TABLE Parameters
--
DROP TABLE Parameters;
CREATE VIEW Parameters AS SELECT * FROM Parameters@cmsonr;

-- TABLE ParameterTypes
DROP TABLE ParameterTypes;
CREATE VIEW ParameterTypes AS SELECT * FROM ParameterTypes@cmsonr;

--
-- TABLE 'SuperIdVecParamSetAssoc'
--
DROP TABLE SuperIdVecParamSetAssoc;
CREATE VIEW SuperIdVecParamSetAssoc AS SELECT * FROM SuperIdVecParamSetAssoc@cmsonr;

--
-- TABLE 'SuperIdParamSetAssoc'
--
DROP TABLE SuperIdParamSetAssoc;
CREATE VIEW SuperIdParamSetAssoc AS SELECT * FROM SuperIdParamSetAssoc@cmsonr;

--
-- TABLE 'ConfigurationParamSetAssoc'
--
DROP TABLE ConfigurationParamSetAssoc;
CREATE VIEW ConfigurationParamSetAssoc AS SELECT * FROM ConfigurationParamSetAssoc@cmsonr;

--
-- TABLE 'VecParameterSets'
--
DROP TABLE VecParameterSets;
CREATE VIEW VecParameterSets AS SELECT * FROM VecParameterSets@cmsonr;

--
-- TABLE 'ParameterSets'
--
DROP TABLE ParameterSets;
CREATE VIEW ParameterSets AS SELECT * FROM ParameterSets@cmsonr;

--
-- TABLE 'StreamECStatementAssoc'
--
DROP TABLE ECStatementAssoc;
CREATE VIEW ECStatementAssoc AS SELECT * FROM ECStatementAssoc@cmsonr;

--
-- TABLE 'EventContentStatements'
--
DROP TABLE EventContentStatements;
CREATE VIEW EventContentStatements AS SELECT * FROM EventContentStatements@cmsonr;

--
-- TABLE 'SequenceModuleAssoc'
--
DROP TABLE SequenceOutputModAssoc;
CREATE VIEW SequenceOutputModAssoc AS SELECT * FROM SequenceOutputModAssoc@cmsonr;

--
-- TABLE 'SequenceModuleAssoc'
--
DROP TABLE SequenceModuleAssoc;
CREATE VIEW SequenceModuleAssoc AS SELECT * FROM SequenceModuleAssoc@cmsonr;

--
-- TABLE 'PathModuleAssoc'
--
DROP TABLE PathModuleAssoc;
CREATE VIEW PathModuleAssoc AS SELECT * FROM PathModuleAssoc@cmsonr;

--
-- TABLE 'PathOutputModAssoc'
--
DROP TABLE PathOutputModAssoc;
CREATE VIEW PathOutputModAssoc AS SELECT * FROM PathOutputModAssoc@cmsonr;

--
-- TABLE 'Modules'
--
DROP TABLE Modules;
CREATE VIEW Modules AS SELECT * FROM Modules@cmsonr;

--
-- TABLE 'ModuleTemplates'
--
DROP TABLE ModuleTemplates;
CREATE VIEW ModuleTemplates AS SELECT * FROM ModuleTemplates@cmsonr;

--
-- TABLE 'ModuleTypes'
--
DROP TABLE ModuleTypes;
CREATE VIEW ModuleTypes AS SELECT * FROM ModuleTypes@cmsonr;

--
-- TABLE 'ConfigurationESModuleAssoc'
--
DROP TABLE ConfigurationESModuleAssoc;
CREATE VIEW ConfigurationESModuleAssoc AS SELECT * FROM ConfigurationESModuleAssoc@cmsonr;

--
-- TABLE 'ESModules'
--
DROP TABLE ESModules;
CREATE VIEW ESModules AS SELECT * FROM ESModules@cmsonr;

--
-- TABLE 'ESModuleTemplates'
--
DROP TABLE ESModuleTemplates;
CREATE VIEW ESModuleTemplates AS SELECT * FROM ESModuleTemplates@cmsonr;

--
-- TABLE 'ConfigurationESSourceAssoc'
--
DROP TABLE ConfigurationESSourceAssoc;
CREATE VIEW ConfigurationESSourceAssoc AS SELECT * FROM ConfigurationESSourceAssoc@cmsonr;

--
-- TABLE 'ESSources'
--
DROP TABLE ESSources;
CREATE VIEW ESSources AS SELECT * FROM ESSources@cmsonr;

--
-- TABLE 'ESSourceTemplates'
--
DROP TABLE ESSourceTemplates;
CREATE VIEW ESSourceTemplates AS SELECT * FROM ESSourceTemplates@cmsonr;

--
-- TABLE 'ConfigurationEDSourceAssoc'
--
DROP TABLE ConfigurationEDSourceAssoc;
CREATE VIEW ConfigurationEDSourceAssoc AS SELECT * FROM ConfigurationEDSourceAssoc@cmsonr;

--
-- TABLE 'EDSources'
--
DROP TABLE EDSources;
CREATE VIEW EDSources AS SELECT * FROM EDSources@cmsonr;

--
-- TABLE 'EDSourceTemplates'
--
DROP TABLE EDSourceTemplates;
CREATE VIEW EDSourceTemplates AS SELECT * FROM EDSourceTemplates@cmsonr;

--
-- TABLE 'ConfigurationServiceAssoc'
--
DROP TABLE ConfigurationServiceAssoc;
CREATE VIEW ConfigurationServiceAssoc AS SELECT * FROM ConfigurationServiceAssoc@cmsonr;

--
-- TABLE 'Services'
--
DROP TABLE Services;
CREATE VIEW Services AS SELECT * FROM Services@cmsonr;

--
-- TABLE 'ServiceTemplates'
--
DROP TABLE ServiceTemplates;
CREATE VIEW ServiceTemplates AS SELECT * FROM ServiceTemplates@cmsonr;

--
-- TABLE 'SequenceInSequenceAssoc'
--
DROP TABLE SequenceInSequenceAssoc;
CREATE VIEW SequenceInSequenceAssoc AS SELECT * FROM SequenceInSequenceAssoc@cmsonr;

--
-- TABLE 'PathSequenceAssoc'
--
DROP TABLE PathSequenceAssoc;
CREATE VIEW PathSequenceAssoc AS SELECT * FROM PathSequenceAssoc@cmsonr;

--
-- TABLE 'ConfigurationSequenceAssoc'
--
DROP TABLE ConfigurationSequenceAssoc;
CREATE VIEW ConfigurationSequenceAssoc AS SELECT * FROM ConfigurationSequenceAssoc@cmsonr;

--
-- TABLE 'Sequences'
--
DROP TABLE Sequences;
CREATE VIEW Sequences AS SELECT * FROM Sequences@cmsonr;

--
-- TABLE 'PathStreamDataSetAssoc'
--
DROP TABLE PathStreamDatasetAssoc;
CREATE VIEW PathStreamDatasetAssoc AS SELECT * FROM PathStreamDatasetAssoc@cmsonr;

-- TABLE 'StreamDatasetAssoc'
DROP TABLE StreamDatasetAssoc;
CREATE VIEW StreamDatasetAssoc AS SELECT * FROM StreamDatasetAssoc@cmsonr;

--
-- TABLE 'PrimaryDatasets'
--
DROP TABLE PrimaryDatasets;
CREATE VIEW PrimaryDatasets AS SELECT * FROM PrimaryDatasets@cmsonr;

--
-- TABLE 'EventContentStreamAssoc'
--
DROP TABLE ECStreamAssoc;
CREATE VIEW ECStreamAssoc AS SELECT * FROM ECStreamAssoc@cmsonr;

--
-- TABLE 'Streams'
--
DROP TABLE Streams;
CREATE VIEW Streams AS SELECT * FROM Streams@cmsonr;

--
-- TABLE 'PathInPathAssoc'
--
DROP TABLE PathInPathAssoc;
CREATE VIEW PathInPathAssoc AS SELECT * FROM PathInPathAssoc@cmsonr;

--
-- TABLE 'ConfigurationPathAssoc'
--
DROP TABLE ConfigurationPathAssoc;
CREATE VIEW ConfigurationPathAssoc AS SELECT * FROM ConfigurationPathAssoc@cmsonr;

--
-- TABLE 'Paths'
--
DROP TABLE Paths;
-- NO CLOBs are allowed with dblinks.
--CREATE VIEW Paths AS SELECT * FROM Paths@cmsonr;
CREATE VIEW Paths AS SELECT pathId, name, isEndPath FROM Paths@cmsonr;


--
-- TABLE 'SuperIdReleaseAssoc'
--
DROP TABLE SuperIdReleaseAssoc;
CREATE VIEW SuperIdReleaseAssoc AS SELECT * FROM SuperIdReleaseAssoc@cmsonr;

--
-- TABLE 'SuperIds'
--
DROP TABLE SuperIds;
CREATE VIEW SuperIds AS SELECT * FROM SuperIds@cmsonr;

--
-- TABLE 'ConfigurationContentAssoc'
--
DROP TABLE ConfigurationContentAssoc;
CREATE VIEW ConfigurationContentAssoc AS SELECT * FROM ConfigurationContentAssoc@cmsonr;

--
-- TABLE 'EventContents'
--
DROP TABLE EventContents;
CREATE VIEW EventContents AS SELECT * FROM EventContents@cmsonr;

--
-- TABLE 'LockedConfigurations'
--
DROP TABLE LockedConfigurations;
-- CREATE VIEW LockedConfigurations AS SELECT * FROM LockedConfigurations@cmsonr;
-- Needed to write into this table. NOTE: foreign key dropped.
CREATE TABLE LockedConfigurations
(
	parentDirId	NUMBER	  	NOT NULL,
	config		VARCHAR2(128)	NOT NULL,
	userName        VARCHAR2(128)	NOT NULL,
	CONSTRAINT uk_lockedConfigurations UNIQUE (parentDirId,config)
);


--
-- TABLE 'Configurations'
--
DROP TABLE Configurations;
CREATE VIEW Configurations AS SELECT * FROM Configurations@cmsonr;

--
-- TABLE 'Directories'
--
DROP TABLE Directories;
CREATE VIEW Directories AS SELECT * FROM Directories@cmsonr;

--
-- TABLE 'SoftwarePackages'
--
DROP TABLE SoftwarePackages;
CREATE VIEW SoftwarePackages AS SELECT * FROM SoftwarePackages@cmsonr;

--
-- TABLE 'SoftwareSubsystems'
--
DROP TABLE SoftwareSubsystems;
CREATE VIEW SoftwareSubsystems AS SELECT * FROM SoftwareSubsystems@cmsonr;

--
-- TABLE 'SoftwareReleases'
--
DROP TABLE SoftwareReleases;
CREATE VIEW SoftwareReleases AS SELECT * FROM SoftwareReleases@cmsonr;




-- RECOMPILE STORED PROCEDURES:
ALTER PROCEDURE load_parameter_value COMPILE;
ALTER PROCEDURE load_parameters COMPILE;
ALTER PROCEDURE load_template COMPILE;
ALTER PROCEDURE load_templates COMPILE;
ALTER PROCEDURE load_templates_for_config COMPILE;
ALTER PROCEDURE load_configuration COMPILE;




--
-- GRANT SELECT TO WRITER ACCOUNT
--
GRANT SELECT ON FileInPathParamValues             TO CMS_HLT_GUI_W;
GRANT SELECT ON VEventIDParamValues               TO CMS_HLT_GUI_W;
GRANT SELECT ON EventIDParamValues                TO CMS_HLT_GUI_W;
GRANT SELECT ON VInputTagParamValues              TO CMS_HLT_GUI_W;
GRANT SELECT ON InputTagParamValues               TO CMS_HLT_GUI_W;
GRANT SELECT ON VStringParamValues                TO CMS_HLT_GUI_W;
GRANT SELECT ON StringParamValues                 TO CMS_HLT_GUI_W;
GRANT SELECT ON VDoubleParamValues                TO CMS_HLT_GUI_W;
GRANT SELECT ON DoubleParamValues                 TO CMS_HLT_GUI_W;
GRANT SELECT ON BoolParamValues                   TO CMS_HLT_GUI_W;
GRANT SELECT ON VUInt64ParamValues                TO CMS_HLT_GUI_W;
GRANT SELECT ON UInt64ParamValues                 TO CMS_HLT_GUI_W;
GRANT SELECT ON VInt64ParamValues                 TO CMS_HLT_GUI_W;
GRANT SELECT ON Int64ParamValues                  TO CMS_HLT_GUI_W;
GRANT SELECT ON VUInt32ParamValues                TO CMS_HLT_GUI_W;
GRANT SELECT ON UInt32ParamValues                 TO CMS_HLT_GUI_W;
GRANT SELECT ON VInt32ParamValues                 TO CMS_HLT_GUI_W;
GRANT SELECT ON Int32ParamValues                  TO CMS_HLT_GUI_W;
GRANT SELECT ON SuperIdParameterAssoc             TO CMS_HLT_GUI_W;
GRANT SELECT ON Parameters                        TO CMS_HLT_GUI_W;
GRANT SELECT ON ParameterTypes                    TO CMS_HLT_GUI_W;
GRANT SELECT ON SuperIdVecParamSetAssoc           TO CMS_HLT_GUI_W;
GRANT SELECT ON SuperIdParamSetAssoc              TO CMS_HLT_GUI_W;
GRANT SELECT ON ConfigurationParamSetAssoc        TO CMS_HLT_GUI_W;
GRANT SELECT ON VecParameterSets                  TO CMS_HLT_GUI_W;
GRANT SELECT ON ParameterSets                     TO CMS_HLT_GUI_W;
GRANT SELECT ON ECStatementAssoc                  TO CMS_HLT_GUI_W;
GRANT SELECT ON EventContentStatements            TO CMS_HLT_GUI_W;
GRANT SELECT ON SequenceOutputModAssoc            TO CMS_HLT_GUI_W;
GRANT SELECT ON SequenceModuleAssoc               TO CMS_HLT_GUI_W;
GRANT SELECT ON PathModuleAssoc                   TO CMS_HLT_GUI_W;
GRANT SELECT ON PathOutputModAssoc                TO CMS_HLT_GUI_W;
GRANT SELECT ON Modules                           TO CMS_HLT_GUI_W;
GRANT SELECT ON ModuleTemplates                   TO CMS_HLT_GUI_W;
GRANT SELECT ON ModuleTypes                       TO CMS_HLT_GUI_W;
GRANT SELECT ON ConfigurationESModuleAssoc        TO CMS_HLT_GUI_W;
GRANT SELECT ON ESModules                         TO CMS_HLT_GUI_W;
GRANT SELECT ON ESModuleTemplates                 TO CMS_HLT_GUI_W;
GRANT SELECT ON ConfigurationESSourceAssoc        TO CMS_HLT_GUI_W;
GRANT SELECT ON ESSources                         TO CMS_HLT_GUI_W;
GRANT SELECT ON ESSourceTemplates                 TO CMS_HLT_GUI_W;
GRANT SELECT ON ConfigurationEDSourceAssoc        TO CMS_HLT_GUI_W;
GRANT SELECT ON EDSources                         TO CMS_HLT_GUI_W;
GRANT SELECT ON EDSourceTemplates                 TO CMS_HLT_GUI_W;
GRANT SELECT ON ConfigurationServiceAssoc         TO CMS_HLT_GUI_W;
GRANT SELECT ON Services                          TO CMS_HLT_GUI_W;
GRANT SELECT ON ServiceTemplates                  TO CMS_HLT_GUI_W;
GRANT SELECT ON SequenceInSequenceAssoc           TO CMS_HLT_GUI_W;
GRANT SELECT ON PathSequenceAssoc                 TO CMS_HLT_GUI_W;
GRANT SELECT ON ConfigurationSequenceAssoc        TO CMS_HLT_GUI_W;
GRANT SELECT ON Sequences                         TO CMS_HLT_GUI_W;
GRANT SELECT ON PathStreamDatasetAssoc            TO CMS_HLT_GUI_W;
GRANT SELECT ON StreamDatasetAssoc                TO CMS_HLT_GUI_W;
GRANT SELECT ON PrimaryDatasets                   TO CMS_HLT_GUI_W;
GRANT SELECT ON ECStreamAssoc                     TO CMS_HLT_GUI_W;
GRANT SELECT ON Streams                           TO CMS_HLT_GUI_W;
GRANT SELECT ON PathInPathAssoc                   TO CMS_HLT_GUI_W;
GRANT SELECT ON ConfigurationPathAssoc            TO CMS_HLT_GUI_W;
GRANT SELECT ON Paths                             TO CMS_HLT_GUI_W;
GRANT SELECT ON SuperIdReleaseAssoc               TO CMS_HLT_GUI_W;
GRANT SELECT ON SuperIds                          TO CMS_HLT_GUI_W;
GRANT SELECT ON ConfigurationContentAssoc         TO CMS_HLT_GUI_W;
GRANT SELECT ON EventContents                     TO CMS_HLT_GUI_W;
GRANT SELECT ON LockedConfigurations              TO CMS_HLT_GUI_W;
GRANT SELECT ON Configurations                    TO CMS_HLT_GUI_W;
GRANT SELECT ON Directories                       TO CMS_HLT_GUI_W;
GRANT SELECT ON SoftwarePackages                  TO CMS_HLT_GUI_W;
GRANT SELECT ON SoftwareSubsystems                TO CMS_HLT_GUI_W;
GRANT SELECT ON SoftwareReleases                  TO CMS_HLT_GUI_W;

-- GRANT INSERT:
GRANT INSERT ON LockedConfigurations              TO CMS_HLT_GUI_W;






--
-- GRANT SELECT TO READER ACCOUNT
--
GRANT SELECT ON FileInPathParamValues            TO CMS_HLT_GUI_R;
GRANT SELECT ON VEventIDParamValues              TO CMS_HLT_GUI_R;
GRANT SELECT ON EventIDParamValues               TO CMS_HLT_GUI_R;
GRANT SELECT ON VInputTagParamValues             TO CMS_HLT_GUI_R;
GRANT SELECT ON InputTagParamValues              TO CMS_HLT_GUI_R;
GRANT SELECT ON VStringParamValues               TO CMS_HLT_GUI_R;
GRANT SELECT ON StringParamValues                TO CMS_HLT_GUI_R;
GRANT SELECT ON VDoubleParamValues               TO CMS_HLT_GUI_R;
GRANT SELECT ON DoubleParamValues                TO CMS_HLT_GUI_R;
GRANT SELECT ON BoolParamValues                  TO CMS_HLT_GUI_R;
GRANT SELECT ON VUInt64ParamValues               TO CMS_HLT_GUI_R;
GRANT SELECT ON UInt64ParamValues                TO CMS_HLT_GUI_R;
GRANT SELECT ON VInt64ParamValues                TO CMS_HLT_GUI_R;
GRANT SELECT ON Int64ParamValues                 TO CMS_HLT_GUI_R;
GRANT SELECT ON VUInt32ParamValues               TO CMS_HLT_GUI_R;
GRANT SELECT ON UInt32ParamValues                TO CMS_HLT_GUI_R;
GRANT SELECT ON VInt32ParamValues                TO CMS_HLT_GUI_R;
GRANT SELECT ON Int32ParamValues                 TO CMS_HLT_GUI_R;
GRANT SELECT ON SuperIdParameterAssoc            TO CMS_HLT_GUI_R;
GRANT SELECT ON Parameters                       TO CMS_HLT_GUI_R;
GRANT SELECT ON ParameterTypes                   TO CMS_HLT_GUI_R;
GRANT SELECT ON SuperIdVecParamSetAssoc          TO CMS_HLT_GUI_R;
GRANT SELECT ON SuperIdParamSetAssoc             TO CMS_HLT_GUI_R;
GRANT SELECT ON ConfigurationParamSetAssoc       TO CMS_HLT_GUI_R;
GRANT SELECT ON VecParameterSets                 TO CMS_HLT_GUI_R;
GRANT SELECT ON ParameterSets                    TO CMS_HLT_GUI_R;
GRANT SELECT ON ECStatementAssoc                 TO CMS_HLT_GUI_R;
GRANT SELECT ON EventContentStatements           TO CMS_HLT_GUI_R;
GRANT SELECT ON SequenceOutputModAssoc           TO CMS_HLT_GUI_R;
GRANT SELECT ON SequenceModuleAssoc              TO CMS_HLT_GUI_R;
GRANT SELECT ON PathModuleAssoc                  TO CMS_HLT_GUI_R;
GRANT SELECT ON PathOutputModAssoc               TO CMS_HLT_GUI_R;
GRANT SELECT ON Modules                          TO CMS_HLT_GUI_R;
GRANT SELECT ON ModuleTemplates                  TO CMS_HLT_GUI_R;
GRANT SELECT ON ModuleTypes                      TO CMS_HLT_GUI_R;
GRANT SELECT ON ConfigurationESModuleAssoc       TO CMS_HLT_GUI_R;
GRANT SELECT ON ESModules                        TO CMS_HLT_GUI_R;
GRANT SELECT ON ESModuleTemplates                TO CMS_HLT_GUI_R;
GRANT SELECT ON ConfigurationESSourceAssoc       TO CMS_HLT_GUI_R;
GRANT SELECT ON ESSources                        TO CMS_HLT_GUI_R;
GRANT SELECT ON ESSourceTemplates                TO CMS_HLT_GUI_R;
GRANT SELECT ON ConfigurationEDSourceAssoc       TO CMS_HLT_GUI_R;
GRANT SELECT ON EDSources                        TO CMS_HLT_GUI_R;
GRANT SELECT ON EDSourceTemplates                TO CMS_HLT_GUI_R;
GRANT SELECT ON ConfigurationServiceAssoc        TO CMS_HLT_GUI_R;
GRANT SELECT ON Services                         TO CMS_HLT_GUI_R;
GRANT SELECT ON ServiceTemplates                 TO CMS_HLT_GUI_R;
GRANT SELECT ON SequenceInSequenceAssoc          TO CMS_HLT_GUI_R;
GRANT SELECT ON PathSequenceAssoc                TO CMS_HLT_GUI_R;
GRANT SELECT ON ConfigurationSequenceAssoc       TO CMS_HLT_GUI_R;
GRANT SELECT ON Sequences                        TO CMS_HLT_GUI_R;
GRANT SELECT ON PathStreamDatasetAssoc           TO CMS_HLT_GUI_R;
GRANT SELECT ON StreamDatasetAssoc               TO CMS_HLT_GUI_R;
GRANT SELECT ON PrimaryDatasets                  TO CMS_HLT_GUI_R;
GRANT SELECT ON ECStreamAssoc                    TO CMS_HLT_GUI_R;
GRANT SELECT ON Streams                          TO CMS_HLT_GUI_R;
GRANT SELECT ON PathInPathAssoc                  TO CMS_HLT_GUI_R;
GRANT SELECT ON ConfigurationPathAssoc           TO CMS_HLT_GUI_R;
GRANT SELECT ON Paths                            TO CMS_HLT_GUI_R;
GRANT SELECT ON SuperIdReleaseAssoc              TO CMS_HLT_GUI_R;
GRANT SELECT ON SuperIds                         TO CMS_HLT_GUI_R;
GRANT SELECT ON ConfigurationContentAssoc        TO CMS_HLT_GUI_R;
GRANT SELECT ON EventContents                    TO CMS_HLT_GUI_R;
GRANT SELECT ON LockedConfigurations             TO CMS_HLT_GUI_R;
GRANT SELECT ON Configurations                   TO CMS_HLT_GUI_R;
GRANT SELECT ON Directories                      TO CMS_HLT_GUI_R;
GRANT SELECT ON SoftwarePackages                 TO CMS_HLT_GUI_R;
GRANT SELECT ON SoftwareSubsystems               TO CMS_HLT_GUI_R;
GRANT SELECT ON SoftwareReleases                 TO CMS_HLT_GUI_R;


-- GRANT EXECUTE:
-- EXECUTE
GRANT EXECUTE ON load_parameter_value           TO CMS_HLT_GUI_R;
GRANT EXECUTE ON load_parameters                TO CMS_HLT_GUI_R;
GRANT EXECUTE ON load_template                  TO CMS_HLT_GUI_R;
GRANT EXECUTE ON load_templates                 TO CMS_HLT_GUI_R;
GRANT EXECUTE ON load_templates_for_config      TO CMS_HLT_GUI_R;
GRANT EXECUTE ON load_configuration             TO CMS_HLT_GUI_R;

GRANT SELECT  ON tmp_template_table             TO CMS_HLT_GUI_R;
GRANT SELECT  ON tmp_instance_table             TO CMS_HLT_GUI_R;
GRANT SELECT  ON tmp_parameter_table            TO CMS_HLT_GUI_R;
GRANT SELECT  ON tmp_boolean_table              TO CMS_HLT_GUI_R;
GRANT SELECT  ON tmp_int_table                  TO CMS_HLT_GUI_R;
GRANT SELECT  ON tmp_real_table                 TO CMS_HLT_GUI_R;
GRANT SELECT  ON tmp_string_table               TO CMS_HLT_GUI_R;
GRANT SELECT  ON tmp_path_entries               TO CMS_HLT_GUI_R;
GRANT SELECT  ON tmp_sequence_entries           TO CMS_HLT_GUI_R;

-- EXECUTE
GRANT EXECUTE ON load_parameter_value           TO CMS_HLT_GUI_W;
GRANT EXECUTE ON load_parameters                TO CMS_HLT_GUI_W;
GRANT EXECUTE ON load_template                  TO CMS_HLT_GUI_W;
GRANT EXECUTE ON load_templates                 TO CMS_HLT_GUI_W;
GRANT EXECUTE ON load_templates_for_config      TO CMS_HLT_GUI_W;
GRANT EXECUTE ON load_configuration             TO CMS_HLT_GUI_W;

GRANT SELECT  ON tmp_template_table             TO CMS_HLT_GUI_W;
GRANT SELECT  ON tmp_instance_table             TO CMS_HLT_GUI_W;
GRANT SELECT  ON tmp_parameter_table            TO CMS_HLT_GUI_W;
GRANT SELECT  ON tmp_boolean_table              TO CMS_HLT_GUI_W;
GRANT SELECT  ON tmp_int_table                  TO CMS_HLT_GUI_W;
GRANT SELECT  ON tmp_real_table                 TO CMS_HLT_GUI_W;
GRANT SELECT  ON tmp_string_table               TO CMS_HLT_GUI_W;
GRANT SELECT  ON tmp_path_entries               TO CMS_HLT_GUI_W;
GRANT SELECT  ON tmp_sequence_entries           TO CMS_HLT_GUI_W;

------------------------------------------------------------------------------------------------
-- SYNONYMS:
-- NOTE: Synonyms must be created for every non-schema-owner account.
------------------------------------------------------------------------------------------------
CREATE OR REPLACE SYNONYM FileInPathParamValues         FOR  CMS_HLT_GUI.FileInPathParamValues           ; 
CREATE OR REPLACE SYNONYM VEventIDParamValues           FOR  CMS_HLT_GUI.VEventIDParamValues             ; 
CREATE OR REPLACE SYNONYM EventIDParamValues            FOR  CMS_HLT_GUI.EventIDParamValues              ; 
CREATE OR REPLACE SYNONYM VInputTagParamValues          FOR  CMS_HLT_GUI.VInputTagParamValues            ; 
CREATE OR REPLACE SYNONYM InputTagParamValues           FOR  CMS_HLT_GUI.InputTagParamValues             ; 
CREATE OR REPLACE SYNONYM VStringParamValues            FOR  CMS_HLT_GUI.VStringParamValues              ; 
CREATE OR REPLACE SYNONYM StringParamValues             FOR  CMS_HLT_GUI.StringParamValues               ; 
CREATE OR REPLACE SYNONYM VDoubleParamValues            FOR  CMS_HLT_GUI.VDoubleParamValues              ; 
CREATE OR REPLACE SYNONYM DoubleParamValues             FOR  CMS_HLT_GUI.DoubleParamValues               ; 
CREATE OR REPLACE SYNONYM BoolParamValues               FOR  CMS_HLT_GUI.BoolParamValues                 ; 
CREATE OR REPLACE SYNONYM VUInt64ParamValues            FOR  CMS_HLT_GUI.VUInt64ParamValues              ; 
CREATE OR REPLACE SYNONYM UInt64ParamValues             FOR  CMS_HLT_GUI.UInt64ParamValues               ; 
CREATE OR REPLACE SYNONYM VInt64ParamValues             FOR  CMS_HLT_GUI.VInt64ParamValues               ; 
CREATE OR REPLACE SYNONYM Int64ParamValues              FOR  CMS_HLT_GUI.Int64ParamValues                ; 
CREATE OR REPLACE SYNONYM VUInt32ParamValues            FOR  CMS_HLT_GUI.VUInt32ParamValues              ; 
CREATE OR REPLACE SYNONYM UInt32ParamValues             FOR  CMS_HLT_GUI.UInt32ParamValues               ; 
CREATE OR REPLACE SYNONYM VInt32ParamValues             FOR  CMS_HLT_GUI.VInt32ParamValues               ; 
CREATE OR REPLACE SYNONYM Int32ParamValues              FOR  CMS_HLT_GUI.Int32ParamValues                ; 
CREATE OR REPLACE SYNONYM SuperIdParameterAssoc         FOR  CMS_HLT_GUI.SuperIdParameterAssoc           ; 
CREATE OR REPLACE SYNONYM Parameters                    FOR  CMS_HLT_GUI.Parameters                      ; 
CREATE OR REPLACE SYNONYM ParameterTypes                FOR  CMS_HLT_GUI.ParameterTypes                  ; 
CREATE OR REPLACE SYNONYM SuperIdVecParamSetAssoc       FOR  CMS_HLT_GUI.SuperIdVecParamSetAssoc         ; 
CREATE OR REPLACE SYNONYM SuperIdParamSetAssoc          FOR  CMS_HLT_GUI.SuperIdParamSetAssoc            ; 
CREATE OR REPLACE SYNONYM ConfigurationParamSetAssoc    FOR  CMS_HLT_GUI.ConfigurationParamSetAssoc      ; 
CREATE OR REPLACE SYNONYM VecParameterSets              FOR  CMS_HLT_GUI.VecParameterSets                ; 
CREATE OR REPLACE SYNONYM ParameterSets                 FOR  CMS_HLT_GUI.ParameterSets                   ; 
CREATE OR REPLACE SYNONYM ECStatementAssoc              FOR  CMS_HLT_GUI.ECStatementAssoc                ; 
CREATE OR REPLACE SYNONYM EventContentStatements        FOR  CMS_HLT_GUI.EventContentStatements          ; 
CREATE OR REPLACE SYNONYM SequenceOutputModAssoc        FOR  CMS_HLT_GUI.SequenceOutputModAssoc          ; 
CREATE OR REPLACE SYNONYM SequenceModuleAssoc           FOR  CMS_HLT_GUI.SequenceModuleAssoc             ; 
CREATE OR REPLACE SYNONYM PathModuleAssoc               FOR  CMS_HLT_GUI.PathModuleAssoc                 ; 
CREATE OR REPLACE SYNONYM PathOutputModAssoc            FOR  CMS_HLT_GUI.PathOutputModAssoc              ; 
CREATE OR REPLACE SYNONYM Modules                       FOR  CMS_HLT_GUI.Modules                         ; 
CREATE OR REPLACE SYNONYM ModuleTemplates               FOR  CMS_HLT_GUI.ModuleTemplates                 ; 
CREATE OR REPLACE SYNONYM ModuleTypes                   FOR  CMS_HLT_GUI.ModuleTypes                     ; 
CREATE OR REPLACE SYNONYM ConfigurationESModuleAssoc    FOR  CMS_HLT_GUI.ConfigurationESModuleAssoc      ; 
CREATE OR REPLACE SYNONYM ESModules                     FOR  CMS_HLT_GUI.ESModules                       ; 
CREATE OR REPLACE SYNONYM ESModuleTemplates             FOR  CMS_HLT_GUI.ESModuleTemplates               ; 
CREATE OR REPLACE SYNONYM ConfigurationESSourceAssoc    FOR  CMS_HLT_GUI.ConfigurationESSourceAssoc      ; 
CREATE OR REPLACE SYNONYM ESSources                     FOR  CMS_HLT_GUI.ESSources                       ; 
CREATE OR REPLACE SYNONYM ESSourceTemplates             FOR  CMS_HLT_GUI.ESSourceTemplates               ; 
CREATE OR REPLACE SYNONYM ConfigurationEDSourceAssoc    FOR  CMS_HLT_GUI.ConfigurationEDSourceAssoc      ; 
CREATE OR REPLACE SYNONYM EDSources                     FOR  CMS_HLT_GUI.EDSources                       ; 
CREATE OR REPLACE SYNONYM EDSourceTemplates             FOR  CMS_HLT_GUI.EDSourceTemplates               ; 
CREATE OR REPLACE SYNONYM ConfigurationServiceAssoc     FOR  CMS_HLT_GUI.ConfigurationServiceAssoc       ; 
CREATE OR REPLACE SYNONYM Services                      FOR  CMS_HLT_GUI.Services                        ; 
CREATE OR REPLACE SYNONYM ServiceTemplates              FOR  CMS_HLT_GUI.ServiceTemplates                ; 
CREATE OR REPLACE SYNONYM SequenceInSequenceAssoc       FOR  CMS_HLT_GUI.SequenceInSequenceAssoc         ; 
CREATE OR REPLACE SYNONYM PathSequenceAssoc             FOR  CMS_HLT_GUI.PathSequenceAssoc               ; 
CREATE OR REPLACE SYNONYM ConfigurationSequenceAssoc    FOR  CMS_HLT_GUI.ConfigurationSequenceAssoc      ; 
CREATE OR REPLACE SYNONYM Sequences                     FOR  CMS_HLT_GUI.Sequences                       ; 
CREATE OR REPLACE SYNONYM PathStreamDatasetAssoc        FOR  CMS_HLT_GUI.PathStreamDatasetAssoc          ; 
CREATE OR REPLACE SYNONYM StreamDatasetAssoc            FOR  CMS_HLT_GUI.StreamDatasetAssoc              ; 
CREATE OR REPLACE SYNONYM PrimaryDatasets               FOR  CMS_HLT_GUI.PrimaryDatasets                 ; 
CREATE OR REPLACE SYNONYM ECStreamAssoc                 FOR  CMS_HLT_GUI.ECStreamAssoc                   ; 
CREATE OR REPLACE SYNONYM Streams                       FOR  CMS_HLT_GUI.Streams                         ; 
CREATE OR REPLACE SYNONYM PathInPathAssoc               FOR  CMS_HLT_GUI.PathInPathAssoc                 ; 
CREATE OR REPLACE SYNONYM ConfigurationPathAssoc        FOR  CMS_HLT_GUI.ConfigurationPathAssoc          ; 
CREATE OR REPLACE SYNONYM Paths                         FOR  CMS_HLT_GUI.Paths                           ; 
CREATE OR REPLACE SYNONYM SuperIdReleaseAssoc           FOR  CMS_HLT_GUI.SuperIdReleaseAssoc             ; 
CREATE OR REPLACE SYNONYM SuperIds                      FOR  CMS_HLT_GUI.SuperIds                        ; 
CREATE OR REPLACE SYNONYM ConfigurationContentAssoc     FOR  CMS_HLT_GUI.ConfigurationContentAssoc       ; 
CREATE OR REPLACE SYNONYM EventContents                 FOR  CMS_HLT_GUI.EventContents                   ; 
CREATE OR REPLACE SYNONYM LockedConfigurations          FOR  CMS_HLT_GUI.LockedConfigurations            ; 
CREATE OR REPLACE SYNONYM Configurations                FOR  CMS_HLT_GUI.Configurations                  ; 
CREATE OR REPLACE SYNONYM Directories                   FOR  CMS_HLT_GUI.Directories                     ; 
CREATE OR REPLACE SYNONYM SoftwarePackages              FOR  CMS_HLT_GUI.SoftwarePackages                ; 
CREATE OR REPLACE SYNONYM SoftwareSubsystems            FOR  CMS_HLT_GUI.SoftwareSubsystems              ; 
CREATE OR REPLACE SYNONYM SoftwareReleases              FOR  CMS_HLT_GUI.SoftwareReleases                ; 


-- procedure synonyms:
CREATE OR REPLACE SYNONYM load_parameter_value        FOR CMS_HLT_GUI.load_parameter_value       ;
CREATE OR REPLACE SYNONYM load_parameters             FOR CMS_HLT_GUI.load_parameters            ;
CREATE OR REPLACE SYNONYM load_template               FOR CMS_HLT_GUI.load_template              ;
CREATE OR REPLACE SYNONYM load_templates              FOR CMS_HLT_GUI.load_templates             ;
CREATE OR REPLACE SYNONYM load_templates_for_config   FOR CMS_HLT_GUI.load_templates_for_config  ;
CREATE OR REPLACE SYNONYM load_configuration          FOR CMS_HLT_GUI.load_configuration         ;

-- intermediate tables synonyms:
CREATE OR REPLACE SYNONYM tmp_template_table          FOR CMS_HLT_GUI.tmp_template_table     ;
CREATE OR REPLACE SYNONYM tmp_instance_table          FOR CMS_HLT_GUI.tmp_instance_table     ;
CREATE OR REPLACE SYNONYM tmp_parameter_table         FOR CMS_HLT_GUI.tmp_parameter_table    ;
CREATE OR REPLACE SYNONYM tmp_boolean_table           FOR CMS_HLT_GUI.tmp_boolean_table      ;
CREATE OR REPLACE SYNONYM tmp_int_table               FOR CMS_HLT_GUI.tmp_int_table          ;
CREATE OR REPLACE SYNONYM tmp_real_table              FOR CMS_HLT_GUI.tmp_real_table         ;
CREATE OR REPLACE SYNONYM tmp_string_table            FOR CMS_HLT_GUI.tmp_string_table       ;
CREATE OR REPLACE SYNONYM tmp_path_entries            FOR CMS_HLT_GUI.tmp_path_entries       ;
CREATE OR REPLACE SYNONYM tmp_sequence_entries        FOR CMS_HLT_GUI.tmp_sequence_entries   ;



-- COMMITTING CHANGES!
COMMIT;
