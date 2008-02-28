--
-- MySQL Stored Procedure: load templates for a release
--

-- drop the procedure if it already exists
USE hltdb;
DROP PROCEDURE IF EXISTS load_template;
DROP PROCEDURE IF EXISTS load_templates;
DROP PROCEDURE IF EXISTS load_templates_for_config;
DROP PROCEDURE IF EXISTS load_configuration;
DROP PROCEDURE IF EXISTS load_parameters;
DROP PROCEDURE IF EXISTS load_parameter_value;


-- set delimiter to '//'
DELIMITER //


--
-- PROCEDURE load_template
--
CREATE PROCEDURE load_template(release_id BIGINT UNSIGNED,
                               template_name CHAR(128))
BEGIN
  BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    DROP TABLE IF EXISTS tmp_template_table;
    DROP TABLE IF EXISTS tmp_parameter_table;
    DROP TABLE IF EXISTS tmp_boolean_table;
    DROP TABLE IF EXISTS tmp_int_table;
    DROP TABLE IF EXISTS tmp_real_table;
    DROP TABLE IF EXISTS tmp_string_table;
  END;
  BEGIN
    /* variables */
    DECLARE v_template_id     BIGINT UNSIGNED;
    DECLARE v_template_type   CHAR(64);
    DECLARE v_template_name   CHAR(128);
    DECLARE v_template_cvstag CHAR(32);
    DECLARE v_template_pkgid  BIGINT UNSIGNED;
    DECLARE done              BOOLEAN DEFAULT FALSE;

    /* cursor for edsource templates */
    DECLARE cur_edsource_templates CURSOR FOR
      SELECT EDSourceTemplates.superId,
             EDSourceTemplates.name,
             EDSourceTemplates.cvstag,
	     EDSourceTemplates.packageId
      FROM EDSourceTemplates
      JOIN SuperIdReleaseAssoc
      ON EDSourceTemplates.superId = SuperIdReleaseAssoc.superId
      JOIN SoftwareReleases
      ON SoftwareReleases.releaseId = SuperIdReleaseAssoc.releaseId
      WHERE SoftwareReleases.releaseId = release_id
      AND   EDSourceTemplates.name = template_name;

    /* cursor for essource templates */
    DECLARE cur_essource_templates CURSOR FOR
      SELECT ESSourceTemplates.superId,
             ESSourceTemplates.name,
             ESSourceTemplates.cvstag,
	     ESSourceTemplates.packageId
      FROM ESSourceTemplates
      JOIN SuperIdReleaseAssoc
      ON ESSourceTemplates.superId = SuperIdReleaseAssoc.superId
      JOIN SoftwareReleases
      ON SoftwareReleases.releaseId = SuperIdReleaseAssoc.releaseId
      WHERE SoftwareReleases.releaseId = release_id
      AND   ESSourceTemplates.name = template_name;

    /* cursor for esmodule templates */
    DECLARE cur_esmodule_templates CURSOR FOR
      SELECT ESModuleTemplates.superId,
             ESModuleTemplates.name,
             ESModuleTemplates.cvstag,
	     ESModuleTemplates.packageId
      FROM ESModuleTemplates
      JOIN SuperIdReleaseAssoc
      ON ESModuleTemplates.superId = SuperIdReleaseAssoc.superId
      JOIN SoftwareReleases
      ON SoftwareReleases.releaseId = SuperIdReleaseAssoc.releaseId
      WHERE SuperIdReleaseAssoc.releaseId = release_id
      AND   ESModuleTemplates.name = template_name;

    /* cursor for service templates */
    DECLARE cur_service_templates CURSOR FOR
      SELECT ServiceTemplates.superId,
             ServiceTemplates.name,
             ServiceTemplates.cvstag,
	     ServiceTemplates.packageId
      FROM ServiceTemplates
      JOIN SuperIdReleaseAssoc
      ON ServiceTemplates.superId = SuperIdReleaseAssoc.superId
      JOIN SoftwareReleases
      ON SoftwareReleases.releaseId = SuperIdReleaseAssoc.releaseId
      WHERE SuperIdReleaseAssoc.releaseId = release_id
      AND   ServiceTemplates.name = template_name;

    /* cursor for module templates */
    DECLARE cur_module_templates CURSOR FOR
      SELECT ModuleTemplates.superId,
             ModuleTemplates.name,
             ModuleTemplates.cvstag,
	     ModuleTemplates.packageId,
             ModuleTypes.type
      FROM ModuleTemplates
      JOIN ModuleTypes
      ON   ModuleTemplates.typeId = ModuleTypes.typeId
      JOIN SuperIdReleaseAssoc
      ON ModuleTemplates.superId = SuperIdReleaseAssoc.superId
      JOIN SoftwareReleases
      ON SoftwareReleases.releaseId = SuperIdReleaseAssoc.releaseId
      WHERE SuperIdReleaseAssoc.releaseId = release_id
      AND   ModuleTemplates.name = template_name;

    /* error handlers */
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    /* temporary template table */
    CREATE TEMPORARY TABLE tmp_template_table
    (
      template_id      BIGINT UNSIGNED,
      template_type    CHAR(64),
      template_name    CHAR(128),
      template_cvstag  CHAR(32),
      template_pkgid   BIGINT UNSIGNED
    );

    /* temporary parameter table */
    CREATE TEMPORARY TABLE tmp_parameter_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_type    CHAR(64),
      parameter_name    CHAR(128),
      parameter_trkd    BOOLEAN,
      parameter_seqnb   INT,
      parent_id         BIGINT UNSIGNED
    );

    /* temporary bool parameter-value table */
    CREATE TEMPORARY TABLE tmp_boolean_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   BOOLEAN
    );

    /* temporary int32 parameter-value table */
    CREATE TEMPORARY TABLE tmp_int_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   BIGINT,
      sequence_nb       INT,
      hex 		BOOLEAN
    );

    /* temporary double parameter-value table */
    CREATE TEMPORARY TABLE tmp_real_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   REAL,
      sequence_nb       INT
    );

    /* temporary string parameter-value table */
    CREATE TEMPORARY TABLE tmp_string_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   VARCHAR(512),
      sequence_nb       INT
    );

    /* load edsource templates */
    OPEN cur_edsource_templates;
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    IF done=FALSE THEN
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'EDSource',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      SET done = TRUE;   
    END IF;
    CLOSE cur_edsource_templates;


    /* load essource templates */
    IF done=FALSE THEN
      OPEN cur_essource_templates;
      FETCH cur_essource_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
      IF done=FALSE THEN
        INSERT INTO tmp_template_table
          VALUES(v_template_id,'ESSource',
                 v_template_name,v_template_cvstag,v_template_pkgid);
        CALL load_parameters(v_template_id);
        SET done=TRUE;
      END IF;
      CLOSE cur_essource_templates;
    END IF;

    /* load esmodule templates */
    IF done=FALSE THEN
      OPEN cur_esmodule_templates;
      FETCH cur_esmodule_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
      IF done=FALSE THEN
        INSERT INTO tmp_template_table
          VALUES(v_template_id,'ESModule',
                 v_template_name,v_template_cvstag,v_template_pkgid);
        CALL load_parameters(v_template_id);
      SET done=TRUE;
      END IF;
      CLOSE cur_esmodule_templates;
    END IF;

    /* load service templates */
    IF done=FALSE THEN
      OPEN cur_service_templates;
      FETCH cur_service_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
      IF done=FALSE THEN
        INSERT INTO tmp_template_table
          VALUES(v_template_id,'Service',
                 v_template_name,v_template_cvstag,v_template_pkgid);
        CALL load_parameters(v_template_id);
      SET done=TRUE;
      END IF;
      CLOSE cur_service_templates;
    END IF;

    /* load module templates */
    IF done=FALSE THEN
      OPEN cur_module_templates;
      FETCH cur_module_templates
        INTO v_template_id,v_template_name,
             v_template_cvstag,v_template_pkgid,v_template_type;
      IF done=FALSE THEN
        INSERT INTO tmp_template_table
          VALUES(v_template_id,v_template_type,
                 v_template_name,v_template_cvstag,v_template_pkgid);
        CALL load_parameters(v_template_id);
      SET done=TRUE;
      END IF;
      CLOSE cur_module_templates;
    END IF;

  END;  

