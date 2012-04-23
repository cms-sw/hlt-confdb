--
-- CMS High Level Trigger Configuration Database Schema: ORACLE
-- ------------------------------------------------------------
--
-- CREATED:
-- 01/12/2007 Philipp Schieferdecker <philipp.schieferdecker@cern.ch>
-- 
-- Modified :
-- 24/01/2012 	- Includes documentation fields for Paths table
--				- Change to 4k char for INPUTTAGPARAMVALUES, VINPUTTAGPARAMVALUES, 
				  VSTRINGPARAMVALUES, STRINGPARAMVALUES.
-- 
-- 01/05/2009 ConfDBV1 Schema

--
-- TABLE 'SoftwareReleases'
--
CREATE TABLE SoftwareReleases
(
	releaseId 	NUMBER,
	releaseTag 	VARCHAR2(32)	NOT NULL UNIQUE,
	CONSTRAINT pk_softwareRelease PRIMARY KEY(releaseId)
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
-- TABLE 'SoftwareSubsystems'
--
CREATE TABLE SoftwareSubsystems
(
	subsysId	NUMBER,
	name	 	VARCHAR2(64)	NOT NULL,
	CONSTRAINT pk_softwareSubsystem PRIMARY KEY(subsysId)
);

-- SEQUENCE 'SubsysId_Sequence'
CREATE SEQUENCE SubsysId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'SubsysId_Trigger'
CREATE OR REPLACE TRIGGER SubsysId_Trigger
BEFORE INSERT ON SoftwareSubsystems
FOR EACH ROW
BEGIN
SELECT SubsysId_Sequence.nextval INTO :NEW.subsysId FROM dual;
END;
/


--
-- TABLE 'SoftwarePackages'
--
CREATE TABLE SoftwarePackages
(
	packageId 	NUMBER,
	subsysId	NUMBER 		NOT NULL,
	name	 	VARCHAR2(64)	NOT NULL,
	CONSTRAINT pk_softwarePackages PRIMARY KEY(packageId),
	CONSTRAINT fk_softwarePackages FOREIGN KEY(subsysId) REFERENCES SoftwareSubsystems(subsysId)
);

-- SEQUENCE 'PackageId_Sequence'
CREATE SEQUENCE PackageId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'PackageId_Trigger'
CREATE OR REPLACE TRIGGER PackageId_Trigger
BEFORE INSERT ON SoftwarePackages
FOR EACH ROW
BEGIN
SELECT PackageId_Sequence.nextval INTO :NEW.packageId FROM dual;
END;
/

-- INDEX PackageSubsystemId_idx
CREATE INDEX PackageSubsystemId_idx ON SoftwarePackages(subsysId);



--
-- TABLE 'Directories'
--
CREATE TABLE Directories
(
	dirId		NUMBER,
	parentDirId	NUMBER,
	dirName		VARCHAR2(512)	NOT NULL UNIQUE,
	created		TIMESTAMP	NOT NULL,
	CONSTRAINT pk_directories PRIMARY KEY(dirId),
	CONSTRAINT fk_directories FOREIGN KEY(parentDirId) REFERENCES Directories(dirId)
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
	description     VARCHAR2(1024)  DEFAULT NULL,
	CONSTRAINT uk_Configurations UNIQUE (parentDirId,config,version),
	CONSTRAINT pk_Configurations PRIMARY KEY(configId),
	CONSTRAINT fk_Configurations_sr FOREIGN KEY(releaseId)   REFERENCES SoftwareReleases(releaseId),
	CONSTRAINT fk_Configurations_re FOREIGN KEY(parentDirId) REFERENCES Directories(dirId)
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
	CONSTRAINT uk_lockedConfigurations UNIQUE (parentDirId,config),
	CONSTRAINT fk_lockedConfigurations FOREIGN KEY(parentDirId) REFERENCES Directories(dirId)
);


--
-- new tables to manage streams, output modules, and primary datasets
--

--
-- TABLE 'EventContents'
--
CREATE TABLE EventContents
(
	eventContentId	NUMBER,
  name       	VARCHAR2(128)    NOT NULL,
  CONSTRAINT pk_eventContents PRIMARY KEY(eventContentId)
);

-- SEQUENCE 'ReleaseId_Sequence'
CREATE SEQUENCE EventContentId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'ReleaseId_Trigger'
CREATE OR REPLACE TRIGGER EventContenId_Trigger
BEFORE INSERT ON EventContents
FOR EACH ROW
BEGIN
SELECT EventContentId_Sequence.nextval INTO :NEW.eventContentId FROM dual;
END;
/

--
-- TABLE 'ConfigurationContentAssoc'
--
CREATE TABLE ConfigurationContentAssoc
(
	configId	NUMBER		NOT NULL, 
	eventContentId		NUMBER		NOT NULL,
	CONSTRAINT pk_configContentAssoc PRIMARY KEY(configId,eventContentId),
	CONSTRAINT fk_configContentAssoc_ci FOREIGN KEY(configId) REFERENCES Configurations(configId),
	CONSTRAINT fk_configContentAssoc_ei FOREIGN KEY(eventContentId)   REFERENCES EventContents(eventContentId)
);

--
-- TABLE 'SuperIds'
--
CREATE TABLE SuperIds
(
	superId    	NUMBER,
	CONSTRAINT pk_superIds PRIMARY KEY(superId)
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
	CONSTRAINT pk_superIdReleaseAssoc PRIMARY KEY(superId,releaseId),
	CONSTRAINT fk_superIdReleaseAssoc_si FOREIGN KEY(superId)   REFERENCES SuperIds(superId),
	CONSTRAINT fk_superIdReleaseAssoc_sr FOREIGN KEY(releaseId) REFERENCES SoftwareReleases(releaseId)
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
	name       	VARCHAR2(128)  NOT NULL,
	isEndPath   NUMBER(1)  DEFAULT '0' NOT NULL,
	description	CLOB,
  	contact		CLOB,
	CONSTRAINT pk_paths PRIMARY KEY(pathId)
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
	CONSTRAINT uk_ConfigurationPathAssoc UNIQUE(configId,sequenceNb),
	CONSTRAINT pk_ConfigurationPathAssoc PRIMARY KEY(configId,pathId),
	CONSTRAINT fk_ConfigurationPathAssoc_re FOREIGN KEY(configId) REFERENCES Configurations(configId),
	CONSTRAINT fk_ConfigurationPathAssoc_pa FOREIGN KEY(pathId)   REFERENCES Paths(pathId)
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
	CONSTRAINT uk_PathInPathAssoc UNIQUE(parentPathId,sequenceNb),
	CONSTRAINT pk_PathInPathAssoc PRIMARY KEY(parentPathId,childPathId),
	CONSTRAINT fk_PathInPathAssoc_pp FOREIGN KEY(parentPathId) REFERENCES Paths(pathId),
	CONSTRAINT fk_PathInPathAssoc_pc FOREIGN KEY(childPathId)  REFERENCES Paths(pathId)
);

-- INDEX PathPathAssocChildId_idx
CREATE INDEX PathPathAssocChildId_idx ON PathInPathAssoc(childPathId);

--
-- TABLE 'Streams'
--
CREATE TABLE Streams
(
	streamId	NUMBER,
	streamLabel	VARCHAR2(128)	NOT NULL,
  fracToDisk  FLOAT  DEFAULT '1' NOT NULL,
	CONSTRAINT pk_streams PRIMARY KEY(streamId),
  CONSTRAINT fk_streams FOREIGN KEY(streamId) REFERENCES SuperIds(superId) 
);


--
-- TABLE 'EventContentStreamAssoc'
--
CREATE TABLE ECStreamAssoc
(
	eventContentId	NUMBER		NOT NULL,
	streamId		NUMBER		NOT NULL,
	CONSTRAINT pk_ECStreamsAssoc PRIMARY KEY(eventContentId,streamId),
	CONSTRAINT fk_ECStreamsAssoc_ei FOREIGN KEY(eventContentId) REFERENCES EventContents(eventContentId),
	CONSTRAINT fk_ECStreamsAssoc_si  FOREIGN KEY(streamId)   REFERENCES Streams(streamId)
);

-- INDEX EventContentStreamAssocStreamId_idx
CREATE INDEX ECStreamAssocStreamId_idx ON ECStreamAssoc(streamId);


--
-- TABLE 'PrimaryDatasets'
--
CREATE TABLE PrimaryDatasets
(
	datasetId	NUMBER,
	datasetLabel	VARCHAR2(128)	NOT NULL,
	CONSTRAINT pk_primaryDatasets PRIMARY KEY(datasetId)
);

-- SEQUENCE 'PrimaryDatasetId_Sequence'
CREATE SEQUENCE DatasetId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'PrimaryDatasetId_Trigger'
CREATE OR REPLACE TRIGGER DatasetId_Trigger
BEFORE INSERT ON PrimaryDatasets
FOR EACH ROW
BEGIN
SELECT DatasetId_Sequence.nextval INTO :NEW.datasetId FROM dual;
END;
/


CREATE TABLE StreamDatasetAssoc
(
	datasetId	NUMBER		NOT NULL,
	streamId		NUMBER		NOT NULL,
	CONSTRAINT pk_streamDatasetAssoc PRIMARY KEY(datasetId,streamId),
	CONSTRAINT fk_streamDatasetAssoc_di FOREIGN KEY(datasetId) REFERENCES PrimaryDataSets(datasetId),
	CONSTRAINT fk_streamDatasetAssoc_si FOREIGN KEY(streamId)   REFERENCES Streams(streamId)
);

-- INDEX StreamOutputModuleAssocPathId_idx
CREATE INDEX StreamDatsetAssocStreamId_idx ON StreamDatasetAssoc(streamId);


--
-- TABLE 'PathStreamDataSetAssoc'
--
CREATE TABLE PathStreamDatasetAssoc
(
  --configId Number Not Null,
	pathId	NUMBER	NOT NULL,
	streamId	NUMBER  NOT NULL,
	datasetId  NUMBER	NOT NULL,
  CONSTRAINT pk_pathStreamDatasetAssoc_PSD PRIMARY KEY (pathId, streamId, datasetId),
	CONSTRAINT pk_pathStreamDatasetAssoc_pi FOREIGN KEY(pathId) REFERENCES Paths(pathId),
	CONSTRAINT pk_pathStreamDatasetAssoc_si FOREIGN KEY(streamId) REFERENCES Streams(streamId),
	CONSTRAINT pk_pathStreamDatasetAssoc_di FOREIGN KEY(datasetId) REFERENCES PrimaryDatasets(datasetId)
);

-- INDEX PathStreamAssocStreamId_idx
CREATE INDEX PathStreamDSAssocStreamId_idx ON PathStreamDatasetAssoc(streamId);

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
	CONSTRAINT uk_configSequenceAssoc UNIQUE(configId,sequenceNb),
	CONSTRAINT pk_configSequenceAssoc PRIMARY KEY(configId,sequenceId),
	CONSTRAINT fk_configSequenceAssoc_ci FOREIGN KEY(configId)   REFERENCES Configurations(configId),
	CONSTRAINT fk_configSequenceAssoc_si FOREIGN KEY(sequenceId) REFERENCES Sequences(sequenceId)
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
	UNIQUE(parentSequenceId,sequenceNb),
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
	packageId	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)   REFERENCES SuperIds(superId) ON DELETE CASCADE,
	FOREIGN KEY(packageId) REFERENCES SoftwarePackages(packageId)
);


