/**
 * 
 */
package plsqleditor.parsers;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.Tree;

/**
 * @author Toby Zines
 *
 */
public class AstPackageSegment extends AstDeclarationContainerSegment
{
    public AstPackageSegment(Tree tree)
    {
        super(tree, PACKAGE_NAME);
    }

    public List<FunctionDeclarationSegment> getFunctions()
    {
        List<FunctionDeclarationSegment> toReturn = new ArrayList<FunctionDeclarationSegment>();
        return toReturn;
    }

    public List<ProcedureDeclarationSegment> getProcedures()
    {
        List<ProcedureDeclarationSegment> toReturn = new ArrayList<ProcedureDeclarationSegment>();
        return toReturn;
    }
}