END;
//


--
-- PROCEDURE load_templates
--
CREATE PROCEDURE load_templates(release_id BIGINT UNSIGNED)
BEGIN
  BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    DROP TABLE IF EXISTS tmp_template_table;
    DROP TABLE IF EXISTS tmp_parameter_table;
    DROP TABLE IF EXISTS tmp_boolean_table;
    DROP TABLE IF EXISTS tmp_int_table;
    DROP TABLE IF EXISTS tmp_real_table;
    DROP TABLE IF EXISTS tmp_string_table;
  END;
  BEGIN
    /* variables */
    DECLARE v_template_id     BIGINT UNSIGNED;
    DECLARE v_template_type   CHAR(64);
    DECLARE v_template_name   CHAR(128);
    DECLARE v_template_cvstag CHAR(32);
    DECLARE v_template_pkgid  BIGINT UNSIGNED;
    DECLARE done              BOOLEAN DEFAULT FALSE;

    /* cursor for edsource templates */
    DECLARE cur_edsource_templates CURSOR FOR
      SELECT EDSourceTemplates.superId,
             EDSourceTemplates.name,
             EDSourceTemplates.cvstag,
	     EDSourceTemplates.packageId
      FROM EDSourceTemplates
      JOIN SuperIdReleaseAssoc
      ON EDSourceTemplates.superId = SuperIdReleaseAssoc.superId
      WHERE SuperIdReleaseAssoc.releaseId = release_id;

    /* cursor for essource templates */
    DECLARE cur_essource_templates CURSOR FOR
      SELECT ESSourceTemplates.superId,
             ESSourceTemplates.name,
             ESSourceTemplates.cvstag,
	     ESSourceTemplates.packageId
      FROM ESSourceTemplates
      JOIN SuperIdReleaseAssoc
      ON ESSourceTemplates.superId = SuperIdReleaseAssoc.superId
      WHERE SuperIdReleaseAssoc.releaseId = release_id;

    /* cursor for esmodule templates */
    DECLARE cur_esmodule_templates CURSOR FOR
      SELECT ESModuleTemplates.superId,
             ESModuleTemplates.name,
             ESModuleTemplates.cvstag,
	     ESModuleTemplates.packageId
      FROM ESModuleTemplates
      JOIN SuperIdReleaseAssoc
      ON ESModuleTemplates.superId = SuperIdReleaseAssoc.superId
      WHERE SuperIdReleaseAssoc.releaseId = release_id;

    /* cursor for service templates */
    DECLARE cur_service_templates CURSOR FOR
      SELECT ServiceTemplates.superId,
             ServiceTemplates.name,
             ServiceTemplates.cvstag,
	     ServiceTemplates.packageId
      FROM ServiceTemplates
      JOIN SuperIdReleaseAssoc
      ON ServiceTemplates.superId = SuperIdReleaseAssoc.superId
      WHERE SuperIdReleaseAssoc.releaseId = release_id;

    /* cursor for module templates */
    DECLARE cur_module_templates CURSOR FOR
      SELECT ModuleTemplates.superId,
             ModuleTemplates.name,
             ModuleTemplates.cvstag,
	     ModuleTemplates.packageId,
             ModuleTypes.type
      FROM ModuleTemplates
      JOIN ModuleTypes
      ON   ModuleTemplates.typeId = ModuleTypes.typeId
      JOIN SuperIdReleaseAssoc
      ON ModuleTemplates.superId = SuperIdReleaseAssoc.superId
      WHERE SuperIdReleaseAssoc.releaseId = release_id;

    /* error handlers */
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    /* temporary template table */
    CREATE TEMPORARY TABLE tmp_template_table
    (
      template_id      BIGINT UNSIGNED,
      template_type    CHAR(64),
      template_name    CHAR(128),
      template_cvstag  CHAR(32),
      template_pkgid   BIGINT UNSIGNED
    );

    /* temporary parameter table */
    CREATE TEMPORARY TABLE tmp_parameter_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_type    CHAR(64),
      parameter_name    CHAR(128),
      parameter_trkd    BOOLEAN,
      parameter_seqnb   INT,
      parent_id         BIGINT UNSIGNED
    );

    /* temporary bool parameter-value table */
    CREATE TEMPORARY TABLE tmp_boolean_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   BOOLEAN
    );

    /* temporary int32 parameter-value table */
    CREATE TEMPORARY TABLE tmp_int_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   BIGINT,
      sequence_nb       INT,
      hex		BOOLEAN
    );

    /* temporary double parameter-value table */
    CREATE TEMPORARY TABLE tmp_real_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   REAL,
      sequence_nb       INT
    );

    /* temporary string parameter-value table */
    CREATE TEMPORARY TABLE tmp_string_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   VARCHAR(512),
      sequence_nb       INT
    );

    /* load edsource templates */
    OPEN cur_edsource_templates;
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'EDSource',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_edsource_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    END WHILE;
    CLOSE cur_edsource_templates;
    SET done=FALSE;

    /* load essource templates */
    OPEN cur_essource_templates;
    FETCH cur_essource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'ESSource',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_essource_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    END WHILE;
    CLOSE cur_essource_templates;
    SET done=FALSE;

    /* load esmodule templates */
    OPEN cur_esmodule_templates;
    FETCH cur_esmodule_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'ESModule',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_esmodule_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    END WHILE;
    CLOSE cur_esmodule_templates;
    SET done=FALSE;

    /* load service templates */
    OPEN cur_service_templates;
    FETCH cur_service_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'Service',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_service_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    END WHILE;
    CLOSE cur_service_templates;
    SET done=FALSE;

    /* load module templates */
    OPEN cur_module_templates;
    FETCH cur_module_templates
      INTO v_template_id,v_template_name,
           v_template_cvstag,v_template_pkgid,v_template_type;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,v_template_type,
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_module_templates
        INTO v_template_id,v_template_name,
             v_template_cvstag,v_template_pkgid,v_template_type;
    END WHILE;
    CLOSE cur_module_templates;
    SET done=FALSE;

  END;  

