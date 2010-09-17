package plsqleditor.parsers.antlr;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

public class AstTypeDeclarationSegment extends AstDeclarationSegment
{

    public AstTypeDeclarationSegment(Tree tree, String type, CommonTokenStream stream)
    {
        // types can include TABLE_TYPE_DECLARATION, RECORD_TYPE_DECLARATION...
        super(tree, type, stream);
        // TODO Auto-generated constructor stub
    }

    @Override
    protected String getSegmentNameNodeName()
    {
        return "sometimes uses chain which is nasty";
    }

    
}
