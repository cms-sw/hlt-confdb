--
-- CMS High Level Trigger Configuration Database Schema: ORACLE
-- ------------------------------------------------------------
--
-- CREATED:
-- 01/12/2007 Philipp Schieferdecker <philipp.schieferdecker@cern.ch>
--

--
-- TABLE 'SoftwareReleases'
--
CREATE TABLE SoftwareReleases
(
	releaseId 	NUMBER,
	releaseTag 	VARCHAR2(32)	NOT NULL UNIQUE,
	PRIMARY KEY(releaseId)
);

-- SEQUENCE 'ReleaseId_Sequence'
CREATE SEQUENCE ReleaseId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'ReleaseId_Trigger'
CREATE OR REPLACE TRIGGER ReleaseId_Trigger
BEFORE INSERT ON SoftwareReleases
FOR EACH ROW
BEGIN
SELECT ReleaseId_Sequence.nextval INTO :NEW.releaseId FROM dual;
END;
/


--
-- TABLE 'Directories'
--
CREATE TABLE Directories
(
	dirId		NUMBER,
	parentDirId	NUMBER,
	dirName		VARCHAR2(512)	NOT NULL UNIQUE,
	created		TIMESTAMP	NOT NULL,
	PRIMARY KEY(dirId)
);

-- SEQUENCE 'DirId_Sequence'
CREATE SEQUENCE DirId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'DirId_Trigger'
CREATE OR REPLACE TRIGGER DirId_Trigger
BEFORE INSERT ON Directories
FOR EACH ROW
BEGIN
SELECT DirId_Sequence.nextval INTO :NEW.dirId FROM dual;
END;
/


--
-- TABLE 'Configurations'
--
CREATE TABLE Configurations
(
	configId   	NUMBER,
	releaseId	NUMBER		NOT NULL,
	configDescriptor VARCHAR2(256)  NOT NULL UNIQUE,
	parentDirId     NUMBER		NOT NULL,
	config     	VARCHAR2(128)   NOT NULL,
	version         NUMBER(4)	NOT NULL,
	created         TIMESTAMP       NOT NULL,
	creator		VARCHAR2(128)	NOT NULL,
	processName	VARCHAR2(32)	NOT NULL,
	UNIQUE (parentDirId,config,version),
	PRIMARY KEY(configId),
	FOREIGN KEY(releaseId)   REFERENCES SoftwareReleases(releaseId),
	FOREIGN KEY(parentDirId) REFERENCES Directories(dirId)
);

-- INDEX ConfigParentDirId_idx
CREATE INDEX ConfigParentDirId_idx ON Configurations(parentDirId);

-- SEQUENCE 'ConfigId_Sequence'
CREATE SEQUENCE ConfigId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'ConfigId_Trigger'
CREATE OR REPLACE TRIGGER ConfigId_Trigger
BEFORE INSERT ON Configurations
FOR EACH ROW
BEGIN
SELECT ConfigId_Sequence.nextval INTO :NEW.configId FROM dual;
END;
/


--
-- TABLE 'LockedConfigurations'
--
CREATE TABLE LockedConfigurations
(
	parentDirId	NUMBER	  	NOT NULL,
	config		VARCHAR2(128)	NOT NULL,
	userName        VARCHAR2(128)	NOT NULL,
	UNIQUE (parentDirId,config),
	FOREIGN KEY(parentDirId) REFERENCES Directories(dirId)
);


--
-- TABLE 'Streams'
--
CREATE TABLE Streams
(
	streamId	NUMBER,
	configId	NUMBER		NOT NULL,
	streamLabel	VARCHAR2(128)	NOT NULL,
	UNIQUE (streamId,configId),
	UNIQUE (configId,streamLabel),
	PRIMARY KEY(streamId),
	FOREIGN KEY(configId) REFERENCES Configurations(configId)
);


-- INDEX 'StreamsConfigId_idx'
CREATE INDEX StreamsConfigId_idx ON Streams(configId);

