//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\DelimiterSpec.java

package plsqleditor.parser.framework;

import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.IDelimiterSpecification;
import plsqleditor.parser.util.IInput;
import plsqleditor.parser.util.ParseToken;
import plsqleditor.parser.util.TokenGrabber;

/**
 * This class represents the specification for a delimiter. It is a specific
 * type of parse specification that is used. It can be one or more characters,
 * and be preceded by comments and white space.
 * 
 * Delimiters are always mandatory.
 */
public class DelimiterSpec extends ParseSpecification implements IDelimiterSpecification
{
    private String myActualDelimiter;
    private boolean myIsStandalone;

    /**
     * @param name
     * @param actualDelimiter
     * @roseuid 4313A1250138
     */
    public DelimiterSpec(String name, String actualDelimiter, boolean isStandalone)
    {
        super(name);
        if (actualDelimiter == null || actualDelimiter.length() == 0)
        {
            throw new IllegalStateException("The supplied delimiter [" + actualDelimiter
                    + "] is null or zero length");
        }
        myActualDelimiter = actualDelimiter;
        myIsStandalone = isStandalone;
        DelimiterManager.instance().addDelimiterSpec(this);
    }

    /**
     * This method returns the length of the delimiter that this is specifying.
     * 
     * @return int
     * @roseuid 4313A18001B5
     */
    public int length()
    {
        return myActualDelimiter.length();
    }

    /**
     * This method returns the first char of the delimiter which will help the
     * token grabber to determine whether the next token is a delimiter or not.
     * 
     * @return char
     * @roseuid 4313A1880213
     */
    public char firstChar()
    {
        return myActualDelimiter.charAt(0);
    }

    /**
     * This method returns {@link #myActualDelimiter}.
     * 
     * @return java.lang.String
     * 
     * @roseuid 431A54AC0261
     */
    public String getString()
    {
        return myActualDelimiter;
    }

    /**
     * 
     * @throws ParseException when a NON delimiter is found at the current parse
     *             location.
     */
    public IParseResult parse(Position curPos, Position resultingPos, IInput input)
            throws ParseException
    {
        TokenGrabber grabber = new TokenGrabber(input, curPos.getOffset(), DelimiterManager
                .instance().getAllDelimiters());
        ParseToken token = grabber.nextToken();
        if (token == null)
        {
            throw new ParseException("The end of the input was reached while expecting a ["
                    + toString() + "]", null, curPos.getOffset(), curPos.getLength());
        }
        Position tokenPos = token.getTokenPosition();
        String tokenVal = token.getToken();
        if (!tokenVal.toUpperCase().equals(myActualDelimiter))
        {
            throw new ParseException("Expected the Delimiter [" + myActualDelimiter
                    + "] at position " + getPosition(tokenPos, input) + " but received [" + tokenVal + "]", tokenVal, tokenPos
                    .getOffset(), tokenPos.getLength());
        }
        Delimiter result = new Delimiter(this, token.getPreTokenString(), token
                .getPreTokenPosition(), token.getToken(), tokenPos);
        resultingPos.setOffset(tokenPos.getOffset() + tokenPos.getLength());
        return result;
    }

    public String toString()
    {
        //return "Delimiter (" + myActualDelimiter + ")";
        if (myIsStandalone)
        {
            return "\"" + myActualDelimiter + "\"";
        }
        else
        {
            return myActualDelimiter;
        }
    }

    public boolean isStandaloneDelimiter()
    {
        return myIsStandalone;
    }

    public void checkForInfiniteLoop(Stack previouslyContainedSpecs) throws ContainsLoopException
    {
        // do nothing - always successful
    }

    public Set getFirstTokens()
    {
        Set set = new TreeSet();
        set.add(myActualDelimiter);
        return set;
    }
}
