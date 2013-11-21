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
      v_edsources.id+1000000,
      v_edsources.id_template+1000000,
      v_conf2eds.ord
    FROM v_edsources,v_conf2eds
    WHERE v_conf2eds.id_edsource=v_edsources.id
    and v_conf2eds.id_confver = config_id;

  /* cursor for essources */
  CURSOR cur_essources IS
    SELECT
      v_essources.id+2000000,
      v_essources.id_template+2000000,
      v_essources.name,
      v_conf2ess.prefer,
      v_conf2ess.ord
    FROM v_essources,v_conf2ess
    WHERE v_conf2ess.id_essource=v_essources.id
    and v_conf2ess.id_confver = config_id;

  /* cursor for esmodules */
  CURSOR cur_esmodules IS
    SELECT
      v_esmodules.id+3000000,
      v_esmodules.id_template+3000000,
      v_esmodules.name,
      v_conf2esm.prefer,
      v_conf2esm.ord
    FROM v_esmodules,v_conf2esm
    WHERE v_conf2esm.id_esmodule=v_esmodules.id 
    and v_conf2esm.id_confver = config_id;

  /* cursor for services */
  CURSOR cur_services IS
    SELECT
      v_services.id+4000000,
      v_services.id_template+4000000,
      v_conf2srv.ord
    FROM v_services,v_conf2srv
    WHERE v_conf2srv.id_service=v_services.id
    and v_conf2srv.id_confver = config_id;

  /* cursor for modules from configuration *paths* */
  CURSOR cur_modules_from_paths IS
    SELECT UNIQUE
      h_paelements.id,
      h_pastruct.id_templ,
      h_paelements.name
    FROM h_pastruct,h_paelements, h_pathid2conf
    WHERE h_pathid2conf.id_pathid=h_pastruct.id_pathid
    and h_pastruct.id_pae=h_paelements.id
    and h_paelements.paetype=1
    and h_pathid2conf.id_confver = config_id;

  /* cursor for modules from configuration *sequences* */
  CURSOR cur_modules_from_sequences IS
    SELECT UNIQUE
      h_paelements.id,
      h_pastruct.id_templ,
      h_paelements.name
    FROM h_pastruct,h_paelements, h_pathid2conf, h_pae2moe
    WHERE h_pathid2conf.id_pathid=h_pastruct.id_pathid
    and h_pastruct.id_pae=h_paelements.id
    and h_paelements.paetype=1
    and h_pae2moe.id_pae=h_paelements.id
    and h_pae2moe.lvl>0
    and h_pathid2conf.id_confver = config_id;



  /* cursor for paths */
  CURSOR cur_paths IS
    SELECT
      h_pathid2conf.id_pathid,
      v_paths.name,
      h_pathid2path.isEndPath,
      h_pathid2conf.ord
    FROM v_paths,h_pathid2conf,h_pathid2path
    WHERE h_pathid2path.id_pathid=h_pathid2conf.id_pathid
    and v_paths.id=h_pathid2path.id_path
    and h_pathid2conf.id_confver = config_id order by v_paths.name;

  /* cursor for sequences */
  CURSOR cur_sequences IS
   /* SELECT
      v_paelements.id,
      v_paelements.name,
      v_paelements.ord
    FROM v_paelements, v_pathid2conf
    WHERE v_pathid2conf.id_pathid=v_paelements.id_pathid 
    and v_paelements.paetype=2
    and v_pathid2conf.id_confver = config_id;*/
    SELECT  DISTINCT
      h_paelements.id,
      h_paelements.name, 0
    FROM h_pastruct,h_paelements, h_pathid2conf 
    WHERE h_pathid2conf.id_pathid=h_pastruct.id_pathid
    and h_pastruct.id_pae=h_paelements.id
    and h_paelements.paetype=2
    and h_pathid2conf.id_confver = config_id order by h_paelements.name;

  /* cursor for path-sequence associations */
  CURSOR cur_path_sequence IS
    SELECT UNIQUE
      h_pastruct.id_pathid,
      h_paelements.id,
      0,
      h_pastruct.operator
    FROM h_pastruct,h_paelements, h_pathid2conf
    WHERE h_pathid2conf.id_pathid=h_pastruct.id_pathid
    and h_pastruct.id_pae=h_paelements.id
    and h_paelements.paetype=2
    and h_pastruct.lvl=0
    and h_pathid2conf.id_confver = config_id;

      

  /* cursor for path-module associations */
  CURSOR cur_path_module IS
    SELECT UNIQUE
      h_pastruct.id_pathid,
      h_paelements.id,
      0,
      h_pastruct.operator
    FROM h_pastruct,h_paelements, h_pathid2conf
    WHERE h_pathid2conf.id_pathid=h_pastruct.id_pathid
    and h_pastruct.id_pae=h_paelements.id
    and h_paelements.paetype=1
    and h_pastruct.lvl=0
    and h_pathid2conf.id_confver = config_id;


/* cursor for path-outputmodule associations */
  CURSOR cur_path_outputmod IS
    SELECT
      v_pathid2outm.id_pathId,
      v_streamids.id,
      v_pathid2outm.ord,
      v_pathid2outm.operator
    FROM v_pathid2outm,v_pathid2conf,v_streamids
    WHERE v_pathid2conf.id_pathid=v_pathid2outm.id_pathId
    and v_streamids.id=v_pathid2outm.id_streamid
    and v_pathid2conf.id_confver = config_id;


BEGIN

  execute immediate 'DELETE FROM tmp_instance_table';
/*  execute immediate 'DELETE FROM tmp_parameter_table';*/
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
