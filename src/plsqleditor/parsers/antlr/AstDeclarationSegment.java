package plsqleditor.parsers.antlr;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

/**
 * This class represents the body of a file that contains declarations. It is
 * probably the superclass of any pl-sql file, but at the very least, it is the
 * parent of structures such as those outlined below. Package Bodies (which
 * contain): PACKAGE_NAME, PROCEDURE_BODY, PROCEDURE_BODY, PROCEDURE_BODY,
 * FUNCTION_BODY
 * 
 * Function Bodies (which contain) : FUNCTION_DECLARATION, VARIABLE_DECLARATION,
 * VARIABLE_DECLARATION, VARIABLE_DECLARATION, :=, :=, :=, :=, :=, :=, :=, :=,
 * :=, CHAIN, CHAIN, IF_STATEMENT, CHAIN, :=, LOOP_STATEMENT, RETURN_STATEMENT
 * 
 * Procedure Bodies (which contain) : PROCEDURE_DECLARATION,
 * VARIABLE_DECLARATION, VARIABLE_DECLARATION, CHAIN, :=, :=, :=, :=,
 * IF_STATEMENT, :=, :=, :=, :=, :=, :=, IF_STATEMENT, CHAIN
 * 
 * @author Toby Zines
 */
public abstract class AstDeclarationSegment extends AstSegment
{
    List<FunctionDeclarationSegment>    myFunctions;
    List<ProcedureDeclarationSegment>   myProcedures;
    List<AstVariableDeclarationSegment> myVariables;
    List<AstTypeDeclarationSegment>     myTypes;
    private List<AstSegment>            myExtras;
    private AstSegment mySegmentNameNode;

    public AstDeclarationSegment(Tree tree, String type, CommonTokenStream stream)
    {
        super(tree, type, stream);
    }

    protected void parseTree()
    {
        if (myFunctions == null)
        {
            myFunctions = new ArrayList<FunctionDeclarationSegment>();
            myProcedures = new ArrayList<ProcedureDeclarationSegment>();
            myVariables = new ArrayList<AstVariableDeclarationSegment>();
            myTypes = new ArrayList<AstTypeDeclarationSegment>();
            myExtras = new ArrayList<AstSegment>();

            for (int i = 0; i < getTree().getChildCount(); i++)
            {
                Tree child = getTree().getChild(i);
                AstSegment segment = generateSegment(child, getTokenStream());
                if (segment instanceof FunctionDeclarationSegment)
                {
                    myFunctions.add((FunctionDeclarationSegment) segment);
                }
                else if (segment instanceof ProcedureDeclarationSegment)
                {
                    myProcedures.add((ProcedureDeclarationSegment) segment);
                }
                else if (segment instanceof ProcedureDeclarationSegment)
                {
                    myVariables.add((AstVariableDeclarationSegment) segment);
                }
                else if (segment instanceof ProcedureDeclarationSegment)
                {
                    myTypes.add((AstTypeDeclarationSegment) segment);
                }
                else
                {
                    String segName = segment.getName();
                    if (segName != null && segName.equals(getSegmentNameNodeName()))
                    {
                        setSegmentNameNode(segment);
                    }
                    else
                    {
                        myExtras.add(segment);
                    }
                }
            }
        }
    }

    private void setSegmentNameNode(AstSegment segment)
    {
        if (mySegmentNameNode != null)
        {
            throw new IllegalStateException("There is already a segment name node for segment [" + toString() + "]: segment [" + segment.toString() +"] is not needed");
        }
        mySegmentNameNode = segment;
    }

    /**
     * This method gets the name (text from Tree.getText()) of the Tree node that holds the 
     * name of the tree containing it.
     * For example, a PackageDeclaration segment will contain a node with a getText() value of PACKAGE_NAME,
     * a FunctionDeclaration segment will contain a node with a getText() value of FUNCTION_DECLARATION etc.
     * 
     * @return The name of the direct child node that provides the identifying name of the parent node (if there is one).
     */
    protected abstract String getSegmentNameNodeName();

