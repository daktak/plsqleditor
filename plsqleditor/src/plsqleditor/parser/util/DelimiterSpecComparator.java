package plsqleditor.parser.util;

import java.util.Comparator;

/**
 * This class represents 
 */
public class DelimiterSpecComparator implements Comparator
{
    /**
     */
    public DelimiterSpecComparator()
    {
        //
    }

    /**
     * A standard compareTo implementation.
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compare(Object o1, Object o2)
    {
        IDelimiterSpecification lhs = (IDelimiterSpecification) o1;
        IDelimiterSpecification rhs = (IDelimiterSpecification) o2;
        int result = rhs.length() - lhs.length();
        if (result == 0)
        {
            result = lhs.getString().compareTo(rhs.getString());
        }
        return result;
    }
}