-- SEQUENCE 'StreamId_Sequence'
CREATE SEQUENCE StreamId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'StreamId_Trigger'
CREATE OR REPLACE TRIGGER StreamId_Trigger
BEFORE INSERT ON Streams
FOR EACH ROW
BEGIN
SELECT StreamId_Sequence.nextval INTO :NEW.streamId FROM dual;
END;
/


--
-- TABLE 'SuperIds'
--
CREATE TABLE SuperIds
(
	superId    	NUMBER,
	PRIMARY KEY(superId)
);

-- SEQUENCE 'SuperId_Sequence'
CREATE SEQUENCE SuperId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'SuperId_Trigger'
CREATE OR REPLACE TRIGGER SuperId_Trigger
BEFORE INSERT ON SuperIds
FOR EACH ROW
BEGIN
SELECT SuperId_Sequence.nextval INTO :NEW.superId FROM dual;
END;
/


--
-- TABLE 'SuperIdReleaseAssoc'
--
CREATE TABLE SuperIdReleaseAssoc
(
	superId    	NUMBER          NOT NULL,
	releaseId   	NUMBER 		NOT NULL,
	PRIMARY KEY(superId,releaseId),
	FOREIGN KEY(superId)   REFERENCES SuperIds(superId),
	FOREIGN KEY(releaseId) REFERENCES SoftwareReleases(releaseId)
);

-- INDEX SuperIdRelAssocSuperId_idx
CREATE INDEX SuperIdRelAssocSuperId_idx ON SuperIdReleaseAssoc(superId);

-- INDEX SuperIdRelAssocReleaseId_idx
CREATE INDEX SuperIdRelAssocReleaseId_idx ON SuperIdReleaseAssoc(releaseId);


--
-- TABLE 'Paths'
--
CREATE TABLE Paths
(
	pathId     	NUMBER,
	name       	VARCHAR2(128)    NOT NULL,
	isEndPath       NUMBER(1)       DEFAULT '0' NOT NULL,
	PRIMARY KEY(pathId)
);

-- SEQUENCE 'PathId_Sequence'
CREATE SEQUENCE PathId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'PathId_Trigger'
CREATE OR REPLACE TRIGGER PathId_Trigger
BEFORE INSERT ON Paths
FOR EACH ROW
BEGIN
SELECT PathId_Sequence.nextval INTO :NEW.pathId FROM dual;
END;
/


--
-- TABLE 'ConfigurationPathAssoc'
--
CREATE TABLE ConfigurationPathAssoc
(
	configId	NUMBER		NOT NULL,
	pathId		NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(configId,sequenceNb),
	PRIMARY KEY(configId,pathId),
	FOREIGN KEY(configId) REFERENCES Configurations(configId),
	FOREIGN KEY(pathId)   REFERENCES Paths(pathId)
);

-- INDEX ConfigPathAssocPathId_idx
CREATE INDEX ConfigPathAssocPathId_idx ON ConfigurationPathAssoc(pathId);


--
-- TABLE 'PathInPathAssoc'
--
CREATE TABLE PathInPathAssoc
(
	parentPathId	NUMBER		NOT NULL,
	childPathId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	UNIQUE(parentPathId,sequenceNb),
	PRIMARY KEY(parentPathId,childPathId),
	FOREIGN KEY(parentPathId) REFERENCES Paths(pathId),
	FOREIGN KEY(childPathId)  REFERENCES Paths(pathId)
);

-- INDEX PathPathAssocChildId_idx
CREATE INDEX PathPathAssocChildId_idx ON PathInPathAssoc(childPathId);


--
-- TABLE 'StreamPathAssoc'
--
CREATE TABLE StreamPathAssoc
(
	streamId	NUMBER	 	NOT NULL,
	pathId		NUMBER    	NOT NULL,
	PRIMARY KEY(streamId,pathId),
	FOREIGN KEY(streamId) REFERENCES Streams(streamId),
	FOREIGN KEY(pathId)   REFERENCES Paths(pathId)
);

