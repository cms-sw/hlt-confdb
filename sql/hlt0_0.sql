--
-- Oracle Stored Procedures
--
-- Modified :
-- 24/01/2012   - Change to 4k char for TMP_STRING_TABLE. 
                                - Update procedure load_parameter_value.


--
-- create global temporary tables
--
CREATE GLOBAL TEMPORARY TABLE tmp_template_table
(
  template_id      NUMBER,
  template_type    VARCHAR2(64),
  template_name    VARCHAR2(128),
  template_cvstag  VARCHAR2(32),
  template_pkgid   NUMBER
) ON COMMIT PRESERVE ROWS;

/*CREATE GLOBAL TEMPORARY TABLE tmp_instance_table*/
CREATE TABLE tmp_instance_table
(
  instance_id       NUMBER,
  template_id       NUMBER,
  instance_type     VARCHAR2(64),
  instance_name     VARCHAR2(128),
  flag              NUMBER(1),
  sequence_nb       NUMBER
);
/*) ON COMMIT PRESERVE ROWS;*/


CREATE GLOBAL TEMPORARY TABLE tmp_boolean_table
(
  parameter_id          NUMBER,
  parameter_value       NUMBER(1)
) ON COMMIT PRESERVE ROWS;

/*CREATE GLOBAL TEMPORARY TABLE tmp_parameter_table */
CREATE TABLE tmp_parameter_table
(
  parameter_id      NUMBER,
  parameter_type    VARCHAR2(64),
  parameter_name    VARCHAR2(128),
  parameter_trkd    NUMBER(1),
  parameter_seqnb   NUMBER,
  parent_id         NUMBER,
  lvl               NUMBER,
  value		    VARCHAR2(4000),
  valuelob          CLOB
);
/*) ON COMMIT PRESERVE ROWS;   */

CREATE GLOBAL TEMPORARY TABLE tmp_int_table
(
  parameter_id    NUMBER,
  parameter_value NUMBER,
  sequence_nb     NUMBER,
  hex             NUMBER(1)
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_real_table
(
  parameter_id    NUMBER,
  parameter_value FLOAT,
  sequence_nb     NUMBER
) ON COMMIT PRESERVE ROWS;

CREATE GLOBAL TEMPORARY TABLE tmp_string_table
(
  parameter_id      NUMBER,
  parameter_value   VARCHAR(4000),
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
  entry_type        VARCHAR2(64),
  operator          NUMBER
) ON COMMIT PRESERVE ROWS;