END;
//


--
-- PROCEDURE load_templates_for_config
--
CREATE PROCEDURE load_templates_for_config(config_id BIGINT UNSIGNED)
BEGIN
  BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    DROP TABLE IF EXISTS tmp_template_table;
    DROP TABLE IF EXISTS tmp_parameter_table;
    DROP TABLE IF EXISTS tmp_boolean_table;
    DROP TABLE IF EXISTS tmp_int_table;
    DROP TABLE IF EXISTS tmp_real_table;
    DROP TABLE IF EXISTS tmp_string_table;
  END;
  BEGIN
    /* variables */
    DECLARE v_template_id     BIGINT UNSIGNED;
    DECLARE v_template_type   CHAR(64);
    DECLARE v_template_name   CHAR(128);
    DECLARE v_template_cvstag CHAR(32);
    DECLARE v_template_pkgid  BIGINT UNSIGNED;
    DECLARE done              BOOLEAN DEFAULT FALSE;

    /* cursor for edsource templates */
    DECLARE cur_edsource_templates CURSOR FOR
      SELECT DISTINCT
        EDSourceTemplates.superId,
        EDSourceTemplates.name,
        EDSourceTemplates.cvstag,
	EDSourceTemplates.packageId
      FROM EDSourceTemplates
      JOIN EDSources 
      ON EDSources.templateId = EDSourceTemplates.superId
      JOIN ConfigurationEDSourceAssoc
      ON EDSources.superId = ConfigurationEDSourceAssoc.edsourceId
      WHERE ConfigurationEDSourceAssoc.configId = config_id;

    /* cursor for essource templates */
    DECLARE cur_essource_templates CURSOR FOR
      SELECT DISTINCT
        ESSourceTemplates.superId,
        ESSourceTemplates.name,
        ESSourceTemplates.cvstag,
	ESSourceTemplates.packageId
      FROM ESSourceTemplates
      JOIN ESSources
      ON ESSources.templateId = ESSourceTemplates.superId
      JOIN ConfigurationESSourceAssoc
      ON ESSources.superId = ConfigurationESSourceAssoc.essourceId
      WHERE ConfigurationESSourceAssoc.configId = config_id;

    /* cursor for esmodule templates */
    DECLARE cur_esmodule_templates CURSOR FOR
      SELECT DISTINCT
        ESModuleTemplates.superId,
        ESModuleTemplates.name,
        ESModuleTemplates.cvstag,
	ESModuleTemplates.packageId
      FROM ESModuleTemplates
      JOIN ESModules
      ON ESModules.templateId = ESModuleTemplates.superId
      JOIN ConfigurationESModuleAssoc
      ON ESModules.superId = ConfigurationESModuleAssoc.esmoduleId
      WHERE ConfigurationESModuleAssoc.configId = config_id;

    /* cursor for service templates */
    DECLARE cur_service_templates CURSOR FOR
      SELECT DISTINCT
        ServiceTemplates.superId,
        ServiceTemplates.name,
        ServiceTemplates.cvstag,
	ServiceTemplates.packageId
      FROM ServiceTemplates
      JOIN Services
      ON   Services.templateId = ServiceTemplates.superId
      JOIN ConfigurationServiceAssoc
      ON   Services.superId = ConfigurationServiceAssoc.serviceId
      WHERE ConfigurationServiceAssoc.configId = config_id;

    /* cursor for module templates from configuration *paths* */
    DECLARE cur_module_templates_from_paths CURSOR FOR
      SELECT DISTINCT
        ModuleTemplates.superId,
        ModuleTemplates.name,
        ModuleTemplates.cvstag,
	ModuleTemplates.packageId,
        ModuleTypes.type
      FROM ModuleTemplates
      JOIN ModuleTypes
      ON   ModuleTemplates.typeId = ModuleTypes.typeId
      JOIN Modules
      ON   Modules.templateId = ModuleTemplates.superId
      JOIN PathModuleAssoc
      ON   PathModuleAssoc.moduleId = Modules.superId
      JOIN ConfigurationPathAssoc
      ON   PathModuleAssoc.pathId = ConfigurationPathAssoc.pathId
      WHERE ConfigurationPathAssoc.configId = config_id;

    /* cursor for module templates from configuration *sequences* */
    DECLARE cur_module_templates_from_sequences CURSOR FOR
      SELECT DISTINCT
        ModuleTemplates.superId,
        ModuleTemplates.name,
        ModuleTemplates.cvstag,
	ModuleTemplates.packageId,
        ModuleTypes.type
      FROM ModuleTemplates
      JOIN ModuleTypes
      ON   ModuleTemplates.typeId = ModuleTypes.typeId
      JOIN Modules
      ON   Modules.templateId = ModuleTemplates.superId
      JOIN SequenceModuleAssoc
      ON   SequenceModuleAssoc.moduleId = Modules.superId
      JOIN ConfigurationSequenceAssoc
      ON   SequenceModuleAssoc.sequenceId=ConfigurationSequenceAssoc.sequenceId
      WHERE ConfigurationSequenceAssoc.configId = config_id;

    /* error handlers */
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    /* temporary template table */
    CREATE TEMPORARY TABLE tmp_template_table
    (
      template_id      BIGINT UNSIGNED,
      template_type    CHAR(64),
      template_name    CHAR(128),
      template_cvstag  CHAR(32),
      template_pkgid   BIGINT UNSIGNED
    );

    /* temporary parameter table */
    CREATE TEMPORARY TABLE tmp_parameter_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_type    CHAR(64),
      parameter_name    CHAR(128),
      parameter_trkd    BOOLEAN,
      parameter_seqnb   INT,
      parent_id         BIGINT UNSIGNED
    );

    /* temporary bool parameter-value table */
    CREATE TEMPORARY TABLE tmp_boolean_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   BOOLEAN
    );

    /* temporary int32 parameter-value table */
    CREATE TEMPORARY TABLE tmp_int_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   BIGINT,
      sequence_nb       INT,
      hex		BOOLEAN
    );

    /* temporary double parameter-value table */
    CREATE TEMPORARY TABLE tmp_real_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   REAL,
      sequence_nb       INT
    );

    /* temporary string parameter-value table */
    CREATE TEMPORARY TABLE tmp_string_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   VARCHAR(512),
      sequence_nb       INT
    );

    /* load edsource templates */
    OPEN cur_edsource_templates;
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'EDSource',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_edsource_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    END WHILE;
    CLOSE cur_edsource_templates;
    SET done=FALSE;

    /* load essource templates */
    OPEN cur_essource_templates;
    FETCH cur_essource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'ESSource',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_essource_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    END WHILE;
    CLOSE cur_essource_templates;
    SET done=FALSE;

    /* load esmodule templates */
    OPEN cur_esmodule_templates;
    FETCH cur_esmodule_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'ESModule',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_esmodule_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    END WHILE;
    CLOSE cur_esmodule_templates;
    SET done=FALSE;

    /* load service templates */
    OPEN cur_service_templates;
    FETCH cur_service_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'Service',
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_service_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    END WHILE;
    CLOSE cur_service_templates;
    SET done=FALSE;

    /* load module templates from *paths* */
    OPEN cur_module_templates_from_paths;
    FETCH cur_module_templates_from_paths
      INTO v_template_id,v_template_name,
	   v_template_cvstag,v_template_pkgid,v_template_type;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,v_template_type,
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_module_templates_from_paths
        INTO v_template_id,v_template_name,
             v_template_cvstag,v_template_pkgid,v_template_type;
    END WHILE;
    CLOSE cur_module_templates_from_paths;
    SET done=FALSE;

    /* load module templates from *sequences* */
    OPEN cur_module_templates_from_sequences;
    FETCH cur_module_templates_from_sequences
      INTO v_template_id,v_template_name,
           v_template_cvstag,v_template_pkgid,v_template_type;
    WHILE done=FALSE DO
      INSERT INTO tmp_template_table
        VALUES(v_template_id,v_template_type,
               v_template_name,v_template_cvstag,v_template_pkgid);
      CALL load_parameters(v_template_id);
      FETCH cur_module_templates_from_sequences
        INTO v_template_id,v_template_name,
             v_template_cvstag,v_template_pkgid,v_template_type;
    END WHILE;
    CLOSE cur_module_templates_from_sequences;
    SET done=FALSE;

  END;  

