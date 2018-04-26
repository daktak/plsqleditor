/**
 * 
 */
package plsqleditor.parsers.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

/**
 * @author Toby Zines
 *
 */
public class AstDefaultSQLSegment extends AstDeclarationSegment
{
    public AstDefaultSQLSegment(Tree tree, String type, CommonTokenStream stream)
    {
        super(tree, type, stream); // TODO fix this to work as default, not "unknown"
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

    @Override
    protected String getSegmentNameNodeName()
    {
        return "too generic to know for sure";
    }
}
