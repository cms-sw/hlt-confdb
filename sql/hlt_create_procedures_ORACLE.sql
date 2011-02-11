--
-- Oracle Stored Procedures
--


--
-- create global temporary tables
--
CREATE GLOBAL TEMPORARY TABLE tmp_template_table
(
  template_id	   NUMBER,
  template_type    VARCHAR2(64),
  template_name    VARCHAR2(128),
  template_cvstag  VARCHAR2(32),
  template_pkgid   NUMBER
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_instance_table
(
  instance_id       NUMBER,
  template_id       NUMBER,
  instance_type     VARCHAR2(64),
  instance_name     VARCHAR2(128),
  flag              NUMBER(1),
  sequence_nb       NUMBER
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_parameter_table
(
  parameter_id      NUMBER,
  parameter_type    VARCHAR2(64),
  parameter_name    VARCHAR2(128),
  parameter_trkd    NUMBER(1),
  parameter_seqnb   NUMBER,
  parent_id         NUMBER
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_boolean_table
(
  parameter_id		NUMBER,
  parameter_value   	NUMBER(1)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_int_table
(
  parameter_id	  NUMBER,
  parameter_value NUMBER,
  sequence_nb     NUMBER,
  hex		  NUMBER(1)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_real_table
(
  parameter_id	  NUMBER,
  parameter_value FLOAT,
  sequence_nb     NUMBER
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_string_table
(
  parameter_id	    NUMBER,
  parameter_value   VARCHAR2(1024),
  sequence_nb       NUMBER
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_path_entries
(
  path_id           NUMBER,
  entry_id          NUMBER,
  sequence_nb       NUMBER,
  entry_type        VARCHAR2(64),
  operator          NUMBER
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_sequence_entries
(
  sequence_id       NUMBER,
  entry_id          NUMBER,
  sequence_nb       NUMBER,
  entry_type        VARCHAR2(64)
) ON COMMIT PRESERVE ROWS;



--
-- PROCDEDURE load_parameter_value
--
CREATE PROCEDURE load_parameter_value(parameter_id   NUMBER,
                                      parameter_type CHAR)
AS
  v_bool_value   NUMBER(1);
  v_int32_value  NUMBER;
  v_int32_hex    NUMBER(1);
  v_int64_value  NUMBER;
  v_int64_hex    NUMBER(1);
  v_double_value FLOAT;
  v_string_value VARCHAR2(1024);
  v_sequence_nb  PLS_INTEGER;

  /* declare cursors */
  CURSOR cur_bool IS
    SELECT value FROM BoolParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_int32 IS
    SELECT value,hex FROM Int32ParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vint32 IS
    SELECT value,sequenceNb,hex FROM VInt32ParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_uint32 IS
    SELECT value,hex FROM UInt32ParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vuint32 IS
    SELECT value,sequenceNb,hex FROM VUInt32ParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_int64 IS
    SELECT value,hex FROM Int64ParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vint64 IS
    SELECT value,sequenceNb,hex FROM VInt64ParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_uint64 IS
    SELECT value,hex FROM UInt64ParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vuint64 IS
    SELECT value,sequenceNb,hex FROM VUInt64ParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_double IS
    SELECT value FROM DoubleParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vdouble IS
    SELECT value,sequenceNb FROM VDoubleParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_string IS
    SELECT value FROM StringParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vstring IS
    SELECT value,sequenceNb FROM VStringParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_inputtag IS
    SELECT value FROM InputTagParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vinputtag IS
    SELECT value,sequenceNb FROM VInputTagParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_eventid IS
    SELECT value FROM EventIDParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_veventid IS
    SELECT value,sequenceNb FROM VEventIDParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_fileinpath IS
    SELECT value FROM FileInPathParamValues
    WHERE paramId=parameter_id;

BEGIN

  /** load bool values */
  IF parameter_type='bool'
  THEN
    OPEN cur_bool;
    FETCH cur_bool INTO v_bool_value;
    IF cur_bool%FOUND THEN
      INSERT INTO tmp_boolean_table VALUES(parameter_id,v_bool_value);
    END IF;
    CLOSE cur_bool;
  /** load int32 values */
  ELSIF parameter_type='int32'
  THEN
    OPEN cur_int32;
    FETCH cur_int32 INTO v_int32_value,v_int32_hex;
    IF cur_int32%FOUND THEN
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,NULL,v_int32_hex);
    END IF;
    CLOSE cur_int32;
  /** load vint32 values */
  ELSIF parameter_type='vint32'
  THEN
    OPEN cur_vint32;
    LOOP 
      FETCH cur_vint32 INTO v_int32_value,v_sequence_nb,v_int32_hex;
      EXIT WHEN cur_vint32%NOTFOUND;
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,v_sequence_nb,v_int32_hex);
    END LOOP;
    CLOSE cur_vint32;
  /** load uint32 values */
  ELSIF parameter_type='uint32'
  THEN
    OPEN cur_uint32;
    FETCH cur_uint32 INTO v_int32_value,v_int32_hex;
    IF cur_uint32%FOUND THEN
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,NULL,v_int32_hex);
    END IF;
    CLOSE cur_uint32;
  /** load vuint32 values */
  ELSIF parameter_type='vuint32'
  THEN
    OPEN cur_vuint32;
    LOOP
      FETCH cur_vuint32 INTO v_int32_value,v_sequence_nb,v_int32_hex;
      EXIT WHEN cur_vuint32%NOTFOUND;
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,v_sequence_nb,v_int32_hex);
    END LOOP;
    CLOSE cur_vuint32;
  /** load int64 values */
  ELSIF parameter_type='int64'
  THEN
    OPEN cur_int64;
    FETCH cur_int64 INTO v_int64_value,v_int64_hex;
    IF cur_int64%FOUND THEN
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int64_value,NULL,v_int64_hex);
    END IF;
    CLOSE cur_int64;
  /** load vint64 values */
  ELSIF parameter_type='vint64'
  THEN
    OPEN cur_vint64;
    LOOP 
      FETCH cur_vint64 INTO v_int64_value,v_sequence_nb,v_int64_hex;
      EXIT WHEN cur_vint64%NOTFOUND;
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int64_value,v_sequence_nb,v_int64_hex);
    END LOOP;
    CLOSE cur_vint64;
  /** load uint64 values */
  ELSIF parameter_type='uint64'
  THEN
    OPEN cur_uint64;
    FETCH cur_uint64 INTO v_int64_value,v_int64_hex;
    IF cur_uint64%FOUND THEN
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int64_value,NULL,v_int64_hex);
    END IF;
    CLOSE cur_uint64;
  /** load vuint64 values */
  ELSIF parameter_type='vuint64'
  THEN
    OPEN cur_vuint64;
    LOOP
      FETCH cur_vuint64 INTO v_int64_value,v_sequence_nb,v_int64_hex;
      EXIT WHEN cur_vuint64%NOTFOUND;
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int64_value,v_sequence_nb,v_int64_hex);
    END LOOP;
    CLOSE cur_vuint64;
  /** load double values */
  ELSIF parameter_type='double'
  THEN
    OPEN cur_double;
    FETCH cur_double INTO v_double_value;
    IF cur_double%FOUND THEN
      INSERT INTO tmp_real_table VALUES(parameter_id,v_double_value,NULL);
    END IF;
    CLOSE cur_double;
  /** load vdouble values */
  ELSIF parameter_type='vdouble'
  THEN
    OPEN cur_vdouble;
    LOOP 
      FETCH cur_vdouble INTO v_double_value,v_sequence_nb;
      EXIT WHEN cur_vdouble%NOTFOUND;
      INSERT INTO tmp_real_table
        VALUES(parameter_id,v_double_value,v_sequence_nb);
    END LOOP;
    CLOSE cur_vdouble;
  /** load string values */
  ELSIF parameter_type='string'
  THEN
    OPEN cur_string;
    FETCH cur_string INTO v_string_value;
    IF cur_string%FOUND THEN
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_string;
  /** load vstring values */
  ELSIF parameter_type='vstring'
  THEN
    OPEN cur_vstring;
    LOOP
      FETCH cur_vstring INTO v_string_value,v_sequence_nb;
      EXIT WHEN cur_vstring%NOTFOUND;
      INSERT INTO tmp_string_table
        VALUES(parameter_id,v_string_value,v_sequence_nb);
    END LOOP;
    CLOSE cur_vstring;
  /** load inputtag values */
  ELSIF parameter_type='InputTag'
  THEN
    OPEN cur_inputtag;
    FETCH cur_inputtag INTO v_string_value;
    IF cur_inputtag%FOUND THEN
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_inputtag;
  /** load vinputtag values */
  ELSIF parameter_type='VInputTag'
  THEN
    OPEN cur_vinputtag;
    LOOP
      FETCH cur_vinputtag INTO v_string_value,v_sequence_nb;
      EXIT WHEN cur_vinputtag%NOTFOUND;
      INSERT INTO tmp_string_table
        VALUES(parameter_id,v_string_value,v_sequence_nb);
    END LOOP;
    CLOSE cur_vinputtag;
  /** load eventid values */
  ELSIF parameter_type='EventID'
  THEN
    OPEN cur_eventid;
    FETCH cur_eventid INTO v_string_value;
    IF cur_eventid%FOUND THEN
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_eventid;
  /** load veventid values */
  ELSIF parameter_type='VEventID'
  THEN
    OPEN cur_veventid;
    LOOP
      FETCH cur_veventid INTO v_string_value,v_sequence_nb;
      EXIT WHEN cur_veventid%NOTFOUND;
      INSERT INTO tmp_string_table
        VALUES(parameter_id,v_string_value,v_sequence_nb);
    END LOOP;
    CLOSE cur_veventid;
  /** load fileinpath values */
  ELSIF parameter_type='FileInPath'
  THEN
    OPEN cur_fileinpath;
    FETCH cur_fileinpath INTO v_string_value;
    IF cur_fileinpath%FOUND THEN
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_fileinpath;
  END IF;