--
-- TABLE 'Services'
--
CREATE TABLE Services
(
	superId      	NUMBER,
	templateId     	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId) ON DELETE CASCADE,
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
	packageId	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)   REFERENCES SuperIds(superId) ON DELETE CASCADE,
	FOREIGN KEY(packageId) REFERENCES SoftwarePackages(packageId)
);


--
-- TABLE 'EDSources'
--
CREATE TABLE EDSources
(
	superId      	NUMBER,
	templateId     	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId) ON DELETE CASCADE,
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
	packageId	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)   REFERENCES SuperIds(superId) ON DELETE CASCADE,
	FOREIGN KEY(packageId) REFERENCES SoftwarePackages(packageId)
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
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId) ON DELETE CASCADE,
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
	prefer		NUMBER(1)	DEFAULT '0' NOT NULL,
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
	packageId	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)   REFERENCES SuperIds(superId) ON DELETE CASCADE,
	FOREIGN KEY(packageId) REFERENCES SoftwarePackages(packageId)
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
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId) ON DELETE CASCADE,
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
	prefer		NUMBER(1)	DEFAULT '0' NOT NULL,
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
	packageId	NUMBER		NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)   REFERENCES SuperIds(superId) ON DELETE CASCADE,
	FOREIGN KEY(typeId)    REFERENCES ModuleTypes(typeId),
	FOREIGN KEY(packageId) REFERENCES SoftwarePackages(packageId)
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
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId) ON DELETE CASCADE,
	FOREIGN KEY(templateId) REFERENCES ModuleTemplates(superId)
);