END;
//


--
-- PROCEDURE load_configuration
--
CREATE PROCEDURE load_configuration(config_id BIGINT UNSIGNED)
BEGIN
  BEGIN
    DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
    DROP TABLE IF EXISTS tmp_instance_table;
    DROP TABLE IF EXISTS tmp_parameter_table;
    DROP TABLE IF EXISTS tmp_boolean_table;
    DROP TABLE IF EXISTS tmp_int_table;
    DROP TABLE IF EXISTS tmp_real_table;
    DROP TABLE IF EXISTS tmp_string_table;
    DROP TABLE IF EXISTS tmp_path_entries;
    DROP TABLE IF EXISTS tmp_sequence_entries;
  END;
  BEGIN
    /* variables */
    DECLARE v_instance_id     BIGINT UNSIGNED;
    DECLARE v_template_id     BIGINT UNSIGNED;
    DECLARE v_instance_type   CHAR(64);
    DECLARE v_instance_name   CHAR(128);
    DECLARE v_pset_is_trkd    BOOLEAN;
    DECLARE v_endpath         BOOLEAN;
    DECLARE v_prefer          BOOLEAN;
    DECLARE v_parent_id       BIGINT UNSIGNED;
    DECLARE v_child_id        BIGINT UNSIGNED;
    DECLARE v_sequence_nb     INT;
    DECLARE done              BOOLEAN DEFAULT FALSE;

    /* cursor for global psets */
    DECLARE cur_global_psets CURSOR FOR
      SELECT
        ParameterSets.superId,
        ParameterSets.name,
        ParameterSets.tracked,
	ConfigurationParamSetAssoc.sequenceNb
      FROM ParameterSets
      JOIN ConfigurationParamSetAssoc
      ON ParameterSets.superId = ConfigurationParamSetAssoc.psetId
      WHERE ConfigurationParamSetAssoc.configId = config_id;

    /* cursor for edsources */
    DECLARE cur_edsources CURSOR FOR
      SELECT
        EDSources.superId,
        EDSources.templateId,
	ConfigurationEDSourceAssoc.sequenceNb
      FROM EDSources
      JOIN ConfigurationEDSourceAssoc
      ON EDSources.superId = ConfigurationEDSourceAssoc.edsourceId
      WHERE ConfigurationEDSourceAssoc.configId = config_id;

    /* cursor for essources */
    DECLARE cur_essources CURSOR FOR
      SELECT
        ESSources.superId,
	ESSources.templateId,
        ESSources.name,
	ConfigurationESSourceAssoc.prefer,
	ConfigurationESSourceAssoc.sequenceNb
      FROM ESSources
      JOIN ConfigurationESSourceAssoc
      ON ESSources.superId = ConfigurationESSourceAssoc.essourceId
      WHERE ConfigurationESSourceAssoc.configId = config_id;

    /* cursor for esmodules */
    DECLARE cur_esmodules CURSOR FOR
      SELECT
        ESModules.superId,
        ESModules.templateId,
        ESModules.name,
	ConfigurationESModuleAssoc.prefer,
	ConfigurationESModuleAssoc.sequenceNb
      FROM ESModules
      JOIN ConfigurationESModuleAssoc
      ON ESModules.superId = ConfigurationESModuleAssoc.esmoduleId
      WHERE ConfigurationESModuleAssoc.configId = config_id;

    /* cursor for services */
    DECLARE cur_services CURSOR FOR
      SELECT
        Services.superId,
        Services.templateId,
	ConfigurationServiceAssoc.sequenceNb
      FROM Services
      JOIN ConfigurationServiceAssoc
      ON   Services.superId = ConfigurationServiceAssoc.serviceId
      WHERE ConfigurationServiceAssoc.configId = config_id;

    /* cursor for modules from configuration *paths* */
    DECLARE cur_modules_from_paths CURSOR FOR
      SELECT
        Modules.superId,
        Modules.templateId,
        Modules.name
      FROM Modules
      JOIN PathModuleAssoc
      ON   PathModuleAssoc.moduleId = Modules.superId
      JOIN ConfigurationPathAssoc
      ON   PathModuleAssoc.pathId = ConfigurationPathAssoc.pathId
      WHERE ConfigurationPathAssoc.configId = config_id;

    /* cursor for modules from configuration *sequences* */
    DECLARE cur_modules_from_sequences CURSOR FOR
      SELECT
        Modules.superId,
        Modules.templateId,
        Modules.name
      FROM Modules
      JOIN SequenceModuleAssoc
      ON   SequenceModuleAssoc.moduleId = Modules.superId
      JOIN ConfigurationSequenceAssoc
      ON   SequenceModuleAssoc.sequenceId=ConfigurationSequenceAssoc.sequenceId
      WHERE ConfigurationSequenceAssoc.configId = config_id;

    /* cursor for paths */
    DECLARE cur_paths CURSOR FOR
      SELECT
        Paths.pathId,
        Paths.name,
        Paths.isEndPath,
	ConfigurationPathAssoc.sequenceNb
      FROM Paths
      JOIN ConfigurationPathAssoc
      ON Paths.pathId = ConfigurationPathAssoc.pathId
      WHERE ConfigurationPathAssoc.configId = config_id;

    /* cursor for sequences */
    DECLARE cur_sequences CURSOR FOR
      SELECT
        Sequences.sequenceId,
        Sequences.name,
	ConfigurationSequenceAssoc.sequenceNb
      FROM Sequences
      JOIN ConfigurationSequenceAssoc
      ON Sequences.sequenceId = ConfigurationSequenceAssoc.sequenceId
      WHERE ConfigurationSequenceAssoc.configId = config_id;

    /* cursor for path-path associations */
    DECLARE cur_path_path CURSOR FOR
      SELECT
        PathInPathAssoc.parentPathId,
        PathInPathAssoc.childPathId,
        PathInPathAssoc.sequenceNb
      FROM PathInPathAssoc
      JOIN ConfigurationPathAssoc
      ON PathInPathAssoc.parentPathId = ConfigurationPathAssoc.pathId
      WHERE ConfigurationPathAssoc.configId = config_id;

    /* cursor for path-sequence associations */
    DECLARE cur_path_sequence CURSOR FOR
      SELECT
        PathSequenceAssoc.pathId,
        PathSequenceAssoc.sequenceId,
        PathSequenceAssoc.sequenceNb
      FROM PathSequenceAssoc
      JOIN ConfigurationPathAssoc
      ON PathSequenceAssoc.pathId = ConfigurationPathAssoc.pathId
      WHERE ConfigurationPathAssoc.configId = config_id;

    /* cursor for path-module associations */
    DECLARE cur_path_module CURSOR FOR
      SELECT
        PathModuleAssoc.pathId,
        PathModuleAssoc.moduleId,
        PathModuleAssoc.sequenceNb
      FROM PathModuleAssoc
      JOIN ConfigurationPathAssoc
      ON PathModuleAssoc.pathId = ConfigurationPathAssoc.pathId
      WHERE ConfigurationPathAssoc.configId = config_id;

    /* cursor for sequence-sequence associations */
    DECLARE cur_sequence_sequence CURSOR FOR
      SELECT
        SequenceInSequenceAssoc.parentSequenceId,
        SequenceInSequenceAssoc.childSequenceId,
        SequenceInSequenceAssoc.sequenceNb
      FROM SequenceInSequenceAssoc
      JOIN ConfigurationSequenceAssoc
      ON SequenceInSequenceAssoc.parentSequenceId =
         ConfigurationSequenceAssoc.sequenceId
      WHERE ConfigurationSequenceAssoc.configId = config_id;

    /* cursor for sequence-module associations */
    DECLARE cur_sequence_module CURSOR FOR
      SELECT
        SequenceModuleAssoc.sequenceId,
        SequenceModuleAssoc.moduleId,
        SequenceModuleAssoc.sequenceNb
      FROM SequenceModuleAssoc
      JOIN ConfigurationSequenceAssoc
      ON SequenceModuleAssoc.sequenceId = ConfigurationSequenceAssoc.sequenceId
      WHERE ConfigurationSequenceAssoc.configId = config_id;


    /* error handlers */
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;


    /* temporary instance table */
    CREATE TEMPORARY TABLE tmp_instance_table
    (
      instance_id	BIGINT UNSIGNED,
      template_id	BIGINT UNSIGNED,
      instance_type     CHAR(64),
      instance_name     CHAR(128),
      flag              BOOLEAN,
      sequence_nb	INT
    );

    /* temporary parameter table */
    CREATE TEMPORARY TABLE tmp_parameter_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_type    CHAR(64),
      parameter_name    CHAR(128),
      parameter_trkd    BOOLEAN,
      parameter_seqnb   INT,
      parent_id         BIGINT UNSIGNED
    );

    /* temporary bool parameter-value table */
    CREATE TEMPORARY TABLE tmp_boolean_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   BOOLEAN
    );

    /* temporary int32 parameter-value table */
    CREATE TEMPORARY TABLE tmp_int_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   BIGINT,
      sequence_nb       INT,
      hex               BOOLEAN
    );

    /* temporary double parameter-value table */
    CREATE TEMPORARY TABLE tmp_real_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   REAL,
      sequence_nb       INT
    );

    /* temporary string parameter-value table */
    CREATE TEMPORARY TABLE tmp_string_table
    (
      parameter_id	BIGINT UNSIGNED,
      parameter_value   VARCHAR(512),
      sequence_nb       INT
    );

    /* temporary path entry table */
    CREATE TEMPORARY TABLE tmp_path_entries
    (
      path_id           BIGINT UNSIGNED,
      entry_id          BIGINT UNSIGNED,
      sequence_nb       INT,
      entry_type        CHAR(64)
    );

    /* temporary sequence entry table */
    CREATE TEMPORARY TABLE tmp_sequence_entries
    (
      sequence_id       BIGINT UNSIGNED,
      entry_id          BIGINT UNSIGNED,
      sequence_nb       INT,
      entry_type        CHAR(64)
    );


    /* load global psets */
    OPEN cur_global_psets;
    FETCH cur_global_psets
      INTO v_instance_id,v_instance_name,v_pset_is_trkd,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,NULL,'PSet',
               v_instance_name,v_pset_is_trkd,v_sequence_nb);
      CALL load_parameters(v_instance_id);
      FETCH cur_global_psets
        INTO v_instance_id,v_instance_name,v_pset_is_trkd,v_sequence_nb;
    END WHILE;
    CLOSE cur_global_psets;
    SET done=FALSE;
 
    /* load edsources */
    OPEN cur_edsources;
    FETCH cur_edsources INTO v_instance_id,v_template_id,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,v_template_id,'EDSource',NULL,NULL,v_sequence_nb);
      CALL load_parameters(v_instance_id);
      FETCH cur_edsources INTO v_instance_id,v_template_id,v_sequence_nb;
    END WHILE;
    CLOSE cur_edsources;
    SET done=FALSE;

    /* load essources */
    OPEN cur_essources;
    FETCH cur_essources
      INTO v_instance_id,v_template_id,v_instance_name,v_prefer,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,v_template_id,'ESSource',
               v_instance_name,v_prefer,v_sequence_nb);
      CALL load_parameters(v_instance_id);
      FETCH cur_essources
        INTO v_instance_id,v_template_id,v_instance_name,v_prefer,v_sequence_nb;
    END WHILE;
    CLOSE cur_essources;
    SET done=FALSE;

    /* load esmodules */
    OPEN cur_esmodules;
    FETCH cur_esmodules
      INTO v_instance_id,v_template_id,v_instance_name,v_prefer,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,v_template_id,'ESModule',
               v_instance_name,v_prefer,v_sequence_nb);
      CALL load_parameters(v_instance_id);
      FETCH cur_esmodules
      INTO v_instance_id,v_template_id,v_instance_name,v_prefer,v_sequence_nb;
    END WHILE;
    CLOSE cur_esmodules;
    SET done=FALSE;

    /* load services */
    OPEN cur_services;
    FETCH cur_services INTO v_instance_id,v_template_id,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,v_template_id,'Service',NULL,NULL,v_sequence_nb);
      CALL load_parameters(v_instance_id);
      FETCH cur_services INTO v_instance_id,v_template_id,v_sequence_nb;
    END WHILE;
    CLOSE cur_services;
    SET done=FALSE;

    /* load modules from *paths* */
    OPEN cur_modules_from_paths;
    FETCH cur_modules_from_paths
      INTO v_instance_id,v_template_id,v_instance_name;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,v_template_id,'Module',v_instance_name,NULL,NULL);
      CALL load_parameters(v_instance_id);
      FETCH cur_modules_from_paths
        INTO v_instance_id,v_template_id,v_instance_name;
    END WHILE;
    CLOSE cur_modules_from_paths;
    SET done=FALSE;

    /* load modules from *sequences* */
    OPEN cur_modules_from_sequences;
    FETCH cur_modules_from_sequences
      INTO v_instance_id,v_template_id,v_instance_name;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,v_template_id,'Module',v_instance_name,NULL,NULL);
      CALL load_parameters(v_instance_id);
      FETCH cur_modules_from_sequences
        INTO v_instance_id,v_template_id,v_instance_name;
    END WHILE;
    CLOSE cur_modules_from_sequences;
    SET done=FALSE;

    /* load paths */
    OPEN cur_paths;
    FETCH cur_paths INTO v_instance_id,v_instance_name,v_endpath,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,NULL,'Path',v_instance_name,v_endpath,v_sequence_nb);
      FETCH cur_paths INTO v_instance_id,v_instance_name,v_endpath,v_sequence_nb;
    END WHILE;
    CLOSE cur_paths;
    SET done=FALSE;

    /* load sequences */
    OPEN cur_sequences;
    FETCH cur_sequences INTO v_instance_id,v_instance_name,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_instance_table
        VALUES(v_instance_id,NULL,'Sequence',v_instance_name,NULL,v_sequence_nb);
      FETCH cur_sequences INTO v_instance_id,v_instance_name,v_sequence_nb;
    END WHILE;
    CLOSE cur_sequences;
    SET done=FALSE;

    /* load path-path associations */
    OPEN cur_path_path;
    FETCH cur_path_path INTO v_parent_id,v_child_id,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_path_entries
        VALUES(v_parent_id,v_child_id,v_sequence_nb,'Path');
      FETCH cur_path_path INTO v_parent_id,v_child_id,v_sequence_nb;
    END WHILE;
    CLOSE cur_path_path;
    SET done=FALSE;

    /* load path-sequence associations */
    OPEN cur_path_sequence;
    FETCH cur_path_sequence INTO v_parent_id,v_child_id,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_path_entries
        VALUES(v_parent_id,v_child_id,v_sequence_nb,'Sequence');
      FETCH cur_path_sequence INTO v_parent_id,v_child_id,v_sequence_nb;
    END WHILE;
    CLOSE cur_path_sequence;
    SET done=FALSE;

    /* load path-module associations */
    OPEN cur_path_module;
    FETCH cur_path_module INTO v_parent_id,v_child_id,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_path_entries
        VALUES(v_parent_id,v_child_id,v_sequence_nb,'Module');
      FETCH cur_path_module INTO v_parent_id,v_child_id,v_sequence_nb;
    END WHILE;
    CLOSE cur_path_module;
    SET done=FALSE;

    /* load sequence-sequence associations */
    OPEN cur_sequence_sequence;
    FETCH cur_sequence_sequence INTO v_parent_id,v_child_id,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_sequence_entries
        VALUES(v_parent_id,v_child_id,v_sequence_nb,'Sequence');
      FETCH cur_sequence_sequence INTO v_parent_id,v_child_id,v_sequence_nb;
    END WHILE;
    CLOSE cur_sequence_sequence;
    SET done=FALSE;

    /* load sequence-module associations */
    OPEN cur_sequence_module;
    FETCH cur_sequence_module INTO v_parent_id,v_child_id,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_sequence_entries
        VALUES(v_parent_id,v_child_id,v_sequence_nb,'Module');
      FETCH cur_sequence_module INTO v_parent_id,v_child_id,v_sequence_nb;
    END WHILE;
    CLOSE cur_sequence_module;
    SET done=FALSE;

  END;  

