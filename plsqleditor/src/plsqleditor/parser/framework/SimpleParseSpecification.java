//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\SimpleParseSpecification.java

package plsqleditor.parser.framework;

import java.util.Stack;

import org.eclipse.jface.text.Position;
import plsqleditor.parser.util.IInput;
import plsqleditor.parser.util.ParseToken;
import plsqleditor.parser.util.TokenGrabber;

/**
 * This class represents any generic single string that can be parsed. It can
 * contain comments and spaces before the actual single token, and it will not
 * be a delimiter (as per the generic superclass).
 */
public class SimpleParseSpecification extends ParseSpecification
{

    /**
     * @param name
     * @roseuid 431584E70280
     */
    public SimpleParseSpecification(String name)
    {
        super(name);
    }

    /**
     * This method overrides the base parse to just read a single token, and
     * ensure that it is not a delimiter.
     * 
     * @param curPos
     * @param resultingPos
     * @param input
     * @return plsqleditor.parser.framework.IParseResult
     * @throws ParseException when a delimiter is found at the current parse location.
     * @roseuid 4315850B0242
     */
    public IParseResult parse(Position curPos, Position resultingPos, IInput input) throws ParseException
    {
        TokenGrabber grabber = new TokenGrabber(input, curPos.getOffset(), DelimiterManager
                .instance().getAllDelimiters());
        ParseToken token = grabber.nextToken();
        if (token == null)
        {
            throw new ParseException("The end of the input was reached while expecting a ["
                    + toString() + "]", null, curPos.getOffset(), curPos.getLength());
        }
        String tokenVal = token.getToken().toUpperCase();
        Position tokenPos = token.getTokenPosition();
        if (DelimiterManager.instance().isDelimiter(tokenVal))
        {
            throw new ParseException("Expected a NON Delimiter at this position " + getPosition(tokenPos, input), tokenVal, tokenPos.getOffset(), tokenPos.getLength());
        }
        SimpleParseResult result = new SimpleParseResult(this, token.getPreTokenString(), token
                .getPreTokenPosition(), token.getToken(), tokenPos);
        resultingPos.setOffset(tokenPos.getOffset() + tokenPos.getLength());
        return result;
    }
    
    /**
     * This method does nothing but exits successfully, since it can never cause 
     * an infinite loop.
     *
     * @param previouslyContainedParseSpecifications The list of <b>names</b> of the previous possible calls in a sequence in which this specification could being called.
     */
    public void checkForInfiniteLoop(Stack previouslyContainedSpecs)
    {
    	// do nothing - always successful
    }

//    public String toString()
//    {
//        return "Simple String (" + getName() + ")";
//    }
}
