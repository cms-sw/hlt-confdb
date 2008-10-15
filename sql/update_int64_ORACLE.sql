--
-- CREATE TABLES
--

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
-- INSERT Parameter Types
--
INSERT INTO ParameterTypes VALUES (15,'int64');
INSERT INTO ParameterTypes VALUES (16,'vint64');
INSERT INTO ParameterTypes VALUES (17,'uint64');
INSERT INTO ParameterTypes VALUES (18,'vuint64');
