/**
 * 
 */
package plsqleditor.parsers.antlr;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

import plsqleditor.stores.PlSqlType;

/**
 * This class represents a block of code within a sql file. 
 * @author Toby Zines
 *
 */
public class AstCodeSegment extends AstDeclarationSegment
{
    public AstCodeSegment(Tree tree, String type, CommonTokenStream stream)
    {
        // types include LOOP_STATEMENT, IF_STATEMENT, RETURN_STATEMENT, := (assignment)
        super(tree, type, stream); 
    }

    /**
     * This method indicates the type of the code segment,
     * this could be a function call, proc call, boolean statement,
     */
    public String getCodeType()
    {
        //TODO implement this
        return null;
    }
    
    public Parameter [] getParameters()
    {
        return null; // TODO implement this
    }
    
    /**
     * This method gets the type, or return type of the code segment
     */
    PlSqlType getPlSqlReturnType()
    {
        return null;
        // TODO implement this
    }

    @Override
    protected String getSegmentNameNodeName()
    {
        return "too generic to know for sure";
    }
}
