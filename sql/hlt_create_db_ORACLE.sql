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
	configDescriptor VARCHAR2(256)  NOT NULL UNIQUE,
	parentDirId     NUMBER		NOT NULL,
	config     	VARCHAR2(64)    NOT NULL,
	version         NUMBER(4)	NOT NULL,
	created         TIMESTAMP       NOT NULL,
	UNIQUE (parentDirId,config,version),
	PRIMARY KEY(configId),
	FOREIGN KEY(parentDirId) REFERENCES Directories(dirId)
);

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
-- TABLE 'ConfigurationReleaseAssoc'
--
CREATE TABLE ConfigurationReleaseAssoc
(
	configId   	NUMBER		NOT NULL,
	releaseId   	NUMBER		NOT NULL,
	FOREIGN KEY(configId) REFERENCES Configurations(configId),
	FOREIGN KEY(releaseId) REFERENCES SoftwareReleases(releaseId)
);

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
	FOREIGN KEY(superId)   REFERENCES SuperIds(superId),
	FOREIGN KEY(releaseId) REFERENCES SoftwareReleases(releaseId)
);

--
-- TABLE 'Paths'
--
CREATE TABLE Paths
(
	pathId     	NUMBER,
	configId   	NUMBER		NOT NULL,
	name       	VARCHAR2(64)    NOT NULL,
	sequenceNb 	NUMBER(3) 	NOT NULL,
	isEndPath       NUMBER(1)       DEFAULT '0' NOT NULL,
	UNIQUE(configId,name),
	UNIQUE(configId,sequenceNb),
	PRIMARY KEY(pathId),
	FOREIGN KEY(configId) REFERENCES Configurations(configId)
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
-- TABLE 'PathInPathAssoc'
--
CREATE TABLE PathInPathAssoc
(
	parentPathId	NUMBER		NOT NULL,
	childPathId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	FOREIGN KEY (parentPathId) REFERENCES Paths(pathId),
	FOREIGN KEY (childPathId)  REFERENCES Paths(pathId)
);


--
-- TABLE 'Sequences'
--
CREATE TABLE Sequences
(
	sequenceId	NUMBER		NOT NULL,
	configId        NUMBER		NOT NULL,
	name		VARCHAR2(64)	NOT NULL,
	PRIMARY KEY(sequenceId),
	FOREIGN KEY(configId) REFERENCES Configurations(configId)
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
-- TABLE 'PathSequenceAssoc'
--
CREATE TABLE PathSequenceAssoc
(
	pathId		NUMBER		NOT NULL,
	sequenceId	NUMBER   	NOT NULL,
	sequenceNb      NUMBER(3) 	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	UNIQUE(pathId,sequenceId),
	FOREIGN KEY(pathId)     REFERENCES Paths(pathId),
	FOREIGN KEY(sequenceId) REFERENCES Sequences(sequenceId)
);


--
-- TABLE 'SequenceInSequenceAssoc'
--
CREATE TABLE SequenceInSequenceAssoc
(
	parentSequenceId NUMBER		NOT NULL,
	childSequenceId	 NUMBER		NOT NULL,
	sequenceNb	 NUMBER(3)	NOT NULL,
	operator	 NUMBER(3)	DEFAULT '0' NOT NULL,
	FOREIGN KEY (parentSequenceId) REFERENCES Sequences(sequenceId),
	FOREIGN KEY (childSequenceId)  REFERENCES Sequences(sequenceId)
);


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
	name       	VARCHAR2(64)	NOT NULL,
	cvstag       	VARCHAR2(64)	NOT NULL,
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
	configId   	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES ServiceTemplates(superId),
	FOREIGN KEY(configId)   REFERENCES Configurations(configId)
);


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
	name       	VARCHAR2(64)	NOT NULL,
	cvstag       	VARCHAR2(64)	NOT NULL,
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
	configId   	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES EDSourceTemplates(superId),
	FOREIGN KEY(configId)   REFERENCES Configurations(configId)
);


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
	name       	VARCHAR2(64)	NOT NULL,
	cvstag       	VARCHAR2(64)	NOT NULL,
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
	configId   	NUMBER		NOT NULL,
	name       	VARCHAR2(64)	NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES ESSourceTemplates(superId),
	FOREIGN KEY(configId)   REFERENCES Configurations(configId)
);


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
	name       	VARCHAR2(64)	NOT NULL,
	cvstag       	VARCHAR2(64)	NOT NULL,
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
	configId   	NUMBER		NOT NULL,
	name       	VARCHAR2(64)	NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES ESModuleTemplates(superId),
	FOREIGN KEY(configId)   REFERENCES Configurations(configId)
);


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
	name       	VARCHAR2(64)	NOT NULL,
	cvstag       	VARCHAR2(64)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId) REFERENCES SuperIds(superId),
	FOREIGN KEY(typeId)  REFERENCES ModuleTypes(typeId)
);


