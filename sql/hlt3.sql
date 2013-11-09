--
-- PROCEDURE load_configuration
--
CREATE OR REPLACE PROCEDURE load_configuration(config_id IN NUMBER)
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
/*sv CURSOR cur_global_psets IS
    SELECT
      ParameterSets.id,
      ParameterSets.name,
      ParameterSets.tracked,
      ConfigurationParamSetAssoc.sequenceNb
    FROM ParameterSets
    JOIN ConfigurationParamSetAssoc
    ON ParameterSets.id = ConfigurationParamSetAssoc.psetId
    WHERE ConfigurationParamSetAssoc.configId = config_id;
*/

  /* cursor for edsources */
  CURSOR cur_edsources IS
    SELECT
      u_edsources.id,
      u_edsources.id_template,
      u_conf2eds.ord
    FROM u_edsources,u_conf2eds
    WHERE u_conf2eds.id_edsource=u_edsources.id
    and u_conf2eds.id_confver = config_id;

  /* cursor for essources */
  CURSOR cur_essources IS
    SELECT
      u_essources.id,
      u_essources.id_template,
      u_essources.name,
      u_conf2ess.prefer,
      u_conf2ess.ord
    FROM u_essources,u_conf2ess
    WHERE u_conf2ess.id_essource=u_essources.id
    and u_conf2ess.id_confver = config_id;

  /* cursor for esmodules */
  CURSOR cur_esmodules IS
    SELECT
      u_esmodules.id,
      u_esmodules.id_template,
      u_esmodules.name,
      u_conf2esm.prefer,
      u_conf2esm.ord
    FROM u_esmodules,u_conf2esm
    WHERE u_conf2esm.id_esmodule=u_esmodules.id 
    and u_conf2esm.id_confver = config_id;

  /* cursor for services */
  CURSOR cur_services IS
    SELECT
      u_services.id,
      u_services.id_template,
      u_conf2srv.ord
    FROM u_services,u_conf2srv
    WHERE u_conf2srv.id_service=u_services.id
    and u_conf2srv.id_confver = config_id;

  /* cursor for modules from configuration *paths* */
  CURSOR cur_modules_from_paths IS
      SELECT
      d.id,
      u_mod2templ.id_templ,
      u_paelements.name
    FROM u_paelements, u_pathid2conf, u_mod2templ,(select min(aa.id)as id, aa.crc32 from u_paelements aa,u_pathid2conf bb,u_pathids cc where aa.id_pathid = bb.id_pathid AND cc.id=aa.id_pathid AND bb.id_confver =config_id group by aa.crc32,aa.paetype) d
    WHERE u_pathid2conf.id_pathid=u_paelements.id_pathid 
    and u_mod2templ.id_pae=u_paelements.id 
    and u_paelements.paetype=1 and u_paelements.crc32=d.crc32
    and u_pathid2conf.id_confver = config_id;

  /* cursor for modules from configuration *sequences* */
  CURSOR cur_modules_from_sequences IS
    SELECT
      d.id,
      u_mod2templ.id_templ,
      u_paelements.name
    FROM u_paelements, u_pathid2conf, u_mod2templ, (select min(aa.id)as id, aa.crc32 from u_paelements aa,u_pathid2conf bb,u_pathids cc where aa.id_pathid = bb.id_pathid AND cc.id=aa.id_pathid AND bb.id_confver =config_id group by aa.crc32,aa.paetype) d
    WHERE u_pathid2conf.id_pathid=u_paelements.id_pathid
    and u_mod2templ.id_pae=u_paelements.id
    and u_paelements.paetype=1 and u_paelements.crc32=d.crc32
    and u_paelements.lvl>0
    and u_pathid2conf.id_confver = config_id;


  /* cursor for paths */
  CURSOR cur_paths IS
    SELECT
      u_pathids.pathId,
      u_paths.name,
      u_pathids.isEndPath,
      u_pathid2conf.ord
    FROM u_pathids, u_paths,u_pathid2conf
    WHERE u_pathid2conf.id_pathid=u_pathids.id
    and u_paths.id=u_pathids.id_path
    and u_pathid2conf.id_confver = config_id;

  /* cursor for sequences */
  CURSOR cur_sequences IS
   /* SELECT
      u_paelements.id,
      u_paelements.name,
      u_paelements.ord
    FROM u_paelements, u_pathid2conf
    WHERE u_pathid2conf.id_pathid=u_paelements.id_pathid 
    and u_paelements.paetype=2
    and u_pathid2conf.id_confver = config_id;*/
    SELECT
      d.id,
      a.name,
      a.ord
    FROM u_paelements a, u_pathid2conf, (select min(aa.id)as id, aa.crc32 from u_paelements aa,u_pathid2conf bb,u_pathids cc where aa.id_pathid = bb.id_pathid AND cc.id=aa.id_pathid AND bb.id_confver =config_id group by aa.crc32,aa.paetype) d
    WHERE u_pathid2conf.id_pathid=a.id_pathid
    and a.paetype=2 and a.crc32=d.crc32
    and u_pathid2conf.id_confver = config_id;

  /* cursor for path-sequence associations */
  CURSOR cur_path_sequence IS
    SELECT
      u_pathids.pathId,
      d.id,
      u_paelements.ord,
      u_paelements.operator
    FROM  u_pathids,u_paelements,u_pathid2conf,(select min(aa.id)as id, aa.crc32 from u_paelements aa,u_pathid2conf bb,u_pathids cc where aa.id_pathid = bb.id_pathid AND cc.id=aa.id_pathid AND bb.id_confver =config_id group by aa.crc32,aa.paetype) d
    WHERE u_pathid2conf.id_pathid=u_pathids.id
    and  u_pathid2conf.id_pathid=u_paelements.id_pathid
    and u_paelements.paetype=2 and u_paelements.crc32=d.crc32
    and u_pathid2conf.id_confver = config_id;

  /* cursor for path-module associations */
  CURSOR cur_path_module IS
    SELECT
      u_pathids.pathId,
      d.id,
      u_paelements.ord,
      u_paelements.operator
    FROM u_pathids,u_paelements, u_pathid2conf, (select min(aa.id)as id, aa.crc32 from u_paelements aa,u_pathid2conf bb,u_pathids cc where aa.id_pathid = bb.id_pathid AND cc.id=aa.id_pathid AND bb.id_confver =config_id group by aa.crc32,aa.paetype) d
    WHERE u_pathid2conf.id_pathid=u_paelements.id_pathid
    and u_pathid2conf.id_pathid=u_pathids.id
    and u_paelements.paetype=1 and u_paelements.crc32=d.crc32
    and u_pathid2conf.id_confver = config_id;

