SET VERIFY OFF

DECLARE
  description desc_table.description;
  cols        desc_table.cols_t;
  cur_col_no  number;
  i    number;

BEGIN

  description := desc_table.describe('&1');
  cols        := description.cols;

  dbms_output.new_line;
  dbms_output.put_line(' Describing ' || description.tab.own || '.' || description.tab.nam);
  dbms_output.put_line(' Type:      ' || description.tab_type);
  dbms_output.put_line(' Comment:   ' || description.tab_comment);

  dbms_output.put_line(' ------------------------------------------------------------');
  dbms_output.put_line(' Name                           Null?    Type              PK');
  dbms_output.put_line(' ------------------------------ -------- ----------------- --');

  cur_col_no := cols.first;
  WHILE cur_col_no IS NOT NULL LOOP
    dbms_output.put(' ');

    dbms_output.put(rpad(cols(cur_col_no).name,30));

    dbms_output.put(' ');

    IF (cols(cur_col_no).nullable) THEN
      dbms_output.put('         ');        
    ELSE
      dbms_output.put('NOT NULL ');
    END IF;

    dbms_output.put(rpad(cols(cur_col_no).datatype, 17));

    IF description.pks.exists(cols(cur_col_no).name) THEN
      dbms_output.put(lpad(to_char(description.pks(cols(cur_col_no).name)),3)); 
    ELSE
      dbms_output.put('   ');
    END IF;
   
    dbms_output.new_line();
    cur_col_no:=cols.next(cur_col_no);
  END LOOP;

  IF description.parents.count > 0 THEN
    dbms_output.new_line;
    dbms_output.put_line(' Parents: ');

    FOR parent_no IN 1 .. description.parents.count LOOP
      dbms_output.put_line('  ' || description.parents(parent_no).own || '.' || description.parents(parent_no).nam);
    END LOOP;

    dbms_output.new_line;
  END IF;

  IF description.children.count > 0 THEN
    dbms_output.new_line;
    dbms_output.put_line(' Children: ');

    FOR child_no IN 1 .. description.children.count LOOP
      dbms_output.put_line('  ' || description.children(child_no).own || '.' || description.children(child_no).nam);
    END LOOP;

    dbms_output.new_line;
  END IF;

  IF description.col_comments.count > 0 THEN
    dbms_output.new_line;
    dbms_output.put_line(' Column comments:');
    dbms_output.put_line(' -----------------');
    dbms_output.new_line;
  
    FOR cur_col_idx /* not pos ! */ IN 1 .. description.col_comments.count LOOP
      dbms_output.put(' ');
  
      dbms_output.put_line(cols(description.col_comments(cur_col_idx).pos).name || ': ' || description.col_comments(cur_col_idx).comment);
      dbms_output.new_line;
     
      cur_col_no:=cols.next(cur_col_no);
    END LOOP;
  END IF;

  EXCEPTION
    WHEN desc_table.table_does_not_exist THEN
      dbms_output.put_line('no such table: &1');

    WHEN OTHERS THEN
      dbms_output.put_line('unknown exception, ' || sqlerrm || '(' || sqlcode || ')');
END;
/