package plsqleditor.parsers;

import org.antlr.runtime.tree.Tree;

public class ProcedureDeclarationSegment extends AstDeclarationContainerSegment
{
    public ProcedureDeclarationSegment(Tree tree)
    {
        super(tree, PROCEDURE_DECLARATION);
    }
}