-- INDEX StreamPathAssocPathId_idx
CREATE INDEX StreamPathAssocPathId_idx ON StreamPathAssoc(pathId);


--
-- TABLE 'Sequences'
--
CREATE TABLE Sequences
(
	sequenceId	NUMBER		NOT NULL,
	name		VARCHAR2(128)	NOT NULL,
	PRIMARY KEY(sequenceId)
);

-- SEQUENCE 'SequenceId_Sequence'
CREATE SEQUENCE SequenceId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'SequenceId_Trigger'
CREATE OR REPLACE TRIGGER SequenceId_Trigger
BEFORE INSERT ON Sequences
FOR EACH ROW
BEGIN
SELECT SequenceId_Sequence.nextval INTO :NEW.sequenceId FROM dual;
END;
/


--
-- TABLE 'ConfigurationSequenceAssoc'
--
CREATE TABLE ConfigurationSequenceAssoc
(
	configId	NUMBER		NOT NULL,
	sequenceId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(configId,sequenceNb),
	PRIMARY KEY(configId,sequenceId),
	FOREIGN KEY(configId)   REFERENCES Configurations(configId),
	FOREIGN KEY(sequenceId) REFERENCES Sequences(sequenceId)
);

-- INDEX ConfigSeqAssocSequenceId_idx
CREATE INDEX ConfigSeqAssocSequenceId_idx ON ConfigurationSequenceAssoc(sequenceId);


--
-- TABLE 'PathSequenceAssoc'
--
CREATE TABLE PathSequenceAssoc
(
	pathId		NUMBER		NOT NULL,
	sequenceId	NUMBER   	NOT NULL,
	sequenceNb      NUMBER(3) 	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	UNIQUE(pathId,sequenceNb),
	PRIMARY KEY(pathId,sequenceId),
	FOREIGN KEY(pathId)     REFERENCES Paths(pathId),
	FOREIGN KEY(sequenceId) REFERENCES Sequences(sequenceId)
);

-- INDEX PathSeqAssocSequenceId_idx
CREATE INDEX PathSeqAssocSequenceId_idx ON PathSequenceAssoc(sequenceId);


--
-- TABLE 'SequenceInSequenceAssoc'
--
CREATE TABLE SequenceInSequenceAssoc
(
	parentSequenceId NUMBER		NOT NULL,
	childSequenceId	 NUMBER		NOT NULL,
	sequenceNb	 NUMBER(3)	NOT NULL,
	operator	 NUMBER(3)	DEFAULT '0' NOT NULL,
	UNIQUE(parentSequenceId,SequenceNb),
	PRIMARY KEY(parentSequenceId,childSequenceId),
	FOREIGN KEY(parentSequenceId) REFERENCES Sequences(sequenceId),
	FOREIGN KEY(childSequenceId)  REFERENCES Sequences(sequenceId)
);

-- INDEX SeqSeqAssocChildId_idx
CREATE INDEX SeqSeqAssocChildId_idx ON SequenceInSequenceAssoc(childSequenceId);


--
--
-- SERVICES
--
--

--
-- TABLE 'ServiceTemplates'
--
CREATE TABLE ServiceTemplates
(
	superId  	NUMBER,
	name       	VARCHAR2(128)	NOT NULL,
	cvstag       	VARCHAR2(32)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId)
);


--
-- TABLE 'Services'
--
CREATE TABLE Services
(
	superId      	NUMBER,
	templateId     	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES ServiceTemplates(superId)
);

-- INDEX ServicesTemplateId_idx
CREATE INDEX ServicesTemplateId_idx ON Services(templateId);


--
-- TABLE 'ConfigurationServiceAssoc'
--
CREATE TABLE ConfigurationServiceAssoc
(
	configId	NUMBER		NOT NULL,
	serviceId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(configId,sequenceNb),
	PRIMARY KEY(configId,serviceId),
	FOREIGN KEY(configId)  REFERENCES Configurations(configId),
	FOREIGN KEY(serviceId) REFERENCES Services(superId)
);
	
