//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\SequentialLoopingParseSpecification.java

package plsqleditor.parser.framework;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.text.Position;
import plsqleditor.parser.util.IInput;

/**
 * The following delimiter must not match the preceding delimiter (otherwise it
 * cannot be used as a terminator).
 */
public class SequentialLoopingParseSpecification extends CompositeParseSpecification
{

    private class IntHolder
    {
        int i;

        IntHolder(int i)
        {
            this.i = i;
        }
    }

    /**
     * @param name
     * @roseuid 431582940138
     */
    public SequentialLoopingParseSpecification(String name)
    {
        super(name);
    }

    /**
     * This method indicates whether this parse specification is actually valid
     * to use to parse something. If it is not valid, it should not be used.
     * Validity is determined by the fact that the last entry does not have a
     * maximum value greater than 1.
     * 
     * @return boolean
     * @roseuid 4315830C01E4
     */
    public boolean isValid()
    {
        ParseSpecificationHolder[] specs = getContainedParseSpecifications();
        return specs.length > 0 && specs[specs.length - 1].getMax() == 1;
    }

    /**
     * If this is successful, it runs through its contained specs, determining
     * how often to run each spec. See the sequence diagram.
     * 
     * @param curPos
     * @param resultingPos
     * @param input
     * @return plsqleditor.parser.framework.IParseResult
     * @throws ParseException
     * @roseuid 431583FD003E
     */
    public IParseResult parse(Position curPos, Position resultingPos, IInput input)
            throws ParseException
    {
        if (!isValid())
        {
            throw new ParseException("This parse specification is not valid", "", curPos
                    .getOffset(), curPos.getLength());
        }
        ParseSpecificationHolder[] specs = getContainedParseSpecifications();
        CompositeParseResult result = new CompositeParseResult(this);
        Position tempEnd = new Position(0, 0);
        for (int i = 0; i < specs.length; i++)
        {
            ParseSpecificationHolder spec = specs[i];
            int numLoops = 0;
            int max = spec.getMax();
            int min = spec.getMin();
            for (int j = min; j > 0; j--)
            {
                numLoops++;

                // if this throws an exception, we have failed a mandatory
                // requirement
                IParseResult localResult = spec.parse(curPos, tempEnd, input);
                result.addParseResult(localResult);
                curPos = tempEnd;
                tempEnd = new Position(0, 0);
            }
            if (numLoops < max)
            {
                for (; numLoops < max; numLoops++)
                {
                    IntHolder holder = new IntHolder(i);
                    IParseResult localResult = parseNext(curPos, input, specs, tempEnd, holder);
                    if (localResult != null)
                    {
                        i = holder.i;
                        result.addParseResult(localResult);
                        curPos = tempEnd;
                        tempEnd = new Position(0, 0);
                        break;
                    }
                    else
                    {
                        localResult = spec.parse(curPos, tempEnd, input);
                        result.addParseResult(localResult);
                        curPos = tempEnd;
                        tempEnd = new Position(0, 0);
                    }
                }
            }
        }
        resultingPos.setOffset(curPos.getOffset());
        return result;
    }

    /**
     * This method performs the lookahead logic to determine whether any of the
     * ParseSpecifications in <code>specs</code> after when the spec located
     * at <code>specs[i]</code> can be satisfied (and if so, then
     * <code>specs[i]</code> is finished).
     * 
     * @param curPos The current position to start the parse from.
     * 
     * @param input The input being parsed.
     * 
     * @param specs The array of specifications that we are making our way
     *            through.
     * 
     * @param tempEnd The value to store the end position in.
     * 
     * @param i The index into the specs of the currently parsed (and optional)
     *            spec.
     * 
     * @return The valid result of a parse, or null if there is no valid result.
     */
    private IParseResult parseNext(Position curPos,
                                   IInput input,
                                   ParseSpecificationHolder[] specs,
                                   Position tempEnd,
                                   IntHolder i)
    {
        i.i += 1;
        ParseSpecificationHolder next = specs[i.i];
        try
        {
            return next.parse(curPos, tempEnd, input);
        }
        catch (ParseException e)
        {
            if (next.getMin() == 0)
            {
                return parseNext(curPos, input, specs, tempEnd, i);
            }
        }
        return null;
    }
    
    public Set getFirstTokens()
    {
        Set set = new TreeSet(); 
        ParseSpecificationHolder [] specs = getContainedParseSpecifications();
        for (int i = 0; i < specs.length; i++)
        {
            ParseSpecificationHolder spec = specs[i];
            IParseSpecification pspec = spec.getParseSpecification(); 
            Set subSet = pspec.getFirstTokens(); 
            int min = spec.getMin();
            for (Iterator it = subSet.iterator(); it.hasNext();)
            {
                String token = (String) it.next();
                if (!set.contains(token))
                {
                    set.add(token);
                }
            }
            if (min > 0)
            {
                break;
            }
        }
        return set;
    }
}
