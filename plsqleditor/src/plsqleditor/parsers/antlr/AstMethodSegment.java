package plsqleditor.parsers.antlr;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.tree.Tree;

import plsqleditor.parsers.Segment;

public abstract class AstMethodSegment extends AstDeclarationSegment
{
    /** The name (and name only) of the method */
    private String                              myMethodName;
    private List<ParameterDeclarationSegment>   myParameterList;
    private List<AstVariableDeclarationSegment> myFieldList;
    private boolean                             isExceptionHandlerRetrieved = false;
    private AstCodeSegment                      myExceptionHandler;

    public AstMethodSegment(Tree tree, String type, CommonTokenStream stream)
    {
        super(tree, type, stream);
    }

    /**
     * This method gets all the parameters from the segment.
     * 
     * @return The parameter list in the format (p1,p2,p3).
     */
    public String getParameterListAsString(boolean overrideParameterSettings)
    {
        getParameterList();
        if (myParameterList.isEmpty())
        {
            return "";
        }
        StringBuffer sb = new StringBuffer("(");
        for (Iterator<ParameterDeclarationSegment> it = myParameterList.iterator(); it.hasNext();)
        {
            ParameterDeclarationSegment p = it.next();
            // sb.append(p.toString(overrideParameterSettings));
            if (it.hasNext())
            {
                sb.append(", ");
            }
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * This method gets all the parameters from the segment.
     * 
     * @return The parameter list (as {@link Segment}s) in the format
     *         (p1,p2,p3).
     */
    public List<? extends Segment> getParameterList()
    {
        if (myParameterList == null)
        {
            myParameterList = new ArrayList<ParameterDeclarationSegment>();
            int childCount = getTree().getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                Tree child = getTree().getChild(i);
                String childText = child.getText();
                if (childText != null && childText.equals("PARAMETER_DECLARATION"))
                {
                    ParameterDeclarationSegment pds = new ParameterDeclarationSegment(child, getTokenStream());
                    myParameterList.add(pds);
                }
            }
        }
        return myParameterList;
    }

    /**
     * This method gets all the fields from the segment.
     * 
     * @return {@link #myFieldList}.
     */
    public List<? extends Segment> getFieldList()
    {
        // FIELD_DECLARATION (maybe),VARIABLE_DECLARATION or others?
        return myFieldList;
    }

    protected void setMethodName(String methodName)
    {
        myMethodName = methodName;
    }

    /**
     * @return the methodName
     */
    public String getMethodName()
    {
        return myMethodName;
    }

    public AstCodeSegment getExceptionHandler()
    {
        if (!isExceptionHandlerRetrieved)
        {
            int childCount = getTree().getChildCount();
            for (int i = 0; i < childCount; i++)
            {
                Tree child = getTree().getChild(i);
                String childText = child.getText();
                if (childText != null && childText.equals("EXCEPTION_HANDLER"))
                {
                    myExceptionHandler = new AstCodeSegment(child, childText, getTokenStream());
                    break;
                }
            }
            isExceptionHandlerRetrieved = true;
        }
        return myExceptionHandler;
    }
}
