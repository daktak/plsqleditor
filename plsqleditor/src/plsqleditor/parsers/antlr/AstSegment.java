/**
 * 
 */
package plsqleditor.parsers.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;
import org.eclipse.jface.text.Position;

import plsqleditor.parsers.Segment;

/**
 * This class is a Segment that is specifically an AST sub tree of an ANTLR
 * parsed grammar tree. This class wraps the Segment get methods and returns the
 * values extracted from the internal antlr tree.
 * 
 * @author Toby Zines
 */
public class AstSegment extends Segment
{
    /**
     * This is the antlr tree that the AstSegment is wrapping.
     */
    private Tree myTree;


    /**
     * This field will be FUNCTION_DECLARATION or PROCEDURE_DECLARATION
     * indicating the type of declaration segment is is.
     */
    private String                      myType;


    private CommonTokenStream myTokenStream;

    public AstSegment(Tree tree, String type, CommonTokenStream tokenStream)
    {
        super("AstSegment", new Position(tree.getTokenStartIndex(), tree.getTokenStopIndex()
                - tree.getTokenStartIndex())); // TODO check what segment type
        // is needed
        setName(determineName(tree));
        myTree = tree;
        myType = type;
        myTokenStream = tokenStream;
    }

    /**
     * @return the tokenStream
     */
    protected CommonTokenStream getTokenStream()
    {
        return myTokenStream;
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

    public static AstSegment generateSegment(Tree tree, CommonTokenStream stream)
    {
        AstSegment toReturn = null;
        String text = tree.getText();
        if (text != null)
        {
            if (text.equals("CREATE_PACKAGE"))
            {
                toReturn = new AstPackageDeclarationSegment(tree,stream);
                // PACKAGE_NAME, PROCEDURE_BODY, PROCEDURE_BODY, PROCEDURE_BODY,
                // FUNCTION_BODY
            }
            else if (text.equals("FUNCTION_BODY"))
            {
                toReturn = new FunctionDeclarationSegment(tree,stream);
                // eg.
                // FUNCTION_DECLARATION,
                // VARIABLE_DECLARATION, VARIABLE_DECLARATION,
                // VARIABLE_DECLARATION,
                // :=, :=, :=, :=, :=, :=, :=, :=, :=, CHAIN, CHAIN,
                // IF_STATEMENT, CHAIN, :=, LOOP_STATEMENT, RETURN_STATEMENT
            }
            else if (text.equals("PROCEDURE_BODY"))
            {
                toReturn = new ProcedureDeclarationSegment(tree,stream);
                // eg.
                // PROCEDURE_DECLARATION,
                // VARIABLE_DECLARATION, VARIABLE_DECLARATION,
                // CHAIN, :=, :=, :=, :=, IF_STATEMENT, :=, :=, :=, :=, :=, :=,
                // IF_STATEMENT, CHAIN
                // each := can have function calls (known as chains)
                // each chain can be a function call, single word or other...
            }
            else if (text.equals("CURSOR_DECLARATION"))
            {
                toReturn = new AstCursorDeclarationSegment(tree,text,stream);
            }
            else if (text.equals("VARIABLE_DECLARATION"))
            {
                toReturn = new AstVariableDeclarationSegment(tree,stream);
            }
            else if (text.equals("PLSQL_BLOCK"))
            {
                toReturn = new AstCodeSegment(tree, text,stream); 
            }
            else if (text.equals("RETURN_STATEMENT"))
            {
                toReturn = new AstCodeSegment(tree, text,stream); 
            }
            else if (text.equals("IF_STATEMENT"))
            {
                toReturn = new AstCodeSegment(tree, text,stream); 
            }
            else
            {
                toReturn = new AstSegment(tree); // Record these
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
     * This method returns the start and end location in a file that this
     * segment is in.
     * 
     * @return
     */
    public Position getLocation()
    {
        return null;
        // TODO implement this
    }

    /**
     * This method gets a particular segment contained within this segment
     * contained at the identified location.
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

    public String toString()
    {
        return myTree.toStringTree();
        // StringBuffer sb = new StringBuffer(500);
        // for (Segment segment : getContainedSegments())
        // {
        // sb.append(segment.toString());
        // }
    }
}
