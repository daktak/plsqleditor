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
public class AnyTokenSpecification extends ParseSpecification
{

    /**
     * @param name
     * @roseuid 431584E70280
     */
    public AnyTokenSpecification(String name)
    {
        super(name);
    }

    /**
     * This method overrides the base parse to just read a single token, and
     * ignore whether it a delimiter or not - either can be returned from this.
     * 
     * @param curPos
     * @param resultingPos
     * @param input
     * @return plsqleditor.parser.framework.IParseResult
     * @throws ParseException when a delimiter is found at the current parse
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
        String tokenVal = token.getToken();
        Position tokenPos = token.getTokenPosition();
        IParseResult result = null;
        if (DelimiterManager.instance().isDelimiter(tokenVal))
        {
            result = new Delimiter(this, token.getPreTokenString(), token
                    .getPreTokenPosition(), tokenVal, tokenPos);
        }
        else
        {
            result = new SimpleParseResult(this, token.getPreTokenString(), token
                    .getPreTokenPosition(), tokenVal, tokenPos);
        }
        resultingPos.setOffset(tokenPos.getOffset() + tokenPos.getLength());
        return result;
    }

    public void checkForInfiniteLoop(Stack previouslyContainedSpecs) throws ContainsLoopException
    {
        // do nothing
    }

//    public String toString()
//    {
//        return "Any Token (" + getName() + ")";
//    }
}
