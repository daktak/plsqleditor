package plsqleditor.parsers;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.Tree;

/**
 * This class represents the body of a file that contains declarations.
 * It is probably the superclass of any pl-sql file, but at the very least,
 * it is the parent of structures such as those outlined below.
 * Package Bodies (which contain):
 * PACKAGE_NAME, PROCEDURE_BODY, PROCEDURE_BODY, PROCEDURE_BODY, FUNCTION_BODY
 * 
 * Function Bodies (which contain) :
 * FUNCTION_DECLARATION,
 * VARIABLE_DECLARATION, VARIABLE_DECLARATION,
 * VARIABLE_DECLARATION,
 * :=, :=, :=, :=, :=, :=, :=, :=, :=, CHAIN, CHAIN,
 * IF_STATEMENT, CHAIN, :=, LOOP_STATEMENT, 
 * RETURN_STATEMENT
 * 
 * Procedure Bodies (which contain) :
 * PROCEDURE_DECLARATION,
 * VARIABLE_DECLARATION, VARIABLE_DECLARATION,
 * CHAIN, :=, :=, :=, :=, IF_STATEMENT, :=, :=, :=, :=, :=, :=,
 * IF_STATEMENT, CHAIN
 * 
 * @author Toby Zines
 */
public abstract class AstDeclarationContainerSegment extends AstSegment
{
    public static final String VARIABLE_DECLARATION = "VARIABLE_DECLARATION";
    public static final String FUNCTION_DECLARATION = "FUNCTION_DECLARATION";
    public static final String PROCEDURE_DECLARATION = "PROCEDURE_DECLARATION";
    public static final String PACKAGE_NAME = "PACKAGE_NAME";
    private List<AstSegment> myVariables;
    private List<AstSegment> myStatements;
    private AstSegment       myDeclarationSegment;
    /** 
     * This field will be FUNCTION_DECLARATION or PROCEDURE_DECLARATION
     * indicating the type of declaration segment is is. 
     */
    private String           myType;
    
    public AstDeclarationContainerSegment(Tree tree, String type)
    {
        super(tree);
        myType = type;
    }

    /**
     * This method gets the actual declaration text aspect of the segment
     * @return
     */
    public AstSegment getDeclaration()
    {
        if (myDeclarationSegment == null)
        {
            Tree tree = getTree();
            int childCount = tree.getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                Tree child = tree.getChild(i);
                String text = child.getText();
                if (text != null && text.equals(myType))
                {
                    myDeclarationSegment = new AstSegment(child);
                    break;
                }
            }
        }
        return myDeclarationSegment;
    }
    
    public List<AstSegment> getVariables()
    {
        if (myVariables == null)
        {
            myVariables = new ArrayList<AstSegment>();
            Tree tree = getTree();
            int childCount = tree.getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                Tree child = tree.getChild(i);
                String text = child.getText();
                if (text != null && text.equals(AstDeclarationContainerSegment.VARIABLE_DECLARATION))
                {
                    myVariables.add(new AstSegment(child));
                }
            }
        }
        return myVariables;
    }
    
    public List<AstSegment> getStatements()
    {
        if (myStatements == null)
        {
            myStatements = new ArrayList<AstSegment>();
            Tree tree = getTree();
            int childCount = tree.getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                Tree child = tree.getChild(i);
                String text = child.getText();
                if (text != null && !text.equals(FUNCTION_DECLARATION) && !text.equals(PROCEDURE_DECLARATION))
                {
                    myStatements.add(new AstSegment(child));
                }
            }
        }
        return myStatements;
    }
    
    
}
