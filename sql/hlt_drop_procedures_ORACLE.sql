--
-- Oracle PL/SQL: drop all procedures/functions/packages
--

DROP PROCEDURE load_parameter_value;
DROP PROCEDURE load_parameters;
DROP FUNCTION  load_template;
--DROP FUNCTION  load_templates;
DROP PROCEDURE  load_templates;
DROP FUNCTION  load_templates_for_config;
DROP FUNCTION  load_configuration;
DROP FUNCTION  get_parameters;
DROP FUNCTION  get_boolean_values;
DROP FUNCTION  get_int_values;
DROP FUNCTION  get_real_values;
DROP FUNCTION  get_string_values;
DROP FUNCTION  get_path_entries;
DROP FUNCTION  get_sequence_entries;
DROP FUNCTION  get_stream_entries;

DROP PACKAGE   types;

DROP TABLE tmp_template_table;
DROP TABLE tmp_instance_table;
DROP TABLE tmp_parameter_table;
DROP TABLE tmp_boolean_table;
DROP TABLE tmp_int_table;
DROP TABLE tmp_real_table;
DROP TABLE tmp_string_table;
DROP TABLE tmp_path_entries;
DROP TABLE tmp_sequence_entries;
DROP TABLE tmp_stream_entries;