END;
/


--
-- PROCEDURE load_parameters
--
CREATE PROCEDURE load_parameters(parent_id IN NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;

  /* cursor for parameters */
  CURSOR cur_parameters IS
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
  CURSOR cur_psets IS
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
  CURSOR cur_vpsets IS
    SELECT VecParameterSets.superId,
           VecParameterSets.name,
           VecParameterSets.tracked,
           SuperIdVecParamSetAssoc.sequenceNb
    FROM VecParameterSets
    JOIN SuperIdVecParamSetAssoc
    ON SuperIdVecParamSetAssoc.vpsetId = VecParameterSets.superId
    WHERE SuperIdVecParamSetAssoc.superId = parent_id
    ORDER BY SuperIdVecParamSetAssoc.sequenceNb ASC;

BEGIN

  /* load the parameters and fill them into temporary param table */
  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
             v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id);
    load_parameter_value(v_parameter_id,v_parameter_type);
  END LOOP;
  CLOSE cur_parameters;

  /* load psets and fill them into temporary param table */
  OPEN cur_psets;
  LOOP
    FETCH cur_psets
      INTO v_parameter_id,v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
    EXIT WHEN cur_psets%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,'PSet',
             v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id);
    load_parameters(v_parameter_id);
  END LOOP;
  CLOSE cur_psets;

  /* load vpsets and fill them into temporary param table */
  OPEN cur_vpsets;
  LOOP
    FETCH cur_vpsets
      INTO v_parameter_id,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb;
    EXIT WHEN cur_vpsets%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,'VPSet',
             v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id);
    load_parameters(v_parameter_id);
  END LOOP;
  CLOSE cur_vpsets;
