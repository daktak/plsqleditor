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
public class AstDefaultSQLSegment extends AstDeclarationContainerSegment
{
    public AstDefaultSQLSegment(Tree tree)
    {
        super(tree, "UNKNOWN"); // TODO fix this to work as default, not "unknown"
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
