package plsqleditor.parsers.antlr;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

public class AstCursorDeclarationSegment extends AstMethodSegment
{

    public AstCursorDeclarationSegment(Tree tree, String type, CommonTokenStream stream)
    {
        super(tree, type, stream);
        String text = tree.getText();
        if (text == null || !text.equals("CURSOR_DECLARATION"))
        {
            throw new IllegalArgumentException("The tree [" + tree + "] passed in is not a cursor declaration");
        }
        // TODO get the name of the cursor
    }

    @Override
    protected String getSegmentNameNodeName()
    {
        return "SELECT_COMMAND";
    }

}
