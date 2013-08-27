
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
    SELECT u_edstemplates.id,
           u_edstemplates.name,
           u_edstemplates.cvstag,
	   u_edstemplates.packageId
    FROM u_edstemplates,u_edst2rele, U_SOFTRELEASES 
    where  U_SOFTRELEASES.releaseid=release_id 
    and u_edst2rele.ID_release=U_SOFTRELEASES.id and u_edst2rele.ID_EDSTEMPLATE=u_edstemplates.id
    AND   u_edstemplates.name = template_name;

  /* cursor for essource templates */
  CURSOR cur_essource_templates IS
    SELECT u_esstemplates.id,
           u_esstemplates.name,
           u_esstemplates.cvstag,
	   u_esstemplates.packageId
    FROM u_esstemplates,u_esst2rele,u_softreleases
    where u_softreleases.releaseid=release_id
    and u_esst2rele.id_release=u_softreleases.id and u_esst2rele.id_esstemplate=u_esstemplates.id
    AND   u_esstemplates.name = template_name;

  /* cursor for esmodule templates */
  CURSOR cur_esmodule_templates IS
    SELECT u_esmtemplates.id,
           u_esmtemplates.name,
           u_esmtemplates.cvstag,
	   u_esmtemplates.packageId
    FROM u_esmtemplates,u_esmt2rele,u_softreleases
    where u_softreleases.releaseid=release_id
    and u_esmt2rele.id_release=u_softreleases.id and u_esmt2rele.id_esmtemplate=u_esmtemplates.id
    AND   u_esmtemplates.name = template_name;


  /* cursor for service templates */
  CURSOR cur_service_templates IS
    SELECT u_srvtemplates.id,
           u_srvtemplates.name,
           u_srvtemplates.cvstag,
	   u_srvtemplates.packageId
    FROM u_srvtemplates,u_srvt2rele,u_softreleases
     where u_softreleases.releaseid=release_id
    and u_srvt2rele.id_release=u_softreleases.id and u_srvt2rele.id_srvtemplate=u_srvtemplates.id
    AND   u_srvtemplates.name = template_name;

  /* cursor for module templates */
  CURSOR cur_module_templates IS
    SELECT u_moduletemplates.id,
           u_moduletemplates.name,
           u_moduletemplates.cvstag,
           u_moduletemplates.packageId,
           u_moduletypes.type
    FROM u_moduletemplates,u_moduletypes,u_softreleases,u_modt2rele
    where u_softreleases.releaseid=release_id
    and u_modt2rele.id_release=u_softreleases.id and u_modt2rele.id_modtemplate=u_moduletemplates.id
    and u_moduletypes.id=u_moduletemplates.id_mtype
    AND   u_moduletemplates.name = template_name;

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
/*    load_parameters(v_template_id);*/
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
/*      load_parameters(v_template_id); */
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
/*      load_parameters(v_template_id); */
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
      /*load_parameters(v_template_id);*/
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
      /*load_parameters(v_template_id);*/
      template_found := TRUE;
    END LOOP;
    CLOSE cur_module_templates;
  END IF;

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
    SELECT u_edstemplates.Id,
           u_edstemplates.name,
           u_edstemplates.cvstag,
           u_edstemplates.packageId
    FROM u_edstemplates,u_edst2rele, U_SOFTRELEASES
    where  U_SOFTRELEASES.releaseid=release_id
    and u_edst2rele.ID_release=U_SOFTRELEASES.id and u_edst2rele.ID_EDSTEMPLATE=u_edstemplates.id;

  /* cursor for essource templates */
  CURSOR cur_essource_templates IS
    SELECT u_esstemplates.Id,
           u_esstemplates.name,
           u_esstemplates.cvstag,
           u_esstemplates.packageId
    FROM u_esstemplates,u_esst2rele,u_softreleases
    where u_softreleases.releaseid=release_id
    and u_esst2rele.id_release=u_softreleases.id and u_esst2rele.id_esstemplate=u_esstemplates.id;


  /* cursor for esmodule templates */
  CURSOR cur_esmodule_templates IS
    SELECT u_esmtemplates.Id,
           u_esmtemplates.name,
           u_esmtemplates.cvstag,
           u_esmtemplates.packageId
    FROM u_esmtemplates,u_esmt2rele,u_softreleases
    where u_softreleases.releaseid=release_id
    and u_esmt2rele.id_release=u_softreleases.id and u_esmt2rele.id_esmtemplate=u_esmtemplates.id;


  /* cursor for service templates */
  CURSOR cur_service_templates IS
    SELECT u_srvtemplates.Id,
           u_srvtemplates.name,
           u_srvtemplates.cvstag,
           u_srvtemplates.packageId
    FROM u_srvtemplates,u_srvt2rele,u_softreleases
     where u_softreleases.releaseid=release_id
    and u_srvt2rele.id_release=u_softreleases.id and u_srvt2rele.id_srvtemplate=u_srvtemplates.id;


  /* cursor for module templates */
  CURSOR cur_module_templates IS
    SELECT u_moduletemplates.Id,
           u_moduletemplates.name,
           u_moduletemplates.cvstag,
           u_moduletemplates.packageId,
           u_moduletypes.type
    FROM u_moduletemplates,u_moduletypes,u_softreleases,u_modt2rele
    where u_softreleases.releaseid=release_id
    and u_modt2rele.id_release=u_softreleases.id and u_modt2rele.id_modtemplate=u_moduletemplates.id
    and u_moduletypes.id=u_moduletemplates.id_mtype;

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
   /* load_parameters(v_template_id);   */
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
    /*load_parameters(v_template_id);*/
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
    /*load_parameters(v_template_id);*/
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
    /*load_parameters(v_template_id); */
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
    /*load_parameters(v_template_id);*/
  END LOOP;
  CLOSE cur_module_templates;

END;  
/

commit