END;
//


--
-- PROCEDURE load_parameters
--
CREATE PROCEDURE load_parameters(parent_id BIGINT UNSIGNED)
proc:
BEGIN
  DECLARE v_parameter_id    BIGINT UNSIGNED;
  DECLARE v_parameter_type  CHAR(64);
  DECLARE v_parameter_name  CHAR(128);
  DECLARE v_parameter_trkd  BOOLEAN;
  DECLARE v_parameter_seqnb INT;
  DECLARE done              BOOLEAN DEFAULT FALSE;

  /* cursor for parameters */
  DECLARE cur_parameters CURSOR FOR
    SELECT Parameters.paramId,
           ParameterTypes.paramType,
           Parameters.name,
           Parameters.tracked,
           SuperIdParameterAssoc.sequenceNb
    FROM Parameters
    JOIN ParameterTypes
    ON ParameterTypes.paramTypeId = Parameters.paramTypeId
    JOIN SuperIdParameterAssoc
    ON SuperIdParameterAssoc.paramId = Parameters.paramId
    WHERE SuperIdParameterAssoc.superId = parent_id
    ORDER BY SuperIdParameterAssoc.sequenceNb ASC;

  /* cursor for psets */
  DECLARE cur_psets CURSOR FOR
    SELECT ParameterSets.superId,
           ParameterSets.name,
           ParameterSets.tracked,
           SuperIdParamSetAssoc.sequenceNb
    FROM ParameterSets
    JOIN SuperIdParamSetAssoc
    ON SuperIdParamSetAssoc.psetId = ParameterSets.superId
    WHERE SuperIdParamSetAssoc.superId = parent_id
    ORDER BY SuperIdParamSetAssoc.sequenceNb ASC;

  /* cursor for vpsets */
  DECLARE cur_vpsets CURSOR FOR
    SELECT VecParameterSets.superId,
           VecParameterSets.name,
           VecParameterSets.tracked,
           SuperIdVecParamSetAssoc.sequenceNb
    FROM VecParameterSets
    JOIN SuperIdVecParamSetAssoc
    ON SuperIdVecParamSetAssoc.vpsetId = VecParameterSets.superId
    WHERE SuperIdVecParamSetAssoc.superId = parent_id
    ORDER BY SuperIdVecParamSetAssoc.sequenceNb ASC;

  /* error handlers */
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  /* load the parameters and fill them into temporary param table */
  OPEN cur_parameters;
  FETCH cur_parameters
    INTO v_parameter_id,v_parameter_type,
         v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
  WHILE done=FALSE DO
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
             v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id);
    CALL load_parameter_value(v_parameter_id,v_parameter_type);
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
  END WHILE;
  CLOSE cur_parameters;
  SET done = FALSE;

  /* load psets and fill them into temporary param table */
  OPEN cur_psets;
  FETCH cur_psets
    INTO v_parameter_id,v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
  WHILE done=FALSE DO
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,'PSet',
             v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id);
    CALL load_parameters(v_parameter_id);
    FETCH cur_psets
      INTO v_parameter_id,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
  END WHILE;
  CLOSE cur_psets;
  SET done = FALSE;

  /* load vpsets and fill them into temporary param table */
  OPEN cur_vpsets;
  FETCH cur_vpsets
    INTO v_parameter_id,
         v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
  WHILE done=FALSE DO
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,'VPSet',
             v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id);
    CALL load_parameters(v_parameter_id);
    FETCH cur_vpsets
      INTO v_parameter_id,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
  END WHILE;
  CLOSE cur_vpsets;
  SET done = FALSE;

