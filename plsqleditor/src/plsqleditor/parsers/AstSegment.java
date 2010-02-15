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
 * This class is a Segment that is specifically an AST sub tree of an ANTLR parsed grammar tree.
 * This class wraps the Segment get methods and returns the values extracted from the internal antlr tree.
 * 
 * @author Toby Zines
 */
public class AstSegment extends Segment
{
    /**
     * This is the antlr tree that the AstSegment is wrapping.
     */
    private Tree myTree;

    public AstSegment(Tree tree)
    {
        super("AstSegment",new Position(tree.getTokenStartIndex(),tree.getTokenStopIndex() - tree.getTokenStartIndex())); // TODO check what segment type is needed
        setName(determineName(tree));
        myTree = tree;
    }

    private String determineName(Tree tree)
    {
        // TODO Auto-generated method stub
        return tree.getText();
    }

    public String format()
    {
        // TODO Auto-generated method stub
        return super.format();
    }

    public static AstSegment generateSegment(Tree tree)
    {
        AstSegment toReturn = null;
        String text = tree.getText();
        if (text != null)
        {
            if (text.equals("CREATE_PACKAGE")) 
            {
                toReturn = new AstPackageSegment(tree);
                // PACKAGE_NAME, PROCEDURE_BODY, PROCEDURE_BODY, PROCEDURE_BODY, FUNCTION_BODY
            }
            else if (text.equals("FUNCTION_BODY"))
            {
                toReturn = new FunctionDeclarationSegment(tree);
                // eg.
                // FUNCTION_DECLARATION,
                // VARIABLE_DECLARATION, VARIABLE_DECLARATION,
                // VARIABLE_DECLARATION,
                // :=, :=, :=, :=, :=, :=, :=, :=, :=, CHAIN, CHAIN,
                // IF_STATEMENT, CHAIN, :=, LOOP_STATEMENT, RETURN_STATEMENT
            }
            else if (text.equals("PROCEDURE_BODY"))
            {
                toReturn = new ProcedureDeclarationSegment(tree);
                // eg.
                // PROCEDURE_DECLARATION,
                // VARIABLE_DECLARATION, VARIABLE_DECLARATION,
                // CHAIN, :=, :=, :=, :=, IF_STATEMENT, :=, :=, :=, :=, :=, :=,
                // IF_STATEMENT, CHAIN
                // each := can have function calls (known as chains)
                // each chain can be a function call, single word or other...
            } 
        }
        else
        {
            toReturn = new AstSegment(tree);
        }
        
        return toReturn;
    }
    
    public List<Segment> getContainedSegments()
    {
        List<Segment> segments = new ArrayList<Segment>();
        int childCount = myTree.getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            Tree child = myTree.getChild(i);
            AstSegment segment = generateSegment(child);
            segments.add(segment);
        }
        return segments;
    }

    /**
     * @return the tree
     */
    protected Tree getTree()
    {
        return myTree;
    }

    /**
     * @param tree the tree to set
     */
    protected void setTree(Tree tree)
    {
        myTree = tree;
    }
    
    /**
     * This method returns the start and end location in a file that this segment is in.
     * @return
     */
    public Position getLocation()
    {
        return null;
        // TODO implement this
    }
    
    /**
     * This method gets a particular segment contained within this segment contained at
     * the identified location.
     * 
     * @param location
     * @return The segment at the specified location
     */
    public AstSegment getContainedSegmentForLocation(Position location)
    {
        return null;
        // TODO implement this
    }
    
    
    /**
     * This method gets the documentation associated to this segment.
     */
    public String getDocumentation()
    {
        return null;
        // TODO implement this
    }
}