END;
/


--
-- PROCEDURE load_template
--
CREATE PROCEDURE load_template(release_id IN NUMBER,
                               template_name IN CHAR)
AS
  v_template_id     NUMBER;
  v_template_type   VARCHAR2(64);
  v_template_name   VARCHAR2(128);
  v_template_cvstag VARCHAR2(32);
  v_template_pkgid  NUMBER;
  template_found    BOOLEAN := FALSE;

  /* cursor for edsource templates */
  CURSOR cur_edsource_templates IS
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
  CURSOR cur_essource_templates IS
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
  CURSOR cur_esmodule_templates IS
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
  CURSOR cur_service_templates IS
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
  CURSOR cur_module_templates IS
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

BEGIN

  /* prepare temporary tables */
  execute immediate 'DELETE FROM tmp_template_table';
  execute immediate 'DELETE FROM tmp_parameter_table';
  execute immediate 'DELETE FROM tmp_boolean_table';
  execute immediate 'DELETE FROM tmp_int_table';
  execute immediate 'DELETE FROM tmp_real_table';
  execute immediate 'DELETE FROM tmp_string_table';

  /* load edsource templates */
  OPEN cur_edsource_templates;
  LOOP
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_edsource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'EDSource',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
    template_found := TRUE;
  END LOOP;
  CLOSE cur_edsource_templates;


  /* load essource templates */
  IF template_found=FALSE THEN
    OPEN cur_essource_templates;
    LOOP
      FETCH cur_essource_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
      EXIT WHEN cur_essource_templates%NOTFOUND;
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'ESSource',
               v_template_name,v_template_cvstag,v_template_pkgid);
      load_parameters(v_template_id);
      template_found := TRUE;
    END LOOP;
    CLOSE cur_essource_templates;
  END IF;

  /* load esmodule templates */
  IF template_found=FALSE THEN
    OPEN cur_esmodule_templates;
    LOOP
      FETCH cur_esmodule_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
      EXIT WHEN cur_esmodule_templates%NOTFOUND;
      INSERT INTO tmp_template_table
         VALUES(v_template_id,'ESModule',
                v_template_name,v_template_cvstag,v_template_pkgid);
      load_parameters(v_template_id);
      template_found := TRUE;
    END LOOP;
    CLOSE cur_esmodule_templates;
  END IF;

  /* load service templates */
  IF template_found=FALSE THEN
    OPEN cur_service_templates;
    LOOP
      FETCH cur_service_templates
        INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
      EXIT WHEN cur_service_templates%NOTFOUND;
      INSERT INTO tmp_template_table
        VALUES(v_template_id,'Service',
               v_template_name,v_template_cvstag,v_template_pkgid);
      load_parameters(v_template_id);
      template_found := TRUE;
    END LOOP;
    CLOSE cur_service_templates;
  END IF;

  /* load module templates */
  IF template_found=FALSE THEN
    OPEN cur_module_templates;
    LOOP
      FETCH cur_module_templates
        INTO v_template_id,v_template_name,
             v_template_cvstag,v_template_pkgid,v_template_type;
      EXIT WHEN cur_module_templates%NOTFOUND;
      INSERT INTO tmp_template_table
        VALUES(v_template_id,v_template_type,
               v_template_name,v_template_cvstag,v_template_pkgid);
      load_parameters(v_template_id);
      template_found := TRUE;
    END LOOP;
    CLOSE cur_module_templates;
  END IF;