/* cursor for path-outputmodule associations */
  CURSOR cur_path_outputmod IS
    SELECT
      u_pathid2outm.id_pathId,
      u_streamids.id,
      u_pathid2outm.ord,
      u_pathid2outm.operator
    FROM u_pathid2outm,u_pathid2conf,u_streamids
    WHERE u_pathid2conf.id_pathid=u_pathid2outm.id_pathId
    and u_streamids.id=u_pathid2outm.id_streamid
    and u_pathid2conf.id_confver = config_id;


BEGIN

  execute immediate 'DELETE FROM tmp_instance_table';
  execute immediate 'DELETE FROM tmp_parameter_table';
  execute immediate 'DELETE FROM tmp_boolean_table';
  execute immediate 'DELETE FROM tmp_int_table';
  execute immediate 'DELETE FROM tmp_real_table';
  execute immediate 'DELETE FROM tmp_string_table';
  execute immediate 'DELETE FROM tmp_path_entries';
  execute immediate 'DELETE FROM tmp_sequence_entries';

  load_gpset_parameters(config_id);

  OPEN cur_edsources;
  LOOP
    FETCH cur_edsources INTO v_instance_id,v_template_id,v_sequence_nb;
    EXIT WHEN cur_edsources%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,v_template_id,'EDSource',NULL,NULL,v_sequence_nb);
    /*load_eds_parameters(v_instance_id);*/
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
    /*load_ess_parameters(v_instance_id);*/
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
    /*load_esm_parameters(v_instance_id);*/
  END LOOP;
  CLOSE cur_esmodules;

  /* load services */
  OPEN cur_services;
  LOOP
    FETCH cur_services INTO v_instance_id,v_template_id,v_sequence_nb;
    EXIT WHEN cur_services%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,v_template_id,'Service',NULL,NULL,v_sequence_nb);
    /*load_serv_parameters(v_instance_id);*/
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
    /*load_mod_parameters(v_instance_id);*/
  END LOOP;
  CLOSE cur_modules_from_paths;

 /* load modules from *sequences* 
  OPEN cur_modules_from_sequences;
  LOOP
    FETCH cur_modules_from_sequences
      INTO v_instance_id,v_template_id,v_instance_name;
    EXIT WHEN cur_modules_from_sequences%NOTFOUND;
    INSERT INTO tmp_instance_table
      VALUES(v_instance_id,v_template_id,'Module',v_instance_name,NULL,NULL);
    load_mod_parameters(v_instance_id);
  END LOOP;
  CLOSE cur_modules_from_sequences;*/

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
      /*load_parameters(v_child_id);*/
  END LOOP;
  CLOSE cur_path_outputmod;

END;  
/


COMMIT;
