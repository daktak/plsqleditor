/**
 * 
 */
package plsqleditor.parsers;

import java.io.IOException;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;

import au.com.alcatel.fulfil.tools.codecheck.parser.ANTLRNoCaseFileStream;
import au.com.alcatel.fulfil.tools.codecheck.parser.PlSqlLexer;
import au.com.alcatel.fulfil.tools.codecheck.parser.PlSqlParser;
import junit.framework.TestCase;

/**
 * @author Toby
 * 
 */
public class TestRootSegment extends TestCase
{

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    /*
     * (non-Javadoc)
     * 
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    private String processTree(Tree tree)
    {
        String text = tree.getText();
        StringBuffer output = new StringBuffer("Text = " + text);
        if (text != null)
        {
            if (text.equals("CREATE_PACKAGE")) 
            {
                System.out.println("Create Package");
                // PACKAGE_NAME, PROCEDURE_BODY, PROCEDURE_BODY, PROCEDURE_BODY, FUNCTION_BODY
            }
            else if (text.equals("FUNCTION_BODY"))
            {
                System.out.println("Function Body");
                // eg.
                // FUNCTION_DECLARATION,
                // VARIABLE_DECLARATION, VARIABLE_DECLARATION,
                // VARIABLE_DECLARATION,
                // :=, :=, :=, :=, :=, :=, :=, :=, :=, CHAIN, CHAIN,
                // IF_STATEMENT, CHAIN, :=, LOOP_STATEMENT, RETURN_STATEMENT
            }
            else if (text.equals("PROCEDURE_BODY"))
            {
                System.out.println("Procedure Body");
                // eg.
                // PROCEDURE_DECLARATION,
                // VARIABLE_DECLARATION, VARIABLE_DECLARATION,
                // CHAIN, :=, :=, :=, :=, IF_STATEMENT, :=, :=, :=, :=, :=, :=,
                // IF_STATEMENT, CHAIN
                // each := can have function calls (known as chains)
                // each chain can be a function call, single word or other...
            } 
        }
        output.append("\nType = " + tree.getType());
        output.append("\nChild Count = " + tree.getChildCount());
        System.out.println(output);
        for (int i = 0; i < tree.getChildCount(); i++)
        {
            Tree child = tree.getChild(i);
            output.append(processTree(child));
        }
        return output.toString();
    }

    public void testBasicParse()
    {
        PlSqlLexer lexer;
        try
        {
            lexer = new PlSqlLexer(new ANTLRNoCaseFileStream(
                    "C:\\dev\\workspace\\sql_243\\service_manager\\svcmgr_managed_connection.pkb"));
            // "C:\\dev\\workspace\\sql_243\\service_manager\\svcmgr_port_allocation.pkb"));
            CommonTokenStream tokens = new CommonTokenStream();
            tokens.setTokenSource(lexer);
            PlSqlParser parser = new PlSqlParser(tokens);

            final PlSqlParser.start_rule_return ptree;
            ptree = parser.start_rule();
            // RootSegment rs = new RootSegment(ptree);
            Tree tree = (Tree) ptree.getTree();
            processTree(tree);
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (RecognitionException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
