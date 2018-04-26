package plsqleditor.parsers.antlr;

import org.antlr.runtime.CommonTokenStream;
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
public class FunctionDeclarationSegment extends AstMethodSegment
{

    /**
     * @param tree
     */
    public FunctionDeclarationSegment(Tree tree, CommonTokenStream stream)
    {
        super(tree, "FUNCTION_BODY", stream);
        String text = tree.getText();
        if (text == null || !text.equals("FUNCTION_BODY"))
        {
            throw new IllegalArgumentException("The tree [" + tree + "] passed in is not a function declaration");
        }
        int childCount = getTree().getChildCount();
        for (int i = 0; i < childCount; i++)
        {
            Tree child = getTree().getChild(i);
            String childText = child.getText();
            if (childText !=  null && childText.equals(getSegmentNameNodeName()))
            {
                setMethodName(child.getChild(0).getText());
            }
        }
    }

    @Override
    protected String getSegmentNameNodeName()
    {
        return "FUNCTION_DECLARATION";
    }

}
