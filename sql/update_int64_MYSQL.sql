-- INSERT valid parameter types
INSERT INTO ParameterTypes (paramType) VALUES("int64");
INSERT INTO ParameterTypes (paramType) VALUES("vint64");
INSERT INTO ParameterTypes (paramType) VALUES("uint64");
INSERT INTO ParameterTypes (paramType) VALUES("vuint64");

-- TABLE 'Int64ParamValues'
CREATE TABLE Int64ParamValues
(
	paramId    	BIGINT UNSIGNED   NOT NULL UNIQUE,
	value      	BIGINT            NOT NULL,
	hex		BOOLEAN           NOT NULL DEFAULT false,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
) ENGINE=INNODB;

-- TABLE 'VInt64ParamValues'
CREATE TABLE VInt64ParamValues
(
	paramId    	BIGINT UNSIGNED   NOT NULL,
	sequenceNb 	SMALLINT UNSIGNED NOT NULL,
	value      	BIGINT            NOT NULL,
	hex		BOOLEAN		  NOT NULL DEFAULT false,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
) ENGINE=INNODB;

-- TABLE 'UInt64ParamValues'
CREATE TABLE UInt64ParamValues
(
	paramId    	BIGINT UNSIGNED   NOT NULL UNIQUE,
	value      	BIGINT UNSIGNED   NOT NULL,
        hex		BOOLEAN		  NOT NULL DEFAULT false,
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
) ENGINE=INNODB;

-- TABLE 'VUInt64ParamValues'
CREATE TABLE VUInt64ParamValues
(
	paramId    	BIGINT UNSIGNED   NOT NULL,
	sequenceNb 	SMALLINT UNSIGNED NOT NULL,
	value      	BIGINT UNSIGNED   NOT NULL,
	hex		BOOLEAN		  NOT NULL DEFAULT false,
	UNIQUE(paramId,sequenceNb),
	FOREIGN KEY(paramId) REFERENCES Parameters(paramId) ON DELETE CASCADE
) ENGINE=INNODB;
