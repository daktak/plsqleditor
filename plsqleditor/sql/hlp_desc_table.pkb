CREATE OR REPLACE PACKAGE BODY desc_table AS 
/*
 */

  /**
   * @param table_name
   * @return description
   */
  FUNCTION describe(table_name IN VARCHAR2) RETURN description IS 
    -- used for dbms_utility.name_resolve:
    util_context       NUMBER := 2;
    util_schema        VARCHAR2(30);
    util_part1         VARCHAR2(30);
    util_part2         VARCHAR2(30);
    util_dblink        VARCHAR2(128);
    util_part1_type    NUMBER;
    util_object_NUMBER NUMBER;

    tab                table_t;
  BEGIN
    dbms_utility.name_resolve(table_name, util_context, util_schema, util_part1, util_part2, util_dblink, util_part1_type, util_object_NUMBER);

    tab.own := util_schema;
    tab.nam := util_part1;

    return describe(tab);

  EXCEPTION
    WHEN OTHERS THEN 
      CASE 
        WHEN SQLCODE = -6564 THEN 
        RAISE table_does_not_exist;
      ELSE
        dbms_output.put_line('exception: ' || sqlerrm || '(' || sqlcode || ')' ); 
    END CASE;

  END describe;

  /**
   * @param tab
   * @return description
   */
  FUNCTION describe(tab in table_t) RETURN description IS
    col_r         col_t;
    ret           description;
    v_table_name  VARCHAR2(30);
    v_table_owner VARCHAR2(30);
    col_pos            NUMBER;

  BEGIN

    ret.tab          := tab;

    ret.cols         := cols_t        ();
    ret.col_comments := col_comments_t();
    ret.parents      := tables_t      ();
    ret.children     := tables_t      ();

    SELECT comments,table_type INTO ret.tab_comment, ret.tab_type FROM all_tab_comments 
     WHERE table_name = tab.nam AND owner = tab.own;

    col_pos := 1;

    FOR r IN (
      SELECT t.column_name, t.data_type, t.data_length, t.data_precision, t.data_scale, t.nullable, c.comments
      FROM
        all_tab_columns t INNER JOIN all_col_comments c
                              ON t.table_name = c.table_name AND
                                 t.column_name = c.column_name AND
                                 t.owner = c.owner
        WHERE
          t.table_name = tab.nam and t.owner = tab.own
        ORDER BY
          column_id) LOOP

      col_r.name       := r.column_name;
      col_r.nullable   := case when r.nullable = 'Y' then true else false end;
      col_r.datatype   := r.data_type;
      col_r.checks     := check_t();

      IF r.data_length IS NOT NULL AND r.data_precision IS NULL THEN
        IF r.data_type <> 'DATE' THEN
          col_r.datatype := col_r.datatype || '(' || r.data_length || ')';
        END IF;
      END IF;

      IF r.data_precision IS NOT NULL THEN
        col_r.datatype := col_r.datatype || '(' || r.data_precision;

        IF r.data_scale IS NOT NULL AND r.data_scale > 0 THEN
          col_r.datatype := col_r.datatype || ',' || r.data_scale;
        END IF;

        col_r.datatype := col_r.datatype || ')';
      END IF;

      ret.cols.extend;
      ret.cols(ret.cols.count) := col_r;

      IF r.comments IS NOT NULL THEN
        ret.col_comments.extend;
        ret.col_comments(ret.col_comments.count).pos     := col_pos; 
        ret.col_comments(ret.col_comments.count).comment := r.comments;
      END IF;
      
      col_pos := col_pos+1;
    END LOOP;

    -- finding constraints
    FOR r IN (
      SELECT
        r_owner, constraint_name, r_constraint_name, constraint_type, search_condition
      FROM
        all_constraints
      WHERE
        table_name = tab.nam and owner = tab.own) LOOP

        IF r.constraint_type = 'P' THEN
          FOR c IN (
            SELECT column_name, table_name, position
              FROM all_cons_columns
             WHERE constraint_name = r.constraint_name) LOOP

            ret.pks(c.column_name) := c.position;
          END LOOP;

          SELECT DISTINCT /* distinct in case a table has two foreign keys to table */
            owner, table_name BULK COLLECT into ret.children
          FROM
            all_constraints
          WHERE
            r_constraint_name = r.constraint_name AND
            owner             = tab.own;

        ELSIF r.constraint_type = 'R' THEN -- foreign key

          SELECT owner, table_name INTO v_table_owner, v_table_name 
            FROM all_constraints 
           WHERE constraint_name = r.r_constraint_name AND owner = r.r_owner;

          ret.parents.extend;
          ret.parents(ret.parents.count).own := v_table_owner;
          ret.parents(ret.parents.count).nam := v_table_name;
        END IF;

      END LOOP;

    RETURN ret;

  END describe;

END desc_table;
/