END;  
/



--
-- PROCEDURE load_templates
--
CREATE PROCEDURE load_templates(release_id IN NUMBER)
AS
  v_template_id     NUMBER;
  v_template_type   VARCHAR2(64);
  v_template_name   VARCHAR2(128);
  v_template_cvstag VARCHAR2(32);
  v_template_pkgid  NUMBER;

  /* cursor for edsource templates */
  CURSOR cur_edsource_templates IS
    SELECT EDSourceTemplates.superId,
           EDSourceTemplates.name,
           EDSourceTemplates.cvstag,
	   EDSourceTemplates.packageId
    FROM EDSourceTemplates
    JOIN SuperIdReleaseAssoc
    ON EDSourceTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

  /* cursor for essource templates */
  CURSOR cur_essource_templates IS
    SELECT ESSourceTemplates.superId,
           ESSourceTemplates.name,
           ESSourceTemplates.cvstag,
	   ESSourceTemplates.packageId
    FROM ESSourceTemplates
    JOIN SuperIdReleaseAssoc
    ON ESSourceTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

  /* cursor for esmodule templates */
  CURSOR cur_esmodule_templates IS
    SELECT ESModuleTemplates.superId,
           ESModuleTemplates.name,
           ESModuleTemplates.cvstag,
	   ESModuleTemplates.packageId
    FROM ESModuleTemplates
    JOIN SuperIdReleaseAssoc
    ON ESModuleTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

  /* cursor for service templates */
  CURSOR cur_service_templates IS
    SELECT ServiceTemplates.superId,
           ServiceTemplates.name,
           ServiceTemplates.cvstag,
	   ServiceTemplates.packageId
    FROM ServiceTemplates
    JOIN SuperIdReleaseAssoc
    ON ServiceTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

  /* cursor for module templates */
  CURSOR cur_module_templates IS
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

BEGIN
  /* prepare temporary tables */
  execute immediate 'DELETE FROM tmp_template_table';
  execute immediate 'DELETE FROM tmp_parameter_table';
  execute immediate 'DELETE FROM tmp_boolean_table';
  execute immediate 'DELETE FROM tmp_int_table';
  execute immediate 'DELETE FROM tmp_real_table';
  execute immediate 'DELETE FROM tmp_string_table';
  
  /* load edsource templates */
  OPEN cur_edsource_templates;
  LOOP
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_edsource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'EDSource',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);   
  END LOOP;
  CLOSE cur_edsource_templates;

  /* load essource templates */
  OPEN cur_essource_templates;
  LOOP
    FETCH cur_essource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_essource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'ESSource',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_essource_templates;

  /* load esmodule templates */
  OPEN cur_esmodule_templates;
  LOOP
    FETCH cur_esmodule_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_esmodule_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'ESModule',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_esmodule_templates;

  /* load service templates */
  OPEN cur_service_templates;
  LOOP 
    FETCH cur_service_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_service_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'Service',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_service_templates;

  /* load module templates */
  OPEN cur_module_templates;
  LOOP
    FETCH cur_module_templates
      INTO v_template_id,v_template_name,
           v_template_cvstag,v_template_pkgid,v_template_type;
    EXIT WHEN cur_module_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,v_template_type,
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_module_templates;