-- INDEX ModulesTemplateId_idx
CREATE INDEX ModulesTemplateId_idx ON Modules(templateId);


--
-- TABLE 'PathModuleAssoc'
--
CREATE TABLE PathOutputModAssoc
(
	pathId     	NUMBER		NOT NULL,
  outputModuleId   	NUMBER		NOT NULL,
	sequenceNb	NUMBER(4)	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	UNIQUE(pathId,sequenceNb),
	PRIMARY KEY(pathId,outputModuleId),
	FOREIGN KEY(pathId)   REFERENCES Paths(pathId),
	FOREIGN KEY(outputModuleId) REFERENCES Streams(streamId)
);

-- INDEX PathOutModAssocModuleId_idx
CREATE INDEX PathOutModAssocModId_idx ON PathOutputModAssoc(outputModuleId);

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
-- TABLE 'SequenceModuleAssoc'
--
CREATE TABLE SequenceOutputModAssoc
(
	sequenceId     	NUMBER		NOT NULL,
  outputModuleId   	NUMBER		NOT NULL,
	sequenceNb	NUMBER(4)	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	UNIQUE(sequenceId,sequenceNb),
	PRIMARY KEY(sequenceId,outputModuleId),
	FOREIGN KEY(sequenceId)   REFERENCES Sequences(sequenceId),
	FOREIGN KEY(outputModuleId) REFERENCES Streams(streamId)
);

