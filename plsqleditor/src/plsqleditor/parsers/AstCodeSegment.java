/**
 * 
 */
package plsqleditor.parsers;

import org.antlr.runtime.tree.Tree;

import plsqleditor.stores.PlSqlType;

/**
 * This class represents a single declaration within a sql file. 
 * @author Toby Zines
 *
 */
public class AstCodeSegment extends AstSegment
{
    public AstCodeSegment(Tree tree)
    {
        super(tree); 
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
}
