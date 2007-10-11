--
-- Oracle Stored Procedure
--


--
-- TYPE declaration(s)
--
CREATE PACKAGE types
AS
  TYPE ref_cursor IS REF CURSOR;
END;


--
-- PROCEDURE load_templates
--
CREATE FUNCTION load_templates(release_id IN NUMBER)
  RETURN types.ref_cursor
AS
  template_cursor    types.ref_cursor;
  v_template_id     NUMBER;
  v_template_type   CHAR(64);
  v_template_name   CHAR(128);
  v_template_cvstag CHAR(32);

  /* cursor for edsource templates */
  CURSOR cur_edsource_templates IS
    SELECT EDSourceTemplates.superId,
           EDSourceTemplates.name,
           EDSourceTemplates.cvstag
    FROM EDSourceTemplates
    JOIN SuperIdReleaseAssoc
    ON EDSourceTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

  /* cursor for essource templates */
  CURSOR cur_essource_templates IS
    SELECT ESSourceTemplates.superId,
           ESSourceTemplates.name,
           ESSourceTemplates.cvstag
    FROM ESSourceTemplates
    JOIN SuperIdReleaseAssoc
    ON ESSourceTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

  /* cursor for esmodule templates */
  CURSOR cur_esmodule_templates IS
    SELECT ESModuleTemplates.superId,
           ESModuleTemplates.name,
           ESModuleTemplates.cvstag
    FROM ESModuleTemplates
    JOIN SuperIdReleaseAssoc
    ON ESModuleTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

  /* cursor for service templates */
  CURSOR cur_service_templates IS
    SELECT ServiceTemplates.superId,
           ServiceTemplates.name,
           ServiceTemplates.cvstag
    FROM ServiceTemplates
    JOIN SuperIdReleaseAssoc
    ON ServiceTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

  /* cursor for module templates */
  CURSOR cur_module_templates IS
    SELECT ModuleTemplates.superId,
           ModuleTemplates.name,
           ModuleTemplates.cvstag,
           ModuleTypes.type
    FROM ModuleTemplates
    JOIN ModuleTypes
    ON   ModuleTemplates.typeId = ModuleTypes.typeId
    JOIN SuperIdReleaseAssoc
    ON ModuleTemplates.superId = SuperIdReleaseAssoc.superId
    WHERE SuperIdReleaseAssoc.releaseId = release_id;

BEGIN
  /* temporary template table */
  CREATE TEMPORARY TABLE tmp_template_table
  (
    template_id	NUMBER,
    template_type    CHAR(64),
    template_name    CHAR(128),
    template_cvstag  CHAR(32)
  );

  /* temporary parameter table */
  CREATE TEMPORARY TABLE tmp_parameter_table
  (
    parameter_id	NUMBER,
    parameter_type    CHAR(64),
    parameter_name    CHAR(128),
    parameter_trkd    NUMBER(1),
    parameter_seqnb   NUMBER,
    parent_id         NUMBER
  );

  /* temporary bool parameter-value table */
  CREATE TEMPORARY TABLE tmp_boolean_table
  (
    parameter_id	NUMBER,
    parameter_value   NUMBER(1)
  );

  /* temporary int32 parameter-value table */
  CREATE TEMPORARY TABLE tmp_int_table
  (
    parameter_id	NUMBER,
    parameter_value   NUMBER,
    sequence_nb       NUMBER
  );

  /* temporary double parameter-value table */
  CREATE TEMPORARY TABLE tmp_real_table
  (
    parameter_id	NUMBER
    parameter_value   FLOAT,
    sequence_nb       NUMBER
  );

  /* temporary string parameter-value table */
  CREATE TEMPORARY TABLE tmp_string_table
  (
    parameter_id	NUMBER,
    parameter_value   VARCHAR2(512),
    sequence_nb       NUMBER
  );

  /* load edsource templates */
  OPEN cur_edsource_templates;
  LOOP
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag;
    EXIT WHEN cur_edsource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'EDSource',v_template_name,v_template_cvstag);
    load_parameters(v_template_id);   
  END LOOP;
  CLOSE cur_edsource_templates;

  /* load essource templates */
  OPEN cur_essource_templates;
  LOOP
    FETCH cur_essource_templates
      INTO v_template_id,v_template_name,v_template_cvstag;
    EXIT WHEN cur_essource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'ESSource',v_template_name,v_template_cvstag);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_essource_templates;

  /* load esmodule templates */
  OPEN cur_esmodule_templates;
  LOOP
    FETCH cur_esmodule_templates
      INTO v_template_id,v_template_name,v_template_cvstag;
    EXIT WHEN cur_esmodule_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'ESModule',v_template_name,v_template_cvstag);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_esmodule_templates;

  /* load service templates */
  OPEN cur_service_templates;
  LOOP 
    FETCH cur_service_templates
      INTO v_template_id,v_template_name,v_template_cvstag;
    EXIT WHEN cur_service_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'Service',v_template_name,v_template_cvstag);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_service_templates;

  /* load module templates */
  OPEN cur_module_templates;
  LOOP
    FETCH cur_module_templates
      INTO v_template_id,v_template_name,
           v_template_cvstag,v_template_type;
    EXIT WHEN cur_module_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,v_template_type,
             v_template_name,v_template_cvstag);
    load_parameters(v_template_id);
  END LOOP;
  CLOSE cur_module_templates;

  /* generate the final result set by selecting the temporary table */
  OPEN template_cursor FOR
    SELECT template_id,template_type,template_name,template_cvstag
    FROM tmp_template_table;
  DROP TEMPORARY TABLE tmp_template_table;
  RETURN template_cursor;