-- INDEX SequenceOutModAssocModuleId_idx
CREATE INDEX SequenceOutModAssocModId_idx ON SequenceOutputModAssoc(outputModuleId);


--
-- TABLE 'EventContentStatements'
--
CREATE TABLE EventContentStatements
(
  statementId Number,
  classN  VARCHAR2(256) DEFAULT '*',
  moduleL  VARCHAR2(256) DEFAULT '*',
  extraN  VARCHAR2(256) DEFAULT '*',
  processN  VARCHAR2(256) DEFAULT '*',
  statementType  NUMBER(1) DEFAULT '1' NOT NULL,
  PRIMARY KEY(statementId)
);


-- SEQUENCE 'StatementId_Sequence'
CREATE SEQUENCE StatementId_Sequence START WITH 1 INCREMENT BY 1;

-- TRIGGER 'StatementId_Trigger'
CREATE OR REPLACE TRIGGER StatementId_Trigger
BEFORE INSERT ON EventContentStatements
FOR EACH ROW
BEGIN
SELECT StatementId_Sequence.nextval INTO :NEW.statementId FROM dual;
END;
/

--
-- TABLE 'StreamECStatementAssoc'
--

CREATE TABLE ECStatementAssoc
(
  statementRank  Number,
  statementId  NUMBER    NOT NULL,
  eventContentId  NUMBER    NOT NULL,
  pathId  NUMBER, 
  UNIQUE(statementId,eventContentId),
  FOREIGN KEY(statementId) REFERENCES EventContentStatements(statementId),
  FOREIGN KEY(eventContentId)   REFERENCES EventContents(eventContentId)
);

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
	FOREIGN KEY(superId) REFERENCES SuperIds(superId) ON DELETE CASCADE
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
	FOREIGN KEY(superId) REFERENCES SuperIds(superId) ON DELETE CASCADE
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
	hex		NUMBER(1)       DEFAULT '0' NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	hex		NUMBER(1)       DEFAULT '0' NOT NULL,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	hex		NUMBER(1)       DEFAULT '0' NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	hex		NUMBER(1)       DEFAULT '0' NOT NULL,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
);

-- INDEX VUInt32ValuesParamId_idx
CREATE INDEX VUInt32ValuesParamId_idx ON VUInt32ParamValues(paramId);


--
-- TABLE 'Int64ParamValues'
--
CREATE TABLE Int64ParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER		NOT NULL,
	hex		NUMBER(1)       DEFAULT '0' NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
);

-- INDEX Int64ValuesParamId_idx
CREATE INDEX Int64ParamValuesParamId_idx ON Int64ParamValues(paramId);


--
-- TABLE 'VInt64ParamValues'
--
CREATE TABLE VInt64ParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	NUMBER		NOT NULL,
	hex		NUMBER(1)       DEFAULT '0' NOT NULL,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
);

-- INDEX VInt64ValuesParamId_idx
CREATE INDEX VInt64ValuesParamId_idx ON VInt64ParamValues(paramId);


--
-- TABLE 'UInt64ParamValues'
--
CREATE TABLE UInt64ParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER		NOT NULL,
	hex		NUMBER(1)       DEFAULT '0' NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
);

-- INDEX UInt64ValuesParamId_idx
CREATE INDEX UInt64ValuesParamId_idx ON UInt64ParamValues(paramId);


--
-- TABLE 'VUInt64ParamValues'
--
CREATE TABLE VUInt64ParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	NUMBER		NOT NULL,
	hex		NUMBER(1)       DEFAULT '0' NOT NULL,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
);

-- INDEX VUInt64ValuesParamId_idx
CREATE INDEX VUInt64ValuesParamId_idx ON VUInt64ParamValues(paramId);


--
-- TABLE 'BoolParamValues'
--
CREATE TABLE BoolParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER(1)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
);

-- INDEX VDoubleValuesParamId_idx
CREATE INDEX VDoubleValuesParamId_idx ON VDoubleParamValues(paramId);


--
-- TABLE 'StringParamValues'
--
CREATE TABLE StringParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(4000),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	value      	VARCHAR2(4000),
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
);

-- INDEX VStringValuesParamId_idx
CREATE INDEX VStringValuesParamId_idx ON VStringParamValues(paramId);


--
-- TABLE 'InputTagParamValues'
--
CREATE TABLE InputTagParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(4000)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	value      	VARCHAR2(4000)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
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
INSERT INTO ParameterTypes VALUES (15,'int64');
INSERT INTO ParameterTypes VALUES (16,'vint64');
INSERT INTO ParameterTypes VALUES (17,'uint64');
INSERT INTO ParameterTypes VALUES (18,'vuint64');


COMMIT;
