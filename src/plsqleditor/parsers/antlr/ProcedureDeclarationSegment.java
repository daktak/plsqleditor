package plsqleditor.parsers.antlr;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

public class ProcedureDeclarationSegment extends AstMethodSegment
{
    public ProcedureDeclarationSegment(Tree tree, CommonTokenStream stream)
    {
        super(tree, "PROCEDURE_BODY", stream);
        String text = tree.getText();
        if (text == null || !text.equals("PROCEDURE_BODY"))
        {
            throw new IllegalArgumentException("The tree [" + tree + "] passed in is not a procedure declaration");
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
        return "PROCEDURE_DECLARATION";
    }
}