END;  
/


--
-- PROCEDURE load_templates_for_config
--
CREATE PROCEDURE load_templates_for_config(config_id IN NUMBER)
AS
  v_template_id     NUMBER;
  v_template_type   VARCHAR2(64);
  v_template_name   VARCHAR2(128);
  v_template_cvstag VARCHAR2(32);
  v_template_pkgid  NUMBER;

  /* cursor for edsource templates */
  CURSOR cur_edsource_templates IS
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
  CURSOR cur_essource_templates IS
    SELECT DISTINCT
      ESSourceTemplates.superId,
      ESSourceTemplates.name,
      ESSourceTemplates.cvstag,
      ESsourceTemplates.packageId
    FROM ESSourceTemplates
    JOIN ESSources
    ON ESSources.templateId = ESSourceTemplates.superId
    JOIN ConfigurationESSourceAssoc
    ON ESSources.superId = ConfigurationESSourceAssoc.essourceId
    WHERE ConfigurationESSourceAssoc.configId = config_id;

  /* cursor for esmodule templates */
  CURSOR cur_esmodule_templates IS
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
  CURSOR cur_service_templates IS
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
  CURSOR cur_module_templates_paths IS
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
  CURSOR cur_module_templates_sequences IS
    SELECT DISTINCT
      ModuleTemplates.superId,
      ModuleTemplates.name,
      ModuleTemplates.cvstag,
      ModuleTemplates.packageId,
      ModuleTypes.type
    FROM ModuleTemplates
    JOIN ModuleTypes
    ON ModuleTemplates.typeId = ModuleTypes.typeId
    JOIN Modules
    ON Modules.templateId = ModuleTemplates.superId
    JOIN SequenceModuleAssoc
    ON SequenceModuleAssoc.moduleId = Modules.superId
    JOIN ConfigurationSequenceAssoc
    ON SequenceModuleAssoc.sequenceId=ConfigurationSequenceAssoc.sequenceId
    WHERE ConfigurationSequenceAssoc.configId = config_id;

BEGIN

  /* prepare temporary tables */
  execute immediate 'DELETE FROM tmp_template_table';
  execute immediate 'DELETE FROM tmp_parameter_table';
  execute immediate 'DELETE FROM tmp_boolean_table';
  execute immediate 'DELETE FROM tmp_int_table';
  execute immediate 'DELETE FROM tmp_real_table';
  execute immediate 'DELETE FROM tmp_string_table';

  /* load edsource templates */
  OPEN cur_edsource_templates;
  LOOP
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_edsource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'EDSource',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_edsource_templates;

  /* load essource templates */
  OPEN cur_essource_templates;
  LOOP
    FETCH cur_essource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_essource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'ESSource',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_essource_templates;

  /* load esmodule templates */
  OPEN cur_esmodule_templates;
  LOOP
    FETCH cur_esmodule_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_esmodule_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'ESModule',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_esmodule_templates;

  /* load service templates */
  OPEN cur_service_templates;
  LOOP
    FETCH cur_service_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_service_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'Service',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_service_templates;

  /* load module templates from *paths* */
  OPEN cur_module_templates_paths;
  LOOP
    FETCH cur_module_templates_paths
      INTO v_template_id,v_template_name,
           v_template_cvstag,v_template_pkgid,v_template_type;
    EXIT WHEN cur_module_templates_paths%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,v_template_type,
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_module_templates_paths;

  /* load module templates from *sequences* */
  OPEN cur_module_templates_sequences;
  LOOP
    FETCH cur_module_templates_sequences
      INTO v_template_id,v_template_name,
           v_template_cvstag,v_template_pkgid,v_template_type;
    EXIT WHEN cur_module_templates_sequences%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,v_template_type,
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_module_templates_sequences;

END;  
/


