


--
-- PROCEDURE load_parameters
--
CREATE OR REPLACE PROCEDURE load_eds_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_edselements
    WHERE id_edsource = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/

CREATE OR REPLACE PROCEDURE load_edst_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_edstelements
    WHERE id_edstemplate = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/

CREATE OR REPLACE PROCEDURE load_esst_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_esstelements
    WHERE id_esstemplate = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/
 
CREATE OR REPLACE PROCEDURE load_ess_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_esselements
    WHERE id_essource = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/
 
CREATE OR REPLACE PROCEDURE load_esmt_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_esmtelements
    WHERE id_esmtemplate = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/

CREATE OR REPLACE PROCEDURE load_esm_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_esmelements
    WHERE id_esmodule = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/

CREATE OR REPLACE PROCEDURE load_serv_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_srvelements
    WHERE id_service = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/

CREATE OR REPLACE PROCEDURE load_servt_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_srvtelements
    WHERE id_srvtemplate = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/

CREATE OR REPLACE PROCEDURE load_modt_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_modtelements
    WHERE id_modtemplate = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/

CREATE OR REPLACE PROCEDURE load_mod_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT id,
           paramType,
           name,
           tracked,
           ord,
           lvl,
           value,
           valuelob
    FROM u_moelements
    WHERE id_pae = parent_id
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/

CREATE OR REPLACE PROCEDURE load_gpset_parameters(parent_id IN  NUMBER)
AS
  v_parameter_id    NUMBER;
  v_parameter_type  VARCHAR2(64);
  v_parameter_name  VARCHAR2(128);
  v_parameter_trkd  NUMBER(1);
  v_parameter_seqnb PLS_INTEGER;
  v_parameter_lvl    NUMBER;
  v_parameter_value VARCHAR2(4000);
  v_parameter_valob CLOB;

  /* cursor for parameters */
  CURSOR cur_parameters IS
    SELECT u_gpsetelements.id,
           u_gpsetelements.paramType,
           u_gpsetelements.name,
           u_gpsetelements.tracked,
           u_gpsetelements.ord,
           u_gpsetelements.lvl,
           u_gpsetelements.value,
           u_gpsetelements.valuelob
    FROM u_gpsetelements,u_conf2gpset
    WHERE u_conf2gpset.id_confver = parent_id 
    AND u_gpsetelements.id_gpset=u_conf2gpset.id_gpset
    ORDER BY id ASC;


BEGIN



  OPEN cur_parameters;
  LOOP
    FETCH cur_parameters
      INTO v_parameter_id,v_parameter_type,
           v_parameter_name,v_parameter_trkd,v_parameter_seqnb,v_parameter_lvl,v_parameter_value,v_parameter_valob;
    EXIT WHEN cur_parameters%NOTFOUND;
    INSERT INTO tmp_parameter_table
      VALUES(v_parameter_id,v_parameter_type,
          v_parameter_name,v_parameter_trkd,v_parameter_seqnb,parent_id,v_parameter_lvl,v_parameter_value,v_parameter_valob);
  END LOOP;
  CLOSE cur_parameters;

END;
/
