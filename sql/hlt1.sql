
--
-- PROCEDURE load_template
--
CREATE OR REPLACE PROCEDURE load_template(release_id IN NUMBER,
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
    SELECT v_edstemplates.id,
           v_edstemplates.name,
           v_edstemplates.cvstag,
	   v_edstemplates.packageId
    FROM v_edstemplates,v_edst2rele, v_SOFTRELEASES 
    where  v_SOFTRELEASES.id=release_id 
    and v_edst2rele.ID_release=v_SOFTRELEASES.id and v_edst2rele.ID_EDSTEMPLATE=v_edstemplates.id
    AND   v_edstemplates.name = template_name;

  /* cursor for essource templates */
  CURSOR cur_essource_templates IS
    SELECT v_esstemplates.id,
           v_esstemplates.name,
           v_esstemplates.cvstag,
	   v_esstemplates.packageId
    FROM v_esstemplates,v_esst2rele,v_softreleases
    where v_softreleases.id=release_id
    and v_esst2rele.id_release=v_softreleases.id and v_esst2rele.id_esstemplate=v_esstemplates.id
    AND   v_esstemplates.name = template_name;

  /* cursor for esmodule templates */
  CURSOR cur_esmodule_templates IS
    SELECT v_esmtemplates.id,
           v_esmtemplates.name,
           v_esmtemplates.cvstag,
	   v_esmtemplates.packageId
    FROM v_esmtemplates,v_esmt2rele,v_softreleases
    where v_softreleases.id=release_id
    and v_esmt2rele.id_release=v_softreleases.id and v_esmt2rele.id_esmtemplate=v_esmtemplates.id
    AND   v_esmtemplates.name = template_name;


  /* cursor for service templates */
  CURSOR cur_service_templates IS
    SELECT v_srvtemplates.id,
           v_srvtemplates.name,
           v_srvtemplates.cvstag,
	   v_srvtemplates.packageId
    FROM v_srvtemplates,v_srvt2rele,v_softreleases
     where v_softreleases.id=release_id
    and v_srvt2rele.id_release=v_softreleases.id and v_srvt2rele.id_srvtemplate=v_srvtemplates.id
    AND   v_srvtemplates.name = template_name;

  /* cursor for module templates */
  CURSOR cur_module_templates IS
    SELECT h_moduletemplates.id,
           v_moduletemplates.name,
           v_moduletemplates.cvstag,
           v_moduletemplates.packageId,
           v_moduletypes.type
    FROM v_moduletemplates,v_moduletypes,v_softreleases,v_modt2rele,h_moduletemplates
    where v_softreleases.id=release_id
    and v_modt2rele.id_release=v_softreleases.id and v_modt2rele.id_modtemplate=v_moduletemplates.id
    and v_moduletypes.id=v_moduletemplates.id_mtype
    and h_moduletemplates.crc32=v_moduletemplates.crc32
    AND   v_moduletemplates.name = template_name;

BEGIN

  /* prepare temporary tables */
  execute immediate 'TRUNCATE TABLE tmp_template_table';
  execute immediate 'TRUNCATE TABLE tmp_parameter_table';
  execute immediate 'TRUNCATE TABLE tmp_boolean_table';
  execute immediate 'TRUNCATE TABLE tmp_int_table';
  execute immediate 'TRUNCATE TABLE tmp_real_table';
  execute immediate 'TRUNCATE TABLE tmp_string_table';

  /* load edsource templates */
  OPEN cur_edsource_templates;
  LOOP
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_edsource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id,'EDSource',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_edst_parameters(v_template_id);
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
      load_esst_parameters(v_template_id); 
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
      load_esmt_parameters(v_template_id); 
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
      load_servt_parameters(v_template_id);
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
      load_modt_parameters(v_template_id);
      template_found := TRUE;
    END LOOP;
    CLOSE cur_module_templates;
  END IF;

END;  
/

--
-- PROCEDURE load_templates
--
CREATE OR REPLACE PROCEDURE load_templates_for_config(config_id IN NUMBER)
AS
  release_id NUMBER;

CURSOR cur_release IS
     SELECT id_release from v_confversions where id=config_id ;

BEGIN 
     OPEN cur_release;
      FETCH cur_release
        INTO release_id;
       load_templates(release_id);
     CLOSE cur_release;
END;
/

--
-- PROCEDURE load_templates
--
CREATE OR REPLACE PROCEDURE load_templates(release_id IN NUMBER)
AS
  v_template_id     NUMBER;
  v_template_type   VARCHAR2(64);
  v_template_name   VARCHAR2(128);
  v_template_cvstag VARCHAR2(32);
  v_template_pkgid  NUMBER;

  /* cursor for edsource templates */
  CURSOR cur_edsource_templates IS
    SELECT v_edstemplates.Id,
           v_edstemplates.name,
           v_edstemplates.cvstag,
           v_edstemplates.packageId
    FROM v_edstemplates,v_edst2rele, v_SOFTRELEASES
    where  v_SOFTRELEASES.id=release_id
    and v_edst2rele.ID_release=v_SOFTRELEASES.id and v_edst2rele.ID_EDSTEMPLATE=v_edstemplates.id;

  /* cursor for essource templates */
  CURSOR cur_essource_templates IS
    SELECT v_esstemplates.Id,
           v_esstemplates.name,
           v_esstemplates.cvstag,
           v_esstemplates.packageId
    FROM v_esstemplates,v_esst2rele,v_softreleases
    where v_softreleases.id=release_id
    and v_esst2rele.id_release=v_softreleases.id and v_esst2rele.id_esstemplate=v_esstemplates.id;


  /* cursor for esmodule templates */
  CURSOR cur_esmodule_templates IS
    SELECT v_esmtemplates.Id,
           v_esmtemplates.name,
           v_esmtemplates.cvstag,
           v_esmtemplates.packageId
    FROM v_esmtemplates,v_esmt2rele,v_softreleases
    where v_softreleases.id=release_id
    and v_esmt2rele.id_release=v_softreleases.id and v_esmt2rele.id_esmtemplate=v_esmtemplates.id;


  /* cursor for service templates */
  CURSOR cur_service_templates IS
    SELECT v_srvtemplates.Id,
           v_srvtemplates.name,
           v_srvtemplates.cvstag,
           v_srvtemplates.packageId
    FROM v_srvtemplates,v_srvt2rele,v_softreleases
     where v_softreleases.id=release_id
    and v_srvt2rele.id_release=v_softreleases.id and v_srvt2rele.id_srvtemplate=v_srvtemplates.id;


  /* cursor for module templates */
  CURSOR cur_module_templates IS
    SELECT h_moduletemplates.Id,
           v_moduletemplates.name,
           v_moduletemplates.cvstag,
           v_moduletemplates.packageId,
           v_moduletypes.type
    FROM v_moduletemplates,v_moduletypes,v_softreleases,v_modt2rele,h_moduletemplates
    where v_softreleases.id=release_id
    and v_modt2rele.id_release=v_softreleases.id and v_modt2rele.id_modtemplate=v_moduletemplates.id
    and h_moduletemplates.crc32=v_moduletemplates.crc32
    and v_moduletypes.id=v_moduletemplates.id_mtype;

BEGIN
  /* prepare temporary tables */
  execute immediate 'TRUNCATE TABLE tmp_template_table';
  execute immediate 'TRUNCATE TABLE tmp_parameter_table';
  execute immediate 'TRUNCATE TABLE tmp_boolean_table';
  execute immediate 'TRUNCATE TABLE tmp_int_table';
  execute immediate 'TRUNCATE TABLE tmp_real_table';
  execute immediate 'TRUNCATE TABLE tmp_string_table';
  
  /* load edsource templates */
  OPEN cur_edsource_templates;
  LOOP
    FETCH cur_edsource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_edsource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id+1000000,'EDSource',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_edst_parameters(v_template_id);   
  END LOOP;
  CLOSE cur_edsource_templates;

  /* load essource templates */
  OPEN cur_essource_templates;
  LOOP
    FETCH cur_essource_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_essource_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id+2000000,'ESSource',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_esst_parameters(v_template_id);
  END LOOP;
  CLOSE cur_essource_templates;

  /* load esmodule templates */
  OPEN cur_esmodule_templates;
  LOOP
    FETCH cur_esmodule_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_esmodule_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id+3000000,'ESModule',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_esmt_parameters(v_template_id);
  END LOOP;
  CLOSE cur_esmodule_templates;

  /* load service templates */
  OPEN cur_service_templates;
  LOOP 
    FETCH cur_service_templates
      INTO v_template_id,v_template_name,v_template_cvstag,v_template_pkgid;
    EXIT WHEN cur_service_templates%NOTFOUND;
    INSERT INTO tmp_template_table
      VALUES(v_template_id+4000000,'Service',
             v_template_name,v_template_cvstag,v_template_pkgid);
    load_servt_parameters(v_template_id); 
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
    load_modt_parameters(v_template_id);
  END LOOP;
  CLOSE cur_module_templates;

END;  
/

commit