-- INDEX ConfigSvcAssocServiceId_idx
CREATE INDEX ConfigSvcAssocServiceId_idx ON ConfigurationServiceAssoc(serviceId);


--
--
-- EDSources
--
--

--
-- TABLE 'EDSourceTemplates'
--
CREATE TABLE EDSourceTemplates
(
	superId  	NUMBER,
	name       	VARCHAR2(128)	NOT NULL,
	cvstag       	VARCHAR2(32)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId)
);


--
-- TABLE 'EDSources'
--
CREATE TABLE EDSources
(
	superId      	NUMBER,
	templateId     	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES EDSourceTemplates(superId)
);

-- INDEX EDSourcesTemplateId_idx
CREATE INDEX EDSourcesTemplateId_idx ON EDSources(templateId);


--
-- TABLE 'ConfigurationEDSourceAssoc'
--
CREATE TABLE ConfigurationEDSourceAssoc
(
	configId	NUMBER		NOT NULL,
	edsourceId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(configId,sequenceNb),
	PRIMARY KEY(configId,edsourceId),
	FOREIGN KEY(configId)   REFERENCES Configurations(configId),
	FOREIGN KEY(edsourceId) REFERENCES EDSources(superId)
);

-- INDEX ConfigEDSrcAssocEDSourceId_idx
CREATE INDEX ConfigEDSrcAssocEDSourceId_idx ON ConfigurationEDSourceAssoc(edsourceId);


--
--
-- ESSources
--
--

--
-- TABLE 'ESSourceTemplates'
--
CREATE TABLE ESSourceTemplates
(
	superId  	NUMBER,
	name       	VARCHAR2(128)	NOT NULL,
	cvstag       	VARCHAR2(32)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId)
);


--
-- TABLE 'ESSources'
--
CREATE TABLE ESSources
(
	superId      	NUMBER,
	templateId     	NUMBER		NOT NULL,
	name       	VARCHAR2(128)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES ESSourceTemplates(superId)
);

-- INDEX ESSourcesTemplateId_idx
CREATE INDEX ESSourcesTemplateId_idx ON ESSources(templateId);


--
-- TABLE 'ConfigurationESSourceAssoc'
--
CREATE TABLE ConfigurationESSourceAssoc
(
	configId	NUMBER		NOT NULL,
	essourceId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(configId,sequenceNb),
	PRIMARY KEY(configId,essourceId),
	FOREIGN KEY(configId)   REFERENCES Configurations(configId),
	FOREIGN KEY(essourceId) REFERENCES ESSources(superId)
);
	
-- INDEX ConfigESSrcAssocESSourceId_idx
CREATE INDEX ConfigESSrcAssocESSourceId_idx ON ConfigurationESSourceAssoc(essourceId);


--
--
-- ESModules
--
--

--
-- TABLE 'ESModuleTemplates'
--
CREATE TABLE ESModuleTemplates
(
	superId  	NUMBER,
	name       	VARCHAR2(128)	NOT NULL,
	cvstag       	VARCHAR2(32)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId)
);


--
-- TABLE 'ESModules'
--
CREATE TABLE ESModules
(
	superId      	NUMBER,
	templateId     	NUMBER		NOT NULL,
	name       	VARCHAR2(128)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES ESModuleTemplates(superId)
);

-- INDEX ESModulesTemplateId_idx
CREATE INDEX ESModulesTemplateId_idx ON ESModules(templateId);


--
-- TABLE 'ConfigurationESModuleAssoc'
--
CREATE TABLE ConfigurationESModuleAssoc
(
	configId	NUMBER		NOT NULL,
	esmoduleId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(configId,sequenceNb),
	PRIMARY KEY(configId,esmoduleId),
	FOREIGN KEY(configId)   REFERENCES Configurations(configId),
	FOREIGN KEY(esmoduleId) REFERENCES ESModules(superId)
);