--
-- PROCEDURE load_configuration
--
CREATE PROCEDURE load_configuration(config_id IN NUMBER)
AS
  v_instance_id     NUMBER;
  v_template_id     NUMBER;
  v_instance_type   VARCHAR2(64);
  v_instance_name   VARCHAR2(128);
  v_pset_is_trkd    NUMBER(1);
  v_endpath         NUMBER(1);
  v_prefer          NUMBER(1);
  v_parent_id       NUMBER;
  v_child_id        NUMBER;
  v_sequence_nb     NUMBER;
  v_operator        NUMBER;

  /* cursor for global psets */
  CURSOR cur_global_psets IS
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
  CURSOR cur_edsources IS
    SELECT
      EDSources.superId,
      EDSources.templateId,
      ConfigurationEDSourceAssoc.sequenceNb
    FROM EDSources
    JOIN ConfigurationEDSourceAssoc
    ON EDSources.superId = ConfigurationEDSourceAssoc.edsourceId
    WHERE ConfigurationEDSourceAssoc.configId = config_id;

  /* cursor for essources */
  CURSOR cur_essources IS
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
  CURSOR cur_esmodules IS
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
  CURSOR cur_services IS
    SELECT
      Services.superId,
      Services.templateId,
      ConfigurationServiceAssoc.sequenceNb
    FROM Services
    JOIN ConfigurationServiceAssoc
    ON   Services.superId = ConfigurationServiceAssoc.serviceId
    WHERE ConfigurationServiceAssoc.configId = config_id;

  /* cursor for modules from configuration *paths* */
  CURSOR cur_modules_from_paths IS
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
  CURSOR cur_modules_from_sequences IS
    SELECT
      Modules.superId,
      Modules.templateId,
      Modules.name
    FROM Modules
    JOIN SequenceModuleAssoc
    ON SequenceModuleAssoc.moduleId = Modules.superId
    JOIN ConfigurationSequenceAssoc
    ON SequenceModuleAssoc.sequenceId=ConfigurationSequenceAssoc.sequenceId
    WHERE ConfigurationSequenceAssoc.configId = config_id;

  /* cursor for paths */
  CURSOR cur_paths IS
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
  CURSOR cur_sequences IS
    SELECT
      Sequences.sequenceId,
      Sequences.name,
      ConfigurationSequenceAssoc.sequenceNb
    FROM Sequences
    JOIN ConfigurationSequenceAssoc
    ON Sequences.sequenceId = ConfigurationSequenceAssoc.sequenceId
    WHERE ConfigurationSequenceAssoc.configId = config_id;

  /* cursor for path-path associations */
  CURSOR cur_path_path IS
    SELECT
      PathInPathAssoc.parentPathId,
      PathInPathAssoc.childPathId,
      PathInPathAssoc.sequenceNb,
      PathInPathAssoc.operator
    FROM PathInPathAssoc
    JOIN ConfigurationPathAssoc
    ON PathInPathAssoc.parentPathId = ConfigurationPathAssoc.pathId
    WHERE ConfigurationPathAssoc.configId = config_id;

  /* cursor for path-sequence associations */
  CURSOR cur_path_sequence IS
    SELECT
      PathSequenceAssoc.pathId,
      PathSequenceAssoc.sequenceId,
      PathSequenceAssoc.sequenceNb,
      PathSequenceAssoc.operator
    FROM PathSequenceAssoc
    JOIN ConfigurationPathAssoc
    ON PathSequenceAssoc.pathId = ConfigurationPathAssoc.pathId
    WHERE ConfigurationPathAssoc.configId = config_id;

  /* cursor for path-module associations */
  CURSOR cur_path_module IS
    SELECT
      PathModuleAssoc.pathId,
      PathModuleAssoc.moduleId,
      PathModuleAssoc.sequenceNb,
      PathModuleAssoc.operator
    FROM PathModuleAssoc
    JOIN ConfigurationPathAssoc
    ON PathModuleAssoc.pathId = ConfigurationPathAssoc.pathId
    WHERE ConfigurationPathAssoc.configId = config_id;