END;  
/


--
-- PROCEDURE load_parameters
--
CREATE PROCEDURE load_parameters(parent_id IN NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  CHAR(64);
  v_parameter_name  CHAR(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb NUMBER;

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
-- PROCDEDURE load_parameter_value
--
CREATE PROCEDURE load_parameter_value(parameter_id IN NUMBER,
                                      parameter_type IN CHAR(64))
AS
  v_bool_value   NUMBER(1);
  v_int32_value  NUMBER;
  v_double_value FLOAT;
  v_string_value VARCHAR2(512);
  v_sequence_nb  NUMBER;

  /* declare cursors */
  CURSOR cur_bool IS
    SELECT value FROM BoolParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_int32 IS
    SELECT value FROM Int32ParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vint32 IS
    SELECT value,sequenceNb FROM VInt32ParamValues
    WHERE paramId=parameter_id
    ORDER BY sequenceNb ASC;
  CURSOR cur_uint32 IS
    SELECT value FROM UInt32ParamValues
    WHERE paramId=parameter_id;
  CURSOR cur_vuint32 IS
    SELECT value,sequenceNb FROM VUInt32ParamValues
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
    IF cur_bool%FOUND
    THEN
      INSERT INTO tmp_boolean_table VALUES(parameter_id,v_bool_value);
    END IF;
    CLOSE cur_bool;
  /** load int32 values */
  ELSIF parameter_type='int32'
  THEN
    OPEN cur_int32;
    FETCH cur_int32 INTO v_int32_value;
    IF cur_int32%FOUND
    THEN
      INSERT INTO tmp_int_table VALUES(parameter_id,v_int32_value,NULL);
    END IF;
    CLOSE cur_int32;
  /** load vint32 values */
  ELSIF parameter_type='vint32'
  THEN
    OPEN cur_vint32;
    LOOP 
      FETCH cur_vint32 INTO v_int32_value,v_sequence_nb;
      EXIT WHEN cur_vint32%NOTFOUND;
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,v_sequence_nb);
    END LOOP;
    CLOSE cur_vint32;
  /** load uint32 values */
  ELSIF parameter_type='uint32'
  THEN
    OPEN cur_uint32;
    FETCH cur_uint32 INTO v_int32_value;
    IF cur_uint32%FOUND
    THEN
      INSERT INTO tmp_int_table VALUES(parameter_id,v_int32_value,NULL);
    END IF;
    CLOSE cur_uint32;
  /** load vuint32 values */
  ELSIF parameter_type='vuint32'
  THEN
    OPEN cur_vuint32;
    LOOP
      FETCH cur_vuint32 INTO v_int32_value,v_sequence_nb;
      EXIT WHEN cur_vuint32%NOTFOUND;
      INSERT INTO tmp_int_table
        VALUES(parameter_id,v_int32_value,v_sequence_nb);
    END LOOP;
    CLOSE cur_vuint32;
  /** load double values */
  ELSIF parameter_type='double'
  THEN
    OPEN cur_double;
    FETCH cur_double INTO v_double_value;
    IF cur_double%FOUND
    THEN
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
    IF cur_string%FOUND
    THEN
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
    IF cur_inputtag%FOUND
    THEN
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
    IF cur_eventid%FOUND
    THEN
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
    IF cur_fileinpath%FOUND
      INSERT INTO tmp_string_table VALUES(parameter_id,v_string_value,NULL);
    END IF;
    CLOSE cur_fileinpath;
  END IF;
END;
/


--
-- PROCEDURE get_parameters
--
CREATE FUNCTION get_parameters()
  RETURN types.ref_cursor
AS
  parameter_cursor types.ref_cursor;
BEGIN
  OPEN parameter_cursor FOR
    SELECT parameter_id,parameter_type,
           parameter_name,parameter_trkd,parameter_seqnb,parent_id
    FROM tmp_parameter_table;
  DROP TEMPORARY TABLE tmp_parameter_table;
  RETURN parameter_cursor;
END;
/


--
-- PROCEDURE get_boolean_values
--
CREATE FUNCTION get_boolean_values()
  RETURN types.ref_cursor
AS
  value_cursor types.ref_cursor;
BEGIN
  OPEN value_cursor FOR
    SELECT parameter_id,parameter_value FROM tmp_boolean_table;
  DROP TEMPORARY TABLE tmp_boolean_table;
END;
/


--
-- PROCEDURE get_int_values
--
CREATE FUNCTION get_int_values()
  RETURN types.ref_cursor
AS
  value_cursor types.ref_cursor;
BEGIN
  OPEN value_cursor FOR
    SELECT parameter_id,parameter_value,sequence_nb FROM tmp_int_table;
  DROP TEMPORARY TABLE tmp_int_table;
  RETURN value_cursor;
END;
/


--
-- PROCEDURE get_real_values
--
CREATE FUNCTION get_real_values()
  RETURN types.ref_cursor
AS
  value_cursor types.ref_cursor;
BEGIN
  OPEN value_cursor FOR
    SELECT parameter_id,parameter_value,sequence_nb FROM tmp_real_table;
  DROP TEMPORARY TABLE tmp_real_table;
  RETURN value_cursor;
END;
/


--
-- PROCEDURE get_string_values
--
CREATE FUNCTION get_string_values()
  RETURN types.ref_cursor
AS
  value_cursor types.ref_cursor;
BEGIN
  OPEN value_cursor FOR
    SELECT parameter_id,parameter_value,sequence_nb FROM tmp_string_table;
  DROP TEMPORARY TABLE tmp_string_table;
  RETURN value_cursor;
END;
/
