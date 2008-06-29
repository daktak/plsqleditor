//Source file: C:\\dev\\eclipse\\3.1\\eclipse\\workspace\\plsqleditor\\src\\plsqleditor\\parser\\framework\\ParseSpecificationHolder.java

package plsqleditor.parser.framework;

import org.eclipse.jface.text.Position;
import plsqleditor.parser.util.IInput;

public class ParseSpecificationHolder
{
    private IParseSpecification myParseSpecification;
    private int                 myMinOccurrences;

    /**
     * This is the maximum number of occurrences of the proxied parse
     * specification that may occur.
     */
    private int                 myMaxOccurrences;

    /**
     * 
     * @param spec
     * @param min
     * @param max
     * @roseuid 43157EC70186
     */
    public ParseSpecificationHolder(IParseSpecification spec, int min, int max)
    {
        myParseSpecification = spec;
        myMinOccurrences = min;
        myMaxOccurrences = max;
    }

    /**
     * This method proxies the call onto the contained parse specfication.
     * 
     * @param curPos
     * @param resultingPos
     * @param input
     * @return plsqleditor.parser.framework.IParseResult
     * @throws ParseException
     * 
     * @roseuid 43158042034B
     */
    public IParseResult parse(Position curPos, Position resultingPos, IInput input)
            throws ParseException
    {
        return myParseSpecification.parse(curPos, resultingPos, input);
    }

    /**
     * This method returns the result of getMin() == 0
     * 
     * @return boolean
     * 
     * @roseuid 4315811A0213
     */
    public boolean isOptional()
    {
        return getMin() == 0;
    }

    /**
     * This method gets the minimum number of times that the contained parse
     * specification may be run.
     * 
     * @return the minimum number of times that the contained parse
     *         specification may be run.
     * 
     * @roseuid 43157EE60271
     */
    public int getMin()
    {
        return myMinOccurrences;
    }

    /**
     * This method gets the maximum number of times that the contained parse
     * specification may be run.
     * 
     * @return the maximum number of times that the contained parse
     *         specification may be run.
     * 
     * @roseuid 43157EEB032C
     */
    public int getMax()
    {
        return myMaxOccurrences;
    }

    /**
     * This method gets the proxied parse specification.
     * 
     * @return the proxied parse specification.
     */
    protected IParseSpecification getParseSpecification()
    {
        return myParseSpecification;
    }

    public String toString()
    {
    	return toString(3);
    }

    public String toString(int depth)
    {
    	StringBuffer sb = new StringBuffer();
    	if (myMinOccurrences == 0)
    	{
    	    sb.append("[");
    	}
    	String parseSpecString = myParseSpecification.toString(depth);
        sb.append(parseSpecString);
    	if (myMinOccurrences == 0)
    	{
    	    sb.append("]");
    	}
        if (myMaxOccurrences > 1)
        {
            sb.append(" [").append(parseSpecString).append("]...");
        }
        //sb.append(" (").append(myMinOccurrences).append(", ")
        //.append(myMaxOccurrences).append(")");
        return sb.toString();
    }
}
