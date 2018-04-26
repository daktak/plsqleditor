CREATE OR REPLACE PACKAGE BODY 
/*
 * packaged comments
 */fakewrapped IS 
/**
 * header section
 */


/**
 * @return RECORD (aaaa VARCHAR2(20), bbb varchar(20))
 */
TYPE rec_ZZZZ IS RECORD (aaaa VARCHAR2(20), bbb varchar(20));

/**
 * @param a
 * @param b
 */
CURSOR does_something(a NUMBER, b number)
IS SELECT * FROM tab;

/**
 */
CURSOR parses_badly(a VARCHAR2(20), b VARCHAR2(20))
IS SELECT * FROM tab;

/**
 * @return NUMBER
 */
FUNCTION my_func 
RETURN NUMBER
IS
BEGIN
    does_something(a => NUMBER, b => number)
END my_func;
END fakewrapped;

/
SHOW ERRORS PACKAGE BODY fakewrapped