    /**
     * This method gets the functions declared directly in this package if the
     * <code>isDeep</code> is set to <code>false</code> and goes into each of
     * the contained functions and procedures too otherwise.
     * 
     * @param isDeep if <code>true</code>, this will also check the functions in
     *            the functions and procedures within this package.
     * 
     */
    public List<FunctionDeclarationSegment> getFunctions(boolean isDeep)
    {
        if (myFunctions == null)
        {
            parseTree();
        }
        if (!isDeep)
        {
            return myFunctions;
        }
        else
        {
            List<FunctionDeclarationSegment> toReturn = new ArrayList<FunctionDeclarationSegment>();
            toReturn.addAll(myFunctions);
            for (AstMethodSegment functionDeclarationSegment : myFunctions)
            {
                toReturn.addAll(functionDeclarationSegment.getFunctions(isDeep));
            }
            for (ProcedureDeclarationSegment procedureDeclarationSegment : myProcedures)
            {
                toReturn.addAll(procedureDeclarationSegment.getFunctions(isDeep));
            }
            return toReturn;
        }
    }

    /**
     * This method gets the procedures declared directly in this package if the
     * <code>isDeep</code> is set to <code>false</code> and goes into each of
     * the contained functions and procedures too otherwise.
     * 
     * @param isDeep if <code>true</code>, this will also check the procedures
     *            in the functions and procedures within this package.
     * 
     */
    public List<ProcedureDeclarationSegment> getProcedures(boolean isDeep)
    {
        if (myProcedures == null)
        {
            parseTree();
        }
        if (!isDeep)
        {
            return myProcedures;
        }
        else
        {
            List<ProcedureDeclarationSegment> toReturn = new ArrayList<ProcedureDeclarationSegment>();
            toReturn.addAll(myProcedures);
            for (AstMethodSegment functionDeclarationSegment : myFunctions)
            {
                toReturn.addAll(functionDeclarationSegment.getProcedures(isDeep));
            }
            for (ProcedureDeclarationSegment procedureDeclarationSegment : myProcedures)
            {
                toReturn.addAll(procedureDeclarationSegment.getProcedures(isDeep));
            }
            return toReturn;
        }
    }

    /**
     * This method gets the variables declared directly in this package if the
     * <code>isDeep</code> is set to <code>false</code> and goes right into each
     * of the functions and procedures too otherwise.
     * 
     * @param isDeep if <code>true</code>, this will also check the variables in
     *            the functions and procedures within this package.
     * 
     */
    public List<AstVariableDeclarationSegment> getVariables(boolean isDeep)
    {
        if (myVariables == null)
        {
            parseTree();
        }
        if (!isDeep)
        {
            return myVariables;
        }
        else
        {
            List<AstVariableDeclarationSegment> toReturn = new ArrayList<AstVariableDeclarationSegment>();
            toReturn.addAll(myVariables);
            for (AstMethodSegment functionDeclarationSegment : myFunctions)
            {
                toReturn.addAll(functionDeclarationSegment.getVariables(isDeep));
            }
            for (ProcedureDeclarationSegment procedureDeclarationSegment : myProcedures)
            {
                toReturn.addAll(procedureDeclarationSegment.getVariables(isDeep));
            }
            return toReturn;
        }
    }

    /**
     * This method gets the type declared directly in this package if the
     * <code>isDeep</code> is set to <code>false</code> and goes right into each
     * of the functions and procedures too otherwise.
     * 
     * @param isDeep if <code>true</code>, this will also check the types in the
     *            functions and procedures within this package.
     * 
     */
    public List<AstTypeDeclarationSegment> getTypes(boolean isDeep)
    {
        if (myTypes == null)
        {
            parseTree();
        }
        if (!isDeep)
        {
            return myTypes;
        }
        else
        {
            List<AstTypeDeclarationSegment> toReturn = new ArrayList<AstTypeDeclarationSegment>();
            toReturn.addAll(myTypes);
            for (AstMethodSegment functionDeclarationSegment : myFunctions)
            {
                toReturn.addAll(functionDeclarationSegment.getTypes(isDeep));
            }
            for (ProcedureDeclarationSegment procedureDeclarationSegment : myProcedures)
            {
                toReturn.addAll(procedureDeclarationSegment.getTypes(isDeep));
            }
            return toReturn;
        }
    }

    /**
     * @return the segmentNameNode
     */
    public AstSegment getSegmentNameNode()
    {
        return mySegmentNameNode;
    }
}
