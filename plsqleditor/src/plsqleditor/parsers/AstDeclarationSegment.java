/**
 * 
 */
package plsqleditor.parsers;

import org.antlr.runtime.tree.Tree;

/**
 * This class represents a single declaration within a sql file. 
 * @author Toby Zines
 *
 */
public class AstDeclarationSegment extends AstSegment
{
    public AstDeclarationSegment(Tree tree)
    {
        super(tree); 
    }

}
