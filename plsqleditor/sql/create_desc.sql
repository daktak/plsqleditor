create table desc_test_parent (
  pk_1    number,
  pk_2    number,
  col_1   varchar2(30),
  col_2   date,
  col_3   number,
  primary key (pk_1, pk_2)
);

create table desc_test_child (
  fk_1,
  fk_2,
  col_4   blob,
  col_5   raw(50),
  foreign key (fk_1, fk_2) references desc_test_parent(pk_1, pk_2)
);

comment on table  desc_test_parent is 'Parent table';
comment on table  desc_test_child is 'Child table';

comment on column desc_test_parent.pk_1  is 'first part of primary key';
comment on column desc_test_parent.pk_2  is 'second part of primary key';
comment on column desc_test_parent.col_2 is 'Just an idle comment on col_2';

comment on column desc_test_child.fk_1  is 'first part of foreign key';
comment on column desc_test_child.fk_2  is 'second part of foreign key';
comment on column desc_test_child.col_5 is 'Some idle comment on col_5';

connect / as sysdba

grant select any table to rene;