//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\ParseResult.java

package plsqleditor.parser.framework;

import org.eclipse.jface.text.Position;

/**
 * This class represents the Base class of the return from a call to an
 * IParseSpecification to parse a particular set of text from an IInput.
 */
public abstract class ParseResult implements IParseResult
{
    /**
     * This field returns the start position, starting at the comments and
     * whitespace.
     */
    private Position            myStartPosition;

    /**
     * This field returns the end position, starting at the comments and
     * whitespace.
     */
    private Position            myEndPosition;

    private IParseSpecification myParseSpecification;

    /**
     * @param spec
     * @roseuid 4303E4B00006
     */
    public ParseResult(IParseSpecification spec)
    {
        myParseSpecification = spec;
    }

    /**
     * This method returns the start position, starting at the comments and
     * whitespace.
     * 
     * @return javax.swing.text.Position
     * @roseuid 43127F6002DE
     */
    public Position getStartPosition()
    {
        return myStartPosition;
    }

    /**
     * @param pos
     * @roseuid 4312849501F4
     */
    protected void setStartPosition(Position pos)
    {
        myStartPosition = pos;
    }

    /**
     * @return javax.swing.text.Position
     * @roseuid 431284BE0148
     */
    public Position getEndPosition()
    {
        return myEndPosition;
    }

    /**
     * @param pos
     * @roseuid 431284C301E4
     */
    protected void setEndPosition(Position pos)
    {
        myEndPosition = pos;
    }

    /**
     * This method returns the length of the parse result as a result of
     * getEndPosition().offset + getEndPosition.length -
     * getStartPosition().offset
     * 
     * @return int
     * @roseuid 43128070000F
     */
    public int getLength()
    {
        return myEndPosition.offset + myEndPosition.length - myStartPosition.offset;
    }

    /**
     * is useful because it will be the location of the first valid block of
     * addressable source.
     * 
     * @return javax.swing.text.Position
     * @roseuid 431283410109
     */
    public abstract Position getFirstNonWhitespacePosition();

    /**
     * This method returns the text that the parse result contains.
     * 
     * @return The text contained in this parse result.
     * 
     * @roseuid 431A576201A5
     */
    public abstract String getText();

    public IParseSpecification getParseSpecification()
    {
        return myParseSpecification;
    }
    
    /**
     * This method by default returns the result of a call to {@link #getText()}.
     */
    public String toString()
    {
        return getText();
    }
}