/* cursor for path-outputmodule associations */
  CURSOR cur_path_outputmod IS
    SELECT
      PathOutputModAssoc.pathId,
      PathOutputModAssoc.outputModuleId,
      PathOutputModAssoc.sequenceNb,
      PathOutputModAssoc.operator
    FROM PathOutputModAssoc
    JOIN ConfigurationPathAssoc
    ON PathOutputModAssoc.pathId = ConfigurationPathAssoc.pathId
    WHERE ConfigurationPathAssoc.configId = config_id;


  /* cursor for sequence-sequence associations */
  CURSOR cur_sequence_sequence IS
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
  CURSOR cur_sequence_module IS
    SELECT
      SequenceModuleAssoc.sequenceId,
      SequenceModuleAssoc.moduleId,
      SequenceModuleAssoc.sequenceNb
    FROM SequenceModuleAssoc
    JOIN ConfigurationSequenceAssoc
    ON SequenceModuleAssoc.sequenceId =
       ConfigurationSequenceAssoc.sequenceId
    WHERE ConfigurationSequenceAssoc.configId = config_id;


 /* cursor for sequence-outputmod associations */
  CURSOR cur_sequence_outputmod IS
    SELECT
      SequenceOutputModAssoc.sequenceId,
      SequenceOutputModAssoc.outputModuleId,
      SequenceOutputModAssoc.sequenceNb
    FROM SequenceOutputModAssoc
    JOIN ConfigurationSequenceAssoc
    ON SequenceOutputModAssoc.sequenceId =
       ConfigurationSequenceAssoc.sequenceId
    WHERE ConfigurationSequenceAssoc.configId = config_id;

