/**
 * 
 */
package plsqleditor.parsers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.tree.Tree;
import org.eclipse.jface.text.Position;

import au.com.alcatel.fulfil.tools.codecheck.parser.PlSqlParser.start_rule_return;

/**
 * This class is a Segment that is specifically the Root of an ANTLR parsed grammar tree.
 * 
 * It is the container of any pl-sql file, but at the very least,
 * it is the owner of structures such as those outlined below.
 * Package Bodies (which contain):
 * PACKAGE_NAME, PROCEDURE_BODY, PROCEDURE_BODY, PROCEDURE_BODY, FUNCTION_BODY
 * 
 * Function Bodies (which contain) :
 * FUNCTION_DECLARATION,
 * VARIABLE_DECLARATION, VARIABLE_DECLARATION,
 * VARIABLE_DECLARATION,
 * :=, :=, :=, :=, :=, :=, :=, :=, :=, CHAIN, CHAIN,
 * IF_STATEMENT, CHAIN, :=, LOOP_STATEMENT, 
 * RETURN_STATEMENT
 * 
 * Procedure Bodies (which contain) :
 * PROCEDURE_DECLARATION,
 * VARIABLE_DECLARATION, VARIABLE_DECLARATION,
 * CHAIN, :=, :=, :=, :=, IF_STATEMENT, :=, :=, :=, :=, :=, :=,
 * IF_STATEMENT, CHAIN
 * 
 * @author Toby Zines
 */
public class RootSegment extends Segment
{

    private start_rule_return myParseTree;

    private Segment [] myContainedSegments;
    
    public RootSegment(start_rule_return ptree)
    {
        super("RootSegment",new Position(0,0)); // TODO check what segment type is needed
        myParseTree = ptree;
    }

    private Segment [] parseTree(start_rule_return tree)
    {
        List <Segment> segments = new ArrayList<Segment>();
        
        Tree topTree = (Tree) myParseTree.getTree();
        topTree.getText();
        topTree.getLine();
        topTree.getTokenStartIndex();
        topTree.getTokenStopIndex();
        for (int i = 0; i < topTree.getChildCount(); i++)
        {
            Tree child = topTree.getChild(i);
            AstSegment segment = new AstSegment(child);
            segments.add(segment);
        }
        return segments.toArray(new Segment[segments.size()]);
    }
    
    public String format()
    {
        // TODO Auto-generated method stub
        return super.format();
    }

    public List getContainedSegments()
    {
        // TODO Auto-generated method stub
        return super.getContainedSegments();
    }

    public String getDocumentation()
    {
        // TODO Auto-generated method stub
        return super.getDocumentation();
    }

    public String getImageKey()
    {
        // TODO Auto-generated method stub
        return super.getImageKey();
    }

    public String toString()
    {
        // TODO Auto-generated method stub
        return super.toString();
    }

    
}