--
-- TABLE 'Modules'
--
CREATE TABLE Modules
(
	superId   	NUMBER,
	templateId  	NUMBER		NOT NULL,
	name       	VARCHAR2(64)	NOT NULL,
	PRIMARY KEY(superId),
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(templateId) REFERENCES ModuleTemplates(superId)
);


--
-- TABLE 'PathModuleAssoc'
--
CREATE TABLE PathModuleAssoc
(
	pathId     	NUMBER		NOT NULL,
        moduleId   	NUMBER		NOT NULL,
	sequenceNb	NUMBER(4)	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	FOREIGN KEY(pathId)   REFERENCES Paths(pathId),
	FOREIGN KEY(moduleId) REFERENCES Modules(superId)
);


--
-- TABLE 'SequenceModuleAssoc'
--
CREATE TABLE SequenceModuleAssoc
(
	sequenceId     	NUMBER		NOT NULL,
        moduleId   	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	operator	NUMBER(3)	DEFAULT '0' NOT NULL,
	FOREIGN KEY(sequenceId) REFERENCES Sequences(sequenceId),
	FOREIGN KEY(moduleId)   REFERENCES Modules(superId)
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
	name		VARCHAR2(64)	NOT NULL,
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
	name		VARCHAR2(64)	NOT NULL,
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
	paramSetId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	FOREIGN KEY(configId)   REFERENCES Configurations(configId),
	FOREIGN KEY(paramSetId) REFERENCES ParameterSets(superId)
);


--
-- TABLE 'SuperIdParamSetAssoc'
--
CREATE TABLE SuperIdParamSetAssoc
(
	superId		NUMBER		NOT NULL,
	paramSetId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	FOREIGN KEY(superId)    REFERENCES SuperIds(superId),
	FOREIGN KEY(paramSetId) REFERENCES ParameterSets(superId)
);


--
-- TABLE 'SuperIdVecParamSetAssoc'
--
CREATE TABLE SuperIdVecParamSetAssoc
(
	superId		NUMBER		NOT NULL,
	vecParamSetId	NUMBER		NOT NULL,
	sequenceNb	NUMBER(3)	NOT NULL,
	FOREIGN KEY(superId)       REFERENCES SuperIds(superId),
	FOREIGN KEY(vecParamSetId) REFERENCES VecParameterSets(superId)
);


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
	name       	VARCHAR2(64)	NOT NULL,
	tracked         NUMBER(1)       NOT NULL,
	PRIMARY KEY(paramId),
	FOREIGN KEY(paramTypeId) REFERENCES ParameterTypes(paramTypeId)
);

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
	FOREIGN KEY(superId) REFERENCES SuperIds(superId),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


--
-- TABLE 'Int32ParamValues'
--
CREATE TABLE Int32ParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER		NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


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


--
-- TABLE 'UInt32ParamValues'
--
CREATE TABLE UInt32ParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER		NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


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


--
-- TABLE 'BoolParamValues'
--
CREATE TABLE BoolParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	NUMBER(1)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


--
-- TABLE 'DoubleParamValues'
--
CREATE TABLE DoubleParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	FLOAT		NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


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


--
-- TABLE 'StringParamValues'
--
CREATE TABLE StringParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(256)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


--
-- TABLE 'VIntParamValues'
--
CREATE TABLE VStringParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	VARCHAR2(256)	NOT NULL,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


--
-- TABLE 'InputTagParamValues'
--
CREATE TABLE InputTagParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(64)    NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


--
-- TABLE 'VInputTagParamValues'
--
CREATE TABLE VInputTagParamValues
(
	paramId    	NUMBER		NOT NULL,
	sequenceNb 	NUMBER(6)	NOT NULL,
	value      	VARCHAR2(64)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


--
-- TABLE 'EventIDParamValues'
--
CREATE TABLE EventIDParamValues
(
	paramId    	NUMBER		NOT NULL,
	value      	VARCHAR2(32)	NOT NULL,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId)
);


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
INSERT INTO ModuleTypes VALUES (6,'ESProducer');
INSERT INTO ModuleTypes VALUES (7,'OutputModule');


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


COMMIT;
