package plsqleditor.parsers;

import org.antlr.runtime.tree.Tree;

/**
 * This class represents the body of an AstTree that contains a function declaration.
 * 
 * Function Bodies contain segments similar to :
 * FUNCTION_DECLARATION,
 * VARIABLE_DECLARATION, VARIABLE_DECLARATION,
 * VARIABLE_DECLARATION,
 * :=, :=, :=, :=, :=, :=, :=, :=, :=, CHAIN, CHAIN,
 * IF_STATEMENT, CHAIN, :=, LOOP_STATEMENT, 
 * RETURN_STATEMENT
 *
 * @author Toby Zines
 */
public class FunctionDeclarationSegment extends AstDeclarationContainerSegment
{

    /**
     * @param tree
     */
    public FunctionDeclarationSegment(Tree tree)
    {
        super(tree, AstDeclarationContainerSegment.FUNCTION_DECLARATION);
        String text = tree.getText();
        if (text == null || !text.equals(AstDeclarationContainerSegment.FUNCTION_DECLARATION))
        {
            throw new IllegalArgumentException("The tree [" + tree + "] passed in is not a function declaration");
        }
    }

}