BEGIN

  execute immediate 'DELETE FROM tmp_instance_table';
  execute immediate 'DELETE FROM tmp_parameter_table';
  execute immediate 'DELETE FROM tmp_boolean_table';
  execute immediate 'DELETE FROM tmp_int_table';
  execute immediate 'DELETE FROM tmp_real_table';
  execute immediate 'DELETE FROM tmp_string_table';
  execute immediate 'DELETE FROM tmp_path_entries';
  execute immediate 'DELETE FROM tmp_sequence_entries';

  /* load global psets */
  OPEN cur_global_psets;
  LOOP
    FETCH cur_global_psets
      INTO v_instance_id,v_instance_name,v_pset_is_trkd,v_sequence_nb;
    EXIT WHEN cur_global_psets%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,NULL,'PSet',v_instance_name,v_pset_is_trkd,v_sequence_nb);
    load_parameters(v_instance_id);
  END LOOP;
  CLOSE cur_global_psets;
 
  /* load edsources */
  OPEN cur_edsources;
  LOOP
    FETCH cur_edsources INTO v_instance_id,v_template_id,v_sequence_nb;
    EXIT WHEN cur_edsources%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,v_template_id,'EDSource',NULL,NULL,v_sequence_nb);
    load_parameters(v_instance_id);
  END LOOP;
  CLOSE cur_edsources;

  /* load essources */
  OPEN cur_essources;
  LOOP
    FETCH cur_essources
      INTO v_instance_id,v_template_id,v_instance_name,v_prefer,v_sequence_nb;
    EXIT WHEN cur_essources%NOTFOUND;
    INSERT INTO tmp_instance_table
    VALUES(v_instance_id,v_template_id,'ESSource',
           v_instance_name,v_prefer,v_sequence_nb);
    load_parameters(v_instance_id);
  END LOOP;
  CLOSE cur_essources;

  /* load esmodules */
  OPEN cur_esmodules;
  LOOP
    FETCH cur_esmodules
      INTO v_instance_id,v_template_id,v_instance_name,v_prefer,v_sequence_nb;
    EXIT WHEN cur_esmodules%NOTFOUND;
    INSERT INTO tmp_instance_table
    VALUES(v_instance_id,v_template_id,'ESModule',
           v_instance_name,v_prefer,v_sequence_nb);
    load_parameters(v_instance_id);
  END LOOP;
  CLOSE cur_esmodules;

  /* load services */
  OPEN cur_services;
  LOOP
    FETCH cur_services INTO v_instance_id,v_template_id,v_sequence_nb;
    EXIT WHEN cur_services%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,v_template_id,'Service',NULL,NULL,v_sequence_nb);
    load_parameters(v_instance_id);
  END LOOP;
  CLOSE cur_services;

  /* load modules from *paths* */
  OPEN cur_modules_from_paths;
  LOOP
    FETCH cur_modules_from_paths
      INTO v_instance_id,v_template_id,v_instance_name;
    EXIT WHEN cur_modules_from_paths%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,v_template_id,'Module',v_instance_name,NULL,NULL);
    load_parameters(v_instance_id);
  END LOOP;
  CLOSE cur_modules_from_paths;

  /* load modules from *sequences* */
  OPEN cur_modules_from_sequences;
  LOOP
    FETCH cur_modules_from_sequences
      INTO v_instance_id,v_template_id,v_instance_name;
    EXIT WHEN cur_modules_from_sequences%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,v_template_id,'Module',v_instance_name,NULL,NULL);
    load_parameters(v_instance_id);
  END LOOP;
  CLOSE cur_modules_from_sequences;

  /* load paths */
  OPEN cur_paths;
  LOOP
    FETCH cur_paths INTO v_instance_id,v_instance_name,v_endpath,v_sequence_nb;
    EXIT WHEN cur_paths%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,NULL,'Path',v_instance_name,v_endpath,v_sequence_nb);
  END LOOP;
  CLOSE cur_paths;

  /* load sequences */
  OPEN cur_sequences;
  LOOP
    FETCH cur_sequences INTO v_instance_id,v_instance_name,v_sequence_nb;
    EXIT WHEN cur_sequences%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,NULL,'Sequence',v_instance_name,NULL,v_sequence_nb);
  END LOOP;
  CLOSE cur_sequences;

  /* load path-path associations */
  OPEN cur_path_path;
  LOOP
    FETCH cur_path_path INTO v_parent_id,v_child_id,v_sequence_nb,v_operator;
    EXIT WHEN cur_path_path%NOTFOUND;
    INSERT INTO tmp_path_entries
      VALUES(v_parent_id,v_child_id,v_sequence_nb,'Path',v_operator);
  END LOOP;
  CLOSE cur_path_path;

  /* load path-sequence associations */
  OPEN cur_path_sequence;
  LOOP
    FETCH cur_path_sequence INTO v_parent_id,v_child_id,v_sequence_nb,v_operator;
    EXIT WHEN cur_path_sequence%NOTFOUND;
    INSERT INTO tmp_path_entries
      VALUES(v_parent_id,v_child_id,v_sequence_nb,'Sequence',v_operator);
  END LOOP;
  CLOSE cur_path_sequence;

  /* load path-outputmodule associations */
  OPEN cur_path_outputmod;
  LOOP
    FETCH cur_path_outputmod INTO v_parent_id,v_child_id,v_sequence_nb,v_operator;
    EXIT WHEN cur_path_outputmod%NOTFOUND;
    INSERT INTO tmp_path_entries
      VALUES(v_parent_id,v_child_id,v_sequence_nb,'OutputModule',v_operator);
      load_parameters(v_child_id);
  END LOOP;
  CLOSE cur_path_outputmod;
  
   /* load path-module associations */
  OPEN cur_path_module;
  LOOP
    FETCH cur_path_module INTO v_parent_id,v_child_id,v_sequence_nb,v_operator;
    EXIT WHEN cur_path_module%NOTFOUND;
    INSERT INTO tmp_path_entries
      VALUES(v_parent_id,v_child_id,v_sequence_nb,'Module',v_operator);
  END LOOP;
  CLOSE cur_path_module;

  /* load sequence-sequence associations */
  OPEN cur_sequence_sequence;
  LOOP
    FETCH cur_sequence_sequence INTO v_parent_id,v_child_id,v_sequence_nb;
    EXIT WHEN cur_sequence_sequence%NOTFOUND;
    INSERT INTO tmp_sequence_entries
      VALUES(v_parent_id,v_child_id,v_sequence_nb,'Sequence');
  END LOOP;
  CLOSE cur_sequence_sequence;

  /* load sequence-module associations */
  OPEN cur_sequence_module;
  LOOP
    FETCH cur_sequence_module INTO v_parent_id,v_child_id,v_sequence_nb;
    EXIT WHEN cur_sequence_module%NOTFOUND;
    INSERT INTO tmp_sequence_entries
      VALUES(v_parent_id,v_child_id,v_sequence_nb,'Module');
  END LOOP;
  CLOSE cur_sequence_module;

 /* load sequence-module associations */
  OPEN cur_sequence_outputmod;
  LOOP
    FETCH cur_sequence_outputmod INTO v_parent_id,v_child_id,v_sequence_nb;
    EXIT WHEN cur_sequence_outputmod%NOTFOUND;
    INSERT INTO tmp_sequence_entries
      VALUES(v_parent_id,v_child_id,v_sequence_nb,'OutputModule');
      load_parameters(v_child_id);
  END LOOP;
  CLOSE cur_sequence_outputmod;

END;  
/


COMMIT;