-- INDEX ConfigESModAssocEDModuleId_idx
CREATE INDEX ConfigESModAssocESModuleId_idx ON ConfigurationESModuleAssoc(esmoduleId);


--
--
-- MODULES
--
--

--
-- TABLE 'ModuleTypes'
--
CREATE TABLE ModuleTypes
(
	typeId 		NUMBER,
	type   		VARCHAR2(32)	NOT NULL UNIQUE,
	PRIMARY KEY(typeId)
);


--
-- TABLE 'ModuleTemplates'
--
CREATE TABLE ModuleTemplates
(
	superId  	NUMBER,
	typeId  	NUMBER		NOT NULL,
	name       	VARCHAR2(128)	NOT NULL,
	cvstag       	VARCHAR2(32)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId),
	FOREIGN KEY(typeId)  REFERENCES ModuleTypes(typeId)
);

-- INDEX ModuleTemplatesTypeId_idx
CREATE INDEX ModuleTemplatesTypeId_idx ON ModuleTemplates(typeId);


--
-- TABLE 'Modules'
--
CREATE TABLE Modules
(
	superId   	NUMBER,
	templateId  	NUMBER		NOT NULL,
	name       	VARCHAR2(128)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES ModuleTemplates(superId)
);

-- INDEX ModulesTemplateId_idx
CREATE INDEX ModulesTemplateId_idx ON Modules(templateId);


--
-- TABLE 'PathModuleAssoc'
--
CREATE TABLE PathModuleAssoc
(
	pathId     	NUMBER		NOT NULL,
        moduleId   	NUMBER		NOT NULL,
	sequenceNb	NUMBER(4)	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	UNIQUE(pathId,sequenceNb),
	PRIMARY KEY(pathId,moduleId),
	FOREIGN KEY(pathId)   REFERENCES Paths(pathId),
	FOREIGN KEY(moduleId) REFERENCES Modules(superId)
);

-- INDEX PathModAssocModuleId_idx
CREATE INDEX PathModAssocModuleId_idx ON PathModuleAssoc(moduleId);


--
-- TABLE 'SequenceModuleAssoc'
--
CREATE TABLE SequenceModuleAssoc
(
	sequenceId     	NUMBER		NOT NULL,
        moduleId   	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	UNIQUE(sequenceId,sequenceNb),
	PRIMARY KEY(sequenceId,moduleId),
	FOREIGN KEY(sequenceId) REFERENCES Sequences(sequenceId),
	FOREIGN KEY(moduleId)   REFERENCES Modules(superId)
);
	
-- INDEX SeqModAssocModuleId_idx
CREATE INDEX SeqModAssocModuleId_idx ON SequenceModuleAssoc(moduleId);


--
--
-- PARAMETER SETS
--
--

--
-- TABLE 'ParameterSets'
--
CREATE TABLE ParameterSets
(
	superId		NUMBER,
	name		VARCHAR2(128),
	tracked         NUMBER(1)       NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId)
);


--
-- TABLE 'VecParameterSets'
--
CREATE TABLE VecParameterSets
(
	superId		NUMBER,
	name		VARCHAR2(128),
	tracked         NUMBER(1)       NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId)
);


--
-- TABLE 'ConfigurationParamSetAssoc'
--
CREATE TABLE ConfigurationParamSetAssoc
(
	configId        NUMBER		NOT NULL,
	psetId		NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(configId,sequenceNb),
	PRIMARY KEY(configId,psetId),
	FOREIGN KEY(configId) REFERENCES Configurations(configId),
	FOREIGN KEY(psetId)   REFERENCES ParameterSets(superId)
);

-- INDEX ConfigPSetAssocPsetId_idx
CREATE INDEX ConfigPSetAssocPsetId_idx ON ConfigurationParamSetAssoc(psetId);


