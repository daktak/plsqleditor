/**
 * 
 */
package plsqleditor.process.test;

import plsqleditor.process.CannotCompleteException;
import plsqleditor.process.SqlPlusProcessExecutor;
import junit.framework.TestCase;

/**
 * This class 
 * 
 * @author Toby Zines
 * 
 * @version $Id$
 * 
 * Created on 16/03/2005 
 *
 */
public class TestSqlPlusProcess extends TestCase
{
    public void testSvcMdlSmallHeader()
    {
        SqlPlusProcessExecutor sqlplus = new SqlPlusProcessExecutor("sqlplus", "svcmdl", "pass123", "DEV53");
        try
        {
            sqlplus.execute("CREATE OR REPLACE PACKAGE small AS \n" +
                            "   gc_class_name CONSTANT VARCHAR2(50) := \'Fake\';\r\n" + 
                            "   gc_package_name CONSTANT VARCHAR2(50) := upper(gc_class_name);\r\n" + 
                            "\r\n" + 
                            "/**\r\n" + 
                            " * This is a dummy file\r\n" + 
                            " * @headcom\r\n" + 
                            " */\r\n" + 
                            "\r\n" + 
                            "   /**\r\n" + 
                            "    * \r\n" + 
                            "    * @param pin_object_id - a AtmPort object_id\r\n" + 
                            "    * @param pin_traversal_context_id - a TraversalContexts object_id\r\n" + 
                            "    * @return - a LinkSequence object_id\r\n" + 
                            "    */\r\n" + 
                            "   FUNCTION traverse(\r\n" + 
                            "      pin_object_id  IN NUMBER,\r\n" + 
                            "      pin_traversalcontext_id IN NUMBER)\r\n" + 
                            "   RETURN NUMBER\r\n" + 
                            "  ;\r\n" + 
                            "\r\n" + 
                            "/**\r\n" + 
                            " *\r\n" + 
                            " */\r\n" + 
                            "FUNCTION do_something RETURN NUMBER;\r\n" + 
                            "\r\n" + 
                            "/**\r\n" + 
                            " *\r\n" + 
                            " */\r\n" + 
                            "FUNCTION get_name RETURN VARCHAR2;\r\n" + 
                            "\r\n" + 
                            "END small;\r\n" +
                            "/ \r\n" + 
                            "SHOW ERRORS PACKAGE fake;\r\n" + 
                            "");
            assertTrue("Failed to get valid result", sqlplus.isValid());
        }
        catch (CannotCompleteException e)
        {
            e.printStackTrace();
            assertTrue(false);
        }
    }
}