END;
//


--
-- PROCDEDURE load_parameter_value
--
CREATE PROCEDURE load_parameter_value(parameter_id   BIGINT UNSIGNED,
                                      parameter_type CHAR(64))
proc:
BEGIN
  DECLARE v_bool_value   BOOLEAN;
  DECLARE v_int32_value  BIGINT;
  DECLARE v_int32_hex    BOOLEAN;
  DECLARE v_double_value REAL;
  DECLARE v_string_value VARCHAR(512);
  DECLARE v_sequence_nb  INT;
  DECLARE done           BOOLEAN DEFAULT FALSE;

  /* declare cursors */
  DECLARE cur_bool CURSOR FOR
    SELECT value FROM BoolParamValues
    WHERE paramId=parameter_id;
  DECLARE cur_int32 CURSOR FOR
    SELECT value,hex FROM Int32ParamValues
    WHERE paramId=parameter_id;
  DECLARE cur_vint32 CURSOR FOR
    SELECT value,sequenceNb,hex FROM VInt32ParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  DECLARE cur_uint32 CURSOR FOR
    SELECT value,hex FROM UInt32ParamValues
    WHERE paramId=parameter_id;
  DECLARE cur_vuint32 CURSOR FOR
    SELECT value,sequenceNb,hex FROM VUInt32ParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  DECLARE cur_double CURSOR FOR
    SELECT value FROM DoubleParamValues
    WHERE paramId=parameter_id;
  DECLARE cur_vdouble CURSOR FOR
    SELECT value,sequenceNb FROM VDoubleParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  DECLARE cur_string CURSOR FOR
    SELECT value FROM StringParamValues
    WHERE paramId=parameter_id;
  DECLARE cur_vstring CURSOR FOR
    SELECT value,sequenceNb FROM VStringParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  DECLARE cur_inputtag CURSOR FOR
    SELECT value FROM InputTagParamValues
    WHERE paramId=parameter_id;
  DECLARE cur_vinputtag CURSOR FOR
    SELECT value,sequenceNb FROM VInputTagParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  DECLARE cur_eventid CURSOR FOR
    SELECT value FROM EventIDParamValues
    WHERE paramId=parameter_id;
  DECLARE cur_veventid CURSOR FOR
    SELECT value,sequenceNb FROM VEventIDParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  DECLARE cur_fileinpath CURSOR FOR
    SELECT value FROM FileInPathParamValues
    WHERE paramId=parameter_id;

  /* declare error handlers */
  DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

  /** load bool values */
  IF parameter_type='bool' THEN
    OPEN cur_bool;
    FETCH cur_bool INTO v_bool_value;
    IF done=FALSE THEN
      INSERT INTO tmp_boolean_table VALUES(parameter_id,v_bool_value);
    END IF;
    CLOSE cur_bool;
    LEAVE proc;
  END IF;

  /** load int32 values */
  IF parameter_type='int32' THEN
    OPEN cur_int32;
    FETCH cur_int32 INTO v_int32_value,v_int32_hex;
    IF done=FALSE THEN
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,NULL,v_int32_hex);
    END IF;
    CLOSE cur_int32;
    LEAVE proc;
  END IF;

  /** load vint32 values */
  IF parameter_type='vint32' THEN
    OPEN cur_vint32;
    FETCH cur_vint32 INTO v_int32_value,v_sequence_nb,v_int32_hex;
    WHILE done=FALSE DO
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,v_sequence_nb,v_int32_hex);
      FETCH cur_vint32 INTO v_int32_value,v_sequence_nb,v_int32_hex;
    END WHILE;
    CLOSE cur_vint32;
    LEAVE proc;
  END IF;

  /** load uint32 values */
  IF parameter_type='uint32' THEN
    OPEN cur_uint32;
    FETCH cur_uint32 INTO v_int32_value,v_int32_hex;
    IF done=FALSE THEN
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,NULL,v_int32_hex);
    END IF;
    CLOSE cur_uint32;
    LEAVE proc;
  END IF;

  /** load vuint32 values */
  IF parameter_type='vuint32' THEN
    OPEN cur_vuint32;
    FETCH cur_vuint32 INTO v_int32_value,v_sequence_nb,v_int32_hex;
    WHILE done=FALSE DO
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,v_sequence_nb,v_int32_hex);
      FETCH cur_vuint32 INTO v_int32_value,v_sequence_nb,v_int32_hex;
    END WHILE;
    CLOSE cur_vuint32;
    LEAVE proc;
  END IF;

  /** load double values */
  IF parameter_type='double' THEN
    OPEN cur_double;
    FETCH cur_double INTO v_double_value;
    IF done=FALSE THEN
      INSERT INTO tmp_real_table VALUES(parameter_id,v_double_value,NULL);
    END IF;
    CLOSE cur_double;
    LEAVE proc;
  END IF;

  /** load vdouble values */
  IF parameter_type='vdouble' THEN
    OPEN cur_vdouble;
    FETCH cur_vdouble INTO v_double_value,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_real_table
        VALUES(parameter_id,v_double_value,v_sequence_nb);
      FETCH cur_vdouble INTO v_double_value,v_sequence_nb;
    END WHILE;
    CLOSE cur_vdouble;
    LEAVE proc;
  END IF;

  /** load string values */
  IF parameter_type='string' THEN
    OPEN cur_string;
    FETCH cur_string INTO v_string_value;
    IF done=FALSE THEN
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_string;
    LEAVE proc;
  END IF;

  /** load vstring values */
  IF parameter_type='vstring' THEN
    OPEN cur_vstring;
    FETCH cur_vstring INTO v_string_value,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_string_table
        VALUES(parameter_id,v_string_value,v_sequence_nb);
      FETCH cur_vstring INTO v_string_value,v_sequence_nb;
    END WHILE;
    CLOSE cur_vstring;
    LEAVE proc;
  END IF;

  /** load inputtag values */
  IF parameter_type='InputTag' THEN
    OPEN cur_inputtag;
    FETCH cur_inputtag INTO v_string_value;
    IF done=FALSE THEN
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_inputtag;
    LEAVE proc;
  END IF;

  /** load vinputtag values */
  IF parameter_type='VInputTag' THEN
    OPEN cur_vinputtag;
    FETCH cur_vinputtag INTO v_string_value,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_string_table
        VALUES(parameter_id,v_string_value,v_sequence_nb);
      FETCH cur_vinputtag INTO v_string_value,v_sequence_nb;
    END WHILE;
    CLOSE cur_vinputtag;
    LEAVE proc;
  END IF;

  /** load eventid values */
  IF parameter_type='EventID' THEN
    OPEN cur_eventid;
    FETCH cur_eventid INTO v_string_value;
    IF done=FALSE THEN
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_eventid;
    LEAVE proc;
  END IF;

  /** load veventid values */
  IF parameter_type='VEventID' THEN
    OPEN cur_veventid;
    FETCH cur_veventid INTO v_string_value,v_sequence_nb;
    WHILE done=FALSE DO
      INSERT INTO tmp_string_table
        VALUES(parameter_id,v_string_value,v_sequence_nb);
      FETCH cur_veventid INTO v_string_value,v_sequence_nb;
    END WHILE;
    CLOSE cur_veventid;
    LEAVE proc;
  END IF;

  /** load fileinpath values */
  IF parameter_type='FileInPath' THEN
    OPEN cur_fileinpath;
    FETCH cur_fileinpath INTO v_string_value;
    IF done=FALSE THEN
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_fileinpath;
    LEAVE proc;
  END IF;

  SELECT 'UNKNOWN PARAMETER TYPE',parameter_type;
END;
//


-- reset delimiter to ';'
DELIMITER ; //
