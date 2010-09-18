CREATE OR REPLACE PACKAGE BODY fake IS
/*
 * File documentation goes here.  This is required for the outline to work correctly.
 *
 * header details
   gc_class_name CONSTANT VARCHAR2(50) := 'Fake';
   gc_package_name CONSTANT VARCHAR2(50) := upper(gc_class_name);
 * end header details
 *
 * @authid CURRENT_USER
 */

/**
 * This is a test file.  This part of the documentation is generated as a package header
 * when using pldoc generation.  This is indicated by the use of the headcom tag below.
 *
 * @headcom
 */
 
   /**
    * This is a generic pl doc method documentation.  
    * 
    * @pragma RESTRICT_REFERENCES( traverse, WNDS, WNPS, RNDS)
    * 
    * @return something useful to someone hopefully
    */
   FUNCTION traverse(
      pin_object_id  IN NUMBER,
      pin_traversalcontext_id IN NUMBER)
   RETURN NUMBER
   AS
      ln_number NUMBER;
      ls_val    VARCHAR2(100);
   BEGIN
       ls_val := gc_package_name;
       dbms_output.put_line(pin_object_id);
       ln_number :=  do_something(pin_obj => ln_number, pin_extra => 'extra');
      RETURN ln_number;
   END traverse;

/**
 * This method will be private because of the use of the "@private" tag,
 * so it will not appear in the header file, but it will have pl doc
 * documentation generated.
 * @param pin_obj The id of the object to operate on.
 * @param pin_extra
 * @return NUMBER
 *
 * @private
 */
FUNCTION do_something(pin_obj IN NUMBER,
                      pin_extra IN VARCHAR2)
RETURN NUMBER IS
  lb_test BOOLEAN := FALSE;
BEGIN
    IF lb_boolean THEN
	    RETURN 0;
    END IF;
    RETURN 1;
END do_something;

/*
 * This method will not be in the header file, and will not have pl documentation generated
 * because is has only one star after the opening slash.
 * @return VARCHAR2 (useless, because this won't be pl doc'ed.
 */
FUNCTION get_name RETURN VARCHAR2 IS
BEGIN
	RETURN gc_class_name;
END get_name;
END fake;
/
SHOW ERRORS PACKAGE BODY fake
