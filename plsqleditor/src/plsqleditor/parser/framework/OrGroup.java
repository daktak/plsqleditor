//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\OrGroup.java

package plsqleditor.parser.framework;

import java.util.Set;
import java.util.Stack;

import org.eclipse.jface.text.Position;
import plsqleditor.parser.util.IInput;
import plsqleditor.parser.util.ParseToken;
import plsqleditor.parser.util.TokenGrabber;

/**
 * Of the contained parse specifications, one and only one will be parsed
 * correctly.
 */
public class OrGroup extends CompositeParseSpecification
{
    /**
     * @param name
     * @roseuid 43143C6D0167
     */
    public OrGroup(String name)
    {
        super(name);
    }

    /**
     * This method will return a parse result which matches one of the parse
     * specifications contained in the call to getContainedParseSpecifications()
     * 
     * @param curPos
     * @param resultingPos
     * @param input
     * @return plsqleditor.parser.framework.IParseResult
     * @throws ParseException
     * @roseuid 43143C7F0186
     */
    public IParseResult parse(Position curPos, Position resultingPos, IInput input)
            throws ParseException
    {
        ParseSpecificationHolder[] specs = getContainedParseSpecifications();
        int furthestPosition = curPos.offset;
        ParseException pe = null;
        for (int i = 0; i < specs.length; i++)
        {
            ParseSpecificationHolder holder = specs[i];
            try
            {
                return holder.parse(curPos, resultingPos, input);
            }
            catch (ParseException e)
            {
                int exceptionPosition = e.getOffset();
                if (exceptionPosition > furthestPosition)
                {
                    pe = e;
                    furthestPosition = exceptionPosition;
                }
            }
        }

        if (pe != null)
        {
            throw pe;
        }

        raiseException(curPos, input);
        return null; // cannot be reached - raiseException always raises exception
    }

    /**
     * This method checks that no infinite loop is contained in the
     * specification. This throws a ContainsLoopException if there is a possible
     * loop. A loop is determined to be possible if ANY one of the contained
     * specifications contains this spec (or a previous spec in the sequence
     * supplied). This implementation overrides the default implementation
     * supplied by the {@link CompositeParseSpecification}.
     * 
     * @param previouslyContainedParseSpecifications The list of <b>names</b>
     *            of the previous possible calls in a sequence in which this
     *            specification could being called.
     */
    public void checkForInfiniteLoop(Stack previouslyContainedSpecs) throws ContainsLoopException
    {
        String name = getName();
        int index = previouslyContainedSpecs.indexOf(name);
        if (index != -1)
        {
            // boolean iShouldContinue = ((Boolean)
            // previouslyContainedSpecs.elementAt(index + 1)).booleanValue();
            boolean iShouldContinue = previouslyContainedSpecs.lastIndexOf(Boolean.TRUE) != -1;
            if (iShouldContinue)
            {
                return;
            }
            throw new ContainsLoopException("There is a possible infinite loop in this list:"
                    + previouslyContainedSpecs + ", " + name);
        }
        previouslyContainedSpecs.push(name);
        previouslyContainedSpecs.push(new Boolean(false));
        ParseSpecificationHolder[] specs = getContainedParseSpecifications();
        for (int i = 0; i < specs.length; i++)
        {
            try
            {
                ParseSpecificationHolder specHolder = specs[i];
                IParseSpecification spec = specHolder.getParseSpecification();
                spec.checkForInfiniteLoop(previouslyContainedSpecs);
            }
            catch (ContainsLoopException e)
            {
                previouslyContainedSpecs.pop();
                previouslyContainedSpecs.pop();
                throw e;
            }
        }
        previouslyContainedSpecs.pop();
        previouslyContainedSpecs.pop();
        return;
    }

    private void raiseException(Position curPos, IInput input) throws ParseException
    {
        TokenGrabber tg = new TokenGrabber(input, curPos.getOffset(), DelimiterManager.instance()
                .getAllDelimiters());
        ParseToken token = tg.nextToken();
        Set firstTokens = getFirstTokens();
        Position tokenPos = curPos;
        String tok = "EOF";
        if (token != null)
        {
            tokenPos = token.getTokenPosition();
            tok = token.getToken();
        }
        throw new ParseException("The token [" + tok
                + "] satisfied none of the expected tokens. One of " + firstTokens
                + " was expected" + getLoc(tokenPos), tok, tokenPos.getOffset(), tokenPos
                .getLength());
    }

    public String toString(int depth)
    {
        if (depth == 0)
        {
            return super.toString(0);
        }
        ParseSpecificationHolder[] specs = getContainedParseSpecifications();
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        for (int i = 0; i < specs.length; i++)
        {
            ParseSpecificationHolder spec = specs[i];
            sb.append(spec.toString(depth - 1));
            if (i < specs.length - 1)
            {
                sb.append(" | ");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public void addParseSpecification(IParseSpecification spec, int min, int max)
    {
        if (min != 1 || max != 1)
        {
            throw new IllegalArgumentException("An OrGroup can only accept mandatory specs");
        }
        super.addParseSpecification(spec, min, max);
    }
}