--
-- TABLE 'SuperIdParamSetAssoc'
--
CREATE TABLE SuperIdParamSetAssoc
(
	superId		NUMBER		NOT NULL,
	psetId		NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(superId,sequenceNb),
	PRIMARY KEY(superId,psetId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId),
	FOREIGN KEY(psetId)  REFERENCES ParameterSets(superId)
);

-- INDEX SuperIdPSetAssocPSetId_idx
CREATE INDEX SuperIdPSetAssocPSetId_idx ON SuperIdParamSetAssoc(psetId);


--
-- TABLE 'SuperIdVecParamSetAssoc'
--
CREATE TABLE SuperIdVecParamSetAssoc
(
	superId		NUMBER		NOT NULL,
	vpsetId		NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(superId,sequenceNb),
	PRIMARY KEY(superId,vpsetId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId),
	FOREIGN KEY(vpsetId) REFERENCES VecParameterSets(superId)
);

-- INDEX SuperIdVPSetAssocVPSetId_idx
CREATE INDEX SuperIdVPSetAssocVPSetId_idx ON SuperIdVecParamSetAssoc(vpsetId);


--
--
-- PARAMETERS
--
--

-- TABLE ParameterTypes
CREATE TABLE ParameterTypes
(
	paramTypeId	NUMBER,
	paramType       VARCHAR2(32)	NOT NULL UNIQUE,
	PRIMARY KEY(paramTypeId)
);


--
-- TABLE Parameters
--
CREATE TABLE Parameters
(
	paramId    	NUMBER,
	paramTypeId    	NUMBER		NOT NULL,
	name       	VARCHAR2(128)	NOT NULL,
	tracked         NUMBER(1)       NOT NULL,
	PRIMARY KEY(paramId),
	FOREIGN KEY(paramTypeId) REFERENCES ParameterTypes(paramTypeId)
);

-- INDEX ParametersTypeId_idx
CREATE INDEX ParametersTypeId_idx ON Parameters(paramTypeId);

-- SEQUENCE 'ParamId_Sequence'
CREATE SEQUENCE ParamId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'ParamId_Trigger'
CREATE OR REPLACE TRIGGER ParamId_Trigger
BEFORE INSERT ON Parameters
FOR EACH ROW
BEGIN
SELECT ParamId_Sequence.nextval INTO :NEW.paramId FROM dual;
END;
/


