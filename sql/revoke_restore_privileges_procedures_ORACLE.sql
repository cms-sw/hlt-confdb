---------------------------------------------------------------------------------
--- CREATOR:	Raul Jimenez Estupinan  <raul.jimenez.estupinan@cern.ch>
--- DATE:	2011-12-16 
--- FILE:	revoke_restore_privileges_procedures_ORACLE.sql
---------------------------------------------------------------------------------
-- SET SERVEROUTPUT ON;

---------------------------------------------------------------------------------
-- TEMPORARY TABLE TO STORE TABLE'S PRIVILEGES:
-- NOTE: 'on commit preserve rows' holds on to the data until the session ends.
---------------------------------------------------------------------------------
CREATE GLOBAL TEMPORARY TABLE temp_stored_privileges
(
  TABLE_NAME	    VARCHAR2(512),
  GRANTEE         VARCHAR2(512),
  PRIVILEGE 	    VARCHAR2(512) 
) ON COMMIT PRESERVE ROWS;


---------------------------------------------------------------------------------
-- StoreAndRevokePrivileges
-- Stores previously granted privileges for all the users of one particular
-- table, into the table 'temp_stored_privileges'.
---------------------------------------------------------------------------------
CREATE OR REPLACE Procedure StoreAndRevokePrivileges (tableName IN varchar2)
IS
  Exist  NUMBER;
  v_table_name   VARCHAR2(512);
  v_grantee      VARCHAR2(512);
  v_privilege    VARCHAR2(512);
  sql_statement  VARCHAR2(1024);
  
  /* store privileges cursor: */
  CURSOR cur_privileges IS
      SELECT  table_name  ,
              grantee      ,
             privilege     
      FROM user_tab_privs
      WHERE table_name = tableName;
  /* store privileges cursor: */
  CURSOR cur_revoke IS
      SELECT  table_name  ,
              grantee     ,
              privilege     
      FROM temp_stored_privileges;
BEGIN

        Select count(*) into Exist from all_tab_columns
        where table_name = tableName;
        
        if(Exist = 0) then
          DBMS_OUTPUT.PUT_LINE('TABLE ' || tableName || ' DOES NOT EXIST');
          return;
        end if;
        
        DBMS_OUTPUT.PUT_LINE('[StoreAndRevokePrivileges] Storing table privileges for table ' || tableName);
        
        execute immediate 'DELETE FROM temp_stored_privileges';
        
      /* load privileges: */
      OPEN cur_privileges;
      LOOP
        FETCH cur_privileges
          INTO v_table_name,v_grantee,v_privilege;
        EXIT WHEN cur_privileges%NOTFOUND;
        INSERT INTO temp_stored_privileges VALUES(v_table_name, v_grantee, v_privilege);
      END LOOP;
      CLOSE cur_privileges;
        
        DBMS_OUTPUT.PUT_LINE('[StoreAndRevokePrivileges] Revoking privileges for table ' || tableName);
      /* revoke privileges: */
      OPEN cur_revoke;
      LOOP
        FETCH cur_revoke
          INTO v_table_name,v_grantee,v_privilege;
        EXIT WHEN cur_revoke%NOTFOUND;
        sql_statement:= 'REVOKE ' || v_privilege || ' ON ' || v_table_name || ' FROM ' || v_grantee;
        DBMS_OUTPUT.PUT_LINE('[StoreAndRevokePrivileges] '|| sql_statement);
        execute immediate sql_statement;
      END LOOP;
      CLOSE cur_revoke;
      
      DBMS_OUTPUT.PUT_LINE('[StoreAndRevokePrivileges] Privileges for ' || tableName || ' have been stored in table ''temp_stored_privileges''');
END;
/

---------------------------------------------------------------------------------
-- LoadAndGrantPrivileges
-- Restores previously revoked privileges by StoreAndRevokePrivileges, using the
-- table 'temp_stored_privileges'.
---------------------------------------------------------------------------------
CREATE OR REPLACE Procedure LoadAndGrantPrivileges (tableName IN varchar2)
IS
  Exist  NUMBER;
  v_table_name   VARCHAR2(512);
  v_grantee      VARCHAR2(512);
  v_privilege    VARCHAR2(512);
  sql_statement  VARCHAR2(1024);
  
  /* store privileges cursor: */
  CURSOR cur_grant IS
      SELECT  table_name  ,
              grantee     ,
              privilege     
      FROM temp_stored_privileges;
BEGIN

        Select count(*) into Exist from all_tab_columns
        where table_name = tableName;
        
        if(Exist = 0) then
          DBMS_OUTPUT.PUT_LINE('TABLE ' || tableName || ' DOES NOT EXIST');
          return;
        end if;
        
        DBMS_OUTPUT.PUT_LINE('[LoadAndGrantPrivileges] Grantting privileges for ' || tableName );
        
      /* grant privileges: */
      OPEN cur_grant;
      LOOP
        FETCH cur_grant
          INTO v_table_name,v_grantee,v_privilege;
        EXIT WHEN cur_grant%NOTFOUND;
        sql_statement:= 'GRANT ' || v_privilege || ' ON ' || v_table_name || ' TO ' || v_grantee;
        DBMS_OUTPUT.PUT_LINE('[LoadAndGrantPrivileges] '|| sql_statement);
        execute immediate sql_statement;
      END LOOP;
      CLOSE cur_grant;
      
      DBMS_OUTPUT.PUT_LINE('[LoadAndGrantPrivileges] Privileges for ' || tableName || ' have been restored');
END;
/

COMMIT;
