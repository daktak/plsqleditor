package plsqleditor.parsers.antlr;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

public class AstVariableDeclarationSegment extends AstDeclarationSegment
{

    public AstVariableDeclarationSegment(Tree tree, CommonTokenStream stream)
    {
        super(tree, "VARIABLE_DECLARATION", stream);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getSegmentNameNodeName()
    {
        // TODO Auto-generated method stub
        return "don't know if there is one";
    }

}
