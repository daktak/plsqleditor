//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\ParseSpecification.java

package plsqleditor.parser.framework;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.IInput;
import plsqleditor.parser.util.IInput.BadLocationException;

/**
 * This class is the abstract superclass of a parse specification.
 */
public abstract class ParseSpecification implements IParseSpecification
{
    /**
     * This field represents the name of the particular specification, so that
     * the type of string or string sequence can be more easily identified. It
     * allows sub trees within the main parse tree to be given identifiers (such
     * as "Expression" or "AssignmentExpression". Another case might be the name
     * "Parameter" or "Variable" to be used to describe two instances of the
     * same class (SimpleParseSpecification).
     */
    private String myName;

    /**
     * @param name
     * @roseuid 4303D87A0212
     */
    public ParseSpecification(String name)
    {
        myName = name;
    }

    /**
     * @param curPos
     * @param resultingPos
     * @param input
     * @return plsqleditor.parser.framework.IParseResult
     * @roseuid 43126DB7036B
     */
    public abstract IParseResult parse(Position curPos, Position resultingPos, IInput input)
            throws ParseException;

    /**
     * This method returns true if a call to
     * {@link #parse(Position, Position, IInput)} returns anything other than
     * null.
     * 
     * @param curPos
     * @param resultingPos
     * @param input
     * @return boolean
     * @roseuid 4303E2E70192
     */
    public boolean isSatisfiedBy(Position curPos, Position resultingPos, IInput input)
    {
        try
        {
            return parse(curPos, resultingPos, input) != null;
        }
        catch (ParseException e)
        {
            return false;
        }
    }

    /**
     * This method returns false by default, making it mandatory.
     * 
     * @return boolean
     * @roseuid 4313C8C00242
     */
    public boolean isOptional()
    {
        return false;
    }

    public String getName()
    {
        return myName;
    }

    /**
     * This method produces the string that identifies the line and column of a
     * parse error.
     * 
     * @param p The position of the error.
     * 
     * @param input The input from which the position was obtained.
     * 
     * @return string that identifies the line and column of a parse error.
     */
    protected String getPosition(Position p, IInput input)
    {
        try
        {
            int offset = p.getOffset();
            int line = input.getLineOfOffset(offset);
            int lineOffset = input.getLineOffset(line);
            int charInLine = offset - lineOffset;
            return "(Line " + line + ", column " + charInLine + ")";
        }
        catch (BadLocationException e)
        {
            return "Illegal position [" + p.getOffset() + "] passed for input of length ["
                    + input.getLength() + "]";
        }
    }

    public String toString()
    {
        return getName();
    }
    
    public String toString(int depth)
    {
        return getName();
    }
    
    public Set getFirstTokens()
    {
        return new TreeSet();
    }
    
    protected String getLoc(Position tokenPos)
    {
        return " location " + tokenPos.getOffset() + " length " + tokenPos.getLength(); 
    }


}