--
-- TABLE 'SuperIdParameterAssoc'
--
CREATE TABLE SuperIdParameterAssoc
(
	superId		NUMBER		NOT NULL,
	paramId		NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	UNIQUE(superId,sequenceNb),
	PRIMARY KEY(superId,paramId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX SuperIdParamAssocParamId_idx
CREATE INDEX SuperIdParamAssocParamId_idx ON SuperIdParameterAssoc(paramId);


--
-- TABLE 'Int32ParamValues'
--
CREATE TABLE Int32ParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER		NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX Int32ValuesParamId_idx
CREATE INDEX Int32ParamValuesParamId_idx ON Int32ParamValues(paramId);


--
-- TABLE 'VInt32ParamValues'
--
CREATE TABLE VInt32ParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	NUMBER		NOT NULL,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX VInt32ValuesParamId_idx
CREATE INDEX VInt32ValuesParamId_idx ON VInt32ParamValues(paramId);


--
-- TABLE 'UInt32ParamValues'
--
CREATE TABLE UInt32ParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER		NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX UInt32ValuesParamId_idx
CREATE INDEX UInt32ValuesParamId_idx ON UInt32ParamValues(paramId);


--
-- TABLE 'VUInt32ParamValues'
--
CREATE TABLE VUInt32ParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	NUMBER		NOT NULL,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX VUInt32ValuesParamId_idx
CREATE INDEX VUInt32ValuesParamId_idx ON VUInt32ParamValues(paramId);


--
-- TABLE 'BoolParamValues'
--
CREATE TABLE BoolParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER(1)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX BoolValuesParamId_idx
CREATE INDEX BoolValuesParamId_idx ON BoolParamValues(paramId);


--
-- TABLE 'DoubleParamValues'
--
CREATE TABLE DoubleParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	FLOAT		NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX DoubleValuesParamId_idx
CREATE INDEX DoubleValuesParamId_idx ON DoubleParamValues(paramId);


--
-- TABLE 'VDoubleParamValues'
--
CREATE TABLE VDoubleParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	FLOAT 		NOT NULL,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX VDoubleValuesParamId_idx
CREATE INDEX VDoubleValuesParamId_idx ON VDoubleParamValues(paramId);


--
-- TABLE 'StringParamValues'
--
CREATE TABLE StringParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(512),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX StringValuesParamId_idx
CREATE INDEX StringValuesParamId_idx ON StringParamValues(paramId);


--
-- TABLE 'VStringParamValues'
--
CREATE TABLE VStringParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	VARCHAR2(512),
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX VStringValuesParamId_idx
CREATE INDEX VStringValuesParamId_idx ON VStringParamValues(paramId);


--
-- TABLE 'InputTagParamValues'
--
CREATE TABLE InputTagParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(128)   NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX InputTagValuesParamId_idx
CREATE INDEX InputTagValuesParamId_idx ON InputTagParamValues(paramId);


--
-- TABLE 'VInputTagParamValues'
--
CREATE TABLE VInputTagParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	VARCHAR2(128)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX VInputTagValuesParamId_idx
CREATE INDEX VInputTagValuesParamId_idx ON VInputTagParamValues(paramId);


--
-- TABLE 'EventIDParamValues'
--
CREATE TABLE EventIDParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(32)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX EventIDValuesParamId_idx
CREATE INDEX EventIDValuesParamId_idx ON EventIDParamValues(paramId);


--
-- TABLE 'VEventIDParamValues'
--
CREATE TABLE VEventIDParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	VARCHAR2(32)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX VEventIDValuesParamId_idx
CREATE INDEX VEventIDValuesParamId_idx ON VEventIDParamValues(paramId);


--
-- TABLE 'FileInPathParamValues'
--
CREATE TABLE FileInPathParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(512)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);

-- INDEX EventIDValuesParamId_idx
CREATE INDEX FileInPathValuesParamId_idx ON FileInPathParamValues(paramId);


COMMIT;


--
--
-- INSERT Module and Parameter Types
--
--

--
-- INSERT root directory
--
INSERT INTO Directories (parentDirId,dirName,created) VALUES(null,'/',SYSDATE);


--
-- INSERT ModuleTypes
--

INSERT INTO ModuleTypes VALUES (1,'EDProducer');
INSERT INTO ModuleTypes VALUES (2,'EDAnalyzer');
INSERT INTO ModuleTypes VALUES (3,'EDFilter');
INSERT INTO ModuleTypes VALUES (4,'HLTProducer');
INSERT INTO ModuleTypes VALUES (5,'HLTFilter');
INSERT INTO ModuleTypes VALUES (6,'OutputModule');


--
-- INSERT Parameter Types
--
INSERT INTO ParameterTypes VALUES ( 1,'bool');
INSERT INTO ParameterTypes VALUES ( 2,'int32');
INSERT INTO ParameterTypes VALUES ( 3,'vint32');
INSERT INTO ParameterTypes VALUES ( 4,'uint32');
INSERT INTO ParameterTypes VALUES ( 5,'vuint32');
INSERT INTO ParameterTypes VALUES ( 6,'double');
INSERT INTO ParameterTypes VALUES ( 7,'vdouble');
INSERT INTO ParameterTypes VALUES ( 8,'string');
INSERT INTO ParameterTypes VALUES ( 9,'vstring');
INSERT INTO ParameterTypes VALUES (10,'InputTag');
INSERT INTO ParameterTypes VALUES (11,'VInputTag');
INSERT INTO ParameterTypes VALUES (12,'EventID');
INSERT INTO ParameterTypes VALUES (13,'VEventID');
INSERT INTO ParameterTypes VALUES (14,'FileInPath');


COMMIT;
