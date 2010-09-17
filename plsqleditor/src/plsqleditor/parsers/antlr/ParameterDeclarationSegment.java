package plsqleditor.parsers.antlr;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

public class ParameterDeclarationSegment extends AstDeclarationSegment
{

    public ParameterDeclarationSegment(Tree tree, CommonTokenStream stream)
    {
        super(tree, "PARAMETER_DECLARATION", stream);
    }

    @Override
    protected String getSegmentNameNodeName()
    {
        return "first child is the name";
    }

}
