//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\SequentialCompositeParseSpecification.java

package plsqleditor.parser.framework;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.jface.text.Position;

import plsqleditor.parser.util.IInput;

/**
 * This class represents a sequential set of ParseSpecifications. Some may be
 * optional, but the order must be met, and all the mandatory specifications
 * must be contained. The contained parseSpecifications may not be in a loop -
 * the sequence starts at the beginning, and proceeds sequentially until the
 * end. Each contained parse specification will be present zero or one
 * time.ained parse specification will be present zero or one time.
 */
public class SequentialCompositeParseSpecification extends CompositeParseSpecification
{

    /**
     * @param name
     * @roseuid 43158144008C
     */
    public SequentialCompositeParseSpecification(String name)
    {
        super(name);
    }

    /**
     * This method runs through its contained specs, trying to run each spec. If
     * one is unsuccessful, it is checked for optionality. If optional, it will
     * continue. Otherwise, an exception is raised. See the sequence diagram.
     * 
     * @param curPos
     * @param resultingPos
     * @param input
     * @return plsqleditor.parser.framework.IParseResult
     * @throws ParseException
     * @roseuid 4315843000DA
     */
    public IParseResult parse(Position curPos, Position resultingPos, IInput input)
            throws ParseException
    {
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
            try
            {
                for (; numLoops < max; numLoops++)
                {
                    IParseResult localResult = spec.parse(curPos, tempEnd, input);
                    result.addParseResult(localResult);
                    curPos = tempEnd;
                    tempEnd = new Position(0, 0);
                }
            }
            catch (ParseException e)
            {
                continue;
            }
        }
        resultingPos.setOffset(curPos.getOffset());
        return result;
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
