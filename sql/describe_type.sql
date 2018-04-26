/**
 * A basic function to describe a type that is built into the database.
 * As of yet, this does not work for PLSQL types that are declared in packages.
 * @param type_name The type name, not including owner
 * @param owner_name The owner name, defaults to current owner
 * @param indent The padding to put on the left of this description.  Used for 
 * nested types.  For example, use '    ' (4 spaces) for the first nested type.
 * @param eol The end of line constant to use... depends on the OS.
 * @param column_width The width of the Name and Type Columns for displaying type information.
 * 
 * Examples:

SQL> select describe_type('T1') from dual;

DESCRIBE_TYPE('T1')
--------------------------------------------------------------------------------

T1                                       TABLE OF VARCHAR2(100)

SQL> select describe_type('T2') from dual;

DESCRIBE_TYPE('T2')
--------------------------------------------------------------------------------

T2                                       TABLE OF T1
    T1                                   TABLE OF VARCHAR2(100)



SQL> select describe_type('T3') from dual;

DESCRIBE_TYPE('T3')
--------------------------------------------------------------------------------

T3                                       TABLE OF T2
    T2                                   TABLE OF T1
        T1                               TABLE OF VARCHAR2(100)




SQL> select describe_type('MY_NVARRAY') from dual;

DESCRIBE_TYPE('MY_NVARRAY')
--------------------------------------------------------------------------------

MY_NVARRAY                               VARRAY(100) OF VARCHAR2(1000)

SQL> select describe_type('MY_RECORD') from dual;
DESCRIBE_TYPE('MY_RECORD')
-------------------------------------------------------------------------------

MY_RECORD is OBJECT of (N, V, VA, F)

N                                        NUMBER
V                                        VARCHAR2(100)
VA                                       MY_NVARRAY
    MY_NVARRAY                           VARRAY(100) OF VARCHAR2(1000)
F                                        FSS_VARCHAR_TABLE
    FSS_VARCHAR_TABLE                    TABLE OF VARCHAR2(4000)


SQL>
 */
CREATE OR REPLACE FUNCTION describe_type(type_name IN VARCHAR2
                                        ,owner_name IN VARCHAR2 DEFAULT NULL
                                        ,indent IN VARCHAR2 DEFAULT ''
                                        ,eol  IN VARCHAR2 DEFAULT chr(10)
                                        ,column_width IN NUMBER DEFAULT 40)
   RETURN VARCHAR2 AUTHID CURRENT_USER IS
   the_result VARCHAR2(4000);
   ls_coll_type VARCHAR2(30);
   is_object_type BOOLEAN;
   name VARCHAR2(30) := upper(type_name);
BEGIN
   -- If it's a collection
   FOR coll IN (SELECT * FROM ALL_COLL_TYPES WHERE type_name = NAME AND owner = nvl(UPPER(owner_name), owner)) LOOP
      -- set coll_type
      IF coll.coll_type LIKE 'VARYING%' THEN
         ls_coll_type := 'VARRAY(' || coll.upper_bound || ')';
      ELSE
         ls_coll_type := coll.coll_type;
      END IF;
      
      the_result := rpad(indent || coll.type_name,column_width+1) || ls_coll_type || ' OF ' || coll.elem_type_name;
      IF coll.elem_type_name = 'VARCHAR2' THEN
        the_result := the_result || '(' || coll.length || ')';
      END IF;
       IF coll.elem_type_name NOT IN ('VARCHAR2','NUMBER') THEN
          the_result := the_result || eol || describe_type(coll.elem_type_name, indent => indent||'    ');
       END IF;
       the_result := the_result || eol;
   END LOOP;
   
   --If it's an object
   FOR attr IN (SELECT * FROM ALL_TYPE_ATTRS WHERE type_name = NAME AND owner = nvl(UPPER(owner_name), owner) ORDER BY attr_no) LOOP
       is_object_type := TRUE;
       IF attr.attr_no > 1 THEN
           the_result := the_result || ', ';
       ELSE
           the_result := NAME || ' is OBJECT of (';
       END IF;
       the_result := the_result || attr.attr_name;
   END LOOP;
   IF is_object_type THEN
      the_result := the_result || ')' || eol  || eol;
   END IF;
   
   FOR attr IN (SELECT * FROM ALL_TYPE_ATTRS WHERE type_name = NAME  AND owner = nvl(UPPER(owner_name), owner) ORDER BY attr_no) LOOP
--       IF attr.attr_no = 1 THEN
--         the_result := the_result || rpad('Name',column_width+1) || rpad('Type',column_width) || eol;
--         the_result := the_result || rpad('-',column_width,'-') || ' ' || rpad('-',column_width,'-')  || eol;
--       END IF;
       the_result := the_result || indent || rpad(attr.attr_name,column_width+1) || attr.attr_type_name;
       IF attr.attr_type_name = 'VARCHAR2' THEN
          the_result := the_result || '(' || attr.length || ')';
       END IF;
       the_result := the_result || eol;
       IF attr.attr_type_name NOT IN ('VARCHAR2','NUMBER') THEN
          the_result := the_result || describe_type(attr.attr_type_name, indent => indent||'    ');
       END IF;
   END LOOP;

   RETURN the_result;
END describe_type;
/
