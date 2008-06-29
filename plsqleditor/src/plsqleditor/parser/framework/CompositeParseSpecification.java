package plsqleditor.parser.framework;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;


public abstract class CompositeParseSpecification extends ParseSpecification
{
    /**
     * This is a list of the contained Parse Specifications in the form of
     * {@link ParseSpecificationHolder}s.
     */
    private List myParseSpecifications;

    public CompositeParseSpecification(String name)
    {
        super(name);
        myParseSpecifications = new ArrayList();
    }

    /**
     * @return plsqleditor.parser.framework.ParseSpecificationHolder[]
     */
    public ParseSpecificationHolder[] getContainedParseSpecifications()
    {
        return (ParseSpecificationHolder[]) myParseSpecifications
                .toArray(new ParseSpecificationHolder[myParseSpecifications.size()]);
    }

    /**
     * @param spec
     * @param min
     * @param max
     */
    public void addParseSpecification(IParseSpecification spec, int min, int max)
    {
        myParseSpecifications.add(new ParseSpecificationHolder(spec, min, max));
    }

    /**
     * This method checks that no infinite loop is contained in the specification.
     * This throws a ContainsLoopException if there is a possible loop.
     * A loop is determined to be possible if one of the contained specifications 
     * contains this spec (or a previous spec in the sequence supplied) and the 
     * specification that contains the duplicate occurs prior to any mandatory 
     * specification that does NOT contain a duplicate.
     * This is the default implementation for a composite parse specification.
     *
     * @param previouslyContainedParseSpecifications The list of <b>names</b> 
     *        of the previous possible calls in a sequence in which this specification 
     *        could being called.
     */
    public void checkForInfiniteLoop(Stack previouslyContainedSpecs)
    throws ContainsLoopException
    {
    	String name = getName();
        int index = previouslyContainedSpecs.indexOf(name);
        if (index != -1)
        {
            //boolean iShouldContinue = ((Boolean) previouslyContainedSpecs.elementAt(index + 1)).booleanValue();
            boolean iShouldContinue = previouslyContainedSpecs.lastIndexOf(Boolean.TRUE) > index;
            if (iShouldContinue)
            {
                return;
            }
            throw new ContainsLoopException("There is a possible infinite loop in this list:" +
            previouslyContainedSpecs + ", " + name);
        }
        Stack nextLevelCopy = (Stack) previouslyContainedSpecs.clone();
        nextLevelCopy.push(name);
        nextLevelCopy.push(new Boolean(false));
        ParseSpecificationHolder[] specs = getContainedParseSpecifications();
        boolean isSwitchedToThisLevel = false;
        for (int i = 0; i < specs.length; i++)
        {
            try
            {
                ParseSpecificationHolder specHolder = specs[i];
                IParseSpecification spec = specHolder.getParseSpecification();
                spec.checkForInfiniteLoop(nextLevelCopy);
                if (specHolder.getMin() > 0)
                {
                    if (!isSwitchedToThisLevel)
                    {
                        isSwitchedToThisLevel = true;
                        nextLevelCopy = (Stack) previouslyContainedSpecs.clone(); //new Stack();
                        nextLevelCopy.push(name);
                        nextLevelCopy.push(new Boolean(true));
                    }
                }
            }
            catch (ContainsLoopException e)
            {
                throw e;
            }
        }        
        return;        
    }

    public String toString()
    {
        return toString(3);
    }
    
    public String toString(int depth)
    {
        if (depth == 0)
        {
            return super.toString(0);
        }
        ParseSpecificationHolder [] specs = getContainedParseSpecifications();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < specs.length; i++)
        {
            sb.append(specs[i].toString(depth - 1));
            if (i < specs.length - 1)
            {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    public Set getFirstTokens()
    {
        Set set = new TreeSet(); 
        ParseSpecificationHolder [] specs = getContainedParseSpecifications();
        for (int i = 0; i < specs.length; i++)
        {
            IParseSpecification spec = specs[i].getParseSpecification();
            Set subSet = spec.getFirstTokens(); 
            for (Iterator it = subSet.iterator(); it.hasNext();)
            {
                String token = (String) it.next();
                if (!set.contains(token))
                {
                    set.add(token);
                }
            }
        }
        return set;
    }
}
